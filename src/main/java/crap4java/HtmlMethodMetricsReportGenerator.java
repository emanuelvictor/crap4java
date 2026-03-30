package crap4java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class HtmlMethodMetricsReportGenerator {

    public static void generate(List<MethodMetrics> metrics, Path output) throws IOException {
        MethodsStats methodsStats = MethodsStats.from(metrics);
        StringBuilder sb = new StringBuilder();

        sb.append("""
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head>
                    <meta charset="UTF-8">
                    <title>CRAP Report</title>
                    <style>
                        body { font-family: sans-serif; padding: 2rem; background: #f5f5f5; }
                        h1 { color: #333; }
                        table { width: 100%; border-collapse: collapse; background: white; }
                        th { background: #333; color: white; padding: 5px; text-align: left; cursor: pointer; user-select: none; }
                        th:hover { background: #555; }
                        td { padding: 10px; border-bottom: 1px solid #ddd; }
                        tr:hover td { background: #f0f0f0; }
                        .crappy { background: #ffe0e0; }
                        .warn   { background: #fff8e1; }
                        .ok     { background: #e8f5e9; }
                        .methodsStats-grid {
                            display: grid;
                            grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
                            gap: 1rem;
                            margin-bottom: 2rem;
                        }
                        .card {
                            background: white;
                            border-radius: 8px;
                            padding: 1rem 1.2rem;
                            box-shadow: 0 1px 4px rgba(0,0,0,0.1);
                        }
                        .card .label { font-size: 0.75rem; color: #888; text-transform: uppercase; margin-bottom: 4px; }
                        .card .value { font-size: 1.6rem; font-weight: bold; color: #333; }
                        .card.red .value   { color: #c0392b; }
                        .card.yellow .value { color: #e67e22; }
                        .card.green .value  { color: #27ae60; }
                        .legend { display: flex; gap: 1rem; margin-bottom: 1rem; font-size: 0.85rem; }
                        .legend span { display: flex; align-items: center; gap: 4px; }
                        .dot { width: 12px; height: 12px; border-radius: 50%; display: inline-block; }
                    </style>
                </head>
                <body>
                    <h1>CRAP Report</h1>
                """);

        // --- Stats cards ---
        sb.append(String.format("""
                            <div class="methodsStats-grid">
                                <div class="card"><div class="label">Total Methods</div><div class="value">%d</div></div>
                                <div class="card red"><div class="label">Crappy (&gt; 15)</div><div class="value">%d</div></div>
                                <div class="card yellow"><div class="label">Warning (8–15)</div><div class="value">%d</div></div>
                                <div class="card green"><div class="label">OK (≤ 8)</div><div class="value">%d</div></div>
                                <div class="card"><div class="label">Avg CRAP Score</div><div class="value">%.1f</div></div>
                                <div class="card"><div class="label">Avg Coverage</div><div class="value">%.1f%%</div></div>
                                <div class="card"><div class="label">Avg Complexity</div><div class="value">%.1f</div></div>
                            </div>
                             <div class="methodsStats-grid">
                                <div class="card red"><div class="label">Worst Method</div><div class="value" style="font-size:0.95rem">%s</div></div>
                            </div>
                        """,
                methodsStats.total, methodsStats.crappy, methodsStats.warn, methodsStats.ok,
                methodsStats.avgCrap, methodsStats.avgCoverage, methodsStats.avgComplexity,
                methodsStats.worstMethod
        ));

        // --- Legend ---
        sb.append("""
                    <div class="legend">
                        <span><span class="dot" style="background:#e74c3c"></span> Crappy (> 15)</span>
                        <span><span class="dot" style="background:#f39c12"></span> Warning (8–15)</span>
                        <span><span class="dot" style="background:#2ecc71"></span> OK (≤ 8)</span>
                    </div>
                """);

        // --- Table ---
        sb.append("""
                    <table id="crapTable">
                        <thead>
                            <tr>
                                <th onclick="sortTable(0)">Class</th>
                                <th onclick="sortTable(1)">Method</th>
                                <th onclick="sortTable(2)">Complexity (CC)</th>
                                <th onclick="sortTable(3)">Coverage (%)</th>
                                <th onclick="sortTable(4)">CRAP Score</th>
                            </tr>
                        </thead>
                        <tbody>
                """);

        metrics.stream().filter(Objects::nonNull).filter(m -> m.crapScore() != null)
                .sorted(Comparator.comparingDouble(m -> -m.crapScore()))
                .forEach(m -> sb.append(String.format("""
                                        <tr class="%s">
                                            <td>%s</td>
                                            <td>%s</td>
                                            <td>%d</td>
                                            <td>%.1f%%</td>
                                            <td>%.2f</td>
                                        </tr>
                                """,
                        crapRowClass(m.crapScore()),
                        m.className(),
                        m.methodName(),
                        m.complexity(),
                        m.coveragePercent(),
                        m.crapScore()
                )));

        metrics.stream().filter(Objects::nonNull).filter(m -> m.crapScore() == null)
                .forEach(m -> sb.append(String.format("""
                                        <tr class="%s">
                                            <td>%s</td>
                                            <td>%s</td>
                                            <td>%d</td>
                                            <td>%.1f%%</td>
                                            <td>No crap score</td>
                                        </tr>
                                """,
                        "none",
                        m.className(),
                        m.methodName(),
                        m.complexity(),
                        m.coveragePercent()
                )));

        sb.append("""
                        </tbody>
                    </table>
                    <script>
                        let sortDir = {};
                        function sortTable(col) {
                            const table = document.getElementById("crapTable");
                            const rows = Array.from(table.tBodies[0].rows);
                            sortDir[col] = !sortDir[col];
                            rows.sort((a, b) => {
                                const av = a.cells[col].innerText.replace('%','');
                                const bv = b.cells[col].innerText.replace('%','');
                                const diff = isNaN(av) ? av.localeCompare(bv) : parseFloat(av) - parseFloat(bv);
                                return sortDir[col] ? diff : -diff;
                            });
                            rows.forEach(r => table.tBodies[0].appendChild(r));
                        }
                    </script>
                </body>
                </html>
                """);

        Files.writeString(output, sb.toString(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static String crapRowClass(Double crapScore) {
        if (crapScore == null) return "";
        if (crapScore > 15) return "crappy";
        if (crapScore > 8) return "warn";
        return "ok";
    }

    private record MethodsStats(
            int total, int crappy, int warn, int ok,
            double avgCrap, double avgCoverage, double avgComplexity,
            String worstMethod
    ) {
        static MethodsStats from(List<MethodMetrics> metrics) {
            int total = metrics.size();
            int crappy = (int) metrics.stream().filter(Objects::nonNull)
                    .filter(m -> m.crapScore() != null).filter(m -> m.crapScore() > 15).count();
            int warn = (int) metrics.stream().filter(Objects::nonNull).filter(m -> m.crapScore() != null)
                    .filter(m -> m.crapScore() > 8 && m.crapScore() <= 15).count();
            int ok = total - crappy - warn;
            double avgCrap = metrics.stream().filter(Objects::nonNull)
                    .filter(m -> m.crapScore() != null).mapToDouble(MethodMetrics::crapScore)
                    .average().orElse(0);
            double avgCoverage = metrics.stream().filter(Objects::nonNull)
                    .filter(m -> m.coveragePercent() != null).mapToDouble(MethodMetrics::coveragePercent)
                    .average().orElse(0);
            double avgComplexity = metrics.stream().filter(Objects::nonNull)
                    .filter(m -> m.complexity() != null)
                    .mapToDouble(MethodMetrics::complexity).average().orElse(0);
            String worstMethod = metrics.stream().filter(Objects::nonNull)
                    .filter(m -> m.crapScore() != null)
                    .max(Comparator.comparingDouble(MethodMetrics::crapScore))
                    .map(m -> m.className() + "." + m.methodName())
                    .orElse("N/A");
            return new MethodsStats(total, crappy, warn, ok, avgCrap, avgCoverage, avgComplexity, worstMethod);
        }
    }
}