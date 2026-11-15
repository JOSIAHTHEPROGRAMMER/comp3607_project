package comp3607_project.execution;

import comp3607_project.model.EventSink;
import java.util.function.LongSupplier;

public class ExecutionTracker {
    private final EventSink sink;
    private final LongSupplier clock;
    private int totalRuns;
    private int completedRuns;
    private int exceptionRuns;
    private int terminatedRuns;

    public ExecutionTracker(EventSink sink, LongSupplier clock) {
        this.sink = sink;
        this.clock = clock;
    }

    public synchronized void invocationFinished(InvocationInfo info) {
        totalRuns++;
        switch (info.outcome()) {
            case COMPLETED -> completedRuns++;
            case EXCEPTION -> exceptionRuns++;
            case TERMINATED -> terminatedRuns++;
            case UNKNOWN -> {
            }
        }
        sink.accept(new ExecutionEvent(kindOf(info), clock.getAsLong(), info));
    }

    public synchronized int getTotalRuns() {
        return totalRuns;
    }

    public synchronized int getCompletedRuns() {
        return completedRuns;
    }

    public synchronized int getExceptionRuns() {
        return exceptionRuns;
    }

    public synchronized int getTerminatedRuns() {
        return terminatedRuns;
    }

    private ExecutionKind kindOf(InvocationInfo info) {
        if (info.methodName() == null) {
            if (info.outcome() == ExecutionOutcome.COMPLETED) {
                return ExecutionKind.CONSTRUCTOR;
            }
            return info.objectName() != null ? ExecutionKind.INSTANCE_METHOD : ExecutionKind.UNKNOWN;
        }
        if (info.objectName() == null) {
            return "main".equals(info.methodName()) ? ExecutionKind.MAIN_METHOD : ExecutionKind.STATIC_METHOD;
        }
        return ExecutionKind.INSTANCE_METHOD;
    }
}