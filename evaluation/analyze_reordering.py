#!/usr/bin/env python3
"""Step 7: statement-reordering study, from score CSVs written by Evaluation_Script.py.

  python3 Evaluation_Script.py --out results_on  --enable-reordering true
  python3 Evaluation_Script.py --out results_off --enable-reordering false
  python3 analyze_reordering.py --on results_on/Experimental_Results.csv --off results_off/Experimental_Results.csv \
         --thresholds results_on/thresholds.json --out results_reorder

Reports, on the TEST split: recall by reordering level (10/25/50/75 %), by kind of moved statement,
by tree size/depth, effect of the dependency-aware canonicalisation (flag on vs off), threshold
sensitivity, and the false negatives themselves. Thresholds come from the DEV split (thresholds.json).
"""
import argparse, json, os
import numpy as np, pandas as pd

def load(path, col):
    d = pd.read_csv(path); d = d[(d.status == 'ok') & (d.split == 'test')].copy()
    m = d['meta'].apply(lambda s: json.loads(s) if isinstance(s, str) and s.startswith('{') else {})
    d['level'] = m.apply(lambda x: x.get('level')); d['moved'] = m.apply(lambda x: x.get('moved'))
    d['kinds'] = m.apply(lambda x: x.get('kinds', ''))
    return d

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--on', required=True); ap.add_argument('--off'); ap.add_argument('--thresholds', required=True)
    ap.add_argument('--formula', default='score_production'); ap.add_argument('--out', default='results_reorder')
    a = ap.parse_args(); os.makedirs(a.out, exist_ok=True)
    thr = json.load(open(a.thresholds))['thresholds'][a.formula]
    on = load(a.on, a.formula); R = on[on.category == 'reordering']; N = on[on.label == 0]
    rows = []
    def rec(name, df, d=None):
        d = d if d is not None else on
        return dict(group=name, n=len(df), recall=float((df[a.formula] >= thr).mean()) if len(df) else np.nan,
                    mean_score=float(df[a.formula].mean()) if len(df) else np.nan)
    for lv in sorted(R.level.dropna().unique()): rows.append(rec(f'level {int(lv*100)}%', R[R.level == lv]))
    for k in ('call', 'arith', 'literal'):
        rows.append(rec(f'moved set includes "{k}" statement', R[R.kinds.str.contains(k, na=False)]))
    for lang in sorted(R.language.unique()): rows.append(rec(f'language {lang}', R[R.language == lang]))
    if 'nodes_a' in R:
        size = (R.nodes_a + R.nodes_b) / 2
        for lo, hi in ((0, 150), (150, 300), (300, 10**9)):
            rows.append(rec(f'avg nodes {lo}-{hi if hi < 10**9 else "+"}', R[(size >= lo) & (size < hi)]))
    if a.off:
        off = load(a.off, a.formula); Roff = off[off.category == 'reordering']
        rows.append(rec('ALL reordering, canonicalisation ON', R)); rows.append(rec('ALL reordering, canonicalisation OFF', Roff))
        for lv in sorted(Roff.level.dropna().unique()):
            rows.append(rec(f'level {int(lv*100)}% canonicalisation OFF', Roff[Roff.level == lv]))
        N_off = off[off.label == 0]
        rows.append(dict(group='false-positive rate on negatives, ON', n=len(N), recall=float((N[a.formula] >= thr).mean()), mean_score=np.nan))
        rows.append(dict(group='false-positive rate on negatives, OFF', n=len(N_off), recall=float((N_off[a.formula] >= thr).mean()), mean_score=np.nan))
    pd.DataFrame(rows).to_csv(os.path.join(a.out, 'reordering_breakdown.csv'), index=False)
    # threshold sensitivity (diagnostic only; the reported threshold stays the dev-selected one)
    sens = [dict(threshold=t, reordering_recall=float((R[a.formula] >= t).mean()),
                 negatives_false_positive_rate=float((N[a.formula] >= t).mean())) for t in range(40, 100, 5)]
    pd.DataFrame(sens).to_csv(os.path.join(a.out, 'reordering_threshold_sensitivity.csv'), index=False)
    R[R[a.formula] < thr].drop(columns=[c for c in R.columns if c.startswith('source')], errors='ignore') \
        .to_csv(os.path.join(a.out, 'reordering_false_negatives.csv'), index=False)
    print(pd.DataFrame(rows).round(3).to_string(index=False)); print(f'\nthreshold used (dev-selected): {thr}')

if __name__ == '__main__': main()
