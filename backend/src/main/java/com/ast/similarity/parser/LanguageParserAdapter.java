package com.ast.similarity.parser;

import com.ast.similarity.ast.CommonAstNode;

public interface LanguageParserAdapter {
    String getLanguage();
    RawParseResult parseRaw(String sourceCode) throws Exception;
    CommonAstNode parseToCommonAst(String sourceCode, String idPrefix) throws Exception;
}
