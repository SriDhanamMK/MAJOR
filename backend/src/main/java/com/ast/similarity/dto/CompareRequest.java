package com.ast.similarity.dto;

import jakarta.validation.constraints.NotBlank;

public class CompareRequest {
    @NotBlank(message = "Language must not be blank")
    private String language;

    @NotBlank(message = "Source Code A must not be blank")
    private String sourceA;

    @NotBlank(message = "Source Code B must not be blank")
    private String sourceB;

    private boolean enableLiteralAbstraction = false;
    private boolean enableReordering = true;

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getSourceA() { return sourceA; }
    public void setSourceA(String sourceA) { this.sourceA = sourceA; }

    public String getSourceB() { return sourceB; }
    public void setSourceB(String sourceB) { this.sourceB = sourceB; }

    public boolean isEnableLiteralAbstraction() { return enableLiteralAbstraction; }
    public void setEnableLiteralAbstraction(boolean enableLiteralAbstraction) { this.enableLiteralAbstraction = enableLiteralAbstraction; }

    public boolean isEnableReordering() { return enableReordering; }
    public void setEnableReordering(boolean enableReordering) { this.enableReordering = enableReordering; }
}
