package comp3607_project.pdf_exporter.pdf;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import comp3607_project.pdf_exporter.charts.ChartRenderer;

public class ImageCommand implements ReportCommand {

    private final ChartRenderer renderer;
    private final float maxWidth;

    public ImageCommand(ChartRenderer renderer) { this(renderer, 480f); }
    public ImageCommand(ChartRenderer renderer, float maxWidth) {
        this.renderer = renderer; this.maxWidth = maxWidth;
    }

    @Override
    public void execute(PDDocument doc, PDPageContentStream cs,
                        PdfWriter w, ReportMetricsView v) throws IOException {
        w.drawImage(cs, doc, renderer.render(), maxWidth);
    }
}