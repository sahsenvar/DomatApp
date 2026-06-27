"""PR review orkestratörü: diff -> Copilot -> bulgular -> GitHub review + status + Slack."""
import os
import sys

from . import config, diff, engine, github_api, slack

SEV_EMOJI = {"critical": "🛑", "high": "⚠️", "warning": "🟡"}


def build_review_prompt(owner, repo, number, pr, files, diff_text):
    persona = engine.load_prompt("persona.md")
    task = engine.load_prompt("review_task.md")
    file_lines = "\n".join(
        f"- {f['filename']} (+{f.get('additions', 0)}/-{f.get('deletions', 0)}, {f.get('status', '')})"
        for f in files
    ) or "- (dosya listesi yok)"
    body = (pr.get("body") or "").strip() or "(açıklama yok)"
    truncated = ""
    if len(diff_text) > config.MAX_DIFF_CHARS:
        diff_text = diff_text[: config.MAX_DIFF_CHARS]
        truncated = "\n\n[... diff kısaltıldı; tam bağlam için repodaki dosyaları oku ...]"
    return (
        f"{persona}\n\n{task}\n\n"
        f"## PR Bilgisi\n"
        f"- repo: {owner}/{repo}\n"
        f"- PR #{number}: {pr.get('title', '')}\n"
        f"- Açıklama:\n{body}\n\n"
        f"## Değişen dosyalar\n{file_lines}\n\n"
        f"## Diff (unified)\n```diff\n{diff_text}{truncated}\n```\n"
    )


def classify(findings, diff_text):
    """Bulguları inline / diff-dışı olarak ayır ve severity listesini çıkar."""
    valid = diff.commentable_lines(diff_text)
    inline, offdiff, severities = [], [], []
    for f in findings:
        sev = str(f.get("severity", "")).lower().strip()
        if sev not in config.VALID_SEVERITIES:
            sev = "warning"
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
    counts = {s: severities.count(s) for s in ("critical", "high", "warning")}
    if block:
        head = "## 🛑 Değişiklik isteniyor"
    elif counts["warning"]:
        head = "## ✅ Onaylandı (ufak notlarla)"
    else:
        head = "## ✅ Onaylandı"
    lines = [
        head,
        "",
        summary or "_Özet yok._",
        "",
        f"**Bulgular:** 🛑 {counts['critical']} critical · ⚠️ {counts['high']} high · 🟡 {counts['warning']} warning",
    ]
    if offdiff:
        lines += ["", "### Diff dışı / genel notlar"]
        for sev, path, line, title, body in offdiff:
            loc = f"`{path}`" + (f":{line}" if line else "") if path else "_genel_"
            lines.append(f"- {SEV_EMOJI[sev]} **[{sev.upper()}]** {loc} — {title}: {body}")
    lines += ["", "---", "🤖 _Reviewer Guy — otomatik inceleme. Yanlışsam etiketleyip itiraz et._"]
    return "\n".join(lines)


def post_slack(pr, repo_full, slack_blurb, severities, block):
    if not (config.SLACK_BOT_TOKEN and config.SLACK_CHANNEL):
        print("[slack] token/channel yok, atlanıyor.")
        return None
    counts = {s: severities.count(s) for s in ("critical", "high", "warning")}
    verdict = "🛑 Değişiklik istendi" if block else "✅ Onaylandı"
    head_emoji = "🛑" if block else "✅"
    text = (
        f"{head_emoji} <{pr['html_url']}|{repo_full} #{pr['number']}: {pr.get('title', '')}>\n"
        f"{slack_blurb}\n"
        f"_{verdict} · 🛑{counts['critical']} ⚠️{counts['high']} 🟡{counts['warning']}_"
    )
    if config.DRY_RUN:
        print("[slack DRY_RUN]\n" + text)
        return None
    return slack.post_message(
        config.SLACK_BOT_TOKEN, config.SLACK_CHANNEL, text,
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
        post_slack(pr, f"{owner}/{repo}", slack_blurb, severities, block)
        return 0

    github_api.create_review(owner, repo, number, token, body, event, inline)
    github_api.create_commit_status(
        owner, repo, head_sha, token,
        state="failure" if block else "success",
        context=config.STATUS_CONTEXT,
        description=("Critical/high bulgular var" if block else "İnceleme geçti"),
        target_url=pr["html_url"],
    )
    post_slack(pr, f"{owner}/{repo}", slack_blurb, severities, block)

    if block and config.FAIL_JOB_ON_BLOCK:
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
