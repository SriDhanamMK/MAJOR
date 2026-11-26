package com.ast.similarity.comparison;

import com.ast.similarity.dto.ScoreExplanationDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScoringService {

    public static class ScoreComputation {
        private final double similarityPercentage;
        private final ScoreExplanationDto explanation;

        public ScoreComputation(double similarityPercentage, ScoreExplanationDto explanation) {
            this.similarityPercentage = similarityPercentage;
            this.explanation = explanation;
        }

        public double getSimilarityPercentage() { return similarityPercentage; }
        public ScoreExplanationDto getExplanation() { return explanation; }
    }

    /**
     * Calculates: S(P,Q) = 100 * (1 - D(P,Q) / (|T'_P| + |T'_Q|))
     */
    public ScoreComputation computeScore(double treeEditDistance, int sizeA, int sizeB) {
        int denominator = sizeA + sizeB;

        if (denominator == 0) {
            ScoreExplanationDto exp = new ScoreExplanationDto(
                    "S(P,Q) = 100 * (1 - D / (|T_A| + |T_B|))",
                    0.0, 0, 0, 0, 100.0, 100.0,
                    List.of("Both trees are empty; similarity is defined as 100.00% by default.")
            );
            return new ScoreComputation(100.0, exp);
        }

        double normalizedRatio = treeEditDistance / (double) denominator;
        double rawScore = 100.0 * (1.0 - normalizedRatio);

        // Clamp to ensure mathematical safety within [0.0, 100.0]
        double clampedScore = Math.max(0.0, Math.min(100.0, rawScore));
        double roundedScore = Math.round(clampedScore * 100.0) / 100.0;

        List<String> steps = new ArrayList<>();
        steps.add("Step 1: Compute post-order tree node counts: |T'_A| = " + sizeA + ", |T'_B| = " + sizeB);
        steps.add("Step 2: Compute safe upper-bound denominator: |T'_A| + |T'_B| = " + denominator);
        steps.add("Step 3: Execute Zhang-Shasha tree-edit distance: D(T'_A, T'_B) = " + String.format("%.2f", treeEditDistance));
        steps.add("Step 4: Compute normalized edit ratio: D / (|T'_A| + |T'_B|) = " + String.format("%.4f", normalizedRatio));
        steps.add("Step 5: Apply formula S = 100 * (1 - " + String.format("%.4f", normalizedRatio) + ") = " + String.format("%.2f%%", roundedScore));

        ScoreExplanationDto explanation = new ScoreExplanationDto(
                "S(P,Q) = 100 * (1 - D(P,Q) / (|T'_P| + |T'_Q|))",
                Math.round(treeEditDistance * 100.0) / 100.0,
                sizeA,
                sizeB,
                denominator,
                rawScore,
                roundedScore,
                steps
        );

        return new ScoreComputation(roundedScore, explanation);
    }
}
