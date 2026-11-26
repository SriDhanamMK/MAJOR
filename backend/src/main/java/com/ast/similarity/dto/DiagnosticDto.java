package com.ast.similarity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DiagnosticDto {
    private String severity;
    private int line;
    private int column;
    private int endLine;
    private int endColumn;
    private String message;

    public DiagnosticDto() {}

    public DiagnosticDto(String severity, int line, int column, int endLine, int endColumn, String message) {
        this.severity = severity;
        this.line = line;
        this.column = column;
        this.endLine = endLine;
        this.endColumn = endColumn;
        this.message = message;
    }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public int getLine() { return line; }
    public void setLine(int line) { this.line = line; }

    public int getColumn() { return column; }
    public void setColumn(int column) { this.column = column; }

    public int getEndLine() { return endLine; }
    public void setEndLine(int endLine) { this.endLine = endLine; }

    public int getEndColumn() { return endColumn; }
    public void setEndColumn(int endColumn) { this.endColumn = endColumn; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
