# Comparative Results: Proposed System, JPlag, and MOSS

| System | Evaluation scope | Pairs | Precision | Recall | F1-score | Accuracy |
|---|---|---:|---:|---:|---:|---:|
| Proposed AST system (reordering ON) | C, C++, Python; held-out test split | 318 | 98.97% | 94.12% | 96.48% | 95.60% |
| JPlag | C, C++, Python; controlled dataset | 210 | 96.77% | 100.00% | 98.36% | 97.14% |
| MOSS | C only; controlled subset | 70 | 100.00% | 100.00% | 100.00% | 100.00% |

**Caution:** These are not directly equivalent test conditions. The systems were evaluated on different sample sizes and language scopes. MOSS results cover C only. The table should be presented as a supplementary comparative evaluation, not as a controlled ranking across identical test pairs.
