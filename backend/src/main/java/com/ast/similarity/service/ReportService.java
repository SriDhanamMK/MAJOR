package com.ast.similarity.service;

import com.ast.similarity.dto.CompareResponse;
import com.ast.similarity.dto.MatchDetailDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReportService {

    private final ObjectMapper objectMapper;

    public ReportService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String generateJsonReport(CompareResponse response) throws Exception {
        Map<String, Object> report = new HashMap<>();
        report.put("title", "AST-Based Structural Source Code Similarity Report");
        report.put("algorithmVersion", response.getAlgorithmVersion());
        report.put("timestamp", response.getTimestamp());
        report.put("comparisonId", response.getComparisonId());
        report.put("language", response.getLanguage());

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("similarityPercentage", response.getSimilarityScore());
        metrics.put("treeEditDistance", response.getTreeEditDistance());
        metrics.put("nodeCountA", response.getNodeCountA());
        metrics.put("nodeCountB", response.getNodeCountB());
        metrics.put("matchedNodes", response.getMatchedNodeCount());
        metrics.put("insertedNodes", response.getInsertedNodeCount());
        metrics.put("deletedNodes", response.getDeletedNodeCount());
        metrics.put("substitutedNodes", response.getSubstitutedNodeCount());
        report.put("metrics", metrics);

        report.put("scoreExplanation", response.getScoreExplanation());
        report.put("transformations", response.getTransformations());
        report.put("matchesSummary", response.getMatches());
        report.put("warningsAndLimitations", response.getWarnings());

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(report);
    }

    public String generateCsvSummary(CompareResponse response) {
        StringWriter sw = new StringWriter();
        sw.append("Property,Value\n");
        sw.append("Comparison ID,").append(escapeCsv(response.getComparisonId())).append("\n");
        sw.append("Language,").append(escapeCsv(response.getLanguage())).append("\n");
        sw.append("Final Similarity Percentage,").append(String.format("%.2f%%", response.getSimilarityScore())).append("\n");
        sw.append("Tree Edit Distance,").append(String.format("%.2f", response.getTreeEditDistance())).append("\n");
        sw.append("Node Count Program A,").append(String.valueOf(response.getNodeCountA())).append("\n");
        sw.append("Node Count Program B,").append(String.valueOf(response.getNodeCountB())).append("\n");
        sw.append("Matched Nodes Count,").append(String.valueOf(response.getMatchedNodeCount())).append("\n");
        sw.append("Inserted Nodes Count,").append(String.valueOf(response.getInsertedNodeCount())).append("\n");
        sw.append("Deleted Nodes Count,").append(String.valueOf(response.getDeletedNodeCount())).append("\n");
        sw.append("Substituted Nodes Count,").append(String.valueOf(response.getSubstitutedNodeCount())).append("\n");
        sw.append("Algorithm Version,").append(escapeCsv(response.getAlgorithmVersion())).append("\n");
        sw.append("Timestamp,").append(escapeCsv(response.getTimestamp())).append("\n\n");

        sw.append("Match ID A,Match ID B,Type A,Type B,Status,Cost,Detail\n");
        if (response.getMatches() != null) {
            for (MatchDetailDto m : response.getMatches()) {
                sw.append(escapeCsv(m.getIdA())).append(",")
                        .append(escapeCsv(m.getIdB())).append(",")
                        .append(escapeCsv(m.getTypeA())).append(",")
                        .append(escapeCsv(m.getTypeB())).append(",")
                        .append(escapeCsv(m.getStatus())).append(",")
                        .append(String.format("%.2f", m.getCost())).append(",")
                        .append(escapeCsv(m.getDetail())).append("\n");
            }
        }

        return sw.toString();
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }
}
