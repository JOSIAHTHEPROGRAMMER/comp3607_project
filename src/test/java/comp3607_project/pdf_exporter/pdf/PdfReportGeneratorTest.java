package comp3607_project.pdf_exporter.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import comp3607_project.pdf_exporter.SampleMetrics;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;

class PdfReportGeneratorTest {

    private final ReportMetricsView view = ReportMetricsView.of(SampleMetrics.typical());

    /** A section that adds blank pages and notes in a log that it was rendered. */
    private static ReportSection section(int order, boolean applicable, int pages,
            List<Integer> log) {
        return new ReportSection() {
            @Override
            public void render(PDDocument doc, PdfWriter writer, ReportMetricsView v) {
                log.add(order);
                for (int i = 0; i < pages; i++) {
                    doc.addPage(new PDPage());
                }
            }

            @Override
            public int order() {
                return order;
            }

            @Override
            public boolean isApplicable(ReportMetricsView v) {
                return applicable;
            }
        };
    }

    private int pagesAfter(List<ReportSection> sections) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            new PdfReportGenerator(sections, new PdfBoxWriter()).writeTo(doc, view);
            return doc.getNumberOfPages();
        }
    }

    @Test
    void sectionsAreRenderedInTheirOrderNotTheOrderTheyWereGiven() throws IOException {
        List<Integer> log = new ArrayList<>();
        List<ReportSection> sections = List.of(section(30, true, 1, log),
                section(10, true, 1, log), section(20, true, 1, log));

        pagesAfter(sections);

        assertEquals(List.of(10, 20, 30), log);
    }

    @Test
    void sectionsThatAreNotApplicableAreSkipped() throws IOException {
        List<Integer> log = new ArrayList<>();
        List<ReportSection> sections = List.of(section(10, true, 2, log),
                section(20, false, 2, log));

        int pages = pagesAfter(sections);

        assertEquals(List.of(10), log);
        assertEquals(2, pages);
    }

    @Test
    void aOnePageReportIsPaddedToTwoPages() throws IOException {
        List<Integer> log = new ArrayList<>();

        assertEquals(2, pagesAfter(List.of(section(10, true, 1, log))));
    }

    @Test
    void aReportWithNoSectionsIsStillTwoPages() throws IOException {
        assertEquals(2, pagesAfter(List.of()));
    }

    @Test
    void fourPagesIsAllowed() throws IOException {
        List<Integer> log = new ArrayList<>();

        assertEquals(4, pagesAfter(List.of(section(10, true, 4, log))));
    }

    @Test
    void moreThanFourPagesIsRejected() {
        List<Integer> log = new ArrayList<>();
        List<ReportSection> sections = List.of(section(10, true, 5, log));

        IOException error = assertThrows(IOException.class, () -> pagesAfter(sections));

        assertTrue(error.getMessage().contains("exceeded 4 pages"));
    }
}
