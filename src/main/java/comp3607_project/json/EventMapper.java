package comp3607_project.json;

import java.util.Map;

import comp3607_project.model.TelemetryEvent;

/** Strategy: writes the type-specific JSON fields of one kind of event. */
public interface EventMapper {
    boolean supports(TelemetryEvent event);

    void addFields(TelemetryEvent event, Map<String, Object> out);

    /** Project the event belongs to, or null if unknown. */
    String projectOf(TelemetryEvent event);

    /**
     * Reports this event's contribution to the session totals. Most events
     * contribute nothing.
     */
    default void accumulate(TelemetryEvent event, SummaryBuilder builder) {
    }
}