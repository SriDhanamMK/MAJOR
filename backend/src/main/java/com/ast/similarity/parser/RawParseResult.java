package com.ast.similarity.parser;

import com.ast.similarity.dto.DiagnosticDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RawParseResult {
    private String status;
    private String language;
    private boolean hasErrors;
    private String message;
    private int nodeCount;
    private RawAstNode root;
    private List<DiagnosticDto> diagnostics = new ArrayList<>();

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public boolean isHasErrors() { return hasErrors; }
    public void setHasErrors(boolean hasErrors) { this.hasErrors = hasErrors; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getNodeCount() { return nodeCount; }
    public void setNodeCount(int nodeCount) { this.nodeCount = nodeCount; }

    public RawAstNode getRoot() { return root; }
    public void setRoot(RawAstNode root) { this.root = root; }

    public List<DiagnosticDto> getDiagnostics() { return diagnostics; }
    public void setDiagnostics(List<DiagnosticDto> diagnostics) { this.diagnostics = diagnostics; }
}
