package comp3607_project.pdf_exporter.pdf;


import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import comp3607_project.pdf_exporter.format.DurationFormatter;

/**
 * Draws the stacked active-vs-idle bar plus the two labelled lines beneath it.
 */
public class ActiveIdleBarCommand implements ReportCommand {

    private final DurationFormatter formatter;

    public ActiveIdleBarCommand(DurationFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public void execute(PDDocument doc, PDPageContentStream cs,
                        PdfWriter w, ReportMetricsView v) throws IOException {

        long total = Math.max(1, v.activeTimeMillis() + v.idleTimeMillis());
        float activePct = (float) v.activeTimeMillis() * 100f / total;
        float idlePct   = 100f - activePct;

        float barWidth = 400f, barHeight = 18f;
        float barY = w.cursor() - barHeight;

        cs.setNonStrokingColor(0.20f, 0.55f, 0.85f);
        cs.addRect(w.margin(), barY, barWidth * activePct / 100f, barHeight);
        cs.fill();

        cs.setNonStrokingColor(0.85f, 0.30f, 0.30f);
        cs.addRect(w.margin() + barWidth * activePct / 100f, barY,
                   barWidth * idlePct / 100f, barHeight);
        cs.fill();
        cs.setNonStrokingColor(0, 0, 0);
        w.advance(barHeight + 16);

        w.paragraph(cs, String.format("Active coding: %s (%.1f%%)",
                formatter.format(v.activeTimeMillis()), activePct), 12);
        w.paragraph(cs, String.format("Idle:          %s (%.1f%%)",
                formatter.format(v.idleTimeMillis()), idlePct), 12);
    }
}