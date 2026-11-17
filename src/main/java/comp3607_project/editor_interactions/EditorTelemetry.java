package comp3607_project.editor_interactions;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import comp3607_project.model.EventSink;

/**
 * Starts and stops the editor interaction sources and routes their events to
 * the sink.
 */
public final class EditorTelemetry {
    private static final long IDLE_THRESHOLD_MS = 5_000;

    private final EventSink sink;
    private final Supplier<String> currentProject;
    private final Map<Path, FileWatchSource> watchers = new HashMap<>();
    private KeyboardSource keyboard;

    public EditorTelemetry(EventSink sink, Supplier<String> currentProject) {
        this.sink = sink;
        this.currentProject = currentProject;
    }

    public synchronized void start() {
        if (keyboard != null) {
            return;
        }
        InteractionMonitor monitor = new InteractionMonitor();
        monitor.addObserver(new TelemetryInteractionObserver(sink, currentProject,
                TelemetryInteractionObserver::editsAndIdleOnly));
        keyboard = new KeyboardSource(monitor, new ThresholdIdleClassifier(IDLE_THRESHOLD_MS));
        keyboard.start();
    }

    /** Starts watching a project folder, once per folder. */
    public synchronized void projectOpened(Path dir) {
        if (dir == null || watchers.containsKey(dir)) {
            return;
        }
        Path name = dir.getFileName();
        String project = name == null ? null : name.toString();
        InteractionMonitor monitor = new InteractionMonitor();
        monitor.addObserver(new TelemetryInteractionObserver(sink, () -> project));
        FileWatchSource watcher = new FileWatchSource(dir, monitor);
        watcher.start();
        watchers.put(dir, watcher);
    }

    public synchronized void projectClosed(Path dir) {
        FileWatchSource watcher = dir == null ? null : watchers.remove(dir);
        if (watcher != null) {
            watcher.close();
        }
    }

    public synchronized void close() {
        watchers.values().forEach(FileWatchSource::close);
        watchers.clear();
        if (keyboard != null) {
            keyboard.close();
            keyboard = null;
        }
    }
}