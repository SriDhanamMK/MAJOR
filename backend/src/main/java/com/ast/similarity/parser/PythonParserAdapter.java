package com.ast.similarity.parser;

import com.ast.similarity.ast.AstTreeBuilder;
import com.ast.similarity.ast.CommonAstNode;
import org.springframework.stereotype.Component;

@Component
public class PythonParserAdapter implements LanguageParserAdapter {

    private final ParserEngineBridge bridge;
    private final AstTreeBuilder treeBuilder;

    public PythonParserAdapter(ParserEngineBridge bridge, AstTreeBuilder treeBuilder) {
        this.bridge = bridge;
        this.treeBuilder = treeBuilder;
    }

    @Override
    public String getLanguage() {
        return "PYTHON";
    }

    @Override
    public RawParseResult parseRaw(String sourceCode) throws Exception {
        return bridge.parse("PYTHON", sourceCode);
    }

    @Override
    public CommonAstNode parseToCommonAst(String sourceCode, String idPrefix) throws Exception {
        RawParseResult result = parseRaw(sourceCode);
        if (result.isHasErrors() || !"SUCCESS".equalsIgnoreCase(result.getStatus())) {
            throw new IllegalArgumentException("Python source contains syntax errors: " +
                    (result.getDiagnostics().isEmpty() ? "Unknown syntax error" : result.getDiagnostics().get(0).getMessage()));
        }
        return treeBuilder.buildTree(result.getRoot(), "PYTHON", idPrefix);
    }
}
