package comp3607_project.json;

import java.util.Map;

import comp3607_project.lifecycle.LifecycleEvent;
import comp3607_project.model.TelemetryEvent;

public class LifecycleEventMapper implements EventMapper {
    @Override
    public boolean supports(TelemetryEvent event) {
        return event instanceof LifecycleEvent;
    }

    @Override
    public void addFields(TelemetryEvent event, Map<String, Object> out) {
        LifecycleEvent le = (LifecycleEvent) event;
        out.put("project", le.getProject());
        out.put("className", le.getClassName());
        out.put("detail", le.getDetail());
    }

    @Override
    public String projectOf(TelemetryEvent event) {
        return ((LifecycleEvent) event).getProject();
    }
}