package comp3607_project.pdf_exporter.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.image.BufferedImage;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

/** Checks how each drawing call moves the writer's cursor down the page. */
class PdfBoxWriterTest {

    private static final float DELTA = 0.001f;

    private final PdfBoxWriter writer = new PdfBoxWriter();

    @Test
    void marginAndContentTopFollowTheA4Page() {
        assertEquals(50f, writer.margin(), DELTA);
        assertEquals(PDRectangle.A4.getHeight() - 50f, writer.contentTop(), DELTA);
    }

    @Test
    void beginPageMovesTheCursorToTheTop() {
        writer.advance(100);

        writer.beginPage();

        assertEquals(writer.contentTop(), writer.cursor(), DELTA);
    }

    @Test
    void advanceMovesTheCursorDown() {
        writer.beginPage();

        writer.advance(25f);

        assertEquals(writer.contentTop() - 25f, writer.cursor(), DELTA);
    }

    @Test
    void textLinesEachTakeSixteenPoints() throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            writer.beginPage();
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                writer.heading(cs, "Heading");
                writer.subheading(cs, "Subheading");
                writer.paragraph(cs, "Paragraph", 11);
            }

            assertEquals(writer.contentTop() - 48f, writer.cursor(), DELTA);
        }
    }

    @Test
    void imagesTakeTheirScaledHeightPlusTwentyPoints() throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            writer.beginPage();
            BufferedImage image = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                // 200pt wide from a 100x50 image is 100pt tall
                writer.drawImage(cs, doc, image, 200f);
            }

            assertEquals(writer.contentTop() - 120f, writer.cursor(), DELTA);
        }
    }

    @Test
    void tablesTakeTwentyTwoPointsPerRowPlusTen() throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            writer.beginPage();
            String[][] rows = {{"Metric", "Value"}, {"Total", "4"}, {"Failed", "2"}};
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                writer.table(cs, rows, 260f, 120f);
            }

            assertEquals(writer.contentTop() - (3 * 22f + 10f), writer.cursor(), DELTA);
        }
    }

    @Test
    void unsupportedUnicodeDoesNotPreventPdfTextGeneration() throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            writer.beginPage();
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                writer.paragraph(cs, "Telemetry 😀 value", 11);
            }

            assertEquals("Telemetry ? value", new PDFTextStripper().getText(doc).trim());
        }
    }
}
