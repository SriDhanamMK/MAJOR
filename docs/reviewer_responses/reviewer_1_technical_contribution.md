\# Reviewer 1 – Technical Contribution and Implementation Evidence



\## Reviewer Comment



Establish the original technical contribution of the proposed framework beyond conventional AST normalization and tree edit distance techniques.



\## 1. Overview of the Proposed Contribution



This work presents an integrated, configurable AST-based source-code similarity detection framework for C, C++, and Python.



The framework combines language-specific parsing adapters, a common AST representation, structural normalization, identifier canonicalization, optional literal abstraction, dependency-informed statement reordering, and weighted Zhang–Shasha tree edit distance.



The contribution primarily lies in the integration and implementation of these components into a unified comparison pipeline, together with configurable transformations and interpretable structural comparison results. The work does not propose a new tree edit distance algorithm.



\## 2. Multi-Language Parsing Architecture



The framework supports three programming languages:



\- C

\- C++

\- Python



Each language has a dedicated parser adapter:



\- `CParserAdapter.java`

\- `CppParserAdapter.java`

\- `PythonParserAdapter.java`



All three implement the common `LanguageParserAdapter` interface.



The interface provides:

\- `getLanguage()`

\- `parseRaw(String sourceCode)`

\- `parseToCommonAst(String sourceCode, String idPrefix)`



The `ParserFactory.java` class dynamically registers available adapters and selects the appropriate adapter based on the requested language. It also supports aliases such as `C++`, `CXX`, and `PY`.



Each adapter uses the shared `ParserEngineBridge` and `AstTreeBuilder` to convert parser output into a common representation.



\### Common AST Representation



`CommonAstNode.java` provides the shared structure used by the comparison pipeline.



Important fields include:



\- `originalType` and `normalizedType`: original and normalized syntax node types.

\- `category`: AST node category.

\- `rawValue` and `normalizedValue`: original and normalized node values.

\- `startLine`, `startCol`, `endLine`, and `endCol`: source location information.

\- `children`: hierarchical AST relationships.

\- `matchStatus`, `matchedNodeId`, and `editCost`: comparison and alignment metadata.

\- `defVars`, `useVars`, and `hasSideEffects`: dependency-analysis metadata.



This common representation enables the same normalization and comparison stages to process ASTs from the three supported languages.



\## 3. Structural Normalization



The normalization pipeline is implemented through `AstNormalizer.java` and the associated normalization components.



\### 3.1 Formatting Normalization



`FormattingNormalizer.java` recursively removes AST nodes classified as comments.



This makes structural comparison less sensitive to comment differences.



Note: The current implementation removes comments. Although its documentation mentions collapsing redundant synthetic empty blocks, the inspected implementation does not perform that operation.



\### 3.2 Identifier Canonicalization



`IdentifierNormalizer.java` and `SymbolScope.java` implement scope-based identifier canonicalization.



The implementation:

\- Creates a global symbol scope.

\- Creates a new scope when processing a function declaration.

\- Registers function parameters using canonical names such as `param\_0`, `param\_1`, and so on.

\- Registers local variables using canonical names such as `var\_0`, `var\_1`, and so on.

\- Resolves identifiers through the current scope and its parent scopes.

\- Preserves unresolved identifiers in their original form.



Function names are explicitly preserved during normalization.



The normalizer also records identifier-renaming information, including original names, canonical names, symbol kinds, and source lines.



Implementation limitation: The inspected code does not establish complete block-level lexical scopes for every nested block. The preserved-symbol list is defined in `SymbolScope`, but its preservation check is not invoked by the registration and resolution methods inspected. Therefore, claims of complete language-specific lexical-scope correctness or universal built-in preservation should be avoided.



\### 3.3 Optional Literal Abstraction



`LiteralNormalizer.java` supports configurable literal abstraction.



When enabled, literals are mapped into categories:



\- Numeric literals → `<NUMERIC\_LITERAL>`

\- String and character literals → `<STRING\_LITERAL>`

\- Boolean literals → `<BOOLEAN\_LITERAL>`

\- Other literals → `<LITERAL>`



When disabled, the original literal values are retained.



This allows the framework to compare programs with either exact literal values or abstracted literal categories.



\## 4. Dependency-Informed Statement Reordering



`StatementDependencyAnalyzer.java` implements statement dependency analysis for optional statement-order normalization.



The implementation examines:

\- Read-after-write (RAW) dependencies.

\- Write-after-read (WAR) dependencies.

\- Write-after-write (WAW) dependencies.

