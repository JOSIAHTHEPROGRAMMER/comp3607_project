package comp3607_project.json;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import comp3607_project.model.TelemetryEvent;

/**
 * Writes the session PDF the same way SessionExporter writes the JSON:
 * take the events from EventCollector, reduce them with SessionSummary /
 * SummaryBuilder, and render the result.
 *
 * Deliberately PDFBox-only: no JFreeChart, no java.awt image rendering, no
 * separate journals. Charts are drawn as plain rectangles. Always produces
 * exactly 2 pages (summary + event log), which satisfies a 2-4 page limit.
 */
public class SessionPdfExporter {

    private static final float MARGIN = 50f;
    private static final float PAGE_W = PDRectangle.A4.getWidth();
    private static final float PAGE_H = PDRectangle.A4.getHeight();
    private static final int MAX_EVENT_ROWS = 40;
    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter STAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private final List<EventMapper> mappers;

    public SessionPdfExporter() {
        this(EventMappers.defaults());
    }

    public SessionPdfExporter(List<EventMapper> mappers) {
        this.mappers = List.copyOf(mappers);
    }

    public Path export(List<TelemetryEvent> events, String studentId,
                       long startMillis, long endMillis, Path out) throws IOException {
        SessionSummary s = SessionSummary.from(events, mappers); // same reduction as the JSON
        Path parent = out.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);

