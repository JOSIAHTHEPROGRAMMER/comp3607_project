package comp3607_project.pdf_exporter.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import comp3607_project.compile.CompileErrorClassifier.Category;
import comp3607_project.compile.CompileEvent;
import comp3607_project.editor_interactions.InteractionEvent;
import comp3607_project.execution.ExecutionEvent;
import comp3607_project.execution.ExecutionKind;
import comp3607_project.execution.ExecutionOutcome;
import comp3607_project.execution.InvocationInfo;
import comp3607_project.lifecycle.LifecycleEvent;
import comp3607_project.lifecycle.LifecycleEventType;
import comp3607_project.model.TelemetryEvent;
import comp3607_project.pdf_exporter.journal.InteractionJournal;
import comp3607_project.pdf_exporter.journal.TelemetryJournal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * The calculations behind the PDF report: compile totals and success counts, error
 * frequencies, error-interval buckets, run outcomes, the class timeline and active vs
 * idle time.
 */
class DefaultMetricsAggregatorTest {

    private final DefaultMetricsAggregator aggregator = new DefaultMetricsAggregator(
            new InteractionIdleTimeCalculator(5_000), new FixedBucketHistogramBuilder(),
            ZoneId.of("UTC"));

    private static TelemetryEvent stub(long timestamp) {
        return new TelemetryEvent() {
            @Override
            public String getEventType() {
                return "TEST";
            }

            @Override
            public long getTimestamp() {
                return timestamp;
            }

            @Override
            public String getTarget() {
                return "t";
            }
        };
    }

    private static ExecutionEvent run(long timestamp, ExecutionOutcome outcome) {
        return new ExecutionEvent(ExecutionKind.MAIN_METHOD, timestamp,
                new InvocationInfo("proj", "Dog", null, "main", 0, outcome, null));
    }

    private static InteractionEvent key(long timestamp) {
        return new InteractionEvent(InteractionEvent.Kind.KEY, Instant.ofEpochMilli(timestamp),
                0, "A", false);
    }

    @Test
    void emptySessionIsAllZeroAndEntirelyIdle() {
        TelemetryJournal journal = new TelemetryJournal(0);
        journal.end(10_000);

        ReportMetrics m = aggregator.aggregate(journal, new InteractionJournal());

        assertEquals(0, m.activeTimeMillis());
        assertEquals(10_000, m.idleTimeMillis());
        assertEquals(0, m.totalCompilations());
        assertEquals(0, m.totalRuns());
        assertTrue(m.errorFrequencyByCategory().isEmpty());
        assertTrue(m.compilationOutcomes().isEmpty());
        assertTrue(m.timelineEntries().isEmpty());
        assertTrue(m.errorIntervalBuckets().values().stream().allMatch(count -> count == 0));
    }

    @Test
    void compileAttemptsAreCountedOncePerAttemptNotPerErrorRow() {
        TelemetryJournal journal = new TelemetryJournal(0);
        journal.record(CompileEvent.error(1, 1_000, "p", "Dog.java", 3, "expected",
                Category.SYNTAX, "';' expected"));
        journal.record(CompileEvent.error(1, 1_000, "p", "Dog.java", 7, "prob.found.req",
                Category.TYPE, "incompatible types"));
        journal.record(CompileEvent.success(2, 3_000, "p", "Dog.java"));
        journal.record(CompileEvent.error(3, 5_000, "p", "Dog.java", 9, "cant.resolve",
                Category.SYMBOL, "cannot find symbol"));

        ReportMetrics m = aggregator.aggregate(journal, null);

        assertEquals(3, m.totalCompilations());
        assertEquals(1, m.successfulCompilations());
        assertEquals(2, m.failedCompilations());
        assertEquals(List.of(false, true, false), m.compilationOutcomes());
    }

