"""Motor: review için katı JSON, mention için düz metin.

İki backend destekler:
- "models"     : GitHub Models REST API (urllib ile HTTP POST; ekstra key yok)
- "copilot-cli": Copilot CLI (copilot -p ... -s); Copilot seat'li token gerekir
RG_BACKEND ile seçilir (config.BACKEND).
"""
import json
import os
import subprocess
import urllib.error
import urllib.request

from . import config

PROMPT_DIR = os.path.join(os.path.dirname(__file__), "prompts")


def load_prompt(name):
    with open(os.path.join(PROMPT_DIR, name), encoding="utf-8") as fh:
        return fh.read()


def run_copilot(prompt, model, cwd, timeout=900):
    """copilot -p ... -s (yalnızca yanıt) çalıştırır, stdout döndürür."""
    cmd = [
        "copilot",
        "-p", prompt,
        "-s",
        "--allow-all-tools",
        "--no-color",
        "--no-ask-user",
        "--log-level", "error",
        "--model", model,
    ]
    proc = subprocess.run(
        cmd, cwd=cwd, capture_output=True, text=True, timeout=timeout
    )
    if proc.returncode != 0:
        raise RuntimeError(
            f"copilot exit {proc.returncode}\nSTDERR:\n{proc.stderr[-2000:]}"
        )
    return proc.stdout.strip()


def run_models(prompt, model, json_mode=False, timeout=300):
    """GitHub Models chat/completions çağrısı; asistan içeriğini döndürür."""
    return _models_call(prompt, model, json_mode=json_mode, minimal=False, timeout=timeout)


def _models_call(prompt, model, json_mode, minimal, timeout):
    token = config.models_token()
    if not token:
        raise RuntimeError(
            "Models token yok: GITHUB_TOKEN / GH_TOKEN / RG_MODELS_TOKEN gerekli "
            "(models:read izniyle)."
        )
    payload = {
        "model": model,
        "messages": [
            {"role": "system",
             "content": "You are Reviewer Guy, a meticulous senior code reviewer. "
                        "Follow the user's instructions and output format exactly."},
            {"role": "user", "content": prompt},
        ],
    }
    if not minimal:
        payload["temperature"] = 0.2
        if json_mode:
            payload["response_format"] = {"type": "json_object"}
    data = json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(
        config.MODELS_ENDPOINT, data=data, method="POST",
        headers={
            "Authorization": f"Bearer {token}",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "User-Agent": "reviewer-guy",
        },
    )
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            obj = json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as exc:
        detail = exc.read().decode("utf-8", "replace")[-1500:]
        # Bazı modeller response_format/temperature kabul etmez -> sade payload'la bir kez dene.
        if exc.code == 400 and not minimal:
            return _models_call(prompt, model, json_mode=False, minimal=True, timeout=timeout)
        raise RuntimeError(f"Models API HTTP {exc.code}: {detail}") from exc
    return obj["choices"][0]["message"]["content"].strip()


def _run(prompt, model, cwd, json_mode=False):
    """Aktif backend'e göre prompt'u çalıştırır."""
    if config.BACKEND == "models":
        return run_models(prompt, model, json_mode=json_mode)
    return run_copilot(prompt, model, cwd)


def _extract_json(text):
    text = text.strip()
    # Bazen ```json ... ``` ile sarmalayabilir; soyalım.
    if text.startswith("```"):
        text = text.strip("`")
        if text.lower().startswith("json"):
            text = text[4:]
        text = text.strip()
    try:
        return json.loads(text)
    except json.JSONDecodeError:
        pass
    start, end = text.find("{"), text.rfind("}")
    if start != -1 and end != -1 and end > start:
        return json.loads(text[start:end + 1])
    raise ValueError("Yanıttan JSON çıkarılamadı")


def review(prompt, model, cwd):
    """Review prompt'unu çalıştırır; JSON parse hatasında bir kez daha dener."""
    out = _run(prompt, model, cwd, json_mode=True)
    try:
        return _extract_json(out)
    except (ValueError, json.JSONDecodeError):
        strict = (
            prompt
            + "\n\nUYARI: Önceki yanıt geçerli JSON değildi. SADECE tek bir ham JSON "
            "nesnesi döndür; başka hiçbir metin, kod bloğu veya açıklama olmasın."
        )
        out = _run(strict, model, cwd, json_mode=True)
        return _extract_json(out)


def reply(prompt, model, cwd):
    """Mention yanıtı: düz metin döndürür."""
    return _run(prompt, model, cwd, json_mode=False)
