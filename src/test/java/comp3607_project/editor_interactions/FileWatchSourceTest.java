package comp3607_project.editor_interactions;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileWatchSourceTest {

    private final PrintStream originalErr = System.err;
    private ByteArrayOutputStream errCapture;

    @BeforeEach
    void captureStderr() {
        errCapture = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errCapture));
    }

    @AfterEach
    void restoreStderr() {
        System.setErr(originalErr);
    }

    @Test
    void closeWithoutStartIsHarmless(@TempDir Path dir) {
        FileWatchSource source = new FileWatchSource(dir, new InteractionMonitor());

        assertDoesNotThrow(source::close);
    }

    @Test
    void startThenCloseTwiceIsHarmless(@TempDir Path dir) {
        FileWatchSource source = new FileWatchSource(dir, new InteractionMonitor());

        source.start();
        source.start(); // second call is ignored
        source.close();

        assertDoesNotThrow(source::close);
    }

    @Test
    void startOnAMissingFolderReportsTheFailureInsteadOfThrowing(@TempDir Path dir) {
        Path missing = dir.resolve("does-not-exist");
        FileWatchSource source = new FileWatchSource(missing, new InteractionMonitor());

        assertDoesNotThrow(source::start);

        assertTrue(errCapture.toString().contains("FileWatchSource: failed to start"));
        assertDoesNotThrow(source::close);
    }

    /**
     * Edits a .java file and a .txt file in a temporary folder. Only the .java
     * edit should be published. On macOS the JDK file watcher polls about every
     * 10 seconds, so this test can take up to that long; the timeout is generous.
     */
    @Test
    void javaFileEditIsPublishedAndOtherFilesAreIgnored(@TempDir Path dir) throws Exception {
        Path javaFile = dir.resolve("Dog.java");
        Path textFile = dir.resolve("notes.txt");
        Files.writeString(javaFile, "class Dog {}");
        Files.writeString(textFile, "hello");

        List<InteractionEvent> received = new CopyOnWriteArrayList<>();
        CountDownLatch gotEvent = new CountDownLatch(1);
        InteractionMonitor monitor = new InteractionMonitor();
        monitor.addObserver(e -> {
            received.add(e);
            gotEvent.countDown();
        });

        FileWatchSource source = new FileWatchSource(dir, monitor);
        source.start();
        try {
            Files.writeString(textFile, "changed");
            Files.writeString(javaFile, "class Dog { int age; }");
            // Polling watchers compare timestamps, so make sure they really changed.
            FileTime later = FileTime.fromMillis(System.currentTimeMillis() + 5_000);
            Files.setLastModifiedTime(textFile, later);
            Files.setLastModifiedTime(javaFile, later);

            assertTrue(gotEvent.await(30, TimeUnit.SECONDS), "no edit event was published");
            Thread.sleep(500); // let any further events from the same change arrive
        } finally {
            source.close();
        }

        InteractionEvent first = received.get(0);
        assertEquals(InteractionEvent.Kind.EDIT, first.kind());
        assertEquals("Dog.java", first.detail());
        assertEquals(0, first.gapSincePreviousMs());
        assertFalse(first.classifiedAsIdle());
        assertNotNull(first.timestamp());
        for (InteractionEvent event : received) {
            assertTrue(event.detail().endsWith(".java"), "non-java file was reported: " + event.detail());
        }
    }
}
