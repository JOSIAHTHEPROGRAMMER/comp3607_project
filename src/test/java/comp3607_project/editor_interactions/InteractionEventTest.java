package comp3607_project.editor_interactions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class InteractionEventTest {

    private static final Instant TIME = Instant.ofEpochMilli(1_700_000_000_000L);

    @Test
    void gettersReturnTheValuesGivenToTheConstructor() {
        InteractionEvent event = new InteractionEvent(
                InteractionEvent.Kind.KEY, TIME, 250, "A", false);

        assertEquals(InteractionEvent.Kind.KEY, event.kind());
        assertEquals(TIME, event.timestamp());
        assertEquals(250, event.gapSincePreviousMs());
        assertEquals("A", event.detail());
        assertFalse(event.classifiedAsIdle());
    }

    @Test
    void idleFlagIsReported() {
        InteractionEvent event = new InteractionEvent(
                InteractionEvent.Kind.KEY, TIME, 6_000, "A", true);

        assertTrue(event.classifiedAsIdle());
    }

    @Test
    void toStringUsesTheKindWhenNotIdle() {
        InteractionEvent event = new InteractionEvent(
                InteractionEvent.Kind.KEY, TIME, 250, "A", false);

        assertEquals("[KEY] gap=250ms detail=A", event.toString());
    }

    @Test
    void toStringShowsIdleTagWhenClassifiedAsIdle() {
        InteractionEvent event = new InteractionEvent(
                InteractionEvent.Kind.KEY, TIME, 6_000, "A", true);

        assertEquals("[IDLE] gap=6000ms detail=A", event.toString());
    }

    @Test
    void editEventsAreLabelledEdit() {
        InteractionEvent event = new InteractionEvent(
                InteractionEvent.Kind.EDIT, TIME, 0, "Dog.java", false);

        assertEquals(InteractionEvent.Kind.EDIT, event.kind());
        assertEquals("[EDIT] gap=0ms detail=Dog.java", event.toString());
    }
}
