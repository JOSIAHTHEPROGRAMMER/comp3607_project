package comp3607_project.pdf_exporter.pdf;


import comp3607_project.pdf_exporter.metrics.ReportMetrics;

import java.util.List;
import java.util.Map;

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
            public long activeTimeMillis()                        { return m.activeTimeMillis(); }
            public long idleTimeMillis()                          { return m.idleTimeMillis(); }
            public Map<String, Integer> errorFrequencyByCategory(){ return m.errorFrequencyByCategory(); }
            public Map<String, Integer> errorIntervalBuckets()    { return m.errorIntervalBuckets(); }
            public List<Boolean> compilationOutcomes()            { return m.compilationOutcomes(); }
            public List<String> timelineEntries()                 { return m.timelineEntries(); }
            public int totalCompilations()                        { return m.totalCompilations(); }
            public int successfulCompilations()                   { return m.successfulCompilations(); }
            public int failedCompilations()                       { return m.failedCompilations(); }
            public int totalRuns()                                { return m.totalRuns(); }
            public int completedRuns()                            { return m.completedRuns(); }
            public int exceptionRuns()                            { return m.exceptionRuns(); }
            public int terminatedRuns()                           { return m.terminatedRuns(); }
            public double compileSuccessRate()                    { return m.compileSuccessRate(); }
            public double compileToRunRatio()                     { return m.compileToRunRatio(); }
        };
    }
}