package comp3607_project.editor_interactions;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public final class FileWatchSource implements InteractionSource {

    private final Path directory;
    private final InteractionMonitor monitor;

    private final AtomicLong lastEditTime = new AtomicLong(0);
    private final AtomicBoolean started = new AtomicBoolean(false);

    private volatile WatchService watchService;
    private volatile Thread thread;

    public FileWatchSource(Path directory, InteractionMonitor monitor) {
        this.directory = directory;
        this.monitor = monitor;
    }

    @Override
    public void start() {
        if (!started.compareAndSet(false, true)) return;

        try {
            watchService = FileSystems.getDefault().newWatchService();
            directory.register(watchService, ENTRY_MODIFY, ENTRY_CREATE);

            thread = new Thread(this::watchLoop, "InteractionRecorder-watch");
            thread.setDaemon(true);
            thread.start();
        } catch (IOException ex) {
            started.set(false);
            System.err.println("FileWatchSource: failed to start: " + ex);
        }
    }

    private void watchLoop() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == ENTRY_MODIFY) {
                        handleModify((Path) event.context());
                    }
                }
                key.reset();
            }
        } catch (InterruptedException | ClosedWatchServiceException ignored) {
            // Shutdown signals — exit cleanly.
        }
    }

    private void handleModify(Path fileName) {
        if (!fileName.toString().endsWith(".java")) return;

        long now = System.currentTimeMillis();
        long previous = lastEditTime.getAndSet(now);
        long gap = (previous == 0) ? 0 : now - previous;

        monitor.publish(new InteractionEvent(
                InteractionEvent.Kind.EDIT,
                Instant.ofEpochMilli(now),
                gap,
                fileName.toString(),
                false));
    }

    @Override
    public void close() {
        if (!started.compareAndSet(true, false)) return;
        try {
            if (watchService != null) watchService.close();
        } catch (IOException ignored) {}
        if (thread != null) thread.interrupt();
    }
}