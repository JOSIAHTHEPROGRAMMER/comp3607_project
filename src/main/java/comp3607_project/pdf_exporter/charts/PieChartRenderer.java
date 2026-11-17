package comp3607_project.pdf_exporter.charts;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

public class PieChartRenderer implements ChartRenderer {
    private final String title;
    private final Map<String, Integer> data;
    private final int width, height;

    public PieChartRenderer(String title, Map<String, Integer> data, int width, int height) {
        this.title = title; this.data = data; this.width = width; this.height = height;
    }

    @Override
    public BufferedImage render() {
        DefaultPieDataset<String> ds = new DefaultPieDataset<>();
        data.forEach(ds::setValue);
        JFreeChart chart = ChartFactory.createPieChart(title, ds, true, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        PiePlot<?> plot = (PiePlot<?>) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        return chart.createBufferedImage(width, height);
    }
}
