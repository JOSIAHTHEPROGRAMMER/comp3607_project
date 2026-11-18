package comp3607_project.pdf_exporter.pdf;

import comp3607_project.pdf_exporter.charts.ChartRenderer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;

public class ChartsSection extends AbstractReportSection {

    private final ChartRenderer pieChart;
    private final ChartRenderer histogram;

    public ChartsSection(ChartRenderer pieChart, ChartRenderer histogram) {
        this.pieChart = pieChart;
        this.histogram = histogram;
    }

    @Override
    public int order() {
        return 20;
    }

    @Override
    public void render(PDDocument doc, PdfWriter writer, ReportMetricsView view) throws IOException {
        try (PDPageContentStream cs = newPage(doc, writer)) {
            writer.heading(cs, "2. Compilation Error Analysis");
            writer.advance(10);
            writer.drawImage(cs, doc, pieChart.render(), 480f);
            writer.advance(10);
            writer.drawImage(cs, doc, histogram.render(), 480f);
        }
    }
}
