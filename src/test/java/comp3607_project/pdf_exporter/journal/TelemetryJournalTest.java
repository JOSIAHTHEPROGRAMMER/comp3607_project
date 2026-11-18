package comp3607_project.pdf_exporter.journal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import comp3607_project.compile.CompileEvent;
import comp3607_project.lifecycle.LifecycleEvent;
import comp3607_project.lifecycle.LifecycleEventType;
import comp3607_project.model.TelemetryEvent;
import java.util.List;
import org.junit.jupiter.api.Test;

class TelemetryJournalTest {

    private static TelemetryEvent stub(long timestamp) {
        return new TelemetryEvent() {
            @Override
            public String getEventType() {
                return "TEST";
            }

            @Override
            public long getTimestamp() {
                return timestamp;
            }

            @Override
            public String getTarget() {
                return "t";
            }
        };
    }

    private static LifecycleEvent created(long timestamp) {
        return new LifecycleEvent(LifecycleEventType.CLASS_CREATED, timestamp, "p", "Dog", null);
    }

    @Test
    void remembersTheStartTime() {
        assertEquals(1_234, new TelemetryJournal(1_234).startedAtMillis());
    }

    @Test
    void eventsComeBackSortedByTimestamp() {
        TelemetryJournal journal = new TelemetryJournal(0);
        TelemetryEvent late = stub(3_000);
        TelemetryEvent early = stub(1_000);
        TelemetryEvent middle = stub(2_000);
        journal.record(late);
        journal.record(early);
        journal.record(middle);

        assertEquals(List.of(early, middle, late), journal.events());
    }

    @Test
    void returnedEventListCannotBeModified() {
        TelemetryJournal journal = new TelemetryJournal(0);
        journal.record(stub(1_000));
        List<TelemetryEvent> events = journal.events();

        assertThrows(UnsupportedOperationException.class, () -> events.add(stub(2_000)));
    }

    @Test
    void eventsOfTypeKeepsOnlyThatTypeInTimeOrder() {
        TelemetryJournal journal = new TelemetryJournal(0);
        journal.record(created(3_000));
        journal.record(CompileEvent.success(1, 2_000, "p", "Dog.java"));
        journal.record(created(1_000));

        List<LifecycleEvent> lifecycle = journal.eventsOfType(LifecycleEvent.class);

        assertEquals(2, lifecycle.size());
        assertEquals(1_000, lifecycle.get(0).getTimestamp());
        assertEquals(3_000, lifecycle.get(1).getTimestamp());
    }

    @Test
    void eventsOfTypeIsEmptyWhenNothingMatches() {
        TelemetryJournal journal = new TelemetryJournal(0);
        journal.record(stub(1_000));

        assertTrue(journal.eventsOfType(CompileEvent.class).isEmpty());
    }

    @Test
    void eventsWithTypePrefixMatchesTheStartOfTheEventType() {
        TelemetryJournal journal = new TelemetryJournal(0);
        journal.record(CompileEvent.success(1, 2_000, "p", "Dog.java"));
        journal.record(CompileEvent.error(2, 1_000, "p", "Dog.java", 1, "expected",
                comp3607_project.compile.CompileErrorClassifier.Category.SYNTAX, "m"));
        journal.record(created(3_000));

        List<TelemetryEvent> compile = journal.eventsWithTypePrefix("COMPILE");

        assertEquals(2, compile.size());
        assertEquals("COMPILE_ERROR", compile.get(0).getEventType());
        assertEquals("COMPILE_SUCCESS", compile.get(1).getEventType());
    }

    @Test
    void endIsTheLatestEventTimeWhenTheSessionWasNotEnded() {
        TelemetryJournal journal = new TelemetryJournal(1_000);
        journal.record(stub(5_000));
        journal.record(stub(9_000));

        assertEquals(9_000, journal.endedAtMillis());
        assertEquals(8_000, journal.totalDurationMillis());
    }

    @Test
    void endIsTheStartWhenThereAreNoEventsAndNoExplicitEnd() {
        TelemetryJournal journal = new TelemetryJournal(1_000);

        assertEquals(1_000, journal.endedAtMillis());
        assertEquals(0, journal.totalDurationMillis());
    }

    @Test
    void explicitEndOverridesTheLatestEvent() {
        TelemetryJournal journal = new TelemetryJournal(1_000);
        journal.record(stub(5_000));

        journal.end(20_000);

        assertEquals(20_000, journal.endedAtMillis());
        assertEquals(19_000, journal.totalDurationMillis());
    }
}
