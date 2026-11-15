package comp3607_project.compile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import comp3607_project.compile.CompileErrorClassifier.Category;
import comp3607_project.compile.CompileErrorClassifier.Classification;

class CompileErrorClassifierTest {
    private final CompileErrorClassifier classifier = new CompileErrorClassifier();

    private void assertClassified(String message, String code, Category category) {
        Classification c = classifier.classify(message);
        assertEquals(code, c.code());
        assertEquals(category, c.category());
    }

    @Test
    void missingSemicolonIsSyntax() {
        assertClassified("';' expected", "expected", Category.SYNTAX);
    }

    @Test
    void undefinedVariableIsSymbol() {
        assertClassified("cannot find symbol\n  symbol:   variable missing\n  location: class test",
                "cant.resolve", Category.SYMBOL);
    }

    @Test
    void wrongTypeIsType() {
        assertClassified("incompatible types: java.lang.String cannot be converted to int",
                "prob.found.req", Category.TYPE);
    }

    @Test
    void missingReturnIsType() {
        assertClassified("missing return statement", "missing.ret.stmt", Category.TYPE);
    }

    @Test
    void endOfFileIsSyntax() {
        assertClassified("reached end of file while parsing", "premature.eof", Category.SYNTAX);
    }

    @Test
    void alreadyDefinedIsSymbol() {
        assertClassified("variable x is already defined in method foo()",
                "already.defined", Category.SYMBOL);
    }

    @Test
    void matchingIsCaseInsensitive() {
        assertClassified("CANNOT FIND SYMBOL", "cant.resolve", Category.SYMBOL);
    }

    @Test
    void unrecognisedMessageIsUnknown() {
        assertClassified("something javac never says", "unknown", Category.OTHER);
    }

    @Test
    void nullAndBlankAreUnknown() {
        assertClassified(null, "unknown", Category.OTHER);
        assertClassified("   ", "unknown", Category.OTHER);
    }
}