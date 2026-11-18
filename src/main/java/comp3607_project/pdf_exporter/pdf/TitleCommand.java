package comp3607_project.pdf_exporter.pdf;



import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

public class TitleCommand implements ReportCommand {

    private final String text;

    public TitleCommand(String text) { this.text = text; }

    @Override
    public void execute(PDDocument doc, PDPageContentStream cs,
                        PdfWriter w, ReportMetricsView v) throws IOException {
        w.heading(cs, text);
    }
}