import React from 'react';
import { ArrowRight, Code2, Cpu, ShieldAlert, Sparkles, CheckCircle2, GitMerge, Zap, Scale } from 'lucide-react';

export default function HomePage({ onStartComparing, health }) {
  const isHealthy = health && health.status === 'UP';

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 py-12 space-y-16">
      {/* Hero Section */}
      <div className="text-center space-y-6 pt-4">
        <div className="inline-flex items-center space-x-2 px-3 py-1.5 rounded-full bg-sky-950/80 border border-sky-800/80 text-sky-400 text-xs font-semibold tracking-wide">
          <Sparkles className="w-3.5 h-3.5" />
          <span>IEEE Research Prototype • Tree-sitter AST Engine</span>
        </div>

        <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold text-white tracking-tight leading-tight max-w-4xl mx-auto">
          AST-Based Intelligent Source Code{' '}
          <span className="text-transparent bg-clip-text bg-gradient-to-r from-sky-400 via-indigo-400 to-purple-400">
            Similarity Detection
          </span>
        </h1>

        <p className="text-lg text-slate-300 max-w-2xl mx-auto leading-relaxed">
          Robust, transformation-aware source code comparison using genuine compiler syntax trees,
          lexical scope canonicalization, dataflow dependency verification, and exact Zhang-Shasha tree-edit distance.
        </p>

        {/* CTA and Quick Badges */}
        <div className="pt-4 flex flex-col sm:flex-row items-center justify-center gap-4">
          <button
            onClick={onStartComparing}
            className="w-full sm:w-auto px-8 py-3.5 rounded-xl bg-gradient-to-r from-sky-500 to-indigo-600 hover:from-sky-400 hover:to-indigo-500 text-white font-semibold shadow-lg shadow-sky-500/25 flex items-center justify-center space-x-2 transition-all transform hover:-translate-y-0.5"
          >
            <span>Start Comparing Code</span>
            <ArrowRight className="w-4 h-4" />
          </button>
        </div>

        {/* Supported Languages */}
        <div className="flex items-center justify-center gap-3 pt-4 text-xs font-mono">
          <span className="text-slate-400 font-sans">Supported Parsers:</span>
          <span className="px-2.5 py-1 rounded-md bg-slate-800 border border-slate-700 text-sky-400 font-semibold">C (Tree-sitter)</span>
          <span className="px-2.5 py-1 rounded-md bg-slate-800 border border-slate-700 text-indigo-400 font-semibold">C++ (Tree-sitter)</span>
          <span className="px-2.5 py-1 rounded-md bg-slate-800 border border-slate-700 text-emerald-400 font-semibold">Python (Tree-sitter)</span>
        </div>
      </div>

      {/* Problem & Solution Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-6 space-y-3">
          <div className="w-10 h-10 rounded-xl bg-rose-500/10 border border-rose-500/20 flex items-center justify-center text-rose-400">
            <ShieldAlert className="w-5 h-5" />
          </div>
          <h3 className="text-xl font-bold text-white">The Problem</h3>
          <p className="text-sm text-slate-400 leading-relaxed">
            Conventional plagiarism and similarity detectors rely heavily on token-stream n-grams or textual diffs.
            Trivial code transformations—such as variable renaming, whitespace modification, comment insertions,
            and statement permutations—easily evade token matching or cause false positives.
          </p>
        </div>

        <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-6 space-y-3">
          <div className="w-10 h-10 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400">
            <CheckCircle2 className="w-5 h-5" />
          </div>
          <h3 className="text-xl font-bold text-white">The Solution</h3>
          <p className="text-sm text-slate-400 leading-relaxed">
            We parse programs into genuine compiler Abstract Syntax Trees (ASTs) using Tree-sitter grammars.
            We canonicalize local identifiers within lexical scopes, filter concrete layout formatting, verify
            statement independence via dataflow hazard analysis (RAW/WAR/WAW), and compute exact Tree-Edit Distance.
          </p>
        </div>
      </div>

      {/* How the System Works (Pipeline) */}
      <div className="bg-slate-900/50 border border-slate-800 rounded-2xl p-8 space-y-6">
        <div className="text-center max-w-xl mx-auto space-y-2">
          <h2 className="text-2xl font-bold text-white">Five-Stage Research Pipeline</h2>
          <p className="text-sm text-slate-400">From raw untrusted source code to mathematically sound similarity score.</p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
          <div className="bg-slate-800/60 border border-slate-700/60 rounded-xl p-4 text-center space-y-2">
            <div className="text-xs font-mono font-bold text-sky-400">01. PARSE</div>
            <h4 className="font-semibold text-white text-sm">Tree-sitter AST</h4>
            <p className="text-xs text-slate-400">Extracts complete concrete syntax tree & detects syntax errors.</p>
          </div>

          <div className="bg-slate-800/60 border border-slate-700/60 rounded-xl p-4 text-center space-y-2">
            <div className="text-xs font-mono font-bold text-indigo-400">02. COMMON AST</div>
            <h4 className="font-semibold text-white text-sm">Unified Model</h4>
            <p className="text-xs text-slate-400">Maps grammar nodes into standardized semantic AST categories.</p>
          </div>

          <div className="bg-slate-800/60 border border-slate-700/60 rounded-xl p-4 text-center space-y-2">
            <div className="text-xs font-mono font-bold text-purple-400">03. NORMALIZE</div>
            <h4 className="font-semibold text-white text-sm">Scope Canonical</h4>
            <p className="text-xs text-slate-400">Canonicalizes parameters and local variables; abstracts formatting.</p>
          </div>

          <div className="bg-slate-800/60 border border-slate-700/60 rounded-xl p-4 text-center space-y-2">
            <div className="text-xs font-mono font-bold text-emerald-400">04. DATAFLOW</div>
            <h4 className="font-semibold text-white text-sm">Hazard Checking</h4>
            <p className="text-xs text-slate-400">Validates independence before allowing conservative statement reordering.</p>
          </div>

          <div className="bg-slate-800/60 border border-slate-700/60 rounded-xl p-4 text-center space-y-2">
            <div className="text-xs font-mono font-bold text-amber-400">05. DISTANCE</div>
            <h4 className="font-semibold text-white text-sm">Zhang-Shasha TED</h4>
            <p className="text-xs text-slate-400">Calculates minimum edit cost and exact percentage formula.</p>
          </div>
        </div>
      </div>

      {/* Limitations and IEEE Ethical Stance */}
      <div className="bg-amber-950/20 border border-amber-800/40 rounded-2xl p-6 space-y-3">
        <div className="flex items-center space-x-2 text-amber-400">
          <Scale className="w-5 h-5" />
          <h3 className="font-bold text-white">Scientific Scope & Limitations</h3>
        </div>
        <p className="text-sm text-slate-300 leading-relaxed">
          This system assesses <strong>structural source-code similarity</strong> based on syntax trees.
          In accordance with IEEE academic integrity guidelines, a similarity score is <strong>not proof of plagiarism</strong>,
          authorship attribution, or semantic behavioral equivalence. Independent human review of contextual factors
          remains essential.
        </p>
      </div>
    </div>
  );
}
