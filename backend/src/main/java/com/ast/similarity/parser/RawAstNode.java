package com.ast.similarity.parser;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RawAstNode {
    private String id;
    private String type;
    private boolean isNamed;
    private boolean isMissing;
    private boolean isError;
    private boolean hasError;
    private int startLine;
    private int startCol;
    private int endLine;
    private int endCol;
    private int startByte;
    private int endByte;
    private String text;
    private List<RawAstNode> children = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    @JsonProperty("isNamed")
    public boolean isNamed() { return isNamed; }
    public void setNamed(boolean named) { isNamed = named; }

    @JsonProperty("isMissing")
    public boolean isMissing() { return isMissing; }
    public void setMissing(boolean missing) { isMissing = missing; }

    @JsonProperty("isError")
    public boolean isError() { return isError; }
    public void setError(boolean error) { isError = error; }

    @JsonProperty("hasError")
    public boolean hasError() { return hasError; }
    public void setHasError(boolean hasError) { this.hasError = hasError; }

    public int getStartLine() { return startLine; }
    public void setStartLine(int startLine) { this.startLine = startLine; }

    public int getStartCol() { return startCol; }
    public void setStartCol(int startCol) { this.startCol = startCol; }

    public int getEndLine() { return endLine; }
    public void setEndLine(int endLine) { this.endLine = endLine; }

    public int getEndCol() { return endCol; }
    public void setEndCol(int endCol) { this.endCol = endCol; }

    public int getStartByte() { return startByte; }
    public void setStartByte(int startByte) { this.startByte = startByte; }

    public int getEndByte() { return endByte; }
    public void setEndByte(int endByte) { this.endByte = endByte; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public List<RawAstNode> getChildren() { return children; }
    public void setChildren(List<RawAstNode> children) { this.children = children; }
}
