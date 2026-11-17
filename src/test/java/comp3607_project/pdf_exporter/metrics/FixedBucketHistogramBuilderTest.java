package comp3607_project.pdf_exporter.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FixedBucketHistogramBuilderTest {

    private final FixedBucketHistogramBuilder builder = new FixedBucketHistogramBuilder();

    private static final List<String> LABELS =
            List.of("0-30s", "30-60s", "1-2m", "2-5m", "5-10m", ">10m");

    @Test
    void emptyInputGivesEverySixBucketsAtZeroInOrder() {
        Map<String, Integer> result = builder.build(List.of());

        assertEquals(LABELS, List.copyOf(result.keySet()));
        assertTrue(result.values().stream().allMatch(count -> count == 0));
    }

    @Test
    void singleTimestampHasNoGaps() {
        Map<String, Integer> result = builder.build(List.of(5_000L));

        assertTrue(result.values().stream().allMatch(count -> count == 0));
    }

    @Test
    void eachGapLandsInItsOwnBucket() {
        // gaps: 10s, 30s, 60s, 150s, 450s, 700s
        List<Long> timestamps = List.of(0L, 10_000L, 40_000L, 100_000L, 250_000L,
                700_000L, 1_400_000L);

        Map<String, Integer> result = builder.build(timestamps);

        for (String label : LABELS) {
            assertEquals(1, result.get(label), "bucket " + label);
        }
    }

    @Test
    void justUnderAnUpperBoundStaysInTheLowerBucket() {
        Map<String, Integer> result = builder.build(List.of(0L, 29_999L));

        assertEquals(1, result.get("0-30s"));
        assertEquals(0, result.get("30-60s"));
    }

    @Test
    void exactlyOnABoundMovesToTheNextBucket() {
        Map<String, Integer> result = builder.build(List.of(0L, 30_000L));

        assertEquals(0, result.get("0-30s"));
        assertEquals(1, result.get("30-60s"));
    }

    @Test
    void tenMinutesExactlyIsTheLongestBucket() {
        Map<String, Integer> result = builder.build(List.of(0L, 600_000L));

        assertEquals(1, result.get(">10m"));
        assertEquals(0, result.get("5-10m"));
    }

    @Test
    void identicalTimestampsCountAsAZeroGap() {
        Map<String, Integer> result = builder.build(List.of(1_000L, 1_000L, 1_000L));

        assertEquals(2, result.get("0-30s"));
    }

    @Test
    void gapsAreCountedBetweenNeighboursOnly() {
        Map<String, Integer> result = builder.build(List.of(0L, 1_000L, 2_000L, 3_000L));

        assertEquals(3, result.get("0-30s"));
    }

    @Test
    void timestampsOutOfOrderProduceNoBucketEntry() {
        Map<String, Integer> result = builder.build(List.of(10_000L, 0L));

        assertTrue(result.values().stream().allMatch(count -> count == 0));
    }

    @Test
    void worksThroughTheHistogramBuilderInterface() {
        ErrorIntervalHistogramBuilder asInterface = builder;

        assertEquals(6, asInterface.build(List.of()).size());
    }
}
