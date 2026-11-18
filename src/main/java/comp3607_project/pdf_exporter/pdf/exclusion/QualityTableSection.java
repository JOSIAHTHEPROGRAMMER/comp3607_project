package comp3607_project.pdf_exporter.pdf;


import comp3607_project.pdf_exporter.charts.ChartRenderer;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.awt.image.BufferedImage;
import java.io.IOException;


public class QualityTableSection extends AbstractReportSection {

    private final ChartRenderer lineChart;

    public QualityTableSection(ChartRenderer lineChart) {
        this.lineChart = lineChart;
    }

    @Override
    public int order() {
        return 30;
    }

    @Override
    public void render(PDDocument doc, PdfWriter w, ReportMetricsView v) throws IOException {
        try (PDPageContentStream cs = newPage(doc, w)) {
            w.heading(cs, "3. Compilation Progression & Quality");
            w.advance(6);

            BufferedImage chart = lineChart.render();
            w.drawImage(cs, doc, chart, 480f);

            w.subheading(cs, "Compile Quality Metrics");
            w.advance(6);

            String[][] rows = {
                    {"Metric",                 "Value"},
                    {"Total Compilations",     String.valueOf(v.totalCompilations())},
                    {"Successful",             String.valueOf(v.successfulCompilations())},
                    {"Failed",                 String.valueOf(v.failedCompilations())},
                    {"Compile Success Rate",   String.format("%.1f%%", v.compileSuccessRate())},
                    {"Total Program Runs",     String.valueOf(v.totalRuns())},
                    {"Compile-to-Run Ratio",   String.format("%.2f", v.compileToRunRatio())}
            };

            w.table(cs, rows, 260f, 120f);
        }
    }
}