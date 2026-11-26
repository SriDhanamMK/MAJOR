package com.ast.similarity.dto;

public class MatchDetailDto {
    private String idA;
    private String idB;
    private String typeA;
    private String typeB;
    private String status; // MATCHED, SUBSTITUTED, INSERTED, DELETED
    private double cost;
    private String detail;

    public MatchDetailDto() {}

    public MatchDetailDto(String idA, String idB, String typeA, String typeB, String status, double cost, String detail) {
        this.idA = idA;
        this.idB = idB;
        this.typeA = typeA;
        this.typeB = typeB;
        this.status = status;
        this.cost = cost;
        this.detail = detail;
    }

    public String getIdA() { return idA; }
    public void setIdA(String idA) { this.idA = idA; }

    public String getIdB() { return idB; }
    public void setIdB(String idB) { this.idB = idB; }

    public String getTypeA() { return typeA; }
    public void setTypeA(String typeA) { this.typeA = typeA; }

    public String getTypeB() { return typeB; }
    public void setTypeB(String typeB) { this.typeB = typeB; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
}
