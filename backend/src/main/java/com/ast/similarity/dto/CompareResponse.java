package com.ast.similarity.dto;

import java.util.ArrayList;
import java.util.List;

public class CompareResponse {
    private String comparisonId;
    private String language;
    private double similarityScore; // 0.00 to 100.00
    private double treeEditDistance;
    private int nodeCountA;
    private int nodeCountB;
    private int matchedNodeCount;
    private int insertedNodeCount;
    private int deletedNodeCount;
    private int substitutedNodeCount;

    private AstNodeDto treeA;
    private AstNodeDto treeB;

    private List<MatchDetailDto> matches = new ArrayList<>();
    private TransformationSummaryDto transformations;
    private ScoreExplanationDto scoreExplanation;

    private List<DiagnosticDto> diagnosticsA = new ArrayList<>();
    private List<DiagnosticDto> diagnosticsB = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();

    private String algorithmVersion = "Zhang-Shasha-TED-v1.0-IEEE";
    private String timestamp;

    public String getComparisonId() { return comparisonId; }
    public void setComparisonId(String comparisonId) { this.comparisonId = comparisonId; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public double getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(double similarityScore) { this.similarityScore = similarityScore; }

    public double getTreeEditDistance() { return treeEditDistance; }
    public void setTreeEditDistance(double treeEditDistance) { this.treeEditDistance = treeEditDistance; }

    public int getNodeCountA() { return nodeCountA; }
    public void setNodeCountA(int nodeCountA) { this.nodeCountA = nodeCountA; }

    public int getNodeCountB() { return nodeCountB; }
    public void setNodeCountB(int nodeCountB) { this.nodeCountB = nodeCountB; }

    public int getMatchedNodeCount() { return matchedNodeCount; }
    public void setMatchedNodeCount(int matchedNodeCount) { this.matchedNodeCount = matchedNodeCount; }

    public int getInsertedNodeCount() { return insertedNodeCount; }
    public void setInsertedNodeCount(int insertedNodeCount) { this.insertedNodeCount = insertedNodeCount; }

    public int getDeletedNodeCount() { return deletedNodeCount; }
    public void setDeletedNodeCount(int deletedNodeCount) { this.deletedNodeCount = deletedNodeCount; }

    public int getSubstitutedNodeCount() { return substitutedNodeCount; }
    public void setSubstitutedNodeCount(int substitutedNodeCount) { this.substitutedNodeCount = substitutedNodeCount; }

    public AstNodeDto getTreeA() { return treeA; }
    public void setTreeA(AstNodeDto treeA) { this.treeA = treeA; }

    public AstNodeDto getTreeB() { return treeB; }
    public void setTreeB(AstNodeDto treeB) { this.treeB = treeB; }

    public List<MatchDetailDto> getMatches() { return matches; }
    public void setMatches(List<MatchDetailDto> matches) { this.matches = matches; }

    public TransformationSummaryDto getTransformations() { return transformations; }
    public void setTransformations(TransformationSummaryDto transformations) { this.transformations = transformations; }

    public ScoreExplanationDto getScoreExplanation() { return scoreExplanation; }
    public void setScoreExplanation(ScoreExplanationDto scoreExplanation) { this.scoreExplanation = scoreExplanation; }

    public List<DiagnosticDto> getDiagnosticsA() { return diagnosticsA; }
    public void setDiagnosticsA(List<DiagnosticDto> diagnosticsA) { this.diagnosticsA = diagnosticsA; }

    public List<DiagnosticDto> getDiagnosticsB() { return diagnosticsB; }
    public void setDiagnosticsB(List<DiagnosticDto> diagnosticsB) { this.diagnosticsB = diagnosticsB; }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }

    public String getAlgorithmVersion() { return algorithmVersion; }
    public void setAlgorithmVersion(String algorithmVersion) { this.algorithmVersion = algorithmVersion; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
