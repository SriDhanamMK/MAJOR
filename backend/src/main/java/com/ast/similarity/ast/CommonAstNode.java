package com.ast.similarity.ast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class CommonAstNode {
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

    private boolean isNamed = true;
    private boolean isError = false;

    private List<CommonAstNode> children = new ArrayList<>();

    // Comparison and alignment metadata
    private String matchStatus = "UNCHANGED"; // MATCHED, INSERTED, DELETED, SUBSTITUTED
    private String matchedNodeId;
    private double editCost = 0.0;

    // Dataflow and reordering metadata
    private Set<String> defVars = new HashSet<>();
    private Set<String> useVars = new HashSet<>();
    private boolean hasSideEffects = false;

    public CommonAstNode() {}

    public CommonAstNode(String id, String originalType, String normalizedType, AstCategory category) {
        this.id = id;
        this.originalType = originalType;
        this.normalizedType = normalizedType;
        this.category = category;
    }

    public void addChild(CommonAstNode child) {
        if (child != null) {
            this.children.add(child);
        }
    }

    public int getDeepNodeCount() {
        int count = 1;
        for (CommonAstNode child : children) {
            count += child.getDeepNodeCount();
        }
        return count;
    }

    public List<CommonAstNode> getPostOrderList() {
        List<CommonAstNode> list = new ArrayList<>();
        collectPostOrder(this, list);
        return list;
    }

    private static void collectPostOrder(CommonAstNode node, List<CommonAstNode> list) {
        for (CommonAstNode child : node.getChildren()) {
            collectPostOrder(child, list);
        }
        list.add(node);
    }

    public CommonAstNode deepCopy(String idPrefix) {
        CommonAstNode copy = new CommonAstNode();
        copy.id = (idPrefix != null ? idPrefix : "") + this.id;
        copy.originalType = this.originalType;
        copy.normalizedType = this.normalizedType;
        copy.category = this.category;
        copy.rawValue = this.rawValue;
        copy.normalizedValue = this.normalizedValue;
        copy.startLine = this.startLine;
        copy.startCol = this.startCol;
        copy.endLine = this.endLine;
        copy.endCol = this.endCol;
        copy.isNamed = this.isNamed;
        copy.isError = this.isError;
        copy.matchStatus = this.matchStatus;
        copy.matchedNodeId = this.matchedNodeId;
        copy.editCost = this.editCost;
        copy.defVars = new HashSet<>(this.defVars);
        copy.useVars = new HashSet<>(this.useVars);
        copy.hasSideEffects = this.hasSideEffects;

        for (CommonAstNode child : this.children) {
            copy.addChild(child.deepCopy(idPrefix));
        }
        return copy;
    }

    // Getters and Setters
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

    public boolean isNamed() { return isNamed; }
    public void setNamed(boolean named) { isNamed = named; }

    public boolean isError() { return isError; }
    public void setError(boolean error) { isError = error; }

    public List<CommonAstNode> getChildren() { return children; }
    public void setChildren(List<CommonAstNode> children) { this.children = children; }

    public String getMatchStatus() { return matchStatus; }
    public void setMatchStatus(String matchStatus) { this.matchStatus = matchStatus; }

    public String getMatchedNodeId() { return matchedNodeId; }
    public void setMatchedNodeId(String matchedNodeId) { this.matchedNodeId = matchedNodeId; }

    public double getEditCost() { return editCost; }
    public void setEditCost(double editCost) { this.editCost = editCost; }

    public Set<String> getDefVars() { return defVars; }
    public void setDefVars(Set<String> defVars) { this.defVars = defVars; }

    public Set<String> getUseVars() { return useVars; }
    public void setUseVars(Set<String> useVars) { this.useVars = useVars; }

    public boolean isHasSideEffects() { return hasSideEffects; }
    public void setHasSideEffects(boolean hasSideEffects) { this.hasSideEffects = hasSideEffects; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommonAstNode that = (CommonAstNode) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
