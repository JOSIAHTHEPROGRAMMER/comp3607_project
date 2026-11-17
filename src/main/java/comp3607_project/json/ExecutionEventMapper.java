package comp3607_project.json;

import java.util.Map;

import comp3607_project.execution.ExecutionEvent;
import comp3607_project.execution.ExecutionOutcome;
import comp3607_project.model.TelemetryEvent;

public class ExecutionEventMapper implements EventMapper {
    @Override
    public boolean supports(TelemetryEvent event) {
        return event instanceof ExecutionEvent;
    }

    @Override
    public void addFields(TelemetryEvent event, Map<String, Object> out) {
        ExecutionEvent ee = (ExecutionEvent) event;
        out.put("project", ee.getProject());
        out.put("kind", ee.getKind() == null ? null : ee.getKind().name());
        out.put("outcome", ee.getOutcome() == null ? null : ee.getOutcome().name());
        out.put("className", ee.getClassName());
        out.put("methodName", ee.getMethodName());
        out.put("parameterCount", ee.getParameterCount());
        out.put("resultType", ee.getResultType());
    }

    @Override
    public void accumulate(TelemetryEvent event, SummaryBuilder builder) {
        builder.run(((ExecutionEvent) event).getOutcome() == ExecutionOutcome.EXCEPTION);
    }

    @Override
    public String projectOf(TelemetryEvent event) {
        return ((ExecutionEvent) event).getProject();
    }
}