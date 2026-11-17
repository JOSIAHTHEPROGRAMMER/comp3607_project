package comp3607_project.editor_interactions;

import comp3607_project.model.TelemetryEvent;

/**
 * Adapts an InteractionEvent so it can travel through EventSink like every
 * other event.
 */
public final class InteractionTelemetryEvent implements TelemetryEvent {
    private final InteractionEvent event;
    private final String project;

    public InteractionTelemetryEvent(InteractionEvent event, String project) {
        this.event = event;
        this.project = project;
    }

    public InteractionEvent getEvent() {
        return event;
    }

    /** Project this activity belongs to, or null if unknown. */
    public String getProject() {
        return project;
    }

    @Override
    public String getEventType() {
        return event.classifiedAsIdle() ? "EDITOR_IDLE" : "EDITOR_" + event.kind().name();
    }

    @Override
    public long getTimestamp() {
        return event.timestamp().toEpochMilli();
    }

    @Override
    public String getTarget() {

        return event.kind() == InteractionEvent.Kind.EDIT && event.detail() != null
                ? event.detail()
                : project;
    }

    @Override
    public String toString() {
        return getEventType() + " project=" + project + " gap=" + event.gapSincePreviousMs() + "ms";
    }
}