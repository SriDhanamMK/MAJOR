import React from 'react';
import { Network, Activity, BookOpen, GitCompare, Home, CheckCircle2, AlertCircle } from 'lucide-react';

export default function Navbar({ activeTab, setActiveTab, health, hasResults }) {
  const isHealthy = health && health.status === 'UP';

  return (
    <header className="sticky top-0 z-50 bg-slate-900/90 backdrop-blur-md border-b border-slate-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo & Project Title */}
          <div className="flex items-center space-x-3 cursor-pointer" onClick={() => setActiveTab('home')}>
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-sky-500 to-indigo-600 flex items-center justify-center shadow-lg shadow-sky-500/20 ring-1 ring-white/20">
              <Network className="w-6 h-6 text-white" />
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <span className="font-bold text-lg text-slate-100 tracking-tight">AST Similarity</span>
                <span className="text-[10px] font-semibold tracking-wider uppercase px-2 py-0.5 rounded-full bg-sky-950/80 text-sky-400 border border-sky-800/60">
                  IEEE Prototype
                </span>
              </div>
              <p className="text-xs text-slate-400 hidden sm:block">Structural Normalization & Tree-Edit Distance</p>
            </div>
          </div>

          {/* Navigation Links */}
          <nav className="flex items-center space-x-1 sm:space-x-2">
            <button
              onClick={() => setActiveTab('home')}
              className={`flex items-center space-x-2 px-3 py-2 rounded-lg text-sm font-medium transition-all ${
                activeTab === 'home'
                  ? 'bg-slate-800 text-sky-400 shadow-sm border border-slate-700'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
              }`}
            >
              <Home className="w-4 h-4" />
              <span>Home</span>
            </button>

            <button
              onClick={() => setActiveTab('workspace')}
              className={`flex items-center space-x-2 px-3 py-2 rounded-lg text-sm font-medium transition-all ${
                activeTab === 'workspace'
                  ? 'bg-slate-800 text-sky-400 shadow-sm border border-slate-700'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
              }`}
            >
              <GitCompare className="w-4 h-4" />
              <span>Workspace</span>
            </button>

            {hasResults && (
              <>
                <button
                  onClick={() => setActiveTab('dashboard')}
                  className={`flex items-center space-x-2 px-3 py-2 rounded-lg text-sm font-medium transition-all ${
                    activeTab === 'dashboard'
                      ? 'bg-slate-800 text-sky-400 shadow-sm border border-slate-700'
                      : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
                  }`}
                >
                  <Activity className="w-4 h-4" />
                  <span>Results</span>
                </button>

                <button
                  onClick={() => setActiveTab('visualizer')}
                  className={`flex items-center space-x-2 px-3 py-2 rounded-lg text-sm font-medium transition-all ${
                    activeTab === 'visualizer'
                      ? 'bg-slate-800 text-sky-400 shadow-sm border border-slate-700'
                      : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
                  }`}
                >
                  <Network className="w-4 h-4" />
                  <span>AST Visualizer</span>
                </button>
              </>
            )}

            <button
              onClick={() => setActiveTab('methodology')}
              className={`flex items-center space-x-2 px-3 py-2 rounded-lg text-sm font-medium transition-all ${
                activeTab === 'methodology'
                  ? 'bg-slate-800 text-sky-400 shadow-sm border border-slate-700'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
              }`}
            >
              <BookOpen className="w-4 h-4" />
              <span>Methodology</span>
            </button>
          </nav>

          {/* Backend Status Pill */}
          <div className="flex items-center space-x-3">
            <div
              className={`flex items-center space-x-2 px-3 py-1.5 rounded-full text-xs font-medium border ${
                isHealthy
                  ? 'bg-emerald-950/40 text-emerald-400 border-emerald-800/50'
                  : 'bg-rose-950/40 text-rose-400 border-rose-800/50'
              }`}
              title={isHealthy ? 'Backend & Genuine Parsers Connected' : 'Backend connection unavailable'}
            >
              <span className="relative flex h-2 w-2">
                {isHealthy && (
                  <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
                )}
                <span
                  className={`relative inline-flex rounded-full h-2 w-2 ${
                    isHealthy ? 'bg-emerald-500' : 'bg-rose-500'
                  }`}
                ></span>
              </span>
              <span className="hidden sm:inline">
                {isHealthy ? 'Parsers: C, C++, Python' : 'Backend Offline'}
              </span>
            </div>
          </div>
        </div>
      </div>
    </header>
  );
}
