package comp3607_project.adapter;

import bluej.extensions2.ExtensionException;
import bluej.extensions2.event.InvocationFinishedEvent;
import bluej.extensions2.event.InvocationFinishedListener;
import comp3607_project.execution.ExecutionOutcome;
import comp3607_project.execution.ExecutionTracker;
import comp3607_project.execution.InvocationInfo;

public class BlueJExecutionAdapter implements InvocationFinishedListener {
    private final ExecutionTracker tracker;

    public BlueJExecutionAdapter(ExecutionTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public void invocationFinished(InvocationFinishedEvent ev) {
        try {

            // System.out.println("[Telemetry-raw] " + ev);

            String project = ev.getPackage().getProject().getName();
            tracker.invocationFinished(new InvocationInfo(
                    project,
                    ev.getClassName(),
                    ev.getObjectName(),
                    ev.getMethodName(),
                    parameterCountOf(ev),
                    outcomeOf(ev.getEventType()),
                    resultTypeOf(ev)));
        } catch (ExtensionException | RuntimeException e) {
            System.out.println("[Telemetry] invocation event failed: " + e);
        }
    }

    private int parameterCountOf(InvocationFinishedEvent ev) {
        try {
            String[] parameters = ev.getParameters();
            return parameters == null ? 0 : parameters.length;
        } catch (RuntimeException e) {
            System.out.println("[Telemetry] could not read parameters: " + e);
            return 0;
        }
    }

    private String resultTypeOf(InvocationFinishedEvent ev) {
        try {
            Object result = ev.getResult();
            return result == null ? null : result.getClass().getName();
        } catch (RuntimeException e) {
            System.out.println("[Telemetry] could not read result: " + e);
            return null;
        }
    }

    private ExecutionOutcome outcomeOf(InvocationFinishedEvent.EventType type) {
        if (type == null) {
            return ExecutionOutcome.UNKNOWN;
        }
        return switch (type) {
            case NORMAL_EXIT -> ExecutionOutcome.COMPLETED;
            case EXCEPTION_EXIT -> ExecutionOutcome.EXCEPTION;
            case TERMINATED_EXIT -> ExecutionOutcome.TERMINATED;
            case UNKNOWN_EXIT -> ExecutionOutcome.UNKNOWN;
        };
    }
}