    @Test
    void errorFrequencyCountsEveryErrorRowByCategory() {
        TelemetryJournal journal = new TelemetryJournal(0);
        journal.record(CompileEvent.error(1, 1_000, "p", "Dog.java", 3, "expected",
                Category.SYNTAX, "m"));
        journal.record(CompileEvent.error(1, 1_000, "p", "Dog.java", 4, "expected",
                Category.SYNTAX, "m"));
        journal.record(CompileEvent.error(2, 2_000, "p", "Dog.java", 5, "cant.resolve",
                Category.SYMBOL, "m"));
        journal.record(CompileEvent.success(3, 3_000, "p", "Dog.java"));

        ReportMetrics m = aggregator.aggregate(journal, null);

        assertEquals(Map.of("SYNTAX", 2, "SYMBOL", 1), m.errorFrequencyByCategory());
    }

    @Test
    void errorWithoutACategoryIsCountedAsOther() {
        TelemetryJournal journal = new TelemetryJournal(0);
        journal.record(new CompileEvent(1, 1_000, "p", false, "unknown", null, "odd",
                "Dog.java", 1));

        ReportMetrics m = aggregator.aggregate(journal, null);

        assertEquals(Map.of("OTHER", 1), m.errorFrequencyByCategory());
    }

    @Test
    void errorIntervalsAreBucketedByTheTimeBetweenFailures() {
        TelemetryJournal journal = new TelemetryJournal(0);
        journal.record(CompileEvent.error(1, 1_000, "p", "Dog.java", 1, "expected",
                Category.SYNTAX, "m"));
        journal.record(CompileEvent.error(2, 11_000, "p", "Dog.java", 1, "expected",
                Category.SYNTAX, "m"));
        journal.record(CompileEvent.error(3, 71_000, "p", "Dog.java", 1, "expected",
                Category.SYNTAX, "m"));

        ReportMetrics m = aggregator.aggregate(journal, null);

        assertEquals(1, m.errorIntervalBuckets().get("0-30s"));
        assertEquals(1, m.errorIntervalBuckets().get("1-2m"));
    }

    @Test
    void successfulCompilationsDoNotCountTowardsErrorIntervals() {
        TelemetryJournal journal = new TelemetryJournal(0);
        journal.record(CompileEvent.success(1, 1_000, "p", "Dog.java"));
        journal.record(CompileEvent.success(2, 500_000, "p", "Dog.java"));

        ReportMetrics m = aggregator.aggregate(journal, null);

        assertTrue(m.errorIntervalBuckets().values().stream().allMatch(count -> count == 0));
    }

    @Test
    void runsAreSplitByOutcome() {
        TelemetryJournal journal = new TelemetryJournal(0);
        journal.record(run(1_000, ExecutionOutcome.COMPLETED));
        journal.record(run(2_000, ExecutionOutcome.COMPLETED));
        journal.record(run(3_000, ExecutionOutcome.EXCEPTION));
        journal.record(run(4_000, ExecutionOutcome.TERMINATED));
        journal.record(run(5_000, ExecutionOutcome.UNKNOWN));

        ReportMetrics m = aggregator.aggregate(journal, null);

        assertEquals(5, m.totalRuns());
        assertEquals(2, m.completedRuns());
        assertEquals(1, m.exceptionRuns());
        assertEquals(1, m.terminatedRuns());
    }

    @Test
    void timelineListsClassCreationsModificationsAndRenamesInTimeOrder() {
        TelemetryJournal journal = new TelemetryJournal(0);
        journal.record(new LifecycleEvent(LifecycleEventType.CLASS_MODIFIED, 3_725_000,
                "p", "Dog", "edited"));
        journal.record(new LifecycleEvent(LifecycleEventType.CLASS_CREATED, 3_723_000,
                "p", "Dog", null));
        journal.record(new LifecycleEvent(LifecycleEventType.CLASS_RENAMED, 3_730_000,
                "p", "Cat", "was Dog"));

        ReportMetrics m = aggregator.aggregate(journal, null);

        assertEquals(List.of(
                "01:02:03  \u2013  CLASS_CREATED: Dog",
                "01:02:05  \u2013  CLASS_MODIFIED: Dog (edited)",
                "01:02:10  \u2013  CLASS_RENAMED: Cat (was Dog)"), m.timelineEntries());
    }

