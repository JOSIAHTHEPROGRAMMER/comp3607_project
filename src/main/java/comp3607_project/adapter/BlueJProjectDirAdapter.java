package comp3607_project.adapter;

import java.nio.file.Path;
import java.util.function.Consumer;

import bluej.extensions2.ExtensionException;
import bluej.extensions2.event.PackageEvent;
import bluej.extensions2.event.PackageListener;
import comp3607_project.json.ProjectDirectoryTracker;

/**
 * Remembers each opened project's folder and reports open and close to
 * callbacks.
 */
public class BlueJProjectDirAdapter implements PackageListener {
    private final ProjectDirectoryTracker tracker;
    private final Consumer<Path> onProjectOpened;
    private final Consumer<Path> onProjectClosing;

    public BlueJProjectDirAdapter(ProjectDirectoryTracker tracker, Consumer<Path> onProjectOpened,
            Consumer<Path> onProjectClosing) {
        this.tracker = tracker;
        this.onProjectOpened = onProjectOpened;
        this.onProjectClosing = onProjectClosing;
    }

    @Override
    public void packageOpened(PackageEvent ev) {
        Path dir = dirOf(ev);
        if (dir == null) {
            return;
        }
        try {
            tracker.projectOpened(dir);
            onProjectOpened.accept(dir);
        } catch (RuntimeException e) {
            System.out.println("[Telemetry] project open handling failed: " + e);
        }
    }

    @Override
    public void packageClosing(PackageEvent ev) {
        try {
            onProjectClosing.accept(dirOf(ev));
        } catch (RuntimeException e) {
            System.out.println("[Telemetry] project close handling failed: " + e);
        }
    }

    private Path dirOf(PackageEvent ev) {
        try {
            return ev.getPackage().getProject().getDir().toPath();
        } catch (ExtensionException | RuntimeException e) {
            System.out.println("[Telemetry] could not read project folder: " + e);
            return null;
        }
    }
}