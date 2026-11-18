package comp3607_project.pdf_exporter.pdf;


import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.IOException;

public interface ReportSection {
    void render(PDDocument doc, PdfWriter writer, ReportMetricsView view) throws IOException;
    default int order() { return 0; }
    default boolean isApplicable(ReportMetricsView view) { return true; }
}
