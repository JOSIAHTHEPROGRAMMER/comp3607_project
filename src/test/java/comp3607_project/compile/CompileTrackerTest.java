package comp3607_project.compile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import comp3607_project.compile.CompileErrorClassifier.Category;
import comp3607_project.model.EventSink;

class CompileTrackerTest {

    private List<CompileEvent> events;
    private AtomicLong now;
    private CompileTracker tracker;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        events = new ArrayList<>();
        now = new AtomicLong(1000);
        EventSink sink = event -> events.add((CompileEvent) event);
        tracker = new CompileTracker(sink, now::get);
    }

    @Test
    void successfulCompileEmitsSuccessEvent() {
        tracker.compileStarted();
        tracker.compileSucceeded("proj", "Dog.java");

        assertEquals(1, events.size());
        CompileEvent e = events.get(0);
        assertTrue(e.isSuccess());
        assertEquals("COMPILE_SUCCESS", e.getEventType());
        assertEquals("proj", e.getProject());
        assertEquals("Dog.java", e.getTarget());
        assertEquals(1000, e.getTimestamp());
        assertNull(e.getErrorCode());
    }

    @Test
    void errorEventCarriesCodeCategoryLineAndMessage() {
        tracker.compileStarted();
        tracker.compileError("proj", "Dog.java", 19, "';' expected");

        CompileEvent e = events.get(0);
        assertFalse(e.isSuccess());
        assertEquals("COMPILE_ERROR", e.getEventType());
        assertEquals("expected", e.getErrorCode());
        assertEquals(Category.SYNTAX, e.getCategory());
        assertEquals(19, e.getLine());
        assertEquals("';' expected", e.getMessage());
        assertEquals("Dog.java", e.getFile());
    }

    @Test
    void multipleErrorsInOneAttemptShareAttemptId() {
        tracker.compileStarted();
        tracker.compileError("proj", "Dog.java", 5, "cannot find symbol");
        tracker.compileError("proj", "Dog.java", 9,
                "incompatible types: java.lang.String cannot be converted to int");
        tracker.compileFailed("proj");

        assertEquals(2, events.size());
        assertEquals(events.get(0).getAttemptId(), events.get(1).getAttemptId());
    }

    @Test
    void eachCompileGetsNextAttemptId() {
        tracker.compileStarted();
        tracker.compileSucceeded("proj", "Dog.java");
        tracker.compileStarted();
        tracker.compileError("proj", "Dog.java", 3, "';' expected");
        tracker.compileFailed("proj");

        assertEquals(1, events.get(0).getAttemptId());
        assertEquals(2, events.get(1).getAttemptId());
    }

    @Test
    void failedWithoutErrorCallbackStillRecordsUnknownError() {
        tracker.compileStarted();
        tracker.compileFailed("proj");

        assertEquals(1, events.size());
        assertFalse(events.get(0).isSuccess());
        assertEquals("unknown", events.get(0).getErrorCode());
        assertEquals(Category.OTHER, events.get(0).getCategory());
    }

    @Test
    void failedAfterErrorsDoesNotAddExtraEvent() {
        tracker.compileStarted();
        tracker.compileError("proj", "Dog.java", 3, "';' expected");
        tracker.compileFailed("proj");

        assertEquals(1, events.size());
    }

    @Test
    void resultWithoutStartStillGetsAnAttemptId() {
        tracker.compileSucceeded("proj", "Dog.java");

        assertEquals(1, events.size());
        assertEquals(1, events.get(0).getAttemptId());
    }

}