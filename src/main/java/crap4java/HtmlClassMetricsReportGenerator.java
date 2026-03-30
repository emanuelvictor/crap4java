package crap4java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class HtmlClassMetricsReportGenerator {

    public static void generate(Collection<ClassMetrics> classMetrics, Path output) throws IOException {
        ClassStats classStats = ClassStats.from(classMetrics);
        MethodsStats methodsStats = MethodsStats.from(classMetrics.stream()
                .flatMap(cm -> cm.getMethodMetrics().stream())
                .toList());
        StringBuilder sb = new StringBuilder();

        sb.append("""
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head>
                    <meta charset="UTF-8">
                    <title>CRAP Report</title>
                    <style>
                        body { font-family: sans-serif; padding: 2rem; background: #f5f5f5; }
                        h1, h2 { color: #333; }
                        table { width: 100%; border-collapse: collapse; background: white; margin-bottom: 2rem; }
                        th { background: #333; color: white; padding: 8px; text-align: left; cursor: pointer; user-select: none; }
                        th:hover { background: #555; }
                        td { padding: 10px; border-bottom: 1px solid #ddd; }
                        tr:hover td { background: #f0f0f0; }
                        .crappy { background: #ffe0e0; }
                        .warn   { background: #fff8e1; }
                        .ok     { background: #e8f5e9; }
                        .stats-grid {
                            display: grid;
                            grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
                            gap: 1rem;
                            margin-bottom: 2rem;
                        }
                        .card { background: white; border-radius: 8px; padding: 1rem 1.2rem; box-shadow: 0 1px 4px rgba(0,0,0,0.1); }
                        .card .label { font-size: 0.75rem; color: #888; text-transform: uppercase; margin-bottom: 4px; }
                        .card .value { font-size: 1.6rem; font-weight: bold; color: #333; }
                        .card.red .value    { color: #c0392b; }
                        .card.yellow .value { color: #e67e22; }
                        .card.green .value  { color: #27ae60; }
                        .legend { display: flex; gap: 1rem; margin-bottom: 1rem; font-size: 0.85rem; }
                        .legend span { display: flex; align-items: center; gap: 4px; }
                        .dot { width: 12px; height: 12px; border-radius: 50%; display: inline-block; }
                        details { margin-bottom: 1rem; background: white; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.1); overflow: hidden; }
                        summary { padding: 1rem; cursor: pointer; font-weight: bold; font-size: 0.95rem; list-style: none; display: flex; align-items: center; gap: 0.75rem; }
                        summary:hover { background: #f9f9f9; }
                        summary .badge { font-size: 0.75rem; padding: 2px 8px; border-radius: 999px; font-weight: normal; }
                        summary .badge.crappy { background: #ffe0e0; color: #c0392b; }
                        summary .badge.warn   { background: #fff8e1; color: #e67e22; }
                        summary .badge.ok     { background: #e8f5e9; color: #27ae60; }
                        details table { margin-bottom: 0; box-shadow: none; }
                    </style>
                </head>
                <body>
                    <h1>CRAP Report</h1>
                """);

        // --- Stats cards ---
        sb.append(String.format("""
                            <div class="stats-grid">
                                <div class="card"><div class="label">Total Classes</div><div class="value">%d</div></div>
                                <div class="card red"><div class="label">Crappy (&gt; 30)</div><div class="value">%d</div></div>
                                <div class="card yellow"><div class="label">Warning (10–30)</div><div class="value">%d</div></div>
                                <div class="card green"><div class="label">OK (≤ 10)</div><div class="value">%d</div></div>
                                <div class="card"><div class="label">Avg CRAP Score</div><div class="value">%.1f</div></div>
                                <div class="card"><div class="label">Avg Coverage</div><div class="value">%.1f%%</div></div>
                                <div class="card"><div class="label">Avg Complexity</div><div class="value">%.1f</div></div>
                            </div>
                            <div class="stats-grid">
                                <div class="card"><div class="label">Worst Class</div><div class="value" style="font-size:0.95rem">%s</div></div>
                            </div>
                        """,
                classStats.total, classStats.crappy, classStats.warn, classStats.ok,
                classStats.avgCrap, classStats.avgCoverage, classStats.avgComplexity,
                classStats.worstClass
        ));

        sb.append("""
                    <div class="legend">
                        <span><span class="dot"></span> <b>Classes:</b> </span>
                        <span><span class="dot" style="background:#e74c3c"></span> Crappy (> 30)</span>
                        <span><span class="dot" style="background:#f39c12"></span> Warning (20–30)</span>
                        <span><span class="dot" style="background:#2ecc71"></span> OK (≤ 20)</span>
                    </div>
                """);

        sb.append(String.format("""
                            <div class="stats-grid">
                                <div class="card"><div class="label">Total of methods</div><div class="value">%d</div></div>
                                <div class="card red"><div class="label">Crappy (&gt; 15)</div><div class="value">%d</div></div>
                                <div class="card yellow"><div class="label">Warning (8–15)</div><div class="value">%d</div></div>
                                <div class="card green"><div class="label">OK (≤ 8)</div><div class="value">%d</div></div>
                                <div class="card"><div class="label">Avg CRAP Score</div><div class="value">%.1f</div></div>
                                <div class="card"><div class="label">Avg Coverage</div><div class="value">%.1f%%</div></div>
                                <div class="card"><div class="label">Avg Complexity</div><div class="value">%.1f</div></div>
                            </div>
                            <div class="stats-grid">
                                <div class="card"><div class="label">Worst Method</div><div class="value" style="font-size:0.95rem">%s</div></div>
                            </div>
                        """,
                methodsStats.total, methodsStats.crappy, methodsStats.warn, methodsStats.ok,
                methodsStats.avgCrap, methodsStats.avgCoverage, methodsStats.avgComplexity,
                methodsStats.worstMethod
        ));

        // --- Legend ---
        sb.append("""
                    <div class="legend">
                        <span><span class="dot"></span> <b>Methods:</b> </span>
                        <span><span class="dot" style="background:#e74c3c"></span> Crappy (> 15)</span>
                        <span><span class="dot" style="background:#f39c12"></span> Warning (8–15)</span>
                        <span><span class="dot" style="background:#2ecc71"></span> OK (≤ 8)</span>
                    </div>
                """);

        // --- Classes agrupadas com métodos expansíveis ---
        sb.append("<h2>Classes</h2>\n");

        classMetrics.stream()
                .sorted(Comparator.comparingDouble(c -> -(c.getSumOfCrapScore() != null ? c.getSumOfCrapScore() : 0)))
                .forEach(cls -> {
                    String rowClass = crapClass(cls.getSumOfCrapScore());
                    String crapLabel = cls.getSumOfCrapScore() != null
                            ? String.format("CRAP SUM %.2f", cls.getSumOfCrapScore())
                            : "No CRAP score";

                    sb.append(String.format("""
                                    <details>
                                        <summary>
                                            %s
                                            <span class="badge %s">%s</span>
                                            <span style="color:#888;font-weight:normal;font-size:0.85rem">
                                                CC SUM: %.1f%% &nbsp;|&nbsp; Coverage AVG: %.1f%%
                                            </span>
                                        </summary>
                                    """,
                            cls.getClassName(),
                            rowClass, crapLabel,
                            cls.getSumOfComplexity(),
                            cls.getAverageOfCoverage()
                    ));

                    // Tabela de métodos dentro do details
                    sb.append("""
                                <table>
                                    <thead>
                                        <tr>
                                            <th>Method</th>
                                            <th>Complexity (CC)</th>
                                            <th>Coverage (%)</th>
                                            <th>CRAP Score</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                            """);

                    cls.getMethodMetrics().stream()
                            .filter(Objects::nonNull)
                            .sorted(Comparator.comparingDouble(m -> -(m.crapScore() != null ? m.crapScore() : 0)))
                            .forEach(m -> {
                                if (m.crapScore() != null) {
                                    sb.append(String.format("""
                                                            <tr class="%s">
                                                                <td>%s</td>
                                                                <td>%d</td>
                                                                <td>%.1f%%</td>
                                                                <td>%.2f</td>
                                                            </tr>
                                                    """,
                                            crapMethod(m.crapScore()),
                                            m.methodName(),
                                            m.complexity(),
                                            m.coveragePercent(),
                                            m.crapScore()
                                    ));
                                } else {
                                    sb.append(String.format("""
                                                            <tr>
                                                                <td>%s</td>
                                                                <td>%d</td>
                                                                <td>%.1f%%</td>
                                                                <td>No CRAP score</td>
                                                            </tr>
                                                    """,
                                            m.methodName(),
                                            m.complexity(),
                                            m.coveragePercent()
                                    ));
                                }
                            });

                    sb.append("""
                                    </tbody>
                                </table>
                            </details>
                            """);
                });

        sb.append("""
                </body>
                </html>
                """);

        Files.writeString(output, sb.toString(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    private record ClassStats(
            int total, int crappy, int warn, int ok,
            double avgCrap, double avgCoverage, double avgComplexity,
            String worstClass
    ) {
        static ClassStats from(Collection<ClassMetrics> metrics) {
            int total = metrics.size();
            int crappy = (int) metrics.stream().filter(c -> c.getSumOfCrapScore() != null && c.getSumOfCrapScore() > 30).count();
            int warn = (int) metrics.stream().filter(c -> c.getSumOfCrapScore() != null && c.getSumOfCrapScore() > 20 && c.getSumOfCrapScore() <= 30).count();
            int ok = total - crappy - warn;
            double avgCrap = metrics.stream().filter(c -> c.getSumOfCrapScore() != null).mapToDouble(ClassMetrics::getSumOfCrapScore).average().orElse(0);
            double avgCoverage = metrics.stream().filter(c -> c.getAverageOfCoverage() != null).mapToDouble(ClassMetrics::getAverageOfCoverage).average().orElse(0);
            double avgComplexity = metrics.stream().filter(c -> c.getSumOfComplexity() != null).mapToDouble(ClassMetrics::getSumOfComplexity).average().orElse(0);
            String worstClass = metrics.stream().filter(c -> c.getSumOfCrapScore() != null)
                    .max(Comparator.comparingDouble(ClassMetrics::getSumOfCrapScore))
                    .map(ClassMetrics::getClassName)
                    .orElse("N/A");
            return new ClassStats(total, crappy, warn, ok, avgCrap, avgCoverage, avgComplexity, worstClass);
        }
    }

    private static String crapMethod(Double crapScore) {
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

    private static String crapClass(Double crapScore) {
        if (crapScore == null) return "";
        if (crapScore > 30) return "crappy";
        if (crapScore > 20) return "warn";
        return "ok";
    }
}