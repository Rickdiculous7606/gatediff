import React, { useState } from 'react';
import { Header } from './components/Header';
import { VisualDiffView } from './components/VisualDiffView';
import { TerminalView } from './components/TerminalView';
import { MockLabView } from './components/MockLabView';
import { OpenApiMapperView } from './components/OpenApiMapperView';
import { ConfigEditorView } from './components/ConfigEditorView';
import { JavaDocsView } from './components/JavaDocsView';

export default function App() {
  const [activeTab, setActiveTab] = useState<string>('diff');

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 font-sans selection:bg-cyan-500/30 selection:text-cyan-200">
      {/* Top Navigation Bar */}
      <Header activeTab={activeTab} setActiveTab={setActiveTab} testCount={38} />

      {/* Main Container */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        {activeTab === 'diff' && <VisualDiffView />}
        {activeTab === 'terminal' && <TerminalView />}
        {activeTab === 'mocklab' && <MockLabView />}
        {activeTab === 'mapper' && <OpenApiMapperView />}
        {activeTab === 'config' && <ConfigEditorView />}
        {activeTab === 'docs' && <JavaDocsView />}
      </main>

      {/* Footer */}
      <footer className="border-t border-slate-900 bg-slate-950 py-6 text-center text-xs text-slate-500 font-mono">
        <p>GateDiff — Open-Source Java API Gateway Migration Testing CLI & Lab &bull; Apache-2.0 License</p>
        <p className="mt-1 text-slate-600 font-sans">
          Built with Java 21, Maven, Picocli, Jackson, and JUnit 5 &bull; Dual Gateway Behavioral Equivalence Testing
        </p>
      </footer>
    </div>
  );
}
