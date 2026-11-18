package comp3607_project.pdf_exporter;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;

import comp3607_project.model.EventSink;
import comp3607_project.pdf_exporter.charts.BarChartRenderer;
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
import comp3607_project.pdf_exporter.pdf.PdfReportGenerator;
import comp3607_project.pdf_exporter.pdf.PdfWriter;
import comp3607_project.pdf_exporter.pdf.ReportMetricsView;
import comp3607_project.pdf_exporter.pdf.ReportSection;
import comp3607_project.pdf_exporter.pdf.ReportSectionFactory;

public class TelemetryReportFacade {

    public static final long DEFAULT_IDLE_THRESHOLD_MS = 5_000;

    private final TelemetryJournal telemetryJournal;
    private final InteractionJournal interactionJournal;
    private final ReportEventSink eventSink;
    private final ReportInteractionObserver interactionObserver;
    private final DefaultMetricsAggregator aggregator;

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

        this.telemetryJournal = new TelemetryJournal(startedAtMillis);
        this.interactionJournal = new InteractionJournal();
        this.eventSink = new ReportEventSink(delegate, telemetryJournal);
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

        List<ReportSection> sections = ReportSectionFactory.create(
                new HumanReadableDurationFormatter(),
                new PieChartRenderer(
                        "Compilation Errors by Category",
                        metrics.errorFrequencyByCategory(), 700, 400),
                new BarChartRenderer(
                        "Time Between Compilation Errors", "Interval", "Errors",
                        metrics.errorIntervalBuckets(), 700, 400),
                new LineChartRenderer(
                        "Cumulative Compilation Outcomes", "Attempt", "Count",
                        metrics.compilationOutcomes(), 700, 400));

        PdfWriter writer = new PdfBoxWriter();
        PdfReportGenerator generator = new PdfReportGenerator(sections, writer);

        File parent = output.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Could not create PDF report directory: " + parent);
        }

        try (PDDocument doc = new PDDocument()) {
            generator.writeTo(doc, ReportMetricsView.of(metrics));
            doc.save(output.getAbsoluteFile());
        }
        return output;
    }
}
