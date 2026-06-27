"""PR review orkestratörü: diff -> Copilot -> bulgular -> GitHub review + status + Slack."""
import json
import os
import sys

from . import config, diff, engine, github_api, slack

SEV_EMOJI = {"critical": "❗", "warning": "⚠️", "suggestion": "💡"}
SEV_ORDER = ("critical", "warning", "suggestion")

# Model bazen 3 kademe dışı (eski/yakın) değer üretebilir; güvenli eşleme.
_SEV_SYNONYMS = {
    "high": "warning", "medium": "warning", "major": "warning", "warn": "warning",
    "blocker": "critical", "severe": "critical", "error": "critical", "crit": "critical",
    "low": "suggestion", "minor": "suggestion", "info": "suggestion",
    "nit": "suggestion", "style": "suggestion", "suggest": "suggestion",
}


def normalize_severity(raw):
    """Severity'yi 3 kademeye indir. Bilinmeyen/uydurma değer -> 'suggestion' (bloklamaz)."""
    sev = str(raw or "").lower().strip()
    sev = _SEV_SYNONYMS.get(sev, sev)
    return sev if sev in config.VALID_SEVERITIES else "suggestion"


def build_review_prompt(owner, repo, number, pr, files, diff_text):
    persona = engine.load_prompt("persona.md")
    task = engine.load_prompt("review_task.md")
    file_lines = "\n".join(
        f"- {f['filename']} (+{f.get('additions', 0)}/-{f.get('deletions', 0)}, {f.get('status', '')})"
        for f in files
    ) or "- (dosya listesi yok)"
    body = (pr.get("body") or "").strip() or "(açıklama yok)"
    author = (pr.get("user") or {}).get("login", "?")
    truncated = ""
    if len(diff_text) > config.MAX_DIFF_CHARS:
        diff_text = diff_text[: config.MAX_DIFF_CHARS]
        truncated = "\n\n[... diff kısaltıldı; tam bağlam için repodaki dosyaları oku ...]"
    return (
        f"{persona}\n\n{task}\n\n"
        f"## PR Bilgisi\n"
        f"- repo: {owner}/{repo}\n"
        f"- PR #{number}: {pr.get('title', '')}\n"
        f"- PR sahibi: @{author}\n"
        f"- Açıklama:\n{body}\n\n"
        f"## Değişen dosyalar\n{file_lines}\n\n"
        f"## Diff (unified)\n```diff\n{diff_text}{truncated}\n```\n"
    )


def classify(findings, diff_text):
    """Bulguları inline / diff-dışı olarak ayır ve severity listesini çıkar."""
    valid = diff.commentable_lines(diff_text)
    inline, offdiff, severities = [], [], []
    for f in findings:
        sev = normalize_severity(f.get("severity"))
        severities.append(sev)
        path = (f.get("path") or "").strip()
        title = (f.get("title") or "").strip()
        body = (f.get("body") or "").strip()
        try:
            target = int(f.get("line"))
        except (TypeError, ValueError):
            target = None
        comment_body = f"{SEV_EMOJI[sev]} **[{sev.upper()}] {title}**\n\n{body}"
        mapped = diff.nearest_commentable(valid.get(path), target) if target else None
        if mapped:
            inline.append({"path": path, "line": mapped, "side": "RIGHT", "body": comment_body})
        else:
            offdiff.append((sev, path, f.get("line"), title, body))
    return inline, offdiff, severities


def render_body(summary, severities, offdiff, block):
    counts = {s: severities.count(s) for s in SEV_ORDER}
    if block:
        head = "## 🛑 Değişiklik isteniyor"
    elif counts["suggestion"]:
        head = "## ✅ Onaylandı (öneri notlarıyla)"
    else:
        head = "## ✅ Onaylandı"
    lines = [
        head,
        "",
        summary or "_Özet yok._",
        "",
        f"**Bulgular:** ❗ {counts['critical']} critical · ⚠️ {counts['warning']} warning · 💡 {counts['suggestion']} suggestion",
    ]
    if offdiff:
        lines += ["", "### Diff dışı / genel notlar"]
        for sev, path, line, title, body in offdiff:
            loc = f"`{path}`" + (f":{line}" if line else "") if path else "_genel_"
            lines.append(f"- {SEV_EMOJI[sev]} **[{sev.upper()}]** {loc} — {title}: {body}")
    lines += ["", "---", "🤖 _Reviewer Guy — otomatik inceleme. Yanlışsam etiketleyip itiraz et._"]
    return "\n".join(lines)


def ci_summary(owner, repo, sha, token):
    """PR head'i için CI durumunu özetle — Reviewer Guy'ın KENDİ check/status'u hariç.
    Best-effort: App'in checks/statuses izni yoksa sessizce kısmi/None döner."""
    exclude = config.STATUS_CONTEXT.lower()
    passed = failed = pending = 0
    seen = False
    try:
        runs = github_api.list_check_runs(owner, repo, sha, token).get("check_runs", [])
        for r in runs:
            if exclude in (r.get("name") or "").lower():
                continue
            seen = True
            if r.get("status") != "completed":
                pending += 1
            elif r.get("conclusion") in ("success", "neutral", "skipped"):
                passed += 1
            else:
                failed += 1
    except Exception:
        pass
    try:
        st = github_api.get_combined_status(owner, repo, sha, token)
        for s in st.get("statuses", []):
            if exclude in (s.get("context") or "").lower():
                continue
            seen = True
            state = s.get("state")
            if state == "success":
                passed += 1
            elif state == "pending":
                pending += 1
            else:
                failed += 1
    except Exception:
        pass
    if not seen:
        return {"state": "none", "label": "— (CI yok)"}
    total = passed + failed + pending
    if failed:
        return {"state": "failure", "label": f"❌ {failed} başarısız"}
    if pending:
        return {"state": "pending", "label": f"⏳ {pending} sürüyor"}
    return {"state": "success", "label": f"✅ {passed}/{total} geçti"}


