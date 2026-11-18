package comp3607_project.pdf_exporter;

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
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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

    private static long imagesOnPage(File pdf, int pageIndex) throws IOException {
        try (PDDocument doc = PDDocument.load(pdf)) {
            var resources = doc.getPage(pageIndex).getResources();
            long imageCount = 0;
            for (var name : resources.getXObjectNames()) {
                if (resources.getXObject(name) instanceof PDImageXObject) {
                    imageCount++;
                }
            }
            return imageCount;
        }
    }

    private static float yOfText(File pdf, String text) throws IOException {
        try (PDDocument doc = PDDocument.load(pdf)) {
            class PositionStripper extends PDFTextStripper {
                private float y = Float.NaN;

                PositionStripper() throws IOException {}

                @Override
                protected void writeString(String line, List<TextPosition> positions)
                        throws IOException {
                    if (line.contains(text) && !positions.isEmpty()) {
                        y = positions.get(0).getYDirAdj();
                    }
                    super.writeString(line, positions);
                }
            }

            PositionStripper stripper = new PositionStripper();
            stripper.getText(doc);
            return stripper.y;
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

    /*@Test
    void exportCreatesMissingParentDirectories(@TempDir Path dir) throws IOException {
        TelemetryReportFacade facade = new TelemetryReportFacade(0L);
        facade.endSession(10_000L);
        File target = dir.resolve("missing").resolve("nested").resolve("report.pdf").toFile();

        File written = facade.export(target);

        assertSame(target, written);
        assertTrue(target.isFile());
        assertTrue(target.length() > 0);
    }*/

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
    void reportIncludesRequestedChartsAndQualityTable(@TempDir Path dir) throws IOException {
        TelemetryReportFacade facade = new TelemetryReportFacade(0L);
        recordTypicalSession(facade.sinkForTrackers());
        facade.endSession(10_000L);
        File pdf = facade.export(dir.resolve("report.pdf").toFile());

        assertEquals(2, imagesOnPage(pdf, 1));
        assertEquals(1, imagesOnPage(pdf, 2));
        String text = textOf(pdf);
        assertTrue(text.contains("Compile Success Rate"));
        assertTrue(text.contains("Compile-to-Run Ratio"));
        assertTrue(yOfText(pdf, "Metric") - yOfText(pdf, "Compile Quality Metrics") >= 20f);
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

    /*@Test
    void interactionTelemetryThroughTheTrackerSinkIsAnalyzed(@TempDir Path dir) throws IOException {
        TelemetryReportFacade facade = new TelemetryReportFacade(0L);
        InteractionEvent event = new InteractionEvent(
                InteractionEvent.Kind.KEY, Instant.ofEpochMilli(30_000), 0, "A", false);
        facade.sinkForTrackers().accept(new InteractionTelemetryEvent(event, "proj"));
        facade.endSession(10_000L);

        File pdf = facade.export(dir.resolve("nested/report.pdf").toFile());

        assertTrue(pdf.isFile());
        assertTrue(textOf(pdf).contains("25s (83.3%)"));
    }*/

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
