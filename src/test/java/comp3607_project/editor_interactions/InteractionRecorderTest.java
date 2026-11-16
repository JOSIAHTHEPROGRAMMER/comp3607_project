package comp3607_project.editor_interactions;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * startup() needs a live BlueJ object, so only the parts that work without
 * BlueJ are tested here.
 */
class InteractionRecorderTest {

    @Test
    void reportsItsNameAndVersion() {
        InteractionRecorder recorder = new InteractionRecorder();

        assertEquals("Interaction Recorder", recorder.getName());
        assertEquals("2.0", recorder.getVersion());
    }

    @Test
    void describesWhatItRecords() {
        InteractionRecorder recorder = new InteractionRecorder();

        String description = recorder.getDescription();

        assertTrue(description.contains("typing cadence"));
        assertTrue(description.contains("idle time"));
        assertTrue(description.contains("edit frequency"));
    }

    @Test
    void isCompatibleWithTheRunningBlueJ() {
        assertTrue(new InteractionRecorder().isCompatible());
    }

    @Test
    void terminateBeforeStartupIsHarmless() {
        InteractionRecorder recorder = new InteractionRecorder();

        assertDoesNotThrow(recorder::terminate);
        assertDoesNotThrow(recorder::terminate);
    }
}
