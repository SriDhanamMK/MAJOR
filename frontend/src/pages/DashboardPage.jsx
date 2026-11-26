import React, { useState } from 'react';
import ScoreGauge from '../components/ScoreGauge';
import { Download, Printer, CheckCircle, AlertCircle, RefreshCw, Calculator, FileSpreadsheet, FileJson, ArrowRight } from 'lucide-react';
import { getJsonReportUrl, getCsvReportUrl } from '../services/api';

export default function DashboardPage({ results, onViewAst }) {
  const [matchFilter, setMatchFilter] = useState('ALL');

  if (!results) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-20 text-center space-y-4">
        <h3 className="text-xl font-bold text-white">No Analysis Results Available</h3>
        <p className="text-sm text-slate-400">Run a source code comparison in the Workspace to view results here.</p>
      </div>
    );
  }

  const {
    comparisonId,
    language,
    similarityScore,
    treeEditDistance,
    nodeCountA,
    nodeCountB,
    matchedNodeCount,
    insertedNodeCount,
    deletedNodeCount,
    substitutedNodeCount,
    transformations,
    scoreExplanation,
    matches,
    algorithmVersion,
    timestamp
  } = results;

  const filteredMatches = (matches || []).filter((m) => {
    if (matchFilter === 'ALL') return true;
    return m.status === matchFilter;
  });

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 py-6 space-y-8">
      {/* Top Banner & Export Actions */}
      <div className="flex flex-wrap items-center justify-between gap-4 bg-slate-900 border border-slate-800 rounded-2xl p-4 shadow-lg">
        <div>
          <div className="flex items-center space-x-2">
            <span className="text-xs font-mono font-bold text-sky-400 uppercase tracking-wider">Report ID:</span>
            <span className="text-xs font-mono text-slate-300">{comparisonId ? comparisonId.substring(0, 18) + '...' : 'local'}</span>
            <span className="text-xs px-2 py-0.5 rounded-full bg-slate-800 text-slate-300 border border-slate-700">
              {language}
            </span>
          </div>
          <span className="text-[11px] text-slate-500">{timestamp || new Date().toLocaleString()}</span>
        </div>

        {/* Action buttons */}
        <div className="flex items-center space-x-2">
          <button
            onClick={onViewAst}
            className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 border border-slate-700 text-xs font-bold text-white flex items-center space-x-1.5 transition-colors"
          >
            <span>Inspect Trees</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </button>

          {comparisonId && (
            <>
              <a
                href={getJsonReportUrl(comparisonId)}
                download
                className="px-3.5 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 border border-slate-700 text-xs font-semibold text-slate-300 hover:text-white flex items-center space-x-1.5 transition-colors"
              >
                <FileJson className="w-4 h-4 text-sky-400" />
                <span>JSON Report</span>
              </a>

              <a
                href={getCsvReportUrl(comparisonId)}
                download
                className="px-3.5 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 border border-slate-700 text-xs font-semibold text-slate-300 hover:text-white flex items-center space-x-1.5 transition-colors"
              >
                <FileSpreadsheet className="w-4 h-4 text-emerald-400" />
                <span>CSV Summary</span>
              </a>
            </>
          )}

          <button
            onClick={() => window.print()}
            className="p-2 rounded-xl bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-300 hover:text-white transition-colors"
            title="Print or Save as PDF"
          >
            <Printer className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Primary Score Gauge & Metrics */}
      <ScoreGauge
        similarityScore={similarityScore}
        treeEditDistance={treeEditDistance}
        nodeCountA={nodeCountA}
        nodeCountB={nodeCountB}
        matchedCount={matchedNodeCount}
      />

      {/* Mathematical Calculation Explainer */}
      {scoreExplanation && (
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-4">
          <div className="flex items-center space-x-2">
            <Calculator className="w-5 h-5 text-indigo-400" />
            <h3 className="font-bold text-white">Mathematical Score Derivation</h3>
          </div>

          <div className="bg-slate-950 border border-slate-800 rounded-xl p-4 font-mono text-xs text-sky-300">
            {scoreExplanation.formula}
          </div>

          <div className="space-y-2 text-xs">
            {scoreExplanation.calculationSteps && scoreExplanation.calculationSteps.map((step, idx) => (
              <div key={idx} className="flex items-start space-x-2 text-slate-300">
                <span className="text-slate-500 font-mono">•</span>
                <p className="font-mono">{step}</p>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Transformation Summary: Identifier Renaming & Reordering */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Identifier Canonicalization Report */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-4">
          <h4 className="font-bold text-white text-sm flex items-center justify-between">
            <span>Identifier Normalization</span>
            <span className="text-xs font-mono text-slate-400 font-normal">
              {(transformations?.identifierRenamingsA?.length || 0) + (transformations?.identifierRenamingsB?.length || 0)} mapped
            </span>
          </h4>

          <div className="space-y-3 max-h-60 overflow-y-auto pr-2">
            <div>
              <span className="text-[11px] font-semibold text-slate-400 block mb-1">Program A Local Variables:</span>
              {transformations?.identifierRenamingsA?.length > 0 ? (
                <ul className="space-y-1 font-mono text-xs text-slate-300">
                  {transformations.identifierRenamingsA.map((r, i) => (
                    <li key={i} className="bg-slate-800/60 px-2 py-1 rounded border border-slate-700/50">{r}</li>
                  ))}
                </ul>
              ) : (
                <p className="text-xs text-slate-500 italic">No eligible local variables canonicalized.</p>
              )}
            </div>

            <div>
              <span className="text-[11px] font-semibold text-slate-400 block mb-1">Program B Local Variables:</span>
              {transformations?.identifierRenamingsB?.length > 0 ? (
                <ul className="space-y-1 font-mono text-xs text-slate-300">
                  {transformations.identifierRenamingsB.map((r, i) => (
                    <li key={i} className="bg-slate-800/60 px-2 py-1 rounded border border-slate-700/50">{r}</li>
                  ))}
                </ul>
              ) : (
                <p className="text-xs text-slate-500 italic">No eligible local variables canonicalized.</p>
              )}
            </div>
          </div>
        </div>

        {/* Statement Reordering Analysis */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-4">
          <h4 className="font-bold text-white text-sm flex items-center justify-between">
            <span>Statement Reordering Analysis</span>
            <span className="text-xs font-mono text-slate-400 font-normal">Hazard Checked</span>
          </h4>

          <div className="space-y-2 max-h-60 overflow-y-auto pr-2">
            {transformations?.statementReorderingsA?.length > 0 ? (
              transformations.statementReorderingsA.map((msg, i) => (
                <div key={i} className="p-2.5 rounded-xl bg-slate-800/60 border border-slate-700/50 text-xs text-slate-300">
                  {msg}
                </div>
              ))
            ) : (
              <p className="text-xs text-slate-500 italic py-4 text-center">
                No consecutive statement reordering permutations detected.
              </p>
            )}
          </div>
        </div>
      </div>

      {/* Alignment Details Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl space-y-4 p-6">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <h4 className="font-bold text-white text-base">AST Construct Alignment & Edit Operations</h4>
            <p className="text-xs text-slate-400">Detailed mapping of nodes between Source A and Source B.</p>
          </div>

          {/* Filter Pills */}
          <div className="bg-slate-800 p-1 rounded-xl border border-slate-700 flex text-xs">
            {['ALL', 'MATCHED', 'SUBSTITUTED', 'INSERTED', 'DELETED'].map((f) => (
              <button
                key={f}
                onClick={() => setMatchFilter(f)}
                className={`px-3 py-1 rounded-lg font-semibold transition-all ${
                  matchFilter === f ? 'bg-sky-500 text-white shadow' : 'text-slate-400 hover:text-slate-200'
                }`}
              >
                {f}
              </button>
            ))}
          </div>
        </div>

        {/* Table */}
        <div className="overflow-x-auto">
          <table className="w-full text-left font-mono text-xs">
            <thead className="bg-slate-800/80 text-slate-400 uppercase text-[10px]">
              <tr>
                <th className="py-2.5 px-3">Status</th>
                <th className="py-2.5 px-3">Node A</th>
                <th className="py-2.5 px-3">Node B</th>
                <th className="py-2.5 px-3">Edit Cost</th>
                <th className="py-2.5 px-3 font-sans">Explanation</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {filteredMatches.slice(0, 50).map((m, idx) => (
                <tr key={idx} className="hover:bg-slate-800/40">
                  <td className="py-2 px-3">
                    <span
                      className={`text-[10px] px-2 py-0.5 rounded font-bold ${
                        m.status === 'MATCHED'
                          ? 'bg-emerald-950 text-emerald-400 border border-emerald-800'
                          : m.status === 'SUBSTITUTED'
                          ? 'bg-amber-950 text-amber-400 border border-amber-800'
                          : m.status === 'INSERTED'
                          ? 'bg-sky-950 text-sky-400 border border-sky-800'
                          : 'bg-rose-950 text-rose-400 border border-rose-800'
                      }`}
                    >
                      {m.status}
                    </span>
                  </td>
                  <td className="py-2 px-3 text-slate-300">{m.typeA ? `${m.typeA} (${m.idA || '-'})` : '-'}</td>
                  <td className="py-2 px-3 text-slate-300">{m.typeB ? `${m.typeB} (${m.idB || '-'})` : '-'}</td>
                  <td className="py-2 px-3 font-bold text-white">{m.cost.toFixed(2)}</td>
                  <td className="py-2 px-3 font-sans text-slate-400">{m.detail}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {filteredMatches.length > 50 && (
            <p className="text-center py-3 text-xs text-slate-500 font-sans">
              Showing top 50 of {filteredMatches.length} alignments. Download full JSON report for complete dataset.
            </p>
          )}
        </div>
      </div>
    </div>
  );
}
