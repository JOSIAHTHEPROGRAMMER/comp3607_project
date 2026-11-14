package comp3607_project.execution;

import comp3607_project.model.TelemetryEvent;

public final class ExecutionEvent implements TelemetryEvent {
    private final ExecutionKind kind;
    private final long timestamp;
    private final InvocationInfo info;

    public ExecutionEvent(ExecutionKind kind, long timestamp, InvocationInfo info) {
        this.kind = kind;
        this.timestamp = timestamp;
        this.info = info;
    }

    public ExecutionKind getKind() {
        return kind;
    }

    public ExecutionOutcome getOutcome() {
        return info.outcome();
    }

    public String getProject() {
        return info.project();
    }

    public String getClassName() {
        return info.className();
    }

    public String getObjectName() {
        return info.objectName();
    }

    public String getMethodName() {
        return info.methodName();
    }

    public int getParameterCount() {
        return info.parameterCount();
    }

    public String getResultType() {
        return info.resultType();
    }

    @Override
    public String getEventType() {
        return "EXECUTION_" + info.outcome().name();
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getTarget() {
        if (kind == ExecutionKind.CONSTRUCTOR) {
            return info.className() + ".<init>";
        }
        if (info.methodName() == null) {
            return info.className();
        }
        return info.className() + "." + info.methodName();
    }

    @Override
    public String toString() {
        return getEventType() + " kind=" + kind + " target=" + getTarget()
                + " project=" + info.project()
                + " object=" + info.objectName()
                + " method=" + info.methodName()
                + " params=" + info.parameterCount()
                + (info.resultType() != null ? " result=" + info.resultType() : "")
                + " at=" + timestamp;
    }
}