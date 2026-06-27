"""Ortam değişkenlerinden çalışma zamanı yapılandırması."""
import os

# Beyin (LLM) backend'i: "models" (GitHub Models API, ekstra key gerektirmez)
# veya "copilot-cli" (Copilot CLI; Copilot seat'li bir token gerekir).
BACKEND = os.environ.get("RG_BACKEND", "models")

# Hangi modelle inceleme yapılacak. Backend'e göre makul varsayılan.
_DEFAULT_MODEL = "openai/gpt-4.1" if BACKEND == "models" else "gpt-5.5"
MODEL = os.environ.get("RG_MODEL", _DEFAULT_MODEL)

# GitHub Models inference endpoint'i.
MODELS_ENDPOINT = os.environ.get(
    "RG_MODELS_ENDPOINT", "https://models.github.ai/inference/chat/completions"
)


def models_token():
    """Models API için token (çağrı anında okunur; lokal/CI farkını yönetir)."""
    return (
        os.environ.get("RG_MODELS_TOKEN")
        or os.environ.get("GITHUB_TOKEN")
        or os.environ.get("GH_TOKEN", "")
    )


# owner/repo (GitHub Actions otomatik sağlar).
GITHUB_REPOSITORY = os.environ.get("GITHUB_REPOSITORY", "")

# Yorumları/review'ı bu kimlikle post ederiz: GitHub App installation token'ı.
# (Beyin = Models/Copilot token'ı; yüz = bu token. Test'te GITHUB_TOKEN de olabilir.)
GH_APP_TOKEN = os.environ.get("GH_APP_TOKEN", "")

# Slack
SLACK_BOT_TOKEN = os.environ.get("SLACK_BOT_TOKEN", "")
SLACK_CHANNEL = os.environ.get("SLACK_CHANNEL", "")

# Reviewer Guy'ın Slack'te görüneceği isim/avatar (chat:write.customize ile).
RG_USERNAME = os.environ.get("RG_USERNAME", "Reviewer Guy")
RG_ICON_URL = os.environ.get("RG_ICON_URL", "")

# Mention tetikleyici (kullanıcı bunu yorumda yazınca cevap verir).
RG_MENTION = os.environ.get("RG_MENTION", "@reviewer-guy")

# Commit status context'i (branch protection bunu "required" yapabilir).
STATUS_CONTEXT = os.environ.get("RG_STATUS_CONTEXT", "Reviewer Guy")

# Diff'i prompt'a koyarken üst sınır (token patlamasını önler).
# GitHub Models ücretsiz katmanı istek başına ~8000 token sınırlar; küçük tutuyoruz.
MAX_DIFF_CHARS = int(os.environ.get("RG_MAX_DIFF_CHARS", "14000"))

# Models backend'inde tüm prompt için kademeli karakter bütçeleri (413'te küçülerek tekrar dener).
MODELS_PROMPT_BUDGETS = [
    int(x) for x in os.environ.get("RG_MODELS_PROMPT_BUDGETS", "20000,12000,8000").split(",")
]

# Hiçbir şey post etme, sadece ne yapacağını yazdır.
DRY_RUN = os.environ.get("RG_DRY_RUN", "") == "1"

# Bloklarken (critical/high) iş job'ını da fail et (status'a ek olarak).
FAIL_JOB_ON_BLOCK = os.environ.get("RG_FAIL_JOB_ON_BLOCK", "") == "1"

BLOCKING_SEVERITIES = {"critical", "high"}
VALID_SEVERITIES = {"critical", "high", "warning"}


def owner_repo():
    owner, _, repo = GITHUB_REPOSITORY.partition("/")
    return owner, repo
