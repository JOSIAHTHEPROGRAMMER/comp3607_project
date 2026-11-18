package comp3607_project.pdf_exporter.pdf;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;
import java.util.List;


public class TimelineSection extends AbstractReportSection {

    private static final int MAX_LINES = 30;

    @Override
    public int order() {
        return 40;
    }

    @Override
    public boolean isApplicable(ReportMetricsView view) {
        return view.timelineEntries() != null && !view.timelineEntries().isEmpty();
    }

    @Override
    public void render(PDDocument doc, PdfWriter w, ReportMetricsView v) throws IOException {
        try (PDPageContentStream cs = newPage(doc, w)) {
            w.heading(cs, "4. Progression Timeline");
            w.advance(6);
            w.paragraph(cs, "Class creations, modifications, and renames during the session:", 11);
            w.advance(10);

            List<String> entries = v.timelineEntries();
            int shown = 0;
            for (String entry : entries) {
                if (shown >= MAX_LINES) break;
                if (w.cursor() < w.margin() + 40f) break;
                w.paragraph(cs, "• " + entry, 10);
                shown++;
            }

            if (entries.size() > shown) {
                w.advance(6);
                w.paragraph(cs, "... and " + (entries.size() - shown) + " more entries.", 10);
            }
        }
    }
}
