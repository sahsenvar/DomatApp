"""Soru için checkout edilmiş repodan ilgili kod/metin parçalarını toplar.

GitHub Models backend'i araç (tool) kullanamaz; modele dosya OKUTAMAYIZ. Bu yüzden
soruyla alakalı dosya parçalarını burada (hafif retrieval) bulup prompt'a enjekte
ederiz. copilot-cli backend'inde model zaten okuyabilir ama bağlam yine de yardımcı.

Büyük repolarda (ör. zad-android) patlamamak için tarama sınırlı: yalnız metin
uzantıları, büyük/binary dosyalar ve build/vendor dizinleri atlanır, toplam çıktı
karakter sınırına vurunca kesilir.
"""
import os
import re

# Aramaya dahil edilecek metin/kod uzantıları.
_TEXT_EXT = {
    ".kt", ".kts", ".java", ".xml", ".gradle", ".toml", ".py", ".js", ".ts",
    ".tsx", ".jsx", ".go", ".rb", ".rs", ".swift", ".mm", ".m", ".c", ".cc",
    ".cpp", ".h", ".hpp", ".md", ".yml", ".yaml", ".json", ".properties",
    ".sh", ".pro", ".cfg", ".sql", ".graphql", ".proto",
}
# Hiç girilmeyecek dizinler (+ nokta ile başlayanlar zaten atlanır).
_SKIP_DIRS = {
    "build", "gradle", "node_modules", "dist", "out", "vendor", "venv",
    "Pods", "DerivedData", "captures", "__pycache__",
}
# Türkçe + İngilizce gürültü kelimeleri ve jenerik kod sözcükleri.
_STOP = {
    "ve", "ile", "bir", "bu", "şu", "için", "ama", "veya", "the", "and", "for",
    "with", "this", "that", "what", "where", "when", "how", "why", "which",
    "does", "did", "are", "was", "were", "has", "have", "can", "could", "would",
    "should", "from", "into", "your", "you", "not", "but", "neden", "nasıl",
    "nerede", "hangi", "kaç", "reviewer", "guy", "function", "fonksiyon",
    "method", "metod", "class", "sınıf", "kod", "code", "var", "val", "fun",
    "nedir", "hani", "yani", "diye", "olan", "kullan", "kullanılıyor",
}

_IDENT = re.compile(r"[A-Za-zÇĞİÖŞÜçğıöşü_][A-Za-z0-9ÇĞİÖŞÜçğıöşü_]{2,}")
_CAMEL = re.compile(r"[A-Z]+(?=[A-Z][a-z])|[A-Z]?[a-z]+|[A-Z]+|[0-9]+")


def _split_camel(token):
    return [p.lower() for p in _CAMEL.findall(token) if len(p) >= 3]


def keywords(question):
    """Sorudan anahtar kelimeler: tanımlayıcılar + camelCase parçaları (stopword'süz)."""
    kws = set()
    for tok in _IDENT.findall(question or ""):
        low = tok.lower()
        if len(low) >= 3 and low not in _STOP:
            kws.add(low)
        # Yalnız kod-tanımlayıcısı gibi görünenleri parçala (PascalCase/camelCase/snake);
        # düz Türkçe kelimeleri ASCII parçalara bölüp gürültü üretme.
        if any(c.isupper() for c in tok) or "_" in tok:
            for part in _split_camel(tok):
                if part not in _STOP:
                    kws.add(part)
    return kws


def _iter_files(root, scan_limit):
    seen = 0
    for dirpath, dirnames, filenames in os.walk(root):
        dirnames[:] = [
            d for d in dirnames if d not in _SKIP_DIRS and not d.startswith(".")
        ]
        for name in filenames:
            if os.path.splitext(name)[1].lower() not in _TEXT_EXT:
                continue
            path = os.path.join(dirpath, name)
            try:
                if os.path.getsize(path) > 256_000:
                    continue
            except OSError:
                continue
            yield path
            seen += 1
            if seen >= scan_limit:
                return


def _read(path):
    try:
        with open(path, encoding="utf-8", errors="ignore") as fh:
            return fh.read()
    except OSError:
        return ""


def _score_file(text_low, path_low, kws):
    score = 0
    for kw in kws:
        score += text_low.count(kw)
        if kw in path_low:
            score += 8  # dosya adı/yol eşleşmesi güçlü sinyaldir
    return score


def _windows(lines, kws, max_lines, win):
    hits = []
    for i, line in enumerate(lines):
        ll = line.lower()
        s = sum(ll.count(kw) for kw in kws)
        if s:
            hits.append((i, s))
    if not hits:
        return []
    hits.sort(key=lambda x: x[1], reverse=True)
    ranges, chosen = [], set()
    for idx, _ in hits:
        lo, hi = max(0, idx - win), min(len(lines), idx + win + 1)
        ranges.append((lo, hi))
        chosen.update(range(lo, hi))
        if len(chosen) >= max_lines:
            break
    ranges.sort()
    merged = []
    for lo, hi in ranges:
        if merged and lo <= merged[-1][1]:
            merged[-1] = (merged[-1][0], max(merged[-1][1], hi))
        else:
            merged.append((lo, hi))
    return [
        "\n".join(f"{n + 1}: {lines[n]}" for n in range(lo, hi))
        for lo, hi in merged
    ]


def build(question, root, max_files=6, max_chars=9000, scan_limit=6000,
          win=18, per_file_lines=55):
    """Soruyla alakalı dosya parçalarını markdown bağlam bloğu olarak döndürür."""
    kws = keywords(question)
    if not kws:
        return ""
    scored = []
    for path in _iter_files(root, scan_limit):
        text = _read(path)
        if not text:
            continue
        rel = os.path.relpath(path, root)
        s = _score_file(text.lower(), rel.lower(), kws)
        if s > 0:
            scored.append((s, rel, text))
    if not scored:
        return ""
    scored.sort(key=lambda x: x[0], reverse=True)
    blocks, total = [], 0
    for _, rel, text in scored[:max_files]:
        wins = _windows(text.splitlines(), kws, per_file_lines, win)
        if not wins:
            continue
        block = f"### {rel}\n```\n" + "\n   ...\n".join(wins) + "\n```"
        if total + len(block) > max_chars:
            blocks.append(block[: max(0, max_chars - total)] + "\n[... kısaltıldı ...]\n```")
            break
        blocks.append(block)
        total += len(block)
    if not blocks:
        return ""
    file_list = ", ".join(rel for _, rel, _ in scored[:max_files])
    return (
        "## Repo bağlamı (soruyla alakalı dosya parçaları — checkout'tan, satır no'lu)\n"
        f"Taranıp en alakalı bulunanlar: {file_list}\n\n"
        + "\n\n".join(blocks)
        + "\n"
    )
