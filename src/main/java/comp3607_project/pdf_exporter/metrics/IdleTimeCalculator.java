package comp3607_project.pdf_exporter.metrics;


import comp3607_project.editor_interactions.InteractionEvent;
import comp3607_project.model.TelemetryEvent;

import java.util.List;

public interface IdleTimeCalculator {
    long calculateIdleMillis(List<InteractionEvent> interactionEvents,
                             List<TelemetryEvent> modelEvents,
                             long sessionStartMillis,
                             long sessionEndMillis);
}