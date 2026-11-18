package comp3607_project.pdf_exporter.pdf;


import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;


@FunctionalInterface
public interface ReportCommand {
    void execute(PDDocument doc,
                 PDPageContentStream stream,
                 PdfWriter writer,
                 ReportMetricsView view) throws IOException;
}