package comp3607_project.pdf_exporter.pdf;


import comp3607_project.pdf_exporter.format.DurationFormatter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;

public class SummarySection extends AbstractReportSection {

    private final DurationFormatter formatter;

    public SummarySection(DurationFormatter formatter) {
        this.formatter = formatter;
    }

    @Override public int order() { return 10; }

    @Override
    public void render(PDDocument doc, PdfWriter w, ReportMetricsView v) throws IOException {
        try (PDPageContentStream cs = newPage(doc, w)) {
            w.heading(cs, "BlueJ Session Telemetry Report");
            w.paragraph(cs, "1. Active Coding Time vs. Idle Time", 13);
            w.advance(6);

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

            w.advance(20);
            w.subheading(cs, "Session KPIs");
            w.advance(6);
            w.paragraph(cs, "Total compilations:       " + v.totalCompilations(), 11);
            w.paragraph(cs, "Successful compilations:  " + v.successfulCompilations(), 11);
            w.paragraph(cs, "Failed compilations:      " + v.failedCompilations(), 11);
            w.paragraph(cs, "Total program runs:       " + v.totalRuns(), 11);
            w.paragraph(cs, "  · Completed:            " + v.completedRuns(), 11);
            w.paragraph(cs, "  · Exceptions:           " + v.exceptionRuns(), 11);
            w.paragraph(cs, "  · Terminated:           " + v.terminatedRuns(), 11);
            w.paragraph(cs, String.format("Compile success rate:     %.1f%%", v.compileSuccessRate()), 11);
            w.paragraph(cs, String.format("Compile-to-run ratio:     %.2f", v.compileToRunRatio()), 11);
        }
    }
}
