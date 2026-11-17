package comp3607_project.json;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import comp3607_project.compile.CompileErrorClassifier.Category;
import comp3607_project.compile.CompileEvent;
import comp3607_project.execution.ExecutionEvent;
import comp3607_project.execution.ExecutionKind;
import comp3607_project.execution.ExecutionOutcome;
import comp3607_project.execution.InvocationInfo;
import comp3607_project.model.TelemetryEvent;

class SessionSummaryTest {

    private static CompileEvent ok(long attempt, long at) {
        return new CompileEvent(attempt, at, "p", true, null, null, null, "A.java", -1);
    }

    private static CompileEvent err(long attempt, long at, Category c) {
        return new CompileEvent(attempt, at, "p", false, "code", c, "msg", "A.java", 3);
    }

    private static ExecutionEvent run(ExecutionOutcome outcome) {
        return new ExecutionEvent(ExecutionKind.CONSTRUCTOR, 5,
                new InvocationInfo("p", "A", "obj", "m", 0, outcome, null));
    }

    @Test
    void emptyListGivesZerosAndAllCategories() {
        SessionSummary s = SessionSummary.from(List.of());
        assertEquals(0, s.totalCompilations());
        assertEquals(0, s.totalRuns());
        assertEquals(4, s.errorCountsByCategory().size());
        assertTrue(s.compileIntervalsMillis().isEmpty());
    }

    @Test
    void countsAttemptsNotRows() {
        List<TelemetryEvent> events = List.of(
                err(1, 100, Category.SYNTAX), err(1, 101, Category.SYMBOL), ok(2, 200));
        SessionSummary s = SessionSummary.from(events);
        assertEquals(2, s.totalCompilations());
        assertEquals(1, s.successfulCompilations());
        assertEquals(1, s.failedCompilations());
    }

    @Test
    void errorRowsAreCountedPerCategory() {
        SessionSummary s = SessionSummary.from(List.of(
                err(1, 100, Category.SYNTAX), err(1, 101, Category.SYMBOL),
                err(2, 300, Category.SYNTAX)));
        assertEquals(2, s.errorCountsByCategory().get("SYNTAX"));
        assertEquals(1, s.errorCountsByCategory().get("SYMBOL"));
        assertEquals(0, s.errorCountsByCategory().get("TYPE"));
    }

    @Test
    void intervalsAreGapsBetweenAttemptStarts() {
        SessionSummary s = SessionSummary.from(List.of(
                ok(1, 1000), err(2, 1500, Category.TYPE), ok(3, 2700)));
        assertEquals(List.of(500L, 1200L), s.compileIntervalsMillis());
    }

    @Test
    void runsAndExceptionsAreCounted() {
        SessionSummary s = SessionSummary.from(List.of(
                run(ExecutionOutcome.COMPLETED), run(ExecutionOutcome.EXCEPTION)));
        assertEquals(2, s.totalRuns());
        assertEquals(1, s.runtimeExceptions());
    }
}