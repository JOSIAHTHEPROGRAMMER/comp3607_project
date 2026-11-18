package comp3607_project.pdf_exporter.pdf;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;

public class ParagraphCommand implements ReportCommand {

    private final String text;
    private final int fontSize;

    public ParagraphCommand(String text) { this(text, 11); }
    public ParagraphCommand(String text, int fontSize) {
        this.text = text; this.fontSize = fontSize;
    }

    @Override
    public void execute(PDDocument doc, PDPageContentStream cs,
                        PdfWriter w, ReportMetricsView v) throws IOException {
        w.paragraph(cs, text, fontSize);
    }
}