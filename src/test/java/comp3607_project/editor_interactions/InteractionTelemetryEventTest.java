package comp3607_project.editor_interactions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import comp3607_project.model.TelemetryEvent;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class InteractionTelemetryEventTest {

    private static InteractionEvent raw(InteractionEvent.Kind kind, long gap, String detail,
            boolean idle) {
        return new InteractionEvent(kind, Instant.ofEpochMilli(5_000L), gap, detail, idle);
    }

    @Test
    void ordinaryKeystrokeIsTypedEditorKey() {
        InteractionTelemetryEvent e = new InteractionTelemetryEvent(
                raw(InteractionEvent.Kind.KEY, 100, "A", false), "demo");

        assertEquals("EDITOR_KEY", e.getEventType());
    }

    @Test
    void fileEditIsTypedEditorEdit() {
        InteractionTelemetryEvent e = new InteractionTelemetryEvent(
                raw(InteractionEvent.Kind.EDIT, 0, "Dog.java", false), "demo");

        assertEquals("EDITOR_EDIT", e.getEventType());
    }

    @Test
    void idleGapIsTypedEditorIdle() {
        InteractionTelemetryEvent e = new InteractionTelemetryEvent(
                raw(InteractionEvent.Kind.KEY, 9_000, "A", true), "demo");

        assertEquals("EDITOR_IDLE", e.getEventType());
    }

    @Test
    void idleTakesPrecedenceOverTheKind() {
        InteractionTelemetryEvent e = new InteractionTelemetryEvent(
                raw(InteractionEvent.Kind.EDIT, 9_000, "Dog.java", true), "demo");

        assertEquals("EDITOR_IDLE", e.getEventType());
    }

    @Test
    void timestampIsTheEventTimeInEpochMillis() {
        InteractionTelemetryEvent e = new InteractionTelemetryEvent(
                raw(InteractionEvent.Kind.KEY, 0, "A", false), "demo");

        assertEquals(5_000L, e.getTimestamp());
    }

    @Test
    void editTargetIsTheEditedFile() {
        InteractionTelemetryEvent e = new InteractionTelemetryEvent(
                raw(InteractionEvent.Kind.EDIT, 0, "Dog.java", false), "demo");

        assertEquals("Dog.java", e.getTarget());
    }

    @Test
    void editWithoutADetailFallsBackToTheProject() {
        InteractionTelemetryEvent e = new InteractionTelemetryEvent(
                raw(InteractionEvent.Kind.EDIT, 0, null, false), "demo");

        assertEquals("demo", e.getTarget());
    }

    @Test
    void keystrokeTargetIsTheProjectNotTheKey() {
        InteractionTelemetryEvent e = new InteractionTelemetryEvent(
                raw(InteractionEvent.Kind.KEY, 0, "A", false), "demo");

        assertEquals("demo", e.getTarget());
    }

    @Test
    void exposesTheWrappedEventAndProject() {
        InteractionEvent inner = raw(InteractionEvent.Kind.KEY, 0, "A", false);
        InteractionTelemetryEvent e = new InteractionTelemetryEvent(inner, "demo");

        assertSame(inner, e.getEvent());
        assertEquals("demo", e.getProject());
    }

    @Test
    void projectMayBeUnknown() {
        InteractionTelemetryEvent e = new InteractionTelemetryEvent(
                raw(InteractionEvent.Kind.KEY, 0, "A", false), null);

        assertNull(e.getProject());
        assertNull(e.getTarget());
    }

    @Test
    void toStringSummarisesTypeProjectAndGap() {
        InteractionTelemetryEvent e = new InteractionTelemetryEvent(
                raw(InteractionEvent.Kind.KEY, 250, "A", false), "demo");

        assertEquals("EDITOR_KEY project=demo gap=250ms", e.toString());
    }

    @Test
    void canBeUsedAsAGenericTelemetryEvent() {
        TelemetryEvent e = new InteractionTelemetryEvent(
                raw(InteractionEvent.Kind.EDIT, 0, "Dog.java", false), "demo");

        assertEquals("EDITOR_EDIT", e.getEventType());
    }
}
