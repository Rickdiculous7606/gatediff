package io.gatediff.reporting;

import io.gatediff.model.FieldMismatch;
import io.gatediff.model.TestExecutionResult;
import io.gatediff.model.TestSuiteReport;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Generates an interactive, zero-dependency HTML migration report for human review and CI artifacts.
 */
public class HtmlReporter {

    public static String generateHtml(TestSuiteReport report) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
        sb.append("<meta charset=\"UTF-8\" />\n<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n");
        sb.append("<title>GateDiff Report - ").append(escapeHtml(report.getProjectName())).append("</title>\n");
        sb.append("<style>\n");
        sb.append("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background: #0f172a; color: #f8fafc; margin: 0; padding: 2rem; }\n");
        sb.append(".container { max-width: 1200px; margin: 0 auto; }\n");
        sb.append(".header { background: #1e293b; padding: 1.5rem; border-radius: 8px; border: 1px solid #334155; margin-bottom: 1.5rem; }\n");
        sb.append(".title { font-size: 1.5rem; font-weight: bold; margin: 0 0 0.5rem 0; color: #38bdf8; }\n");
        sb.append(".stats { display: flex; gap: 1rem; margin-top: 1rem; flex-wrap: wrap; }\n");
        sb.append(".stat-card { background: #0f172a; padding: 0.75rem 1.25rem; border-radius: 6px; border: 1px solid #334155; min-width: 120px; }\n");
        sb.append(".stat-val { font-size: 1.25rem; font-weight: bold; }\n");
        sb.append(".stat-lbl { font-size: 0.75rem; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.05em; }\n");
        sb.append(".card { background: #1e293b; border-radius: 8px; border: 1px solid #334155; margin-bottom: 1rem; overflow: hidden; }\n");
        sb.append(".card-header { padding: 1rem; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #334155; }\n");
        sb.append(".badge { font-size: 0.75rem; font-weight: 700; padding: 0.25rem 0.6rem; border-radius: 4px; text-transform: uppercase; }\n");
        sb.append(".badge-pass { background: #065f46; color: #34d399; }\n");
        sb.append(".badge-fail { background: #991b1b; color: #f87171; }\n");
        sb.append(".badge-err  { background: #854d0e; color: #fde047; }\n");
        sb.append(".card-body { padding: 1rem; }\n");
        sb.append(".diff-box { background: #0f172a; padding: 0.75rem; border-radius: 4px; font-family: monospace; font-size: 0.85rem; border-left: 3px solid #ef4444; margin-top: 0.5rem; }\n");
        sb.append(".grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-top: 0.75rem; }\n");
        sb.append(".pane { background: #0f172a; padding: 0.75rem; border-radius: 4px; font-family: monospace; font-size: 0.8rem; overflow-x: auto; max-height: 250px; }\n");
        sb.append("</style>\n</head>\n<body>\n<div class=\"container\">\n");

        sb.append("<div class=\"header\">\n");
        sb.append("  <div class=\"title\">GateDiff Migration Report: ").append(escapeHtml(report.getProjectName())).append("</div>\n");
        sb.append("  <div style=\"color:#94a3b8; font-size:0.875rem;\">Generated at ").append(report.getTimestamp()).append("</div>\n");
        sb.append("  <div class=\"stats\">\n");
        sb.append("    <div class=\"stat-card\"><div class=\"stat-val\">").append(report.getTotalTests()).append("</div><div class=\"stat-lbl\">Total Tests</div></div>\n");
        sb.append("    <div class=\"stat-card\"><div class=\"stat-val\" style=\"color:#34d399;\">").append(report.getPassedCount()).append("</div><div class=\"stat-lbl\">Passed</div></div>\n");
        sb.append("    <div class=\"stat-card\"><div class=\"stat-val\" style=\"color:#f87171;\">").append(report.getFailedCount()).append("</div><div class=\"stat-lbl\">Failed</div></div>\n");
        sb.append("    <div class=\"stat-card\"><div class=\"stat-val\" style=\"color:#fde047;\">").append(report.getErrorCount()).append("</div><div class=\"stat-lbl\">Errors</div></div>\n");
        sb.append("    <div class=\"stat-card\"><div class=\"stat-val\">").append(report.getTotalDurationMs()).append("ms</div><div class=\"stat-lbl\">Duration</div></div>\n");
        sb.append("  </div>\n");
        sb.append("</div>\n");

        for (TestExecutionResult r : report.getResults()) {
            String badgeClass = r.isPassed() ? "badge-pass" : (r.isExecutionError() ? "badge-err" : "badge-fail");
            String badgeText = r.isPassed() ? "PASS" : (r.isExecutionError() ? "ERROR" : "FAIL");

            sb.append("<div class=\"card\">\n");
            sb.append("  <div class=\"card-header\">\n");
            sb.append("    <div><strong>").append(escapeHtml(r.getServiceId())).append("</strong> &bull; <code>").append(r.getRequest().getMethod()).append(" ").append(escapeHtml(r.getRequest().getPath())).append("</code></div>\n");
            sb.append("    <div><span class=\"badge ").append(badgeClass).append("\">").append(badgeText).append("</span></div>\n");
            sb.append("  </div>\n");
            sb.append("  <div class=\"card-body\">\n");

            if (r.isExecutionError()) {
                sb.append("    <div class=\"diff-box\"><strong>Execution Error:</strong> ").append(escapeHtml(r.getExecutionErrorMessage())).append("</div>\n");
            } else if (!r.isPassed() && r.getComparison() != null) {
                for (FieldMismatch m : r.getComparison().getMismatches()) {
                    sb.append("    <div class=\"diff-box\"><strong>").append(escapeHtml(m.getPath())).append("</strong>: ").append(escapeHtml(m.getMessage())).append("</div>\n");
                }
            }

            if (r.getSourceResponse() != null && r.getTargetResponse() != null) {
                sb.append("    <div class=\"grid\">\n");
                sb.append("      <div>\n");
                sb.append("        <div style=\"font-size:0.75rem; color:#94a3b8; margin-bottom:0.25rem;\">SOURCE (HTTP ").append(r.getSourceResponse().getStatusCode()).append(" - ").append(r.getSourceResponse().getDurationMs()).append("ms)</div>\n");
                sb.append("        <pre class=\"pane\">").append(escapeHtml(r.getSourceResponse().getBody())).append("</pre>\n");
                sb.append("      </div>\n");
                sb.append("      <div>\n");
                sb.append("        <div style=\"font-size:0.75rem; color:#94a3b8; margin-bottom:0.25rem;\">TARGET (HTTP ").append(r.getTargetResponse().getStatusCode()).append(" - ").append(r.getTargetResponse().getDurationMs()).append("ms)</div>\n");
                sb.append("        <pre class=\"pane\">").append(escapeHtml(r.getTargetResponse().getBody())).append("</pre>\n");
                sb.append("      </div>\n");
                sb.append("    </div>\n");
            }

            sb.append("  </div>\n");
            sb.append("</div>\n");
        }

        sb.append("</div>\n</body>\n</html>\n");
        return sb.toString();
    }

    public static void writeToFile(TestSuiteReport report, File destination) throws IOException {
        if (destination.getParentFile() != null && !destination.getParentFile().exists()) {
            destination.getParentFile().mkdirs();
        }
        try (FileWriter fw = new FileWriter(destination)) {
            fw.write(generateHtml(report));
        }
    }

    private static String escapeHtml(String str) {
        if (str == null) return "";
        return str.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
