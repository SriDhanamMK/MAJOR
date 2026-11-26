package com.ast.similarity;

import com.ast.similarity.ast.AstCategory;
import com.ast.similarity.ast.CommonAstNode;
import com.ast.similarity.comparison.ComparisonEngine;
import com.ast.similarity.comparison.TreeEditDistance;
import com.ast.similarity.dto.CompareRequest;
import com.ast.similarity.dto.CompareResponse;
import com.ast.similarity.normalization.AstNormalizer;
import com.ast.similarity.parser.LanguageParserAdapter;
import com.ast.similarity.parser.ParserFactory;
import com.ast.similarity.parser.RawParseResult;
import com.ast.similarity.comparison.StatementDependencyAnalyzer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Diagnostic runner: traces postorder, lld, keyroots for a failing test case
 * to pinpoint the exact invariant violation.
 */
@SpringBootTest
public class TestDiagnosticRunner {

    @Autowired
    private ParserFactory parserFactory;

    @Autowired
    private AstNormalizer astNormalizer;

    @Autowired
    private StatementDependencyAnalyzer dependencyAnalyzer;

    @Autowired
    private TreeEditDistance treeEditDistance;

    @Autowired
    private ComparisonEngine comparisonEngine;

    /**
     * Trace the exact postorder and lld arrays for the failing testFormattingVariations input.
     */
    @Test
    void traceFailingCase() throws Exception {
        String codeA = "int main() {\n  int x = 1;\n  return x;\n}";
        String codeB = "int    main  (  )\n{\n\n    int   x   =   1  ;\n\n    // Arbitrary comment\n    return    x ;\n}\n";

        LanguageParserAdapter adapter = parserFactory.getAdapter("C");

        CommonAstNode treeA = adapter.parseToCommonAst(codeA, "A_");
        CommonAstNode treeB = adapter.parseToCommonAst(codeB, "B_");

        AstNormalizer.NormalizationResult normA = astNormalizer.normalize(treeA, false);
        AstNormalizer.NormalizationResult normB = astNormalizer.normalize(treeB, false);

        dependencyAnalyzer.analyzeAndCanonicalizeReordering(normA.getNormalizedRoot(), false);
        dependencyAnalyzer.analyzeAndCanonicalizeReordering(normB.getNormalizedRoot(), false);

        CommonAstNode rootA = normA.getNormalizedRoot();
        CommonAstNode rootB = normB.getNormalizedRoot();

        // Trace postorder
        List<CommonAstNode> postA = rootA.getPostOrderList();
        List<CommonAstNode> postB = rootB.getPostOrderList();
        int m = postA.size();
        int n = postB.size();

        System.out.println("=== TREE A postorder (" + m + " nodes) ===");
        Map<String, Integer> idxA = new HashMap<>();
        for (int i = 0; i < m; i++) {
            idxA.put(postA.get(i).getId(), i + 1);
            System.out.printf("  [%2d] id=%-15s type=%-20s val=%-10s children=%d%n",
                    i + 1,
                    postA.get(i).getId(),
                    postA.get(i).getNormalizedType(),
                    postA.get(i).getNormalizedValue(),
                    postA.get(i).getChildren().size());
        }

        // Compute lld manually (same logic as TreeEditDistance)
        int[] lldA = computeLldDebug(postA, idxA);
        int[] krA  = computeKeyrootsDebug(m, lldA);

        System.out.println("\n=== LLD(A) ===");
        for (int i = 1; i <= m; i++) {
            System.out.printf("  lldA[%2d] (%s) = %d%n", i,
                    postA.get(i-1).getNormalizedType(), lldA[i]);
        }

        System.out.println("\n=== Keyroots(A) ===");
        System.out.println("  " + Arrays.toString(krA));

        // Now simulate computeForestDistance for each keyroot pair and find the crash
        System.out.println("\n=== Simulating computeForestDistance ===");
        boolean foundViolation = false;
        for (int ki : krA) {
            int startA = lldA[ki];
            int fRows = ki - startA + 2;
            for (int i = startA; i <= ki; i++) {
                if (lldA[i] < startA) {
                    System.out.printf("INVARIANT VIOLATION: i=%d lldA[i]=%d startA=%d (for keyroot %d)%n",
                            i, lldA[i], startA, ki);
                    System.out.printf("  pI = lldA[i] - startA = %d  (NEGATIVE! fDist size=%d)%n",
                            lldA[i] - startA, fRows);
                    foundViolation = true;
                } else if (lldA[i] - startA >= fRows) {
                    System.out.printf("UPPER BOUND VIOLATION: i=%d lldA[i]=%d startA=%d pI=%d fDist size=%d%n",
                            i, lldA[i], startA, lldA[i]-startA, fRows);
                    foundViolation = true;
                }
            }
        }
        if (!foundViolation) {
            System.out.println("  No invariant violation found in lld/keyroot arrays.");
            System.out.println("  The bug must be elsewhere (e.g., treeDist access).");
        }

        // Also check whether getLeftmostLeaf() for any node returns a node NOT in postorder
        System.out.println("\n=== Leftmost Leaf consistency check ===");
        for (int i = 0; i < m; i++) {
            CommonAstNode node = postA.get(i);
            CommonAstNode leaf = getLeftmostLeaf(node);
            Integer leafIdx = idxA.get(leaf.getId());
            if (leafIdx == null) {
                System.out.printf("MISSING LEAF: node[%d]=%s -> leaf id=%s NOT IN POSTORDER!%n",
                        i+1, node.getId(), leaf.getId());
                foundViolation = true;
            }
        }
        if (!foundViolation) {
            System.out.println("  All leftmost leaves are in the postorder list.");
        }
    }

    private int[] computeLldDebug(List<CommonAstNode> postOrder, Map<String, Integer> indexMap) {
        int n = postOrder.size();
        int[] lld = new int[n + 1];
        for (int i = 0; i < n; i++) {
            CommonAstNode node = postOrder.get(i);
            CommonAstNode leftMost = getLeftmostLeaf(node);
            Integer idx = indexMap.get(leftMost.getId());
            lld[i + 1] = idx != null ? idx : (i + 1);
            if (idx == null) {
                System.out.printf("FALLBACK USED: node[%d] id=%s -> leftMost id=%s not in map, using %d%n",
                        i+1, node.getId(), leftMost.getId(), i+1);
            }
        }
        return lld;
    }

    private int[] computeKeyrootsDebug(int n, int[] lld) {
        Map<Integer, Integer> maxForLld = new HashMap<>();
        for (int i = 1; i <= n; i++) {
            maxForLld.put(lld[i], i);
        }
        List<Integer> list = new ArrayList<>(maxForLld.values());
        Collections.sort(list);
        int[] res = new int[list.size()];
        for (int i = 0; i < list.size(); i++) res[i] = list.get(i);
        return res;
    }

    private CommonAstNode getLeftmostLeaf(CommonAstNode node) {
        CommonAstNode curr = node;
        while (curr.getChildren() != null && !curr.getChildren().isEmpty()) {
            curr = curr.getChildren().get(0);
        }
        return curr;
    }
}
