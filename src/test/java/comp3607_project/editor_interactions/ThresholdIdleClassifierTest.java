package comp3607_project.editor_interactions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ThresholdIdleClassifierTest {

    @Test
    void gapAboveThresholdIsIdle() {
        assertTrue(new ThresholdIdleClassifier(5_000).isIdle(5_001));
    }

    @Test
    void gapEqualToThresholdIsNotIdle() {
        assertFalse(new ThresholdIdleClassifier(5_000).isIdle(5_000));
    }

    @Test
    void gapBelowThresholdIsNotIdle() {
        assertFalse(new ThresholdIdleClassifier(5_000).isIdle(120));
    }

    @Test
    void zeroGapIsNotIdle() {
        assertFalse(new ThresholdIdleClassifier(5_000).isIdle(0));
    }

    @Test
    void zeroThresholdIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ThresholdIdleClassifier(0));
    }

    @Test
    void negativeThresholdIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ThresholdIdleClassifier(-1));
    }
}
