package comp3607_project.pdf_exporter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import comp3607_project.compile.CompileErrorClassifier.Category;
import comp3607_project.compile.CompileEvent;
import comp3607_project.editor_interactions.InteractionEvent;
import comp3607_project.execution.ExecutionEvent;
import comp3607_project.execution.ExecutionKind;
import comp3607_project.execution.ExecutionOutcome;
import comp3607_project.execution.InvocationInfo;
import comp3607_project.lifecycle.LifecycleEvent;
import comp3607_project.lifecycle.LifecycleEventType;
import comp3607_project.model.EventSink;
import comp3607_project.model.TelemetryEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * End-to-end check of the report: events go in through the sink, a real PDF file comes
 * out, and the numbers in that PDF can be read back.
 */
class TelemetryReportFacadeTest {

    private static void recordTypicalSession(EventSink sink) {
        sink.accept(new LifecycleEvent(LifecycleEventType.CLASS_CREATED, 1_000, "proj", "Dog",
                null));
        sink.accept(CompileEvent.error(1, 2_000, "proj", "Dog.java", 3, "expected",
                Category.SYNTAX, "';' expected"));
        sink.accept(CompileEvent.success(2, 4_000, "proj", "Dog.java"));
        sink.accept(new ExecutionEvent(ExecutionKind.MAIN_METHOD, 5_000,
                new InvocationInfo("proj", "Dog", null, "main", 0, ExecutionOutcome.COMPLETED,
                        null)));
    }

    private static String textOf(File pdf) throws IOException {
        try (PDDocument doc = PDDocument.load(pdf)) {
            return new PDFTextStripper().getText(doc);
        }
    }

    private static int pagesOf(File pdf) throws IOException {
        try (PDDocument doc = PDDocument.load(pdf)) {
            return doc.getNumberOfPages();
        }
    }

    @Test
    void exportWritesARealPdfFile(@TempDir Path dir) throws IOException {
        TelemetryReportFacade facade = new TelemetryReportFacade(0L);
        recordTypicalSession(facade.sinkForTrackers());
        facade.endSession(10_000L);
        File target = dir.resolve("report.pdf").toFile();

        File written = facade.export(target);

        assertSame(target, written);
        assertTrue(target.exists());
        assertTrue(target.length() > 0);
        byte[] header = Files.readAllBytes(target.toPath());
        assertEquals("%PDF", new String(header, 0, 4, java.nio.charset.StandardCharsets.US_ASCII));
    }

    @Test
    void reportShowsTheSessionNumbers(@TempDir Path dir) throws IOException {
        TelemetryReportFacade facade = new TelemetryReportFacade(0L);
        recordTypicalSession(facade.sinkForTrackers());
        facade.endSession(10_000L);
        File pdf = facade.export(dir.resolve("report.pdf").toFile());

        String text = textOf(pdf);

        assertTrue(text.contains("BlueJ Session Telemetry Report"));
        assertTrue(text.contains("Active Coding Time vs. Idle Time"));
        assertTrue(text.contains("Total compilations"));
        assertTrue(text.contains("Compile success rate"));
        assertTrue(text.contains("50.0%"));
        assertTrue(text.contains("2. Compilation Error Analysis"));
        assertTrue(text.contains("3. Compilation Progression & Quality"));
    }

    @Test
    void reportWithClassActivityHasAFourthTimelinePage(@TempDir Path dir) throws IOException {
        TelemetryReportFacade facade = new TelemetryReportFacade(0L);
        recordTypicalSession(facade.sinkForTrackers());
        facade.endSession(10_000L);
        File pdf = facade.export(dir.resolve("report.pdf").toFile());

        assertEquals(4, pagesOf(pdf));
        assertTrue(textOf(pdf).contains("4. Progression Timeline"));
        assertTrue(textOf(pdf).contains("CLASS_CREATED: Dog"));
    }

    @Test
    void reportWithoutClassActivityHasThreePages(@TempDir Path dir) throws IOException {
        TelemetryReportFacade facade = new TelemetryReportFacade(0L);
        facade.sinkForTrackers().accept(CompileEvent.success(1, 2_000, "proj", "Dog.java"));
        facade.endSession(10_000L);

        File pdf = facade.export(dir.resolve("report.pdf").toFile());

        assertEquals(3, pagesOf(pdf));
    }

    @Test
    void emptySessionStillProducesAValidReport(@TempDir Path dir) throws IOException {
        TelemetryReportFacade facade = new TelemetryReportFacade(0L);
        facade.endSession(10_000L);

        File pdf = facade.export(dir.resolve("empty.pdf").toFile());

        assertEquals(3, pagesOf(pdf));
        assertTrue(textOf(pdf).contains("BlueJ Session Telemetry Report"));
    }

    @Test
    void eventsAreAlsoForwardedToTheDelegateSink() {
        List<TelemetryEvent> forwarded = new ArrayList<>();
        TelemetryReportFacade facade = new TelemetryReportFacade(0L, forwarded::add);
        TelemetryEvent event = CompileEvent.success(1, 1_000, "proj", "Dog.java");

        facade.sinkForTrackers().accept(event);

        assertEquals(List.of(event), forwarded);
    }

    @Test
    void interactionObserverFeedsTheReport(@TempDir Path dir) throws IOException {
        TelemetryReportFacade facade = new TelemetryReportFacade(0L);
        assertNotNull(facade.interactionObserver());
        facade.interactionObserver().onInteraction(new InteractionEvent(
                InteractionEvent.Kind.KEY, Instant.ofEpochMilli(30_000), 0, "A", false));
        facade.endSession(10_000L);

        File pdf = facade.export(dir.resolve("report.pdf").toFile());

        // the key press at 30s widens the 10s session to 30s, so 25s of it is idle
        assertTrue(textOf(pdf).contains("25s (83.3%)"));
    }

    @Test
    void customIdleCalculatorAndHistogramBuilderAreUsed(@TempDir Path dir) throws IOException {
        TelemetryReportFacade facade = new TelemetryReportFacade(0L,
                (interactions, model, start, end) -> 90_000L,
                timestamps -> new LinkedHashMap<String, Integer>(Map.of("custom", 1)),
                ZoneId.of("UTC"));
        facade.endSession(100_000L);

        File pdf = facade.export(dir.resolve("report.pdf").toFile());

        assertTrue(textOf(pdf).contains("1m 30s (90.0%)"));
    }
}
