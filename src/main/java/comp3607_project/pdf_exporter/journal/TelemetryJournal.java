package comp3607_project.pdf_exporter.journal;


import comp3607_project.model.TelemetryEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public final class TelemetryJournal {

    private final List<TelemetryEvent> events = new ArrayList<>();
    private final long startedAtMillis;
    private long endedAtMillis = -1;

    public TelemetryJournal(long startedAtMillis) {
        this.startedAtMillis = startedAtMillis;
    }

    public synchronized void record(TelemetryEvent event) {
        events.add(event);
    }

    public synchronized void end(long endedAtMillis) {
        this.endedAtMillis = endedAtMillis;
    }

    public synchronized long startedAtMillis() { return startedAtMillis; }

    public synchronized long endedAtMillis() {
        if (endedAtMillis >= 0) return endedAtMillis;
        return events.stream().mapToLong(TelemetryEvent::getTimestamp)
                .max().orElse(startedAtMillis);
    }

    public synchronized long totalDurationMillis() {
        return endedAtMillis() - startedAtMillis;
    }

    public synchronized List<TelemetryEvent> events() {
        return events.stream()
                .sorted(Comparator.comparingLong(TelemetryEvent::getTimestamp))
                .collect(Collectors.toUnmodifiableList());
    }

    public synchronized <T extends TelemetryEvent> List<T> eventsOfType(Class<T> type) {
        return events.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .sorted(Comparator.comparingLong(TelemetryEvent::getTimestamp))
                .collect(Collectors.toUnmodifiableList());
    }

    public synchronized List<TelemetryEvent> eventsWithTypePrefix(String prefix) {
        return events.stream()
                .filter(e -> e.getEventType().startsWith(prefix))
                .sorted(Comparator.comparingLong(TelemetryEvent::getTimestamp))
                .collect(Collectors.toUnmodifiableList());
    }
}