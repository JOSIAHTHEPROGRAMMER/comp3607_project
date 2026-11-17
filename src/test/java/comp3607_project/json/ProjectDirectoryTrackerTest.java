package comp3607_project.json;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class ProjectDirectoryTrackerTest {

    @Test
    void startsEmpty() {
        assertTrue(new ProjectDirectoryTracker().getLastKnownDir().isEmpty());
    }

    @Test
    void remembersOpenedProject() {
        ProjectDirectoryTracker t = new ProjectDirectoryTracker();
        t.projectOpened(Path.of("a"));
        assertEquals(Path.of("a"), t.getLastKnownDir().get());
    }

    @Test
    void latestProjectWins() {
        ProjectDirectoryTracker t = new ProjectDirectoryTracker();
        t.projectOpened(Path.of("a"));
        t.projectOpened(Path.of("b"));
        assertEquals(Path.of("b"), t.getLastKnownDir().get());
    }

    @Test
    void nullIsIgnored() {
        ProjectDirectoryTracker t = new ProjectDirectoryTracker();
        t.projectOpened(Path.of("a"));
        t.projectOpened(null);
        assertEquals(Path.of("a"), t.getLastKnownDir().get());
    }
}