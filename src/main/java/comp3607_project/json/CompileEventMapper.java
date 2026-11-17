package comp3607_project.json;

import java.util.Map;

import comp3607_project.compile.CompileEvent;
import comp3607_project.model.TelemetryEvent;

public class CompileEventMapper implements EventMapper {
    @Override
    public boolean supports(TelemetryEvent event) {
        return event instanceof CompileEvent;
    }

    @Override
    public void addFields(TelemetryEvent event, Map<String, Object> out) {
        CompileEvent ce = (CompileEvent) event;
        out.put("attemptId", ce.getAttemptId());
        out.put("project", ce.getProject());
        out.put("success", ce.isSuccess());
        if (!ce.isSuccess()) {
            out.put("errorCode", ce.getErrorCode());
            out.put("category", ce.getCategory() == null ? null : ce.getCategory().name());
            out.put("line", ce.getLine());
            out.put("message", ce.getMessage());
        }
    }

    @Override
    public void accumulate(TelemetryEvent event, SummaryBuilder builder) {
        CompileEvent ce = (CompileEvent) event;
        builder.compileRow(ce.getAttemptId(), ce.getTimestamp(), ce.isSuccess(),
                ce.getCategory() == null ? null : ce.getCategory().name());
    }

    @Override
    public String projectOf(TelemetryEvent event) {
        return ((CompileEvent) event).getProject();
    }
}