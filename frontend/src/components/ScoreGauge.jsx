import React from 'react';
import { Award, GitCommit, Layers, FileCode2, Info } from 'lucide-react';

export default function ScoreGauge({ similarityScore, treeEditDistance, nodeCountA, nodeCountB, matchedCount }) {
  // Score color grading
  const getScoreColor = (score) => {
    if (score >= 90) return 'text-emerald-400 border-emerald-500/50 bg-emerald-500/10 shadow-emerald-500/20';
    if (score >= 70) return 'text-sky-400 border-sky-500/50 bg-sky-500/10 shadow-sky-500/20';
    if (score >= 40) return 'text-amber-400 border-amber-500/50 bg-amber-500/10 shadow-amber-500/20';
    return 'text-rose-400 border-rose-500/50 bg-rose-500/10 shadow-rose-500/20';
  };

  const getScoreLabel = (score) => {
    if (score >= 95) return 'Strong Structural Identity';
    if (score >= 70) return 'Moderate Structural Correspondence';
    if (score >= 40) return 'Partial Syntactic Overlap';
    return 'Structurally Distinct';
  };

  const ringCircumference = 2 * Math.PI * 54;
  const strokeDashoffset = ringCircumference - (similarityScore / 100) * ringCircumference;

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl relative overflow-hidden">
      {/* Background glow */}
      <div className="absolute top-0 right-0 -mt-10 -mr-10 w-48 h-48 bg-sky-500/5 rounded-full blur-3xl pointer-events-none"></div>

      <div className="grid grid-cols-1 md:grid-cols-12 gap-6 items-center">
        {/* Radial Gauge */}
        <div className="md:col-span-5 flex flex-col items-center justify-center">
          <div className="relative w-40 h-40 flex items-center justify-center">
            <svg className="w-full h-full transform -rotate-90" viewBox="0 0 120 120">
              <circle
                cx="60"
                cy="60"
                r="54"
                className="text-slate-800"
                strokeWidth="10"
                stroke="currentColor"
                fill="transparent"
              />
              <circle
                cx="60"
                cy="60"
                r="54"
                className="text-sky-500 transition-all duration-1000 ease-out"
                strokeWidth="10"
                strokeDasharray={ringCircumference}
                strokeDashoffset={strokeDashoffset}
                strokeLinecap="round"
                stroke="currentColor"
                fill="transparent"
              />
            </svg>
            <div className="absolute flex flex-col items-center justify-center text-center">
              <span className="text-3xl font-extrabold tracking-tight text-white font-mono">
                {similarityScore.toFixed(2)}%
              </span>
              <span className="text-[11px] font-medium text-slate-400 uppercase tracking-wider mt-0.5">
                Similarity
              </span>
            </div>
          </div>

          <div className="mt-3 text-center">
            <span className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold border ${getScoreColor(similarityScore)}`}>
              <Award className="w-3.5 h-3.5 mr-1.5" />
              {getScoreLabel(similarityScore)}
            </span>
          </div>
        </div>

        {/* Metrics Grid */}
        <div className="md:col-span-7 grid grid-cols-2 gap-4">
          <div className="bg-slate-800/60 border border-slate-700/60 rounded-xl p-4">
            <div className="flex items-center text-slate-400 text-xs font-medium mb-1">
              <GitCommit className="w-3.5 h-3.5 mr-1.5 text-sky-400" />
              Tree-Edit Distance
            </div>
            <div className="text-2xl font-bold font-mono text-white">
              {treeEditDistance.toFixed(2)}
            </div>
            <p className="text-[11px] text-slate-400 mt-0.5">Zhang-Shasha min edit cost</p>
          </div>

          <div className="bg-slate-800/60 border border-slate-700/60 rounded-xl p-4">
            <div className="flex items-center text-slate-400 text-xs font-medium mb-1">
              <Layers className="w-3.5 h-3.5 mr-1.5 text-emerald-400" />
              Matched Nodes
            </div>
            <div className="text-2xl font-bold font-mono text-emerald-400">
              {matchedCount}
            </div>
            <p className="text-[11px] text-slate-400 mt-0.5">Aligned AST constructs</p>
          </div>

          <div className="bg-slate-800/60 border border-slate-700/60 rounded-xl p-4">
            <div className="flex items-center text-slate-400 text-xs font-medium mb-1">
              <FileCode2 className="w-3.5 h-3.5 mr-1.5 text-indigo-400" />
              AST Size (Program A)
            </div>
            <div className="text-xl font-bold font-mono text-white">
              {nodeCountA} <span className="text-xs font-normal text-slate-400">nodes</span>
            </div>
            <p className="text-[11px] text-slate-400 mt-0.5">Normalized tree size |T_A|</p>
          </div>

          <div className="bg-slate-800/60 border border-slate-700/60 rounded-xl p-4">
            <div className="flex items-center text-slate-400 text-xs font-medium mb-1">
              <FileCode2 className="w-3.5 h-3.5 mr-1.5 text-violet-400" />
              AST Size (Program B)
            </div>
            <div className="text-xl font-bold font-mono text-white">
              {nodeCountB} <span className="text-xs font-normal text-slate-400">nodes</span>
            </div>
            <p className="text-[11px] text-slate-400 mt-0.5">Normalized tree size |T_B|</p>
          </div>
        </div>
      </div>

      {/* IEEE Disclaimer footer */}
      <div className="mt-5 pt-4 border-t border-slate-800/80 flex items-start space-x-2 text-xs text-slate-400">
        <Info className="w-4 h-4 text-sky-400 flex-shrink-0 mt-0.5" />
        <p>
          <strong className="text-slate-300">Analysis Note:</strong> Final similarity percentage is computed strictly via{' '}
          <code className="text-sky-300 bg-slate-800 px-1 py-0.5 rounded text-[11px]">S = 100 × (1 − D / (|T_A| + |T_B|))</code>.
          This measures abstract syntactic structural alignment; it does not constitute legal proof of plagiarism or runtime behavioral identity.
        </p>
      </div>
    </div>
  );
}
