#!/usr/bin/env python3
"""Reusable evaluation of the AST similarity system on a labelled pair dataset.

Pipeline
  1. score every pair with the *running application* (POST /api/compare); responses are cached
     in <out>/Experimental_Results.csv so an interrupted run resumes;
  2. production formula  S = 100*(1 - D/(|T_A|+|T_B|))   (what the code computes)
     experimental formula S_max = 100*(1 - D/max(|T_A|,|T_B|)) clamped to [0,100]
     (computed here from the returned D and node counts; production code is not modified);
  3. choose a threshold that maximises F1 on the DEV split only, freeze it, report on TEST;
  4. per-category / per-language precision, recall, F1, confusion matrices, charts.

Per-category definition: positives of that category + every negative of the same split.
Precision therefore depends on the shared negatives; recall is category specific.

Examples
  python3 Evaluation_Script.py --server http://localhost:8080 --out results
  python3 Evaluation_Script.py --scores results/Experimental_Results.csv --out results   # re-analyse only
  python3 Evaluation_Script.py --selftest     # synthetic scores, checks the harness only (NOT results)
"""
import argparse, csv, hashlib, json, os, platform, random, sys, time, urllib.request
import numpy as np
import pandas as pd

FORMULAS = ['score_production', 'score_max_variant']
SEED = 20260226


# ---------------------------------------------------------------- scoring ----
def depth(node):
    if not node: return 0
    kids = node.get('children') or []
    return 1 + max((depth(k) for k in kids), default=0)


def call_server(url, row, enable_reordering, enable_literals, timeout=60):
    body = json.dumps(dict(language=row['language'], sourceA=row['source_a'], sourceB=row['source_b'],
                           enableReordering=enable_reordering, enableLiteralAbstraction=enable_literals)).encode()
    req = urllib.request.Request(url.rstrip('/') + '/api/compare', data=body, headers={'Content-Type': 'application/json'})
    t0 = time.perf_counter()
    with urllib.request.urlopen(req, timeout=timeout) as r:
        resp = json.loads(r.read().decode())
    return resp, (time.perf_counter() - t0) * 1000.0


def score_pairs(df, server, out_csv, enable_reordering, enable_literals, limit=None):
    done = {}
    if os.path.exists(out_csv):
        done = {r['pair_id']: r for r in pd.read_csv(out_csv).to_dict('records')}
    rows = []
    todo = df if limit is None else df.head(limit)
    for i, r in enumerate(todo.to_dict('records')):
        if r['pair_id'] in done:
            rows.append(done[r['pair_id']]); continue
        rec = {k: r[k] for k in ['pair_id', 'split', 'language', 'base_a', 'base_b', 'label', 'category',
                                 'transformation', 'size', 'meta']}
        try:
            resp, ms = call_server(server, r, enable_reordering, enable_literals)
            nA, nB, D = resp.get('nodeCountA', 0), resp.get('nodeCountB', 0), resp.get('treeEditDistance', 0.0)
            ok = not resp.get('diagnosticsA') and not resp.get('diagnosticsB')
            rec.update(status='ok' if ok else 'syntax_error', score_production=resp.get('similarityScore'),
                       ted=D, nodes_a=nA, nodes_b=nB, matched=resp.get('matchedNodeCount'),
                       inserted=resp.get('insertedNodeCount'), deleted=resp.get('deletedNodeCount'),
                       substituted=resp.get('substitutedNodeCount'), depth_a=depth(resp.get('treeA')),
                       depth_b=depth(resp.get('treeB')), latency_ms=round(ms, 2),
                       algorithm=resp.get('algorithmVersion'))
        except Exception as e:  # keep going; failures are reported, never silently scored
            rec.update(status=f'error:{type(e).__name__}', score_production=np.nan)
        rows.append(rec)
        if (i + 1) % 25 == 0:
            pd.DataFrame(rows).to_csv(out_csv, index=False); print(f'  scored {i + 1}/{len(todo)}', flush=True)
    res = pd.DataFrame(rows); res.to_csv(out_csv, index=False)
    return res


