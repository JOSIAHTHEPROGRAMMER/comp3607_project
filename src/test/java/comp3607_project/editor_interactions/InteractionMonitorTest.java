package comp3607_project.editor_interactions;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InteractionMonitorTest {

    private static class RecordingObserver implements InteractionObserver {
        final List<InteractionEvent> received = new ArrayList<>();

        @Override
        public void onInteraction(InteractionEvent event) {
            received.add(event);
        }
    }

    private final PrintStream originalErr = System.err;
    private ByteArrayOutputStream errCapture;
    private InteractionMonitor monitor;

    @BeforeEach
    void setUp() {
        monitor = new InteractionMonitor();
        errCapture = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errCapture));
    }

    @AfterEach
    void restoreStderr() {
        System.setErr(originalErr);
    }

    private static InteractionEvent event(String detail) {
        return new InteractionEvent(InteractionEvent.Kind.KEY, Instant.now(), 0, detail, false);
    }

    @Test
    void publishDeliversTheEventToEveryObserver() {
        RecordingObserver first = new RecordingObserver();
        RecordingObserver second = new RecordingObserver();
        monitor.addObserver(first);
        monitor.addObserver(second);
        InteractionEvent e = event("A");

        monitor.publish(e);

        assertEquals(List.of(e), first.received);
        assertEquals(List.of(e), second.received);
    }

    @Test
    void publishKeepsEventsInTheOrderTheyWerePublished() {
        RecordingObserver observer = new RecordingObserver();
        monitor.addObserver(observer);
        InteractionEvent a = event("A");
        InteractionEvent b = event("B");

        monitor.publish(a);
        monitor.publish(b);

        assertEquals(List.of(a, b), observer.received);
    }

    @Test
    void publishWithNoObserversDoesNothing() {
        assertDoesNotThrow(() -> monitor.publish(event("A")));
    }

    @Test
    void nullObserverIsIgnored() {
        monitor.addObserver(null);

        assertDoesNotThrow(() -> monitor.publish(event("A")));
    }

    @Test
    void removedObserverNoLongerReceivesEvents() {
        RecordingObserver observer = new RecordingObserver();
        monitor.addObserver(observer);
        monitor.removeObserver(observer);

        monitor.publish(event("A"));

        assertTrue(observer.received.isEmpty());
    }

    @Test
    void removingAnObserverThatWasNeverAddedIsHarmless() {
        assertDoesNotThrow(() -> monitor.removeObserver(new RecordingObserver()));
    }

    @Test
    void failingObserverDoesNotStopTheOthers() {
        RecordingObserver healthy = new RecordingObserver();
        monitor.addObserver(e -> {
            throw new IllegalStateException("boom");
        });
        monitor.addObserver(healthy);

        monitor.publish(event("A"));

        assertEquals(1, healthy.received.size());
        assertTrue(errCapture.toString().contains("Observer failed"));
    }
}
