package comp3607_project.pdf_exporter.charts;


import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class LineChartRenderer implements ChartRenderer {
    private final String title, xLabel, yLabel;
    private final List<Boolean> outcomes;
    private final int width, height;

    public LineChartRenderer(String title, String xLabel, String yLabel,
                             List<Boolean> outcomes, int width, int height) {
        this.title = title; this.xLabel = xLabel; this.yLabel = yLabel;
        this.outcomes = outcomes; this.width = width; this.height = height;
    }

    @Override
    public BufferedImage render() {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        int ok = 0, ko = 0;
        for (int i = 0; i < outcomes.size(); i++) {
            if (outcomes.get(i)) ok++; else ko++;
            String x = "Compile #" + (i + 1);
            ds.addValue(ok, "Successful", x);
            ds.addValue(ko, "Failed", x);
        }
        JFreeChart chart = ChartFactory.createLineChart(title, xLabel, yLabel, ds,
                PlotOrientation.VERTICAL, true, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        chart.getCategoryPlot().setBackgroundPaint(Color.WHITE);
        return chart.createBufferedImage(width, height);
    }
}