

\# Reviewer 6 – Computational Complexity and Scalability Analysis



\## Reviewer Comment



The paper should report computational complexity, execution time, and scalability across multiple programming languages and larger source-code repositories.



\## Response



We extended the evaluation to include a theoretical complexity analysis and empirical scalability experiments across C, C++, and Python.



The analysis considers the tree edit distance algorithm used by the proposed framework and measures execution time for increasing source-code sizes.



\---



\## 1. Theoretical Computational Complexity



The proposed framework uses the Zhang–Shasha tree edit distance algorithm to calculate structural differences between normalized abstract syntax trees.



Let:



\- \\(n\_1\\) and \\(n\_2\\) represent the number of nodes in the two ASTs.

\- \\(d\_1\\) and \\(d\_2\\) represent their respective depths.

\- \\(l\_1\\) and \\(l\_2\\) represent their respective numbers of leaves.



The time complexity of the Zhang–Shasha algorithm is:



\\\[

O(n\_1 n\_2 \\min(d\_1,l\_1)\\min(d\_2,l\_2))

\\]



The space complexity is:



\\\[

O(n\_1 n\_2)

\\]



The actual execution time depends on AST size and structure, particularly the number of nodes, tree depth, and number of leaves.



The overall framework also includes parsing and AST normalization operations before tree edit distance computation.



\---



\## 2. Scalability Evaluation Methodology



A scalability experiment was conducted using source-code examples of increasing sizes.



The evaluation covered:



\- Programming languages: C, C++, and Python.

\- Eight source-code size configurations.

\- Five repeated measurements per configuration.

\- 24 language-size configurations in total.



The number of functions in the evaluated source examples was increased progressively from 1 to 128.



Execution time was measured through the comparison API. Median execution time was used to reduce the influence of individual timing variations.



The evaluation was performed using the existing application implementation without changing the similarity algorithm.



\---



\## 3. Execution Time Results



The following table reports the median execution time in milliseconds.



| Number of Functions | C (ms) | C++ (ms) | Python (ms) |

|---:|---:|---:|---:|

| 1 | 987.45 | 812.23 | 808.42 |

| 2 | 991.79 | 853.11 | 974.78 |

| 4 | 1046.34 | 914.67 | 1012.59 |

| 8 | 1329.03 | 1121.89 | 1137.54 |

| 16 | 1681.86 | 1916.23 | 1328.07 |

| 32 | 4040.16 | 5359.44 | 2763.79 |

| 64 | 13808.78 | 19822.51 | 9013.96 |

| 128 | 56465.81 | 81233.25 | 36663.97 |



\### Observations



\- Execution time generally increases with source-code size.

\- The largest evaluated C example required a median of approximately 56.47 seconds.

\- The largest evaluated C++ example required approximately 81.23 seconds.

\- The largest evaluated Python example required approximately 36.66 seconds.

\- The results demonstrate that larger ASTs require substantially more processing time.



The measured execution time includes the relevant application and parsing overhead. In particular, Python parser subprocess execution time is included in the wall-clock measurement.



\---



\## 4. Largest Evaluated AST Sizes



The largest evaluated examples contained the following AST node counts:



| Language | Number of Functions | AST Nodes | Median Execution Time |

|---|---:|---:|---:|

| C | 128 | 10,025 | 56.47 seconds |

| C++ | 128 | 12,081 | 81.23 seconds |

| Python | 128 | 8,205 | 36.66 seconds |



The results indicate that the framework can process examples containing several thousand AST nodes, although execution time increases considerably for larger inputs.



\---



\## 5. Additional Real-World Source-Code Evaluation



Additional execution-time measurements were performed using selected source files from the SQLite codebase.



These experiments examined comparisons between source files of different AST sizes.



| Source File Pair | AST Nodes | Tree Edit Distance | Similarity | Median Time |

|---|---:|---:|---:|---:|

| hash.c vs table.c | 1367 + 1041 | 1143.6 | 52.51% | 2.290 s |

| hash.c vs mem0.c | 1367 + 158 | 1252.8 | 17.85% | 1.198 s |

| vtab.c vs mem0.c | 7571 + 158 | 7439.6 | 3.74% | 2.122 s |



These measurements provide additional execution-time evidence using real-world source files.



They are performance measurements only and should not be interpreted as labelled clone-detection accuracy results.



\---



\## 6. Memory Measurement



During scalability testing, process memory was sampled using the application process and its recursive child processes.



The measurements represent combined process-level memory observations rather than isolated memory consumption for an individual comparison request.



This distinction is important because the application includes parser processes and other runtime components.



\---



\## 7. Discussion



The theoretical analysis indicates that tree edit distance computation can become expensive as AST sizes increase.



The scalability experiments are consistent with this observation: execution time increases substantially for larger source-code examples.



The results also show differences among C, C++, and Python. These differences may be influenced by parser behaviour, generated AST structure, and runtime overhead.



The experiments provide practical evidence of the current implementation's processing capability while identifying large AST comparisons as an area for further optimization.



\---



\## 8. Limitations



The scalability evaluation has the following limitations:



\- The experiments use controlled source-code examples with increasing numbers of functions.

\- The largest tested examples contain up to 128 functions.

\- The measurements include application and parsing overhead rather than isolating tree edit distance computation alone.

\- Memory measurements represent combined process-level observations.

\- The experiments do not establish unlimited scalability for arbitrarily large repositories.

\- The SQLite comparisons measure execution performance and are not labelled clone-detection evaluations.



\---



\## 9. Supporting Files



The scalability evaluation is supported by:



\- Evaluation script: `evaluation/scalability.py`

\- Results: `results/Scalability\_Results.csv`



The experiment can be reproduced using:



```cmd

python evaluation\\scalability.py --server http://localhost:8080 --server-pid 6668 --repeats 5

```



\---



\## 10. Conclusion for Reviewer 6



We have added theoretical time and space complexity analysis for the Zhang–Shasha tree edit distance algorithm and conducted empirical scalability experiments across C, C++, and Python.



The execution-time results demonstrate the framework's behaviour across increasing AST sizes and provide additional performance measurements using real-world SQLite source files.



The limitations of the current implementation and the computational cost associated with larger AST comparisons are also acknowledged.



