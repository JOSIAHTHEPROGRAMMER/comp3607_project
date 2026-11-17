package comp3607_project.pdf_exporter.metrics;


import comp3607_project.editor_interactions.InteractionEvent;
import comp3607_project.model.TelemetryEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class InteractionIdleTimeCalculator implements IdleTimeCalculator {

    private final long idleThresholdMillis;

    public InteractionIdleTimeCalculator(long idleThresholdMillis) {
        if (idleThresholdMillis <= 0) throw new IllegalArgumentException("threshold must be > 0");
        this.idleThresholdMillis = idleThresholdMillis;
    }

    @Override
    public long calculateIdleMillis(List<InteractionEvent> interactionEvents,
                                    List<TelemetryEvent> modelEvents,
                                    long sessionStartMillis,
                                    long sessionEndMillis) {
        long sessionDuration = Math.max(0, sessionEndMillis - sessionStartMillis);
        if (sessionDuration == 0) {
            return 0;
        }

        List<Activity> activities = new ArrayList<>();
        for (InteractionEvent event : interactionEvents) {
            activities.add(new Activity(event.timestamp().toEpochMilli(), event.classifiedAsIdle()));
        }
        for (TelemetryEvent event : modelEvents) {
            activities.add(new Activity(event.getTimestamp(), false));
        }
        if (activities.isEmpty()) {
            return sessionDuration;
        }

        activities.sort(Comparator.comparingLong(Activity::timestampMillis));
        long idle = 0;
        long prev = sessionStartMillis;

        for (Activity activity : activities) {
            long now = Math.max(sessionStartMillis,
                    Math.min(sessionEndMillis, activity.timestampMillis()));
            long gap = now - prev;

            if (activity.classifiedAsIdle() || gap > idleThresholdMillis) {
                idle += (gap > idleThresholdMillis) ? gap - idleThresholdMillis : gap;
            }
            prev = Math.max(prev, now);
        }

        long tail = sessionEndMillis - prev;
        if (tail > idleThresholdMillis) idle += tail - idleThresholdMillis;
        return Math.min(sessionDuration, idle);
    }

    private record Activity(long timestampMillis, boolean classifiedAsIdle) {
    }
}
