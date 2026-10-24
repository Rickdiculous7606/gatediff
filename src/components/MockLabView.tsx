import React, { useState } from 'react';
import { LAB_SCENARIOS, ScenarioConfig } from '../data/mockData';
import { Server, Activity, ShieldAlert, Zap, Globe, RefreshCw } from 'lucide-react';

export const MockLabView: React.FC = () => {
  const [activeScenario, setActiveScenario] = useState<string>('MATCHING');
  const [selectedEndpoint, setSelectedEndpoint] = useState<string>('/api/customers/C-101');
  const [simulatedMethod, setSimulatedMethod] = useState<string>('GET');

  const scenario: ScenarioConfig = LAB_SCENARIOS[activeScenario] || LAB_SCENARIOS.MATCHING;

  const endpoints = [
    { method: 'GET', path: '/api/customers/C-101', desc: 'Customer C-101 by ID' },
    { method: 'GET', path: '/api/customers', desc: 'List all customers' },
    { method: 'POST', path: '/api/customers', desc: 'Create new customer' },
    { method: 'DELETE', path: '/api/customers/C-101', desc: 'Delete customer' },
    { method: 'GET', path: '/health', desc: 'Gateway health check' },
  ];

  return (
    <div className="space-y-6">
      {/* Intro Header */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm">
        <div className="flex items-center gap-2 text-cyan-400 text-xs font-semibold uppercase tracking-wider">
          <Server className="w-4 h-4" /> Phase 2 — Local Fictional Migration Lab
        </div>
        <h2 className="text-xl font-bold text-white mt-1">
          Zero-Dependency Local Mock Gateways
        </h2>
        <p className="text-sm text-slate-400 mt-1 max-w-3xl">
          Powered entirely by Java’s built-in <code className="text-cyan-300">com.sun.net.httpserver.HttpServer</code>.
          Binds strictly to <code className="text-cyan-300">127.0.0.1</code> (localhost) to guarantee no external network exposure or telemetry.
          No Docker, Docker Desktop, paid cloud accounts, or third-party gateways required.
        </p>

        <div className="mt-4 grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="bg-slate-950 p-3.5 rounded-lg border border-slate-800/80 flex items-center justify-between">
            <div>
              <div className="text-xs text-slate-400 font-semibold uppercase">Source Gateway (Mock)</div>
              <div className="text-sm font-bold text-slate-100 flex items-center gap-2 mt-0.5">
                <span className="w-2 h-2 rounded-full bg-cyan-400"></span>
                Kong Enterprise 3.4
              </div>
            </div>
            <code className="text-xs font-mono text-cyan-400 bg-cyan-950/40 px-2 py-1 rounded border border-cyan-800/30">
              http://localhost:8081
            </code>
          </div>

          <div className="bg-slate-950 p-3.5 rounded-lg border border-slate-800/80 flex items-center justify-between">
            <div>
              <div className="text-xs text-slate-400 font-semibold uppercase">Target Gateway (Mock)</div>
              <div className="text-sm font-bold text-slate-100 flex items-center gap-2 mt-0.5">
                <span className="w-2 h-2 rounded-full bg-indigo-400"></span>
                Gravitee APIM 4.2
              </div>
            </div>
            <code className="text-xs font-mono text-indigo-400 bg-indigo-950/40 px-2 py-1 rounded border border-indigo-800/30">
              http://localhost:8082
            </code>
          </div>
        </div>
      </div>

      {/* Scenario Selector Matrix */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm">
        <h3 className="text-sm font-bold text-slate-200 mb-3 flex items-center gap-2">
          <Activity className="w-4 h-4 text-cyan-400" />
          Select Simulation Scenario Mode
        </h3>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
          {Object.entries(LAB_SCENARIOS).map(([key, item]) => {
            const isSelected = activeScenario === key;
            return (
              <button
                key={key}
                onClick={() => setActiveScenario(key)}
                className={`text-left p-3 rounded-lg border transition-all ${
                  isSelected
                    ? 'bg-cyan-950/40 border-cyan-500 shadow-sm text-white'
                    : 'bg-slate-950/60 border-slate-800 hover:border-slate-700 text-slate-300'
                }`}
              >
                <div className="flex items-center justify-between mb-1">
                  <span className="font-semibold text-xs text-cyan-300">{item.name}</span>
                  {isSelected && <span className="w-1.5 h-1.5 rounded-full bg-cyan-400"></span>}
                </div>
                <p className="text-[11px] text-slate-400 line-clamp-2">{item.description}</p>
              </button>
            );
          })}
        </div>
      </div>

      {/* Endpoint Live Probe Simulation */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div>
            <h3 className="text-sm font-bold text-slate-200">Test Mock Endpoints</h3>
            <p className="text-xs text-slate-400">
              Select an endpoint to inspect dual gateway behavior under{' '}
              <strong className="text-cyan-300">{scenario.name}</strong> mode:
            </p>
          </div>
          <div className="flex items-center gap-2">
            {endpoints.map((ep) => (
              <button
                key={ep.path}
                onClick={() => {
                  setSelectedEndpoint(ep.path);
                  setSimulatedMethod(ep.method);
                }}
                className={`text-xs px-2.5 py-1 rounded font-mono transition-colors ${
                  selectedEndpoint === ep.path
                    ? 'bg-cyan-500 text-slate-950 font-bold'
                    : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
                }`}
              >
                {ep.method} {ep.path.replace('/api', '')}
              </button>
            ))}
          </div>
        </div>

        {/* Live response simulation grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="bg-slate-950 p-4 rounded-lg border border-slate-800 font-mono text-xs">
            <div className="flex items-center justify-between mb-2 pb-2 border-b border-slate-800">
              <span className="text-cyan-400 font-bold">Source: 8081</span>
              <span className="text-emerald-400 font-semibold">HTTP {scenario.sourceStatus} OK</span>
            </div>
            <pre className="text-slate-300 overflow-x-auto whitespace-pre">
              {JSON.stringify(scenario.sourceBody, null, 2)}
            </pre>
          </div>

          <div className="bg-slate-950 p-4 rounded-lg border border-slate-800 font-mono text-xs">
            <div className="flex items-center justify-between mb-2 pb-2 border-b border-slate-800">
              <span className="text-indigo-400 font-bold">Target: 8082</span>
              <span
                className={`font-semibold ${
                  scenario.targetStatus === 200 ? 'text-emerald-400' : 'text-rose-400'
                }`}
              >
                HTTP {scenario.targetStatus}{' '}
                {scenario.targetStatus === 200 ? 'OK' : scenario.targetStatus === 404 ? 'Not Found' : 'Server Error'}
              </span>
            </div>
            <pre
              className={`overflow-x-auto whitespace-pre ${
                scenario.targetStatus === 200 ? 'text-slate-300' : 'text-rose-300'
              }`}
            >
              {JSON.stringify(scenario.targetBody, null, 2)}
            </pre>
          </div>
        </div>
      </div>
    </div>
  );
};
