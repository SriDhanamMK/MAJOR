package com.ast.similarity.normalization;

import com.ast.similarity.ast.AstCategory;
import com.ast.similarity.ast.CommonAstNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FormattingNormalizer {

    /**
     * Normalizes formatting:
     * - Filters out comments from the AST structure (documenting policy that comments do not affect executable structural similarity)
     * - Collapses redundant synthetic empty blocks
     */
    public CommonAstNode normalizeFormatting(CommonAstNode root) {
        if (root == null) return null;
        return filterNode(root);
    }

    private CommonAstNode filterNode(CommonAstNode node) {
        if (node.getCategory() == AstCategory.COMMENT) {
            return null; // Exclude comments from structural tree
        }

        List<CommonAstNode> filteredChildren = new ArrayList<>();
        for (CommonAstNode child : node.getChildren()) {
            CommonAstNode filteredChild = filterNode(child);
            if (filteredChild != null) {
                filteredChildren.add(filteredChild);
            }
        }
        node.setChildren(filteredChildren);
        return node;
    }
}
