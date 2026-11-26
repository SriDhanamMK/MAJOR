#!/usr/bin/env python3
"""Generate the labelled program-pair dataset.

Positive pair  (label 1): one program is a transformed copy of the other.
Negative pair  (label 0): independently written programs (same task or different task).

Every program is compiled / byte-compiled (syntax check) and executed. A transformed
copy is accepted only if it produces the same stdout as the original it came from.
Dev and test splits are made by *base program*, never by pair.

Usage: python3 make_dataset.py [--out dataset]
"""
import argparse, csv, hashlib, itertools, json, os, random, re, subprocess, sys, tempfile
from programs import C, CPP, PY, C_PRELUDE, CPP_PRELUDE, PY_PRELUDE

SEED = 20260226
LANGS = {'C': (C, C_PRELUDE, '.c', '//'), 'CPP': (CPP, CPP_PRELUDE, '.cpp', '//'),
         'PYTHON': (PY, PY_PRELUDE, '.py', '#')}
MARK = '@R'
INIT_KINDS = ['lit', 'arith', 'call', 'lit', 'arith', 'lit', 'call', 'arith', 'lit', 'lit']


# ------------------------------------------------------------ rendering ----
def split_args(s):
    out, depth, cur = [], 0, ''
    for ch in s:
        if ch in '([': depth += 1
        if ch in ')]': depth -= 1
        if ch == ',' and depth == 0:
            out.append(cur.strip()); cur = ''
        else:
            cur += ch
    out.append(cur.strip())
    return out


def expand_macros(src, lang, variant):
    div = '//' if lang == 'PYTHON' else '/'
    while True:
        m = re.search(r'@(A1|A2|AM)\(', src)
        if not m:
            return src
        i, depth = m.end(), 1
        while depth:
            depth += {'(': 1, ')': -1}.get(src[i], 0); i += 1
        args = [expand_macros(a, lang, variant) for a in split_args(src[m.end():i - 1])]
        k = m.group(1)
        if k == 'A1': rep = f'1 + {args[0]}' if variant else f'{args[0]} + 1'
        elif k == 'A2': rep = f'{args[0]} + {args[0]}' if variant else f'{args[0]} * 2'
        else:
            a, b = args
            rep = f'{a} - {a} {div} {b} * {b} == 0' if variant else f'{a} % {b} == 0'
        src = src[:m.start()] + rep + src[i:]


def init_block(lang, rng, indent):
    cm = LANGS[lang][3]
    vals, lines = rng.sample(range(11, 97), 40), []
    for n, kind in enumerate(INIT_KINDS):
        v = vals.pop()
        if kind == 'lit': rhs = str(v)
        elif kind == 'arith': rhs = f'{vals.pop() % 9 + 2} * {vals.pop() % 9 + 2} - {vals.pop() % 7 + 1}'
        else: rhs = f'sq({vals.pop() % 9 + 2})'
        decl = f'int cfg{n} = {rhs};' if lang != 'PYTHON' else f'cfg{n} = {rhs}'
        lines.append(f'{indent}{decl}  {cm}{MARK}')
    return '\n'.join(lines)


def chk_line(lang):
    s = ' + '.join(f'cfg{n}' for n in range(10))
    if lang == 'C': return f'printf("chk %d\\n", {s});'
    if lang == 'CPP': return f'cout << "chk " << ({s}) << endl;'
    return f'print("chk", {s})'


def render(base_id, lang, text, arith_variant=False, seed_tag=''):
    """Expand template markers into a concrete program (with @R markers kept)."""
    prelude = LANGS[lang][1]
    rng = random.Random(f'{SEED}-{base_id}-{lang}-{seed_tag}')
    out = []
    for line in text.split('\n'):
        ind = re.match(r'\s*', line).group(0)
        if line.strip() == '@INIT@': out.append(init_block(lang, rng, ind))
        elif line.strip() == '@CHK@': out.append(ind + chk_line(lang))
        else: out.append(line)
    return prelude + expand_macros('\n'.join(out), lang, arith_variant)


