package comp3607_project.lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LifecycleEventTest {

    @Test
    void targetIsClassNameWhenPresent() {
        LifecycleEvent event = new LifecycleEvent(LifecycleEventType.CLASS_CREATED, 1L, "proj", "Dog", null);

        assertEquals("Dog", event.getTarget());
    }

    @Test
    void targetIsProjectWhenThereIsNoClass() {
        LifecycleEvent event = new LifecycleEvent(LifecycleEventType.PROJECT_OPENED, 1L, "proj", null, null);

        assertEquals("proj", event.getTarget());
    }

    @Test
    void eventTypeIsTheEnumName() {
        LifecycleEvent event = new LifecycleEvent(LifecycleEventType.CLASS_DELETED, 1L, "proj", "Dog", null);

        assertEquals("CLASS_DELETED", event.getEventType());
    }
}