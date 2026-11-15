package comp3607_project.compile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import comp3607_project.compile.CompileErrorClassifier.Category;

class CompileEventTest {

    private CompileEvent success(String file) {
        return new CompileEvent(1, 1000, "proj", true, null, null, null, file, -1);
    }

    private CompileEvent error(String file) {
        return new CompileEvent(2, 2000, "proj", false, "expected", Category.SYNTAX,
                "';' expected", file, 19);
    }

    @Test
    void successEventType() {
        assertEquals("COMPILE_SUCCESS", success("Dog.java").getEventType());
    }

    @Test
    void errorEventType() {
        assertEquals("COMPILE_ERROR", error("Dog.java").getEventType());
    }

    @Test
    void targetIsFileWhenPresent() {
        assertEquals("Dog.java", error("Dog.java").getTarget());
    }

    @Test
    void targetFallsBackToProjectWhenFileIsNull() {
        assertEquals("proj", error(null).getTarget());
    }

    @Test
    void timestampIsReturned() {
        assertEquals(2000, error("Dog.java").getTimestamp());
    }

    @Test
    void gettersReturnConstructorValues() {
        CompileEvent e = error("Dog.java");
        assertEquals(2, e.getAttemptId());
        assertEquals("proj", e.getProject());
        assertFalse(e.isSuccess());
        assertEquals("expected", e.getErrorCode());
        assertEquals(Category.SYNTAX, e.getCategory());
        assertEquals("';' expected", e.getMessage());
        assertEquals("Dog.java", e.getFile());
        assertEquals(19, e.getLine());
    }

    @Test
    void successEventHasNoErrorDetails() {
        CompileEvent e = success("Dog.java");
        assertTrue(e.isSuccess());
        assertNull(e.getErrorCode());
        assertNull(e.getCategory());
        assertNull(e.getMessage());
    }

    @Test
    void toStringOfErrorIncludesDetails() {
        String s = error("Dog.java").toString();
        assertTrue(s.contains("COMPILE_ERROR"));
        assertTrue(s.contains("attempt=2"));
        assertTrue(s.contains("code=expected"));
        assertTrue(s.contains("category=SYNTAX"));
        assertTrue(s.contains("line=19"));
    }

    @Test
    void toStringOfSuccessOmitsErrorDetails() {
        String s = success("Dog.java").toString();
        assertTrue(s.contains("COMPILE_SUCCESS"));
        assertFalse(s.contains("code="));
    }
}