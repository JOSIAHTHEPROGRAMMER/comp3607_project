package comp3607_project.pdf_exporter.pdf;


import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * Writes nothing itself — the builder opens a new page before executing it.
 * Kept as a command so page breaks are part of the same queued script.
 */
public class PageBreakCommand implements ReportCommand {
    @Override
    public void execute(PDDocument doc, PDPageContentStream cs,
                        PdfWriter w, ReportMetricsView v) throws IOException {
        // nothing — the builder already handled the page switch
    }

    static PDPage newPage(PDDocument doc) {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        return page;
    }
}