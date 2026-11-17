package comp3607_project.json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import comp3607_project.compile.CompileEvent;

class SessionExporterTest {

    @TempDir
    Path tempDir;

    @Test
    void writesParseableJsonFileIntoGivenFolder() throws IOException {
        EventCollector collector = new EventCollector();
        collector.accept(new CompileEvent(1, 1000, "p", true, null, null, null, "A.java", -1));
        SessionExporter exporter = new SessionExporter(collector, new TelemetryJsonSerialiser(),
                "STUDENT_ID", 0, () -> 5000L);

        Path out = exporter.export(tempDir);

        assertEquals(tempDir, out.getParent());
        assertTrue(out.getFileName().toString().startsWith("telemetry-"));
        assertTrue(out.getFileName().toString().endsWith(".json"));
        JsonObject doc = JsonParser.parseString(Files.readString(out)).getAsJsonObject();
        assertEquals(1, doc.getAsJsonArray("events").size());
        assertEquals(5, doc.getAsJsonObject("session").get("durationSeconds").getAsLong());
    }

    @Test
    void createsMissingFolder() throws IOException {
        SessionExporter exporter = new SessionExporter(new EventCollector(),
                new TelemetryJsonSerialiser(), "S", 0, () -> 1000L);

        Path out = exporter.export(tempDir.resolve("sub").resolve("dir"));

        assertTrue(Files.exists(out));
    }

    @Test
    void exportIfNewSkipsWhenNothingChanged() throws IOException {
        String project = tempDir.getFileName().toString();
        EventCollector collector = new EventCollector();
        collector.accept(new CompileEvent(1, 1000, project, true, null, null, null, "A.java", -1));
        SessionExporter exporter = new SessionExporter(collector, new TelemetryJsonSerialiser(),
                "S", 0, () -> 5000L);

        assertTrue(exporter.exportIfNew(tempDir).isPresent());
        assertTrue(exporter.exportIfNew(tempDir).isEmpty());

        collector.accept(new CompileEvent(2, 2000, project, true, null, null, null, "A.java", -1));
        assertTrue(exporter.exportIfNew(tempDir).isPresent());
    }

    @Test
    void eachProjectFolderGetsOnlyItsOwnEvents() throws IOException {
        Path dirA = Files.createDirectory(tempDir.resolve("projA"));
        Path dirB = Files.createDirectory(tempDir.resolve("projB"));
        EventCollector collector = new EventCollector();
        collector.accept(new CompileEvent(1, 1000, "projA", true, null, null, null, "A.java", -1));
        collector.accept(new CompileEvent(2, 2000, "projB", true, null, null, null, "B.java", -1));
        collector.accept(new CompileEvent(3, 3000, "projB", true, null, null, null, "B.java", -1));
        SessionExporter exporter = new SessionExporter(collector, new TelemetryJsonSerialiser(),
                "S", 0, () -> 5000L);

        Path a = exporter.exportIfNew(dirA).get();
        Path b = exporter.exportIfNew(dirB).get();

        assertEquals(1, JsonParser.parseString(Files.readString(a)).getAsJsonObject()
                .getAsJsonArray("events").size());
        assertEquals(2, JsonParser.parseString(Files.readString(b)).getAsJsonObject()
                .getAsJsonArray("events").size());
    }

    @Test
    void exportIfNewWritesNothingWhenThereAreNoEvents() throws IOException {
        SessionExporter exporter = new SessionExporter(new EventCollector(),
                new TelemetryJsonSerialiser(), "S", 0, () -> 1000L);

        assertTrue(exporter.exportIfNew(tempDir).isEmpty());
        try (var files = Files.list(tempDir)) {
            assertEquals(0, files.count());
        }
    }

    @Test
    void repeatedExportsOverwriteTheSameFile() throws IOException {
        EventCollector collector = new EventCollector();
        collector.accept(new CompileEvent(1, 1000, "p", true, null, null, null, "A.java", -1));
        SessionExporter exporter = new SessionExporter(collector, new TelemetryJsonSerialiser(),
                "S", 0, () -> 5000L);

        Path first = exporter.export(tempDir);
        collector.accept(new CompileEvent(2, 2000, "p", true, null, null, null, "A.java", -1));
        Path second = exporter.export(tempDir);

        assertEquals(first, second);
        try (var files = Files.list(tempDir)) {
            assertEquals(1, files.count());
        }
        JsonObject doc = JsonParser.parseString(Files.readString(second)).getAsJsonObject();
        assertEquals(2, doc.getAsJsonArray("events").size());
    }
}