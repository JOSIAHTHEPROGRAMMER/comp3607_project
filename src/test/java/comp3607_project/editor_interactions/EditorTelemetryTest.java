package comp3607_project.editor_interactions;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import comp3607_project.model.TelemetryEvent;
import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class EditorTelemetryTest {

    private static int keyListenerCount() {
        return Toolkit.getDefaultToolkit().getAWTEventListeners(AWTEvent.KEY_EVENT_MASK).length;
    }

    @Test
    void startRegistersOneKeyListenerEvenWhenCalledTwice() {
        int before = keyListenerCount();
        EditorTelemetry telemetry = new EditorTelemetry(e -> {
        }, () -> "demo");
        try {
            telemetry.start();
            telemetry.start();

            assertEquals(before + 1, keyListenerCount());
        } finally {
            telemetry.close();
        }
    }

    @Test
    void closeRemovesTheKeyListener() {
        int before = keyListenerCount();
        EditorTelemetry telemetry = new EditorTelemetry(e -> {
        }, () -> "demo");
        telemetry.start();

        telemetry.close();

        assertEquals(before, keyListenerCount());
    }

    @Test
    void canBeRestartedAfterClose() {
        int before = keyListenerCount();
        EditorTelemetry telemetry = new EditorTelemetry(e -> {
        }, () -> "demo");
        telemetry.start();
        telemetry.close();

        telemetry.start();
        try {
            assertEquals(before + 1, keyListenerCount());
        } finally {
            telemetry.close();
        }
    }

    @Test
    void closeWithoutStartIsHarmless() {
        EditorTelemetry telemetry = new EditorTelemetry(e -> {
        }, () -> "demo");

        assertDoesNotThrow(telemetry::close);
    }

    @Test
    void nullProjectFoldersAreIgnored() {
        EditorTelemetry telemetry = new EditorTelemetry(e -> {
        }, () -> "demo");

        assertDoesNotThrow(() -> telemetry.projectOpened(null));
        assertDoesNotThrow(() -> telemetry.projectClosed(null));
        telemetry.close();
    }

    @Test
    void closingAFolderThatWasNeverOpenedIsHarmless(@TempDir Path dir) {
        EditorTelemetry telemetry = new EditorTelemetry(e -> {
        }, () -> "demo");

        assertDoesNotThrow(() -> telemetry.projectClosed(dir));
        telemetry.close();
    }

    @Test
    void openingTheSameFolderTwiceIsHarmless(@TempDir Path dir) {
        EditorTelemetry telemetry = new EditorTelemetry(e -> {
        }, () -> "demo");
        try {
            assertDoesNotThrow(() -> telemetry.projectOpened(dir));
            assertDoesNotThrow(() -> telemetry.projectOpened(dir));
        } finally {
            telemetry.close();
        }
    }

    @Test
    void openedThenClosedFolderCanBeClosedAgainSafely(@TempDir Path dir) {
        EditorTelemetry telemetry = new EditorTelemetry(e -> {
        }, () -> "demo");
        telemetry.projectOpened(dir);

        telemetry.projectClosed(dir);

        assertDoesNotThrow(() -> telemetry.projectClosed(dir));
        telemetry.close();
    }

    /**
     * Opens a folder, edits a .java file inside it and checks that an editor edit
     * event, labelled with the folder's name, reaches the sink. The JDK file
     * watcher
     * can take a few seconds on macOS, so the timeout is generous.
     */
    @Test
    void editingAFileInAnOpenedProjectReachesTheSink(@TempDir Path dir) throws Exception {
        Path javaFile = dir.resolve("Dog.java");
        Files.writeString(javaFile, "class Dog {}");

        List<TelemetryEvent> received = new CopyOnWriteArrayList<>();
        CountDownLatch gotEvent = new CountDownLatch(1);
        EditorTelemetry telemetry = new EditorTelemetry(e -> {
            received.add(e);
            gotEvent.countDown();
        }, () -> "ignored");

        telemetry.projectOpened(dir);
        try {
            Files.writeString(javaFile, "class Dog { int age; }");
            Files.setLastModifiedTime(javaFile,
                    FileTime.fromMillis(System.currentTimeMillis() + 5_000));

            assertTrue(gotEvent.await(30, TimeUnit.SECONDS), "no event reached the sink");
        } finally {
            telemetry.close();
        }

        InteractionTelemetryEvent event = assertInstanceOf(InteractionTelemetryEvent.class, received.get(0));
        assertEquals("EDITOR_EDIT", event.getEventType());
        assertEquals("Dog.java", event.getTarget());
        assertEquals(dir.getFileName().toString(), event.getProject());
    }
}
