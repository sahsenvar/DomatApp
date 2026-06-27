"""@mention yanıtlayıcı: bir yorumda etiketlenince Reviewer Guy olarak cevap verir."""
import json
import os
import sys

from . import config, engine, github_api, repo_context


def load_event():
    path = os.environ.get("GITHUB_EVENT_PATH", "")
    if not path or not os.path.exists(path):
        sys.exit("GITHUB_EVENT_PATH yok.")
    with open(path, encoding="utf-8") as fh:
        return json.load(fh)


def _clip(text, limit, label):
    if text and len(text) > limit:
        return text[:limit] + f"\n[... {label} kısaltıldı ...]"
    return text


def _review_comment_context(comment):
    """Inline review yorumu ise: dosya yolu + diff hunk (en alakalı yerel bağlam)."""
    path = comment.get("path")
    if not path:
        return ""
    head = f"## Yorumun bağlı olduğu yer\n- dosya: `{path}`"
    line = comment.get("line") or comment.get("original_line")
    if line:
        head += f" (satır ~{line})"
    hunk = comment.get("diff_hunk", "")
    if hunk:
        head += f"\n```diff\n{hunk[-1500:]}\n```"
    return head + "\n"


def build_mention_prompt(owner, repo, number, asker, message,
                         diff_text, repo_ctx, code_ctx):
    persona = engine.load_prompt("persona.md")
    task = engine.load_prompt("mention_task.md")
    parts = [persona, task, f"## Konum\n- repo: {owner}/{repo}\n- PR #{number}\n"]
    if code_ctx:
        parts.append(code_ctx)
    if diff_text:
        diff_text = _clip(diff_text, config.MAX_DIFF_CHARS, "diff")
        parts.append(f"## PR Diff (bağlam)\n```diff\n{diff_text}\n```")
    if repo_ctx:
        parts.append(repo_ctx)
    parts.append(f"## @{asker} sana şunu yazdı\n{message}\n")
    return "\n\n".join(parts)


def main():
    event = load_event()
    event_name = os.environ.get("GITHUB_EVENT_NAME", "")
    comment = event.get("comment", {}) or {}
    body = comment.get("body", "") or ""
    user = comment.get("user", {}) or {}

    # Bot döngüsünü engelle: bot yorumlarına (kendi cevaplarımıza) cevap verme.
    if user.get("type") == "Bot":
        print("[mention] bot yorumu, atlanıyor.")
        return 0
    if config.RG_MENTION.lower() not in body.lower():
        print("[mention] tetikleyici yok, atlanıyor.")
        return 0

    is_review = event_name == "pull_request_review_comment"
    if is_review:
        number = (event.get("pull_request") or {}).get("number")
    else:  # issue_comment
        issue = event.get("issue", {}) or {}
        number = issue.get("number")
        if "pull_request" not in issue:
            print("[mention] PR değil (issue), atlanıyor.")
            return 0
    if not number:
        print("[mention] PR numarası bulunamadı, atlanıyor.")
        return 0

    asker = user.get("login", "siz")
    owner, repo = config.owner_repo()
    token = config.GH_APP_TOKEN

    diff_text = ""
    if token:
        try:
            diff_text = github_api.get_pull_diff(owner, repo, number, token)
        except RuntimeError as exc:
            print(f"[mention] diff alınamadı: {exc}")

    message = body.replace(config.RG_MENTION, "").strip() or "(boş)"

    # Repo'dan soruyla alakalı parçaları topla (Models backend dosya okuyamaz).
    repo_ctx = ""
    try:
        repo_ctx = repo_context.build(message, os.getcwd())
        if repo_ctx:
            print(f"[mention] repo bağlamı eklendi ({len(repo_ctx)} karakter).")
        else:
            print("[mention] repo bağlamı: alakalı dosya bulunamadı.")
    except Exception as exc:  # retrieval asla cevabı engellemesin
        print(f"[mention] repo bağlamı atlandı: {exc}")

    code_ctx = _review_comment_context(comment) if is_review else ""

    prompt = build_mention_prompt(
        owner, repo, number, asker, message, diff_text, repo_ctx, code_ctx
    )
    answer = engine.reply(prompt, config.MODEL, os.getcwd()).strip()
    reply_body = f"@{asker} {answer}"

    if config.DRY_RUN or not token:
        print("[mention DRY_RUN]\n" + reply_body)
        return 0

    if is_review:
        github_api.reply_pull_review_comment(
            owner, repo, number, comment.get("id"), token, reply_body
        )
        print(f"[mention] PR #{number} inline thread yanıtlandı.")
    else:
        github_api.post_issue_comment(owner, repo, number, token, reply_body)
        print(f"[mention] PR #{number} yorumu yanıtlandı.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
