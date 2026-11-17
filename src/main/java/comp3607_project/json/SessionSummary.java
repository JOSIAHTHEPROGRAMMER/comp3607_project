package comp3607_project.json;

import comp3607_project.model.TelemetryEvent;
import java.util.List;
import java.util.Map;

/**
 * Totals derived from a list of telemetry events. Pure Java, easy to unit test.
 */
public record SessionSummary(int totalCompilations,
        int successfulCompilations,
        int failedCompilations,
        int totalRuns,
        int runtimeExceptions,
        Map<String, Integer> errorCountsByCategory,
        List<Long> compileIntervalsMillis) {

    public static SessionSummary from(List<TelemetryEvent> events) {
        return from(events, EventMappers.defaults());
    }

    public static SessionSummary from(List<TelemetryEvent> events, List<EventMapper> mappers) {
        SummaryBuilder builder = new SummaryBuilder();
        for (TelemetryEvent event : events) {
            for (EventMapper mapper : mappers) {
                if (mapper.supports(event)) {
                    mapper.accumulate(event, builder);
                    break;
                }
            }
        }
        return builder.build();
    }
}