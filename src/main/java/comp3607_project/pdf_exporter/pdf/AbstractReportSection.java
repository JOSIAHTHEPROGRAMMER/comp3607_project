package comp3607_project.pdf_exporter.pdf;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.IOException;

public abstract class AbstractReportSection implements ReportSection {

    protected PDPageContentStream newPage(PDDocument doc, PdfWriter writer) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        writer.beginPage();
        return new PDPageContentStream(doc, page);
    }
}
