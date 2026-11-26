package com.ast.similarity.comparison;

import com.ast.similarity.ast.AstCategory;
import com.ast.similarity.ast.CommonAstNode;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class StatementDependencyAnalyzer {

    public static class ReorderingRecord {
        private final int startLine;
        private final int endLine;
        private final boolean independent;
        private final String explanation;
        private final List<String> statementSummaries;

        public ReorderingRecord(int startLine, int endLine, boolean independent, String explanation, List<String> statementSummaries) {
            this.startLine = startLine;
            this.endLine = endLine;
            this.independent = independent;
            this.explanation = explanation;
            this.statementSummaries = statementSummaries;
        }

        public int getStartLine() { return startLine; }
        public int getEndLine() { return endLine; }
        public boolean isIndependent() { return independent; }
        public String getExplanation() { return explanation; }
        public List<String> getStatementSummaries() { return statementSummaries; }
    }

    /**
     * Examines blocks and compound statements.
     * When consecutive statements are verified to have no RAW, WAR, or WAW hazards and no side effects,
     * they can be conservatively reordered to canonical order.
     */
    public List<ReorderingRecord> analyzeAndCanonicalizeReordering(CommonAstNode root, boolean enableReordering) {
        List<ReorderingRecord> records = new ArrayList<>();
        if (root == null) return records;

        walkBlocks(root, records, enableReordering);
        return records;
    }

    private void walkBlocks(CommonAstNode node, List<ReorderingRecord> records, boolean enableReordering) {
        if (node.getCategory() == AstCategory.BLOCK || node.getNormalizedType().equals("Block") ||
                node.getOriginalType().contains("compound_statement")) {
            processBlockStatements(node, records, enableReordering);
        }

        for (CommonAstNode child : node.getChildren()) {
            walkBlocks(child, records, enableReordering);
        }
    }

    private void processBlockStatements(CommonAstNode blockNode, List<ReorderingRecord> records, boolean enableReordering) {
        List<CommonAstNode> statements = new ArrayList<>();
        for (CommonAstNode c : blockNode.getChildren()) {
            if (isStatementLike(c)) {
                statements.add(c);
            }
        }

        if (statements.size() < 2) return;

        // Check pairs of consecutive statements
        int i = 0;
        while (i < statements.size()) {
            int j = i + 1;
            List<CommonAstNode> group = new ArrayList<>();
            group.add(statements.get(i));

            while (j < statements.size() && areMutuallyIndependent(statements.get(j - 1), statements.get(j))) {
                group.add(statements.get(j));
                j++;
            }

            if (group.size() > 1) {
                List<String> summaries = new ArrayList<>();
                for (CommonAstNode stmt : group) {
                    summaries.add("L" + stmt.getStartLine() + ": " + stmt.getNormalizedType() +
                            " [def=" + stmt.getDefVars() + ", use=" + stmt.getUseVars() + "]");
                }
                String msg = "Verified " + group.size() + " consecutive independent statements with disjoint reads/writes and no side effects.";
                records.add(new ReorderingRecord(group.get(0).getStartLine(), group.get(group.size() - 1).getEndLine(), true, msg, summaries));

                if (enableReordering) {
                    // Canonicalize group ordering by structural signature
                    canonicalizeStatementGroup(blockNode, group);
                }
                i = j;
            } else {
                // If not independent, check why and record limitation
                if (i + 1 < statements.size()) {
                    CommonAstNode s1 = statements.get(i);
                    CommonAstNode s2 = statements.get(i + 1);
                    String hazard = detectHazard(s1, s2);
                    if (hazard != null) {
                        records.add(new ReorderingRecord(s1.getStartLine(), s2.getEndLine(), false,
                                "Dependency hazard between L" + s1.getStartLine() + " and L" + s2.getStartLine() + ": " + hazard + ". Order strictly preserved.",
                                Arrays.asList("L" + s1.getStartLine(), "L" + s2.getStartLine())));
                    }
                }
                i++;
            }
        }
    }

    private boolean isStatementLike(CommonAstNode node) {
        return node.getCategory() == AstCategory.STATEMENT ||
                node.getCategory() == AstCategory.VARIABLE_DECLARATION ||
                node.getCategory() == AstCategory.CONTROL_FLOW;
    }

    private boolean areMutuallyIndependent(CommonAstNode s1, CommonAstNode s2) {
        if (s1.isHasSideEffects() || s2.isHasSideEffects()) return false;
        if (s1.getCategory() == AstCategory.CONTROL_FLOW || s2.getCategory() == AstCategory.CONTROL_FLOW) return false;

        // Check RAW (Read After Write)
        for (String def : s1.getDefVars()) {
            if (s2.getUseVars().contains(def)) return false;
        }
        // Check WAR (Write After Read)
        for (String use : s1.getUseVars()) {
            if (s2.getDefVars().contains(use)) return false;
        }
        // Check WAW (Write After Write)
        for (String def1 : s1.getDefVars()) {
            if (s2.getDefVars().contains(def1)) return false;
        }
        return true;
    }

    private String detectHazard(CommonAstNode s1, CommonAstNode s2) {
        if (s1.isHasSideEffects() || s2.isHasSideEffects()) {
            return "Potential observable side effect or function call";
        }
        if (s1.getCategory() == AstCategory.CONTROL_FLOW || s2.getCategory() == AstCategory.CONTROL_FLOW) {
            return "Control-flow boundary";
        }
        for (String def : s1.getDefVars()) {
            if (s2.getUseVars().contains(def)) return "RAW (Read-After-Write) on variable '" + def + "'";
        }
        for (String use : s1.getUseVars()) {
            if (s2.getDefVars().contains(use)) return "WAR (Write-After-Read) on variable '" + use + "'";
        }
        for (String def1 : s1.getDefVars()) {
            if (s2.getDefVars().contains(def1)) return "WAW (Write-After-Write) on variable '" + def1 + "'";
        }
        return null;
    }

    private void canonicalizeStatementGroup(CommonAstNode blockNode, List<CommonAstNode> group) {
        List<CommonAstNode> sorted = new ArrayList<>(group);
        sorted.sort(Comparator.comparing(this::computeStructuralSignature));

        // Re-inject sorted into block children
        List<CommonAstNode> newChildren = new ArrayList<>();
        int groupIdx = 0;
        for (CommonAstNode child : blockNode.getChildren()) {
            if (groupIdx < group.size() && child == group.get(groupIdx)) {
                newChildren.add(sorted.get(groupIdx));
                groupIdx++;
            } else {
                newChildren.add(child);
            }
        }
        blockNode.setChildren(newChildren);
    }

    private String computeStructuralSignature(CommonAstNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(node.getNormalizedType()).append("#").append(node.getNormalizedValue()).append("#");
        for (CommonAstNode c : node.getChildren()) {
            sb.append(computeStructuralSignature(c));
        }
        return sb.toString();
    }
}