def add_formulas(res):
    res = res.copy()
    nmax = np.maximum(res['nodes_a'].astype(float), res['nodes_b'].astype(float))
    with np.errstate(divide='ignore', invalid='ignore'):
        alt = 100.0 * (1.0 - res['ted'].astype(float) / nmax)
    res['score_max_variant'] = np.clip(alt.fillna(0.0), 0.0, 100.0).round(2)
    res['score_production'] = res['score_production'].astype(float)
    return res


# ---------------------------------------------------------------- metrics ----
def confusion(y, pred):
    y, pred = np.asarray(y).astype(bool), np.asarray(pred).astype(bool)
    return dict(TP=int((y & pred).sum()), FP=int((~y & pred).sum()), TN=int((~y & ~pred).sum()), FN=int((y & ~pred).sum()))


def prf(c):
    p = c['TP'] / (c['TP'] + c['FP']) if (c['TP'] + c['FP']) else float('nan')
    r = c['TP'] / (c['TP'] + c['FN']) if (c['TP'] + c['FN']) else float('nan')
    f = 2 * p * r / (p + r) if (p == p and r == r and (p + r) > 0) else float('nan')
    n = sum(c.values())
    return dict(precision=p, recall=r, f1=f, accuracy=(c['TP'] + c['TN']) / n if n else float('nan'))


def auc(y, s):
    y, s = np.asarray(y).astype(bool), np.asarray(s, float)
    pos, neg = s[y], s[~y]
    if len(pos) == 0 or len(neg) == 0: return float('nan')
    ranks = pd.Series(np.concatenate([pos, neg])).rank().to_numpy()
    return float((ranks[:len(pos)].sum() - len(pos) * (len(pos) + 1) / 2) / (len(pos) * len(neg)))


def best_threshold(y, s):
    """Threshold maximising F1 (predict similar if score >= t). Ties -> middle of the optimal range."""
    cands = np.unique(np.asarray(s, float))
    best, ts = -1.0, []
    for t in cands:
        f = prf(confusion(y, np.asarray(s) >= t))['f1']
        f = -1.0 if f != f else f
        if f > best + 1e-12: best, ts = f, [t]
        elif abs(f - best) <= 1e-12: ts.append(t)
    return float(np.median(ts)), best


def subset_metrics(df, col, thr):
    c = confusion(df['label'], df[col] >= thr)
    return {**c, **prf(c), 'n_pos': int(df['label'].sum()), 'n_neg': int((df['label'] == 0).sum())}


def category_table(df, col, thr):
    rows = []
    negs = df[df['label'] == 0]
    for cat in sorted(df[df['label'] == 1]['category'].unique()):
        sub = pd.concat([df[(df['label'] == 1) & (df['category'] == cat)], negs])
        rows.append(dict(category=cat, formula=col, threshold=thr, **subset_metrics(sub, col, thr)))
    for cat in sorted(negs['category'].unique()):  # negatives: report specificity
        sub = negs[negs['category'] == cat]
        tn = int((sub[col] < thr).sum())
        rows.append(dict(category=cat + ' (negatives)', formula=col, threshold=thr, TP=0, FP=len(sub) - tn, TN=tn,
                         FN=0, precision=float('nan'), recall=float('nan'), f1=float('nan'),
                         accuracy=tn / len(sub), n_pos=0, n_neg=len(sub)))
    return pd.DataFrame(rows)


def bootstrap_f1(df, col, thr, n=1000):
    rng = random.Random(SEED); bases = sorted(set(df['base_a']))
    groups = {b: df[df['base_a'] == b] for b in bases}; out = []
    for _ in range(n):
        s = pd.concat([groups[rng.choice(bases)] for _ in bases])
        f = prf(confusion(s['label'], s[col] >= thr))['f1']
        if f == f: out.append(f)
    return (float(np.percentile(out, 2.5)), float(np.percentile(out, 97.5))) if out else (float('nan'),) * 2


