package comp3607_project.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import comp3607_project.compile.CompileEvent;
import comp3607_project.execution.ExecutionEvent;
import comp3607_project.execution.ExecutionKind;
import comp3607_project.execution.ExecutionOutcome;
import comp3607_project.execution.InvocationInfo;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExecutionEventMapperTest {

    private final ExecutionEventMapper mapper = new ExecutionEventMapper();

    private static ExecutionEvent event(ExecutionKind kind, ExecutionOutcome outcome) {
        InvocationInfo info = new InvocationInfo("alpha", "Calc", "calc1", "add", 2, outcome,
                "int");
        return new ExecutionEvent(kind, 1_000L, info);
    }

    @Test
    void supportsOnlyExecutionEvents() {
        assertTrue(mapper.supports(event(ExecutionKind.INSTANCE_METHOD,
                ExecutionOutcome.COMPLETED)));
        assertFalse(mapper.supports(CompileEvent.success(1L, 1_000L, "alpha", "Dog.java")));
    }

    @Test
    void writesEveryExecutionField() {
        Map<String, Object> out = new LinkedHashMap<>();

        mapper.addFields(event(ExecutionKind.INSTANCE_METHOD, ExecutionOutcome.COMPLETED), out);

        assertEquals("alpha", out.get("project"));
        assertEquals("INSTANCE_METHOD", out.get("kind"));
        assertEquals("COMPLETED", out.get("outcome"));
        assertEquals("Calc", out.get("className"));
        assertEquals("add", out.get("methodName"));
        assertEquals(2, out.get("parameterCount"));
        assertEquals("int", out.get("resultType"));
    }

    @Test
    void missingKindAndOutcomeAreWrittenAsNull() {
        Map<String, Object> out = new LinkedHashMap<>();

        mapper.addFields(event(null, null), out);

        assertTrue(out.containsKey("kind"));
        assertNull(out.get("kind"));
        assertTrue(out.containsKey("outcome"));
        assertNull(out.get("outcome"));
    }

    @Test
    void projectOfReturnsTheEventsProject() {
        assertEquals("alpha",
                mapper.projectOf(event(ExecutionKind.MAIN_METHOD, ExecutionOutcome.COMPLETED)));
    }

    @Test
    void accumulateCountsACompletedRunWithoutAnException() {
        SummaryBuilder builder = new SummaryBuilder();

        mapper.accumulate(event(ExecutionKind.MAIN_METHOD, ExecutionOutcome.COMPLETED), builder);
        SessionSummary summary = builder.build();

        assertEquals(1, summary.totalRuns());
        assertEquals(0, summary.runtimeExceptions());
    }

    @Test
    void accumulateCountsAnExceptionRun() {
        SummaryBuilder builder = new SummaryBuilder();

        mapper.accumulate(event(ExecutionKind.MAIN_METHOD, ExecutionOutcome.EXCEPTION), builder);
        SessionSummary summary = builder.build();

        assertEquals(1, summary.totalRuns());
        assertEquals(1, summary.runtimeExceptions());
    }

    @Test
    void accumulateDoesNotCountATerminatedRunAsAnException() {
        SummaryBuilder builder = new SummaryBuilder();

        mapper.accumulate(event(ExecutionKind.MAIN_METHOD, ExecutionOutcome.TERMINATED), builder);
        SessionSummary summary = builder.build();

        assertEquals(1, summary.totalRuns());
        assertEquals(0, summary.runtimeExceptions());
    }
}
