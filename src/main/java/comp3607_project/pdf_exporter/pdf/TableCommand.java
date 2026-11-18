package comp3607_project.pdf_exporter.pdf;


import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

public class TableCommand implements ReportCommand {

    private final String[][] rows;
    private final float col1;
    private final float col2;

    public TableCommand(String[][] rows) { this(rows, 260f, 120f); }
    public TableCommand(String[][] rows, float col1, float col2) {
        this.rows = rows; this.col1 = col1; this.col2 = col2;
    }

    @Override
    public void execute(PDDocument doc, PDPageContentStream cs,
                        PdfWriter w, ReportMetricsView v) throws IOException {
        w.table(cs, rows, col1, col2);
    }
}
