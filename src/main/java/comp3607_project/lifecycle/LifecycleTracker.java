package comp3607_project.lifecycle;

import comp3607_project.model.EventSink;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.LongSupplier;

public class LifecycleTracker {
    public static final long DEFAULT_SETTLE_MILLIS = 5000;

    private final EventSink sink;
    private final LongSupplier clock;
    private final long settleMillis;
    private final Map<String, Set<String>> knownClasses = new HashMap<>();
    private final Map<String, Long> settleDeadlines = new HashMap<>();

    public LifecycleTracker(EventSink sink, LongSupplier clock) {
        this(sink, clock, DEFAULT_SETTLE_MILLIS);
    }

    public LifecycleTracker(EventSink sink, LongSupplier clock, long settleMillis) {
        this.sink = sink;
        this.clock = clock;
        this.settleMillis = settleMillis;
    }

    public synchronized void projectOpened(String project, Collection<String> existingClasses) {
        knownClasses.put(project, new HashSet<>(existingClasses));
        emit(LifecycleEventType.PROJECT_OPENED, project, null, null);
    }

    public synchronized void projectClosed(String project) {
        knownClasses.remove(project);
        settleDeadlines.keySet().removeIf(key -> key.startsWith(project + "::"));
        emit(LifecycleEventType.PROJECT_CLOSED, project, null, null);
    }

    public synchronized void classStateChanged(String project, String className, boolean compiled, boolean hasError) {
        Set<String> known = knownClasses.computeIfAbsent(project, key -> new HashSet<>());
        if (known.add(className)) {
            armSettle(project, className);
            emit(LifecycleEventType.CLASS_CREATED, project, className, null);
            return;
        }
        if (compiled || hasError) {
            return;
        }
        if (consumeSettle(project, className)) {
            return;
        }
        emit(LifecycleEventType.CLASS_MODIFIED, project, className, null);
    }

    public synchronized void classRenamed(String project, String oldName, String newName) {
        Set<String> known = knownClasses.computeIfAbsent(project, key -> new HashSet<>());
        known.remove(oldName);
        known.add(newName);
        settleDeadlines.remove(keyOf(project, oldName));
        armSettle(project, newName);
        emit(LifecycleEventType.CLASS_RENAMED, project, newName, oldName);
    }

    public synchronized void classRemoved(String project, String className) {
        Set<String> known = knownClasses.computeIfAbsent(project, key -> new HashSet<>());
        known.remove(className);
        settleDeadlines.remove(keyOf(project, className));
        emit(LifecycleEventType.CLASS_DELETED, project, className, null);
    }

    private void armSettle(String project, String className) {
        settleDeadlines.put(keyOf(project, className), clock.getAsLong() + settleMillis);
    }

    private boolean consumeSettle(String project, String className) {
        Long deadline = settleDeadlines.remove(keyOf(project, className));
        return deadline != null && clock.getAsLong() <= deadline;
    }

    private String keyOf(String project, String className) {
        return project + "::" + className;
    }

    private void emit(LifecycleEventType type, String project, String className, String detail) {
        sink.accept(new LifecycleEvent(type, clock.getAsLong(), project, className, detail));
    }
}