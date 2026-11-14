package comp3607_project.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;

import comp3607_project.model.EventSink;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExecutionTrackerTest {

    private List<ExecutionEvent> events;
    private AtomicLong now;
    private ExecutionTracker tracker;

    @BeforeEach
    void setUp() {
        events = new ArrayList<>();
        now = new AtomicLong(2000);
        EventSink sink = event -> events.add((ExecutionEvent) event);
        tracker = new ExecutionTracker(sink, now::get);
    }

    private InvocationInfo info(String objectName, String methodName, ExecutionOutcome outcome) {
        return new InvocationInfo("proj", "Calc", objectName, methodName, 0, outcome, null);
    }

    @Test
    void constructorCallIsClassifiedAsConstructor() {
        tracker.invocationFinished(info("calc1", null, ExecutionOutcome.COMPLETED));

        assertEquals(1, events.size());
        assertEquals(ExecutionKind.CONSTRUCTOR, events.get(0).getKind());
    }

    @Test
    void staticMainIsClassifiedAsMainMethod() {
        tracker.invocationFinished(info(null, "main", ExecutionOutcome.COMPLETED));

        assertEquals(ExecutionKind.MAIN_METHOD, events.get(0).getKind());
    }

    @Test
    void otherStaticCallIsClassifiedAsStaticMethod() {
        tracker.invocationFinished(info(null, "greet", ExecutionOutcome.COMPLETED));

        assertEquals(ExecutionKind.STATIC_METHOD, events.get(0).getKind());
    }

    @Test
    void callOnObjectIsClassifiedAsInstanceMethod() {
        tracker.invocationFinished(info("calc1", "add", ExecutionOutcome.COMPLETED));

        assertEquals(ExecutionKind.INSTANCE_METHOD, events.get(0).getKind());
    }

    @Test
    void exceptionOutcomeProducesExceptionEvent() {
        tracker.invocationFinished(info("calc1", "divide", ExecutionOutcome.EXCEPTION));

        assertEquals("EXECUTION_EXCEPTION", events.get(0).getEventType());
        assertEquals(ExecutionOutcome.EXCEPTION, events.get(0).getOutcome());
        assertEquals(1, tracker.getExceptionRuns());
    }

    @Test
    void terminatedOutcomeProducesTerminatedEvent() {
        tracker.invocationFinished(info("calc1", "spin", ExecutionOutcome.TERMINATED));

        assertEquals("EXECUTION_TERMINATED", events.get(0).getEventType());
        assertEquals(1, tracker.getTerminatedRuns());
    }

    @Test
    void countersTrackEveryOutcome() {
        tracker.invocationFinished(info("calc1", "add", ExecutionOutcome.COMPLETED));
        tracker.invocationFinished(info("calc1", "add", ExecutionOutcome.COMPLETED));
        tracker.invocationFinished(info("calc1", "divide", ExecutionOutcome.EXCEPTION));
        tracker.invocationFinished(info("calc1", "spin", ExecutionOutcome.TERMINATED));
        tracker.invocationFinished(info("calc1", "add", ExecutionOutcome.UNKNOWN));

        assertEquals(5, tracker.getTotalRuns());
        assertEquals(2, tracker.getCompletedRuns());
        assertEquals(1, tracker.getExceptionRuns());
        assertEquals(1, tracker.getTerminatedRuns());
    }

    @Test
    void timestampAndDetailsAreCarriedIntoTheEvent() {
        InvocationInfo details = new InvocationInfo(
                "proj", "Calc", "calc1", "add", 2, ExecutionOutcome.COMPLETED, "java.lang.Integer");

        tracker.invocationFinished(details);

        ExecutionEvent event = events.get(0);
        assertEquals(2000, event.getTimestamp());
        assertEquals("proj", event.getProject());
        assertEquals("Calc", event.getClassName());
        assertEquals("calc1", event.getObjectName());
        assertEquals("add", event.getMethodName());
        assertEquals(2, event.getParameterCount());
        assertEquals("java.lang.Integer", event.getResultType());
    }

    @Test
    void exceptionWithoutMethodNameIsClassifiedAsUnknownKind() {
        tracker.invocationFinished(info(null, null, ExecutionOutcome.EXCEPTION));

        assertEquals(ExecutionKind.UNKNOWN, events.get(0).getKind());
        assertEquals("EXECUTION_EXCEPTION", events.get(0).getEventType());
    }

    @Test
    void terminationWithoutMethodNameIsClassifiedAsUnknownKind() {
        tracker.invocationFinished(info(null, null, ExecutionOutcome.TERMINATED));

        assertEquals(ExecutionKind.UNKNOWN, events.get(0).getKind());
    }

    @Test
    void failedRunOnObjectWithUnknownMethodIsClassifiedAsInstanceMethod() {
        tracker.invocationFinished(info("calc1", null, ExecutionOutcome.EXCEPTION));

        assertEquals(ExecutionKind.INSTANCE_METHOD, events.get(0).getKind());
        assertEquals("EXECUTION_EXCEPTION", events.get(0).getEventType());
    }
}