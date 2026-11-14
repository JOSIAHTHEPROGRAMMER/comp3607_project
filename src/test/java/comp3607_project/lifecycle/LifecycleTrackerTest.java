package comp3607_project.lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import comp3607_project.model.EventSink;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LifecycleTrackerTest {

    private static final long SETTLE = 5000;

    private List<LifecycleEvent> events;
    private AtomicLong now;
    private LifecycleTracker tracker;

    @BeforeEach
    void setUp() {
        events = new ArrayList<>();
        now = new AtomicLong(1000);
        EventSink sink = event -> events.add((LifecycleEvent) event);
        tracker = new LifecycleTracker(sink, now::get, SETTLE);
    }

    @Test
    void projectOpenedEmitsEventWithTimestamp() {
        tracker.projectOpened("proj", List.of());

        assertEquals(1, events.size());
        assertEquals(LifecycleEventType.PROJECT_OPENED, events.get(0).getType());
        assertEquals("proj", events.get(0).getProject());
        assertEquals(1000, events.get(0).getTimestamp());
    }

    @Test
    void projectClosedEmitsEvent() {
        tracker.projectOpened("proj", List.of());
        tracker.projectClosed("proj");

        assertEquals(2, events.size());
        assertEquals(LifecycleEventType.PROJECT_CLOSED, events.get(1).getType());
    }

    @Test
    void unknownClassIsReportedAsCreatedOnly() {
        tracker.projectOpened("proj", List.of());
        tracker.classStateChanged("proj", "Dog", false, false);

        assertEquals(2, events.size());
        assertEquals(LifecycleEventType.CLASS_CREATED, events.get(1).getType());
        assertEquals("Dog", events.get(1).getClassName());
    }

    @Test
    void modifiedSignalRightAfterCreationIsIgnored() {
        tracker.projectOpened("proj", List.of());
        tracker.classStateChanged("proj", "Dog", false, false);
        now.addAndGet(5);
        tracker.classStateChanged("proj", "Dog", false, false);

        assertEquals(2, events.size());
    }

    @Test
    void modifiedSignalAfterSettleWindowIsReported() {
        tracker.projectOpened("proj", List.of());
        tracker.classStateChanged("proj", "Dog", false, false);
        now.addAndGet(SETTLE + 1);
        tracker.classStateChanged("proj", "Dog", false, false);

        assertEquals(3, events.size());
        assertEquals(LifecycleEventType.CLASS_MODIFIED, events.get(2).getType());
    }

    @Test
    void editOfExistingClassIsReportedAsModified() {
        tracker.projectOpened("proj", List.of("Dog"));
        tracker.classStateChanged("proj", "Dog", false, false);

        assertEquals(2, events.size());
        assertEquals(LifecycleEventType.CLASS_MODIFIED, events.get(1).getType());
        assertEquals("Dog", events.get(1).getClassName());
    }

    @Test
    void compiledStateIsNotReported() {
        tracker.projectOpened("proj", List.of("Dog"));
        tracker.classStateChanged("proj", "Dog", true, false);

        assertEquals(1, events.size());
    }

    @Test
    void compileErrorStateIsNotReportedAsModification() {
        tracker.projectOpened("proj", List.of("Dog"));
        tracker.classStateChanged("proj", "Dog", false, true);

        assertEquals(1, events.size());
    }

    @Test
    void renameEmitsEventWithOldNameAsDetail() {
        tracker.projectOpened("proj", List.of("Dog"));
        tracker.classRenamed("proj", "Dog", "Cat");

        assertEquals(2, events.size());
        assertEquals(LifecycleEventType.CLASS_RENAMED, events.get(1).getType());
        assertEquals("Cat", events.get(1).getClassName());
        assertEquals("Dog", events.get(1).getDetail());
    }

    @Test
    void renamedClassIsNotTreatedAsNewAfterwards() {
        tracker.projectOpened("proj", List.of("Dog"));
        tracker.classRenamed("proj", "Dog", "Cat");
        now.addAndGet(SETTLE + 1);
        tracker.classStateChanged("proj", "Cat", false, false);

        assertEquals(3, events.size());
        assertEquals(LifecycleEventType.CLASS_MODIFIED, events.get(2).getType());
    }

    @Test
    void modifiedSignalRightAfterRenameIsIgnored() {
        tracker.projectOpened("proj", List.of("Dog"));
        tracker.classRenamed("proj", "Dog", "Cat");
        now.addAndGet(10);
        tracker.classStateChanged("proj", "Cat", false, false);

        assertEquals(2, events.size());
    }

    @Test
    void removalEmitsDeletedEvent() {
        tracker.projectOpened("proj", List.of("Dog"));
        tracker.classRemoved("proj", "Dog");

        assertEquals(2, events.size());
        assertEquals(LifecycleEventType.CLASS_DELETED, events.get(1).getType());
        assertEquals("Dog", events.get(1).getClassName());
    }

    @Test
    void removedClassIsCreatedAgainWhenSeenAgain() {
        tracker.projectOpened("proj", List.of("Dog"));
        tracker.classRemoved("proj", "Dog");
        tracker.classStateChanged("proj", "Dog", false, false);

        assertEquals(3, events.size());
        assertEquals(LifecycleEventType.CLASS_CREATED, events.get(2).getType());
    }

    @Test
    void closingProjectForgetsItsClasses() {
        tracker.projectOpened("proj", List.of("Dog"));
        tracker.projectClosed("proj");
        tracker.projectOpened("proj", List.of());
        tracker.classStateChanged("proj", "Dog", false, false);

        assertEquals(4, events.size());
        assertEquals(LifecycleEventType.CLASS_CREATED, events.get(3).getType());
    }
}