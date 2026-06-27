"""Unified diff parse: inline yorum atılabilecek geçerli (dosya, satır) konumları."""
import re

_HUNK = re.compile(r"^@@ -\d+(?:,\d+)? \+(\d+)(?:,(\d+))? @@")


def commentable_lines(diff_text):
    """path -> diff'te görünen YENİ-taraf satır numaraları kümesi.

    GitHub'a inline yorum yalnızca diff hunk'ında görünen (eklenen veya bağlam)
    satırlara atılabilir. Bu küme dışındaki bulgular özet gövdesine taşınır.
    """
    result = {}
    current = None
    new_line = 0
    for raw in diff_text.splitlines():
        if raw.startswith("diff --git"):
            current = None
            continue
        if raw.startswith("+++ "):
            # "+++ b/path" veya "+++ /dev/null"
            path = raw[4:]
            if path.startswith("b/"):
                path = path[2:]
            current = None if path == "/dev/null" else path
            if current is not None:
                result.setdefault(current, set())
            continue
        if raw.startswith("@@"):
            m = _HUNK.match(raw)
            if m:
                new_line = int(m.group(1))
            continue
        if current is None:
            continue
        if raw.startswith("+"):
            result[current].add(new_line)
            new_line += 1
        elif raw.startswith("-"):
            pass  # silinen satır; yeni tarafta yok
        elif raw.startswith("\\"):
            pass  # "\ No newline at end of file"
        else:
            # bağlam satırı (boşlukla başlar veya boş)
            result[current].add(new_line)
            new_line += 1
    return result


def nearest_commentable(lines_set, target):
    """target geçerli değilse en yakın geçerli satırı döndür (yoksa None)."""
    if not lines_set:
        return None
    if target in lines_set:
        return target
    return min(lines_set, key=lambda n: abs(n - target))
