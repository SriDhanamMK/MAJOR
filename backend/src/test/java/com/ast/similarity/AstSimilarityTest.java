package com.ast.similarity;

import com.ast.similarity.ast.AstCategory;
import com.ast.similarity.ast.CommonAstNode;
import com.ast.similarity.comparison.ComparisonEngine;
import com.ast.similarity.comparison.ScoringService;
import com.ast.similarity.comparison.StatementDependencyAnalyzer;
import com.ast.similarity.comparison.TreeEditDistance;
import com.ast.similarity.dto.*;
import com.ast.similarity.normalization.AstNormalizer;
import com.ast.similarity.normalization.FormattingNormalizer;
import com.ast.similarity.normalization.IdentifierNormalizer;
import com.ast.similarity.normalization.LiteralNormalizer;
import com.ast.similarity.parser.ParserEngineBridge;
import com.ast.similarity.service.ComparisonService;
import com.ast.similarity.service.ParserService;
import com.ast.similarity.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AstSimilarityTest {

    @Autowired
    private ParserEngineBridge parserBridge;

    @Autowired
    private ParserService parserService;

    @Autowired
    private ComparisonService comparisonService;

    @Autowired
    private ComparisonEngine comparisonEngine;

    @Autowired
    private TreeEditDistance treeEditDistance;

    @Autowired
    private ScoringService scoringService;

    @Autowired
    private AstNormalizer astNormalizer;

    @Autowired
    private StatementDependencyAnalyzer dependencyAnalyzer;

    @Autowired
    private ReportService reportService;

    @Test
    @DisplayName("Test 1: Health check reports C, CPP, Python parsers UP")
    void testHealthCheck() {
        Map<String, Object> health = parserBridge.checkHealth();
        assertNotNull(health);
        assertEquals(true, health.get("initialized"));
        Map<String, Object> langs = (Map<String, Object>) health.get("languages");
        assertEquals(true, langs.get("C"));
        assertEquals(true, langs.get("CPP"));
        assertEquals(true, langs.get("PYTHON"));
    }

    @Test
    @DisplayName("Test 2: Identical C programs yield 100% similarity and 0 edit distance")
    void testIdenticalCPrograms() throws Exception {
        String code = "int main() {\n  int x = 42;\n  return x;\n}\n";
        CompareRequest req = new CompareRequest();
        req.setLanguage("C");
        req.setSourceA(code);
        req.setSourceB(code);

        CompareResponse res = comparisonEngine.compare(req);
        assertEquals(100.0, res.getSimilarityScore(), 0.001);
        assertEquals(0.0, res.getTreeEditDistance(), 0.001);
        assertTrue(res.getMatchedNodeCount() > 0);
        assertEquals(0, res.getInsertedNodeCount());
        assertEquals(0, res.getDeletedNodeCount());
    }

    @Test
    @DisplayName("Test 3: Identical C++ programs yield 100% similarity")
    void testIdenticalCppPrograms() throws Exception {
        String code = "#include <iostream>\nint main() {\n  std::cout << 10;\n  return 0;\n}\n";
        CompareRequest req = new CompareRequest();
        req.setLanguage("CPP");
        req.setSourceA(code);
        req.setSourceB(code);

        CompareResponse res = comparisonEngine.compare(req);
        assertEquals(100.0, res.getSimilarityScore(), 0.001);
        assertEquals(0.0, res.getTreeEditDistance(), 0.001);
    }

    @Test
    @DisplayName("Test 4: Identical Python programs yield 100% similarity")
    void testIdenticalPythonPrograms() throws Exception {
        String code = "def add(x, y):\n    return x + y\n";
        CompareRequest req = new CompareRequest();
        req.setLanguage("PYTHON");
        req.setSourceA(code);
        req.setSourceB(code);

        CompareResponse res = comparisonEngine.compare(req);
        assertEquals(100.0, res.getSimilarityScore(), 0.001);
        assertEquals(0.0, res.getTreeEditDistance(), 0.001);
    }

    @Test
    @DisplayName("Test 5: Formatting and whitespace variations do not reduce score")
    void testFormattingVariations() throws Exception {
        String codeA = "int main() {\n  int x = 1;\n  return x;\n}";
        String codeB = "int    main  (  )\n{\n\n    int   x   =   1  ;\n\n    // Arbitrary comment\n    return    x ;\n}\n";

        CompareRequest req = new CompareRequest();
        req.setLanguage("C");
        req.setSourceA(codeA);
        req.setSourceB(codeB);

        CompareResponse res = comparisonEngine.compare(req);
        assertEquals(100.0, res.getSimilarityScore(), 0.001);
        assertEquals(0.0, res.getTreeEditDistance(), 0.001);
    }

    @Test
    @DisplayName("Test 6: Consistent local variable & parameter renaming retains 100% similarity")
    void testIdentifierRenaming() throws Exception {
        String codeA = "int compute(int x, int y) {\n  int total = x + y;\n  return total;\n}";
        String codeB = "int compute(int alpha, int beta) {\n  int sum = alpha + beta;\n  return sum;\n}";

        CompareRequest req = new CompareRequest();
        req.setLanguage("C");
        req.setSourceA(codeA);
        req.setSourceB(codeB);

        CompareResponse res = comparisonEngine.compare(req);
        assertEquals(100.0, res.getSimilarityScore(), 0.001);
        assertEquals(0.0, res.getTreeEditDistance(), 0.001);
        assertTrue(res.getTransformations().getIdentifierRenamingsA().size() >= 3);
    }

    @Test
    @DisplayName("Test 7: Changed numeric literals reduce score according to substitution cost")
    void testChangedLiterals() throws Exception {
        String codeA = "int main() { int a = 10; return a; }";
        String codeB = "int main() { int a = 999; return a; }";

        CompareRequest req = new CompareRequest();
        req.setLanguage("C");
        req.setSourceA(codeA);
        req.setSourceB(codeB);
        req.setEnableLiteralAbstraction(false); // default: literals preserved

        CompareResponse res = comparisonEngine.compare(req);
        assertTrue(res.getSimilarityScore() < 100.0);
        assertTrue(res.getSimilarityScore() > 80.0); // high structural similarity
        assertEquals(1, res.getSubstitutedNodeCount());
    }

    @Test
    @DisplayName("Test 8: Changed operators reduce score deterministically")
    void testChangedOperators() throws Exception {
        String codeA = "int calc(int a, int b) { return a + b; }";
        String codeB = "int calc(int a, int b) { return a * b; }";

        CompareRequest req = new CompareRequest();
        req.setLanguage("C");
        req.setSourceA(codeA);
        req.setSourceB(codeB);

        CompareResponse res = comparisonEngine.compare(req);
        assertTrue(res.getSimilarityScore() < 100.0);
        assertTrue(res.getSimilarityScore() > 75.0);
        assertTrue(res.getSubstitutedNodeCount() > 0);
    }

    @Test
    @DisplayName("Test 9: Inserted statements contribute to edit distance")
    void testInsertedStatement() throws Exception {
        String codeA = "int main() {\n  int x = 1;\n  return x;\n}";
        String codeB = "int main() {\n  int x = 1;\n  int y = 2;\n  return x;\n}";

        CompareRequest req = new CompareRequest();
        req.setLanguage("C");
        req.setSourceA(codeA);
        req.setSourceB(codeB);

        CompareResponse res = comparisonEngine.compare(req);
        assertTrue(res.getSimilarityScore() < 100.0);
        assertTrue(res.getInsertedNodeCount() > 0);
    }

    @Test
    @DisplayName("Test 10: Structurally different programs yield low similarity")
    void testStructurallyDifferentPrograms() throws Exception {
        String codeA = "int main() { return 0; }";
        String codeB = "int fib(int n) {\n  if (n <= 1) return n;\n  return fib(n - 1) + fib(n - 2);\n}";

        CompareRequest req = new CompareRequest();
        req.setLanguage("C");
        req.setSourceA(codeA);
        req.setSourceB(codeB);

        CompareResponse res = comparisonEngine.compare(req);
        assertTrue(res.getSimilarityScore() < 50.0);
        assertTrue(res.getSimilarityScore() >= 0.0);
    }

    @Test
    @DisplayName("Test 11: Safe independent statement reordering recognized")
    void testSafeReordering() throws Exception {
        String codeA = "void test() {\n  int a = 1;\n  int b = 2;\n}";
        String codeB = "void test() {\n  int b = 2;\n  int a = 1;\n}";

        CompareRequest req = new CompareRequest();
        req.setLanguage("C");
        req.setSourceA(codeA);
        req.setSourceB(codeB);
        req.setEnableReordering(true);

        CompareResponse res = comparisonEngine.compare(req);
        assertTrue(res.getSimilarityScore() > 95.0);
        assertTrue(res.getTransformations().getStatementReorderingsA().stream()
                .anyMatch(s -> s.contains("independent")));
    }

    @Test
    @DisplayName("Test 12: Unsafe dependent statement reordering preserved and flagged")
    void testUnsafeReordering() throws Exception {
        String codeA = "int test() {\n  int x = 1;\n  int y = x + 2;\n  return y;\n}";
        String codeB = "int test() {\n  int y = x + 2;\n  int x = 1;\n  return y;\n}";

        CompareRequest req = new CompareRequest();
        req.setLanguage("C");
        req.setSourceA(codeA);
        req.setSourceB(codeB);
        req.setEnableReordering(true);

        CompareResponse res = comparisonEngine.compare(req);
        // Unsafe dependency cannot be silently reordered as equivalent
        assertTrue(res.getSimilarityScore() < 100.0);
    }

    @Test
    @DisplayName("Test 13: Syntax error in C produces diagnostics and rejects comparison")
    void testInvalidCSyntax() throws Exception {
        String badCode = "int main() { int a = ; return ; }";
        CompareRequest req = new CompareRequest();
        req.setLanguage("C");
        req.setSourceA(badCode);
        req.setSourceB("int main() { return 0; }");

        CompareResponse res = comparisonEngine.compare(req);
        assertEquals(0.0, res.getSimilarityScore());
        assertFalse(res.getDiagnosticsA().isEmpty());
    }

    @Test
    @DisplayName("Test 14: Syntax error in Python produces diagnostics and rejects comparison")
    void testInvalidPythonSyntax() throws Exception {
        String badCode = "def broken(:\n    return\n";
        CompareRequest req = new CompareRequest();
        req.setLanguage("PYTHON");
        req.setSourceA(badCode);
        req.setSourceB("def ok():\n    return 0\n");

        CompareResponse res = comparisonEngine.compare(req);
        assertEquals(0.0, res.getSimilarityScore());
        assertFalse(res.getDiagnosticsA().isEmpty());
    }

    @Test
    @DisplayName("Test 15: Empty input is rejected with validation error")
    void testEmptyInput() {
        CompareRequest req = new CompareRequest();
        req.setLanguage("C");
        req.setSourceA("");
        req.setSourceB("int main() { return 0; }");

        assertThrows(IllegalArgumentException.class, () -> comparisonEngine.compare(req));
    }

    @Test
    @DisplayName("Test 16: Deterministic repeated comparisons yield exact same score")
    void testDeterministicExecution() throws Exception {
        String codeA = "int factorial(int n) { if (n <= 1) return 1; return n * factorial(n - 1); }";
        String codeB = "int fact(int val) { if (val <= 1) return 1; return val * fact(val - 1); }";

        CompareRequest req = new CompareRequest();
        req.setLanguage("C");
        req.setSourceA(codeA);
        req.setSourceB(codeB);

        CompareResponse res1 = comparisonEngine.compare(req);
        CompareResponse res2 = comparisonEngine.compare(req);

        assertEquals(res1.getSimilarityScore(), res2.getSimilarityScore(), 0.000001);
        assertEquals(res1.getTreeEditDistance(), res2.getTreeEditDistance(), 0.000001);
        assertEquals(res1.getNodeCountA(), res2.getNodeCountA());
        assertEquals(res1.getNodeCountB(), res2.getNodeCountB());
    }

    @Test
    @DisplayName("Test 17: Score bounds strictly remain in [0.0, 100.0]")
    void testScoreBounds() {
        ScoringService.ScoreComputation scZero = scoringService.computeScore(100.0, 50, 50);
        assertEquals(0.0, scZero.getSimilarityPercentage(), 0.01);

        ScoringService.ScoreComputation scPerfect = scoringService.computeScore(0.0, 50, 50);
        assertEquals(100.0, scPerfect.getSimilarityPercentage(), 0.01);

        ScoringService.ScoreComputation scExcessive = scoringService.computeScore(120.0, 50, 50);
        assertEquals(0.0, scExcessive.getSimilarityPercentage(), 0.01);
    }

    @Test
    @DisplayName("Test 18: Hand-verifiable small tree-edit distance")
    void testSmallTreeEditDistance() {
        CommonAstNode a = new CommonAstNode("a", "root", "Root", AstCategory.PROGRAM);
        CommonAstNode a1 = new CommonAstNode("a1", "leaf", "Leaf", AstCategory.IDENTIFIER);
        a1.setNormalizedValue("x");
        a.addChild(a1);

        CommonAstNode b = new CommonAstNode("b", "root", "Root", AstCategory.PROGRAM);
        CommonAstNode b1 = new CommonAstNode("b1", "leaf", "Leaf", AstCategory.IDENTIFIER);
        b1.setNormalizedValue("x");
        b.addChild(b1);

        TreeEditDistance.DistanceResult dist = treeEditDistance.calculate(a, b);
        assertEquals(0.0, dist.getDistance(), 0.001);

        // Change leaf value
        b1.setNormalizedValue("y");
        TreeEditDistance.DistanceResult distChanged = treeEditDistance.calculate(a, b);
        assertEquals(0.2, distChanged.getDistance(), 0.001);
    }

    @Test
    @DisplayName("Test 19: Report generation generates valid JSON and CSV")
    void testReportGeneration() throws Exception {
        String code = "int main() { return 0; }";
        CompareRequest req = new CompareRequest();
        req.setLanguage("C");
        req.setSourceA(code);
        req.setSourceB(code);

        CompareResponse res = comparisonEngine.compare(req);
        String jsonReport = reportService.generateJsonReport(res);
        assertNotNull(jsonReport);
        assertTrue(jsonReport.contains("similarityPercentage"));
        assertTrue(jsonReport.contains("100"));

        String csvSummary = reportService.generateCsvSummary(res);
        assertNotNull(csvSummary);
        assertTrue(csvSummary.contains("Final Similarity Percentage,100.00%"));
    }
}
