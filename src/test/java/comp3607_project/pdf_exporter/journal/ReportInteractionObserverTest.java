package comp3607_project.pdf_exporter.journal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import comp3607_project.editor_interactions.InteractionEvent;
import comp3607_project.editor_interactions.InteractionObserver;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReportInteractionObserverTest {

    private static InteractionEvent key(long timestamp) {
        return new InteractionEvent(InteractionEvent.Kind.KEY, Instant.ofEpochMilli(timestamp),
                0, "A", false);
    }

    @Test
    void recordsEachInteractionInTheJournal() {
        InteractionJournal journal = new InteractionJournal();
        ReportInteractionObserver observer = new ReportInteractionObserver(journal);
        InteractionEvent a = key(1_000);
        InteractionEvent b = key(2_000);

        observer.onInteraction(a);
        observer.onInteraction(b);

        assertEquals(List.of(a, b), journal.events());
    }

    @Test
    void exposesItsJournal() {
        InteractionJournal journal = new InteractionJournal();

        assertSame(journal, new ReportInteractionObserver(journal).journal());
    }

    @Test
    void canBeUsedAsAnInteractionObserver() {
        InteractionJournal journal = new InteractionJournal();
        InteractionObserver observer = new ReportInteractionObserver(journal);

        observer.onInteraction(key(1_000));

        assertEquals(1, journal.events().size());
    }
}
