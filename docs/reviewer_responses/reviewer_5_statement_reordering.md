

\# Reviewer 5 – Analysis of Statement Reordering Performance



\## Reviewer Comment



The comparatively lower F1-score of 84.14% reported for statement reordering requires further investigation and explanation.



\## Response



We conducted additional experiments to investigate the effect of statement reordering on the similarity detection performance of the proposed AST-based framework.



The additional evaluation examined the framework with statement reordering enabled and disabled, and also analysed detection performance across different reordering levels.



\---



\## 1. Additional Evaluation



An additional statement-reordering experiment was conducted using 79 code pairs.



The results showed:



\- Recall with statement reordering enabled: 100%.

\- Recall with statement reordering disabled: 100%.

\- Mean similarity score with reordering enabled: approximately 97.25%.

\- Mean similarity score with reordering disabled: approximately 97.30%.



These results indicate that the selected additional test pairs were detected under both configurations.



\---



\## 2. Evaluation on the Held-Out Test Set



The main held-out evaluation with statement reordering enabled used 318 pairs.



\### Results



| Metric | Result |

|---|---:|

| True Positives (TP) | 192 |

| False Positives (FP) | 2 |

| True Negatives (TN) | 112 |

| False Negatives (FN) | 12 |

| Precision | 98.97% |

| Recall | 94.12% |

| F1-score | 96.48% |

| Accuracy | 95.60% |

| AUC | 0.9881 |



The development threshold of 89.03% was used for this evaluation.



\---



\## 3. Analysis Across Reordering Levels



The additional reordering evaluation considered four levels, with 19, 20, 20, and 20 pairs respectively.



| Reordering Level | Number of Pairs | Recall |

|---|---:|---:|

| Level 1 | 19 | 100% |

| Level 2 | 20 | 100% |

| Level 3 | 20 | 100% |

| Level 4 | 20 | 100% |

| \*\*Overall\*\* | \*\*79\*\* | \*\*100%\*\* |



\### Mean Similarity Scores



| Reordering Level | Mean Similarity Score |

|---|---:|

| Level 1 | 98.479% |

| Level 2 | 98.096% |

| Level 3 | 97.113% |

| Level 4 | 95.384% |



The mean similarity score decreases as the extent of statement reordering increases. Nevertheless, all pairs in this additional sample remained above the selected detection threshold.



\---



\## 4. Interpretation of the Original 84.14% F1-score



The additional experiments did not reproduce the previously reported F1-score of 84.14%.



The new held-out evaluation produced an F1-score of 96.48% with statement reordering enabled.



The additional 79-pair experiment also achieved 100% recall under both configurations.



However, the original experimental dataset and exact evaluation configuration were not recovered sufficiently to establish the precise reason for the difference between the original and additional results.



Therefore, the difference should not be attributed to a specific algorithmic improvement or dataset characteristic without further evidence.



The results are reported separately to maintain transparency and avoid treating experiments with different evaluation protocols as directly interchangeable.



\---



\## 5. Important Experimental Considerations



A separate evaluation with statement reordering disabled produced:



| Metric | Result |

|---|---:|

| Threshold | 89.63% |

| True Positives (TP) | 199 |

| False Positives (FP) | 3 |

| True Negatives (TN) | 111 |

| False Negatives (FN) | 5 |

| Precision | 98.51% |

| Recall | 97.55% |

| F1-score | 98.03% |

| Accuracy | 97.48% |



The threshold for this configuration was separately tuned. Consequently, the enabled and disabled results should not be interpreted as a strictly controlled comparison of the effect of reordering alone.



\---



\## 6. Discussion and Limitations



The experiments indicate that statement reordering can be handled effectively when the reordered statements satisfy the dependency conditions implemented by the framework.



The dependency analyzer examines read-after-write, write-after-read, and write-after-write relationships, along with side effects and control-flow constraints.



However, the implementation focuses on adjacent statement independence. It does not establish unrestricted or transitive independence across arbitrary statement sequences.



More complex reorderings and control-flow changes may therefore reduce structural similarity scores or affect detection performance.



\---



\## 7. Conclusion for Reviewer 5



Additional experiments were conducted to investigate the effect of statement reordering. The new held-out evaluation achieved an F1-score of 96.48%, while the additional 79-pair reordering experiment achieved 100% recall.



The original F1-score of 84.14% was not reproduced under the additional evaluation protocol. Since the original configuration could not be fully reconciled with the new experiments, both results are reported transparently rather than attributing the difference to an unsupported explanation.



The limitations of dependency-informed statement reordering are also acknowledged.



