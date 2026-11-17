package comp3607_project.pdf_exporter.charts;


import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

public class BarChartRenderer implements ChartRenderer {
    private final String title, xLabel, yLabel;
    private final Map<String, Integer> data;
    private final int width, height;

    public BarChartRenderer(String title, String xLabel, String yLabel,
                            Map<String, Integer> data, int width, int height) {
        this.title = title; this.xLabel = xLabel; this.yLabel = yLabel;
        this.data = data; this.width = width; this.height = height;
    }

    @Override
    public BufferedImage render() {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        data.forEach((k, v) -> ds.addValue(v, "Errors", k));
        JFreeChart chart = ChartFactory.createBarChart(title, xLabel, yLabel, ds,
                PlotOrientation.VERTICAL, false, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        chart.getCategoryPlot().setBackgroundPaint(Color.WHITE);
        return chart.createBufferedImage(width, height);
    }
}
