package com.ast.similarity.comparison;

import com.ast.similarity.ast.CommonAstNode;
import com.ast.similarity.dto.*;
import com.ast.similarity.normalization.AstNormalizer;
import com.ast.similarity.normalization.IdentifierNormalizer;
import com.ast.similarity.parser.LanguageParserAdapter;
import com.ast.similarity.parser.ParserFactory;
import com.ast.similarity.parser.RawParseResult;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
public class ComparisonEngine {

    private final ParserFactory parserFactory;
    private final AstNormalizer astNormalizer;
    private final StatementDependencyAnalyzer dependencyAnalyzer;
    private final TreeEditDistance treeEditDistance;
    private final ScoringService scoringService;

    public ComparisonEngine(ParserFactory parserFactory,
                            AstNormalizer astNormalizer,
                            StatementDependencyAnalyzer dependencyAnalyzer,
                            TreeEditDistance treeEditDistance,
                            ScoringService scoringService) {
        this.parserFactory = parserFactory;
        this.astNormalizer = astNormalizer;
        this.dependencyAnalyzer = dependencyAnalyzer;
        this.treeEditDistance = treeEditDistance;
        this.scoringService = scoringService;
    }

    public CompareResponse compare(CompareRequest request) throws Exception {
        if (request == null) {
            throw new IllegalArgumentException("Comparison request cannot be null");
        }
        if (request.getSourceA() == null || request.getSourceA().trim().isEmpty() ||
                request.getSourceB() == null || request.getSourceB().trim().isEmpty()) {
            throw new IllegalArgumentException("Source code inputs cannot be empty");
        }

        String language = request.getLanguage();
        LanguageParserAdapter adapter = parserFactory.getAdapter(language);

        // 1. Parse both programs into raw Tree-sitter ASTs
        RawParseResult rawA = adapter.parseRaw(request.getSourceA());
        RawParseResult rawB = adapter.parseRaw(request.getSourceB());

        CompareResponse response = new CompareResponse();
        response.setComparisonId(UUID.randomUUID().toString());
        response.setLanguage(adapter.getLanguage());
        response.setTimestamp(Instant.now().toString());

        if (rawA.isHasErrors() || !"SUCCESS".equalsIgnoreCase(rawA.getStatus())) {
            response.setDiagnosticsA(rawA.getDiagnostics());
            response.getWarnings().add("Source Code A contains syntax errors. Structural comparison aborted.");
            response.setSimilarityScore(0.0);
            return response;
        }

        if (rawB.isHasErrors() || !"SUCCESS".equalsIgnoreCase(rawB.getStatus())) {
            response.setDiagnosticsB(rawB.getDiagnostics());
            response.getWarnings().add("Source Code B contains syntax errors. Structural comparison aborted.");
            response.setSimilarityScore(0.0);
            return response;
        }

        // 2. Convert to Common ASTs
        CommonAstNode treeA = adapter.parseToCommonAst(request.getSourceA(), "A_");
        CommonAstNode treeB = adapter.parseToCommonAst(request.getSourceB(), "B_");

        // 3. Normalization (formatting, scope-aware identifiers, literals)
        AstNormalizer.NormalizationResult normA = astNormalizer.normalize(treeA, request.isEnableLiteralAbstraction());
        AstNormalizer.NormalizationResult normB = astNormalizer.normalize(treeB, request.isEnableLiteralAbstraction());

        // 4. Conservative Transformation-Aware Statement Reordering Analysis
        List<StatementDependencyAnalyzer.ReorderingRecord> reordersA =
                dependencyAnalyzer.analyzeAndCanonicalizeReordering(normA.getNormalizedRoot(), request.isEnableReordering());
        List<StatementDependencyAnalyzer.ReorderingRecord> reordersB =
                dependencyAnalyzer.analyzeAndCanonicalizeReordering(normB.getNormalizedRoot(), request.isEnableReordering());

        // 5. Tree-Edit-Distance (Zhang-Shasha)
        TreeEditDistance.DistanceResult distanceResult = treeEditDistance.calculate(
                normA.getNormalizedRoot(), normB.getNormalizedRoot()
        );

        // 6. Similarity Scoring formula: S = 100 * (1 - D / (|T_A| + |T_B|))
        ScoringService.ScoreComputation scoreComputation = scoringService.computeScore(
                distanceResult.getDistance(),
                distanceResult.getTreeSizeA(),
                distanceResult.getTreeSizeB()
        );

        // 7. Aggregate counts and match details
        int matched = 0;
        int inserted = 0;
        int deleted = 0;
        int substituted = 0;
        List<MatchDetailDto> matchDetails = new ArrayList<>();

        for (TreeEditDistance.EditOperation op : distanceResult.getOperations()) {
            switch (op.getType()) {
                case MATCH:
                    matched++;
                    matchDetails.add(new MatchDetailDto(
                            op.getNodeA() != null ? op.getNodeA().getId() : null,
                            op.getNodeB() != null ? op.getNodeB().getId() : null,
                            op.getNodeA() != null ? op.getNodeA().getNormalizedType() : null,
                            op.getNodeB() != null ? op.getNodeB().getNormalizedType() : null,
                            "MATCHED", op.getCost(),
                            "Exact structural match (" + op.getNodeA().getNormalizedType() + ": " + op.getNodeA().getNormalizedValue() + ")"
                    ));
                    break;
                case SUBSTITUTE:
                    substituted++;
                    matchDetails.add(new MatchDetailDto(
                            op.getNodeA() != null ? op.getNodeA().getId() : null,
                            op.getNodeB() != null ? op.getNodeB().getId() : null,
                            op.getNodeA() != null ? op.getNodeA().getNormalizedType() : null,
                            op.getNodeB() != null ? op.getNodeB().getNormalizedType() : null,
                            "SUBSTITUTED", op.getCost(),
                            "Structural substitution between " + op.getNodeA().getNormalizedType() + " and " + op.getNodeB().getNormalizedType()
                    ));
                    break;
                case INSERT:
                    inserted++;
                    matchDetails.add(new MatchDetailDto(
                            null,
                            op.getNodeB() != null ? op.getNodeB().getId() : null,
                            null,
                            op.getNodeB() != null ? op.getNodeB().getNormalizedType() : null,
                            "INSERTED", op.getCost(),
                            "Inserted structure in Program B at line " + op.getNodeB().getStartLine()
                    ));
                    break;
                case DELETE:
                    deleted++;
                    matchDetails.add(new MatchDetailDto(
                            op.getNodeA() != null ? op.getNodeA().getId() : null,
                            null,
                            op.getNodeA() != null ? op.getNodeA().getNormalizedType() : null,
                            null,
                            "DELETED", op.getCost(),
                            "Deleted structure from Program A at line " + op.getNodeA().getStartLine()
                    ));
                    break;
            }
        }

        // 8. Transformation Summary
        TransformationSummaryDto summary = new TransformationSummaryDto();
        summary.setFormattingNormalized(true);
        summary.setLiteralAbstractionEnabled(request.isEnableLiteralAbstraction());
        summary.setReorderingEnabled(request.isEnableReordering());

        for (IdentifierNormalizer.RenamingEntry r : normA.getRenamings()) {
            summary.getIdentifierRenamingsA().add("L" + r.getLine() + ": " + r.getOriginal() + " -> " + r.getCanonical() + " (" + r.getKind() + ")");
        }
        for (IdentifierNormalizer.RenamingEntry r : normB.getRenamings()) {
            summary.getIdentifierRenamingsB().add("L" + r.getLine() + ": " + r.getOriginal() + " -> " + r.getCanonical() + " (" + r.getKind() + ")");
        }

        for (StatementDependencyAnalyzer.ReorderingRecord rr : reordersA) {
            summary.getStatementReorderingsA().add(rr.getExplanation());
        }
        for (StatementDependencyAnalyzer.ReorderingRecord rr : reordersB) {
            summary.getStatementReorderingsB().add(rr.getExplanation());
        }

        summary.getParserWarningsAndLimitations().add("Identifier normalization canonicalizes function parameters and local variables within function scopes; API/global names are preserved.");
        summary.getParserWarningsAndLimitations().add("Reordering is only permitted for proven mutually independent statement groups with disjoint read/write sets and no observable side effects.");
        summary.getParserWarningsAndLimitations().add("Score reflects structural syntax-tree similarity, not proof of semantic equivalence or behavioral identity.");

        // 9. Build response
        response.setSimilarityScore(scoreComputation.getSimilarityPercentage());
        response.setTreeEditDistance(Math.round(distanceResult.getDistance() * 100.0) / 100.0);
        response.setNodeCountA(distanceResult.getTreeSizeA());
        response.setNodeCountB(distanceResult.getTreeSizeB());
        response.setMatchedNodeCount(matched);
        response.setInsertedNodeCount(inserted);
        response.setDeletedNodeCount(deleted);
        response.setSubstitutedNodeCount(substituted);
        response.setTreeA(AstNodeDto.fromCommonNode(normA.getNormalizedRoot()));
        response.setTreeB(AstNodeDto.fromCommonNode(normB.getNormalizedRoot()));
        response.setMatches(matchDetails);
        response.setTransformations(summary);
        response.setScoreExplanation(scoreComputation.getExplanation());

        return response;
    }
}
