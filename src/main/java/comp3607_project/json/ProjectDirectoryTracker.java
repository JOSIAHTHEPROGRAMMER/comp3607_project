package comp3607_project.json;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Remembers the folder of the most recently opened project, so the export still
 * knows where to write after BlueJ has closed the project.
 */
public class ProjectDirectoryTracker {
    private volatile Path lastKnownDir;

    public void projectOpened(Path dir) {
        if (dir != null) {
            lastKnownDir = dir;
        }
    }

    public Optional<Path> getLastKnownDir() {
        return Optional.ofNullable(lastKnownDir);
    }
}