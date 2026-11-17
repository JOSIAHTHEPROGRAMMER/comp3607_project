package comp3607_project.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CompositeSinkTest {

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outCapture;

    @BeforeEach
    void captureStdout() {
        outCapture = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outCapture));
    }

    @AfterEach
    void restoreStdout() {
        System.setOut(originalOut);
    }

    private static TelemetryEvent event(String type) {
        return new TelemetryEvent() {
            @Override
            public String getEventType() {
                return type;
            }

            @Override
            public long getTimestamp() {
                return 1L;
            }

            @Override
            public String getTarget() {
                return "target";
            }
        };
    }

    @Test
    void forwardsEachEventToEverySink() {
        List<TelemetryEvent> first = new ArrayList<>();
        List<TelemetryEvent> second = new ArrayList<>();
        CompositeSink composite = new CompositeSink(first::add, second::add);
        TelemetryEvent e = event("A");

        composite.accept(e);

        assertEquals(1, first.size());
        assertEquals(1, second.size());
        assertSame(e, first.get(0));
        assertSame(e, second.get(0));
    }

    @Test
    void sinksReceiveEventsInTheOrderTheyArrive() {
        List<TelemetryEvent> received = new ArrayList<>();
        CompositeSink composite = new CompositeSink(received::add);
        TelemetryEvent a = event("A");
        TelemetryEvent b = event("B");

        composite.accept(a);
        composite.accept(b);

        assertEquals(List.of(a, b), received);
    }

    @Test
    void aFailingSinkDoesNotBlockTheOthers() {
        List<TelemetryEvent> healthy = new ArrayList<>();
        EventSink failing = e -> {
            throw new IllegalStateException("boom");
        };
        CompositeSink composite = new CompositeSink(failing, healthy::add);

        composite.accept(event("A"));

        assertEquals(1, healthy.size());
    }

    @Test
    void aFailingSinkIsReportedOnTheConsole() {
        EventSink failing = e -> {
            throw new IllegalStateException("boom");
        };

        new CompositeSink(failing).accept(event("A"));

        String printed = outCapture.toString();
        assertTrue(printed.contains("sink failed"));
        assertTrue(printed.contains("boom"));
    }

    @Test
    void aCompositeWithNoSinksDoesNothing() {
        assertDoesNotThrow(() -> new CompositeSink().accept(event("A")));
    }

    @Test
    void aCompositeCanBeUsedAsAnEventSink() {
        List<TelemetryEvent> received = new ArrayList<>();
        EventSink sink = new CompositeSink(received::add);

        sink.accept(event("A"));

        assertEquals(1, received.size());
    }
}
