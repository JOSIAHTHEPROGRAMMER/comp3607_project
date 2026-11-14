package comp3607_project.model;

public class ConsoleSink implements EventSink {
    @Override
    public void accept(TelemetryEvent event) {
        System.out.println("[Telemetry] " + event);
    }
}