package comp3607_project.compile;

import java.util.List;
import java.util.Locale;

/**
 * Maps a javac error message to a javac-style code and a coarse category.
 * Rules are checked in order, so more specific rules must come first.
 */
public class CompileErrorClassifier {

    public enum Category {
        SYNTAX, SYMBOL, TYPE, OTHER
    }

    public record Classification(String code, Category category) {
    }

    private static final Classification UNKNOWN = new Classification("unknown", Category.OTHER);

    private static final List<ClassificationRule> RULES = List.of(
            new ClassificationRule("cant.resolve", Category.SYMBOL, "cannot find symbol", "does not exist"),
            new ClassificationRule("already.defined", Category.SYMBOL, "already defined"),
            new ClassificationRule("prob.found.req", Category.TYPE, "incompatible types", "possible lossy conversion"),
            new ClassificationRule("operator.cant.be.applied", Category.TYPE, "bad operand type"),
            new ClassificationRule("cant.apply.symbol", Category.TYPE, "cannot be applied"),
            new ClassificationRule("missing.ret.stmt", Category.TYPE, "missing return statement"),
            new ClassificationRule("var.might.not.have.been.initialized", Category.TYPE,
                    "might not have been initialized"),
            new ClassificationRule("unreported.exception.need.to.catch.or.throw", Category.TYPE,
                    "unreported exception"),
            new ClassificationRule("cant.deref", Category.TYPE, "cannot be dereferenced"),
            new ClassificationRule("premature.eof", Category.SYNTAX, "reached end of file while parsing"),
            new ClassificationRule("illegal.start.of.expr", Category.SYNTAX, "illegal start of"),
            new ClassificationRule("not.stmt", Category.SYNTAX, "not a statement"),
            new ClassificationRule("unclosed.str.lit", Category.SYNTAX, "unclosed string literal"),
            new ClassificationRule("expected4", Category.SYNTAX, "without if",
                    "class, interface, enum, or record expected"),
            new ClassificationRule("expected", Category.SYNTAX, "expected"));

    public Classification classify(String message) {
        if (message == null || message.isBlank()) {
            return UNKNOWN;
        }
        String m = message.toLowerCase(Locale.ROOT);
        for (ClassificationRule rule : RULES) {
            if (rule.matches(m)) {
                return rule.toClassification();
            }
        }
        return UNKNOWN;
    }
}