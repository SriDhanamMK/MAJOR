package com.ast.similarity.dto;

import java.util.List;

public class ScoreExplanationDto {
    private String formula;
    private double treeEditDistance;
    private int sizeA;
    private int sizeB;
    private int denominator;
    private double rawScore;
    private double finalSimilarityPercentage;
    private List<String> calculationSteps;

    public ScoreExplanationDto() {}

    public ScoreExplanationDto(String formula, double treeEditDistance, int sizeA, int sizeB,
                               int denominator, double rawScore, double finalSimilarityPercentage,
                               List<String> calculationSteps) {
        this.formula = formula;
        this.treeEditDistance = treeEditDistance;
        this.sizeA = sizeA;
        this.sizeB = sizeB;
        this.denominator = denominator;
        this.rawScore = rawScore;
        this.finalSimilarityPercentage = finalSimilarityPercentage;
        this.calculationSteps = calculationSteps;
    }

    public String getFormula() { return formula; }
    public void setFormula(String formula) { this.formula = formula; }

    public double getTreeEditDistance() { return treeEditDistance; }
    public void setTreeEditDistance(double treeEditDistance) { this.treeEditDistance = treeEditDistance; }

    public int getSizeA() { return sizeA; }
    public void setSizeA(int sizeA) { this.sizeA = sizeA; }

    public int getSizeB() { return sizeB; }
    public void setSizeB(int sizeB) { this.sizeB = sizeB; }

    public int getDenominator() { return denominator; }
    public void setDenominator(int denominator) { this.denominator = denominator; }

    public double getRawScore() { return rawScore; }
    public void setRawScore(double rawScore) { this.rawScore = rawScore; }

    public double getFinalSimilarityPercentage() { return finalSimilarityPercentage; }
    public void setFinalSimilarityPercentage(double finalSimilarityPercentage) { this.finalSimilarityPercentage = finalSimilarityPercentage; }

    public List<String> getCalculationSteps() { return calculationSteps; }
    public void setCalculationSteps(List<String> calculationSteps) { this.calculationSteps = calculationSteps; }
}
