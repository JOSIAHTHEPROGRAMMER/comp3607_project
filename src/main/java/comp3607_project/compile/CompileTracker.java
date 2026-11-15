package comp3607_project.compile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.LongSupplier;

import comp3607_project.model.EventSink;

public class CompileTracker {
    private final EventSink sink;
    private final LongSupplier clock;
    private final CompileErrorClassifier classifier;

    private long nextAttemptId = 1;
    private long currentAttemptId = 0;
    private int errorsInCurrentAttempt = 0;
    private long lastStartTimestamp = -1;

    private int successfulCompilations = 0;
    private int failedCompilations = 0;
    private final List<Long> intervalsMillis = new ArrayList<>();

    public CompileTracker(EventSink sink, LongSupplier clock) {
        this(sink, clock, new CompileErrorClassifier());
    }

    public CompileTracker(EventSink sink, LongSupplier clock, CompileErrorClassifier classifier) {
        this.sink = sink;
        this.clock = clock;
        this.classifier = classifier;
    }

    public synchronized void compileStarted() {
        long now = clock.getAsLong();
        if (lastStartTimestamp >= 0) {
            intervalsMillis.add(now - lastStartTimestamp);
        }
        lastStartTimestamp = now;
        currentAttemptId = nextAttemptId++;
        errorsInCurrentAttempt = 0;
    }

    public synchronized void compileError(String project, String file, int line, String message) {
        ensureAttempt();
        CompileErrorClassifier.Classification c = classifier.classify(message);
        errorsInCurrentAttempt++;
        sink.accept(new CompileEvent(currentAttemptId, clock.getAsLong(), project, false,
                c.code(), c.category(), message, file, line));
    }

    public synchronized void compileSucceeded(String project, String file) {
        ensureAttempt();
        successfulCompilations++;
        sink.accept(new CompileEvent(currentAttemptId, clock.getAsLong(), project, true,
                null, null, null, file, -1));
        currentAttemptId = 0;
    }

    public synchronized void compileFailed(String project) {
        ensureAttempt();
        failedCompilations++;
        if (errorsInCurrentAttempt == 0) {
            // BlueJ reported a failure without any error callback: still record it.
            sink.accept(new CompileEvent(currentAttemptId, clock.getAsLong(), project, false,
                    "unknown", CompileErrorClassifier.Category.OTHER, null, null, -1));
        }
        currentAttemptId = 0;
    }

    public synchronized int getTotalAttempts() {
        return successfulCompilations + failedCompilations;
    }

    public synchronized int getSuccessfulCompilations() {
        return successfulCompilations;
    }

    public synchronized int getFailedCompilations() {
        return failedCompilations;
    }

    public synchronized List<Long> getIntervalsMillis() {
        return Collections.unmodifiableList(new ArrayList<>(intervalsMillis));
    }

    /** Guards against a result callback arriving without a start callback. */
    private void ensureAttempt() {
        if (currentAttemptId == 0) {
            currentAttemptId = nextAttemptId++;
            errorsInCurrentAttempt = 0;
        }
    }
}