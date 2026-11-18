package comp3607_project.pdf_exporter.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import comp3607_project.pdf_exporter.SampleMetrics;
import comp3607_project.pdf_exporter.metrics.ReportMetrics;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ReportMetricsViewTest {

    @Test
    void viewExposesEveryMetricUnchanged() {
        ReportMetrics m = SampleMetrics.typical();

        ReportMetricsView view = ReportMetricsView.of(m);

        assertEquals(90_000, view.activeTimeMillis());
        assertEquals(30_000, view.idleTimeMillis());
        assertEquals(Map.of("SYNTAX", 2, "TYPE", 1), view.errorFrequencyByCategory());
        assertEquals(Map.of("0-30s", 2, "30-60s", 1), view.errorIntervalBuckets());
        assertEquals(List.of(false, true, false, true), view.compilationOutcomes());
        assertEquals(List.of("00:00:01 - CLASS_CREATED: Dog"), view.timelineEntries());
        assertEquals(4, view.totalCompilations());
        assertEquals(2, view.successfulCompilations());
        assertEquals(2, view.failedCompilations());
        assertEquals(3, view.totalRuns());
        assertEquals(2, view.completedRuns());
        assertEquals(1, view.exceptionRuns());
        assertEquals(0, view.terminatedRuns());
    }

    @Test
    void viewReportsTheSameDerivedRatesAsTheMetrics() {
        ReportMetrics m = SampleMetrics.typical();

        ReportMetricsView view = ReportMetricsView.of(m);

        assertEquals(50.0, view.compileSuccessRate(), 1e-9);
        assertEquals(m.compileToRunRatio(), view.compileToRunRatio(), 1e-9);
    }
}
