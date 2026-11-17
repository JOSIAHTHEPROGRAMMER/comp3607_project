package comp3607_project.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import comp3607_project.model.EventSink;
import comp3607_project.model.TelemetryEvent;
import java.util.List;
import org.junit.jupiter.api.Test;

class EventCollectorTest {

    private static TelemetryEvent event(String type) {
        return new TelemetryEvent() {
            @Override
            public String getEventType() {
                return type;
            }

            @Override
            public long getTimestamp() {
                return 1L;
            }

            @Override
            public String getTarget() {
                return "t";
            }
        };
    }

    @Test
    void startsEmpty() {
        EventCollector collector = new EventCollector();

        assertEquals(0, collector.size());
        assertTrue(collector.getEvents().isEmpty());
    }

    @Test
    void remembersEventsInArrivalOrder() {
        EventCollector collector = new EventCollector();
        TelemetryEvent a = event("A");
        TelemetryEvent b = event("B");

        collector.accept(a);
        collector.accept(b);

        assertEquals(2, collector.size());
        assertSame(a, collector.getEvents().get(0));
        assertSame(b, collector.getEvents().get(1));
    }

    @Test
    void nullEventsAreIgnored() {
        EventCollector collector = new EventCollector();

        collector.accept(null);

        assertEquals(0, collector.size());
    }

    @Test
    void returnedListCannotBeModified() {
        EventCollector collector = new EventCollector();
        collector.accept(event("A"));
        List<TelemetryEvent> snapshot = collector.getEvents();

        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(event("B")));
    }

    @Test
    void earlierSnapshotIsNotAffectedByLaterEvents() {
        EventCollector collector = new EventCollector();
        collector.accept(event("A"));
        List<TelemetryEvent> snapshot = collector.getEvents();

        collector.accept(event("B"));

        assertEquals(1, snapshot.size());
        assertEquals(2, collector.getEvents().size());
    }

    @Test
    void canBeUsedAsAnEventSink() {
        EventCollector collector = new EventCollector();
        EventSink sink = collector;

        sink.accept(event("A"));

        assertEquals(1, collector.size());
    }
}
