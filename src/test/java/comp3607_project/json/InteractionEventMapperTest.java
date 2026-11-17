package comp3607_project.json;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import comp3607_project.editor_interactions.InteractionEvent;
import comp3607_project.editor_interactions.InteractionTelemetryEvent;
import comp3607_project.model.TelemetryEvent;

class InteractionEventMapperTest {

    private static InteractionTelemetryEvent wrap(InteractionEvent.Kind kind, boolean idle) {
        return new InteractionTelemetryEvent(
                new InteractionEvent(kind, Instant.ofEpochMilli(2000), 350, "Dog.java", idle),
                "proj");
    }

    @Test
    void eventTypeReflectsKindAndIdle() {
        assertEquals("EDITOR_KEY", wrap(InteractionEvent.Kind.KEY, false).getEventType());
        assertEquals("EDITOR_EDIT", wrap(InteractionEvent.Kind.EDIT, false).getEventType());
        assertEquals("EDITOR_IDLE", wrap(InteractionEvent.Kind.KEY, true).getEventType());
    }

    @Test
    void timestampIsEpochMillis() {
        assertEquals(2000, wrap(InteractionEvent.Kind.KEY, false).getTimestamp());
    }

    @Test
    void serialiserWritesEditorFields() {
        List<TelemetryEvent> events = List.of(wrap(InteractionEvent.Kind.EDIT, true));
        String json = new TelemetryJsonSerialiser().serialise(events, "S", 0, 5000);
        JsonObject first = JsonParser.parseString(json).getAsJsonObject()
                .getAsJsonArray("events").get(0).getAsJsonObject();

        assertEquals("EDITOR_IDLE", first.get("type").getAsString());
        assertEquals("proj", first.get("project").getAsString());
        assertEquals(350, first.get("gapSincePreviousMs").getAsLong());
        assertTrue(first.get("idle").getAsBoolean());
    }

    @Test
    void keyTextNeverReachesTheJson() {
        InteractionTelemetryEvent key = new InteractionTelemetryEvent(
                new InteractionEvent(InteractionEvent.Kind.KEY, Instant.ofEpochMilli(1), 10,
                        "Semicolon", true),
                "proj");
        String json = new TelemetryJsonSerialiser().serialise(List.of(key), "S", 0, 5000);

        assertTrue(!json.contains("Semicolon"));
        assertEquals("proj", key.getTarget());
    }

    @Test
    void editKeepsFileNameAsTarget() {
        assertEquals("Dog.java", wrap(InteractionEvent.Kind.EDIT, false).getTarget());
    }

    @Test
    void editorEventsAreFilteredByProject() {
        TelemetryJsonSerialiser s = new TelemetryJsonSerialiser();
        InteractionTelemetryEvent a = wrap(InteractionEvent.Kind.KEY, false);
        assertEquals(1, s.eventsForProject(List.of(a), "proj").size());
        assertEquals(0, s.eventsForProject(List.of(a), "other").size());
    }
}