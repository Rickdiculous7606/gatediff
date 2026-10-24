import React from 'react';
import { Terminal, SplitSquareVertical, Server, FileCode, CheckCircle2, BookOpen } from 'lucide-react';

interface HeaderProps {
  activeTab: string;
  setActiveTab: (tab: string) => void;
  testCount: number;
}

export const Header: React.FC<HeaderProps> = ({ activeTab, setActiveTab, testCount }) => {
  const tabs = [
    { id: 'diff', label: 'Visual Diff Inspector', icon: SplitSquareVertical },
    { id: 'terminal', label: 'CLI Terminal', icon: Terminal },
    { id: 'mocklab', label: 'Mock Gateway Lab', icon: Server },
    { id: 'mapper', label: 'OpenAPI & Mapping', icon: FileCode },
    { id: 'config', label: 'YAML Config Studio', icon: FileCode },
    { id: 'docs', label: 'Java Code & Windows Setup', icon: BookOpen },
  ];

  return (
    <header className="border-b border-slate-800 bg-slate-950/80 backdrop-blur sticky top-0 z-30">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <div className="flex items-center gap-3">
            <div className="h-9 w-9 rounded-lg bg-gradient-to-tr from-cyan-600 to-blue-500 flex items-center justify-center shadow-lg shadow-cyan-500/20 text-white font-black text-lg">
              GD
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="font-bold text-slate-100 tracking-tight text-lg">GateDiff</span>
                <span className="bg-cyan-500/10 text-cyan-400 border border-cyan-500/30 text-xs px-2 py-0.5 rounded-full font-mono font-medium">
                  v1.0.0 (Java 21)
                </span>
              </div>
              <p className="text-xs text-slate-400 hidden sm:block">
                API Gateway Migration Validation CLI & Lab (Kong &rarr; Gravitee)
              </p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-emerald-950/40 border border-emerald-500/30 text-emerald-400 text-xs font-mono font-medium">
              <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
              <span>{testCount}/38 JUnit Tests Passed</span>
            </div>
          </div>
        </div>

        {/* Tab Navigation */}
        <nav className="flex space-x-1 sm:space-x-2 overflow-x-auto py-2 border-t border-slate-800/60 no-scrollbar">
          {tabs.map((tab) => {
            const Icon = tab.icon;
            const isActive = activeTab === tab.id;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`flex items-center gap-2 px-3 py-1.5 text-xs sm:text-sm font-medium rounded-lg whitespace-nowrap transition-all ${
                  isActive
                    ? 'bg-cyan-500/15 text-cyan-300 border border-cyan-500/40 shadow-sm'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-slate-900 border border-transparent'
                }`}
              >
                <Icon className={`w-4 h-4 ${isActive ? 'text-cyan-400' : 'text-slate-500'}`} />
                {tab.label}
              </button>
            );
          })}
        </nav>
      </div>
    </header>
  );
};
