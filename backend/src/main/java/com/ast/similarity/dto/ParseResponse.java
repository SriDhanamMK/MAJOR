package com.ast.similarity.dto;

import java.util.ArrayList;
import java.util.List;

public class ParseResponse {
    private String status; // SUCCESS, SYNTAX_ERROR, ERROR
    private String language;
    private String rootNodeType;
    private int nodeCount;
    private AstNodeDto root;
    private List<DiagnosticDto> diagnostics = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getRootNodeType() { return rootNodeType; }
    public void setRootNodeType(String rootNodeType) { this.rootNodeType = rootNodeType; }

    public int getNodeCount() { return nodeCount; }
    public void setNodeCount(int nodeCount) { this.nodeCount = nodeCount; }

    public AstNodeDto getRoot() { return root; }
    public void setRoot(AstNodeDto root) { this.root = root; }

    public List<DiagnosticDto> getDiagnostics() { return diagnostics; }
    public void setDiagnostics(List<DiagnosticDto> diagnostics) { this.diagnostics = diagnostics; }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }
}
