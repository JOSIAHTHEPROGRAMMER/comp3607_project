package comp3607_project.pdf_exporter.journal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import comp3607_project.editor_interactions.InteractionEvent;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class InteractionJournalTest {

    private static InteractionEvent key(long timestamp) {
        return new InteractionEvent(InteractionEvent.Kind.KEY, Instant.ofEpochMilli(timestamp),
                0, "A", false);
    }

    @Test
    void startsEmpty() {
        assertTrue(new InteractionJournal().events().isEmpty());
    }

    @Test
    void nullEventsAreIgnored() {
        InteractionJournal journal = new InteractionJournal();

        journal.record(null);

        assertTrue(journal.events().isEmpty());
    }

    @Test
    void eventsComeBackSortedByTime() {
        InteractionJournal journal = new InteractionJournal();
        InteractionEvent late = key(3_000);
        InteractionEvent early = key(1_000);
        journal.record(late);
        journal.record(early);

        assertEquals(List.of(early, late), journal.events());
    }

    @Test
    void returnedEventListCannotBeModified() {
        InteractionJournal journal = new InteractionJournal();
        journal.record(key(1_000));
        List<InteractionEvent> events = journal.events();

        assertThrows(UnsupportedOperationException.class, () -> events.add(key(2_000)));
    }

    @Test
    void firstAndLastTimestampsAreTheEarliestAndLatestEvents() {
        InteractionJournal journal = new InteractionJournal();
        journal.record(key(5_000));
        journal.record(key(1_000));
        journal.record(key(9_000));
        Instant fallback = Instant.ofEpochMilli(42);

        assertEquals(Instant.ofEpochMilli(1_000), journal.firstTimestamp(fallback));
        assertEquals(Instant.ofEpochMilli(9_000), journal.lastTimestamp(fallback));
    }

    @Test
    void emptyJournalReturnsTheFallbackTimestamps() {
        InteractionJournal journal = new InteractionJournal();
        Instant fallback = Instant.ofEpochMilli(42);

        assertEquals(fallback, journal.firstTimestamp(fallback));
        assertEquals(fallback, journal.lastTimestamp(fallback));
    }
}
