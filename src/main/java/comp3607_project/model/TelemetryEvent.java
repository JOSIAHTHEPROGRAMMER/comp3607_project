package comp3607_project.model;

public interface TelemetryEvent {
    String getEventType();

    long getTimestamp();

    String getTarget();
}