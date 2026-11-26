package com.ast.similarity.comparison;

import com.ast.similarity.ast.AstCategory;
import com.ast.similarity.ast.CommonAstNode;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TreeEditDistance {

    public static class EditOperation {
        public enum OpType { MATCH, SUBSTITUTE, INSERT, DELETE }

        private final OpType type;
        private final CommonAstNode nodeA;
        private final CommonAstNode nodeB;
        private final double cost;

        public EditOperation(OpType type, CommonAstNode nodeA, CommonAstNode nodeB, double cost) {
            this.type = type;
            this.nodeA = nodeA;
            this.nodeB = nodeB;
            this.cost = cost;
        }

        public OpType getType() { return type; }
        public CommonAstNode getNodeA() { return nodeA; }
        public CommonAstNode getNodeB() { return nodeB; }
        public double getCost() { return cost; }
    }

    public static class DistanceResult {
        private final double distance;
        private final int treeSizeA;
        private final int treeSizeB;
        private final List<EditOperation> operations;

        public DistanceResult(double distance, int treeSizeA, int treeSizeB, List<EditOperation> operations) {
            this.distance = distance;
            this.treeSizeA = treeSizeA;
            this.treeSizeB = treeSizeB;
            this.operations = operations;
        }

        public double getDistance() { return distance; }
        public int getTreeSizeA() { return treeSizeA; }
        public int getTreeSizeB() { return treeSizeB; }
        public List<EditOperation> getOperations() { return operations; }
    }

    public DistanceResult calculate(CommonAstNode treeA, CommonAstNode treeB) {
        if (treeA == null && treeB == null) {
            return new DistanceResult(0.0, 0, 0, Collections.emptyList());
        }
        if (treeA == null) {
            int sizeB = treeB.getDeepNodeCount();
            List<EditOperation> ops = new ArrayList<>();
            for (CommonAstNode b : treeB.getPostOrderList()) {
                b.setMatchStatus("INSERTED");
                b.setEditCost(1.0);
                ops.add(new EditOperation(EditOperation.OpType.INSERT, null, b, 1.0));
            }
            return new DistanceResult(sizeB * 1.0, 0, sizeB, ops);
        }
        if (treeB == null) {
            int sizeA = treeA.getDeepNodeCount();
            List<EditOperation> ops = new ArrayList<>();
            for (CommonAstNode a : treeA.getPostOrderList()) {
                a.setMatchStatus("DELETED");
                a.setEditCost(1.0);
                ops.add(new EditOperation(EditOperation.OpType.DELETE, a, null, 1.0));
            }
            return new DistanceResult(sizeA * 1.0, sizeA, 0, ops);
        }

        List<CommonAstNode> nodesA = treeA.getPostOrderList();
        List<CommonAstNode> nodesB = treeB.getPostOrderList();
        int m = nodesA.size();
        int n = nodesB.size();

        // Compute 1-based leftmost leaf descendants
        int[] lldA = computeLeftmostLeafDescendants(nodesA);
        int[] lldB = computeLeftmostLeafDescendants(nodesB);

        int[] krA = computeKeyroots(nodesA, lldA);
        int[] krB = computeKeyroots(nodesB, lldB);

        double[][] treeDist = new double[m + 1][n + 1];

        // Zhang-Shasha main DP loop
        for (int i : krA) {
            for (int j : krB) {
                computeForestDistance(i, j, lldA, lldB, nodesA, nodesB, treeDist);
            }
        }

        double totalDistance = treeDist[m][n];

        // Reconstruct alignment operations
        List<EditOperation> ops = reconstructOperations(nodesA, nodesB, treeDist);

        return new DistanceResult(totalDistance, m, n, ops);
    }

    private void computeForestDistance(int krA, int krB, int[] lldA, int[] lldB,
                                       List<CommonAstNode> nodesA, List<CommonAstNode> nodesB,
                                       double[][] treeDist) {
        int startA = lldA[krA];
        int startB = lldB[krB];

        int fRows = krA - startA + 2;
        int fCols = krB - startB + 2;
        double[][] fDist = new double[fRows][fCols];
        fDist[0][0] = 0;

        for (int i = startA; i <= krA; i++) {
            fDist[i - startA + 1][0] = fDist[i - startA][0] + 1.0;
        }
        for (int j = startB; j <= krB; j++) {
            fDist[0][j - startB + 1] = fDist[0][j - startB] + 1.0;
        }

        for (int i = startA; i <= krA; i++) {
            for (int j = startB; j <= krB; j++) {
                int rI = i - startA + 1;
                int rJ = j - startB + 1;

                if (lldA[i] == startA && lldB[j] == startB) {
                    double subCost = substitutionCost(nodesA.get(i - 1), nodesB.get(j - 1));
                    fDist[rI][rJ] = Math.min(
                            Math.min(fDist[rI - 1][rJ] + 1.0, fDist[rI][rJ - 1] + 1.0),
                            fDist[rI - 1][rJ - 1] + subCost
                    );
                    treeDist[i][j] = fDist[rI][rJ];
                } else {
                    int pI = lldA[i] - startA;
                    int pJ = lldB[j] - startB;
                    // DIAGNOSTIC: log violation before crash so we can see the state
                    if (pI < 0 || pI >= fRows || pJ < 0 || pJ >= fCols) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("[ZS-DIAGNOSTIC] INVARIANT VIOLATION: krA=").append(krA)
                          .append(" krB=").append(krB)
                          .append(" startA=").append(startA)
                          .append(" startB=").append(startB)
                          .append(" i=").append(i)
                          .append(" j=").append(j)
                          .append(" lldA[i]=").append(lldA[i])
                          .append(" lldB[j]=").append(lldB[j])
                          .append(" pI=").append(pI)
                          .append(" pJ=").append(pJ)
                          .append(" fRows=").append(fRows)
                          .append(" fCols=").append(fCols);
                        sb.append("\n  lldA=[");
                        for (int x = 1; x < lldA.length; x++) sb.append(lldA[x]).append(x < lldA.length-1 ? "," : "]");
                        sb.append("\n  lldB=[");
                        for (int x = 1; x < lldB.length; x++) sb.append(lldB[x]).append(x < lldB.length-1 ? "," : "]");
                        sb.append("\n  nodesA types=[");
                        for (int x = 0; x < nodesA.size(); x++) sb.append(nodesA.get(x).getNormalizedType()).append(x < nodesA.size()-1 ? "," : "]");
                        System.err.println(sb);
                        // Now throw with more context
                        throw new ArrayIndexOutOfBoundsException("[ZS] pI=" + pI + " pJ=" + pJ
                            + " fRows=" + fRows + " fCols=" + fCols
                            + " krA=" + krA + " startA=" + startA
                            + " i=" + i + " lldA[i]=" + lldA[i]);
                    }
                    fDist[rI][rJ] = Math.min(
                            Math.min(fDist[rI - 1][rJ] + 1.0, fDist[rI][rJ - 1] + 1.0),
                            fDist[pI][pJ] + treeDist[i][j]
                    );
                }
            }
        }
    }

    public double substitutionCost(CommonAstNode a, CommonAstNode b) {
        if (a == null || b == null) return 1.0;

        boolean sameType = Objects.equals(a.getNormalizedType(), b.getNormalizedType());
        boolean sameValue = Objects.equals(a.getNormalizedValue(), b.getNormalizedValue());

        if (sameType) {
            if (sameValue) {
                return 0.0; // Exact structural & normalized match
            }
            if (a.getCategory() == AstCategory.LITERAL || a.getCategory() == AstCategory.OPERATOR) {
                return 0.4; // Same syntax role, differing literal/operator value
            }
            return 0.2; // Same node type, slight variation
        }

        if (a.getCategory() == b.getCategory()) {
            return 0.6; // Same semantic category, different specific type
        }

        return 1.0; // Completely different node categories
    }

    private int[] computeLeftmostLeafDescendants(List<CommonAstNode> postOrder) {
        int n = postOrder.size();
        int[] lld = new int[n + 1];
        Map<String, Integer> indexMap = new HashMap<>();

        for (int i = 0; i < n; i++) {
            indexMap.put(postOrder.get(i).getId(), i + 1);
        }

        for (int i = 0; i < n; i++) {
            CommonAstNode node = postOrder.get(i);
            CommonAstNode leftMost = getLeftmostLeaf(node);
            Integer idx = indexMap.get(leftMost.getId());
            lld[i + 1] = idx != null ? idx : (i + 1);
        }
        return lld;
    }

    private CommonAstNode getLeftmostLeaf(CommonAstNode node) {
        CommonAstNode curr = node;
        while (curr.getChildren() != null && !curr.getChildren().isEmpty()) {
            curr = curr.getChildren().get(0);
        }
        return curr;
    }

    private int[] computeKeyroots(List<CommonAstNode> postOrder, int[] lld) {
        int n = postOrder.size();
        // A node i is a keyroot if no j > i has lld[j] == lld[i]
        Map<Integer, Integer> maxIndexForLld = new HashMap<>();
        for (int i = 1; i <= n; i++) {
            maxIndexForLld.put(lld[i], i);
        }

        List<Integer> keyroots = new ArrayList<>(maxIndexForLld.values());
        Collections.sort(keyroots);

        int[] res = new int[keyroots.size()];
        for (int i = 0; i < keyroots.size(); i++) {
            res[i] = keyroots.get(i);
        }
        return res;
    }

    private List<EditOperation> reconstructOperations(List<CommonAstNode> nodesA, List<CommonAstNode> nodesB,
                                                      double[][] treeDist) {
        List<EditOperation> ops = new ArrayList<>();
        Set<String> matchedAIds = new HashSet<>();
        Set<String> matchedBIds = new HashSet<>();

        // Reconstruct best alignment matching using distance table backtracking
        int i = nodesA.size();
        int j = nodesB.size();

        while (i > 0 && j > 0) {
            CommonAstNode a = nodesA.get(i - 1);
            CommonAstNode b = nodesB.get(j - 1);
            double subCost = substitutionCost(a, b);

            double current = treeDist[i][j];
            double diag = treeDist[i - 1][j - 1] + subCost;
            double up = treeDist[i - 1][j] + 1.0;
            double left = treeDist[i][j - 1] + 1.0;

            if (Math.abs(current - diag) < 1e-5) {
                matchedAIds.add(a.getId());
                matchedBIds.add(b.getId());
                a.setMatchedNodeId(b.getId());
                b.setMatchedNodeId(a.getId());
                a.setEditCost(subCost);
                b.setEditCost(subCost);

                if (subCost == 0.0) {
                    a.setMatchStatus("MATCHED");
                    b.setMatchStatus("MATCHED");
                    ops.add(new EditOperation(EditOperation.OpType.MATCH, a, b, 0.0));
                } else {
                    a.setMatchStatus("SUBSTITUTED");
                    b.setMatchStatus("SUBSTITUTED");
                    ops.add(new EditOperation(EditOperation.OpType.SUBSTITUTE, a, b, subCost));
                }
                i--;
                j--;
            } else if (Math.abs(current - up) < 1e-5) {
                a.setMatchStatus("DELETED");
                a.setEditCost(1.0);
                ops.add(new EditOperation(EditOperation.OpType.DELETE, a, null, 1.0));
                matchedAIds.add(a.getId());
                i--;
            } else {
                b.setMatchStatus("INSERTED");
                b.setEditCost(1.0);
                ops.add(new EditOperation(EditOperation.OpType.INSERT, null, b, 1.0));
                matchedBIds.add(b.getId());
                j--;
            }
        }

        while (i > 0) {
            CommonAstNode a = nodesA.get(i - 1);
            if (!matchedAIds.contains(a.getId())) {
                a.setMatchStatus("DELETED");
                a.setEditCost(1.0);
                ops.add(new EditOperation(EditOperation.OpType.DELETE, a, null, 1.0));
            }
            i--;
        }

        while (j > 0) {
            CommonAstNode b = nodesB.get(j - 1);
            if (!matchedBIds.contains(b.getId())) {
                b.setMatchStatus("INSERTED");
                b.setEditCost(1.0);
                ops.add(new EditOperation(EditOperation.OpType.INSERT, null, b, 1.0));
            }
            j--;
        }

        return ops;
    }
}
