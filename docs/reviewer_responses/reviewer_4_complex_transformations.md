

\# Reviewer 4 – Evaluation of Complex Structural Transformations



\## Reviewer Comment



The evaluation should investigate the framework's ability to detect code similarity under complex structural transformations and semantically equivalent implementations.



\## Response



We extended the experimental evaluation to investigate how different source-code transformations affect the similarity scores produced by the proposed AST-based framework.



The experiments considered transformations that preserve the intended computation while changing the source-code structure, identifiers, or statement organization.



\---



\## 1. Transformation Categories



The evaluation considered the following transformation categories:



| Transformation | Description |

|---|---|

| Formatting changes | Changes in whitespace, indentation, and code layout |

| Comment insertion | Addition or modification of comments |

| Identifier renaming | Renaming variables and identifiers |

| Statement reordering | Reordering independent adjacent statements |

| Loop transformation | Replacing a `for` loop with an equivalent `while` loop |

| Nested conditions | Changing the organization of conditional statements |

| Temporary variables | Introducing intermediate variables |

| Arithmetic restructuring | Rewriting expressions using equivalent arithmetic operations |

| Statement splitting | Dividing a statement into multiple statements |

| Combined transformations | Applying multiple transformations to the same implementation |



These transformations were selected to examine both superficial source-code changes and more substantial structural modifications.



\---



\## 2. Experimental Methodology



The experiments were conducted using C, C++, and Python source-code examples.



Original implementations were compared with their transformed variants using the proposed AST-based similarity detection framework.



The following procedure was used:



1\. Select original source-code implementations.

2\. Generate transformed variants using the selected transformation categories.

3\. Compile C and C++ implementations.

4\. Byte-compile or execute Python implementations.

5\. Retain transformed examples only when the tested output matched the original for the selected test input.

6\. Submit the retained pairs to the comparison API.

7\. Record the similarity scores and detection outcomes.



The framework used the existing development threshold of 89.03%.



Statement reordering was enabled during the evaluation.



\---



\## 3. Evaluation of Structural Transformations



The experiments examined how the framework responded to different levels of structural modification.



Formatting and comment changes are expected to have limited influence because the normalization pipeline removes comments and reduces certain superficial differences.



Identifier renaming is addressed through identifier normalization, which maps eligible parameter and local-variable names to canonical representations.



Statement reordering is handled through dependency-informed analysis. The implementation examines adjacent statements and considers read/write dependencies and side effects before allowing reordering.



More substantial transformations, including loop restructuring, nested conditions, temporary variables, arithmetic restructuring, and statement splitting, may produce different AST structures even when the tested outputs remain equivalent.



These transformations therefore provide a more challenging evaluation of structural similarity detection.



\---



\## 4. Semantic Equivalence Validation



To reduce the possibility of including incorrect transformations, the transformed programs were subjected to compilation, byte-compilation, or execution checks, depending on the programming language.



For the tested examples, transformed implementations were retained when their outputs matched those of the corresponding original implementations on the selected test input.



This procedure provides practical validation of the generated examples.



However, matching outputs on a single test input does not constitute a formal proof of semantic equivalence for all possible inputs. This limitation is acknowledged in the evaluation.



\---



\## 5. Discussion



The transformation experiments demonstrate the importance of distinguishing superficial source-code changes from deeper structural modifications.



The proposed framework uses AST normalization to reduce the effect of formatting, comments, and eligible identifier renaming.



Dependency-informed statement reordering provides additional flexibility when independent adjacent statements are rearranged.



However, transformations that substantially alter control flow or expression structure may still produce significant differences between normalized ASTs.



The framework therefore provides structural similarity analysis rather than a formal guarantee of semantic equivalence.



\---



\## 6. Limitations



The following limitations are acknowledged:



\- The transformation evaluation uses a constructed experimental dataset.

\- Output matching was checked using selected test inputs rather than exhaustive input domains.

\- Adjacent statement dependency analysis does not establish unrestricted or transitive statement independence.

\- Complex control-flow transformations may not preserve the same normalized AST structure.

\- The experiments do not establish formal semantic equivalence.



These limitations are important when interpreting the results and defining the scope of the proposed framework.



\---



\## 7. Conclusion for Reviewer 4



The evaluation was extended to include formatting changes, identifier renaming, statement reordering, loop transformations, nested conditions, temporary variables, arithmetic restructuring, statement splitting, and combinations of transformations.



The results and validation procedure provide additional evidence of the framework's behaviour under structurally modified implementations. The limitations of test-based semantic validation and structural matching are also explicitly acknowledged.



