package com.ast.similarity.dto;

import jakarta.validation.constraints.NotBlank;

public class ParseRequest {
    @NotBlank(message = "Language must not be blank")
    private String language;

    @NotBlank(message = "Source code must not be blank")
    private String sourceCode;

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }
}
