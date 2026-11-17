package comp3607_project.pdf_exporter.pdf;


import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class PdfReportGenerator {

    private static final int MIN_PAGES = 2;
    private static final int MAX_PAGES = 4;

    private final List<ReportSection> sections;
    private final PdfWriter writer;

    public PdfReportGenerator(List<ReportSection> sections, PdfWriter writer) {
        this.sections = sections.stream()
                .sorted(Comparator.comparingInt(ReportSection::order))
                .toList();
        this.writer = writer;
    }

    public void writeTo(PDDocument doc, ReportMetricsView view) throws IOException {
        for (ReportSection s : sections) {
            if (s.isApplicable(view)) s.render(doc, writer, view);
        }
        int pages = doc.getNumberOfPages();
        while (pages < MIN_PAGES) {
            new SessionEndSection().render(doc, writer, view);
            pages = doc.getNumberOfPages();
        }
        if (doc.getNumberOfPages() > MAX_PAGES) {
            throw new IOException("Report exceeded " + MAX_PAGES
                    + " pages (" + doc.getNumberOfPages() + "). Tighten a section's output.");
        }
    }
}
