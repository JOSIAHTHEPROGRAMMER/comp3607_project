package comp3607_project.pdf_exporter.pdf;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.awt.image.BufferedImage;
import java.io.IOException;

public interface PdfWriter {
    void heading(PDPageContentStream cs, String text) throws IOException;
    void subheading(PDPageContentStream cs, String text) throws IOException;
    void paragraph(PDPageContentStream cs, String text, int fontSize) throws IOException;
    void drawImage(PDPageContentStream cs, PDDocument doc, BufferedImage image,
                   float maxWidth) throws IOException;
    void table(PDPageContentStream cs, String[][] rows, float col1, float col2) throws IOException;
    float contentTop();
    float margin();
    float cursor();
    void beginPage();
    void advance(float delta);
}
