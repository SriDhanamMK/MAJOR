package com.ast.similarity.service;

import com.ast.similarity.ast.AstTreeBuilder;
import com.ast.similarity.ast.CommonAstNode;
import com.ast.similarity.dto.AstNodeDto;
import com.ast.similarity.dto.ParseRequest;
import com.ast.similarity.dto.ParseResponse;
import com.ast.similarity.parser.LanguageParserAdapter;
import com.ast.similarity.parser.ParserFactory;
import com.ast.similarity.parser.RawParseResult;
import org.springframework.stereotype.Service;

@Service
public class ParserService {

    private final ParserFactory parserFactory;
    private final AstTreeBuilder treeBuilder;

    public ParserService(ParserFactory parserFactory, AstTreeBuilder treeBuilder) {
        this.parserFactory = parserFactory;
        this.treeBuilder = treeBuilder;
    }

    public ParseResponse parse(ParseRequest request) throws Exception {
        LanguageParserAdapter adapter = parserFactory.getAdapter(request.getLanguage());
        RawParseResult raw = adapter.parseRaw(request.getSourceCode());

        ParseResponse response = new ParseResponse();
        response.setLanguage(adapter.getLanguage());
        response.setStatus(raw.getStatus());
        response.setDiagnostics(raw.getDiagnostics());

        if (raw.isHasErrors() || !"SUCCESS".equalsIgnoreCase(raw.getStatus())) {
            response.setNodeCount(0);
            response.setRootNodeType(null);
            response.getWarnings().add("Parsing encountered syntax errors. Tree construction limited.");
            return response;
        }

        CommonAstNode commonRoot = treeBuilder.buildTree(raw.getRoot(), adapter.getLanguage(), "");
        response.setRootNodeType(commonRoot.getOriginalType());
        response.setNodeCount(commonRoot.getDeepNodeCount());
        response.setRoot(AstNodeDto.fromCommonNode(commonRoot));

        return response;
    }
}
