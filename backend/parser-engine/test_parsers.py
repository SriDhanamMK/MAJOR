import subprocess
import json

def test_parse(lang, code):
    payload = json.dumps({'action': 'parse', 'language': lang, 'sourceCode': code})
    p = subprocess.Popen(['python', 'backend/parser-engine/treesitter_parser.py'],
                         stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    out, err = p.communicate(input=payload)
    if err:
        print("STDERR:", err)
    return json.loads(out)

print("--- Testing Milestone 1: Real Parsing Proof of Concept ---")

# 1. Valid C
r = test_parse('C', 'int main() { int a = 10; return a; }')
print('Valid C status:', r['status'], '| Root:', r['root']['type'], '| Nodes:', r['nodeCount'])
assert r['status'] == 'SUCCESS'
assert r['root']['type'] == 'translation_unit'

# 2. Invalid C
r = test_parse('C', 'int main() { int a = ; return ; }')
print('Invalid C status:', r['status'], '| Diagnostics count:', len(r['diagnostics']), '| Detail:', r['diagnostics'][0]['message'])
assert r['status'] == 'SYNTAX_ERROR'
assert len(r['diagnostics']) > 0

# 3. Valid C++
r = test_parse('CPP', '#include <iostream>\nint main() { std::cout << 42; return 0; }')
print('Valid C++ status:', r['status'], '| Root:', r['root']['type'], '| Nodes:', r['nodeCount'])
assert r['status'] == 'SUCCESS'
assert r['root']['type'] == 'translation_unit'

# 4. Invalid C++
r = test_parse('CPP', 'class Foo { void bar() { return } };')
print('Invalid C++ status:', r['status'], '| Diagnostics count:', len(r['diagnostics']), '| Detail:', r['diagnostics'][0]['message'])
assert r['status'] == 'SYNTAX_ERROR'

# 5. Valid Python
r = test_parse('PYTHON', 'def fib(n):\n    if n <= 1:\n        return n\n    return fib(n-1) + fib(n-2)\n')
print('Valid Python status:', r['status'], '| Root:', r['root']['type'], '| Nodes:', r['nodeCount'])
assert r['status'] == 'SUCCESS'
assert r['root']['type'] == 'module'

# 6. Invalid Python
r = test_parse('PYTHON', 'def bad_func(:\n    return\n')
print('Invalid Python status:', r['status'], '| Diagnostics count:', len(r['diagnostics']), '| Detail:', r['diagnostics'][0]['message'])
assert r['status'] == 'SYNTAX_ERROR'

print("\n>>> MILESTONE 1 VERIFICATION PASSED: All 3 genuine Tree-sitter parsers work, extract ASTs, and detect errors accurately! <<<")
