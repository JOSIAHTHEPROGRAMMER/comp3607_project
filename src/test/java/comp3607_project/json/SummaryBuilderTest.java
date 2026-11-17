package comp3607_project.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import comp3607_project.compile.CompileErrorClassifier.Category;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The report maths: compile attempts, success and failure counts, error ratios
 * per
 * category, compile intervals and run counts that the JSON and PDF output rely
 * on.
 */
class SummaryBuilderTest {

    @Test
    void emptyBuilderGivesAllZeros() {
        SessionSummary summary = new SummaryBuilder().build();

        assertEquals(0, summary.totalCompilations());
        assertEquals(0, summary.successfulCompilations());
        assertEquals(0, summary.failedCompilations());
        assertEquals(0, summary.totalRuns());
        assertEquals(0, summary.runtimeExceptions());
        assertTrue(summary.compileIntervalsMillis().isEmpty());
    }

    @Test
    void everyErrorCategoryStartsAtZeroInEnumOrder() {
        SessionSummary summary = new SummaryBuilder().build();

        assertEquals(List.of("SYNTAX", "SYMBOL", "TYPE", "OTHER"),
                List.copyOf(summary.errorCountsByCategory().keySet()));
        assertTrue(summary.errorCountsByCategory().values().stream().allMatch(c -> c == 0));
    }

    @Test
    void successfulAttemptIsCountedOnce() {
        SummaryBuilder builder = new SummaryBuilder();

        builder.compileRow(1, 1_000, true, null);
        SessionSummary summary = builder.build();

        assertEquals(1, summary.totalCompilations());
        assertEquals(1, summary.successfulCompilations());
        assertEquals(0, summary.failedCompilations());
    }

    @Test
    void failedAttemptWithSeveralErrorRowsIsOneFailedAttempt() {
        SummaryBuilder builder = new SummaryBuilder();

        builder.compileRow(1, 1_000, false, "SYNTAX");
        builder.compileRow(1, 1_000, false, "TYPE");
        builder.compileRow(1, 1_000, false, "TYPE");
        SessionSummary summary = builder.build();

        assertEquals(1, summary.totalCompilations());
        assertEquals(0, summary.successfulCompilations());
        assertEquals(1, summary.failedCompilations());
        assertEquals(1, summary.errorCountsByCategory().get("SYNTAX"));
        assertEquals(2, summary.errorCountsByCategory().get("TYPE"));
        assertEquals(0, summary.errorCountsByCategory().get("SYMBOL"));
    }

    @Test
    void errorWithoutACategoryCountsAsOther() {
        SummaryBuilder builder = new SummaryBuilder();

        builder.compileRow(1, 1_000, false, null);

        assertEquals(1, builder.build().errorCountsByCategory().get(Category.OTHER.name()));
    }

    @Test
    void mixOfSuccessAndFailureGivesTheRightTotals() {
        SummaryBuilder builder = new SummaryBuilder();

        builder.compileRow(1, 1_000, false, "SYNTAX");
        builder.compileRow(2, 2_000, true, null);
        builder.compileRow(3, 3_000, true, null);
        builder.compileRow(4, 4_000, false, "SYMBOL");
        SessionSummary summary = builder.build();

        assertEquals(4, summary.totalCompilations());
        assertEquals(2, summary.successfulCompilations());
        assertEquals(2, summary.failedCompilations());
        assertEquals(summary.totalCompilations(),
                summary.successfulCompilations() + summary.failedCompilations());
    }

    @Test
    void intervalsAreTheGapsBetweenConsecutiveAttemptStarts() {
        SummaryBuilder builder = new SummaryBuilder();

        builder.compileRow(1, 1_000, true, null);
        builder.compileRow(2, 4_000, true, null);
        builder.compileRow(3, 9_000, true, null);

        assertEquals(List.of(3_000L, 5_000L), builder.build().compileIntervalsMillis());
    }

    @Test
    void intervalsAreSortedByTimeEvenIfAttemptsArriveOutOfOrder() {
        SummaryBuilder builder = new SummaryBuilder();

        builder.compileRow(3, 9_000, true, null);
        builder.compileRow(1, 1_000, true, null);
        builder.compileRow(2, 4_000, true, null);

        assertEquals(List.of(3_000L, 5_000L), builder.build().compileIntervalsMillis());
    }

    @Test
    void rowsOfTheSameAttemptUseTheFirstTimestamp() {
        SummaryBuilder builder = new SummaryBuilder();

        builder.compileRow(1, 1_000, false, "SYNTAX");
        builder.compileRow(1, 1_500, false, "TYPE");
        builder.compileRow(2, 4_000, true, null);

        assertEquals(List.of(3_000L), builder.build().compileIntervalsMillis());
    }

    @Test
    void singleAttemptHasNoIntervals() {
        SummaryBuilder builder = new SummaryBuilder();

        builder.compileRow(1, 1_000, true, null);

        assertTrue(builder.build().compileIntervalsMillis().isEmpty());
    }

    @Test
    void runsAndRuntimeExceptionsAreCountedSeparately() {
        SummaryBuilder builder = new SummaryBuilder();

        builder.run(false);
        builder.run(true);
        builder.run(false);
        builder.run(true);
        SessionSummary summary = builder.build();

        assertEquals(4, summary.totalRuns());
        assertEquals(2, summary.runtimeExceptions());
    }

    @Test
    void builtSummaryCannotBeChangedThroughItsCollections() {
        SummaryBuilder builder = new SummaryBuilder();
        builder.compileRow(1, 1_000, true, null);
        builder.compileRow(2, 2_000, true, null);
        SessionSummary summary = builder.build();

        assertThrows(UnsupportedOperationException.class,
                () -> summary.errorCountsByCategory().put("SYNTAX", 99));
        assertThrows(UnsupportedOperationException.class,
                () -> summary.compileIntervalsMillis().add(1L));
    }

    @Test
    void summaryDoesNotChangeWhenTheBuilderIsUsedAgain() {
        SummaryBuilder builder = new SummaryBuilder();
        builder.compileRow(1, 1_000, false, "SYNTAX");
        SessionSummary before = builder.build();

        builder.compileRow(2, 2_000, false, "SYNTAX");

        assertEquals(1, before.errorCountsByCategory().get("SYNTAX"));
        assertEquals(2, builder.build().errorCountsByCategory().get("SYNTAX"));
    }
}