        try (PDDocument doc = new PDDocument()) {
            summaryPage(doc, s, studentId, startMillis, endMillis);
            eventLogPage(doc, s, events);
            doc.save(out.toFile());
        }
        return out;
    }

    // ---------------------------------------------------------------- page 1

    private void summaryPage(PDDocument doc, SessionSummary s, String studentId,
                             long start, long end) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            float y = PAGE_H - MARGIN;
            y = text(cs, "BlueJ Telemetry Report", PDType1Font.HELVETICA_BOLD, 20, y);
            y -= 6;

            long secs = Math.max(0L, (end - start) / 1000L);
            y = table(cs, y, new String[][]{
                    {"Session", "Value"},
                    {"Student ID", studentId},
                    {"Started", STAMP.format(Instant.ofEpochMilli(start))},
                    {"Ended", STAMP.format(Instant.ofEpochMilli(end))},
                    {"Duration", duration(secs)},
                    {"Total compilations", String.valueOf(s.totalCompilations())},
                    {"Successful compilations", String.valueOf(s.successfulCompilations())},
                    {"Failed compilations", String.valueOf(s.failedCompilations())},
                    {"Total runs", String.valueOf(s.totalRuns())},
                    {"Runtime exceptions", String.valueOf(s.runtimeExceptions())},
            });

            y -= 10;
            y = text(cs, "Compilation errors by category", PDType1Font.HELVETICA_BOLD, 13, y);
            y = barChart(cs, y, s.errorCountsByCategory());

            y -= 10;
            y = text(cs, "Compile outcome", PDType1Font.HELVETICA_BOLD, 13, y);
            Map<String, Integer> outcome = new java.util.LinkedHashMap<>();
            outcome.put("Success", s.successfulCompilations());
            outcome.put("Failed", s.failedCompilations());
            barChart(cs, y, outcome);
        }
    }

    // ---------------------------------------------------------------- page 2

    private void eventLogPage(PDDocument doc, SessionSummary s,
                              List<TelemetryEvent> events) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            float y = PAGE_H - MARGIN;
            y = text(cs, "Time between compilations", PDType1Font.HELVETICA_BOLD, 13, y);
            List<Long> iv = s.compileIntervalsMillis();
            if (iv.isEmpty()) {
                y = text(cs, "Fewer than two compilation attempts were recorded.",
                        PDType1Font.HELVETICA, 10, y);
            } else {
                long sum = 0, max = 0;
                for (long v : iv) { sum += v; max = Math.max(max, v); }
                y = text(cs, "Average gap: " + duration(sum / iv.size() / 1000)
                        + "   Longest gap: " + duration(max / 1000), PDType1Font.HELVETICA, 10, y);
            }

            y -= 12;
            y = text(cs, "Event log", PDType1Font.HELVETICA_BOLD, 13, y);
            int shown = Math.min(events.size(), MAX_EVENT_ROWS);
            for (int i = 0; i < shown; i++) {
                TelemetryEvent e = events.get(i);
                String line = TIME.format(Instant.ofEpochMilli(e.getTimestamp()))
                        + "  " + e.getEventType()
                        + (e.getTarget() == null ? "" : "  " + e.getTarget());
                y = text(cs, line, PDType1Font.HELVETICA, 9, y);
            }
            if (events.size() > shown) {
                text(cs, "... " + (events.size() - shown) + " more events (see the JSON file)",
                        PDType1Font.HELVETICA_OBLIQUE, 9, y);
            } else if (events.isEmpty()) {
                text(cs, "No events recorded.", PDType1Font.HELVETICA_OBLIQUE, 9, y);
            }
        }
    }

    // --------------------------------------------------------------- drawing

    private float text(PDPageContentStream cs, String t, PDFont font, int size, float y)
            throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(MARGIN, y - size - 2);
        cs.showText(safe(t, font));
        cs.endText();
        return y - size - 4;
    }

    private float table(PDPageContentStream cs, float y, String[][] rows) throws IOException {
        float rowH = 20, c1 = 200, w = 380;
        for (int r = 0; r < rows.length; r++) {
            float top = y - r * rowH;
            cs.setNonStrokingColor(r == 0 ? new Color(46, 71, 115)
                    : r % 2 == 0 ? new Color(237, 242, 250) : Color.WHITE);
            cs.addRect(MARGIN, top - rowH, w, rowH);
            cs.fill();
            cs.setStrokingColor(new Color(153, 153, 153));
            cs.addRect(MARGIN, top - rowH, w, rowH);
            cs.stroke();
            PDFont f = r == 0 ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA;
            cs.setNonStrokingColor(r == 0 ? Color.WHITE : Color.BLACK);
            for (int c = 0; c < 2; c++) {
                cs.beginText();
                cs.setFont(f, 10);
                cs.newLineAtOffset(MARGIN + 6 + (c == 0 ? 0 : c1), top - rowH + 6);
                cs.showText(safe(rows[r][c], f));
                cs.endText();
            }
        }
        cs.setNonStrokingColor(Color.BLACK);
        return y - rows.length * rowH;
    }

    /** Horizontal bar chart from plain rectangles. */
    private float barChart(PDPageContentStream cs, float y, Map<String, Integer> data)
            throws IOException {
        int max = 1;
        for (int v : data.values()) max = Math.max(max, v);
        float labelW = 130, maxBar = 300, barH = 12;
        for (Map.Entry<String, Integer> e : data.entrySet()) {
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 9);
            cs.setNonStrokingColor(Color.BLACK);
            cs.newLineAtOffset(MARGIN, y - barH);
            cs.showText(safe(e.getKey(), PDType1Font.HELVETICA));
            cs.endText();

            float len = maxBar * e.getValue() / max;
            cs.setNonStrokingColor(new Color(70, 120, 190));
            if (len > 0) {
                cs.addRect(MARGIN + labelW, y - barH - 1, len, barH);
                cs.fill();
            }
            cs.setNonStrokingColor(Color.BLACK);
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 9);
            cs.newLineAtOffset(MARGIN + labelW + len + 4, y - barH);
            cs.showText(String.valueOf(e.getValue()));
            cs.endText();
            y -= barH + 6;
        }
        return y;
    }

    private static String duration(long secs) {
        return (secs / 3600) + "h " + ((secs % 3600) / 60) + "m " + (secs % 60) + "s";
    }

    /** PDFBox's built-in fonts only cover WinAnsi; replace anything else with '?'. */
    private static String safe(String t, PDFont font) {
        if (t == null) return "";
        StringBuilder sb = new StringBuilder(t.length());
        for (int i = 0; i < t.length(); ) {
            int cp = t.codePointAt(i);
            i += Character.charCount(cp);
            String ch = Character.isISOControl(cp) ? " " : new String(Character.toChars(cp));
            try { font.encode(ch); sb.append(ch); } catch (Exception ex) { sb.append('?'); }
        }
        return sb.toString();
    }
}