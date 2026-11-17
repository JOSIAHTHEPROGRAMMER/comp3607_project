package comp3607_project.pdf_exporter.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ReportMetricsTest {

    private static ReportMetrics counts(int compilations, int successes, int failures, int runs) {
        return new ReportMetrics(0, 0, Map.of(), Map.of(), List.of(), List.of(),
                compilations, successes, failures, runs, 0, 0, 0);
    }

    @Test
    void successRateIsZeroWhenNothingWasCompiled() {
        assertEquals(0.0, counts(0, 0, 0, 0).compileSuccessRate(), 1e-9);
    }

    @Test
    void successRateIsThePercentageOfSuccessfulCompilations() {
        assertEquals(75.0, counts(4, 3, 1, 0).compileSuccessRate(), 1e-9);
    }

    @Test
    void successRateIsZeroWhenEveryCompilationFailed() {
        assertEquals(0.0, counts(5, 0, 5, 0).compileSuccessRate(), 1e-9);
    }

    @Test
    void successRateIsOneHundredWhenEveryCompilationSucceeded() {
        assertEquals(100.0, counts(5, 5, 0, 0).compileSuccessRate(), 1e-9);
    }

    @Test
    void compileToRunRatioIsZeroWhenNothingWasRun() {
        assertEquals(0.0, counts(6, 3, 3, 0).compileToRunRatio(), 1e-9);
    }

    @Test
    void compileToRunRatioDividesCompilationsByRuns() {
        assertEquals(1.5, counts(6, 3, 3, 4).compileToRunRatio(), 1e-9);
    }

    @Test
    void recordExposesEveryValueItWasGiven() {
        ReportMetrics m = new ReportMetrics(90_000, 30_000, Map.of("SYNTAX", 2),
                Map.of("0-30s", 1), List.of(true, false), List.of("entry"),
                2, 1, 1, 5, 3, 1, 1);

        assertEquals(90_000, m.activeTimeMillis());
        assertEquals(30_000, m.idleTimeMillis());
        assertEquals(Map.of("SYNTAX", 2), m.errorFrequencyByCategory());
        assertEquals(Map.of("0-30s", 1), m.errorIntervalBuckets());
        assertEquals(List.of(true, false), m.compilationOutcomes());
        assertEquals(List.of("entry"), m.timelineEntries());
        assertEquals(2, m.totalCompilations());
        assertEquals(1, m.successfulCompilations());
        assertEquals(1, m.failedCompilations());
        assertEquals(5, m.totalRuns());
        assertEquals(3, m.completedRuns());
        assertEquals(1, m.exceptionRuns());
        assertEquals(1, m.terminatedRuns());
    }
}