# ----------------------------------------------------------------- charts ----
def charts(res, thr, outdir):
    import matplotlib; matplotlib.use('Agg'); import matplotlib.pyplot as plt
    test = res[(res['split'] == 'test') & (res['status'] == 'ok')]
    fig, axes = plt.subplots(1, 2, figsize=(9, 3.6))
    for ax, col in zip(axes, FORMULAS):
        c = confusion(test['label'], test[col] >= thr[col]); m = np.array([[c['TN'], c['FP']], [c['FN'], c['TP']]])
        ax.imshow(m, cmap='Blues'); ax.set_xticks([0, 1], ['pred 0', 'pred 1']); ax.set_yticks([0, 1], ['true 0', 'true 1'])
        for (i, j), v in np.ndenumerate(m): ax.text(j, i, v, ha='center', va='center', color='black')
        ax.set_title(f'{col}\nthreshold {thr[col]:.2f} (test)', fontsize=9)
    fig.tight_layout(); fig.savefig(os.path.join(outdir, 'confusion_matrices.png'), dpi=200); plt.close(fig)

    fig, ax = plt.subplots(figsize=(7, 3.6))
    cats = sorted(test[test['label'] == 1]['category'].unique()); w = 0.38
    for k, col in enumerate(FORMULAS):
        rec = [(test[(test['category'] == c)][col] >= thr[col]).mean() for c in cats]
        ax.bar(np.arange(len(cats)) + (k - .5) * w, rec, w, label=col)
    ax.set_xticks(range(len(cats)), cats, rotation=30, ha='right'); ax.set_ylabel('recall (test)'); ax.set_ylim(0, 1.05)
    ax.legend(fontsize=8); fig.tight_layout(); fig.savefig(os.path.join(outdir, 'recall_by_category.png'), dpi=200); plt.close(fig)

    fig, axes = plt.subplots(1, 2, figsize=(9, 3.4), sharey=True)
    for ax, col in zip(axes, FORMULAS):
        ax.hist(test[test['label'] == 1][col], bins=20, range=(0, 100), alpha=.7, label='copy (1)')
        ax.hist(test[test['label'] == 0][col], bins=20, range=(0, 100), alpha=.7, label='not copy (0)')
        ax.axvline(thr[col], color='k', ls='--', lw=1); ax.set_title(col, fontsize=9); ax.set_xlabel('similarity score (%)')
    axes[0].legend(fontsize=8); axes[0].set_ylabel('pairs'); fig.tight_layout()
    fig.savefig(os.path.join(outdir, 'score_distributions.png'), dpi=200); plt.close(fig)


# ------------------------------------------------------------------- main ----
def analyse(res, outdir, meta):
    res = add_formulas(res)
    bad = res[res['status'] != 'ok']
    if len(bad): print(f'WARNING: {len(bad)} pairs not scored ok (excluded):', bad['status'].value_counts().to_dict())
    ok = res[res['status'] == 'ok']
    dev, test = ok[ok['split'] == 'dev'], ok[ok['split'] == 'test']
    thr, summ, cats, langs = {}, [], [], []
    for col in FORMULAS:
        t, dev_f1 = best_threshold(dev['label'], dev[col]); thr[col] = t          # DEV only
        lo, hi = bootstrap_f1(test, col, t)
        summ.append(dict(formula=col, threshold_from_dev=t, dev_f1=dev_f1, auc_dev=auc(dev['label'], dev[col]),
                         auc_test=auc(test['label'], test[col]), test_f1_ci95_low=lo, test_f1_ci95_high=hi,
                         **subset_metrics(test, col, t)))
        cats.append(category_table(test, col, t))
        for lang in sorted(test['language'].unique()):
            langs.append(dict(language=lang, formula=col, threshold=t, **subset_metrics(test[test['language'] == lang], col, t)))
    json.dump(dict(thresholds=thr, selected_on='dev split only', rule='predict similar if score >= threshold'),
              open(os.path.join(outdir, 'thresholds.json'), 'w'), indent=1)
    pd.DataFrame(summ).to_csv(os.path.join(outdir, 'summary_overall_test.csv'), index=False)
    pd.concat(cats).to_csv(os.path.join(outdir, 'summary_by_category_test.csv'), index=False)
    pd.DataFrame(langs).to_csv(os.path.join(outdir, 'summary_by_language_test.csv'), index=False)
    res.to_csv(os.path.join(outdir, 'Experimental_Results.csv'), index=False)
    json.dump(meta, open(os.path.join(outdir, 'run_metadata.json'), 'w'), indent=1, default=str)
    charts(ok, thr, outdir)
    print(pd.DataFrame(summ).round(4).to_string(index=False))
    print(pd.concat(cats)[['category', 'formula', 'n_pos', 'n_neg', 'precision', 'recall', 'f1']].round(4).to_string(index=False))


