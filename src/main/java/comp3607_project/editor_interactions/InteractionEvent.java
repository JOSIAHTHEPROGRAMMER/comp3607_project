package comp3607_project.editor_interactions;

import java.time.Instant;


public final class InteractionEvent {

    public enum Kind { KEY, EDIT }

    private final Kind kind;
    private final Instant timestamp;
    private final long gapSincePreviousMs;
    private final String detail;
    private final boolean classifiedAsIdle;

    public InteractionEvent(Kind kind,
                            Instant timestamp,
                            long gapSincePreviousMs,
                            String detail,
                            boolean classifiedAsIdle) {
        this.kind = kind;
        this.timestamp = timestamp;
        this.gapSincePreviousMs = gapSincePreviousMs;
        this.detail = detail;
        this.classifiedAsIdle = classifiedAsIdle;
    }

    public Kind kind()                 { return kind; }
    public Instant timestamp()         { return timestamp; }
    public long gapSincePreviousMs()   { return gapSincePreviousMs; }
    public String detail()             { return detail; }
    public boolean classifiedAsIdle()  { return classifiedAsIdle; }

    @Override
    public String toString() {
        String tag = classifiedAsIdle ? "IDLE" : kind.name();
        return "[" + tag + "] gap=" + gapSincePreviousMs + "ms detail=" + detail;
    }
}