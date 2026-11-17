package comp3607_project.pdf_exporter.metrics;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FixedBucketHistogramBuilder implements ErrorIntervalHistogramBuilder {

    private static final long[] BOUNDS = {0, 30_000, 60_000, 120_000, 300_000, 600_000, Long.MAX_VALUE};
    private static final String[] LABELS = {"0-30s", "30-60s", "1-2m", "2-5m", "5-10m", ">10m"};

    @Override
    public Map<String, Integer> build(List<Long> timestampsMillis) {
        Map<String, Integer> buckets = new LinkedHashMap<>();
        for (String l : LABELS) buckets.put(l, 0);

        for (int i = 1; i < timestampsMillis.size(); i++) {
            long gap = timestampsMillis.get(i) - timestampsMillis.get(i - 1);
            for (int b = 0; b < BOUNDS.length - 1; b++) {
                if (gap >= BOUNDS[b] && gap < BOUNDS[b + 1]) {
                    buckets.merge(LABELS[b], 1, Integer::sum);
                    break;
                }
            }
        }
        return buckets;
    }
}