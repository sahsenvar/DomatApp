"""Slack kart durumunu PR'a bağlama (kalıcılık).

Slack mesajının `ts`/`channel`'ı ve kartın yeniden çizilmesi için gereken küçük
durum (başlık, blurb, sayımlar, CI etiketi) bir PR yorumunun içine GİZLİ bir
base64 marker olarak yazılır:

    <!-- rg-slack <base64-json> -->

Böylece sonradan gelen bir olay (synchronize, check_suite tamamlandı) aynı Slack
mesajını bulup `chat.update` ile yerinde güncelleyebilir. base64 kullanıyoruz ki
JSON içindeki tırnak/emoji/`-->` gibi karakterler HTML yorumunu bozmasın.
"""
import base64
import json
import re

from . import github_api

_RE = re.compile(r"<!--\s*rg-slack\s+([A-Za-z0-9+/=]+)\s*-->")
_CAPTION = "<sub>🍅 Reviewer Guy · Slack kartı bu PR'a bağlı (CI bitince otomatik güncellenir).</sub>"


def _encode(state):
    raw = json.dumps(state, ensure_ascii=False, separators=(",", ":")).encode("utf-8")
    return base64.b64encode(raw).decode("ascii")


def _decode(b64):
    return json.loads(base64.b64decode(b64).decode("utf-8"))


def _body(state):
    return f"{_CAPTION}\n<!-- rg-slack {_encode(state)} -->"


def read(owner, repo, number, token):
    """(comment_id, state) döner; yoksa (None, None). Bozuk marker -> (id, None)."""
    try:
        comments = github_api.list_issue_comments(owner, repo, number, token)
    except Exception as exc:  # noqa: BLE001
        print(f"[tracker] yorumlar okunamadı: {exc}")
        return None, None
    for c in comments:
        m = _RE.search(c.get("body") or "")
        if not m:
            continue
        try:
            return c.get("id"), _decode(m.group(1))
        except Exception as exc:  # noqa: BLE001
            print(f"[tracker] marker çözülemedi: {exc}")
            return c.get("id"), None
    return None, None


def write(owner, repo, number, token, state, comment_id=None):
    """State'i tracker yorumuna yaz (varsa düzenle, yoksa oluştur)."""
    if comment_id is None:
        comment_id, _ = read(owner, repo, number, token)
    body = _body(state)
    if comment_id:
        return github_api.update_issue_comment(owner, repo, comment_id, token, body)
    return github_api.post_issue_comment(owner, repo, number, token, body)
