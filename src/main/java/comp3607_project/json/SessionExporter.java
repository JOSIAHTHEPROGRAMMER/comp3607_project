package comp3607_project.json;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongSupplier;

import comp3607_project.model.TelemetryEvent;

/**
 * Writes one JSON file per session into each project folder.
 * Each file holds only that project's events and is overwritten on each export.
 */
public class SessionExporter {
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
            .withZone(ZoneId.systemDefault());

    private final EventCollector collector;
    private final TelemetryJsonSerialiser serialiser;
    private final String studentId;
    private final long sessionStartMillis;
    private final LongSupplier clock;
    private final Map<Path, Integer> lastExportedCounts = new HashMap<>();

    public SessionExporter(EventCollector collector, TelemetryJsonSerialiser serialiser,
            String studentId, long sessionStartMillis, LongSupplier clock) {
        this.collector = collector;
        this.serialiser = serialiser;
        this.studentId = studentId;
        this.sessionStartMillis = sessionStartMillis;
        this.clock = clock;
    }

    /** Writes all events into dir, overwriting the session file. */
    public synchronized Path export(Path dir) throws IOException {
        return write(dir, collector.getEvents());
    }

    /**
     * Writes the events of dir's project, only if there are some and they changed.
     */
    public synchronized Optional<Path> exportIfNew(Path dir) throws IOException {
        Path name = dir.getFileName();
        String project = name == null ? null : name.toString();
        List<TelemetryEvent> events = serialiser.eventsForProject(collector.getEvents(), project);
        if (events.isEmpty()) {
            return Optional.empty();
        }
        Integer last = lastExportedCounts.get(dir);
        if (last != null && last == events.size()) {
            return Optional.empty();
        }
        return Optional.of(write(dir, events));
    }

    private Path write(Path dir, List<TelemetryEvent> events) throws IOException {
        String json = serialiser.serialise(events, studentId, sessionStartMillis,
                clock.getAsLong());
        Files.createDirectories(dir);
        Path out = dir.resolve("telemetry-"
                + STAMP.format(Instant.ofEpochMilli(sessionStartMillis)) + ".json");
        Files.writeString(out, json, StandardCharsets.UTF_8);
        lastExportedCounts.put(dir, events.size());
        return out;
    }
}