package comp3607_project.model;

import java.util.List;

/**
 * Forwards each event to several sinks. One failing sink never blocks the
 * others.
 */
public class CompositeSink implements EventSink {
    private final List<EventSink> sinks;

    public CompositeSink(EventSink... sinks) {
        this.sinks = List.of(sinks);
    }

    @Override
    public void accept(TelemetryEvent event) {
        for (EventSink sink : sinks) {
            try {
                sink.accept(event);
            } catch (RuntimeException e) {
                System.out.println("[Telemetry] sink failed: " + e);
            }
        }
    }
}