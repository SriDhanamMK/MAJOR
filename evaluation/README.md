# Reproducible evaluation – AST-based source-code similarity

Everything here runs against the **unmodified** application (`backend/`). The production
similarity formula is `S = 100 * (1 - D / (|T_A| + |T_B|))` (`ScoringService.java`). An alternative
`max` normalisation is computed *offline* from the returned `D` and node counts; production code is not changed.

## Status (what has and has not been executed)

| Item | Status |
|---|---|
| Dataset generation and validation (`make_dataset.py`) | **Executed** – 633 pairs, 446 programs, all compiled/run |
| Evaluation harness (`Evaluation_Script.py`) | Written; pipeline tested with a synthetic scorer only (`--selftest`). **Not yet run on the real system** |
| Reordering study (`analyze_reordering.py`) | Written; tested on synthetic scores only |
| Scalability workloads (`scalability.py`) | Workloads generated and syntax-checked (C, C++, Python, 1–128 functions). **Timing not run** |
| Existing 19 JUnit tests | **Not re-run** here (no Maven cache, no network). The repo's committed surefire report shows 19/19 passed on the author's machine |
| Public benchmarks, JPlag, MOSS | **Not done** (see "Limits") |

Reason: the machine used to prepare this had no `tree_sitter*` Python packages and could not install packages,
so the application could not parse anything. No similarity score in this folder comes from a stand-in parser.

## Reproduce (on a machine where the application already works)

```bash
# 0. Python parser dependencies used by backend/parser-engine/treesitter_parser.py
pip install tree-sitter tree-sitter-c tree-sitter-cpp tree-sitter-python
pip freeze | grep -i tree     # record the real versions (the health endpoint prints hard-coded ones)

# 1. Start the backend (from backend/, so parser-engine/treesitter_parser.py resolves)
cd backend && java -jar target/ast-code-similarity-backend-1.0.0-SNAPSHOT.jar   # or: mvn spring-boot:run
#    parser.python.executable is "python" in application.properties – make sure that interpreter has the packages

# 2. Dataset (needs gcc, g++, python3). Deterministic (fixed seed).
cd evaluation && python3 make_dataset.py --out dataset

# 3. Score + dev-only threshold + frozen test evaluation (reordering canonicalisation ON = application default)
python3 Evaluation_Script.py --server http://localhost:8080 --out results_on  --enable-reordering true
python3 Evaluation_Script.py --server http://localhost:8080 --out results_off --enable-reordering false

# 4. Reordering study (Step 7)
python3 analyze_reordering.py --on results_on/Experimental_Results.csv --off results_off/Experimental_Results.csv \
        --thresholds results_on/thresholds.json --out results_reorder

# 5. Scalability (Step 8). Optional --server-pid samples JVM + parser memory
python3 scalability.py --server http://localhost:8080 --server-pid <PID> --repeats 5
python3 -c "import platform;print(platform.platform())"; java -version; nproc; free -h   # record hardware
```

Harness self-test (synthetic scores, proves the pipeline only, never report these): `python3 Evaluation_Script.py --selftest`.

## Dataset design (`dataset/`)

* 20 base tasks: 8 C, 4 C++ (native `std::vector`/`cout`), 8 Python. Each has an original and an
  *independently written* alternative solution. Sizes small/medium/large (11–60 lines of task code).
* **Split by base program**: `dev` tasks pick the threshold, `test` tasks are only used to report. No variant of a
  test base appears in dev. Negatives pair programs from the same split and language.
* **Positives (label 1)**: formatting (3 variants/language), comments added/removed, rename locals, rename locals **and**
  function names, reordering at 10/25/50/75 % (2 seeds each), for→while, nested condition, temporary variable,
  equivalent arithmetic, statement splitting, light and heavy combinations.
* **Negatives (label 0)**: `unrelated` (different tasks, including transformed copies of the other program) and
  `independent_solution` (same task, independently written). The second kind is deliberately labelled *not a copy*:
  it measures false positives on legitimately similar solutions.
* **Validation**: every program is compiled (`gcc -std=gnu11`, `g++ -std=c++17`) or byte-compiled and **executed**; a
  transformed program is kept only if its stdout equals the original's. Negative controls (a behaviour-changing edit, a
  syntax error) were verified to be caught. `program_validation.csv`, `generation_log.csv` hold the evidence.
* Reordering moves lines of a block of 10 independent statements (5 literal, 3 arithmetic, 2 call-initialised). The
  moved-statement kind is stored in `meta` for the Step 7 analysis.

Per-category metrics: *positives of the category + all negatives of the same split*. Recall is category specific;
precision depends on the shared negatives.

## Limits (be explicit about these in the paper)

* Programs are small, synthetic, author-generated; reordering uses an artificial block of independent initialisations,
  which favours canonicalisation. "C++" coverage is four native programs. Results do not transfer to real student code.
* Transformations are mechanical. Semantic equivalence is checked by running one fixed input per program, not by proof.
* **BigCloneBench** (Java; the system supports C/C++/Python only) is not directly usable. **POJ-104** (C/C++) is a
  *classification/retrieval* benchmark (104 problems), so a pair is "similar" only in the sense of "same problem", not
  "copied"; using it needs an explicit protocol (e.g. mean average precision for same-problem retrieval) and must not be
  presented as plagiarism ground truth. Not run.
* **JPlag**: needs the JPlag jar (Java) and a network download; **MOSS**: needs a registered user id and uploads to a
  remote server. Neither was available. Not run, no numbers reported.
* The API returns total time only. Parse / normalise / TED stage timings need a small test-scope benchmark class that calls
  `ParserEngineBridge`, `AstNormalizer` and `TreeEditDistance` separately; not written.
* Zhang–Shasha complexity: time O(|T1|·|T2|·min(depth1,leaves1)·min(depth2,leaves2)), space O(|T1|·|T2|) (the code
  allocates a `(m+1)×(n+1)` double matrix plus a forest matrix per keyroot pair). This is the textbook bound; measured
  scaling must come from `scalability.py`.

## Findings in the existing code that affect the paper (not changed)

1. Normalisation, TED and scoring are in **Java**; Python only runs Tree-sitter (paper Sec. IV and Fig. 3 said Python).
2. Eq. (1) in the paper used `max(|T_P|,|T_Q|)`; code divides by `|T_P|+|T_Q|`.
3. Substitution costs are graded (0 / 0.2 / 0.4 / 0.6 / 1.0), not unit cost.
4. `enableReordering` defaults to **true**; the paper said statement order is not handled.
5. To verify by test: `AstTreeBuilder.mapCategory` tests `endsWith("_statement")` before the control-flow test, so
   `if/for/while` statements appear to map to a generic `Statement` type; C `int x = 1;` appears to record `x` as a *use*,
   not a definition (`annotateNodeDataflow`); reordering groups are formed by *adjacent-pair* independence, which is not transitive.
6. `TreeEditDistance.reconstructOperations` backtracks over the subtree-distance matrix as if it were a sequence
   alignment, so the matched/inserted/deleted counts shown in the UI may not describe an optimal edit script. The distance `D` itself is the Zhang–Shasha value.
7. `SymbolScope.PRESERVED_SYMBOLS` is never used.
