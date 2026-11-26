package com.ast.similarity.dto;

import com.ast.similarity.ast.AstCategory;
import com.ast.similarity.ast.CommonAstNode;

import java.util.ArrayList;
import java.util.List;

public class AstNodeDto {
    private String id;
    private String originalType;
    private String normalizedType;
    private AstCategory category;
    private String rawValue;
    private String normalizedValue;
    private int startLine;
    private int startCol;
    private int endLine;
    private int endCol;
    private String matchStatus; // MATCHED, INSERTED, DELETED, SUBSTITUTED, UNCHANGED
    private String matchedNodeId;
    private double editCost;
    private List<AstNodeDto> children = new ArrayList<>();

    public static AstNodeDto fromCommonNode(CommonAstNode node) {
        if (node == null) return null;
        AstNodeDto dto = new AstNodeDto();
        dto.setId(node.getId());
        dto.setOriginalType(node.getOriginalType());
        dto.setNormalizedType(node.getNormalizedType());
        dto.setCategory(node.getCategory());
        dto.setRawValue(node.getRawValue());
        dto.setNormalizedValue(node.getNormalizedValue());
        dto.setStartLine(node.getStartLine());
        dto.setStartCol(node.getStartCol());
        dto.setEndLine(node.getEndLine());
        dto.setEndCol(node.getEndCol());
        dto.setMatchStatus(node.getMatchStatus());
        dto.setMatchedNodeId(node.getMatchedNodeId());
        dto.setEditCost(node.getEditCost());

        for (CommonAstNode c : node.getChildren()) {
            dto.getChildren().add(fromCommonNode(c));
        }
        return dto;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOriginalType() { return originalType; }
    public void setOriginalType(String originalType) { this.originalType = originalType; }

    public String getNormalizedType() { return normalizedType; }
    public void setNormalizedType(String normalizedType) { this.normalizedType = normalizedType; }

    public AstCategory getCategory() { return category; }
    public void setCategory(AstCategory category) { this.category = category; }

    public String getRawValue() { return rawValue; }
    public void setRawValue(String rawValue) { this.rawValue = rawValue; }

    public String getNormalizedValue() { return normalizedValue; }
    public void setNormalizedValue(String normalizedValue) { this.normalizedValue = normalizedValue; }

    public int getStartLine() { return startLine; }
    public void setStartLine(int startLine) { this.startLine = startLine; }

    public int getStartCol() { return startCol; }
    public void setStartCol(int startCol) { this.startCol = startCol; }

    public int getEndLine() { return endLine; }
    public void setEndLine(int endLine) { this.endLine = endLine; }

    public int getEndCol() { return endCol; }
    public void setEndCol(int endCol) { this.endCol = endCol; }

    public String getMatchStatus() { return matchStatus; }
    public void setMatchStatus(String matchStatus) { this.matchStatus = matchStatus; }

    public String getMatchedNodeId() { return matchedNodeId; }
    public void setMatchedNodeId(String matchedNodeId) { this.matchedNodeId = matchedNodeId; }

    public double getEditCost() { return editCost; }
    public void setEditCost(double editCost) { this.editCost = editCost; }

    public List<AstNodeDto> getChildren() { return children; }
    public void setChildren(List<AstNodeDto> children) { this.children = children; }
}
