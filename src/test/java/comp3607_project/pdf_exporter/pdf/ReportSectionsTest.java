package comp3607_project.pdf_exporter.pdf;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import comp3607_project.pdf_exporter.SampleMetrics;
import comp3607_project.pdf_exporter.charts.ChartRenderer;
import comp3607_project.pdf_exporter.format.HumanReadableDurationFormatter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

class ReportSectionsTest {

    private static final ChartRenderer SMALL_CHART =
            () -> new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);

    private static ReportMetricsView view(comp3607_project.pdf_exporter.metrics.ReportMetrics m) {
        return ReportMetricsView.of(m);
    }

    private static String render(ReportSection section, ReportMetricsView view, int expectedPages)
            throws IOException {
        try (PDDocument doc = new PDDocument()) {
            section.render(doc, new PdfBoxWriter(), view);
            assertEquals(expectedPages, doc.getNumberOfPages());
            return new PDFTextStripper().getText(doc);
        }
    }

    @Test
    void summarySectionShowsActiveIdleTimeAndKpis() throws IOException {
        SummarySection section = new SummarySection(new HumanReadableDurationFormatter());

        String text = render(section, view(SampleMetrics.typical()), 1);

        assertTrue(text.contains("BlueJ Session Telemetry Report"));
        assertTrue(text.contains("1m 30s (75.0%)"));
        assertTrue(text.contains("30s (25.0%)"));
        assertTrue(text.contains("Total compilations"));
        assertTrue(text.contains("Failed compilations"));
        assertTrue(text.contains("50.0%"));
    }

    @Test
    void summarySectionHandlesASessionWithNoTimeAtAll() {
        SummarySection section = new SummarySection(new HumanReadableDurationFormatter());

        assertDoesNotThrow(() -> render(section, view(SampleMetrics.empty()), 1));
    }

    @Test
    void timelineSectionAppliesOnlyWhenThereAreEntries() {
        TimelineSection section = new TimelineSection();

        assertTrue(section.isApplicable(view(SampleMetrics.withTimeline(List.of("entry")))));
        assertFalse(section.isApplicable(view(SampleMetrics.withTimeline(List.of()))));
        assertFalse(section.isApplicable(view(SampleMetrics.withTimeline(null))));
    }

    @Test
    void timelineSectionListsTheEntries() throws IOException {
        List<String> entries = List.of("10:00:00 - CLASS_CREATED: Dog",
                "10:05:00 - CLASS_MODIFIED: Dog");

        String text = render(new TimelineSection(), view(SampleMetrics.withTimeline(entries)), 1);

        assertTrue(text.contains("4. Progression Timeline"));
        assertTrue(text.contains("CLASS_CREATED: Dog"));
        assertTrue(text.contains("CLASS_MODIFIED: Dog"));
        assertFalse(text.contains("more entries"));
    }

    @Test
    void timelineSectionShowsAtMostThirtyEntriesAndSaysHowManyWereLeftOut() throws IOException {
        List<String> entries = new ArrayList<>();
        for (int i = 1; i <= 40; i++) {
            entries.add("entry " + i);
        }

        String text = render(new TimelineSection(), view(SampleMetrics.withTimeline(entries)), 1);

        assertTrue(text.contains("entry 30"));
        assertFalse(text.contains("entry 31"));
        assertTrue(text.contains("... and 10 more entries."));
    }

    @Test
    void chartsSectionDrawsBothChartsOnOnePage() throws IOException {
        ChartsSection section = new ChartsSection(SMALL_CHART, SMALL_CHART);

        String text = render(section, view(SampleMetrics.typical()), 1);

        assertTrue(text.contains("2. Compilation Error Analysis"));
    }

    @Test
    void qualityTableSectionShowsTheChartAndTheMetricsTable() throws IOException {
        QualityTableSection section = new QualityTableSection(SMALL_CHART);

        String text = render(section, view(SampleMetrics.typical()), 1);

        assertTrue(text.contains("3. Compilation Progression & Quality"));
        assertTrue(text.contains("Compile Quality Metrics"));
        assertTrue(text.contains("Total Compilations"));
        assertTrue(text.contains("Compile Success Rate"));
        assertTrue(text.contains("50.0%"));
        assertTrue(text.contains("Compile-to-Run Ratio"));
    }

    @Test
    void sessionEndSectionAddsAClosingPage() throws IOException {
        String text = render(new SessionEndSection(), view(SampleMetrics.empty()), 1);

        assertTrue(text.contains("Session Report"));
        assertTrue(text.contains("End of report."));
    }

    @Test
    void sectionsAreOrderedSummaryChartsQualityTimelineEnd() {
        assertEquals(10, new SummarySection(new HumanReadableDurationFormatter()).order());
        assertEquals(20, new ChartsSection(SMALL_CHART, SMALL_CHART).order());
        assertEquals(30, new QualityTableSection(SMALL_CHART).order());
        assertEquals(40, new TimelineSection().order());
        assertEquals(50, new SessionEndSection().order());
    }

    @Test
    void sectionsAreApplicableByDefault() {
        ReportMetricsView v = view(SampleMetrics.typical());

        assertTrue(new SummarySection(new HumanReadableDurationFormatter()).isApplicable(v));
        assertTrue(new ChartsSection(SMALL_CHART, SMALL_CHART).isApplicable(v));
        assertTrue(new QualityTableSection(SMALL_CHART).isApplicable(v));
        assertTrue(new SessionEndSection().isApplicable(v));
    }

    @Test
    void factoryBuildsTheFourStandardSectionsInOrder() {
        List<ReportSection> sections = ReportSectionFactory.create(
                new HumanReadableDurationFormatter(), SMALL_CHART, SMALL_CHART, SMALL_CHART);

        assertEquals(4, sections.size());
        assertInstanceOf(SummarySection.class, sections.get(0));
        assertInstanceOf(ChartsSection.class, sections.get(1));
        assertInstanceOf(QualityTableSection.class, sections.get(2));
        assertInstanceOf(TimelineSection.class, sections.get(3));
    }
}
