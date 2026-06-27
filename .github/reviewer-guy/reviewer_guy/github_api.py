"""Bağımlılıksız (urllib) GitHub REST istemcisi. App installation token kullanır."""
import json
import urllib.request
import urllib.error

API = "https://api.github.com"


def _request(method, url, token, data=None, accept="application/vnd.github+json"):
    headers = {
        "Accept": accept,
        "Authorization": f"Bearer {token}",
        "X-GitHub-Api-Version": "2022-11-28",
        "User-Agent": "reviewer-guy",
    }
    body = None
    if data is not None:
        body = json.dumps(data).encode("utf-8")
        headers["Content-Type"] = "application/json"
    req = urllib.request.Request(url, data=body, method=method, headers=headers)
    try:
        with urllib.request.urlopen(req) as resp:
            raw = resp.read().decode("utf-8")
            if "diff" in accept or "raw" in accept:
                return raw
            return json.loads(raw) if raw else {}
    except urllib.error.HTTPError as exc:
        detail = exc.read().decode("utf-8", "replace")
        raise RuntimeError(f"GitHub {method} {url} -> HTTP {exc.code}: {detail}") from exc


def get_pull(owner, repo, number, token):
    return _request("GET", f"{API}/repos/{owner}/{repo}/pulls/{number}", token)


def get_pull_diff(owner, repo, number, token):
    return _request(
        "GET",
        f"{API}/repos/{owner}/{repo}/pulls/{number}",
        token,
        accept="application/vnd.github.v3.diff",
    )


def list_pull_files(owner, repo, number, token):
    files, page = [], 1
    while True:
        batch = _request(
            "GET",
            f"{API}/repos/{owner}/{repo}/pulls/{number}/files?per_page=100&page={page}",
            token,
        )
        if not batch:
            break
        files.extend(batch)
        if len(batch) < 100:
            break
        page += 1
    return files


def create_review(owner, repo, number, token, body, event, comments):
    payload = {"body": body, "event": event}
    if comments:
        payload["comments"] = comments
    return _request(
        "POST", f"{API}/repos/{owner}/{repo}/pulls/{number}/reviews", token, data=payload
    )


def post_issue_comment(owner, repo, number, token, body):
    return _request(
        "POST",
        f"{API}/repos/{owner}/{repo}/issues/{number}/comments",
        token,
        data={"body": body},
    )


def post_commit_comment(owner, repo, sha, token, body):
    return _request(
        "POST",
        f"{API}/repos/{owner}/{repo}/commits/{sha}/comments",
        token,
        data={"body": body},
    )


def create_commit_status(owner, repo, sha, token, state, context, description, target_url=None):
    payload = {"state": state, "context": context, "description": description[:140]}
    if target_url:
        payload["target_url"] = target_url
    return _request(
        "POST", f"{API}/repos/{owner}/{repo}/statuses/{sha}", token, data=payload
    )
