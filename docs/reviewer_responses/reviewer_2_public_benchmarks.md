

\# Reviewer 2 – Validation Using Public Source-Code Clone Benchmarks



\## Reviewer Comment



The experimental validation should be extended using established public source-code clone detection benchmarks to demonstrate the effectiveness and generalizability of the proposed approach.



\## Response



We extended the evaluation using two publicly available source-code datasets:



1\. SFI-Lero SSCD benchmark for C and C++.

2\. Python Plagiarism Code Dataset hosted on Hugging Face.



These evaluations provide additional evidence of the framework's performance across multiple programming languages and different types of code transformations.



\---



\## 1. SFI-Lero SSCD Benchmark (C and C++)



\### Dataset



\- Dataset: SFI-Lero SSCD

\- Repository: `external\_benchmark/SSCD`

\- C dataset: 80 clone pairs.

\- C++ dataset: 100 clone pairs.

\- Total: 180 labelled positive clone pairs.



The benchmark contains function-level, block-level, and fragment-level clone pairs.



\### Experimental Configuration



\- Proposed AST-based similarity detection framework.

\- Programming languages: C and C++.

\- Similarity threshold: 89.03%.

\- All 180 clone pairs were submitted to the comparison API.

\- No API errors occurred.



\### Results



| Clone Type | Total Pairs | Parser Success | Detected | Recall |

|---|---:|---:|---:|---:|

| C Block | 3 | 2 | 0 | 0.00% |

| C Function | 77 | 67 | 8 | 11.94% |

| C++ Block | 6 | 3 | 1 | 33.33% |

| C++ Fragment | 9 | 5 | 3 | 60.00% |

| C++ Function | 85 | 80 | 53 | 66.25% |

| \*\*Overall\*\* | \*\*180\*\* | \*\*157\*\* | \*\*65\*\* | \*\*41.40%\*\* |



\### Overall Observations



\- 157 out of 180 pairs were successfully parsed.

\- Parser coverage was 87.22%.

\- 65 of the 157 successfully parsed pairs were detected.

\- Recall among successfully parsed pairs was 41.40%.

\- End-to-end detection rate, counting parser failures, was 36.11%.

\- There were 23 parser failures and zero API errors.



\### Language-wise Results



| Language | Total Pairs | Parsed | Detected | Recall |

|---|---:|---:|---:|---:|

| C | 80 | 69 | 8 | 11.59% |

| C++ | 100 | 88 | 57 | 64.77% |

| \*\*Overall\*\* | \*\*180\*\* | \*\*157\*\* | \*\*65\*\* | \*\*41.40%\*\* |



\### Interpretation



The results demonstrate that the framework can process and identify a portion of the public C/C++ clone pairs. However, detection performance varies considerably across clone types and programming languages.



The relatively lower C results and parser failures indicate limitations in the current parsing and structural matching pipeline, particularly for some benchmark code structures.



\*\*Important limitation:\*\* The selected SSCD files contain positive clone pairs only. Therefore, precision, specificity, F1-score, and accuracy cannot be calculated reliably from this evaluation. The results are reported using parser coverage and recall-related measures.



\### Result File



`results/SSCD\_Detailed\_Results.csv`



Evaluation script:



`evaluation/run\_sscd\_benchmark.py`



\---



\## 2. Python Plagiarism Code Dataset



\### Dataset



\- Dataset: Python Plagiarism Code Dataset.

\- Source: `nop12/python\_plagiarism\_code\_dataset` on Hugging Face.

\- Local dataset: `external\_benchmark/AtCoder/python\_plagiarism.parquet`.

\- Total records: 15,176.

\- Unique programming assignments: 974.

\- Models represented: DeepSeek Coder and GPT-4o-mini.

\- Transformation levels: Original and Levels 1–6.



The dataset contains original Python implementations and transformed variants generated using language models.



\### Experimental Configuration



A controlled sample of 120 code pairs was selected:



\- 60 positive pairs: original implementations compared with transformed implementations from the same assignment.

\- 60 negative-control pairs: original implementations from different assignments belonging to the same model group.



