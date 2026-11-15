package comp3607_project.compile;

import java.util.Locale;

public class CompileErrorClassifier {

    public enum Category {
        SYNTAX, SYMBOL, TYPE, OTHER
    }

    public record Classification(String code, Category category) {
    }

    public Classification classify(String message) {
        if (message == null || message.isBlank()) {
            return new Classification("unknown", Category.OTHER);
        }
        String m = message.toLowerCase(Locale.ROOT);

        // Symbol problems
        if (m.contains("cannot find symbol") || m.contains("does not exist")) {
            return new Classification("cant.resolve", Category.SYMBOL);
        }
        if (m.contains("already defined")) {
            return new Classification("already.defined", Category.SYMBOL);
        }

        // Type problems
        if (m.contains("incompatible types") || m.contains("possible lossy conversion")) {
            return new Classification("prob.found.req", Category.TYPE);
        }
        if (m.contains("bad operand type")) {
            return new Classification("operator.cant.be.applied", Category.TYPE);
        }
        if (m.contains("cannot be applied")) {
            return new Classification("cant.apply.symbol", Category.TYPE);
        }
        if (m.contains("missing return statement")) {
            return new Classification("missing.ret.stmt", Category.TYPE);
        }
        if (m.contains("might not have been initialized")) {
            return new Classification("var.might.not.have.been.initialized", Category.TYPE);
        }
        if (m.contains("unreported exception")) {
            return new Classification("unreported.exception.need.to.catch.or.throw", Category.TYPE);
        }
        if (m.contains("cannot be dereferenced")) {
            return new Classification("cant.deref", Category.TYPE);
        }

        // Syntax problems
        if (m.contains("reached end of file while parsing")) {
            return new Classification("premature.eof", Category.SYNTAX);
        }
        if (m.contains("illegal start of")) {
            return new Classification("illegal.start.of.expr", Category.SYNTAX);
        }
        if (m.contains("not a statement")) {
            return new Classification("not.stmt", Category.SYNTAX);
        }
        if (m.contains("unclosed string literal")) {
            return new Classification("unclosed.str.lit", Category.SYNTAX);
        }
        if (m.contains("without if") || m.contains("class, interface, enum, or record expected")) {
            return new Classification("expected4", Category.SYNTAX);
        }
        if (m.contains("expected")) {
            return new Classification("expected", Category.SYNTAX);
        }

        return new Classification("unknown", Category.OTHER);
    }
}