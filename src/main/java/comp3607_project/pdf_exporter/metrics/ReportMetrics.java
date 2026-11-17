package comp3607_project.pdf_exporter.metrics;

import java.util.List;
import java.util.Map;

public record ReportMetrics(
        long activeTimeMillis,
        long idleTimeMillis,
        Map<String, Integer> errorFrequencyByCategory,
        Map<String, Integer> errorIntervalBuckets,
        List<Boolean> compilationOutcomes,
        List<String> timelineEntries,
        int totalCompilations,
        int successfulCompilations,
        int failedCompilations,
        int totalRuns,
        int completedRuns,
        int exceptionRuns,
        int terminatedRuns
) {
    public double compileSuccessRate() {
        return totalCompilations == 0 ? 0.0
                : successfulCompilations * 100.0 / totalCompilations;
    }

    public double compileToRunRatio() {
        return totalRuns == 0 ? 0.0 : (double) totalCompilations / totalRuns;
    }
}
