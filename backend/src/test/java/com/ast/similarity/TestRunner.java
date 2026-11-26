package com.ast.similarity;

import com.ast.similarity.ast.AstTreeBuilder;
import com.ast.similarity.comparison.ComparisonEngine;
import com.ast.similarity.comparison.ScoringService;
import com.ast.similarity.comparison.StatementDependencyAnalyzer;
import com.ast.similarity.comparison.TreeEditDistance;
import com.ast.similarity.config.ParserProperties;
import com.ast.similarity.dto.CompareRequest;
import com.ast.similarity.dto.CompareResponse;
import com.ast.similarity.dto.MatchDetailDto;
import com.ast.similarity.normalization.AstNormalizer;
import com.ast.similarity.normalization.FormattingNormalizer;
import com.ast.similarity.normalization.IdentifierNormalizer;
import com.ast.similarity.normalization.LiteralNormalizer;
import com.ast.similarity.parser.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;

public class TestRunner {
    public static void main(String[] args) throws Exception {
        ParserProperties props = new ParserProperties();
        props.setEnginePath("parser-engine/treesitter_parser.py");
        props.setPythonExecutable("python");
        props.setTimeoutMs(5000);
        props.setMaxSourceBytes(524288);

        ObjectMapper mapper = new ObjectMapper();
        ParserEngineBridge bridge = new ParserEngineBridge(props, mapper);
        AstTreeBuilder treeBuilder = new AstTreeBuilder();

        CParserAdapter cAdapter = new CParserAdapter(bridge, treeBuilder);
        CppParserAdapter cppAdapter = new CppParserAdapter(bridge, treeBuilder);
        PythonParserAdapter pyAdapter = new PythonParserAdapter(bridge, treeBuilder);
        ParserFactory factory = new ParserFactory(Arrays.asList(cAdapter, cppAdapter, pyAdapter));

        AstNormalizer normalizer = new AstNormalizer(new FormattingNormalizer(), new IdentifierNormalizer(), new LiteralNormalizer());
        StatementDependencyAnalyzer depAnalyzer = new StatementDependencyAnalyzer();
        TreeEditDistance ted = new TreeEditDistance();
        ScoringService scoring = new ScoringService();
        ComparisonEngine engine = new ComparisonEngine(factory, normalizer, depAnalyzer, ted, scoring);

        // 1. Operator test
        CompareRequest req1 = new CompareRequest();
        req1.setLanguage("C");
        req1.setSourceA("int calc(int a, int b) { return a + b; }");
        req1.setSourceB("int calc(int a, int b) { return a * b; }");
        CompareResponse res1 = engine.compare(req1);
        System.out.println("Operator test: score=" + res1.getSimilarityScore() + ", substituted=" + res1.getSubstitutedNodeCount() + ", matched=" + res1.getMatchedNodeCount() + ", inserted=" + res1.getInsertedNodeCount() + ", deleted=" + res1.getDeletedNodeCount());
        for (MatchDetailDto m : res1.getMatches()) {
            if (!"MATCHED".equals(m.getStatus())) {
                System.out.println("  Op match diff: " + m.getStatus() + " | A=" + m.getTypeA() + " | B=" + m.getTypeB() + " | Cost=" + m.getCost() + " | " + m.getDetail());
            }
        }

        // 2. Safe reordering test
        CompareRequest req2 = new CompareRequest();
        req2.setLanguage("C");
        req2.setSourceA("void test() {\n  int a = 1;\n  int b = 2;\n}");
        req2.setSourceB("void test() {\n  int b = 2;\n  int a = 1;\n}");
        req2.setEnableReordering(true);
        CompareResponse res2 = engine.compare(req2);
        System.out.println("Reorder test: score=" + res2.getSimilarityScore() + ", distance=" + res2.getTreeEditDistance());
        for (MatchDetailDto m : res2.getMatches()) {
            if (!"MATCHED".equals(m.getStatus())) {
                System.out.println("  Reorder diff: " + m.getStatus() + " | A=" + m.getTypeA() + " | B=" + m.getTypeB() + " | Cost=" + m.getCost() + " | " + m.getDetail());
            }
        }

        bridge.cleanup();
    }
}
