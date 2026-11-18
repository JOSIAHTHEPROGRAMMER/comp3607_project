package comp3607_project.pdf_exporter.pdf;

import java.util.List;
import java.util.Map;

import comp3607_project.pdf_exporter.metrics.ReportMetrics;

public interface ReportMetricsView {
    long activeTimeMillis();

    long idleTimeMillis();

    Map<String, Integer> errorFrequencyByCategory();

    Map<String, Integer> errorIntervalBuckets();

    List<Boolean> compilationOutcomes();

    List<String> timelineEntries();

    int totalCompilations();

    int successfulCompilations();

    int failedCompilations();

    int totalRuns();

    int completedRuns();

    int exceptionRuns();

    int terminatedRuns();

    double compileSuccessRate();

    double compileToRunRatio();

    static ReportMetricsView of(ReportMetrics m) {
        return new ReportMetricsView() {
            @Override
            public long activeTimeMillis() {
                return m.activeTimeMillis();
            }

            @Override
            public long idleTimeMillis() {
                return m.idleTimeMillis();
            }

            @Override
            public Map<String, Integer> errorFrequencyByCategory() {
                return m.errorFrequencyByCategory();
            }

            @Override
            public Map<String, Integer> errorIntervalBuckets() {
                return m.errorIntervalBuckets();
            }

            @Override
            public List<Boolean> compilationOutcomes() {
                return m.compilationOutcomes();
            }

            @Override
            public List<String> timelineEntries() {
                return m.timelineEntries();
            }

            @Override
            public int totalCompilations() {
                return m.totalCompilations();
            }

            @Override
            public int successfulCompilations() {
                return m.successfulCompilations();
            }

            @Override
            public int failedCompilations() {
                return m.failedCompilations();
            }

            @Override
            public int totalRuns() {
                return m.totalRuns();
            }

            @Override
            public int completedRuns() {
                return m.completedRuns();
            }

            @Override
            public int exceptionRuns() {
                return m.exceptionRuns();
            }

            @Override
            public int terminatedRuns() {
                return m.terminatedRuns();
            }

            @Override
            public double compileSuccessRate() {
                return m.compileSuccessRate();
            }

            @Override
            public double compileToRunRatio() {
                return m.compileToRunRatio();
            }
        };
    }
}