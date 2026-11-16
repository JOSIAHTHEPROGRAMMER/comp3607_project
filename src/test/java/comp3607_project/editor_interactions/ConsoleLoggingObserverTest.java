package comp3607_project.editor_interactions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConsoleLoggingObserverTest {

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

    @Test
    void printsTheEventToTheConsole() {
        InteractionEvent event = new InteractionEvent(
                InteractionEvent.Kind.KEY, Instant.now(), 300, "B", false);

        new ConsoleLoggingObserver().onInteraction(event);

        assertEquals("[KEY] gap=300ms detail=B", outCapture.toString().trim());
    }

    @Test
    void printsIdleEventsWithTheIdleTag() {
        InteractionEvent event = new InteractionEvent(
                InteractionEvent.Kind.KEY, Instant.now(), 9_000, "B", true);

        new ConsoleLoggingObserver().onInteraction(event);

        assertEquals("[IDLE] gap=9000ms detail=B", outCapture.toString().trim());
    }
}
