package comp3607_project.pdf_exporter.pdf;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class PdfBoxWriter implements PdfWriter {

    private static final float MARGIN = 50f;
    private static final float LINE   = 16f;

    private float currentY;

    @Override public float margin()     { return MARGIN; }
    @Override public float contentTop() { return PDRectangle.A4.getHeight() - MARGIN; }
    @Override public float cursor()     { return currentY; }
    @Override public void beginPage()   { currentY = contentTop(); }
    @Override public void advance(float d) { currentY -= d; }

    @Override
    public void heading(PDPageContentStream cs, String text) throws IOException {
        write(cs, text, PDType1Font.HELVETICA_BOLD, 20);
    }

    @Override
    public void subheading(PDPageContentStream cs, String text) throws IOException {
        write(cs, text, PDType1Font.HELVETICA_BOLD, 13);
    }

    @Override
    public void paragraph(PDPageContentStream cs, String text, int size) throws IOException {
        write(cs, text, PDType1Font.HELVETICA, size);
    }

    @Override
    public void drawImage(PDPageContentStream cs, PDDocument doc,
                          BufferedImage image, float maxWidth) throws IOException {
        PDImageXObject img = LosslessFactory.createFromImage(doc, image);
        float w = maxWidth;
        float h = w * image.getHeight() / image.getWidth();
        cs.drawImage(img, MARGIN, currentY - h - 10, w, h);
        currentY -= (h + 20);
    }

    @Override
    public void table(PDPageContentStream cs, String[][] rows,
                      float col1, float col2) throws IOException {
        float rowH = 22f;
        for (int r = 0; r < rows.length; r++) {
            float rowY = currentY - r * rowH;

            if (r == 0)          cs.setNonStrokingColor(0.18f, 0.28f, 0.45f);
            else if (r % 2 == 0) cs.setNonStrokingColor(0.93f, 0.95f, 0.98f);
            else                 cs.setNonStrokingColor(Color.WHITE);
            cs.addRect(MARGIN, rowY - rowH + 4, col1 + col2, rowH - 2);
            cs.fill();

            cs.setNonStrokingColor(Color.BLACK);
            cs.setStrokingColor(new Color(0.6f, 0.6f, 0.6f));
            cs.addRect(MARGIN, rowY - rowH + 4, col1 + col2, rowH - 2);
            cs.stroke();
            cs.moveTo(MARGIN + col1, rowY - rowH + 4);
            cs.lineTo(MARGIN + col1, rowY + 4);
            cs.stroke();

            PDFont font = (r == 0) ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA;
            if (r == 0) cs.setNonStrokingColor(Color.WHITE);

            cs.beginText(); cs.setFont(font, 11);
            cs.newLineAtOffset(MARGIN + 8, rowY - rowH + 11);
            cs.showText(encodeText(rows[r][0], font)); cs.endText();

            cs.beginText(); cs.setFont(font, 11);
            cs.newLineAtOffset(MARGIN + col1 + 8, rowY - rowH + 11);
            cs.showText(encodeText(rows[r][1], font)); cs.endText();

            cs.setNonStrokingColor(Color.BLACK);
        }
        currentY -= (rows.length * rowH + 10);
    }

    private void write(PDPageContentStream cs, String text, PDFont font, int size) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(MARGIN, currentY - LINE);
        cs.showText(encodeText(text, font));
        cs.endText();
        currentY -= LINE;
    }

    private String encodeText(String text, PDFont font) throws IOException {
        StringBuilder encoded = new StringBuilder(text.length());
        for (int offset = 0; offset < text.length();) {
            int codePoint = text.codePointAt(offset);
            offset += Character.charCount(codePoint);
            String character = Character.isISOControl(codePoint)
                    ? " " : new String(Character.toChars(codePoint));
            try {
                font.encode(character);
                encoded.append(character);
            } catch (IllegalArgumentException e) {
                encoded.append('?');
            }
        }
        return encoded.toString();
    }
}