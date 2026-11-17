package comp3607_project.pdf_exporter.metrics;

import comp3607_project.pdf_exporter.journal.InteractionJournal;
import comp3607_project.pdf_exporter.journal.TelemetryJournal;

public interface MetricsAggregator {
    ReportMetrics aggregate(TelemetryJournal telemetry, InteractionJournal interactions);
}