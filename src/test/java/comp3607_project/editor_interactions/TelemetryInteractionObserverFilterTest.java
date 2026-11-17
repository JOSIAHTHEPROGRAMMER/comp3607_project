package comp3607_project.editor_interactions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import comp3607_project.model.TelemetryEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class TelemetryInteractionObserverFilterTest {

    private static InteractionEvent event(InteractionEvent.Kind kind, boolean idle) {
        return new InteractionEvent(kind, Instant.ofEpochMilli(1_000L), 10, "x", idle);
    }

    @Test
    void editsAreKept() {
        assertTrue(TelemetryInteractionObserver.editsAndIdleOnly(
                event(InteractionEvent.Kind.EDIT, false)));
    }

    @Test
    void idleGapsAreKept() {
        assertTrue(TelemetryInteractionObserver.editsAndIdleOnly(
                event(InteractionEvent.Kind.KEY, true)));
    }

    @Test
    void ordinaryKeystrokesAreDropped() {
        assertFalse(TelemetryInteractionObserver.editsAndIdleOnly(
                event(InteractionEvent.Kind.KEY, false)));
    }

    @Test
    void defaultObserverForwardsEveryEventWithTheCurrentProject() {
        List<TelemetryEvent> received = new ArrayList<>();
        TelemetryInteractionObserver observer = new TelemetryInteractionObserver(received::add, () -> "demo");

        observer.onInteraction(event(InteractionEvent.Kind.KEY, false));

        assertEquals(1, received.size());
        InteractionTelemetryEvent forwarded = assertInstanceOf(InteractionTelemetryEvent.class, received.get(0));
        assertEquals("demo", forwarded.getProject());
    }

    @Test
    void observerWithAFilterOnlyForwardsMatchingEvents() {
        List<TelemetryEvent> received = new ArrayList<>();
        TelemetryInteractionObserver observer = new TelemetryInteractionObserver(
                received::add, () -> "demo", TelemetryInteractionObserver::editsAndIdleOnly);

        observer.onInteraction(event(InteractionEvent.Kind.KEY, false));
        observer.onInteraction(event(InteractionEvent.Kind.EDIT, false));

        assertEquals(1, received.size());
        assertEquals("EDITOR_EDIT", received.get(0).getEventType());
    }

    @Test
    void projectIsLookedUpWhenTheEventArrives() {
        List<TelemetryEvent> received = new ArrayList<>();
        String[] current = { "first" };
        TelemetryInteractionObserver observer = new TelemetryInteractionObserver(received::add, () -> current[0]);

        observer.onInteraction(event(InteractionEvent.Kind.EDIT, false));
        current[0] = "second";
        observer.onInteraction(event(InteractionEvent.Kind.EDIT, false));

        assertEquals("first", ((InteractionTelemetryEvent) received.get(0)).getProject());
        assertEquals("second", ((InteractionTelemetryEvent) received.get(1)).getProject());
    }
}
