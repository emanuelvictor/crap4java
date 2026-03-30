package crap4java;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportFormatterTest {

    @Test
    void formatsExactReportWithScoresAndNaValues() {
        MethodMetrics scored = new MethodMetrics("foo", "demo.Sample", 3, 85.0, 4.5);
        MethodMetrics unknown = new MethodMetrics("bar", "demo.Sample", 2, null, null);

        final var classesMetrics = new ArrayList<ClassMetrics>();
        classesMetrics.add(new ClassMetrics("demo.Sample", List.of(scored, unknown)));

        String report = ReportFormatter.format(classesMetrics);

        String header = String.format("%-30s %-35s %4s %7s %8s", "Method", "Class", "CC", "Cov%", "CRAP");
        String separator = "-".repeat(header.length());
        String expected = """
                CRAP Report for class: demo.Sample
                ===========
                %s
                %s
                %-30s %-35s %4d %7s %8s
                %-30s %-35s %4d %7s %8s
                """.formatted(
                header,
                separator,
                "foo",
                "demo.Sample",
                3,
                "85,0%",
                "4,5",
                "bar",
                "demo.Sample",
                2,
                "   0,0%",
                "     N/A");

        assertEquals(expected, report);
    }

    @Test
    void sortsScoredEntriesAheadOfNaEntriesAndHigherScoresFirst() {
        MethodMetrics lowerScore = new MethodMetrics("low", "demo.Sample", 2, 100.0, 2.0);
        MethodMetrics unknown = new MethodMetrics("unknown", "demo.Sample", 2, null, null);
        MethodMetrics higherScore = new MethodMetrics("high", "demo.Sample", 5, 10.0, 9.0);
        final var classesMetrics = new ArrayList<ClassMetrics>();
        classesMetrics.add(new ClassMetrics("demo.Sample", List.of(lowerScore, unknown, higherScore)));

        String report = ReportFormatter.format(classesMetrics);

        assertTrue(report.indexOf("high") < report.indexOf("low"));
        assertTrue(report.indexOf("low") < report.indexOf("unknown"));
    }
}
