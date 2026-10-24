import React, { useState } from 'react';
import { SAMPLE_MIGRATION_YAML } from '../data/mockData';
import { FileCode, CheckCircle2, AlertTriangle, RotateCcw, Copy, Check } from 'lucide-react';

export const ConfigEditorView: React.FC = () => {
  const [yamlContent, setYamlContent] = useState<string>(SAMPLE_MIGRATION_YAML);
  const [copied, setCopied] = useState<boolean>(false);

  // Validate YAML in real-time
  const validateYaml = (content: string) => {
    const errors: string[] = [];
    if (!content.includes('version: 1')) {
      errors.push("version: Must be 'version: 1' (GateDiff MVP schema standard)");
    }
    if (!content.includes('project:')) {
      errors.push("project: Missing 'project' section definition");
    }
    if (!content.includes('sourceBaseUrl:')) {
      errors.push("services[0].sourceBaseUrl: Missing sourceBaseUrl (must start with http:// or https://)");
    }
    if (!content.includes('targetBaseUrl:')) {
      errors.push("services[0].targetBaseUrl: Missing targetBaseUrl (must start with http:// or https://)");
    }
    if (!content.includes('tests:')) {
      errors.push('services[0].tests: At least one migration test case must be defined');
    }
    return errors;
  };

  const validationErrors = validateYaml(yamlContent);
  const isValid = validationErrors.length === 0;

  const handleCopy = () => {
    navigator.clipboard.writeText(yamlContent);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <div className="text-xs font-semibold uppercase tracking-wider text-cyan-400 flex items-center gap-1.5">
              <FileCode className="w-4 h-4" /> Phase 4 — Versioned YAML Configuration Schema
            </div>
            <h2 className="text-xl font-bold text-white mt-1">
              Migration Specification Editor & Validator
            </h2>
            <p className="text-sm text-slate-400 mt-1 max-w-3xl">
              Deterministic, reviewable YAML schema supporting global defaults, service overrides, custom headers, and granular JSONPath ignore rules.
            </p>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={handleCopy}
              className="bg-slate-800 hover:bg-slate-700 text-slate-200 px-3 py-1.5 rounded-lg text-xs font-medium flex items-center gap-1.5 transition-colors"
            >
              {copied ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
              {copied ? 'Copied' : 'Copy YAML'}
            </button>
            <button
              onClick={() => setYamlContent(SAMPLE_MIGRATION_YAML)}
              className="bg-slate-800 hover:bg-slate-700 text-slate-200 px-3 py-1.5 rounded-lg text-xs font-medium flex items-center gap-1.5 transition-colors"
            >
              <RotateCcw className="w-3.5 h-3.5" /> Reset
            </button>
          </div>
        </div>

        {/* Validation Status Bar */}
        <div className="mt-4 pt-4 border-t border-slate-800 flex items-center justify-between text-xs">
          <div className="flex items-center gap-2">
            <span className="text-slate-400 font-medium">Schema Status:</span>
            {isValid ? (
              <span className="inline-flex items-center gap-1.5 text-emerald-400 font-semibold bg-emerald-500/10 px-2.5 py-0.5 rounded-full border border-emerald-500/30">
                <CheckCircle2 className="w-3.5 h-3.5" /> Validated (Version 1, Ready for Execution)
              </span>
            ) : (
              <span className="inline-flex items-center gap-1.5 text-rose-400 font-semibold bg-rose-500/10 px-2.5 py-0.5 rounded-full border border-rose-500/30">
                <AlertTriangle className="w-3.5 h-3.5" /> Validation Failed ({validationErrors.length} errors)
              </span>
            )}
          </div>
          <span className="text-slate-500 font-mono text-[11px]">examples/configs/migration.yaml</span>
        </div>
      </div>

      {/* Error Callout if invalid */}
      {!isValid && (
        <div className="bg-rose-950/20 border border-rose-500/30 rounded-xl p-4 text-xs font-mono space-y-1">
          <div className="font-bold text-rose-300 font-sans mb-1 flex items-center gap-1.5">
            <AlertTriangle className="w-4 h-4 text-rose-400" /> Validation Errors:
          </div>
          {validationErrors.map((err, i) => (
            <div key={i} className="text-rose-300">
              * {err}
            </div>
          ))}
        </div>
      )}

      {/* YAML Editor Textarea */}
      <div className="bg-slate-950 border border-slate-800 rounded-xl overflow-hidden shadow-2xl font-mono text-xs">
        <div className="bg-slate-900 px-4 py-2 border-b border-slate-800 text-slate-400 flex items-center justify-between">
          <span>migration.yaml</span>
          <span className="text-[11px] text-slate-500 font-sans">Live Editable</span>
        </div>
        <textarea
          value={yamlContent}
          onChange={(e) => setYamlContent(e.target.value)}
          rows={22}
          className="w-full bg-slate-950 text-cyan-300 p-4 font-mono text-xs outline-none focus:ring-0 border-0 resize-y leading-relaxed"
          spellCheck={false}
        />
      </div>
    </div>
  );
};
