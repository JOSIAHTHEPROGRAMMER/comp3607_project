package comp3607_project.json;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import comp3607_project.model.EventSink;
import comp3607_project.model.TelemetryEvent;

/**
 * Sink that remembers every event, in arrival order, for later serialisation.
 */
public class EventCollector implements EventSink {
    private final List<TelemetryEvent> events = new CopyOnWriteArrayList<>();

    @Override
    public void accept(TelemetryEvent event) {
        if (event != null) {
            events.add(event);
        }
    }

    /** Returns a snapshot, so callers can't modify the stored list. */
    public List<TelemetryEvent> getEvents() {
        return List.copyOf(events);
    }

    public int size() {
        return events.size();
    }
}