def selftest(df, outdir):
    """Synthetic scorer: validates the pipeline only. Output is labelled and must never be reported."""
    rng = random.Random(1); rows = []
    base = {'formatting': 99, 'comments': 99, 'rename_locals': 97, 'rename_all': 90, 'semantic': 85, 'reordering': 80,
            'combo': 82, 'unrelated': 55, 'independent_solution': 72}
    for r in df.to_dict('records'):
        s = min(100, max(0, base[r['category']] + rng.gauss(0, 6)))
        na, nb = rng.randint(60, 200), rng.randint(60, 200)
        rows.append(dict({k: r[k] for k in ['pair_id', 'split', 'language', 'base_a', 'base_b', 'label', 'category',
                                           'transformation', 'size', 'meta']}, status='ok', score_production=round(s, 2),
                         ted=round((1 - s / 100) * (na + nb), 2), nodes_a=na, nodes_b=nb))
    analyse(pd.DataFrame(rows), outdir, dict(SELFTEST='SYNTHETIC SCORES - NOT EXPERIMENTAL RESULTS'))


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--dataset', default='dataset/Generated_Test_Dataset.csv')
    ap.add_argument('--server', default='http://localhost:8080')
    ap.add_argument('--out', default='results')
    ap.add_argument('--scores', help='existing Experimental_Results.csv: skip scoring, only analyse')
    ap.add_argument('--enable-reordering', default='true', choices=['true', 'false'])
    ap.add_argument('--enable-literal-abstraction', default='false', choices=['true', 'false'])
    ap.add_argument('--limit', type=int)
    ap.add_argument('--selftest', action='store_true')
    a = ap.parse_args()
    df = pd.read_csv(a.dataset)
    if a.selftest:
        out = os.path.join(a.out, 'SELFTEST_SYNTHETIC'); os.makedirs(out, exist_ok=True); selftest(df, out)
        print('\nSELFTEST finished: synthetic scores written to', out, '(not experimental results)'); return
    os.makedirs(a.out, exist_ok=True)
    out_csv = os.path.join(a.out, 'Experimental_Results.csv')
    res = pd.read_csv(a.scores) if a.scores else score_pairs(df, a.server, out_csv, a.enable_reordering == 'true',
                                                             a.enable_literal_abstraction == 'true', a.limit)
    meta = dict(date=time.strftime('%Y-%m-%d %H:%M:%S'), platform=platform.platform(), python=sys.version.split()[0],
                dataset=a.dataset, dataset_sha1=hashlib.sha1(open(a.dataset, 'rb').read()).hexdigest(),
                server=a.server, enable_reordering=a.enable_reordering, pairs=len(df),
                formulas={'production': '100*(1-D/(|TA|+|TB|)) (as implemented in ScoringService.java)',
                          'max_variant': '100*(1-D/max(|TA|,|TB|)) clamped, experimental only'})
    analyse(res, a.out, meta)


if __name__ == '__main__':
    main()
