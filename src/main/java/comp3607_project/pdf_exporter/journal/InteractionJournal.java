package comp3607_project.pdf_exporter.journal;


import comp3607_project.editor_interactions.InteractionEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class InteractionJournal {

    private final List<InteractionEvent> events = new ArrayList<>();

    public synchronized void record(InteractionEvent event) {
        if (event != null) events.add(event);
    }

    public synchronized List<InteractionEvent> events() {
        return events.stream()
                .sorted(Comparator.comparing(InteractionEvent::timestamp))
                .toList();
    }

    public synchronized Instant firstTimestamp(Instant fallback) {
        return events.stream().map(InteractionEvent::timestamp)
                .min(Comparator.naturalOrder()).orElse(fallback);
    }

    public synchronized Instant lastTimestamp(Instant fallback) {
        return events.stream().map(InteractionEvent::timestamp)
                .max(Comparator.naturalOrder()).orElse(fallback);
    }
}
