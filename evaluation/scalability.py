#!/usr/bin/env python3
"""Step 8: scalability workloads. Generates programs with N copies of a function body (syntax-checked),
then times POST /api/compare (wall clock, includes the Python parser subprocess) and records AST sizes.
Peak server memory is sampled with psutil if --server-pid is given (JVM + child parser process).

  python3 scalability.py --generate-only            # build and validate workloads, no server needed
  python3 scalability.py --server http://localhost:8080 --server-pid <PID> --repeats 5

NOTE: the API returns total time only. Separate parse / normalisation / TED timings need the
stage-level benchmark (see README, 'Limits'); this script does not fabricate them.
"""
import argparse, csv, json, os, subprocess, sys, tempfile, time, urllib.request
import psutil

FUNC = {
 'C': ('int f{n}(int *a, int len) {{\n    int acc = {n};\n    for (int i = 0; i < len; i++) {{\n        if (a[i] > acc && a[i] % 2 == 0) {{\n            acc += a[i] * 2;\n        }} else {{\n            acc -= 1;\n        }}\n    }}\n    return acc;\n}}\n\n',
       '#include <stdio.h>\n\n', 'int main() {{\n    int d[4] = {{1, 2, 3, 4}};\n    printf("%d\\n", f0(d, 4));\n    return 0;\n}}\n', '.c'),
 'CPP': ('int f{n}(const vector<int> &a) {{\n    int acc = {n};\n    for (int i = 0; i < (int)a.size(); i++) {{\n        if (a[i] > acc && a[i] % 2 == 0) {{\n            acc += a[i] * 2;\n        }} else {{\n            acc -= 1;\n        }}\n    }}\n    return acc;\n}}\n\n',
         '#include <iostream>\n#include <vector>\nusing namespace std;\n\n', 'int main() {{\n    vector<int> d = {{1, 2, 3, 4}};\n    cout << f0(d) << endl;\n    return 0;\n}}\n', '.cpp'),
 'PYTHON': ('def f{n}(a):\n    acc = {n}\n    for i in range(len(a)):\n        if a[i] > acc and a[i] % 2 == 0:\n            acc += a[i] * 2\n        else:\n            acc -= 1\n    return acc\n\n\n',
            '', 'print(f0([1, 2, 3, 4]))\n', '.py'),
}
SIZES = [1, 2, 4, 8, 16, 32, 64, 128]

def build(lang, n, rename=False):
    body, pre, main, _ = FUNC[lang]
    funcs = ''.join(body.format(n=i) for i in range(n))
    if rename:
        for i in range(n): funcs = funcs.replace(f'f{i}(', f'g{i}(')
        main = main.replace('f0', 'g0')
    return pre + funcs + main

def syntax_ok(lang, src):
    ext = FUNC[lang][3]
    with tempfile.TemporaryDirectory() as d:
        f = os.path.join(d, 'w' + ext); open(f, 'w').write(src)
        if lang == 'PYTHON': compile(src, f, 'exec'); return True
        cc = ['gcc', '-std=gnu11'] if lang == 'C' else ['g++', '-std=c++17']
        return subprocess.run(cc + ['-fsyntax-only', '-w', f], capture_output=True).returncode == 0

def main():
    ap = argparse.ArgumentParser(); ap.add_argument('--server', default='http://localhost:8080')
    ap.add_argument('--server-pid', type=int); ap.add_argument('--repeats', type=int, default=5)
    ap.add_argument('--generate-only', action='store_true'); ap.add_argument('--out', default='results/Scalability_Results.csv')
    a = ap.parse_args(); rows = []
    for lang in FUNC:
        for n in SIZES:
            p, q = build(lang, n), build(lang, n, rename=True)
            ok = syntax_ok(lang, p) and syntax_ok(lang, q)
            if a.generate_only: print(f'{lang:6} functions={n:4} lines={p.count(chr(10)):6} syntax_ok={ok}'); continue
            if not ok: print('skip invalid workload', lang, n); continue
            lat, nodes, peak = [], (0, 0), 0
            proc = psutil.Process(a.server_pid) if a.server_pid else None
            for _ in range(a.repeats):
                body = json.dumps(dict(language=lang, sourceA=p, sourceB=q)).encode()
                t0 = time.perf_counter()
                with urllib.request.urlopen(urllib.request.Request(a.server.rstrip('/') + '/api/compare', body,
                                            {'Content-Type': 'application/json'}), timeout=600) as r:
                    resp = json.loads(r.read())
                lat.append((time.perf_counter() - t0) * 1000); nodes = (resp['nodeCountA'], resp['nodeCountB'])
                if proc:
                    peak = max(peak, sum(c.memory_info().rss for c in [proc] + proc.children(recursive=True)))
            lat.sort()
            rows.append(dict(language=lang, functions=n, source_lines=p.count('\n'), nodes_a=nodes[0], nodes_b=nodes[1],
                             median_ms=lat[len(lat) // 2], min_ms=lat[0], max_ms=lat[-1], repeats=a.repeats,
                             sampled_peak_rss_mb=round(peak / 2**20, 1) if peak else '', score=resp['similarityScore'],
                             ted=resp['treeEditDistance']))
            print(rows[-1], flush=True)
    if rows:
        os.makedirs(os.path.dirname(a.out), exist_ok=True)
        with open(a.out, 'w', newline='') as f:
            w = csv.DictWriter(f, list(rows[0])); w.writeheader(); w.writerows(rows)

if __name__ == '__main__': main()
