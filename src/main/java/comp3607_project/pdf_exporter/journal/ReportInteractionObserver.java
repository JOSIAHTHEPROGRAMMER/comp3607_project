package comp3607_project.pdf_exporter.journal;


import comp3607_project.editor_interactions.InteractionObserver;
import comp3607_project.editor_interactions.InteractionEvent;

public final class ReportInteractionObserver implements InteractionObserver {

    private final InteractionJournal journal;

    public ReportInteractionObserver(InteractionJournal journal) {
        this.journal = journal;
    }

    @Override
    public void onInteraction(InteractionEvent event) {
        journal.record(event);
    }

    public InteractionJournal journal() {
        return journal;
    }
}