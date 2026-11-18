package comp3607_project.pdf_exporter.pdf;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import comp3607_project.pdf_exporter.charts.ChartRenderer;
import comp3607_project.pdf_exporter.format.DurationFormatter;

/**
 * Fluent PDF builder. Collects commands, then writes them in order on build().
 * Pages are opened implicitly (first command, or whenever newPage() is called).
 */
public class ReportBuilder {

    private static final int MIN_PAGES = 2;
    private static final int MAX_PAGES = 4;

    private final PdfWriter writer;
    private final ReportMetricsView view;
    private final DurationFormatter formatter;
    private final List<ReportCommand> commands = new ArrayList<>();

    public ReportBuilder(PdfWriter writer,
                         ReportMetricsView view,
                         DurationFormatter formatter) {
        this.writer = writer;
        this.view = view;
        this.formatter = formatter;
    }

    // ---------- fluent API --------------------------------------------------

    public ReportBuilder page() {
        commands.add(new PageBreakCommand());
        return this;
    }

    public ReportBuilder title(String text) {
        commands.add(new TitleCommand(text));
        return this;
    }

    public ReportBuilder paragraph(String text) {
        commands.add(new ParagraphCommand(text));
        return this;
    }

    public ReportBuilder paragraph(String text, int size) {
        commands.add(new ParagraphCommand(text, size));
        return this;
    }

    public ReportBuilder chart(ChartRenderer renderer) {
        commands.add(new ImageCommand(renderer));
        return this;
    }

    public ReportBuilder chart(ChartRenderer renderer, float maxWidth) {
        commands.add(new ImageCommand(renderer, maxWidth));
        return this;
    }

    public ReportBuilder table(String[][] rows) {
        commands.add(new TableCommand(rows));
        return this;
    }

    public ReportBuilder activeIdleBar() {
        commands.add(new ActiveIdleBarCommand(formatter));
        return this;
    }

    public ReportBuilder custom(ReportCommand command) {
        commands.add(command);
        return this;
    }

    // ---------- build ------------------------------------------------------

    public File build(File output) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPageContentStream cs = openFirstPage(doc);
            try {
                for (ReportCommand cmd : commands) {
                    if (cmd instanceof PageBreakCommand) {
                        cs.close();
                        cs = openNextPage(doc);
                        continue;
                    }
                    cmd.execute(doc, cs, writer, view);
                }
            } finally {
                cs.close();
            }

            // Enforce the 2–4 page invariant.
            if (doc.getNumberOfPages() < MIN_PAGES) {
                padToMinimum(doc);
            }
            if (doc.getNumberOfPages() > MAX_PAGES) {
                throw new IOException("Report exceeded " + MAX_PAGES
                        + " pages (" + doc.getNumberOfPages() + ").");
            }

            doc.save(output);
        }
        return output;
    }

    // ---------- helpers ----------------------------------------------------

    private PDPageContentStream openFirstPage(PDDocument doc) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        writer.beginPage();
        return new PDPageContentStream(doc, page);
    }

    private PDPageContentStream openNextPage(PDDocument doc) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        writer.beginPage();
        return new PDPageContentStream(doc, page);
    }

    private void padToMinimum(PDDocument doc) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        writer.beginPage();
        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            writer.heading(cs, "Session Notes");
            writer.advance(12);
            writer.paragraph(cs,
                    "The recorded session was too short to generate additional analytics.", 11);
            writer.paragraph(cs,
                    "All metrics above reflect the available telemetry.", 11);
        }
    }
}