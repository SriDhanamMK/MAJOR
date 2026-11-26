# JPlag Comparative Evaluation Results

## Experimental configuration

- Tool: JPlag 4.3.0
- Languages: C, C++, Python
- Similarity threshold: 0.5 (fixed for all languages)
- Dataset: Controlled source-code similarity comparison dataset
- Pairs evaluated: 210 (70 per language)
- Ground truth: 60 similar and 10 unrelated pairs per language.

## Results

| Language | Pairs | TP | FP | TN | FN | Precision (%) | Recall (%) | F1 (%) | Accuracy (%) |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| C | 70 | 60 | 1 | 9 | 0 | 98.36 | 100.00 | 99.17 | 98.57 |\n| C++ | 70 | 60 | 1 | 9 | 0 | 98.36 | 100.00 | 99.17 | 98.57 |\n| Python | 70 | 60 | 4 | 6 | 0 | 93.75 | 100.00 | 96.77 | 94.29 |\n| Overall | 210 | 180 | 6 | 24 | 0 | 96.77 | 100.00 | 98.36 | 97.14 |\n
## Methodology

JPlag pairwise similarity scores were extracted from the generated
JSON comparison reports. A fixed threshold of 0.5 was used to
classify a pair as similar. The same threshold was applied to
all three programming languages.

Precision, recall, F1-score and accuracy were calculated against
the labelled ground-truth pairs.

## Limitations

This is a controlled, synthetic comparative dataset containing
10 programming tasks per language. It is not a standard public
clone-detection benchmark. The results should not be generalized
to large real-world repositories without further evaluation.

The threshold was fixed at 0.5 and was not tuned against the
ground-truth labels.
