package comp3607_project.pdf_exporter;

import comp3607_project.pdf_exporter.metrics.ReportMetrics;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Ready-made report metrics shared by the PDF tests. */
public final class SampleMetrics {

    private SampleMetrics() {
    }

    /** 90s active, 30s idle, 4 compilations (2 ok), 3 runs (2 completed, 1 exception). */
    public static ReportMetrics typical() {
        Map<String, Integer> frequency = new LinkedHashMap<>();
        frequency.put("SYNTAX", 2);
        frequency.put("TYPE", 1);
        Map<String, Integer> buckets = new LinkedHashMap<>();
        buckets.put("0-30s", 2);
        buckets.put("30-60s", 1);
        return new ReportMetrics(90_000L, 30_000L, frequency, buckets,
                List.of(false, true, false, true),
                List.of("00:00:01 - CLASS_CREATED: Dog"),
                4, 2, 2, 3, 2, 1, 0);
    }

    /** Same numbers as typical() but with the given timeline (which may be null or empty). */
    public static ReportMetrics withTimeline(List<String> timeline) {
        ReportMetrics t = typical();
        return new ReportMetrics(t.activeTimeMillis(), t.idleTimeMillis(),
                t.errorFrequencyByCategory(), t.errorIntervalBuckets(),
                t.compilationOutcomes(), timeline,
                t.totalCompilations(), t.successfulCompilations(), t.failedCompilations(),
                t.totalRuns(), t.completedRuns(), t.exceptionRuns(), t.terminatedRuns());
    }

    /** A session in which nothing happened. */
    public static ReportMetrics empty() {
        return new ReportMetrics(0L, 0L, Map.of(), Map.of(), List.of(), List.of(),
                0, 0, 0, 0, 0, 0, 0);
    }
}
