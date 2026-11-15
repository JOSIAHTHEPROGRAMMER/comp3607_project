package comp3607_project.editor_interactions;

public final class ThresholdIdleClassifier implements IdleClassifier {

    private final long thresholdMs;

    public ThresholdIdleClassifier(long thresholdMs) {
        if (thresholdMs <= 0) throw new IllegalArgumentException("thresholdMs must be > 0");
        this.thresholdMs = thresholdMs;
    }

    @Override
    public boolean isIdle(long gapMs) {
        return gapMs > thresholdMs;
    }
}