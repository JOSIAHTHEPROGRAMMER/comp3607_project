package comp3607_project.pdf_exporter.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;

public final class SessionEndSection extends AbstractReportSection {

    @Override
    public int order() {
        return 50;
    }

    @Override
    public void render(PDDocument doc, PdfWriter writer, ReportMetricsView view) throws IOException {
        try (PDPageContentStream cs = newPage(doc, writer)) {
            writer.heading(cs, "Session Report");
            writer.paragraph(cs, "End of report.", 12);
        }
    }
}
