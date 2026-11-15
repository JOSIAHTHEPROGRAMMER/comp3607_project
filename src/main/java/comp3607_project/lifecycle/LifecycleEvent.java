package comp3607_project.lifecycle;

import comp3607_project.model.TelemetryEvent;

public final class LifecycleEvent implements TelemetryEvent {
    private final LifecycleEventType type;
    private final long timestamp;
    private final String project;
    private final String className;
    private final String detail;

    public LifecycleEvent(LifecycleEventType type, long timestamp, String project, String className, String detail) {
        this.type = type;
        this.timestamp = timestamp;
        this.project = project;
        this.className = className;
        this.detail = detail;
    }

    public LifecycleEventType getType() {
        return type;
    }

    public String getProject() {
        return project;
    }

    public String getClassName() {
        return className;
    }

    public String getDetail() {
        return detail;
    }

    @Override
    public String getEventType() {
        return type.name();
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getTarget() {
        return className != null ? className : project;
    }

    @Override
    public String toString() {
        return type + " project=" + project
                + (className != null ? " class=" + className : "")
                + (detail != null ? " detail=" + detail : "")
                + " at=" + timestamp;
    }
}