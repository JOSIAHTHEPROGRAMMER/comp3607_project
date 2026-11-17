package comp3607_project.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import comp3607_project.compile.CompileErrorClassifier.Category;
import comp3607_project.compile.CompileEvent;
import comp3607_project.execution.ExecutionEvent;
import comp3607_project.execution.ExecutionKind;
import comp3607_project.execution.ExecutionOutcome;
import comp3607_project.execution.InvocationInfo;
import comp3607_project.lifecycle.LifecycleEvent;
import comp3607_project.lifecycle.LifecycleEventType;
import comp3607_project.model.TelemetryEvent;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Serialises events, parses the text back with a real JSON parser and checks
 * the
 * structure: the output must be valid JSON, carry the session metadata and keep
 * every event field intact.
 */
class TelemetryJsonSerialiserRoundTripTest {

    private final TelemetryJsonSerialiser serialiser = new TelemetryJsonSerialiser();

    private static TelemetryEvent unknownEvent(long timestamp) {
        return new TelemetryEvent() {
            @Override
            public String getEventType() {
                return "SOMETHING_ELSE";
            }

            @Override
            public long getTimestamp() {
                return timestamp;
            }

            @Override
            public String getTarget() {
                return "somewhere";
            }
        };
    }

    private JsonObject parse(List<TelemetryEvent> events, long start, long end) {
        String json = serialiser.serialise(events, "student-001", start, end);
        return JsonParser.parseString(json).getAsJsonObject();
    }

    @Test
    void emptySessionIsStillValidJsonWithMetadata() {
        JsonObject root = parse(List.of(), 0L, 90_000L);
        JsonObject session = root.getAsJsonObject("session");

        assertEquals("1.0", root.get("schemaVersion").getAsString());
        assertEquals("student-001", session.get("studentId").getAsString());
        assertEquals(0, session.get("totalCompilations").getAsInt());
        assertEquals(0, session.get("totalRuns").getAsInt());
        assertEquals(0, root.getAsJsonArray("events").size());
    }

    @Test
    void schemaVersionConstantMatchesTheDocument() {
        assertEquals(TelemetryJsonSerialiser.SCHEMA_VERSION,
                parse(List.of(), 0L, 0L).get("schemaVersion").getAsString());
    }

    @Test
    void sessionTimesAreIsoTimestampsAndDurationIsInSeconds() {
        JsonObject session = parse(List.of(), 0L, 90_000L).getAsJsonObject("session");

        assertEquals("1970-01-01T00:00:00Z", session.get("startTime").getAsString());
        assertEquals("1970-01-01T00:01:30Z", session.get("endTime").getAsString());
        assertEquals(90, session.get("durationSeconds").getAsLong());
    }

    @Test
    void durationIsNeverNegative() {
        JsonObject session = parse(List.of(), 90_000L, 0L).getAsJsonObject("session");

        assertEquals(0, session.get("durationSeconds").getAsLong());
    }

    @Test
    void everyEventHasTypeTimestampsAndTarget() {
        List<TelemetryEvent> events = List.of(unknownEvent(5_000L));

        JsonObject event = parse(events, 0L, 10_000L).getAsJsonArray("events")
                .get(0).getAsJsonObject();

        assertEquals("SOMETHING_ELSE", event.get("type").getAsString());
        assertEquals("1970-01-01T00:00:05Z", event.get("timestamp").getAsString());
        assertEquals(5_000L, event.get("timestampMillis").getAsLong());
        assertEquals("somewhere", event.get("target").getAsString());
    }

    @Test
    void eventWithoutAMapperGetsOnlyTheBasicFields() {
        List<TelemetryEvent> events = List.of(unknownEvent(5_000L));

        JsonObject event = parse(events, 0L, 10_000L).getAsJsonArray("events")
                .get(0).getAsJsonObject();

        assertEquals(4, event.size());
        assertFalse(event.has("project"));
    }

    @Test
    void eventsKeepTheirOrder() {
        List<TelemetryEvent> events = List.of(unknownEvent(1_000L), unknownEvent(2_000L),
                unknownEvent(3_000L));

        JsonArray array = parse(events, 0L, 10_000L).getAsJsonArray("events");

        assertEquals(3, array.size());
        assertEquals(1_000L, array.get(0).getAsJsonObject().get("timestampMillis").getAsLong());
        assertEquals(3_000L, array.get(2).getAsJsonObject().get("timestampMillis").getAsLong());
    }

