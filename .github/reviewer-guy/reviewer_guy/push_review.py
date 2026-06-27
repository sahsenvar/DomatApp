"""Push incelemesi: bir commit aralığının diff'ini inceler, commit comment + status yazar.

PR olmadığı için satır içi (inline) review yapılmaz; tüm bulgular tek bir commit
yorumunda toplanır. PR akışı için reviewer_guy.review kullanılır.
"""
import os
import subprocess
import sys

from . import config, diff, engine, github_api, review


def _git_diff(before, after):
    zero = not before or set(before) == {"0"}
    if zero:
        # Yeni branch / ilk push: son commit'i incele.
        rng = f"{after}~1..{after}"
    else:
        rng = f"{before}..{after}"
    out = subprocess.run(["git", "diff", rng], capture_output=True, text=True)
    if out.returncode != 0 or not out.stdout.strip():
        # Fallback: tek commit'i göster.
        out = subprocess.run(["git", "show", "--format=", after],
                             capture_output=True, text=True)
    return out.stdout


def _commit_messages(before, after):
    zero = not before or set(before) == {"0"}
    rng = f"{after}~1..{after}" if zero else f"{before}..{after}"
    out = subprocess.run(["git", "log", "--no-merges", "--format=- %s", rng],
                         capture_output=True, text=True)
    return out.stdout.strip() or "(commit mesajı yok)"


def main():
    owner, repo = config.owner_repo()
    before = os.environ.get("RG_BEFORE_SHA", "")
    after = os.environ.get("RG_AFTER_SHA", "")
    branch = os.environ.get("RG_BRANCH", "")
    token = config.GH_APP_TOKEN
    if not token and not config.DRY_RUN:
        sys.exit("GH_APP_TOKEN gerekli (DRY_RUN dışında).")
    if not after:
        sys.exit("RG_AFTER_SHA gerekli.")

    diff_text = _git_diff(before, after)
    if not diff_text.strip():
        print("[push] diff boş, atlanıyor.")
        return 0

    paths = sorted(diff.commentable_lines(diff_text).keys())
    files = [{"filename": p, "status": "modified"} for p in paths]
    compare_url = f"https://github.com/{owner}/{repo}/compare/{before[:12]}...{after[:12]}"
    pseudo_pr = {
        "title": f"Push → {branch or 'branch'}",
        "body": _commit_messages(before, after),
        "number": 0,
        "html_url": compare_url,
        "head": {"sha": after},
    }

    prompt = review.build_review_prompt(owner, repo, 0, pseudo_pr, files, diff_text)
    result = engine.review(prompt, config.MODEL, os.getcwd())

    summary = (result.get("summary") or "").strip()
    findings = result.get("findings") or []

    _, _, severities = review.classify(findings, diff_text)
    block = any(s in config.BLOCKING_SEVERITIES for s in severities)

    # Push'ta hepsini gövdeye topla (inline yok).
    all_as_off = []
    for f in findings:
        sev = review.normalize_severity(f.get("severity"))
        all_as_off.append((sev, (f.get("path") or "").strip(), f.get("line"),
                            (f.get("title") or "").strip(), (f.get("body") or "").strip()))
    body = review.render_body(summary, severities, all_as_off, block)
    body = f"### 🍅 Push incelemesi (`{after[:7]}`)\n\n" + body

    counts = {s: severities.count(s) for s in review.SEV_ORDER}
    print(f"== Reviewer Guy (push) == block={block} findings={len(findings)} "
          f"crit={counts['critical']} warn={counts['warning']} sugg={counts['suggestion']}")

    if config.DRY_RUN:
        print("\n----- COMMIT COMMENT -----\n" + body)
        return 0

    github_api.post_commit_comment(owner, repo, after, token, body)
    github_api.create_commit_status(
        owner, repo, after, token,
        state="failure" if block else "success",
        context=config.STATUS_CONTEXT,
        description=("Critical/Warning bulgu var" if block else "İnceleme geçti"),
        target_url=compare_url,
    )
    # Not: Slack'e SADECE PR açılışında kart gönderilir (review.py). Doğrudan
    # push'ta (açık PR'sız) Slack'e yazmıyoruz — gürültü olmasın.

    if block and config.FAIL_JOB_ON_BLOCK:
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
