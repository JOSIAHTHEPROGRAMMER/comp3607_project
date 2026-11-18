package comp3607_project.pdf_exporter.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import comp3607_project.editor_interactions.InteractionEvent;
import comp3607_project.model.TelemetryEvent;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Active time vs idle time. Idle time is every silent stretch longer than the threshold
 * (only the part beyond the threshold counts), plus the gap before an event the editor
 * itself classified as idle.
 */
class InteractionIdleTimeCalculatorTest {

    private final InteractionIdleTimeCalculator calculator = new InteractionIdleTimeCalculator(5_000);

    private static TelemetryEvent modelEvent(long timestamp) {
        return new TelemetryEvent() {
            @Override
            public String getEventType() {
                return "TEST";
            }

            @Override
            public long getTimestamp() {
                return timestamp;
            }

            @Override
            public String getTarget() {
                return "t";
            }
        };
    }

    private static InteractionEvent key(long timestamp, boolean idle) {
        return new InteractionEvent(InteractionEvent.Kind.KEY, Instant.ofEpochMilli(timestamp),
                0, "A", idle);
    }

    private long idle(List<InteractionEvent> interactions, List<TelemetryEvent> model,
            long start, long end) {
        return calculator.calculateIdleMillis(interactions, model, start, end);
    }

    @Test
    void sessionWithNoActivityIsEntirelyIdle() {
        assertEquals(60_000, idle(List.of(), List.of(), 0, 60_000));
    }

    @Test
    void zeroLengthSessionHasNoIdleTime() {
        assertEquals(0, idle(List.of(), List.of(), 5_000, 5_000));
    }

    @Test
    void endBeforeStartIsTreatedAsZeroLength() {
        assertEquals(0, idle(List.of(), List.of(), 10_000, 5_000));
    }

    @Test
    void steadyActivityHasNoIdleTime() {
        List<TelemetryEvent> model = List.of(modelEvent(1_000), modelEvent(2_000),
                modelEvent(3_000), modelEvent(4_000), modelEvent(5_000));

        assertEquals(0, idle(List.of(), model, 0, 5_000));
    }

    @Test
    void onlyTheTimeBeyondTheThresholdCountsAsIdle() {
        List<TelemetryEvent> model = List.of(modelEvent(1_000), modelEvent(21_000));

        // the 20s gap is 15s longer than the 5s threshold
        assertEquals(15_000, idle(List.of(), model, 0, 21_000));
    }

    @Test
    void silenceAtTheEndOfTheSessionCounts() {
        List<TelemetryEvent> model = List.of(modelEvent(1_000));

        // 10s of silence after the last event, 5s beyond the threshold
        assertEquals(5_000, idle(List.of(), model, 0, 11_000));
    }

    @Test
    void shortSilenceAtTheEndIsNotIdle() {
        List<TelemetryEvent> model = List.of(modelEvent(1_000));

        assertEquals(0, idle(List.of(), model, 0, 4_000));
    }

    @Test
    void gapBeforeAnEventTheEditorCalledIdleCountsInFull() {
        List<InteractionEvent> interactions = List.of(key(2_000, false), key(4_000, true));

        assertEquals(2_000, idle(interactions, List.of(), 0, 4_000));
    }

    @Test
    void eventsAreSortedBeforeTheyAreMeasured() {
        List<TelemetryEvent> model = List.of(modelEvent(21_000), modelEvent(1_000));

        assertEquals(15_000, idle(List.of(), model, 0, 21_000));
    }

    @Test
    void interactionAndModelEventsAreCombined() {
        List<InteractionEvent> interactions = List.of(key(1_000, false));
        List<TelemetryEvent> model = List.of(modelEvent(3_000));

        assertEquals(0, idle(interactions, model, 0, 3_000));
    }

    @Test
    void eventsOutsideTheSessionAreClampedToItsBounds() {
        List<TelemetryEvent> model = List.of(modelEvent(-5_000), modelEvent(99_999));

        // first event clamps to the start, second to the end: one 10s gap, 5s beyond threshold
        assertEquals(5_000, idle(List.of(), model, 0, 10_000));
    }

    @Test
    void aLowerThresholdFindsMoreIdleTime() {
        InteractionIdleTimeCalculator strict = new InteractionIdleTimeCalculator(1_000);
        List<TelemetryEvent> model = List.of(modelEvent(500), modelEvent(3_000));

        assertEquals(1_500, strict.calculateIdleMillis(List.of(), model, 0, 3_000));
    }

    @Test
    void thresholdMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> new InteractionIdleTimeCalculator(0));
        assertThrows(IllegalArgumentException.class, () -> new InteractionIdleTimeCalculator(-1));
    }

    @Test
    void worksThroughTheIdleTimeCalculatorInterface() {
        IdleTimeCalculator asInterface = calculator;

        assertEquals(10_000, asInterface.calculateIdleMillis(List.of(), List.of(), 0, 10_000));
    }
}
