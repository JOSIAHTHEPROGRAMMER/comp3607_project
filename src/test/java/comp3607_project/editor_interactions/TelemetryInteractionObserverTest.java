package comp3607_project.editor_interactions;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import comp3607_project.model.TelemetryEvent;

class TelemetryInteractionObserverTest {
    @Test
    void forwardsWrappedEventWithProject() {
        List<TelemetryEvent> received = new ArrayList<>();
        TelemetryInteractionObserver observer = new TelemetryInteractionObserver(received::add, () -> "proj");

        observer.onInteraction(new InteractionEvent(InteractionEvent.Kind.EDIT,
                Instant.ofEpochMilli(10), 5, "Dog.java", false));

        assertEquals(1, received.size());
        assertEquals("EDITOR_EDIT", received.get(0).getEventType());
        assertEquals("proj", ((InteractionTelemetryEvent) received.get(0)).getProject());
    }

    @Test
    void defaultFilterKeepsEditsAndIdleOnly() {
        List<TelemetryEvent> received = new ArrayList<>();
        TelemetryInteractionObserver observer = new TelemetryInteractionObserver(
                received::add, () -> "p", TelemetryInteractionObserver::editsAndIdleOnly);

        observer.onInteraction(new InteractionEvent(InteractionEvent.Kind.KEY,
                Instant.ofEpochMilli(1), 5, "A", false));
        observer.onInteraction(new InteractionEvent(InteractionEvent.Kind.KEY,
                Instant.ofEpochMilli(2), 9000, "B", true));
        observer.onInteraction(new InteractionEvent(InteractionEvent.Kind.EDIT,
                Instant.ofEpochMilli(3), 5, "Dog.java", false));

        assertEquals(2, received.size());
    }
}