def strip_marks(src):
    return re.sub(r'[ \t]*(//|#)@R[ \t]*$', '', src, flags=re.M)


# ------------------------------------------------------- transformations ----
def lines_of(s): return s.split('\n')
def indent_of(l): return len(l) - len(l.lstrip(' \t'))


def c_block_end(L, start):
    depth = 0
    for k in range(start, len(L)):
        depth += L[k].count('{') - L[k].count('}')
        if depth == 0 and k >= start:
            return k
    return None


def py_block_end(L, start):
    ind = indent_of(L[start]); k = start + 1; last = start
    while k < len(L):
        if L[k].strip() == '': k += 1; continue
        if indent_of(L[k]) <= ind: break
        last = k; k += 1
    return last


def t_formatting(src, lang, kind):
    L = lines_of(src); out = []
    if lang == 'PYTHON':
        for l in L:
            n = indent_of(l); lvl = n // 4; body = l.strip()
            if kind == 'indent2': out.append(' ' * (lvl * 2) + body if body else l)
            elif kind == 'tabs': out.append('\t' * lvl + body if body else l)
            else:
                if '"' not in body and "'" not in body:
                    body = body.replace('(', '( ').replace(')', ' )').replace('( )', '()')
                out.append(' ' * n + body)
                if body.endswith(':'): out.append('')
        return '\n'.join(out)
    for idx, l in enumerate(L):
        body = l.strip(); n = indent_of(l)
        if kind == 'allman':
            if body.endswith('{') and not body.startswith('//') and body not in ('{',):
                head = body[:-1].rstrip(); sp = ' ' * n
                if head.startswith('}'):
                    out += [sp + '}', sp + head[1:].strip(), sp + '{']
                elif head:
                    out += [sp + head, sp + '{']
                else: out.append(l)
            else: out.append(l)
        elif kind == 'tabs':
            out.append('\t' * (n // 4) + body if body else l)
            if body.endswith(';') and idx % 3 == 0: out.append('')
        else:  # compact operators
            if '"' not in body and "'" not in body and not body.startswith('//'):
                for a, b in ((' = ', '='), (' + ', '+'), (' < ', '<'), (' - ', '-'), (' * ', '*'), (' == ', '=='),
                             (' <= ', '<='), (' > ', '>'), (' != ', '!='), (' && ', '&&')):
                    body = body.replace(a, b)
            out.append(' ' * n + body)
    return '\n'.join(out)


def t_comments_added(src, lang):
    cm = LANGS[lang][3]; out = []; k = 0
    for l in lines_of(src):
        b = l.strip()
        if not b or b.startswith(cm) or MARK in b or b.startswith('}') or b.startswith(('else', 'elif', '#include', 'using')):
            out.append(l); continue
        if b.endswith((';', '{', ':')) and k % 2 == 0:
            out.append(' ' * indent_of(l) + f'{cm} step {k}')
        out.append(l if not (b.endswith(';') and k % 3 == 0) or b.startswith('for') else l + f'  {cm} done')
        k += 1
    return f'{cm} reviewed version\n' + '\n'.join(out)


def t_comments_removed(src, lang):
    cm = LANGS[lang][3]; out = []
    for l in lines_of(src):
        if l.strip().startswith(cm) and MARK not in l: continue
        out.append(re.sub(rf'\s+{re.escape(cm)}(?!{MARK}).*$', '', l) if (cm in l and MARK not in l and '"' not in l) else l)
    return '\n'.join(out)


def whole_word_sub(src, mapping):
    # do not touch identifiers inside string literals
    parts = re.split(r'("(?:[^"\\]|\\.)*")', src)
    for i in range(0, len(parts), 2):
        for old, new in mapping.items():
            parts[i] = re.sub(rf'\b{re.escape(old)}\b', new, parts[i])
    return ''.join(parts)


def t_rename(src, lang, spec, with_functions):
    m = {k: v for k, v in spec['renames'].items() if k != v}
    for n in range(10): m[f'cfg{n}'] = f'option_{chr(97 + n)}'
    if with_functions: m.update(spec.get('fn_renames', {}))
    return whole_word_sub(src, m)


def t_for_to_while(src, lang):
    L = lines_of(src); changed = False; i = 0
    while i < len(L):
        l = L[i]
        if lang == 'PYTHON':
            m = re.match(r'^(\s*)for (\w+) in range\((.+)\):\s*$', l)
            if m and MARK not in l:
                ind, v, a = m.group(1), m.group(2), split_args(m.group(3))
                start, end = ('0', a[0]) if len(a) == 1 else (a[0], a[1])
                if len(a) > 2: i += 1; continue
                e = py_block_end(L, i)
                body = L[i + 1:e + 1]
                bind = indent_of(body[0]) if body else len(ind) + 4
                new = [f'{ind}{v} = {start}', f'{ind}while {v} < {end}:'] + body + [' ' * bind + f'{v} += 1']
                L[i:e + 1] = new; changed = True; i += 2; continue
        else:
            m = re.match(r'^(\s*)for \(int (\w+) = ([^;]+); ([^;]+); \2\+\+\) \{\s*$', l)
            if m:
                ind, v, a, cond = m.groups()
                e = c_block_end(L, i)
                if e is None: i += 1; continue
                body = ['    ' + x if x.strip() else x for x in L[i + 1:e]]
                new = [f'{ind}{{', f'{ind}    int {v} = {a};', f'{ind}    while ({cond}) {{'] + body + \
                      [f'{ind}        {v}++;', f'{ind}    }}', f'{ind}}}']
                L[i:e + 1] = new; changed = True; i += 3; continue
        i += 1
    return '\n'.join(L) if changed else None


def t_nested_cond(src, lang):
    L = lines_of(src); changed = False; i = 0
    while i < len(L):
        l = L[i]
        if lang == 'PYTHON':
            m = re.match(r'^(\s*)if (.+?) and (.+):\s*$', l)
            if m:
                ind, A, B = m.groups(); e = py_block_end(L, i)
                nxt = next((x for x in L[e + 1:] if x.strip()), '')
                if nxt.strip().startswith(('else', 'elif')) and indent_of(nxt) == len(ind): i += 1; continue
                body = ['    ' + x if x.strip() else x for x in L[i + 1:e + 1]]
                L[i:e + 1] = [f'{ind}if {A}:', f'{ind}    if {B}:'] + body; changed = True; i += 2; continue
        else:
            m = re.match(r'^(\s*)if \((.+?) && (.+)\) \{\s*$', l)
            if m:
                ind, A, B = m.groups(); e = c_block_end(L, i)
                if e is None or L[e].strip() != '}': i += 1; continue
                body = ['    ' + x if x.strip() else x for x in L[i + 1:e]]
                L[i:e + 1] = [f'{ind}if ({A}) {{', f'{ind}    if ({B}) {{'] + body + [f'{ind}    }}', f'{ind}}}']
                changed = True; i += 2; continue
        i += 1
    return '\n'.join(L) if changed else None


def t_temp_var(src, lang):
    out = []; k = 0; changed = False
    for l in lines_of(src):
        pat = r'^(\s*)(\w+(?:\[[^\]]+\])*) \+= (.+?);?\s*$' if lang != 'PYTHON' else r'^(\s*)(\w+(?:\[[^\]]+\])*) \+= (.+)$'
        m = re.match(pat, l)
        if m and MARK not in l and re.search(r'[*(\[]| [-+/%] ', m.group(3)):
            ind, lhs, expr = m.groups()
            if lang == 'PYTHON': out += [f'{ind}tv{k} = {expr}', f'{ind}{lhs} += tv{k}']
            else: out += [f'{ind}long tv{k} = {expr};', f'{ind}{lhs} += tv{k};']
            k += 1; changed = True
        else: out.append(l)
    return '\n'.join(out) if changed else None


def top_level_last_addsub(expr):
    depth = 0; pos = None
    for i, ch in enumerate(expr):
        if ch in '([': depth += 1
        elif ch in ')]': depth -= 1
        elif depth == 0 and ch in '+-' and i > 0 and expr[i - 1] == ' ' and i + 1 < len(expr) and expr[i + 1] == ' ':
            pos = i
    return pos


def t_split_stmt(src, lang):
    out = []; k = 0; changed = False
    for l in lines_of(src):
        if MARK in l or l.strip().startswith(('return', 'for', 'if', 'while')):
            out.append(l); continue
        pat = r'^(\s*)(?:(int|long) )?(\w+) = (.+?);?\s*$' if lang != 'PYTHON' else r'^(\s*)()(\w+) = (.+)$'
        m = re.match(pat, l)
        if m and '{' not in l and '"' not in l:
            ind, ty, name, rhs = m.groups(); pos = top_level_last_addsub(rhs)
            if pos is not None:
                left, op, right = rhs[:pos].strip(), rhs[pos], rhs[pos + 1:].strip()
                if re.search(r'[*/%(\[]', right):
                    if lang == 'PYTHON':
                        out += [f'{ind}sp{k} = {right}', f'{ind}{name} = {left} {op} sp{k}']
                    else:
                        decl = f'{ty} ' if ty else ''
                        out += [f'{ind}long sp{k} = {right};', f'{ind}{decl}{name} = {left} {op} sp{k};']
                    k += 1; changed = True; continue
            if lang != 'PYTHON' and ty and '[' not in l and re.search(r'[*+\-/(]', rhs):
                out += [f'{ind}{ty} {name};', f'{ind}{name} = {rhs};']; changed = True; continue
        out.append(l)
    return '\n'.join(out) if changed else None


def t_reorder(src, lang, frac, seed):
    L = lines_of(src); idx = [i for i, l in enumerate(L) if MARK in l]
    k = min(len(idx), max(2, -(-int(round(frac * 100)) * len(idx) // 100)))  # ceil(frac*N), at least 2
    rng = random.Random(seed); chosen = sorted(rng.sample(idx, k))
    moved = [L[i] for i in chosen]
    shift = rng.randint(1, k - 1); moved = moved[shift:] + moved[:shift]
    for i, l in zip(chosen, moved): L[i] = l
    kinds = []
    for l in moved:
        rhs = l.split('=', 1)[1]
        kinds.append('call' if 'sq(' in rhs else 'arith' if re.search(r'\d [*+-] \d', rhs) else 'literal')
    return '\n'.join(L), k, kinds


# ------------------------------------------------------------ validation ----
_cache = {}


def run_program(lang, src):
    key = (lang, hashlib.sha1(src.encode()).hexdigest())
    if key in _cache: return _cache[key]
    ext = LANGS[lang][2]; res = dict(syntax_ok=False, run_ok=False, stdout='', error='')
    with tempfile.TemporaryDirectory() as d:
        f = os.path.join(d, 'prog' + ext)
        open(f, 'w').write(src)
        try:
            if lang == 'PYTHON':
                compile(src, f, 'exec'); res['syntax_ok'] = True
                p = subprocess.run([sys.executable, '-I', f], capture_output=True, text=True, timeout=20)
            else:
                cc = ['gcc', '-std=gnu11'] if lang == 'C' else ['g++', '-std=c++17']
                c = subprocess.run(cc + ['-w', '-o', os.path.join(d, 'exe'), f], capture_output=True, text=True, timeout=60)
                if c.returncode != 0: res['error'] = c.stderr[:300]; _cache[key] = res; return res
                res['syntax_ok'] = True
                p = subprocess.run([os.path.join(d, 'exe')], capture_output=True, text=True, timeout=20)
            res['run_ok'] = p.returncode == 0
            res['stdout'] = p.stdout; res['error'] = p.stderr[:300]
        except SyntaxError as e: res['error'] = f'SyntaxError: {e}'
        except Exception as e: res['error'] = f'{type(e).__name__}: {e}'
    _cache[key] = res
    return res


def out_no_chk(s): return '\n'.join(l for l in s.split('\n') if not l.startswith('chk'))


# ---------------------------------------------------------------- build ----
def variants_for(base_id, lang, spec):
    """Return {transformation_name: (src_with_marks, meta)} for one base program."""
    orig = render(base_id, lang, spec['src'])
    V = {}

    def add(name, s, **meta):
        if s is not None and strip_marks(s).strip() != strip_marks(orig).strip():
            V[name] = (s, meta)

    for kind in (('indent2', 'tabs', 'extra') if lang == 'PYTHON' else ('allman', 'tabs', 'compact')):
        add(f'format_{kind}', t_formatting(orig, lang, kind))
    add('comments_added', t_comments_added(orig, lang))
    add('comments_removed', t_comments_removed(orig, lang))
    add('rename_locals', t_rename(orig, lang, spec, False))
    add('rename_all', t_rename(orig, lang, spec, True))
    for frac in (0.10, 0.25, 0.50, 0.75):
        for rep in range(2):
            s, k, kinds = t_reorder(orig, lang, frac, f'{SEED}-{base_id}-{lang}-{frac}-{rep}')
            add(f'reorder_{int(frac * 100)}_r{rep}', s, level=frac, moved=k, kinds='|'.join(kinds))
    add('for_to_while', t_for_to_while(orig, lang))
    add('nested_condition', t_nested_cond(orig, lang))
    add('temp_variable', t_temp_var(orig, lang))
    add('equivalent_arithmetic', render(base_id, lang, spec['src'], arith_variant=True))
    add('split_statement', t_split_stmt(orig, lang))
    # combinations
    light = t_comments_added(t_rename(orig, lang, spec, False), lang)
    add('combo_light', t_formatting(light, lang, 'tabs' if lang != 'PYTHON' else 'indent2'))
    h = orig
    for f in (lambda s: t_for_to_while(s, lang), lambda s: t_nested_cond(s, lang), lambda s: t_temp_var(s, lang)):
        r = f(h); h = r if r else h
    h = render(base_id, lang, spec['src'], arith_variant=True) if h == orig else h
    h = t_reorder(h, lang, 0.5, f'{SEED}-{base_id}-heavy')[0]
    add('combo_heavy', t_rename(h, lang, spec, True))
    return orig, V


CATEGORY = lambda n: ('formatting' if n.startswith('format_') else 'comments' if n.startswith('comments_') else
                      'rename_locals' if n == 'rename_locals' else 'rename_all' if n == 'rename_all' else
                      'reordering' if n.startswith('reorder_') else 'combo' if n.startswith('combo_') else 'semantic')


def main():
    ap = argparse.ArgumentParser(); ap.add_argument('--out', default='dataset'); a = ap.parse_args()
    os.makedirs(a.out, exist_ok=True)
    rng = random.Random(SEED)
    programs, log, pairs = {}, [], []

    def reg(lang, base, name, src, derived_from=None):
        s = strip_marks(src)
        pid = f'{lang}:{base}:{name}'
        r = run_program(lang, s)
        programs[pid] = dict(program_id=pid, language=lang, base=base, transformation=name, syntax_ok=r['syntax_ok'],
                             run_ok=r['run_ok'], error=r['error'], source=s, stdout=r['stdout'])
        return pid

    for lang, (bank, *_rest) in LANGS.items():
        built = {}
        for base, spec in bank.items():
            orig, V = variants_for(base, lang, spec)
            alt = render(base + '_alt', lang, spec['alt'])
            built[base] = dict(spec=spec, orig=reg(lang, base, 'original', orig), alt=reg(lang, base, 'independent_alt', alt), V={})
            for name, (s, meta) in V.items():
                built[base]['V'][name] = (reg(lang, base, name, s), meta)
        # ---- positives (label 1) and independent same-task pairs (label 0)
        for base, b in built.items():
            O = programs[b['orig']]
            for name, (pid, meta) in b['V'].items():
                P = programs[pid]
                ok = P['syntax_ok'] and O['syntax_ok'] and P['run_ok'] and O['run_ok'] and P['stdout'] == O['stdout']
                if not ok:
                    log.append(dict(language=lang, base=base, transformation=name, status='REJECTED',
                                    reason='syntax' if not P['syntax_ok'] else 'run' if not P['run_ok'] else 'output differs',
                                    detail=P['error'][:120]))
                    continue
                pairs.append(dict(language=lang, split=b['spec']['split'], base_a=base, base_b=base, label=1,
                                  category=CATEGORY(name), transformation=name, size=b['spec']['size'],
                                  a=b['orig'], b=pid, meta=json.dumps(meta)))
            A = programs[b['alt']]
            if A['syntax_ok'] and A['run_ok'] and out_no_chk(A['stdout']) == out_no_chk(O['stdout']):
                pairs.append(dict(language=lang, split=b['spec']['split'], base_a=base, base_b=base, label=0,
                                  category='independent_solution', transformation='independent_alt',
                                  size=b['spec']['size'], a=b['orig'], b=b['alt'], meta='{}'))
            else:
                log.append(dict(language=lang, base=base, transformation='independent_alt', status='REJECTED',
                                reason='alt differs from base behaviour or fails', detail=A['error'][:120]))
        # ---- unrelated negatives (label 0): different tasks, same split and language
        for split in ('dev', 'test'):
            names = [n for n, b in built.items() if b['spec']['split'] == split]
            for x, y in itertools.combinations(names, 2):
                pool = [('original', built[y]['orig'])] + [(n, p) for n, (p, m) in built[y]['V'].items()
                        if programs[p]['run_ok'] and not n.startswith('format_')]
                for n, pid in rng.sample(pool, min(8, len(pool))):
                    if programs[built[x]['orig']]['run_ok'] and programs[pid]['run_ok']:
                        pairs.append(dict(language=lang, split=split, base_a=x, base_b=y, label=0, category='unrelated',
                                          transformation=f'other_task:{n}', size=built[x]['spec']['size'],
                                          a=built[x]['orig'], b=pid, meta='{}'))

    # randomise A/B order, drop exact duplicates, assign ids
    seen, rows = set(), []
    for p in pairs:
        key = (programs[p['a']]['source'], programs[p['b']]['source'])
        if key in seen or key[::-1] in seen or key[0] == key[1]: continue
        seen.add(key)
        if rng.random() < 0.5: p['a'], p['b'] = p['b'], p['a']
        rows.append(p)
    for i, p in enumerate(rows):
        p['pair_id'] = f'P{i:04d}'
        p['source_a'] = programs[p['a']]['source']; p['source_b'] = programs[p['b']]['source']
        p['program_a'] = p.pop('a'); p['program_b'] = p.pop('b')
        p['syntax_valid'] = programs[p['program_a']]['syntax_ok'] and programs[p['program_b']]['syntax_ok']

    cols = ['pair_id', 'split', 'language', 'base_a', 'base_b', 'label', 'category', 'transformation', 'size',
            'program_a', 'program_b', 'syntax_valid', 'meta', 'source_a', 'source_b']
    with open(os.path.join(a.out, 'Generated_Test_Dataset.csv'), 'w', newline='') as f:
        w = csv.DictWriter(f, cols); w.writeheader(); [w.writerow({c: r[c] for c in cols}) for r in rows]
    json.dump(rows, open(os.path.join(a.out, 'Generated_Test_Dataset.json'), 'w'), indent=1)
    with open(os.path.join(a.out, 'program_validation.csv'), 'w', newline='') as f:
        pc = ['program_id', 'language', 'base', 'transformation', 'syntax_ok', 'run_ok', 'error']
        w = csv.DictWriter(f, pc); w.writeheader(); [w.writerow({c: p[c] for c in pc}) for p in programs.values()]
    with open(os.path.join(a.out, 'generation_log.csv'), 'w', newline='') as f:
        w = csv.DictWriter(f, ['language', 'base', 'transformation', 'status', 'reason', 'detail'])
        w.writeheader(); [w.writerow(r) for r in log]
    # summary
    import collections
    c = collections.Counter((r['language'], r['split'], r['label']) for r in rows)
    print('programs:', len(programs), ' pairs:', len(rows), ' rejected:', len(log))
    for k in sorted(c): print(k, c[k])
    print(collections.Counter((r['category'], r['label']) for r in rows))


if __name__ == '__main__':
    main()
