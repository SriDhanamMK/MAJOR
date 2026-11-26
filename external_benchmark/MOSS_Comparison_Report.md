# MOSS Comparative Evaluation Results

## Configuration
- Tool: MOSS
- Language: C (`-l c`)
- Common-code filtering option: `-m 10`
- Evaluation subset: 70 labelled C-language pairs from `ast_similarity_comparison_dataset/ground_truth_pairs.csv`
- Decision threshold: 50%
- Pair score: conservative minimum of the two file-side similarity percentages reported for a pair
- Pairs absent from the MOSS report: treated as non-detections

## Confusion matrix

| Metric | Count |
|---|---:|
| True Positives (TP) | 60 |
| False Positives (FP) | 0 |
| True Negatives (TN) | 10 |
| False Negatives (FN) | 0 |
| Total | 70 |

## Performance

| Metric | Result |
|---|---:|
| Precision | 100.00% |
| Recall | 100.00% |
| F1-score | 100.00% |
| Accuracy | 100.00% |

## Category-level results

| Category | Labelled pairs | Detected |
|---|---:|---:|
| Formatting | 20 | 20 |
| Identifier renaming | 20 | 20 |
| Statement reordering | 20 | 20 |
| Independent implementations | 10 | 0 |

## Interpretation and limitations

These results describe performance on this specific controlled C-language subset only. They do not establish universal or real-world MOSS accuracy. The dataset is small and synthetic, and the MOSS evaluation scope (70 C pairs) differs from the JPlag evaluation (210 pairs across C, C++, and Python) and the proposed system's held-out test set (318 pairs). Therefore, the figures are useful as a supplementary comparison, not as a perfectly controlled head-to-head ranking.

The 50% decision threshold and conservative minimum-side aggregation rule are part of the evaluation protocol and must be stated when reporting these results. MOSS's `-m 10` option controls common-code filtering; it is not the pair-classification threshold.
