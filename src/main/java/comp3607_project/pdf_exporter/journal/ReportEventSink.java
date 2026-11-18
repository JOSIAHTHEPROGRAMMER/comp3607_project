package comp3607_project.pdf_exporter.journal;


import comp3607_project.editor_interactions.InteractionTelemetryEvent;
import comp3607_project.model.EventSink;
import comp3607_project.model.TelemetryEvent;


public final class ReportEventSink implements EventSink {

    private final EventSink delegate;
    private final TelemetryJournal journal;
    private final InteractionJournal interactionJournal;

    public ReportEventSink(EventSink delegate, TelemetryJournal journal) {
        this(delegate, journal, null);
    }

    public ReportEventSink(EventSink delegate, TelemetryJournal journal,
                           InteractionJournal interactionJournal) {
        this.delegate = delegate;
        this.journal = journal;
        this.interactionJournal = interactionJournal;
    }

    @Override
    public void accept(TelemetryEvent event) {
        journal.record(event);
        if (interactionJournal != null && event instanceof InteractionTelemetryEvent interaction) {
            interactionJournal.record(interaction.getEvent());
        }
        if (delegate != null) delegate.accept(event);
    }

    public TelemetryJournal journal() {
        return journal;
    }
}
