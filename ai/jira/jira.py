#!/usr/bin/env python3
"""Jira REST helper for the DomatApp board (SCRUM) - used by the `jira-task` skill.

Talks to the REST API directly (sahsenvar.atlassian.net is on the environment allowlist), with
JIRA_BASIC_AUTH = base64("<email>:<api token>") from the environment. No third-party packages.

  jira.py show <KEY>                         summary, status, comment count, current watcher
  jira.py move <KEY> <status name>           transition, e.g. "Devam Ediyor"
  jira.py comments <KEY> [--since <id>]      comments as plain text, oldest first
  jira.py comment <KEY> <text>               post a comment (prefixed with BOT_MARKER)
  jira.py watch <KEY> <trigger_id> <session_id>
  jira.py unwatch <KEY>
  jira.py watcher <KEY>                      prints the stored watcher JSON, or nothing

A "watcher" is the issue property `claude-watch` = {"triggerId", "sessionId"}: the session-bound
Routine that the dispatcher Routine fires when someone comments on that issue.
"""
import base64
import json
import os
import sys
import urllib.error
import urllib.request

BASE_URL = os.environ.get("JIRA_BASE_URL", "https://sahsenvar.atlassian.net").rstrip("/")
PROPERTY = "claude-watch"
# Comments Claude posts carry this prefix. The Jira Automation rule and the dispatcher both skip
# them, otherwise a session answering on Jira (as Sahan's account) would wake itself.
BOT_MARKER = "[claude]"


def request(method, path, body=None):
    auth = os.environ.get("JIRA_BASIC_AUTH")
    if not auth:
        sys.exit("JIRA_BASIC_AUTH is not set")
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(BASE_URL + path, data=data, method=method)
    req.add_header("Authorization", "Basic " + auth)
    req.add_header("Accept", "application/json")
    if data is not None:
        req.add_header("Content-Type", "application/json")
    try:
        with urllib.request.urlopen(req) as resp:
            raw = resp.read()
            return json.loads(raw) if raw else None
    except urllib.error.HTTPError as e:
        if e.code == 404 and method == "GET" and "/properties/" in path:
            return None
        sys.exit(f"{method} {path} -> {e.code}: {e.read().decode(errors='replace')[:500]}")


def adf_text(node):
    """Flatten an Atlassian Document Format node to plain text."""
    if node is None:
        return ""
    if isinstance(node, str):
        return node
    kind = node.get("type")
    if kind == "text":
        return node.get("text", "")
    if kind == "mention":
        return node.get("attrs", {}).get("text", "")
    if kind == "hardBreak":
        return "\n"
    inner = "".join(adf_text(c) for c in node.get("content", []))
    return inner + "\n" if kind in ("paragraph", "heading", "listItem", "codeBlock") else inner


def adf_doc(text):
    return {
        "type": "doc",
        "version": 1,
        "content": [
            {"type": "paragraph", "content": [{"type": "text", "text": line}]} if line else {"type": "paragraph"}
            for line in text.split("\n")
        ],
    }


def watcher(key):
    prop = request("GET", f"/rest/api/3/issue/{key}/properties/{PROPERTY}")
    return prop["value"] if prop else None


def main(argv):
    if len(argv) < 2:
        sys.exit(__doc__)
    cmd, key = argv[0], argv[1]
    if cmd == "show":
        f = request("GET", f"/rest/api/3/issue/{key}?fields=summary,status,comment")["fields"]
        print(f"{key} | {f['summary']} | {f['status']['name']} | comments: {f['comment']['total']}")
        print("watcher:", json.dumps(watcher(key)))
    elif cmd == "move":
        target = argv[2]
        transitions = request("GET", f"/rest/api/3/issue/{key}/transitions")["transitions"]
        match = [t for t in transitions if target.lower() in (t["name"].lower(), t["to"]["name"].lower())]
        if not match:
            sys.exit(f"no transition to '{target}'; available: {[t['to']['name'] for t in transitions]}")
        request("POST", f"/rest/api/3/issue/{key}/transitions", {"transition": {"id": match[0]["id"]}})
        print(request("GET", f"/rest/api/3/issue/{key}?fields=status")["fields"]["status"]["name"])
    elif cmd == "comments":
        since = int(argv[3]) if len(argv) > 3 and argv[2] == "--since" else 0
        data = request("GET", f"/rest/api/3/issue/{key}/comment?orderBy=created&maxResults=100")
        for c in data["comments"]:
            if int(c["id"]) <= since:
                continue
            body = adf_text(c["body"]).strip()
            print(f"--- #{c['id']} {c['author']['displayName']} @ {c['created']}")
            print(body)
    elif cmd == "comment":
        text = argv[2] if argv[2].startswith(BOT_MARKER) else f"{BOT_MARKER} {argv[2]}"
        c = request("POST", f"/rest/api/3/issue/{key}/comment", {"body": adf_doc(text)})
        print("comment", c["id"])
    elif cmd == "watch":
        value = {"triggerId": argv[2], "sessionId": argv[3]}
        request("PUT", f"/rest/api/3/issue/{key}/properties/{PROPERTY}", value)
        print(json.dumps(watcher(key)))
    elif cmd == "unwatch":
        request("DELETE", f"/rest/api/3/issue/{key}/properties/{PROPERTY}")
        print("unwatched", key)
    elif cmd == "watcher":
        w = watcher(key)
        if w:
            print(json.dumps(w))
    else:
        sys.exit(__doc__)


if __name__ == "__main__":
    main(sys.argv[1:])
