package comp3607_project.json;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import comp3607_project.compile.CompileErrorClassifier.Category;
import comp3607_project.compile.CompileEvent;
import comp3607_project.lifecycle.LifecycleEvent;
import comp3607_project.lifecycle.LifecycleEventType;
import comp3607_project.model.TelemetryEvent;

class TelemetryJsonSerialiserTest {
    private final TelemetryJsonSerialiser serialiser = new TelemetryJsonSerialiser();

    private JsonObject parse(List<TelemetryEvent> events) {
        String json = serialiser.serialise(events, "STUDENT_ID", 0, 5000);
        return JsonParser.parseString(json).getAsJsonObject();
    }

    @Test
    void outputIsValidJsonWithSchemaVersion() {
        JsonObject doc = parse(List.of());
        assertEquals("1.0", doc.get("schemaVersion").getAsString());
        assertEquals(0, doc.getAsJsonArray("events").size());
    }

    @Test
    void sessionMetadataIsPresent() {
        JsonObject session = parse(List.of()).getAsJsonObject("session");
        assertEquals("STUDENT_ID", session.get("studentId").getAsString());
        assertEquals("1970-01-01T00:00:00Z", session.get("startTime").getAsString());
        assertEquals(5, session.get("durationSeconds").getAsLong());
        assertEquals(0, session.get("totalCompilations").getAsInt());
        assertTrue(session.has("compileIntervalsMillis"));
        assertTrue(session.getAsJsonObject("errorCountsByCategory").has("SYNTAX"));
    }

    @Test
    void negativeDurationIsClampedToZero() {
        String json = serialiser.serialise(List.of(), "S", 5000, 0);
        JsonObject session = JsonParser.parseString(json).getAsJsonObject()
                .getAsJsonObject("session");
        assertEquals(0, session.get("durationSeconds").getAsLong());
    }

    @Test
    void compileErrorKeepsQuotesAndNewlinesParseable() {
        String message = "cannot find symbol\n  symbol:   variable \"x\"";
        CompileEvent e = new CompileEvent(1, 1000, "p", false, "cant.resolve",
                Category.SYMBOL, message, "A.java", 7);
        JsonArray events = parse(List.of(e)).getAsJsonArray("events");
        JsonObject first = events.get(0).getAsJsonObject();
        assertEquals("COMPILE_ERROR", first.get("type").getAsString());
        assertEquals("SYMBOL", first.get("category").getAsString());
        assertEquals(7, first.get("line").getAsInt());
        assertEquals(message, first.get("message").getAsString());
    }

    @Test
    void successEventOmitsErrorFields() {
        CompileEvent e = new CompileEvent(1, 1000, "p", true, null, null, null, "A.java", -1);
        JsonObject first = parse(List.of(e)).getAsJsonArray("events").get(0).getAsJsonObject();
        assertEquals("COMPILE_SUCCESS", first.get("type").getAsString());
        assertFalse(first.has("errorCode"));
    }

    @Test
    void lifecycleEventIsWrittenWithTypeAndTarget() {
        LifecycleEvent e = new LifecycleEvent(LifecycleEventType.CLASS_CREATED, 2000,
                "p", "Dog", null);
        JsonObject first = parse(List.of(e)).getAsJsonArray("events").get(0).getAsJsonObject();
        assertEquals("CLASS_CREATED", first.get("type").getAsString());
        assertEquals("Dog", first.get("target").getAsString());
        assertEquals("1970-01-01T00:00:02Z", first.get("timestamp").getAsString());
    }

    @Test
    void eventsForProjectKeepsMatchingAndUnknownProjectEvents() {
        TelemetryEvent unknown = new TelemetryEvent() {
            @Override
            public String getEventType() {
                return "X";
            }

            @Override
            public long getTimestamp() {
                return 1;
            }

            @Override
            public String getTarget() {
                return "t";
            }
        };
        CompileEvent a = new CompileEvent(1, 1000, "A", true, null, null, null, "A.java", -1);
        CompileEvent b = new CompileEvent(2, 2000, "B", true, null, null, null, "B.java", -1);

        List<TelemetryEvent> result = serialiser.eventsForProject(List.of(a, b, unknown), "A");

        assertEquals(List.of(a, unknown), result);
    }

    @Test
    void eventsKeepTheirOrder() {
        CompileEvent a = new CompileEvent(1, 1000, "p", true, null, null, null, "A.java", -1);
        CompileEvent b = new CompileEvent(2, 2000, "p", true, null, null, null, "A.java", -1);
        JsonArray events = parse(List.of(a, b)).getAsJsonArray("events");
        assertEquals(1000, events.get(0).getAsJsonObject().get("timestampMillis").getAsLong());
        assertEquals(2000, events.get(1).getAsJsonObject().get("timestampMillis").getAsLong());
    }
}