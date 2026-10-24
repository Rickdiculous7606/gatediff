import React, { useState } from 'react';
import { BookOpen, Laptop, Terminal, FolderTree, Code, CheckCircle, ExternalLink } from 'lucide-react';

export const JavaDocsView: React.FC = () => {
  const [activeSection, setActiveSection] = useState<'windows' | 'intellij' | 'sources' | 'exitcodes'>('windows');

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm">
        <div className="text-xs font-semibold uppercase tracking-wider text-cyan-400 flex items-center gap-1.5">
          <BookOpen className="w-4 h-4" /> Open-Source Maintainer & Beginner Guide
        </div>
        <h2 className="text-xl font-bold text-white mt-1">
          IntelliJ IDEA, Windows PowerShell & Java 21 Guide
        </h2>
        <p className="text-sm text-slate-400 mt-1 max-w-3xl">
          Complete instructions for setting up on Windows with PowerShell, importing into IntelliJ IDEA, running the 38 automated JUnit tests, and packaging the standalone executable JAR.
        </p>

        {/* Sub-navigation tabs */}
        <div className="mt-4 pt-4 border-t border-slate-800 flex flex-wrap gap-2 text-xs">
          <button
            onClick={() => setActiveSection('windows')}
            className={`px-3 py-1.5 rounded-lg font-medium transition-colors ${
              activeSection === 'windows'
                ? 'bg-cyan-500 text-slate-950 font-bold'
                : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
            }`}
          >
            Windows & PowerShell Setup
          </button>
          <button
            onClick={() => setActiveSection('intellij')}
            className={`px-3 py-1.5 rounded-lg font-medium transition-colors ${
              activeSection === 'intellij'
                ? 'bg-cyan-500 text-slate-950 font-bold'
                : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
            }`}
          >
            IntelliJ IDEA Import Instructions
          </button>
          <button
            onClick={() => setActiveSection('exitcodes')}
            className={`px-3 py-1.5 rounded-lg font-medium transition-colors ${
              activeSection === 'exitcodes'
                ? 'bg-cyan-500 text-slate-950 font-bold'
                : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
            }`}
          >
            CI & Exit Code Policies
          </button>
          <button
            onClick={() => setActiveSection('sources')}
            className={`px-3 py-1.5 rounded-lg font-medium transition-colors ${
              activeSection === 'sources'
                ? 'bg-cyan-500 text-slate-950 font-bold'
                : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
            }`}
          >
            Java Package Architecture
          </button>
        </div>
      </div>

      {/* Windows & PowerShell Section */}
      {activeSection === 'windows' && (
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 shadow-sm space-y-4">
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <Terminal className="w-5 h-5 text-cyan-400" />
            Setting Up GateDiff on Windows 10/11 with PowerShell
          </h3>

          <div className="space-y-4 text-xs text-slate-300 leading-relaxed">
            <div>
              <strong className="text-slate-100 block text-sm mb-1">
                1. Install Java 21 & Maven via Windows Package Manager (winget)
              </strong>
              <p className="text-slate-400 mb-2">Open PowerShell as Administrator and run:</p>
              <pre className="p-3 bg-slate-950 rounded-lg text-cyan-300 font-mono border border-slate-800 overflow-x-auto">
                winget install EclipseAdoptium.Temurin.21.JDK&#10;winget install Apache.Maven
              </pre>
            </div>

            <div>
              <strong className="text-slate-100 block text-sm mb-1">
                2. Verify Installation
              </strong>
              <p className="text-slate-400 mb-2">Close and re-open PowerShell, then execute:</p>
              <pre className="p-3 bg-slate-950 rounded-lg text-cyan-300 font-mono border border-slate-800 overflow-x-auto">
                java -version&#10;mvn -version
              </pre>
            </div>

            <div>
              <strong className="text-slate-100 block text-sm mb-1">
                3. Compile and Run Automated Tests
              </strong>
              <pre className="p-3 bg-slate-950 rounded-lg text-emerald-400 font-mono border border-slate-800 overflow-x-auto">
                mvn clean test&#10;mvn package -DskipTests
              </pre>
              <p className="text-slate-400 mt-2">
                This compiles all 38 unit and integration tests and produces the standalone executable JAR at <code className="text-cyan-300 font-mono">target\gatediff.jar</code>.
              </p>
            </div>

            <div>
              <strong className="text-slate-100 block text-sm mb-1">
                4. Start the Mock Gateway Lab & Execute Migration Tests
              </strong>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3 mt-2">
                <div className="p-3 bg-slate-950 rounded-lg border border-slate-800">
                  <span className="text-slate-400 font-mono text-[11px] block mb-1">PowerShell Window 1 (Mock Lab):</span>
                  <pre className="text-cyan-300 font-mono text-xs">java -jar target\gatediff.jar mock start --scenario MATCHING</pre>
                </div>
                <div className="p-3 bg-slate-950 rounded-lg border border-slate-800">
                  <span className="text-slate-400 font-mono text-[11px] block mb-1">PowerShell Window 2 (Test Execution):</span>
                  <pre className="text-cyan-300 font-mono text-xs">java -jar target\gatediff.jar run --config examples\configs\migration.yaml</pre>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* IntelliJ IDEA Section */}
      {activeSection === 'intellij' && (
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 shadow-sm space-y-4">
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <Laptop className="w-5 h-5 text-indigo-400" />
            Importing and Running in IntelliJ IDEA
          </h3>

          <div className="space-y-4 text-xs text-slate-300 leading-relaxed">
            <div className="p-3.5 bg-slate-950 rounded-lg border border-slate-800">
              <strong className="text-cyan-300 text-sm block mb-1">Step 1: Open Project</strong>
              <p className="text-slate-400">
                Launch IntelliJ IDEA. Click <strong>File &rarr; Open...</strong> and select the directory containing <code className="text-slate-200">pom.xml</code>. Choose <strong>Open as Project</strong>.
              </p>
            </div>

            <div className="p-3.5 bg-slate-950 rounded-lg border border-slate-800">
              <strong className="text-cyan-300 text-sm block mb-1">Step 2: Set Project SDK to Java 21</strong>
              <p className="text-slate-400">
                Navigate to <strong>File &rarr; Project Structure &rarr; Project</strong>. Under <strong>SDK</strong>, choose <strong>JDK 21</strong> (e.g. Temurin 21 or Corretto 21). If not installed, IntelliJ can download it directly via &quot;Add SDK &rarr; Download JDK&quot;.
              </p>
            </div>

            <div className="p-3.5 bg-slate-950 rounded-lg border border-slate-800">
              <strong className="text-cyan-300 text-sm block mb-1">Step 3: Run the Test Suite</strong>
              <p className="text-slate-400">
                In the Project Tool Window, expand <code className="text-slate-200 font-mono">src/test/java</code>. Right-click the folder and choose <strong>Run &apos;All Tests&apos;</strong>. You will see all 38 tests pass with green checkmarks.
              </p>
            </div>

            <div className="p-3.5 bg-slate-950 rounded-lg border border-slate-800">
              <strong className="text-cyan-300 text-sm block mb-1">Step 4: Debugging CLI Commands</strong>
              <p className="text-slate-400">
                Open <code className="text-slate-200 font-mono">io.gatediff.GateDiffMain.java</code>. Click the green play icon next to <code className="text-slate-200 font-mono">main()</code>, or configure Program Arguments: <code className="text-cyan-300 font-mono">run --config examples/configs/migration.yaml</code>.
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Exit Codes Policy Section */}
      {activeSection === 'exitcodes' && (
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 shadow-sm space-y-4">
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <CheckCircle className="w-5 h-5 text-emerald-400" />
            Deterministic Process Exit Code Contract for CI Pipelines
          </h3>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs font-mono">
              <thead className="bg-slate-950 text-slate-400 uppercase text-[11px] border-b border-slate-800">
                <tr>
                  <th className="py-2.5 px-4">Exit Code</th>
                  <th className="py-2.5 px-4">Status Constant</th>
                  <th className="py-2.5 px-4">Trigger Condition</th>
                  <th className="py-2.5 px-4 font-sans">CI Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/80 text-slate-300">
                <tr>
                  <td className="py-3 px-4 font-bold text-emerald-400">0</td>
                  <td className="py-3 px-4 text-slate-200">ExitCodes.SUCCESS</td>
                  <td className="py-3 px-4">All executed comparisons passed equivalence assertions.</td>
                  <td className="py-3 px-4 font-sans text-emerald-400 font-semibold">Pipeline Proceeds</td>
                </tr>
                <tr>
                  <td className="py-3 px-4 font-bold text-rose-400">1</td>
                  <td className="py-3 px-4 text-slate-200">ExitCodes.COMPARISON_FAILED</td>
                  <td className="py-3 px-4">One or more response differences or regressions detected.</td>
                  <td className="py-3 px-4 font-sans text-rose-400 font-semibold">Fails Pipeline (Regression)</td>
                </tr>
                <tr>
                  <td className="py-3 px-4 font-bold text-amber-400">2</td>
                  <td className="py-3 px-4 text-slate-200">ExitCodes.INVALID_CONFIG_OR_USAGE</td>
                  <td className="py-3 px-4">YAML schema error, missing file, or invalid CLI parameters.</td>
                  <td className="py-3 px-4 font-sans text-amber-400 font-semibold">Fails Pipeline (Config Error)</td>
                </tr>
                <tr>
                  <td className="py-3 px-4 font-bold text-amber-400">3</td>
                  <td className="py-3 px-4 text-slate-200">ExitCodes.INFRASTRUCTURE_ERROR</td>
                  <td className="py-3 px-4">Gateway endpoint connection refused, timeout, or DNS error.</td>
                  <td className="py-3 px-4 font-sans text-amber-400 font-semibold">Fails Pipeline (Infra Down)</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Java Package Architecture Section */}
      {activeSection === 'sources' && (
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 shadow-sm space-y-4">
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <FolderTree className="w-5 h-5 text-cyan-400" />
            GateDiff Java Package Structure (io.gatediff)
          </h3>

          <div className="p-4 bg-slate-950 rounded-lg border border-slate-800 font-mono text-xs text-slate-300 leading-relaxed overflow-x-auto">
            <div className="text-cyan-400 font-bold mb-2">src/main/java/io/gatediff/</div>
            <div>├── GateDiffMain.java                 <span className="text-slate-500">// CLI main entry point</span></div>
            <div>├── <span className="text-indigo-400">cli/</span></div>
            <div>│   ├── GateDiffCommand.java         <span className="text-slate-500">// Root Picocli command</span></div>
            <div>│   ├── RunCommand.java              <span className="text-slate-500">// &apos;run&apos; execution command</span></div>
            <div>│   ├── MockCommand.java             <span className="text-slate-500">// &apos;mock start&apos; lab command</span></div>
            <div>│   ├── ConfigValidateCommand.java   <span className="text-slate-500">// &apos;config validate&apos; command</span></div>
            <div>│   ├── ImportOpenApiCommand.java    <span className="text-slate-500">// &apos;import openapi&apos; command</span></div>
            <div>│   ├── MapCommand.java              <span className="text-slate-500">// &apos;map&apos; suggestion command</span></div>
            <div>│   ├── GenerateCommand.java         <span className="text-slate-500">// &apos;generate&apos; YAML command</span></div>
            <div>│   ├── ReportCommand.java           <span className="text-slate-500">// &apos;report&apos; converter command</span></div>
            <div>│   └── ExitCodes.java               <span className="text-slate-500">// Constants: 0, 1, 2, 3</span></div>
            <div>├── <span className="text-indigo-400">comparison/</span></div>
            <div>│   └── ComparisonEngine.java        <span className="text-slate-500">// Deep JSONPath diff &amp; rules</span></div>
            <div>├── <span className="text-indigo-400">http/</span></div>
            <div>│   └── HttpRunner.java              <span className="text-slate-500">// Java 21 HttpClient dual runner</span></div>
            <div>├── <span className="text-indigo-400">config/</span></div>
            <div>│   ├── GateDiffConfig.java          <span className="text-slate-500">// Root YAML schema (v1)</span></div>
            <div>│   ├── ConfigValidator.java         <span className="text-slate-500">// Semantic and syntax validator</span></div>
            <div>│   └── ConfigLoader.java            <span className="text-slate-500">// Jackson YAML parser</span></div>
            <div>├── <span className="text-indigo-400">openapi/</span></div>
            <div>│   ├── OpenApiParser.java           <span className="text-slate-500">// OpenAPI 3.x YAML/JSON parser</span></div>
            <div>│   └── OpenApiOperation.java         <span className="text-slate-500">// Extracted operation model</span></div>
            <div>├── <span className="text-indigo-400">mapping/</span></div>
            <div>│   ├── MappingSuggestionEngine.java <span className="text-slate-500">// Heuristic path &amp; method matcher</span></div>
            <div>│   └── YamlConfigGenerator.java     <span className="text-slate-500">// migration.yaml generator</span></div>
            <div>├── <span className="text-indigo-400">mock/</span></div>
            <div>│   ├── LocalMockLab.java            <span className="text-slate-500">// Built-in JDK HttpServer lab</span></div>
            <div>│   └── MockScenario.java            <span className="text-slate-500">// Lab test scenarios</span></div>
            <div>└── <span className="text-indigo-400">reporting/</span></div>
            <div>    ├── TerminalReporter.java        <span className="text-slate-500">// Clean ANSI terminal summary</span></div>
            <div>    ├── JsonReporter.java            <span className="text-slate-500">// Machine-readable CI JSON</span></div>
            <div>    └── HtmlReporter.java            <span className="text-slate-500">// Standalone interactive HTML</span></div>
          </div>
        </div>
      )}
    </div>
  );
};
