"""Slack chat.postMessage — özel isim/avatar ile (chat:write.customize)."""
import json
import urllib.request
import urllib.error


def post_message(token, channel, text, blocks=None, username=None, icon_url=None):
    payload = {"channel": channel, "text": text}
    if blocks:
        payload["blocks"] = blocks
    if username:
        payload["username"] = username
    if icon_url:
        payload["icon_url"] = icon_url
    req = urllib.request.Request(
        "https://slack.com/api/chat.postMessage",
        data=json.dumps(payload).encode("utf-8"),
        method="POST",
        headers={
            "Authorization": f"Bearer {token}",
            "Content-Type": "application/json; charset=utf-8",
        },
    )
    try:
        with urllib.request.urlopen(req) as resp:
            body = json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as exc:
        detail = exc.read().decode("utf-8", "replace")
        raise RuntimeError(f"Slack HTTP {exc.code}: {detail}") from exc
    if not body.get("ok"):
        raise RuntimeError(f"Slack API hatası: {body.get('error')}")
    return body


def update_message(token, channel, ts, text, blocks=None):
    """Var olan bir mesajı yerinde düzenle (chat.update). username/icon_url yok sayılır;
    mesaj ilk gönderildiği kimlikle görünmeye devam eder."""
    payload = {"channel": channel, "ts": ts, "text": text}
    if blocks:
        payload["blocks"] = blocks
    req = urllib.request.Request(
        "https://slack.com/api/chat.update",
        data=json.dumps(payload).encode("utf-8"),
        method="POST",
        headers={
            "Authorization": f"Bearer {token}",
            "Content-Type": "application/json; charset=utf-8",
        },
    )
    try:
        with urllib.request.urlopen(req) as resp:
            body = json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as exc:
        detail = exc.read().decode("utf-8", "replace")
        raise RuntimeError(f"Slack HTTP {exc.code}: {detail}") from exc
    if not body.get("ok"):
        raise RuntimeError(f"Slack API hatası: {body.get('error')}")
    return body
