package comp3607_project.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import comp3607_project.compile.CompileErrorClassifier.Category;
import comp3607_project.compile.CompileEvent;
import comp3607_project.model.TelemetryEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CompileEventMapperTest {

    private final CompileEventMapper mapper = new CompileEventMapper();

    private static CompileEvent error() {
        return CompileEvent.error(8L, 2_000L, "alpha", "Dog.java", 12, "cant.resolve",
                Category.SYMBOL, "cannot find symbol");
    }

    @Test
    void supportsOnlyCompileEvents() {
        TelemetryEvent other = new TelemetryEvent() {
            @Override
            public String getEventType() {
                return "X";
            }

            @Override
            public long getTimestamp() {
                return 0;
            }

            @Override
            public String getTarget() {
                return null;
            }
        };

        assertTrue(mapper.supports(CompileEvent.success(1L, 1_000L, "alpha", "Dog.java")));
        assertFalse(mapper.supports(other));
    }

    @Test
    void successWritesOnlyTheBasicFields() {
        Map<String, Object> out = new LinkedHashMap<>();

        mapper.addFields(CompileEvent.success(7L, 1_000L, "alpha", "Dog.java"), out);

        assertEquals(7L, out.get("attemptId"));
        assertEquals("alpha", out.get("project"));
        assertEquals(true, out.get("success"));
        assertFalse(out.containsKey("errorCode"));
        assertFalse(out.containsKey("category"));
        assertFalse(out.containsKey("line"));
        assertFalse(out.containsKey("message"));
    }

    @Test
    void errorWritesCodeCategoryLineAndMessage() {
        Map<String, Object> out = new LinkedHashMap<>();

        mapper.addFields(error(), out);

        assertEquals(8L, out.get("attemptId"));
        assertEquals(false, out.get("success"));
        assertEquals("cant.resolve", out.get("errorCode"));
        assertEquals("SYMBOL", out.get("category"));
        assertEquals(12, out.get("line"));
        assertEquals("cannot find symbol", out.get("message"));
    }

    @Test
    void errorWithoutACategoryWritesNull() {
        CompileEvent noCategory = new CompileEvent(9L, 3_000L, "alpha", false, "unknown", null,
                "odd message", "Dog.java", 3);
        Map<String, Object> out = new LinkedHashMap<>();

        mapper.addFields(noCategory, out);

        assertTrue(out.containsKey("category"));
        assertNull(out.get("category"));
    }

    @Test
    void projectOfReturnsTheEventsProject() {
        assertEquals("alpha", mapper.projectOf(error()));
    }

    @Test
    void accumulateReportsASuccessfulAttempt() {
        SummaryBuilder builder = new SummaryBuilder();

        mapper.accumulate(CompileEvent.success(1L, 1_000L, "alpha", "Dog.java"), builder);
        SessionSummary summary = builder.build();

        assertEquals(1, summary.totalCompilations());
        assertEquals(1, summary.successfulCompilations());
    }

    @Test
    void accumulateReportsAFailedAttemptWithItsCategory() {
        SummaryBuilder builder = new SummaryBuilder();

        mapper.accumulate(error(), builder);
        SessionSummary summary = builder.build();

        assertEquals(1, summary.failedCompilations());
        assertEquals(1, summary.errorCountsByCategory().get("SYMBOL"));
    }

    @Test
    void accumulateCountsAMissingCategoryAsOther() {
        CompileEvent noCategory = new CompileEvent(9L, 3_000L, "alpha", false, "unknown", null,
                "odd message", "Dog.java", 3);
        SummaryBuilder builder = new SummaryBuilder();

        mapper.accumulate(noCategory, builder);

        assertEquals(1, builder.build().errorCountsByCategory().get("OTHER"));
    }
}