    @Test
    void sessionTotalsMatchTheEventsSerialised() {
        InvocationInfo ok = new InvocationInfo("alpha", "Calc", "c", "add", 1,
                ExecutionOutcome.COMPLETED, "int");
        InvocationInfo bad = new InvocationInfo("alpha", "Calc", "c", "div", 1,
                ExecutionOutcome.EXCEPTION, null);
        List<TelemetryEvent> events = List.of(
                CompileEvent.error(1L, 1_000L, "alpha", "Calc.java", 4, "expected",
                        Category.SYNTAX, "';' expected"),
                CompileEvent.success(2L, 4_000L, "alpha", "Calc.java"),
                new ExecutionEvent(ExecutionKind.INSTANCE_METHOD, 5_000L, ok),
                new ExecutionEvent(ExecutionKind.INSTANCE_METHOD, 6_000L, bad));

        JsonObject session = parse(events, 0L, 10_000L).getAsJsonObject("session");

        assertEquals(2, session.get("totalCompilations").getAsInt());
        assertEquals(1, session.get("successfulCompilations").getAsInt());
        assertEquals(1, session.get("failedCompilations").getAsInt());
        assertEquals(2, session.get("totalRuns").getAsInt());
        assertEquals(1, session.get("runtimeExceptions").getAsInt());
        assertEquals(1, session.getAsJsonObject("errorCountsByCategory")
                .get("SYNTAX").getAsInt());
        assertEquals(0, session.getAsJsonObject("errorCountsByCategory")
                .get("TYPE").getAsInt());
        assertEquals(3_000L, session.getAsJsonArray("compileIntervalsMillis")
                .get(0).getAsLong());
    }

    @Test
    void compileErrorFieldsSurviveTheRoundTrip() {
        List<TelemetryEvent> events = List.of(CompileEvent.error(1L, 1_000L, "alpha",
                "Dog.java", 12, "cant.resolve", Category.SYMBOL, "cannot find symbol"));

        JsonObject event = parse(events, 0L, 10_000L).getAsJsonArray("events")
                .get(0).getAsJsonObject();

        assertEquals("COMPILE_ERROR", event.get("type").getAsString());
        assertEquals("Dog.java", event.get("target").getAsString());
        assertEquals("cant.resolve", event.get("errorCode").getAsString());
        assertEquals("SYMBOL", event.get("category").getAsString());
        assertEquals(12, event.get("line").getAsInt());
        assertEquals("cannot find symbol", event.get("message").getAsString());
    }

    @Test
    void lifecycleFieldsSurviveTheRoundTrip() {
        List<TelemetryEvent> events = List.of(new LifecycleEvent(
                LifecycleEventType.CLASS_CREATED, 1_000L, "alpha", "Dog", "created"));

        JsonObject event = parse(events, 0L, 10_000L).getAsJsonArray("events")
                .get(0).getAsJsonObject();

        assertEquals("CLASS_CREATED", event.get("type").getAsString());
        assertEquals("alpha", event.get("project").getAsString());
        assertEquals("Dog", event.get("className").getAsString());
    }

    @Test
    void specialCharactersAreEscapedAndRestoredExactly() {
        String message = "Bad \"token\" <here>\nnext line\tC:\\temp caf\u00e9";
        List<TelemetryEvent> events = List.of(CompileEvent.error(1L, 1_000L, "alpha",
                "Dog.java", 1, "expected", Category.SYNTAX, message));

        JsonObject event = parse(events, 0L, 10_000L).getAsJsonArray("events")
                .get(0).getAsJsonObject();

        assertEquals(message, event.get("message").getAsString());
    }

    @Test
    void htmlCharactersAreNotEscapedInTheText() {
        List<TelemetryEvent> events = List.of(CompileEvent.error(1L, 1_000L, "alpha",
                "Dog.java", 1, "expected", Category.SYNTAX, "List<String> & more"));

        String json = serialiser.serialise(events, "student-001", 0L, 10_000L);

        assertTrue(json.contains("List<String> & more"));
    }

    @Test
    void outputIsPrettyPrintedOverSeveralLines() {
        String json = serialiser.serialise(List.of(), "student-001", 0L, 1_000L);

        assertTrue(json.lines().count() > 5);
    }

    @Test
    void eventsForProjectKeepsMatchingAndUnknownProjectEventsOnly() {
        CompileEvent alpha = CompileEvent.success(1L, 1_000L, "alpha", "A.java");
        CompileEvent beta = CompileEvent.success(2L, 2_000L, "beta", "B.java");
        TelemetryEvent unknown = unknownEvent(3_000L);

        List<TelemetryEvent> result = serialiser.eventsForProject(
                List.of(alpha, beta, unknown), "alpha");

        assertEquals(List.of(alpha, unknown), result);
    }

    @Test
    void eventsForAnUnknownProjectKeepsOnlyProjectlessEvents() {
        CompileEvent alpha = CompileEvent.success(1L, 1_000L, "alpha", "A.java");
        TelemetryEvent unknown = unknownEvent(3_000L);

        List<TelemetryEvent> result = serialiser.eventsForProject(
                List.of(alpha, unknown), "gamma");

        assertEquals(List.of(unknown), result);
    }

    @Test
    void serialiserWithNoMappersWritesNoTypeSpecificFieldsOrTotals() {
        TelemetryJsonSerialiser bare = new TelemetryJsonSerialiser(List.of());
        List<TelemetryEvent> events = List.of(
                CompileEvent.success(1L, 1_000L, "alpha", "A.java"));

        JsonObject root = JsonParser.parseString(
                bare.serialise(events, "student-001", 0L, 10_000L)).getAsJsonObject();
        JsonObject event = root.getAsJsonArray("events").get(0).getAsJsonObject();

        assertFalse(event.has("attemptId"));
        assertEquals(0, root.getAsJsonObject("session").get("totalCompilations").getAsInt());
    }
}
