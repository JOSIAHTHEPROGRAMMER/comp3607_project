package comp3607_project.pdf_exporter;


import java.io.File;
import java.io.IOException;
import java.time.ZoneId;

import comp3607_project.model.EventSink;
import comp3607_project.pdf_exporter.charts.BarChartRenderer;
import comp3607_project.pdf_exporter.charts.ChartRenderer;
import comp3607_project.pdf_exporter.charts.LineChartRenderer;
import comp3607_project.pdf_exporter.charts.PieChartRenderer;
import comp3607_project.pdf_exporter.format.HumanReadableDurationFormatter;
import comp3607_project.pdf_exporter.journal.InteractionJournal;
import comp3607_project.pdf_exporter.journal.ReportEventSink;
import comp3607_project.pdf_exporter.journal.ReportInteractionObserver;
import comp3607_project.pdf_exporter.journal.TelemetryJournal;
import comp3607_project.pdf_exporter.metrics.DefaultMetricsAggregator;
import comp3607_project.pdf_exporter.metrics.ErrorIntervalHistogramBuilder;
import comp3607_project.pdf_exporter.metrics.FixedBucketHistogramBuilder;
import comp3607_project.pdf_exporter.metrics.IdleTimeCalculator;
import comp3607_project.pdf_exporter.metrics.InteractionIdleTimeCalculator;
import comp3607_project.pdf_exporter.metrics.ReportMetrics;
import comp3607_project.pdf_exporter.pdf.PdfBoxWriter;
import comp3607_project.pdf_exporter.pdf.ReportBuilder;
import comp3607_project.pdf_exporter.pdf.ReportMetricsView;

public class TelemetryReportFacade {

    public static final long DEFAULT_IDLE_THRESHOLD_MS = 5_000;

    private final TelemetryJournal telemetryJournal;
    private final InteractionJournal interactionJournal;
    private final ReportEventSink eventSink;
    private final ReportInteractionObserver interactionObserver;
    private final DefaultMetricsAggregator aggregator;
    private final ZoneId zone;

    public TelemetryReportFacade(long startedAtMillis) {
        this(startedAtMillis, null);
    }

    public TelemetryReportFacade(long startedAtMillis, EventSink delegate) {
        this(startedAtMillis,
             new InteractionIdleTimeCalculator(DEFAULT_IDLE_THRESHOLD_MS),
             new FixedBucketHistogramBuilder(),
             ZoneId.systemDefault(),
             delegate);
    }

    public TelemetryReportFacade(long startedAtMillis,
                                 IdleTimeCalculator idleCalculator,
                                 ErrorIntervalHistogramBuilder histogramBuilder,
                                 ZoneId zone) {
        this(startedAtMillis, idleCalculator, histogramBuilder, zone, null);
    }

    public TelemetryReportFacade(long startedAtMillis,
                                 IdleTimeCalculator idleCalculator,
                                 ErrorIntervalHistogramBuilder histogramBuilder,
                                 ZoneId zone,
                                 EventSink delegate) {
        this.zone = zone;
        this.telemetryJournal   = new TelemetryJournal(startedAtMillis);
        this.interactionJournal = new InteractionJournal();
        this.eventSink          = new ReportEventSink(delegate, telemetryJournal, interactionJournal);
        this.interactionObserver = new ReportInteractionObserver(interactionJournal);
        this.aggregator = new DefaultMetricsAggregator(idleCalculator, histogramBuilder, zone);
    }

    public EventSink sinkForTrackers() {
        return eventSink;
    }

    public ReportInteractionObserver interactionObserver() {
        return interactionObserver;
    }

    public void endSession(long endedAtMillis) {
        telemetryJournal.end(endedAtMillis);
    }

    public File export(File output) throws IOException {
        ReportMetrics metrics = aggregator.aggregate(telemetryJournal, interactionJournal);
        ReportMetricsView view = ReportMetricsView.of(metrics);

        ChartRenderer pie = new PieChartRenderer(
                "Compilation Errors by Category",
                metrics.errorFrequencyByCategory(), 700, 400);
        ChartRenderer histogram = new BarChartRenderer(
                "Time Between Compilation Errors", "Interval", "Errors",
                metrics.errorIntervalBuckets(), 700, 400);
        ChartRenderer line = new LineChartRenderer(
                "Cumulative Compilation Outcomes", "Attempt", "Count",
                metrics.compilationOutcomes(), 700, 400);

        String[][] qualityRows = {
                {"Metric",               "Value"},
                {"Total Compilations",   String.valueOf(metrics.totalCompilations())},
                {"Successful",           String.valueOf(metrics.successfulCompilations())},
                {"Failed",               String.valueOf(metrics.failedCompilations())},
                {"Compile Success Rate", String.format("%.1f%%", metrics.compileSuccessRate())},
                {"Total Program Runs",   String.valueOf(metrics.totalRuns())},
                {"Compile-to-Run Ratio", String.format("%.2f", metrics.compileToRunRatio())}
        };

        ReportBuilder builder = new ReportBuilder(new PdfBoxWriter(), view,
                new HumanReadableDurationFormatter());

        // ---------- Page 1: summary --------------------------------------------
        builder.title("BlueJ Session Telemetry Report")
               .paragraph("1. Active Coding Time vs. Idle Time")
               .activeIdleBar()
               .paragraph("")
               .paragraph("Session KPIs")
               .paragraph("Total compilations:       " + metrics.totalCompilations(), 11)
               .paragraph("Successful compilations:  " + metrics.successfulCompilations(), 11)
               .paragraph("Failed compilations:      " + metrics.failedCompilations(), 11)
               .paragraph("Total program runs:       " + metrics.totalRuns(), 11)
               .paragraph(String.format("Compile success rate:     %.1f%%", metrics.compileSuccessRate()), 11)
               .paragraph(String.format("Compile-to-run ratio:     %.2f", metrics.compileToRunRatio()), 11)

        // ---------- Page 2: charts ---------------------------------------------
               .page()
               .title("2. Compilation Error Analysis")
               .chart(pie)
               .chart(histogram)

        // ---------- Page 3: quality table --------------------------------------
               .page()
               .title("3. Compilation Progression & Quality")
               .chart(line)
               .paragraph("Compile Quality Metrics")
               .paragraph("")
               .table(qualityRows);

        // ---------- Page 4: timeline (skipped when empty) ----------------------
        if (!metrics.timelineEntries().isEmpty()) {
            builder.page().title("4. Progression Timeline")
                   .paragraph("Class creations, modifications, and renames:", 11);

            int shown = 0;
            for (String entry : metrics.timelineEntries()) {
                if (shown++ >= 30) break;
                builder.paragraph("• " + entry, 10);
            }
            if (metrics.timelineEntries().size() > 30) {
                builder.paragraph("... and "
                        + (metrics.timelineEntries().size() - 30) + " more entries.", 10);
            }
        }

        return builder.build(output);   
    }
}
