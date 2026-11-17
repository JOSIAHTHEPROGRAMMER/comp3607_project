package comp3607_project.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import comp3607_project.compile.CompileEvent;
import comp3607_project.lifecycle.LifecycleEvent;
import comp3607_project.lifecycle.LifecycleEventType;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LifecycleEventMapperTest {

    private final LifecycleEventMapper mapper = new LifecycleEventMapper();

    private static LifecycleEvent created() {
        return new LifecycleEvent(LifecycleEventType.CLASS_CREATED, 1_000L, "alpha", "Dog",
                "created");
    }

    @Test
    void supportsOnlyLifecycleEvents() {
        assertTrue(mapper.supports(created()));
        assertFalse(mapper.supports(CompileEvent.success(1L, 1_000L, "alpha", "Dog.java")));
    }

    @Test
    void writesProjectClassAndDetail() {
        Map<String, Object> out = new LinkedHashMap<>();

        mapper.addFields(created(), out);

        assertEquals("alpha", out.get("project"));
        assertEquals("Dog", out.get("className"));
        assertEquals("created", out.get("detail"));
    }

    @Test
    void projectLevelEventsHaveNoClassName() {
        LifecycleEvent opened = new LifecycleEvent(LifecycleEventType.PROJECT_OPENED, 1_000L,
                "alpha", null, null);
        Map<String, Object> out = new LinkedHashMap<>();

        mapper.addFields(opened, out);

        assertEquals("alpha", out.get("project"));
        assertTrue(out.containsKey("className"));
        assertNull(out.get("className"));
    }

    @Test
    void projectOfReturnsTheEventsProject() {
        assertEquals("alpha", mapper.projectOf(created()));
    }

    @Test
    void lifecycleEventsDoNotChangeTheSessionTotals() {
        SummaryBuilder builder = new SummaryBuilder();

        mapper.accumulate(created(), builder);
        SessionSummary summary = builder.build();

        assertEquals(0, summary.totalCompilations());
        assertEquals(0, summary.totalRuns());
        assertEquals(0, summary.runtimeExceptions());
    }
}
