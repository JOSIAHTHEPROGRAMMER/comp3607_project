package comp3607_project.pdf_exporter.metrics;


import java.util.List;
import java.util.Map;

public interface ErrorIntervalHistogramBuilder {
    Map<String, Integer> build(List<Long> errorTimestampsMillis);
}