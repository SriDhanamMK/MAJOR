package com.ast.similarity.normalization;

import com.ast.similarity.ast.AstCategory;
import com.ast.similarity.ast.CommonAstNode;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class IdentifierNormalizer {

    public static class RenamingEntry {
        private final String original;
        private final String canonical;
        private final String kind;
        private final int line;

        public RenamingEntry(String original, String canonical, String kind, int line) {
            this.original = original;
            this.canonical = canonical;
            this.kind = kind;
            this.line = line;
        }

        public String getOriginal() { return original; }
        public String getCanonical() { return canonical; }
        public String getKind() { return kind; }
        public int getLine() { return line; }
    }

    public List<RenamingEntry> normalizeIdentifiers(CommonAstNode root) {
        List<RenamingEntry> renamings = new ArrayList<>();
        if (root == null) return renamings;

        // Top-level scope
        SymbolScope globalScope = new SymbolScope(null);
        traverseAndNormalize(root, globalScope, renamings);
        return renamings;
    }

    private void traverseAndNormalize(CommonAstNode node, SymbolScope currentScope,
                                      List<RenamingEntry> renamings) {
        if (node == null) return;

        SymbolScope activeScope = currentScope;

        if (node.getCategory() == AstCategory.FUNCTION_DECLARATION) {
            // Function declaration creates a new lexical scope for parameters & body
            activeScope = new SymbolScope(currentScope);

            // Function name itself is preserved as-is by default
            CommonAstNode funcNameNode = findFunctionIdentifier(node);
            if (funcNameNode != null && funcNameNode.getRawValue() != null) {
                funcNameNode.setNormalizedValue(funcNameNode.getRawValue());
            }

            // Register parameters
            registerParametersInScope(node, activeScope, renamings);

            // Traverse children with the new function scope
            for (CommonAstNode child : node.getChildren()) {
                if (child != funcNameNode) {
                    traverseAndNormalize(child, activeScope, renamings);
                }
            }
            return;
        }

        if (node.getCategory() == AstCategory.VARIABLE_DECLARATION ||
                node.getOriginalType().contains("declaration") ||
                node.getOriginalType().equals("assignment")) {
            // Variable declaration or assignment: register new local variable(s)
            registerVariablesInScope(node, activeScope, renamings);
        }

        // Identifier resolution
        if (node.getCategory() == AstCategory.IDENTIFIER && node.getRawValue() != null) {
            String orig = node.getRawValue();
            String canonical = activeScope.resolve(orig);
            if (canonical != null) {
                node.setNormalizedValue(canonical);
            } else {
                // Preserved external or global identifier
                node.setNormalizedValue(orig);
            }
        }

        // Recursively traverse children
        for (CommonAstNode child : node.getChildren()) {
            traverseAndNormalize(child, activeScope, renamings);
        }
    }

    private CommonAstNode findFunctionIdentifier(CommonAstNode funcNode) {
        for (CommonAstNode child : funcNode.getChildren()) {
            if (child.getCategory() == AstCategory.IDENTIFIER) {
                return child;
            }
            if (child.getOriginalType().contains("declarator")) {
                CommonAstNode res = findFirstIdentifier(child);
                if (res != null) return res;
            }
        }
        return null;
    }

    private CommonAstNode findFirstIdentifier(CommonAstNode node) {
        if (node.getCategory() == AstCategory.IDENTIFIER) return node;
        for (CommonAstNode child : node.getChildren()) {
            CommonAstNode res = findFirstIdentifier(child);
            if (res != null) return res;
        }
        return null;
    }

    private void registerParametersInScope(CommonAstNode funcNode, SymbolScope scope, List<RenamingEntry> renamings) {
        for (CommonAstNode child : funcNode.getChildren()) {
            if (child.getOriginalType().contains("parameter") || child.getNormalizedType().equals("Block") ||
                    child.getOriginalType().contains("declarator")) {
                collectParameterDeclarations(child, scope, renamings);
            }
        }
    }

    private void collectParameterDeclarations(CommonAstNode node, SymbolScope scope, List<RenamingEntry> renamings) {
        if (node.getOriginalType().contains("parameter_declaration") || node.getNormalizedType().equals("ParamDecl")) {
            CommonAstNode idNode = findFirstIdentifier(node);
            if (idNode != null && idNode.getRawValue() != null) {
                String orig = idNode.getRawValue();
                String canonical = scope.registerParameter(orig);
                idNode.setNormalizedValue(canonical);
                renamings.add(new RenamingEntry(orig, canonical, "PARAMETER", idNode.getStartLine()));
            }
            return;
        }
        for (CommonAstNode child : node.getChildren()) {
            collectParameterDeclarations(child, scope, renamings);
        }
    }

    private void registerVariablesInScope(CommonAstNode varNode, SymbolScope scope, List<RenamingEntry> renamings) {
        if (varNode.getOriginalType().equals("assignment")) {
            // Python: left-hand side is target
            if (!varNode.getChildren().isEmpty()) {
                CommonAstNode target = varNode.getChildren().get(0);
                CommonAstNode idNode = findFirstIdentifier(target);
                if (idNode != null && idNode.getRawValue() != null) {
                    String orig = idNode.getRawValue();
                    String canonical = scope.registerVariable(orig);
                    idNode.setNormalizedValue(canonical);
                    renamings.add(new RenamingEntry(orig, canonical, "LOCAL_VARIABLE", idNode.getStartLine()));
                }
            }
            return;
        }

        // C / C++: look for init_declarator or declarator
        collectCVariableDeclarators(varNode, scope, renamings);
    }

    private void collectCVariableDeclarators(CommonAstNode node, SymbolScope scope, List<RenamingEntry> renamings) {
        if (node.getOriginalType().contains("init_declarator")) {
            CommonAstNode idNode = null;
            for (CommonAstNode child : node.getChildren()) {
                if (child.getCategory() == AstCategory.IDENTIFIER) {
                    idNode = child;
                    break;
                }
                if (child.getOriginalType().contains("declarator")) {
                    idNode = findFirstIdentifier(child);
                    if (idNode != null) break;
                }
            }
            if (idNode != null && idNode.getRawValue() != null) {
                String orig = idNode.getRawValue();
                String canonical = scope.registerVariable(orig);
                idNode.setNormalizedValue(canonical);
                renamings.add(new RenamingEntry(orig, canonical, "LOCAL_VARIABLE", idNode.getStartLine()));
            }
            return;
        }

        for (CommonAstNode child : node.getChildren()) {
            if (child.getOriginalType().contains("declarator")) {
                collectCVariableDeclarators(child, scope, renamings);
            }
        }
    }
}