For the positive pairs, five pairs were selected per transformation level for each model, covering Levels 1–6.



The following configuration was used:



\- Similarity threshold: 89.03%.

\- Literal abstraction: Disabled.

\- Statement reordering: Enabled.

\- All comparisons were performed through the proposed framework's API.



\### Overall Results



| Metric | Result |

|---|---:|

| Total pairs | 120 |

| True Positives (TP) | 34 |

| False Positives (FP) | 0 |

| True Negatives (TN) | 60 |

| False Negatives (FN) | 26 |

| Precision | 100.00% |

| Recall | 56.67% |

| F1-score | 72.34% |

| Accuracy | 78.33% |

| API Errors | 0 |



\### Transformation-Level Results



| Transformation Level | Pairs | Detected | Recall |

|---|---:|---:|---:|

| Level 1 | 10 | 10 | 100% |

| Level 2 | 10 | 10 | 100% |

| Level 3 | 10 | 10 | 100% |

| Level 4 | 10 | 3 | 30% |

| Level 5 | 10 | 0 | 0% |

| Level 6 | 10 | 1 | 10% |

| \*\*Overall\*\* | \*\*60\*\* | \*\*34\*\* | \*\*56.67%\*\* |



\### Model-wise Results



| Model | Positive Pairs | Detected | Recall | Negative Controls Flagged |

|---|---:|---:|---:|---:|

| DeepSeek Coder | 30 | 16 | 53.33% | 0/30 |

| GPT-4o-mini | 30 | 18 | 60.00% | 0/30 |



\### Interpretation



The framework achieved 100% precision on this selected sample, with a recall of 56.67% and an F1-score of 72.34%.



Detection performance was strongest for transformation Levels 1–3. Performance decreased for Levels 4–6, suggesting that more substantial code transformations can make structural similarity harder to detect.



No negative-control pairs were incorrectly classified as clones in this sample.



\*\*Important limitation:\*\* This is a limited constructed evaluation sample, not a complete independent benchmark evaluation. Positive and negative labels were derived from assignment and transformation-level relationships in the dataset. The results should therefore be interpreted as sample-based experimental evidence rather than universal real-world performance.



\### Result File



`results/Python\_Plagiarism\_Detailed\_Results.csv`



Evaluation script:



`evaluation/run\_python\_plagiarism\_benchmark.py`



Dataset README:



`external\_benchmark/AtCoder/python\_dataset\_README.md`



\---



\## 3. Overall Discussion



The two public datasets provide complementary evaluation evidence:



\- The SSCD benchmark evaluates C and C++ clone pairs across multiple clone types.

\- The Python dataset evaluates similarity detection across multiple levels of generated code transformations.

\- The framework successfully processed all submitted requests without API errors in both experiments.

\- The experiments also reveal limitations in parser coverage and detection of heavily transformed code.



The results indicate that the proposed framework can identify structural similarities across multiple programming languages, while also highlighting areas requiring improvement, particularly complex transformations and parser robustness.



These results are reported separately because the datasets use different languages, sampling procedures, and evaluation protocols. Their metrics should not be directly combined into a single performance score.



\## 4. Reproducibility and Supporting Files



| Resource | Repository Path |

|---|---|

| SSCD evaluation script | `evaluation/run\_sscd\_benchmark.py` |

| SSCD detailed results | `results/SSCD\_Detailed\_Results.csv` |

| Python evaluation script | `evaluation/run\_python\_plagiarism\_benchmark.py` |

| Python detailed results | `results/Python\_Plagiarism\_Detailed\_Results.csv` |

| Python dataset README | `external\_benchmark/AtCoder/python\_dataset\_README.md` |

| SSCD benchmark | `external\_benchmark/SSCD` |

| Python dataset | `external\_benchmark/AtCoder/python\_plagiarism.parquet` |



\## 5. Conclusion for Reviewer 2



The evaluation was extended using two publicly available datasets covering C, C++, and Python. The experiments demonstrate the framework's ability to detect structural similarities across different programming languages and transformation levels. The observed limitations in parser coverage and heavily transformed code are also acknowledged and provide directions for future improvement.



