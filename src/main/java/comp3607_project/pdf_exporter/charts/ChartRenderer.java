package comp3607_project.pdf_exporter.charts;


import java.awt.image.BufferedImage;
import java.io.IOException;

@FunctionalInterface
public interface ChartRenderer {
    BufferedImage render() throws IOException;
}