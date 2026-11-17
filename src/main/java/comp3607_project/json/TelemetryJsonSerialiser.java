package comp3607_project.json;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import comp3607_project.model.TelemetryEvent;

//Builds the session JSON document from telemetry events.

public class TelemetryJsonSerialiser {
    public static final String SCHEMA_VERSION = "1.0";
    private final List<EventMapper> mappers;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public String serialise(List<TelemetryEvent> events, String studentId,
            long sessionStartMillis, long sessionEndMillis) {
        return gson.toJson(buildDocument(events, studentId, sessionStartMillis, sessionEndMillis));
    }

    public TelemetryJsonSerialiser() {
        this(EventMappers.defaults());
    }

    public TelemetryJsonSerialiser(List<EventMapper> mappers) {
        this.mappers = List.copyOf(mappers);
    }

    /** Events of the given project, plus events whose project is unknown. */
    public List<TelemetryEvent> eventsForProject(List<TelemetryEvent> events, String project) {
        List<TelemetryEvent> result = new ArrayList<>();
        for (TelemetryEvent e : events) {
            String p = projectOf(e);
            if (p == null || p.equals(project)) {
                result.add(e);
            }
        }
        return result;
    }

    private String projectOf(TelemetryEvent e) {
        for (EventMapper m : mappers) {
            if (m.supports(e)) {
                return m.projectOf(e);
            }
        }
        return null;
    }

    private Map<String, Object> eventToMap(TelemetryEvent e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", e.getEventType());
        m.put("timestamp", iso(e.getTimestamp()));
        m.put("timestampMillis", e.getTimestamp());
        m.put("target", e.getTarget());
        for (EventMapper mapper : mappers) {
            if (mapper.supports(e)) {
                mapper.addFields(e, m);
                break;
            }
        }
        return m;
    }

    Map<String, Object> buildDocument(List<TelemetryEvent> events, String studentId,
            long startMillis, long endMillis) {
        SessionSummary summary = SessionSummary.from(events, mappers);

        Map<String, Object> session = new LinkedHashMap<>();
        session.put("studentId", studentId);
        session.put("startTime", iso(startMillis));
        session.put("endTime", iso(endMillis));
        session.put("durationSeconds", Math.max(0, (endMillis - startMillis) / 1000));
        session.put("totalCompilations", summary.totalCompilations());
        session.put("successfulCompilations", summary.successfulCompilations());
        session.put("failedCompilations", summary.failedCompilations());
        session.put("totalRuns", summary.totalRuns());
        session.put("runtimeExceptions", summary.runtimeExceptions());
        session.put("errorCountsByCategory", summary.errorCountsByCategory());
        session.put("compileIntervalsMillis", summary.compileIntervalsMillis());

        List<Map<String, Object>> eventList = new ArrayList<>();
        for (TelemetryEvent e : events) {
            eventList.add(eventToMap(e));
        }

        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("schemaVersion", SCHEMA_VERSION);
        doc.put("session", session);
        doc.put("events", eventList);
        return doc;
    }

    private static String iso(long millis) {
        return Instant.ofEpochMilli(millis).toString();
    }
}