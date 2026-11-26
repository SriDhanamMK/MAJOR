import React from 'react';
import { BookOpen, Layers, GitMerge, Calculator, ShieldCheck, Scale, CheckCircle2 } from 'lucide-react';

export default function MethodologyPage() {
  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 py-10 space-y-12">
      {/* Page Title */}
      <div className="space-y-3">
        <div className="inline-flex items-center space-x-2 text-sky-400 text-xs font-semibold uppercase tracking-wider">
          <BookOpen className="w-4 h-4" />
          <span>IEEE Research Architecture & Methodology</span>
        </div>
        <h1 className="text-3xl sm:text-4xl font-extrabold text-white tracking-tight">
          Structural Normalization & Transformation-Aware AST Analysis
        </h1>
        <p className="text-slate-400 text-base leading-relaxed">
          Comprehensive documentation of the compiler parsing pipeline, lexical scope canonicalization,
          dataflow independence verification, and exact tree-edit distance scoring.
        </p>
      </div>

      {/* Pipeline Diagram */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 sm:p-8 space-y-6">
        <h3 className="text-xl font-bold text-white flex items-center space-x-2">
          <Layers className="w-5 h-5 text-sky-400" />
          <span>End-to-End Analysis Pipeline</span>
        </h3>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs font-mono">
          <div className="p-4 rounded-xl bg-slate-950 border border-slate-800 space-y-2">
            <span className="text-sky-400 font-bold block">STAGE 1: Concrete Parsing</span>
            <p className="text-slate-400 font-sans">
              Source code in C, C++, or Python is parsed using genuine Tree-sitter native grammars.
              Malformed programs with syntax errors are identified at exact line/col offsets and halted with diagnostics.
            </p>
          </div>

          <div className="p-4 rounded-xl bg-slate-950 border border-slate-800 space-y-2">
            <span className="text-indigo-400 font-bold block">STAGE 2: Common AST Model</span>
            <p className="text-slate-400 font-sans">
              Grammar-specific nodes are mapped into a unified AST representation with standard semantic categories:
              Program, FunctionDecl, VarDecl, Block, ControlFlow, Statement, Expression, Identifier, and Literal.
            </p>
          </div>

          <div className="p-4 rounded-xl bg-slate-950 border border-slate-800 space-y-2">
            <span className="text-purple-400 font-bold block">STAGE 3: Structural Normalization</span>
            <p className="text-slate-400 font-sans">
              Removes concrete formatting delimiters (comments, braces, semicolons).
              Canonicalizes function parameters and local variables into scope-indexed symbols (<code>param_0</code>, <code>var_0</code>).
              Preserves API/function names and literals by default.
            </p>
          </div>

          <div className="p-4 rounded-xl bg-slate-950 border border-slate-800 space-y-2">
            <span className="text-emerald-400 font-bold block">STAGE 4: Dataflow Independence</span>
            <p className="text-slate-400 font-sans">
              Analyzes consecutive statement sequences. Verifies absence of Read-After-Write (RAW),
              Write-After-Read (WAR), Write-After-Write (WAW) hazards and side effects before permitting canonical reordering.
            </p>
          </div>
        </div>
      </div>

      {/* The Scoring Formula */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 sm:p-8 space-y-6">
        <h3 className="text-xl font-bold text-white flex items-center space-x-2">
          <Calculator className="w-5 h-5 text-indigo-400" />
          <span>The Exact Scoring Algorithm</span>
        </h3>

        <div className="bg-slate-950 border border-slate-800 rounded-xl p-5 text-center">
          <p className="text-slate-400 text-xs uppercase tracking-wider mb-2 font-mono">Similarity Equation</p>
          <div className="text-xl sm:text-2xl font-mono font-bold text-sky-400">
            S(P, Q) = 100 × ( 1 − D(P, Q) / ( |T'_P| + |T'_Q| ) )
          </div>
        </div>

        <div className="space-y-3 text-sm text-slate-300 leading-relaxed">
          <p>
            Where:
          </p>
          <ul className="list-disc list-inside space-y-1.5 text-xs font-mono text-slate-400">
            <li><strong className="text-slate-200">D(P, Q)</strong>: Minimum tree-edit distance calculated via the Zhang-Shasha dynamic programming algorithm.</li>
            <li><strong className="text-slate-200">|T'_P|, |T'_Q|</strong>: Total post-order node counts of the normalized ASTs.</li>
            <li><strong className="text-slate-200">|T'_P| + |T'_Q|</strong>: Safe theoretical upper bound on distance (deleting all nodes of P and inserting all nodes of Q).</li>
          </ul>

          <h4 className="font-bold text-white pt-3 text-xs uppercase tracking-wider font-mono">Documented Edit Cost Matrix:</h4>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 text-xs font-mono pt-1">
            <div className="bg-slate-950 p-3 rounded-lg border border-slate-800">
              <span className="text-rose-400 font-bold block">Insertion Cost</span>
              <span className="text-slate-400 font-mono text-sm">1.0</span>
            </div>
            <div className="bg-slate-950 p-3 rounded-lg border border-slate-800">
              <span className="text-rose-400 font-bold block">Deletion Cost</span>
              <span className="text-slate-400 font-mono text-sm">1.0</span>
            </div>
            <div className="bg-slate-950 p-3 rounded-lg border border-slate-800">
              <span className="text-amber-400 font-bold block">Substitution Cost</span>
              <span className="text-slate-400 text-[11px] block mt-0.5">0.0 (identical normalized), 0.4 (operators/literals), 0.6 (same category), 1.0 (different)</span>
            </div>
          </div>
        </div>
      </div>

      {/* Semantic Equivalence & Ethics */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 sm:p-8 space-y-4">
        <h3 className="text-xl font-bold text-white flex items-center space-x-2">
          <Scale className="w-5 h-5 text-amber-400" />
          <span>Distinguishing Syntactic Similarity from Semantic Equivalence</span>
        </h3>

        <p className="text-sm text-slate-300 leading-relaxed">
          It is a fundamental result in theoretical computer science (Rice's Theorem) that semantic equivalence
          of arbitrary programs is undecidable. Therefore, our system specifically and exclusively assesses
          <strong> abstract syntactic structural similarity</strong>.
        </p>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs pt-2">
          <div className="p-4 rounded-xl bg-slate-950 border border-slate-800 space-y-1">
            <span className="text-emerald-400 font-bold block">What This Score Indicates:</span>
            <p className="text-slate-400">
              The degree of shared syntactic grammar structure, normalized lexical bindings, and aligned statement sequences.
            </p>
          </div>
          <div className="p-4 rounded-xl bg-slate-950 border border-slate-800 space-y-1">
            <span className="text-rose-400 font-bold block">What This Score Does NOT Prove:</span>
            <p className="text-slate-400">
              It does not prove identical runtime behavior, intentional code plagiarism, or copyright infringement.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
