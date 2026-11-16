package comp3607_project.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConsoleSinkTest {

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

    private static TelemetryEvent stubEvent(String text) {
        return new TelemetryEvent() {
            @Override
            public String getEventType() {
                return "STUB";
            }

            @Override
            public long getTimestamp() {
                return 42L;
            }

            @Override
            public String getTarget() {
                return "target";
            }

            @Override
            public String toString() {
                return text;
            }
        };
    }

    @Test
    void acceptPrintsTheEventWithATelemetryPrefix() {
        new ConsoleSink().accept(stubEvent("hello"));

        assertEquals("[Telemetry] hello", outCapture.toString().trim());
    }

    @Test
    void consoleSinkCanBeUsedThroughTheEventSinkInterface() {
        EventSink sink = new ConsoleSink();

        sink.accept(stubEvent("via interface"));

        assertEquals("[Telemetry] via interface", outCapture.toString().trim());
    }
}