\- Side-effect indicators.

\- Control-flow boundaries.



Statements considered independent are grouped and sorted using recursive structural signatures. Statements identified as dependent or associated with side effects or control-flow boundaries retain their order.



This mechanism is intended to reduce sensitivity to statement-order variations where dependency checks permit reordering.



Important implementation limitation: The inspected implementation checks adjacent statements when forming independent groups. Adjacent independence does not guarantee that every pair within a larger group is independent. Consequently, the current implementation should be described as dependency-informed reordering, not as a formal guarantee of semantics-preserving reordering.



\## 5. Weighted Tree Edit Distance



The framework uses the Zhang–Shasha tree edit distance algorithm, implemented in `TreeEditDistance.java`.



The implementation uses:

\- Postorder tree traversal.

\- Leftmost leaf descendant information.

\- Keyroot computation.

\- Forest-distance dynamic programming.

\- Insertion, deletion, and substitution costs.



The inspected substitution-cost scheme is:



| Comparison condition | Substitution cost |

|---|---:|

| Same normalized type and value | 0.0 |

| Same type, differing literal/operator value | 0.4 |

| Same type, other value difference | 0.2 |

| Same AST category, different specific type | 0.6 |

| Different AST categories | 1.0 |



Insertion and deletion costs are 1.0.



The framework uses the following normalized similarity score:



S(P,Q) = 100 × \[1 − D(P,Q) / (|T'P| + |T'Q|)]



where:

\- D(P,Q) is the weighted tree edit distance.

\- |T'P| and |T'Q| are the sizes of the normalized ASTs.



The score is clamped to the range 0–100 and rounded to two decimal places. The implementation assigns a score of 100% when both normalized trees are empty.



This scoring scheme provides a normalized structural similarity measure based on the weighted edit distance and the combined sizes of the normalized ASTs.



\## 6. Interpretable Comparison Results



The common AST representation stores comparison metadata, including:

\- Match status.

\- Matched node identifier.

\- Edit cost.



The comparison pipeline also aggregates structural edit information, including matches, substitutions, insertions, and deletions.



This provides information about structural differences rather than returning only a similarity score.



The framework also includes diagnostics and warnings to help users interpret comparison results. Structural similarity should not be interpreted as proof of semantic equivalence.



\## 7. Overall Technical Contribution



The defensible technical contribution of the proposed framework is the integration of:



1\. A common AST representation for C, C++, and Python.

2\. An adapter-based multi-language parsing architecture.

3\. Comment-insensitive structural normalization.

4\. Scope-based identifier canonicalization.

5\. Optional literal abstraction.

6\. Dependency-informed statement reordering.

7\. Weighted Zhang–Shasha tree edit distance.

8\. Normalized similarity scoring.

9\. Interpretable structural edit information.



Together, these components provide a configurable framework for comparing normalized program structures across multiple programming languages.



The contribution should be presented as an integrated framework and implementation with configurable structural transformations and empirical evaluation, rather than as a newly invented tree edit distance algorithm.



\## 8. Claims to Avoid



The following claims are not supported by the inspected implementation and should not be made without further verification:



\- The framework introduces a new tree edit distance algorithm.

\- Statement reordering is formally guaranteed to preserve program semantics.

\- All nested lexical scopes are handled completely.

\- All standard-library and built-in identifiers are universally protected from normalization.

\- Formatting normalization collapses redundant empty blocks.

\- Structural similarity proves functional or semantic equivalence.

\- The framework achieves identical parser coverage or accuracy across all three languages.



\## 9. Source Files Inspected



The following implementation files were inspected to establish the above details:



\*\*Parser architecture\*\*

\- `parser/CParserAdapter.java`

\- `parser/CppParserAdapter.java`

\- `parser/PythonParserAdapter.java`

\- `parser/LanguageParserAdapter.java`

\- `parser/ParserFactory.java`



\*\*AST representation\*\*

\- `ast/CommonAstNode.java`



\*\*Normalization\*\*

\- `normalization/AstNormalizer.java`

\- `normalization/FormattingNormalizer.java`

\- `normalization/IdentifierNormalizer.java`

\- `normalization/LiteralNormalizer.java`

\- `normalization/SymbolScope.java`



\*\*Comparison and dependency analysis\*\*

\- `TreeEditDistance.java`

\- `ScoringService.java`

\- `ComparisonEngine.java`

\- `StatementDependencyAnalyzer.java`



Note: The source files listed above should be checked against the repository before final submission. This document records implementation observations and is intended to support the response to Reviewer 1.

