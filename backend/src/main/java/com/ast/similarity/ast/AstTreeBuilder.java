package com.ast.similarity.ast;

import com.ast.similarity.parser.RawAstNode;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class AstTreeBuilder {

    private static final Set<String> IGNORED_ANONYMOUS_PUNCTUATION = new HashSet<>(Arrays.asList(
            "{", "}", "(", ")", "[", "]", ";", ",", ":", "...", "\n"
    ));

    public CommonAstNode buildTree(RawAstNode rawRoot, String language, String idPrefix) {
        if (rawRoot == null) return null;
        int[] counter = new int[]{0};
        return convertNode(rawRoot, language, idPrefix != null ? idPrefix : "", counter);
    }

    private CommonAstNode convertNode(RawAstNode raw, String language, String prefix, int[] counter) {
        String type = raw.getType();

        // Skip non-essential punctuation tokens
        if (!raw.isNamed() && IGNORED_ANONYMOUS_PUNCTUATION.contains(type)) {
            return null;
        }

        String nodeId = prefix + "n" + (counter[0]++);
        AstCategory category = mapCategory(type, language);
        String normalizedType = mapNormalizedType(type, category);

        CommonAstNode node = new CommonAstNode(nodeId, type, normalizedType, category);
        node.setNamed(raw.isNamed());
        node.setError(raw.isError() || "ERROR".equalsIgnoreCase(type));
        node.setStartLine(raw.getStartLine());
        node.setStartCol(raw.getStartCol());
        node.setEndLine(raw.getEndLine());
        node.setEndCol(raw.getEndCol());
        // Only leaf/token nodes carry significant value; composite/structural nodes rely on AST structure
        boolean hasChildren = raw.getChildren() != null && !raw.getChildren().isEmpty();
        if (hasChildren && category != AstCategory.IDENTIFIER && category != AstCategory.LITERAL && category != AstCategory.OPERATOR) {
            node.setRawValue(null);
            node.setNormalizedValue(null);
        } else {
            node.setRawValue(raw.getText());
            node.setNormalizedValue(raw.getText());
        }

        if (raw.getChildren() != null) {
            for (RawAstNode childRaw : raw.getChildren()) {
                CommonAstNode childNode = convertNode(childRaw, language, prefix, counter);
                if (childNode != null) {
                    node.addChild(childNode);
                }
            }
        }

        // Annotate statements and definitions for reordering & dependency analysis
        annotateNodeDataflow(node, language);

        return node;
    }

    private AstCategory mapCategory(String type, String lang) {
        if (type == null) return AstCategory.OTHER;
        String t = type.toLowerCase();

        if (t.equals("translation_unit") || t.equals("module")) {
            return AstCategory.PROGRAM;
        }
        if (t.contains("function_definition") || t.contains("method_definition") || t.equals("class_definition")) {
            return AstCategory.FUNCTION_DECLARATION;
        }
        if (t.contains("declaration") || t.equals("parameter_declaration") || t.contains("assignment")) {
            return AstCategory.VARIABLE_DECLARATION;
        }
        if (t.endsWith("_statement") || t.equals("return_statement") || t.equals("expression_statement") ||
                t.equals("break_statement") || t.equals("continue_statement") || t.equals("pass_statement")) {
            return AstCategory.STATEMENT;
        }
        if (t.contains("if_") || t.contains("while_") || t.contains("for_") || t.contains("switch_") || t.contains("do_statement")) {
            return AstCategory.CONTROL_FLOW;
        }
        if (t.equals("compound_statement") || t.equals("block") || t.equals("parameters") || t.equals("parameter_list")) {
            return AstCategory.BLOCK;
        }
        if (t.endsWith("_expression") || t.endsWith("_operator") || t.equals("call") || t.equals("call_expression") ||
                t.equals("binary_operator") || t.equals("comparison_operator") || t.equals("subscript")) {
            return AstCategory.EXPRESSION;
        }
        if (t.equals("identifier") || t.equals("field_identifier") || t.equals("type_identifier")) {
            return AstCategory.IDENTIFIER;
        }
        if (t.contains("literal") || t.equals("number_literal") || t.equals("string_literal") ||
                t.equals("char_literal") || t.equals("integer") || t.equals("float") ||
                t.equals("string") || t.equals("true") || t.equals("false") || t.equals("null") || t.equals("none")) {
            return AstCategory.LITERAL;
        }
        if (t.equals("primitive_type") || t.equals("type_specifier") || t.equals("sized_type_specifier")) {
            return AstCategory.TYPE;
        }
        if (t.contains("comment")) {
            return AstCategory.COMMENT;
        }
        if (t.equals("error") || t.equals("missing")) {
            return AstCategory.ERROR;
        }
        if (isOperatorSymbol(type)) {
            return AstCategory.OPERATOR;
        }
        return AstCategory.OTHER;
    }

    private String mapNormalizedType(String originalType, AstCategory category) {
        switch (category) {
            case PROGRAM: return "Program";
            case FUNCTION_DECLARATION: return "FunctionDecl";
            case VARIABLE_DECLARATION:
                if (originalType.contains("parameter")) return "ParamDecl";
                if (originalType.contains("assignment")) return "AssignStmt";
                return "VarDecl";
            case CONTROL_FLOW:
                if (originalType.contains("if")) return "IfStmt";
                if (originalType.contains("while")) return "WhileStmt";
                if (originalType.contains("for")) return "ForStmt";
                if (originalType.contains("switch")) return "SwitchStmt";
                return "ControlFlow";
            case STATEMENT:
                if (originalType.contains("return")) return "ReturnStmt";
                if (originalType.contains("expression")) return "ExprStmt";
                return "Statement";
            case BLOCK: return "Block";
            case EXPRESSION:
                if (originalType.contains("binary")) return "BinaryExpr";
                if (originalType.contains("call")) return "CallExpr";
                if (originalType.contains("unary")) return "UnaryExpr";
                return "Expression";
            case IDENTIFIER: return "Identifier";
            case LITERAL: return "Literal";
            case OPERATOR: return "Operator";
            case TYPE: return "Type";
            case COMMENT: return "Comment";
            case ERROR: return "ErrorNode";
            default: return originalType;
        }
    }

    private boolean isOperatorSymbol(String str) {
        return str != null && Arrays.asList("+", "-", "*", "/", "%", "==", "!=", "<", ">", "<=", ">=",
                "=", "+=", "-=", "*=", "/=", "&&", "||", "&", "|", "^", "!", "~", "<<", ">>").contains(str);
    }

    private void annotateNodeDataflow(CommonAstNode node, String lang) {
        if (node.getCategory() == AstCategory.VARIABLE_DECLARATION) {
            // Find identifier defined
            for (CommonAstNode c : node.getChildren()) {
                if (c.getCategory() == AstCategory.IDENTIFIER) {
                    node.getDefVars().add(c.getRawValue());
                } else {
                    collectUsedIdentifiers(c, node.getUseVars());
                }
            }
        } else if (node.getCategory() == AstCategory.STATEMENT || node.getCategory() == AstCategory.EXPRESSION) {
            collectUsedIdentifiers(node, node.getUseVars());
            if (node.getOriginalType().contains("call") || node.getNormalizedType().equals("CallExpr")) {
                node.setHasSideEffects(true);
            }
            for (CommonAstNode c : node.getChildren()) {
                if (c.isHasSideEffects()) {
                    node.setHasSideEffects(true);
                }
            }
        }
    }

    private void collectUsedIdentifiers(CommonAstNode node, Set<String> useSet) {
        if (node.getCategory() == AstCategory.IDENTIFIER && node.getRawValue() != null) {
            useSet.add(node.getRawValue());
        }
        for (CommonAstNode child : node.getChildren()) {
            collectUsedIdentifiers(child, useSet);
        }
    }
}
