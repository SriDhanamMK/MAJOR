import React, { useState } from 'react';
import Editor from '@monaco-editor/react';
import { Play, ArrowLeftRight, Trash2, Sliders, AlertTriangle, Sparkles, FileCode } from 'lucide-react';
import { EXAMPLE_PAIRS } from '../utils/examples';

export default function WorkspacePage({
  language,
  setLanguage,
  sourceA,
  setSourceA,
  sourceB,
  setSourceB,
  enableLiteralAbstraction,
  setEnableLiteralAbstraction,
  enableReordering,
  setEnableReordering,
  onCompare,
  isLoading,
  error,
  diagnosticsA,
  diagnosticsB
}) {
  const [selectedExampleId, setSelectedExampleId] = useState('');

  const currentExamples = EXAMPLE_PAIRS[language] || [];

  const handleLanguageChange = (newLang) => {
    setLanguage(newLang);
    setSelectedExampleId('');
  };

  const handleSelectExample = (e) => {
    const id = e.target.value;
    setSelectedExampleId(id);
    const pair = currentExamples.find((p) => p.id === id);
    if (pair) {
      setSourceA(pair.sourceA);
      setSourceB(pair.sourceB);
    }
  };

  const handleSwap = () => {
    const temp = sourceA;
    setSourceA(sourceB);
    setSourceB(temp);
  };

  const handleClearBoth = () => {
    setSourceA('');
    setSourceB('');
    setSelectedExampleId('');
  };

  // Map language identifier to Monaco editor language mode
  const getMonacoLang = (lang) => {
    if (lang === 'CPP') return 'cpp';
    if (lang === 'PYTHON') return 'python';
    return 'c';
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 py-6 space-y-6">
      {/* Top Controls Toolbar */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-4 shadow-lg flex flex-wrap items-center justify-between gap-4">
        {/* Left: Language Selector & Example Loader */}
        <div className="flex flex-wrap items-center gap-3">
          {/* Language Selector */}
          <div className="flex items-center space-x-2">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Language:</span>
            <div className="bg-slate-800 p-1 rounded-xl border border-slate-700 flex">
              {['C', 'CPP', 'PYTHON'].map((lang) => (
                <button
                  key={lang}
                  onClick={() => handleLanguageChange(lang)}
                  className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all ${
                    language === lang
                      ? 'bg-sky-500 text-white shadow'
                      : 'text-slate-400 hover:text-slate-200'
                  }`}
                >
                  {lang === 'CPP' ? 'C++' : lang}
                </button>
              ))}
            </div>
          </div>

          {/* Example Pair Dropdown */}
          <div className="flex items-center space-x-2">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Example:</span>
            <select
              value={selectedExampleId}
              onChange={handleSelectExample}
              className="bg-slate-800 text-xs text-slate-200 border border-slate-700 rounded-xl px-3 py-2 font-medium focus:outline-none focus:ring-1 focus:ring-sky-500"
            >
              <option value="">Load an example benchmark...</option>
              {currentExamples.map((ex) => (
                <option key={ex.id} value={ex.id}>
                  {ex.name}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Right: Actions (Swap, Clear, Compare) */}
        <div className="flex items-center space-x-2 ml-auto">
          <button
            onClick={handleSwap}
            title="Swap Source A and Source B"
            className="p-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-300 hover:text-white transition-colors"
          >
            <ArrowLeftRight className="w-4 h-4" />
          </button>

          <button
            onClick={handleClearBoth}
            title="Clear Both Editors"
            className="p-2.5 rounded-xl bg-slate-800 hover:bg-rose-950/60 border border-slate-700 hover:border-rose-800 text-slate-400 hover:text-rose-400 transition-colors"
          >
            <Trash2 className="w-4 h-4" />
          </button>

          <button
            onClick={onCompare}
            disabled={isLoading || !sourceA.trim() || !sourceB.trim()}
            className={`px-6 py-2.5 rounded-xl text-sm font-bold flex items-center space-x-2 shadow-lg transition-all ${
              isLoading || !sourceA.trim() || !sourceB.trim()
                ? 'bg-slate-800 text-slate-600 border border-slate-700 cursor-not-allowed'
                : 'bg-gradient-to-r from-sky-500 to-indigo-600 hover:from-sky-400 hover:to-indigo-500 text-white shadow-sky-500/20 transform hover:-translate-y-0.5'
            }`}
          >
            {isLoading ? (
              <>
                <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"></path>
                </svg>
                <span>Analyzing ASTs...</span>
              </>
            ) : (
              <>
                <Play className="w-4 h-4 fill-current" />
                <span>Compare ASTs</span>
              </>
            )}
          </button>
        </div>
      </div>

      {/* Advanced Analysis Options */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-3 flex flex-wrap items-center justify-between text-xs text-slate-400 gap-4">
        <div className="flex items-center space-x-6">
          <label className="flex items-center space-x-2 cursor-pointer select-none">
            <input
              type="checkbox"
              checked={enableReordering}
              onChange={(e) => setEnableReordering(e.target.checked)}
              className="rounded bg-slate-800 border-slate-700 text-sky-500 focus:ring-0 focus:ring-offset-0"
            />
            <span className="text-slate-300 font-medium">Conservative Statement Reordering Analysis</span>
            <span className="text-[10px] text-slate-500 hidden sm:inline">(Requires dataflow hazard verification)</span>
          </label>

          <label className="flex items-center space-x-2 cursor-pointer select-none">
            <input
              type="checkbox"
              checked={enableLiteralAbstraction}
              onChange={(e) => setEnableLiteralAbstraction(e.target.checked)}
              className="rounded bg-slate-800 border-slate-700 text-sky-500 focus:ring-0 focus:ring-offset-0"
            />
            <span className="text-slate-300 font-medium">Abstract Constant Literals</span>
            <span className="text-[10px] text-slate-500 hidden sm:inline">(Default preserves exact literal values)</span>
          </label>
        </div>

        <div className="text-[11px] text-slate-500 font-mono">
          Algorithm: Zhang-Shasha Ordered TED
        </div>
      </div>

      {/* Error or Syntax Diagnostics Alert */}
      {error && (
        <div className="bg-rose-950/40 border border-rose-800/80 rounded-xl p-4 text-xs text-rose-300 flex items-start space-x-3">
          <AlertTriangle className="w-5 h-5 text-rose-400 flex-shrink-0 mt-0.5" />
          <div className="space-y-1">
            <h5 className="font-bold text-rose-200">Comparison Failed</h5>
            <p>{error}</p>
          </div>
        </div>
      )}

      {/* Dual Monaco Editors Workspace */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Editor A */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl flex flex-col">
          <div className="px-4 py-3 border-b border-slate-800 bg-slate-900/80 flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <span className="w-2.5 h-2.5 rounded-full bg-sky-500"></span>
              <h4 className="font-bold text-sm text-white">Source Code A</h4>
              <span className="text-[11px] font-mono text-slate-500">({language})</span>
            </div>
            <button
              onClick={() => setSourceA('')}
              className="text-xs text-slate-400 hover:text-rose-400 transition-colors"
            >
              Clear
            </button>
          </div>

          <div className="h-[440px] w-full">
            <Editor
              height="100%"
              language={getMonacoLang(language)}
              value={sourceA}
              onChange={(val) => setSourceA(val || '')}
              theme="vs-dark"
              options={{
                minimap: { enabled: false },
                fontSize: 13,
                fontFamily: 'JetBrains Mono, monospace',
                scrollBeyondLastLine: false,
                wordWrap: 'on',
                lineNumbers: 'on',
                tabSize: 4,
                renderLineHighlight: 'all',
                automaticLayout: true
              }}
            />
          </div>

          {diagnosticsA && diagnosticsA.length > 0 && (
            <div className="p-3 bg-rose-950/50 border-t border-rose-900/50 text-xs font-mono text-rose-300">
              <span className="font-bold">Syntax Diagnostics (A):</span>
              <ul className="list-disc list-inside mt-1 space-y-0.5 text-[11px]">
                {diagnosticsA.map((d, i) => (
                  <li key={i}>L{d.line}:{d.column} - {d.message}</li>
                ))}
              </ul>
            </div>
          )}
        </div>

        {/* Editor B */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl flex flex-col">
          <div className="px-4 py-3 border-b border-slate-800 bg-slate-900/80 flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <span className="w-2.5 h-2.5 rounded-full bg-indigo-500"></span>
              <h4 className="font-bold text-sm text-white">Source Code B</h4>
              <span className="text-[11px] font-mono text-slate-500">({language})</span>
            </div>
            <button
              onClick={() => setSourceB('')}
              className="text-xs text-slate-400 hover:text-rose-400 transition-colors"
            >
              Clear
            </button>
          </div>

          <div className="h-[440px] w-full">
            <Editor
              height="100%"
              language={getMonacoLang(language)}
              value={sourceB}
              onChange={(val) => setSourceB(val || '')}
              theme="vs-dark"
              options={{
                minimap: { enabled: false },
                fontSize: 13,
                fontFamily: 'JetBrains Mono, monospace',
                scrollBeyondLastLine: false,
                wordWrap: 'on',
                lineNumbers: 'on',
                tabSize: 4,
                renderLineHighlight: 'all',
                automaticLayout: true
              }}
            />
          </div>

          {diagnosticsB && diagnosticsB.length > 0 && (
            <div className="p-3 bg-rose-950/50 border-t border-rose-900/50 text-xs font-mono text-rose-300">
              <span className="font-bold">Syntax Diagnostics (B):</span>
              <ul className="list-disc list-inside mt-1 space-y-0.5 text-[11px]">
                {diagnosticsB.map((d, i) => (
                  <li key={i}>L{d.line}:{d.column} - {d.message}</li>
                ))}
              </ul>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
