package com.ast.similarity.dto;

import java.util.Map;

public class HealthResponse {
    private String status;
    private String application;
    private String javaVersion;
    private String os;
    private boolean cParserAvailable;
    private boolean cppParserAvailable;
    private boolean pythonParserAvailable;
    private Map<String, Object> details;

    public HealthResponse() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getApplication() { return application; }
    public void setApplication(String application) { this.application = application; }

    public String getJavaVersion() { return javaVersion; }
    public void setJavaVersion(String javaVersion) { this.javaVersion = javaVersion; }

    public String getOs() { return os; }
    public void setOs(String os) { this.os = os; }

    public boolean isCParserAvailable() { return cParserAvailable; }
    public void setCParserAvailable(boolean cParserAvailable) { this.cParserAvailable = cParserAvailable; }

    public boolean isCppParserAvailable() { return cppParserAvailable; }
    public void setCppParserAvailable(boolean cppParserAvailable) { this.cppParserAvailable = cppParserAvailable; }

    public boolean isPythonParserAvailable() { return pythonParserAvailable; }
    public void setPythonParserAvailable(boolean pythonParserAvailable) { this.pythonParserAvailable = pythonParserAvailable; }

    public Map<String, Object> getDetails() { return details; }
    public void setDetails(Map<String, Object> details) { this.details = details; }
}
