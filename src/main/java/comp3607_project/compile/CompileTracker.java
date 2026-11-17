package comp3607_project.compile;

import java.util.function.LongSupplier;

import comp3607_project.model.EventSink;

/**
 * Turns compile callbacks into CompileEvents. No BlueJ dependency, so it can be
 * unit tested.
 */
public class CompileTracker {
    private final EventSink sink;
    private final LongSupplier clock;
    private final CompileErrorClassifier classifier;

    private long nextAttemptId = 1;
    private long currentAttemptId = 0;
    private int errorsInCurrentAttempt = 0;

    public CompileTracker(EventSink sink, LongSupplier clock) {
        this(sink, clock, new CompileErrorClassifier());
    }

    public CompileTracker(EventSink sink, LongSupplier clock, CompileErrorClassifier classifier) {
        this.sink = sink;
        this.clock = clock;
        this.classifier = classifier;
    }

    public synchronized void compileStarted() {
        startAttempt();
    }

    public synchronized void compileError(String project, String file, int line, String message) {
        ensureAttempt();
        CompileErrorClassifier.Classification c = classifier.classify(message);
        errorsInCurrentAttempt++;
        sink.accept(CompileEvent.error(currentAttemptId, clock.getAsLong(), project, file, line,
                c.code(), c.category(), message));
    }

    public synchronized void compileSucceeded(String project, String file) {
        ensureAttempt();
        sink.accept(CompileEvent.success(currentAttemptId, clock.getAsLong(), project, file));
        currentAttemptId = 0;
    }

    public synchronized void compileFailed(String project) {
        ensureAttempt();
        if (errorsInCurrentAttempt == 0) {
            // BlueJ reported a failure without any error callback: still record it.
            sink.accept(CompileEvent.error(currentAttemptId, clock.getAsLong(), project, null,
                    -1, "unknown", CompileErrorClassifier.Category.OTHER, null));
        }
        currentAttemptId = 0;
    }

    /** Guards against a result callback arriving without a start callback. */
    private void ensureAttempt() {
        if (currentAttemptId == 0) {
            startAttempt();
        }
    }

    private void startAttempt() {
        currentAttemptId = nextAttemptId++;
        errorsInCurrentAttempt = 0;
    }
}