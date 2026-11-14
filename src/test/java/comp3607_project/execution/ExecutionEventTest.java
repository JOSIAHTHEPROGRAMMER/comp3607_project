package comp3607_project.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ExecutionEventTest {

    private ExecutionEvent eventFor(ExecutionKind kind, String methodName, ExecutionOutcome outcome) {
        InvocationInfo info = new InvocationInfo("proj", "Calc", "calc1", methodName, 0, outcome, null);
        return new ExecutionEvent(kind, 5L, info);
    }

    @Test
    void targetIsClassAndMethod() {
        ExecutionEvent event = eventFor(ExecutionKind.INSTANCE_METHOD, "add", ExecutionOutcome.COMPLETED);

        assertEquals("Calc.add", event.getTarget());
    }

    @Test
    void targetUsesInitForConstructors() {
        ExecutionEvent event = eventFor(ExecutionKind.CONSTRUCTOR, null, ExecutionOutcome.COMPLETED);

        assertEquals("Calc.<init>", event.getTarget());
    }

    @Test
    void targetIsClassOnlyWhenMethodIsUnknown() {
        ExecutionEvent event = eventFor(ExecutionKind.UNKNOWN, null, ExecutionOutcome.EXCEPTION);

        assertEquals("Calc", event.getTarget());
    }

    @Test
    void eventTypeCombinesPrefixAndOutcome() {
        ExecutionEvent event = eventFor(ExecutionKind.INSTANCE_METHOD, "add", ExecutionOutcome.COMPLETED);

        assertEquals("EXECUTION_COMPLETED", event.getEventType());
    }
}