

\# Reviewer 3 – Comparison with Existing Clone Detection Tools



\## Reviewer Comment



The proposed framework should be compared with established source-code similarity detection tools such as JPlag and MOSS to demonstrate its relative performance.



\## Response



We conducted supplementary comparative experiments using JPlag and MOSS. The experiments were performed on controlled datasets containing similar and unrelated source-code pairs.



Since the tools were evaluated using different datasets and configurations, the results are reported separately and are not treated as a direct, controlled head-to-head comparison.



\---



\## 1. Comparison with JPlag



\### Experimental Configuration



\- Tool: JPlag 4.3.0

\- Languages: C, C++, and Python

\- Controlled dataset: 210 pairs

\- Similar pairs: 180

\- Unrelated pairs: 30

\- JPlag similarity threshold: 0.5



\### Results



| Language | TP | FP | TN | FN | Precision | Recall | F1-score | Accuracy |

|---|---:|---:|---:|---:|---:|---:|---:|---:|

| C | 60 | 1 | 9 | 0 | 98.36% | 100% | 99.17% | 98.57% |

| C++ | 60 | 1 | 9 | 0 | 98.36% | 100% | 99.17% | 98.57% |

| Python | 60 | 4 | 6 | 0 | 93.75% | 100% | 96.77% | 94.29% |

| \*\*Overall\*\* | \*\*180\*\* | \*\*6\*\* | \*\*24\*\* | \*\*0\*\* | \*\*96.77%\*\* | \*\*100%\*\* | \*\*98.36%\*\* | \*\*97.14%\*\* |



\### Interpretation



JPlag achieved high recall across all three languages on the controlled dataset. It detected all labelled similar pairs, with six unrelated pairs incorrectly identified as similar.



The results provide a supplementary reference for evaluating the proposed framework against an established token-based clone detection tool.



\---



\## 2. Comparison with MOSS



\### Experimental Configuration



\- Tool: Stanford MOSS

\- Language: C

\- Evaluated pairs: 70

\- Similar pairs: 60

\- Unrelated pairs: 10

\- MOSS configuration: `-l c -m 10`

\- Pairwise score: Conservative minimum of the two reported file similarity percentages.

\- Classification threshold: 50%.



\### Results



| Metric | Result |

|---|---:|

| True Positives (TP) | 60 |

| False Positives (FP) | 0 |

| True Negatives (TN) | 10 |

| False Negatives (FN) | 0 |

| Precision | 100% |

| Recall | 100% |

| F1-score | 100% |

| Accuracy | 100% |



\### Interpretation



MOSS detected all 60 similar pairs in the selected C dataset. No unrelated pairs were classified as similar using the specified conservative pairwise scoring procedure and threshold.



Two unrelated pairs received reported similarity values of 40% and 47%, respectively, while the remaining eight were not reported as matches.



These results apply only to the selected dataset and scoring procedure.



\---



\## 3. Comparison with the Proposed AST-Based Framework



The proposed framework was evaluated separately using its held-out test split.



\### Proposed Framework Results



| Metric | Result |

|---|---:|

| Programming languages | C, C++, Python |

| Total test pairs | 318 |

| True Positives (TP) | 192 |

| False Positives (FP) | 2 |

| True Negatives (TN) | 112 |

| False Negatives (FN) | 12 |

| Precision | 98.97% |

| Recall | 94.12% |

| F1-score | 96.48% |

| Accuracy | 95.60% |



Configuration:

\- Statement reordering: Enabled.

\- Development threshold: 89.03%.



\### Summary of Comparative Results



| Tool | Languages | Pairs | Precision | Recall | F1-score | Accuracy |

|---|---|---:|---:|---:|---:|---:|

| Proposed AST Framework | C, C++, Python | 318 | 98.97% | 94.12% | 96.48% | 95.60% |

| JPlag 4.3.0 | C, C++, Python | 210 | 96.77% | 100% | 98.36% | 97.14% |

| MOSS | C | 70 | 100% | 100% | 100% | 100% |



\---



\## 4. Discussion



The supplementary comparison demonstrates that established tools such as JPlag and MOSS achieve strong results on the selected controlled datasets.



The proposed framework also demonstrates competitive performance, with 98.97% precision, 94.12% recall, and a 96.48% F1-score on its held-out test split.



The proposed approach differs in its use of normalized abstract syntax trees and tree edit distance. It provides a structural representation of source code and supports C, C++, and Python through a common AST-based processing pipeline.



However, the experiments were conducted on different datasets and under different tool configurations. Therefore, the reported metrics should not be interpreted as a definitive ranking of the tools. A controlled comparison using identical labelled pairs and equivalent evaluation protocols would be required for a strict performance comparison.



\## 5. Supporting Files



The following files contain the experimental results and supporting information:



\- `external\_benchmark/Comparative\_Results\_Table.md`

\- `external\_benchmark/MOSS\_Comparison\_Results.csv`

\- `external\_benchmark/MOSS\_Comparison\_Report.md`

\- JPlag comparison report and result files.



\## 6. Conclusion for Reviewer 3



We have added supplementary comparisons with JPlag and MOSS to provide context for the performance of the proposed AST-based framework. The comparison highlights the framework's multi-language structural analysis approach while acknowledging differences in datasets and experimental configurations.



