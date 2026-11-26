import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import HomePage from './pages/HomePage';
import WorkspacePage from './pages/WorkspacePage';
import DashboardPage from './pages/DashboardPage';
import AstVisualizer from './components/AstVisualizer';
import MethodologyPage from './pages/MethodologyPage';
import { fetchHealth, comparePrograms } from './services/api';

export default function App() {
  const [activeTab, setActiveTab] = useState('home');
  const [language, setLanguage] = useState('C');
  const [sourceA, setSourceA] = useState(`int add(int a, int b) {
    return a + b;
}`);
  const [sourceB, setSourceB] = useState(`int sum(int x, int y) {
    return x + y;
}`);
  const [enableLiteralAbstraction, setEnableLiteralAbstraction] = useState(false);
  const [enableReordering, setEnableReordering] = useState(true);

  const [results, setResults] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [health, setHealth] = useState(null);

  useEffect(() => {
    async function checkBackend() {
      const h = await fetchHealth();
      setHealth(h);
    }
    checkBackend();
  }, []);

  const handleCompare = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await comparePrograms({
        language,
        sourceA,
        sourceB,
        enableLiteralAbstraction,
        enableReordering
      });

      if (data.warnings && data.warnings.length > 0 && data.similarityScore === 0.0) {
        if (data.diagnosticsA?.length > 0 || data.diagnosticsB?.length > 0) {
          setError(data.warnings.join(' '));
        }
      }

      setResults(data);
      if (!data.diagnosticsA?.length && !data.diagnosticsB?.length) {
        setActiveTab('dashboard');
      }
    } catch (err) {
      console.error('Comparison error:', err);
      setError(err.message || 'An error occurred during comparison');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col bg-slate-950 text-slate-100">
      <Navbar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        health={health}
        hasResults={!!results}
      />

      <main className="flex-1">
        {activeTab === 'home' && (
          <HomePage
            onStartComparing={() => setActiveTab('workspace')}
            health={health}
          />
        )}

        {activeTab === 'workspace' && (
          <WorkspacePage
            language={language}
            setLanguage={setLanguage}
            sourceA={sourceA}
            setSourceA={setSourceA}
            sourceB={sourceB}
            setSourceB={setSourceB}
            enableLiteralAbstraction={enableLiteralAbstraction}
            setEnableLiteralAbstraction={setEnableLiteralAbstraction}
            enableReordering={enableReordering}
            setEnableReordering={setEnableReordering}
            onCompare={handleCompare}
            isLoading={isLoading}
            error={error}
            diagnosticsA={results?.diagnosticsA}
            diagnosticsB={results?.diagnosticsB}
          />
        )}

        {activeTab === 'dashboard' && (
          <DashboardPage
            results={results}
            onViewAst={() => setActiveTab('visualizer')}
          />
        )}

        {activeTab === 'visualizer' && (
          <div className="max-w-7xl mx-auto px-4 sm:px-6 py-6">
            <AstVisualizer
              treeA={results?.treeA}
              treeB={results?.treeB}
            />
          </div>
        )}

        {activeTab === 'methodology' && <MethodologyPage />}
      </main>

      {/* Footer */}
      <footer className="border-t border-slate-900 bg-slate-950 py-6 text-center text-xs text-slate-500 font-mono no-print">
        <p>AST-Based Intelligent Source Code Similarity Detection Prototype • IEEE Format Architecture</p>
        <p className="text-[11px] text-slate-600 mt-1">Built with Java 17, Spring Boot, Tree-sitter Native Parsers, React, Vite & Tailwind CSS</p>
      </footer>
    </div>
  );
}
