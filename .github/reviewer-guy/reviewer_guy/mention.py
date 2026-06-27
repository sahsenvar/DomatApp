"""@mention yanıtlayıcı: bir yorumda etiketlenince Reviewer Guy olarak cevap verir."""
import json
import os
import sys

from . import config, engine, github_api


def load_event():
    path = os.environ.get("GITHUB_EVENT_PATH", "")
    if not path or not os.path.exists(path):
        sys.exit("GITHUB_EVENT_PATH yok.")
    with open(path, encoding="utf-8") as fh:
        return json.load(fh)


def build_mention_prompt(owner, repo, number, asker, message, diff_text):
    persona = engine.load_prompt("persona.md")
    task = engine.load_prompt("mention_task.md")
    ctx = ""
    if diff_text:
        if len(diff_text) > config.MAX_DIFF_CHARS:
            diff_text = diff_text[: config.MAX_DIFF_CHARS] + "\n[... kısaltıldı ...]"
        ctx = f"\n## PR Diff (bağlam)\n```diff\n{diff_text}\n```\n"
    return (
        f"{persona}\n\n{task}\n\n"
        f"## Konum\n- repo: {owner}/{repo}\n- PR/issue #{number}\n"
        f"{ctx}\n"
        f"## @{asker} sana şunu yazdı\n{message}\n"
    )


def main():
    event = load_event()
    comment = event.get("comment", {})
    body = comment.get("body", "") or ""
    user = comment.get("user", {}) or {}

    # Bot döngüsünü engelle: bot yorumlarına cevap verme.
    if user.get("type") == "Bot":
        print("[mention] bot yorumu, atlanıyor.")
        return 0
    if config.RG_MENTION.lower() not in body.lower():
        print("[mention] tetikleyici yok, atlanıyor.")
        return 0

    issue = event.get("issue", {})
    number = issue.get("number")
    is_pr = "pull_request" in issue
    asker = user.get("login", "siz")
    owner, repo = config.owner_repo()
    token = config.GH_APP_TOKEN

    diff_text = ""
    if is_pr and token:
        try:
            diff_text = github_api.get_pull_diff(owner, repo, number, token)
        except RuntimeError as exc:
            print(f"[mention] diff alınamadı: {exc}")

    message = body.replace(config.RG_MENTION, "").strip() or "(boş)"
    prompt = build_mention_prompt(owner, repo, number, asker, message, diff_text)
    answer = engine.reply(prompt, config.MODEL, os.getcwd()).strip()
    reply_body = f"@{asker} {answer}"

    if config.DRY_RUN or not token:
        print("[mention DRY_RUN]\n" + reply_body)
        return 0

    github_api.post_issue_comment(owner, repo, number, token, reply_body)
    print(f"[mention] #{number} yanıtlandı.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
