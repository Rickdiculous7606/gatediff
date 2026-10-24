import React, { useState } from 'react';
import { LAB_SCENARIOS, ScenarioConfig } from '../data/mockData';
import { Check, X, AlertTriangle, ArrowRight, ShieldCheck, Filter } from 'lucide-react';

export const VisualDiffView: React.FC = () => {
  const [selectedScenarioKey, setSelectedScenarioKey] = useState<string>('MATCHING');
  const [ignoreRequestId, setIgnoreRequestId] = useState<boolean>(true);
  const [ignoreTimestamp, setIgnoreTimestamp] = useState<boolean>(true);

  const scenario: ScenarioConfig = LAB_SCENARIOS[selectedScenarioKey] || LAB_SCENARIOS.MATCHING;

  // Compute active mismatches taking ignored paths into account
  const activeMismatches = scenario.expectedMismatches.filter((m) => {
    if (ignoreRequestId && m.path.includes('requestId')) return false;
    if (ignoreTimestamp && m.path.includes('timestamp')) return false;
    return true;
  });

  const isPassed = activeMismatches.length === 0 && scenario.sourceStatus === scenario.targetStatus;

  return (
    <div className="space-y-6">
      {/* Top Banner / Scenario Selector */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm">
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2">
              <span className="text-xs font-semibold uppercase tracking-wider text-cyan-400">
                Phase 1 & 3 Dual Execution Diff Engine
              </span>
            </div>
            <h2 className="text-xl font-bold text-white mt-1">
              Gateway Response Comparison: <span className="text-cyan-300">{scenario.name}</span>
            </h2>
            <p className="text-sm text-slate-400 mt-0.5">{scenario.description}</p>
          </div>

          {/* Quick Scenario Buttons */}
          <div className="flex flex-wrap items-center gap-1.5">
            {Object.keys(LAB_SCENARIOS).map((key) => {
              const sc = LAB_SCENARIOS[key];
              const isSelected = selectedScenarioKey === key;
              return (
                <button
                  key={key}
                  onClick={() => setSelectedScenarioKey(key)}
                  className={`text-xs px-2.5 py-1.5 rounded-md font-mono transition-colors ${
                    isSelected
                      ? 'bg-cyan-500 text-slate-950 font-bold shadow'
                      : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
                  }`}
                >
                  {sc.name.split(' ')[0]}
                </button>
              );
            })}
          </div>
        </div>

        {/* Comparison Engine Controls */}
        <div className="mt-4 pt-4 border-t border-slate-800/80 flex flex-wrap items-center justify-between gap-3 text-xs">
          <div className="flex items-center gap-4">
            <span className="text-slate-400 flex items-center gap-1 font-medium">
              <Filter className="w-3.5 h-3.5 text-cyan-400" /> Ignored JSON Paths:
            </span>
            <label className="flex items-center gap-1.5 cursor-pointer text-slate-300 hover:text-white">
              <input
                type="checkbox"
                checked={ignoreRequestId}
                onChange={(e) => setIgnoreRequestId(e.target.checked)}
                className="rounded border-slate-700 bg-slate-950 text-cyan-500 focus:ring-0"
              />
              <code className="text-cyan-300 font-mono">$.requestId</code>
            </label>
            <label className="flex items-center gap-1.5 cursor-pointer text-slate-300 hover:text-white">
              <input
                type="checkbox"
                checked={ignoreTimestamp}
                onChange={(e) => setIgnoreTimestamp(e.target.checked)}
                className="rounded border-slate-700 bg-slate-950 text-cyan-500 focus:ring-0"
              />
              <code className="text-cyan-300 font-mono">$.meta.timestamp</code>
            </label>
          </div>

          <div className="flex items-center gap-2">
            <span className="text-slate-400">Comparison Status:</span>
            {isPassed ? (
              <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
                <Check className="w-3.5 h-3.5" /> PASSED (Equivalence Verified)
              </span>
            ) : (
              <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-rose-500/10 text-rose-400 border border-rose-500/30">
                <X className="w-3.5 h-3.5" /> FAILED ({activeMismatches.length} Mismatch detected)
              </span>
            )}
          </div>
        </div>
      </div>

      {/* Actionable Mismatch Alert Box if failed */}
      {!isPassed && activeMismatches.length > 0 && (
        <div className="bg-rose-950/20 border border-rose-500/30 rounded-xl p-4">
          <div className="flex items-start gap-3">
            <AlertTriangle className="w-5 h-5 text-rose-400 shrink-0 mt-0.5" />
            <div className="space-y-1.5">
              <div className="text-sm font-bold text-rose-300">
                Actionable Path Mismatches Reported by ComparisonEngine:
              </div>
              <div className="space-y-1">
                {activeMismatches.map((m, idx) => (
                  <div key={idx} className="flex items-center gap-2 text-xs font-mono">
                    <span className="bg-rose-500/20 text-rose-300 px-1.5 py-0.5 rounded border border-rose-500/30">
                      {m.path}
                    </span>
                    <span className="text-slate-300">{m.message}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Side-by-Side Comparison Panes */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Source Gateway Pane (Kong) */}
        <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden shadow-sm flex flex-col">
          <div className="bg-slate-950 px-4 py-3 border-b border-slate-800 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="w-2.5 h-2.5 rounded-full bg-cyan-400"></span>
              <span className="font-semibold text-sm text-slate-100">Source Gateway (Kong Enterprise)</span>
              <span className="text-xs text-slate-400 font-mono">:8081</span>
            </div>
            <div className="flex items-center gap-2">
              <span className="text-xs px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 font-mono font-medium">
                HTTP {scenario.sourceStatus}
              </span>
              <span className="text-xs text-slate-400 font-mono">{scenario.sourceDurationMs}ms</span>
            </div>
          </div>

          {/* Source Headers */}
          <div className="p-4 bg-slate-900/50 border-b border-slate-800/80 text-xs font-mono space-y-1">
            <div className="text-slate-400 font-sans text-[11px] uppercase tracking-wider font-semibold mb-1">
              Response Headers:
            </div>
            {Object.entries(scenario.sourceHeaders).map(([k, v]) => (
              <div key={k} className="flex items-baseline gap-2">
                <span className="text-cyan-400">{k}:</span>
                <span className="text-slate-300 truncate">{v}</span>
              </div>
            ))}
          </div>

          {/* Source JSON Body */}
          <div className="p-4 flex-1 bg-slate-950 font-mono text-xs overflow-x-auto text-slate-300">
            <div className="text-slate-400 font-sans text-[11px] uppercase tracking-wider font-semibold mb-2">
              Response Body (JSON):
            </div>
            <pre className="text-emerald-400/90 whitespace-pre">
              {JSON.stringify(scenario.sourceBody, null, 2)}
            </pre>
          </div>
        </div>

        {/* Target Gateway Pane (Gravitee) */}
        <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden shadow-sm flex flex-col">
          <div className="bg-slate-950 px-4 py-3 border-b border-slate-800 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="w-2.5 h-2.5 rounded-full bg-indigo-400"></span>
              <span className="font-semibold text-sm text-slate-100">Target Gateway (Gravitee APIM)</span>
              <span className="text-xs text-slate-400 font-mono">:8082</span>
            </div>
            <div className="flex items-center gap-2">
              <span
                className={`text-xs px-2 py-0.5 rounded font-mono font-medium border ${
                  scenario.targetStatus === scenario.sourceStatus
                    ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                    : 'bg-rose-500/10 text-rose-400 border-rose-500/30'
                }`}
              >
                HTTP {scenario.targetStatus}
              </span>
              <span className="text-xs text-slate-400 font-mono">{scenario.targetDurationMs}ms</span>
            </div>
          </div>

          {/* Target Headers */}
          <div className="p-4 bg-slate-900/50 border-b border-slate-800/80 text-xs font-mono space-y-1">
            <div className="text-slate-400 font-sans text-[11px] uppercase tracking-wider font-semibold mb-1">
              Response Headers:
            </div>
            {Object.entries(scenario.targetHeaders).map(([k, v]) => {
              const srcVal = scenario.sourceHeaders[k];
              const isMismatch = srcVal && srcVal !== v;
              return (
                <div
                  key={k}
                  className={`flex items-baseline gap-2 ${
                    isMismatch ? 'bg-rose-500/15 p-1 rounded text-rose-300' : ''
                  }`}
                >
                  <span className={isMismatch ? 'text-rose-400 font-bold' : 'text-indigo-400'}>
                    {k}:
                  </span>
                  <span className={isMismatch ? 'text-rose-200 font-semibold' : 'text-slate-300'}>
                    {v}
                  </span>
                  {isMismatch && (
                    <span className="text-[10px] bg-rose-500 text-white px-1 rounded uppercase font-sans">
                      Diff
                    </span>
                  )}
                </div>
              );
            })}
          </div>

          {/* Target JSON Body */}
          <div className="p-4 flex-1 bg-slate-950 font-mono text-xs overflow-x-auto text-slate-300">
            <div className="text-slate-400 font-sans text-[11px] uppercase tracking-wider font-semibold mb-2">
              Response Body (JSON):
            </div>
            <pre
              className={`whitespace-pre ${
                isPassed ? 'text-emerald-400/90' : 'text-amber-300/90'
              }`}
            >
              {JSON.stringify(scenario.targetBody, null, 2)}
            </pre>
          </div>
        </div>
      </div>

      {/* Informative Footer Box on Comparison Engine Rules */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-4 text-xs text-slate-400 space-y-2">
        <div className="font-semibold text-slate-200 flex items-center gap-1.5">
          <ShieldCheck className="w-4 h-4 text-cyan-400" /> Comparison Engine Guarantees (Phase 1)
        </div>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-3 pt-1">
          <div className="bg-slate-950/50 p-2.5 rounded-lg border border-slate-800">
            <strong className="text-slate-300 block mb-0.5">Key Order Invariance</strong>
            JSON key re-ordering does not trigger false mismatches; trees are parsed into semantic ASTs.
          </div>
          <div className="bg-slate-950/50 p-2.5 rounded-lg border border-slate-800">
            <strong className="text-slate-300 block mb-0.5">Strict Array Ordering</strong>
            Array element ordering matters by default. GateDiff flags element permutations or shifts.
          </div>
          <div className="bg-slate-950/50 p-2.5 rounded-lg border border-slate-800">
            <strong className="text-slate-300 block mb-0.5">Controlled Error Recovery</strong>
            Non-JSON or malformed payloads produce clean path diagnostics instead of unhandled crashes.
          </div>
        </div>
      </div>
    </div>
  );
};
