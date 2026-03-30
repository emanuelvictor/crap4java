package crap4java;

import java.util.List;
import java.util.Objects;

public class ClassMetrics {

    private final String className;
    private final Double sumOfCoverage;
    private final Double sumOfCrapScore;
    private final Double sumOfComplexity;
    private final List<MethodMetrics> methodMetrics;

    public ClassMetrics(String className, List<MethodMetrics> methodMetrics) {
        this.className = className;
        this.sumOfCoverage = sumOfCoverage(methodMetrics);
        this.sumOfCrapScore = sumCrapScore(methodMetrics);
        this.sumOfComplexity = sumComplexity(methodMetrics);
        this.methodMetrics = methodMetrics;
    }

    private static Double sumComplexity(List<MethodMetrics> methodMetrics) {
        return methodMetrics.stream()
                .map(MethodMetrics::complexity)
                .filter(Objects::nonNull)
                .mapToDouble(complexity -> complexity)
                .sum();
    }

    private static Double sumCrapScore(List<MethodMetrics> methodMetrics) {
        return methodMetrics.stream()
                .map(MethodMetrics::crapScore)
                .filter(Objects::nonNull)
                .mapToDouble(complexity -> complexity)
                .sum();
    }

    private static Double sumOfCoverage(List<MethodMetrics> methodMetrics) {
        Double coverage = 100.0;
        for (MethodMetrics methodMetric : methodMetrics) {
            if (methodMetric.coveragePercent() < coverage) {
                coverage = coverage - (100.0 - methodMetric.coveragePercent());
            }
        }
        return coverage;
    }

    public Double getSumOfCrapScore() {
        return sumOfCrapScore;
    }

    public String getClassName() {
        return className;
    }

    public Double getSumOfComplexity() {
        return sumOfComplexity;
    }


    public List<MethodMetrics> getMethodMetrics() {
        return methodMetrics;
    }

    public  Double getSumOfCoverage() {
        return sumOfCoverage;
    }

}