def build_slack_blocks(owner, repo, pr, slack_blurb, severities, block, ci):
    counts = {s: severities.count(s) for s in SEV_ORDER}
    verdict = "🛑 Değişiklik istendi" if block else "✅ Onaylandı"
    head_emoji = "🛑" if block else "✅"
    repo_full = f"{owner}/{repo}"
    num = pr["number"]
    title = (pr.get("title") or "(başlıksız)").strip()
    url = pr["html_url"]
    author = (pr.get("user") or {}).get("login", "?")
    author_md = f"<https://github.com/{author}|@{author}>"
    blurb = (slack_blurb or "").strip() or "_(yorum yok)_"
    blocks = [
        {"type": "header",
         "text": {"type": "plain_text", "text": f"{head_emoji} {repo_full} #{num}", "emoji": True}},
        {"type": "section",
         "text": {"type": "mrkdwn", "text": f"*<{url}|{title}>*"}},
        {"type": "section", "fields": [
            {"type": "mrkdwn", "text": f"*Sahibi:*\n{author_md}"},
            {"type": "mrkdwn", "text": f"*CI:*\n{ci['label']}"},
            {"type": "mrkdwn", "text": f"*Karar:*\n{verdict}"},
            {"type": "mrkdwn",
             "text": f"*Bulgular:*\n❗{counts['critical']} ⚠️{counts['warning']} 💡{counts['suggestion']}"},
        ]},
        {"type": "divider"},
        {"type": "section",
         "text": {"type": "mrkdwn", "text": f"🕶️ {blurb}"}},
        {"type": "context",
         "elements": [{"type": "mrkdwn", "text": f"Reviewer Guy · otomatik inceleme · <{url}|PR'ı aç>"}]},
    ]
    fallback = f"{head_emoji} {repo_full} #{num}: {title} — {verdict}"
    return blocks, fallback


def post_slack(owner, repo, pr, slack_blurb, severities, block, token):
    if not (config.SLACK_BOT_TOKEN and config.SLACK_CHANNEL):
        print("[slack] token/channel yok, atlanıyor.")
        return None
    ci = {"state": "none", "label": "— (CI yok)"}
    if token:
        try:
            ci = ci_summary(owner, repo, pr["head"]["sha"], token)
        except Exception as exc:
            print(f"[slack] CI durumu alınamadı: {exc}")
    blocks, fallback = build_slack_blocks(owner, repo, pr, slack_blurb, severities, block, ci)
    if config.DRY_RUN:
        print("[slack DRY_RUN] fallback=" + fallback)
        print(json.dumps(blocks, ensure_ascii=False, indent=2))
        return None
    return slack.post_message(
        config.SLACK_BOT_TOKEN, config.SLACK_CHANNEL, fallback, blocks=blocks,
        username=config.RG_USERNAME, icon_url=config.RG_ICON_URL or None,
    )


def main():
    owner, repo = config.owner_repo()
    number = int(os.environ["RG_PR_NUMBER"])
    token = config.GH_APP_TOKEN
    if not token and not config.DRY_RUN:
        sys.exit("GH_APP_TOKEN gerekli (DRY_RUN dışında).")

    pr = github_api.get_pull(owner, repo, number, token)
    files = github_api.list_pull_files(owner, repo, number, token)
    diff_text = github_api.get_pull_diff(owner, repo, number, token)
    head_sha = pr["head"]["sha"]

    prompt = build_review_prompt(owner, repo, number, pr, files, diff_text)
    result = engine.review(prompt, config.MODEL, os.getcwd())

    summary = (result.get("summary") or "").strip()
    slack_blurb = (result.get("slack_blurb") or "").strip()
    findings = result.get("findings") or []

    inline, offdiff, severities = classify(findings, diff_text)
    block = any(s in config.BLOCKING_SEVERITIES for s in severities)
    event = "REQUEST_CHANGES" if block else "APPROVE"
    body = render_body(summary, severities, offdiff, block)

    print(f"== Reviewer Guy == verdict={event} findings={len(findings)} "
          f"inline={len(inline)} offdiff={len(offdiff)}")

    if config.DRY_RUN:
        print("\n----- REVIEW BODY -----\n" + body)
        for c in inline:
            print(f"\n[inline] {c['path']}:{c['line']}\n{c['body']}")
        post_slack(owner, repo, pr, slack_blurb, severities, block, token)
        return 0

    try:
        github_api.create_review(owner, repo, number, token, body, event, inline)
    except RuntimeError as exc:
        # Built-in GITHUB_TOKEN ile GitHub Actions bir PR'ı APPROVE edemez (HTTP 422).
        # Özel "Reviewer Guy" App kimliği gelene kadar COMMENT'e düşürüp review'i
        # yine de yayınla (özet + inline yorumlar kaybolmasın). Yeşil sinyali commit
        # status sağlıyor; REQUEST_CHANGES zaten Actions token ile çalışır.
        if event == "APPROVE" and "not permitted to approve" in str(exc).lower():
            print("[uyarı] Actions token APPROVE edemiyor -> COMMENT olarak gönderiliyor.")
            github_api.create_review(owner, repo, number, token, body, "COMMENT", inline)
        else:
            raise
    github_api.create_commit_status(
        owner, repo, head_sha, token,
        state="failure" if block else "success",
        context=config.STATUS_CONTEXT,
        description=("Critical/Warning bulgu var" if block else "İnceleme geçti"),
        target_url=pr["html_url"],
    )
    post_slack(owner, repo, pr, slack_blurb, severities, block, token)

    if block and config.FAIL_JOB_ON_BLOCK:
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
