package comp3607_project.pdf_exporter.charts;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ChartRenderersTest {

    @Test
    void pieChartHasTheRequestedSize() {
        PieChartRenderer renderer = new PieChartRenderer("Errors",
                Map.of("SYNTAX", 3, "TYPE", 1), 700, 400);

        BufferedImage image = renderer.render();

        assertEquals(700, image.getWidth());
        assertEquals(400, image.getHeight());
    }

    @Test
    void pieChartWithNoDataStillRenders() {
        PieChartRenderer renderer = new PieChartRenderer("Errors", Map.of(), 300, 200);

        assertEquals(300, renderer.render().getWidth());
    }

    @Test
    void barChartHasTheRequestedSize() {
        BarChartRenderer renderer = new BarChartRenderer("Intervals", "Interval", "Errors",
                Map.of("0-30s", 2, "30-60s", 1), 640, 360);

        BufferedImage image = renderer.render();

        assertEquals(640, image.getWidth());
        assertEquals(360, image.getHeight());
    }

    @Test
    void barChartWithNoDataStillRenders() {
        BarChartRenderer renderer = new BarChartRenderer("Intervals", "Interval", "Errors",
                Map.of(), 300, 200);

        assertNotNull(renderer.render());
    }

    @Test
    void lineChartHasTheRequestedSize() {
        LineChartRenderer renderer = new LineChartRenderer("Outcomes", "Attempt", "Count",
                List.of(false, true, true, false), 500, 300);

        BufferedImage image = renderer.render();

        assertEquals(500, image.getWidth());
        assertEquals(300, image.getHeight());
    }

    @Test
    void lineChartWithNoCompilationsStillRenders() {
        LineChartRenderer renderer = new LineChartRenderer("Outcomes", "Attempt", "Count",
                List.of(), 300, 200);

        assertDoesNotThrow(renderer::render);
    }

    @Test
    void allRenderersWorkThroughTheChartRendererInterface() throws IOException {
        ChartRenderer pie = new PieChartRenderer("t", Map.of("A", 1), 200, 100);
        ChartRenderer bar = new BarChartRenderer("t", "x", "y", Map.of("A", 1), 200, 100);
        ChartRenderer line = new LineChartRenderer("t", "x", "y", List.of(true), 200, 100);

        assertEquals(200, pie.render().getWidth());
        assertEquals(200, bar.render().getWidth());
        assertEquals(200, line.render().getWidth());
    }
}
