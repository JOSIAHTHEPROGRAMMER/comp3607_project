package comp3607_project.json;

import java.util.Map;

import comp3607_project.editor_interactions.InteractionEvent;
import comp3607_project.editor_interactions.InteractionTelemetryEvent;
import comp3607_project.model.TelemetryEvent;

public class InteractionEventMapper implements EventMapper {
    @Override
    public boolean supports(TelemetryEvent event) {
        return event instanceof InteractionTelemetryEvent;
    }

    @Override
    public void addFields(TelemetryEvent event, Map<String, Object> out) {
        InteractionTelemetryEvent ie = (InteractionTelemetryEvent) event;
        InteractionEvent raw = ie.getEvent();
        out.put("project", ie.getProject());
        out.put("kind", raw.kind().name());
        out.put("gapSincePreviousMs", raw.gapSincePreviousMs());
        out.put("idle", raw.classifiedAsIdle());
        out.put("detail", raw.kind() == InteractionEvent.Kind.EDIT ? raw.detail() : null);
    }

    @Override
    public String projectOf(TelemetryEvent event) {
        return ((InteractionTelemetryEvent) event).getProject();
    }
}