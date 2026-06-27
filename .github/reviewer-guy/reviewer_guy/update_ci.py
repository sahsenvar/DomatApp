"""CI tamamlandığında Slack kartını yerinde güncelle (chat.update).

`check_suite: completed` olayıyla (yalnız default branch'teki workflow tetiklenir)
çalışır. Modeli YENİDEN çalıştırmaz: tracker'dan kart durumunu okur, sadece CI
özetini yeniden hesaplar ve değiştiyse aynı Slack mesajını günceller.

Ortam:
  RG_HEAD_SHA  -> check_suite.head_sha (tamamlanan CI'ın commit'i)
  RG_PR_NUMBER -> varsa check_suite.pull_requests[0].number (yoksa SHA'dan bulunur)
"""
import os
import sys

from . import config, github_api, review, slack, tracker


def _resolve_pr(owner, repo, token, head_sha):
    num = (os.environ.get("RG_PR_NUMBER") or "").strip()
    if num.isdigit():
        return int(num)
    if not head_sha:
        return None
    try:
        pulls = github_api.list_pulls_for_commit(owner, repo, head_sha, token)
    except Exception as exc:  # noqa: BLE001
        print(f"[ci-update] SHA'dan PR bulunamadı: {exc}")
        return None
    opens = [p for p in pulls if p.get("state") == "open"]
    chosen = opens or pulls
    return chosen[0]["number"] if chosen else None


def main():
    if not (config.SLACK_BOT_TOKEN and config.SLACK_CHANNEL):
        print("[ci-update] Slack yapılandırılmamış, çıkılıyor.")
        return 0
    owner, repo = config.owner_repo()
    # Bu job bot kimliğiyle bir şey "post" etmez; tracker düzenler + CI okur + Slack
    # günceller. Hepsi github.token ile çalışır (issues:write + checks:read garantili).
    token = config.models_token() or config.GH_APP_TOKEN
    head_sha = (os.environ.get("RG_HEAD_SHA") or "").strip()

    number = _resolve_pr(owner, repo, token, head_sha)
    if not number:
        print("[ci-update] İlişkili açık PR yok, çıkılıyor.")
        return 0

    cid, state = tracker.read(owner, repo, number, token)
    if not state or not state.get("ts"):
        print(f"[ci-update] PR #{number} için Slack kart kaydı yok, çıkılıyor.")
        return 0

    card_sha = state.get("head_sha") or head_sha
    # Eski bir commit'in CI'ı geç tamamlandıysa kartı bozma.
    if head_sha and state.get("head_sha") and head_sha != state["head_sha"]:
        print(f"[ci-update] Eski commit CI'ı ({head_sha[:7]} != {state['head_sha'][:7]}), atlanıyor.")
        return 0

    ci_token = config.models_token() or token
    try:
        ci = review.ci_summary(owner, repo, card_sha, ci_token)
    except Exception as exc:  # noqa: BLE001
        print(f"[ci-update] CI özeti alınamadı: {exc}")
        return 0

    if ci["label"] == state.get("ci_label"):
        print(f"[ci-update] CI değişmedi ({ci['label']}), atlanıyor.")
        return 0

    state["ci_label"], state["ci_state"] = ci["label"], ci["state"]
    blocks, fallback = review.build_slack_blocks(state, ci)
    if config.DRY_RUN:
        print(f"[ci-update DRY_RUN] PR #{number} -> {ci['label']}")
        return 0

    slack.update_message(config.SLACK_BOT_TOKEN, state["channel"], state["ts"], fallback, blocks)
    try:
        tracker.write(owner, repo, number, token, state, comment_id=cid)
    except Exception as exc:  # noqa: BLE001
        print(f"[ci-update] tracker güncellenemedi: {exc}")
    print(f"[ci-update] PR #{number} CI güncellendi -> {ci['label']}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
