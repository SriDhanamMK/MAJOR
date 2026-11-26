package com.ast.similarity.normalization;

import com.ast.similarity.ast.AstCategory;
import com.ast.similarity.ast.CommonAstNode;
import org.springframework.stereotype.Component;

@Component
public class LiteralNormalizer {

    public void normalizeLiterals(CommonAstNode node, boolean enableAbstraction) {
        if (node == null) return;

        if (node.getCategory() == AstCategory.LITERAL) {
            if (enableAbstraction) {
                String orig = node.getOriginalType().toLowerCase();
                if (orig.contains("number") || orig.contains("int") || orig.contains("float")) {
                    node.setNormalizedValue("<NUMERIC_LITERAL>");
                } else if (orig.contains("string") || orig.contains("char")) {
                    node.setNormalizedValue("<STRING_LITERAL>");
                } else if (orig.contains("true") || orig.contains("false") || orig.contains("bool")) {
                    node.setNormalizedValue("<BOOLEAN_LITERAL>");
                } else {
                    node.setNormalizedValue("<LITERAL>");
                }
            } else {
                // Preserved exact literal value by default
                node.setNormalizedValue(node.getRawValue());
            }
        }

        for (CommonAstNode child : node.getChildren()) {
            normalizeLiterals(child, enableAbstraction);
        }
    }
}