    @Test
    void timelineLeavesOutDeletionsAndProjectEvents() {
        TelemetryJournal journal = new TelemetryJournal(0);
        journal.record(new LifecycleEvent(LifecycleEventType.PROJECT_OPENED, 1_000,
                "p", null, null));
        journal.record(new LifecycleEvent(LifecycleEventType.CLASS_DELETED, 2_000,
                "p", "Dog", null));

        ReportMetrics m = aggregator.aggregate(journal, null);

        assertTrue(m.timelineEntries().isEmpty());
    }

    @Test
    void activeAndIdleTimeAddUpToTheSessionLength() {
        TelemetryJournal journal = new TelemetryJournal(0);
        journal.record(stub(1_000));
        journal.record(stub(2_000));
        journal.record(stub(3_000));
        journal.record(stub(15_000));
        journal.end(16_000);

        ReportMetrics m = aggregator.aggregate(journal, new InteractionJournal());

        // the 12s gap before the last event is 7s beyond the 5s threshold
        assertEquals(7_000, m.idleTimeMillis());
        assertEquals(9_000, m.activeTimeMillis());
        assertEquals(16_000, m.activeTimeMillis() + m.idleTimeMillis());
    }

    @Test
    void interactionBeforeTheSessionStartWidensTheSession() {
        TelemetryJournal journal = new TelemetryJournal(10_000);
        journal.end(20_000);
        InteractionJournal interactions = new InteractionJournal();
        interactions.record(key(5_000));

        ReportMetrics m = aggregator.aggregate(journal, interactions);

        // session now runs 5s..20s (15s); the 15s of silence after the key press is 10s idle
        assertEquals(10_000, m.idleTimeMillis());
        assertEquals(5_000, m.activeTimeMillis());
    }

    @Test
    void interactionAfterTheSessionEndWidensTheSession() {
        TelemetryJournal journal = new TelemetryJournal(0);
        journal.end(10_000);
        InteractionJournal interactions = new InteractionJournal();
        interactions.record(key(30_000));

        ReportMetrics m = aggregator.aggregate(journal, interactions);

        assertEquals(25_000, m.idleTimeMillis());
        assertEquals(5_000, m.activeTimeMillis());
    }

    @Test
    void aMissingInteractionJournalIsAllowed() {
        TelemetryJournal journal = new TelemetryJournal(0);
        journal.record(stub(1_000));
        journal.end(2_000);

        ReportMetrics m = aggregator.aggregate(journal, null);

        assertEquals(0, m.idleTimeMillis());
        assertEquals(2_000, m.activeTimeMillis());
    }

    @Test
    void theIdleCalculatorAndHistogramBuilderCanBeReplaced() {
        DefaultMetricsAggregator custom = new DefaultMetricsAggregator(
                (interactions, model, start, end) -> 1_234L,
                timestamps -> {
                    Map<String, Integer> result = new LinkedHashMap<>();
                    result.put("custom", timestamps.size());
                    return result;
                },
                ZoneId.of("UTC"));
        TelemetryJournal journal = new TelemetryJournal(0);
        journal.record(CompileEvent.error(1, 1_000, "p", "Dog.java", 1, "expected",
                Category.SYNTAX, "m"));
        journal.end(10_000);

        ReportMetrics m = custom.aggregate(journal, null);

        assertEquals(1_234, m.idleTimeMillis());
        assertEquals(10_000 - 1_234, m.activeTimeMillis());
        assertEquals(Map.of("custom", 1), m.errorIntervalBuckets());
    }

    @Test
    void worksThroughTheMetricsAggregatorInterface() {
        MetricsAggregator asInterface = aggregator;
        TelemetryJournal journal = new TelemetryJournal(0);
        journal.end(1_000);

        assertEquals(0, asInterface.aggregate(journal, null).totalCompilations());
    }
}
