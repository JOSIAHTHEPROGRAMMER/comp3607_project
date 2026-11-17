package comp3607_project.pdf_exporter.journal;


import comp3607_project.model.EventSink;
import comp3607_project.model.TelemetryEvent;


public final class ReportEventSink implements EventSink {

    private final EventSink delegate;
    private final TelemetryJournal journal;

    public ReportEventSink(EventSink delegate, TelemetryJournal journal) {
        this.delegate = delegate;
        this.journal = journal;
    }

    @Override
    public void accept(TelemetryEvent event) {
        journal.record(event);
        if (delegate != null) delegate.accept(event);
    }

    public TelemetryJournal journal() {
        return journal;
    }
}
