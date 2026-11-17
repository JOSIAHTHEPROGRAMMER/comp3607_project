package comp3607_project.pdf_exporter.metrics;


import comp3607_project.compile.CompileErrorClassifier;
import comp3607_project.compile.CompileEvent;
import comp3607_project.editor_interactions.InteractionEvent;
import comp3607_project.execution.ExecutionEvent;
import comp3607_project.lifecycle.LifecycleEvent;
import comp3607_project.lifecycle.LifecycleEventType;
import comp3607_project.model.TelemetryEvent;
import comp3607_project.pdf_exporter.journal.InteractionJournal;
import comp3607_project.pdf_exporter.journal.TelemetryJournal;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultMetricsAggregator implements MetricsAggregator {

    private final IdleTimeCalculator idleCalculator;
    private final ErrorIntervalHistogramBuilder histogramBuilder;
    private final ZoneId zone;

    public DefaultMetricsAggregator(IdleTimeCalculator idleCalculator,
                                    ErrorIntervalHistogramBuilder histogramBuilder,
                                    ZoneId zone) {
        this.idleCalculator = idleCalculator;
        this.histogramBuilder = histogramBuilder;
        this.zone = zone;
    }

    @Override
    public ReportMetrics aggregate(TelemetryJournal telemetry, InteractionJournal interactions) {
        List<TelemetryEvent> modelEvents = telemetry.events();
        List<InteractionEvent> interactionEvents = interactions == null
                ? List.of() : interactions.events();

        long sessionStart = telemetry.startedAtMillis();
        long sessionEnd   = telemetry.endedAtMillis();

        // Widen session bounds when interaction telemetry starts earlier/ends later.
        for (InteractionEvent e : interactionEvents) {
            long t = e.timestamp().toEpochMilli();
            if (t < sessionStart) sessionStart = t;
            if (t > sessionEnd)   sessionEnd   = t;
        }
        long total = Math.max(0, sessionEnd - sessionStart);

        long idle   = idleCalculator.calculateIdleMillis(interactionEvents, modelEvents, sessionStart, sessionEnd);
        long active = Math.max(0, total - idle);

        // ---- Compile ----------------------------------------------------------
        List<CompileEvent> compileEvents = telemetry.eventsOfType(CompileEvent.class);

        Map<Long, Boolean> outcomeByAttempt = new LinkedHashMap<>();
        for (CompileEvent e : compileEvents) {
            outcomeByAttempt.merge(e.getAttemptId(), e.isSuccess(), (a, b) -> a && b);
        }
        List<Boolean> outcomes = new ArrayList<>(outcomeByAttempt.values());
        int success = (int) outcomes.stream().filter(Boolean::booleanValue).count();
        int totalCompilations = outcomes.size();
        int failed = totalCompilations - success;

        Map<String, Integer> freqByCategory = compileEvents.stream()
                .filter(e -> !e.isSuccess())
                .collect(Collectors.groupingBy(
                        e -> e.getCategory() == null
                                ? CompileErrorClassifier.Category.OTHER.name()
                                : e.getCategory().name(),
                        TreeMap::new, Collectors.summingInt(e -> 1)));

        List<Long> failureTimestamps = compileEvents.stream()
                .filter(e -> !e.isSuccess())
                .map(TelemetryEvent::getTimestamp)
                .sorted()
                .toList();
        Map<String, Integer> histogram = histogramBuilder.build(failureTimestamps);

        // ---- Execution --------------------------------------------------------
        List<ExecutionEvent> execEvents = telemetry.eventsOfType(ExecutionEvent.class);
        int completed = 0, exception = 0, terminated = 0;
        for (ExecutionEvent e : execEvents) {
            switch (e.getOutcome()) {
                case COMPLETED -> completed++;
                case EXCEPTION -> exception++;
                case TERMINATED -> terminated++;
                case UNKNOWN -> { }
            }
        }

        // ---- Timeline ---------------------------------------------------------
        List<String> timeline = buildTimeline(telemetry.eventsOfType(LifecycleEvent.class), zone);

        return new ReportMetrics(
                active, idle,
                freqByCategory, histogram,
                outcomes, timeline,
                totalCompilations, success, failed,
                execEvents.size(), completed, exception, terminated);
    }

    private List<String> buildTimeline(List<LifecycleEvent> events, ZoneId zone) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(zone);
        return events.stream()
                .filter(e -> e.getType() == LifecycleEventType.CLASS_CREATED
                          || e.getType() == LifecycleEventType.CLASS_MODIFIED
                          || e.getType() == LifecycleEventType.CLASS_RENAMED)
                .sorted(Comparator.comparingLong(TelemetryEvent::getTimestamp))
                .map(e -> fmt.format(Instant.ofEpochMilli(e.getTimestamp()))
                        + "  –  " + e.getType() + ": " + e.getClassName()
                        + (e.getDetail() != null ? " (" + e.getDetail() + ")" : ""))
                .toList();
    }
}