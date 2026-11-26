package com.ast.similarity.dto;

import java.util.ArrayList;
import java.util.List;

public class TransformationSummaryDto {
    private List<String> identifierRenamingsA = new ArrayList<>();
    private List<String> identifierRenamingsB = new ArrayList<>();
    private List<String> statementReorderingsA = new ArrayList<>();
    private List<String> statementReorderingsB = new ArrayList<>();
    private List<String> parserWarningsAndLimitations = new ArrayList<>();
    private boolean formattingNormalized = true;
    private boolean literalAbstractionEnabled = false;
    private boolean reorderingEnabled = true;

    public List<String> getIdentifierRenamingsA() { return identifierRenamingsA; }
    public void setIdentifierRenamingsA(List<String> identifierRenamingsA) { this.identifierRenamingsA = identifierRenamingsA; }

    public List<String> getIdentifierRenamingsB() { return identifierRenamingsB; }
    public void setIdentifierRenamingsB(List<String> identifierRenamingsB) { this.identifierRenamingsB = identifierRenamingsB; }

    public List<String> getStatementReorderingsA() { return statementReorderingsA; }
    public void setStatementReorderingsA(List<String> statementReorderingsA) { this.statementReorderingsA = statementReorderingsA; }

    public List<String> getStatementReorderingsB() { return statementReorderingsB; }
    public void setStatementReorderingsB(List<String> statementReorderingsB) { this.statementReorderingsB = statementReorderingsB; }

    public List<String> getParserWarningsAndLimitations() { return parserWarningsAndLimitations; }
    public void setParserWarningsAndLimitations(List<String> parserWarningsAndLimitations) { this.parserWarningsAndLimitations = parserWarningsAndLimitations; }

    public boolean isFormattingNormalized() { return formattingNormalized; }
    public void setFormattingNormalized(boolean formattingNormalized) { this.formattingNormalized = formattingNormalized; }

    public boolean isLiteralAbstractionEnabled() { return literalAbstractionEnabled; }
    public void setLiteralAbstractionEnabled(boolean literalAbstractionEnabled) { this.literalAbstractionEnabled = literalAbstractionEnabled; }

    public boolean isReorderingEnabled() { return reorderingEnabled; }
    public void setReorderingEnabled(boolean reorderingEnabled) { this.reorderingEnabled = reorderingEnabled; }
}
