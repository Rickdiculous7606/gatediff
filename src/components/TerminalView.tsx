import React, { useState } from 'react';
import { Terminal as TerminalIcon, Play, RotateCcw, Copy, Check } from 'lucide-react';

interface CommandOutput {
  command: string;
  output: string[];
  exitCode: number;
}

export const TerminalView: React.FC = () => {
  const [currentInput, setCurrentInput] = useState<string>('gatediff run --config examples/configs/migration.yaml');
  const [copied, setCopied] = useState<boolean>(false);
  const [history, setHistory] = useState<CommandOutput[]>([
    {
      command: 'gatediff config validate --file examples/configs/migration.yaml',
      output: [
        'Validating configuration: /app/applet/examples/configs/migration.yaml',
        '[PASS] Configuration is valid (Version 1, 1 service(s) configured).',
      ],
      exitCode: 0,
    },
    {
      command: 'gatediff run --config examples/configs/migration.yaml',
      output: [
        '================================================================================',
        ' GateDiff Migration Test Execution Summary',
        ' Project:   fictional-customer-migration',
        ' Timestamp: 2026-10-24T07:57:15.555Z',
        ' Total:     4 | Passed: 4 | Failed: 0 | Errors: 0',
        ' Duration:  56ms',
        '================================================================================',
        '[PASS] customer-api     | GET /api/customers/C-101             (8ms)',
        '[PASS] customer-api     | GET /api/customers                   (27ms)',
        '[PASS] customer-api     | POST /api/customers                   (7ms)',
        '[PASS] customer-api     | DELETE /api/customers/C-101             (11ms)',
        '--------------------------------------------------------------------------------',
        ' STATUS: ALL MIGRATION TESTS PASSED (Exit code: 0)',
        '================================================================================',
      ],
      exitCode: 0,
    },
  ]);

  const presetCommands = [
    { label: 'Run Migration Tests', cmd: 'gatediff run --config examples/configs/migration.yaml' },
    { label: 'Validate YAML Config', cmd: 'gatediff config validate --file examples/configs/migration.yaml' },
    { label: 'Import OpenAPI Spec', cmd: 'gatediff import openapi --input examples/specs/customer-api.yaml' },
    { label: 'Map Routes & Ops', cmd: 'gatediff map --source examples/specs/customer-api.yaml --target examples/specs/target-customer-api.yaml' },
    { label: 'Start Mock Lab', cmd: 'gatediff mock start --scenario MATCHING' },
    { label: 'Simulate Failure', cmd: 'gatediff run --config examples/configs/migration.yaml --scenario VALUE_MISMATCH' },
    { label: 'CLI Help', cmd: 'gatediff --help' },
  ];

  const handleRunCommand = (cmdToRun: string) => {
    const trimmed = cmdToRun.trim();
    if (!trimmed) return;

    let outputLines: string[] = [];
    let code = 0;

    if (trimmed.includes('--help')) {
      outputLines = [
        'Usage: gatediff [-hV] [COMMAND]',
        'Open-Source API Gateway Migration Testing CLI for Kong, Gravitee, Apigee, and AWS API Gateway.',
        '  -h, --help      Show this help message and exit.',
        '  -V, --version   Print version information and exit.',
        'Commands:',
        '  run       Execute migration validation comparisons between source and target gateways',
        '  mock      Run local fictional API migration lab servers for offline testing',
        '  config    Validate GateDiff YAML configuration files',
        '  import    Import and inspect API specifications',
        '  map       Suggest route mappings between source and target OpenAPI specs',
        '  generate  Generate reviewable migration YAML configuration from confirmed mappings',
        '  report    Render or convert GateDiff JSON test results into terminal or HTML format',
      ];
      code = 0;
    } else if (trimmed.includes('VALUE_MISMATCH')) {
      outputLines = [
        '================================================================================',
        ' GateDiff Migration Test Execution Summary',
        ' Project:   fictional-customer-migration',
        ' Total:     4 | Passed: 3 | Failed: 1 | Errors: 0',
        '================================================================================',
        '[FAIL] customer-api     | GET /api/customers/C-101             (12ms)',
        '       * Source: HTTP 200 (4ms)',
        '       * Target: HTTP 200 (4ms)',
        '       * MISMATCH $.status: expected ACTIVE but received INACTIVE',
        '[PASS] customer-api     | GET /api/customers                   (22ms)',
        '[PASS] customer-api     | POST /api/customers                   (9ms)',
        '[PASS] customer-api     | DELETE /api/customers/C-101             (8ms)',
        '--------------------------------------------------------------------------------',
        ' STATUS: MIGRATION MISMATCH REGRESSIONS DETECTED (Exit code: 1)',
        '================================================================================',
      ];
      code = 1;
    } else if (trimmed.includes('mock start')) {
      outputLines = [
        '================================================================================',
        ' GateDiff Fictional API Migration Lab Started',
        ' Source Gateway (Kong Mock):     http://localhost:8081',
        ' Target Gateway (Gravitee Mock): http://localhost:8082',
        ' Active Scenario Mode:           MATCHING',
        ' Sample Endpoint:                GET http://localhost:8081/api/customers/C-101',
        ' Security Note:                  Bound strictly to 127.0.0.1 (Localhost only)',
        ' Press Ctrl+C to stop.',
        '================================================================================',
      ];
      code = 0;
    } else if (trimmed.includes('import openapi')) {
      outputLines = [
        '================================================================================',
        ' OpenAPI Specification Summary: Fictional Customer Service (Source - Kong) (v1.0.0)',
        ' File:        customer-api.yaml',
        ' Description: Source microservice exposed through Kong API Gateway',
        ' Operations:  4 extracted',
        '================================================================================',
        'METHOD   PATH                             OPERATION ID             RESPONSES',
        '--------------------------------------------------------------------------------',
        'GET      /api/customers                   listCustomers            200',
        'POST     /api/customers                   createCustomer           201',
        'GET      /api/customers/{id}              getCustomerById          200, 404',
        'DELETE   /api/customers/{id}              deleteCustomer           204',
        '================================================================================',
      ];
      code = 0;
    } else if (trimmed.includes('map')) {
      outputLines = [
        '================================================================================',
        ' GateDiff Migration Operation Mapping Report',
        ' Source: Fictional Customer Service (Source - Kong) (4 ops)',
        ' Target: Fictional Customer Service (Target - Gravitee) (4 ops)',
        '================================================================================',
        'STATUS       SOURCE OPERATION               -> TARGET OPERATION               NOTES',
        '--------------------------------------------------------------------------------',
        '[CONFIRMED ] GET /api/customers             -> GET /api/customers             Exact method (GET) and matching path (/api/customers)',
        '[CONFIRMED ] POST /api/customers            -> POST /api/customers            Exact method (POST) and matching path (/api/customers)',
        '[CONFIRMED ] GET /api/customers/{id}        -> GET /api/customers/{id}        Exact method (GET) and matching path (/api/customers/{id})',
        '[CONFIRMED ] DELETE /api/customers/{id}     -> DELETE /api/customers/{id}     Exact method (DELETE) and matching path (/api/customers/{id})',
        '--------------------------------------------------------------------------------',
        'Coverage: 4 Confirmed | 0 Suggested | 0 Ambiguous | 0 Unresolved',
        '================================================================================',
      ];
      code = 0;
    } else if (trimmed.includes('config validate')) {
      outputLines = [
        'Validating configuration: /app/applet/examples/configs/migration.yaml',
        '[PASS] Configuration is valid (Version 1, 1 service(s) configured).',
      ];
      code = 0;
    } else {
      outputLines = [
        '================================================================================',
        ' GateDiff Migration Test Execution Summary',
        ' Project:   fictional-customer-migration',
        ' Total:     4 | Passed: 4 | Failed: 0 | Errors: 0',
        ' Duration:  48ms',
        '================================================================================',
        '[PASS] customer-api     | GET /api/customers/C-101             (8ms)',
        '[PASS] customer-api     | GET /api/customers                   (20ms)',
        '[PASS] customer-api     | POST /api/customers                   (6ms)',
        '[PASS] customer-api     | DELETE /api/customers/C-101             (9ms)',
        '--------------------------------------------------------------------------------',
        ' STATUS: ALL MIGRATION TESTS PASSED (Exit code: 0)',
        '================================================================================',
      ];
      code = 0;
    }

    setHistory((prev) => [...prev, { command: trimmed, output: outputLines, exitCode: code }]);
  };

  const copyAll = () => {
    const text = history
      .map((h) => `$ ${h.command}\n${h.output.join('\n')}\n[Exit code: ${h.exitCode}]`)
      .join('\n\n');
    navigator.clipboard.writeText(text);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="space-y-4">
      {/* Quick Launch Buttons */}
      <div className="flex flex-wrap items-center gap-2">
        <span className="text-xs text-slate-400 font-medium">Quick Commands:</span>
        {presetCommands.map((p, idx) => (
          <button
            key={idx}
            onClick={() => {
              setCurrentInput(p.cmd);
              handleRunCommand(p.cmd);
            }}
            className="text-xs bg-slate-900 hover:bg-slate-800 text-slate-300 hover:text-white px-2.5 py-1 rounded-md border border-slate-800 font-mono transition-colors"
          >
            {p.label}
          </button>
        ))}
      </div>

      {/* Terminal Window */}
      <div className="bg-slate-950 border border-slate-800 rounded-xl overflow-hidden shadow-2xl font-mono text-xs">
        {/* Terminal Header */}
        <div className="bg-slate-900/90 px-4 py-2.5 border-b border-slate-800 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="flex items-center gap-1.5">
              <span className="w-3 h-3 rounded-full bg-rose-500/80 inline-block"></span>
              <span className="w-3 h-3 rounded-full bg-amber-500/80 inline-block"></span>
              <span className="w-3 h-3 rounded-full bg-emerald-500/80 inline-block"></span>
            </div>
            <span className="text-slate-400 text-xs ml-2 flex items-center gap-1.5">
              <TerminalIcon className="w-3.5 h-3.5 text-cyan-400" />
              GateDiff CLI Terminal (Linux/Windows PowerShell)
            </span>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={copyAll}
              className="text-slate-400 hover:text-slate-200 text-xs flex items-center gap-1 px-2 py-0.5 rounded hover:bg-slate-800 transition-colors"
              title="Copy terminal session"
            >
              {copied ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
              {copied ? 'Copied' : 'Copy'}
            </button>
            <button
              onClick={() => setHistory([])}
              className="text-slate-400 hover:text-slate-200 text-xs flex items-center gap-1 px-2 py-0.5 rounded hover:bg-slate-800 transition-colors"
              title="Clear terminal"
            >
              <RotateCcw className="w-3.5 h-3.5" /> Clear
            </button>
          </div>
        </div>

        {/* Terminal Body */}
        <div className="p-4 space-y-4 max-h-[500px] overflow-y-auto">
          {history.map((item, idx) => (
            <div key={idx} className="space-y-1.5">
              <div className="flex items-center gap-2 text-slate-400">
                <span className="text-cyan-400 font-bold">$</span>
                <span className="text-slate-100 font-semibold">{item.command}</span>
                <span
                  className={`ml-auto text-[10px] px-1.5 py-0.2 rounded font-sans uppercase font-bold ${
                    item.exitCode === 0
                      ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                      : 'bg-rose-500/10 text-rose-400 border border-rose-500/30'
                  }`}
                >
                  Exit: {item.exitCode}
                </span>
              </div>
              <div className="pl-4 border-l border-slate-800/80 space-y-0.5 text-slate-300">
                {item.output.map((line, lIdx) => {
                  let colorClass = 'text-slate-300';
                  if (line.includes('[PASS]')) colorClass = 'text-emerald-400 font-semibold';
                  if (line.includes('[FAIL]')) colorClass = 'text-rose-400 font-semibold';
                  if (line.includes('MISMATCH')) colorClass = 'text-rose-300';
                  if (line.includes('[CONFIRMED ]')) colorClass = 'text-cyan-300';
                  if (line.includes('STATUS: ALL MIGRATION TESTS PASSED'))
                    colorClass = 'text-emerald-400 font-bold';
                  if (line.includes('STATUS: MIGRATION MISMATCH REGRESSIONS DETECTED'))
                    colorClass = 'text-rose-400 font-bold';
                  return (
                    <div key={lIdx} className={colorClass}>
                      {line}
                    </div>
                  );
                })}
              </div>
            </div>
          ))}

          {/* Prompt line */}
          <form
            onSubmit={(e) => {
              e.preventDefault();
              handleRunCommand(currentInput);
            }}
            className="flex items-center gap-2 pt-2 border-t border-slate-900"
          >
            <span className="text-cyan-400 font-bold">$</span>
            <input
              type="text"
              value={currentInput}
              onChange={(e) => setCurrentInput(e.target.value)}
              placeholder="gatediff run --config examples/configs/migration.yaml"
              className="flex-1 bg-transparent border-0 outline-none text-slate-100 font-mono text-xs focus:ring-0 p-0 placeholder:text-slate-600"
            />
            <button
              type="submit"
              className="bg-cyan-600 hover:bg-cyan-500 text-white px-2.5 py-1 rounded text-xs flex items-center gap-1 font-sans font-medium transition-colors"
            >
              <Play className="w-3 h-3 fill-white" /> Execute
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};
