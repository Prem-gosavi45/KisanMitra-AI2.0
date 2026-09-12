import re
import glob

def find_texts(text):
    results = []
    idx = 0
    while True:
        idx = text.find('Text(', idx)
        if idx == -1:
            break
        # find matching parenthesis
        start = idx + 4
        paren_count = 0
        end = -1
        for i in range(start, len(text)):
            if text[i] == '(':
                paren_count += 1
            elif text[i] == ')':
                paren_count -= 1
                if paren_count == 0:
                    end = i
                    break
        if end != -1:
            inner = text[start+1:end]
            if 'color' not in inner and 'Toast' not in inner:
                line_no = text.count('\n', 0, idx) + 1
                results.append((line_no, inner))
        idx += 1
    return results

files = glob.glob('app/src/main/java/com/example/*.kt')
for f in files:
    with open(f, 'r') as file:
        content = file.read()
    results = find_texts(content)
    if results:
        print(f"--- {f} ---")
        for line, inner in results:
            print(f"Line {line}: {inner.strip()}")
