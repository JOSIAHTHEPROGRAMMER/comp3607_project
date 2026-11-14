package comp3607_project.model;

public interface EventSink {
    void accept(TelemetryEvent event);
}
