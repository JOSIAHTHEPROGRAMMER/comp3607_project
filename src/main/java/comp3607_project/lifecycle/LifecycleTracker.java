package comp3607_project.lifecycle;

import comp3607_project.model.EventSink;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.LongSupplier;

public class LifecycleTracker {
    private final EventSink sink;
    private final LongSupplier clock;
    private final Map<String, Set<String>> knownClasses = new HashMap<>();

    public LifecycleTracker(EventSink sink, LongSupplier clock) {
        this.sink = sink;
        this.clock = clock;
    }

    public synchronized void projectOpened(String project, Collection<String> existingClasses) {
        knownClasses.put(project, new HashSet<>(existingClasses));
        emit(LifecycleEventType.PROJECT_OPENED, project, null, null);
    }

    public synchronized void projectClosed(String project) {
        knownClasses.remove(project);
        emit(LifecycleEventType.PROJECT_CLOSED, project, null, null);
    }

    public synchronized void classStateChanged(String project, String className, boolean compiled, boolean hasError) {
        Set<String> known = knownClasses.computeIfAbsent(project, key -> new HashSet<>());
        if (known.add(className)) {
            emit(LifecycleEventType.CLASS_CREATED, project, className, null);
            return;
        }
        if (!compiled && !hasError) {
            emit(LifecycleEventType.CLASS_MODIFIED, project, className, null);
        }
    }

    public synchronized void classRenamed(String project, String oldName, String newName) {
        Set<String> known = knownClasses.computeIfAbsent(project, key -> new HashSet<>());
        known.remove(oldName);
        known.add(newName);
        emit(LifecycleEventType.CLASS_RENAMED, project, newName, oldName);
    }

    public synchronized void classRemoved(String project, String className) {
        Set<String> known = knownClasses.computeIfAbsent(project, key -> new HashSet<>());
        known.remove(className);
        emit(LifecycleEventType.CLASS_DELETED, project, className, null);
    }

    private void emit(LifecycleEventType type, String project, String className, String detail) {
        sink.accept(new LifecycleEvent(type, clock.getAsLong(), project, className, detail));
    }
}