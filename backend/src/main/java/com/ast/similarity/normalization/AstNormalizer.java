package com.ast.similarity.normalization;

import com.ast.similarity.ast.CommonAstNode;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AstNormalizer {

    private final FormattingNormalizer formattingNormalizer;
    private final IdentifierNormalizer identifierNormalizer;
    private final LiteralNormalizer literalNormalizer;

    public AstNormalizer(FormattingNormalizer formattingNormalizer,
                         IdentifierNormalizer identifierNormalizer,
                         LiteralNormalizer literalNormalizer) {
        this.formattingNormalizer = formattingNormalizer;
        this.identifierNormalizer = identifierNormalizer;
        this.literalNormalizer = literalNormalizer;
    }

    public static class NormalizationResult {
        private final CommonAstNode normalizedRoot;
        private final List<IdentifierNormalizer.RenamingEntry> renamings;
        private final boolean formattingApplied;
        private final boolean literalsAbstracted;

        public NormalizationResult(CommonAstNode normalizedRoot,
                                   List<IdentifierNormalizer.RenamingEntry> renamings,
                                   boolean formattingApplied,
                                   boolean literalsAbstracted) {
            this.normalizedRoot = normalizedRoot;
            this.renamings = renamings;
            this.formattingApplied = formattingApplied;
            this.literalsAbstracted = literalsAbstracted;
        }

        public CommonAstNode getNormalizedRoot() { return normalizedRoot; }
        public List<IdentifierNormalizer.RenamingEntry> getRenamings() { return renamings; }
        public boolean isFormattingApplied() { return formattingApplied; }
        public boolean isLiteralsAbstracted() { return literalsAbstracted; }
    }

    public NormalizationResult normalize(CommonAstNode rawAst, boolean abstractLiterals) {
        if (rawAst == null) return null;

        // 1. Formatting normalization (filter comments, normalize layout)
        CommonAstNode cleanTree = formattingNormalizer.normalizeFormatting(rawAst);

        // 2. Identifier canonicalization (parameters and local variables)
        List<IdentifierNormalizer.RenamingEntry> renamings = identifierNormalizer.normalizeIdentifiers(cleanTree);

        // 3. Literal normalization (exact by default, or abstracted if option selected)
        literalNormalizer.normalizeLiterals(cleanTree, abstractLiterals);

        return new NormalizationResult(cleanTree, renamings, true, abstractLiterals);
    }
}
