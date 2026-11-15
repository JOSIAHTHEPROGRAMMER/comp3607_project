package comp3607_project.compile;

import comp3607_project.model.TelemetryEvent;

/**
 * One row per compiler error, or one row for a successful attempt.
 * attemptId groups the rows that belong to the same compile.
 */
public final class CompileEvent implements TelemetryEvent {
    private final long attemptId;
    private final long timestamp;
    private final String project;
    private final boolean success;
    private final String errorCode;
    private final CompileErrorClassifier.Category category;
    private final String message;
    private final String file;
    private final int line;

    public CompileEvent(long attemptId, long timestamp, String project, boolean success,
            String errorCode, CompileErrorClassifier.Category category,
            String message, String file, int line) {
        this.attemptId = attemptId;
        this.timestamp = timestamp;
        this.project = project;
        this.success = success;
        this.errorCode = errorCode;
        this.category = category;
        this.message = message;
        this.file = file;
        this.line = line;
    }

    public long getAttemptId() {
        return attemptId;
    }

    public String getProject() {
        return project;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public CompileErrorClassifier.Category getCategory() {
        return category;
    }

    public String getMessage() {
        return message;
    }

    public String getFile() {
        return file;
    }

    public int getLine() {
        return line;
    }

    @Override
    public String getEventType() {
        return success ? "COMPILE_SUCCESS" : "COMPILE_ERROR";
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getTarget() {
        return file != null ? file : project;
    }

    @Override
    public String toString() {
        return getEventType() + " attempt=" + attemptId
                + " target=" + getTarget()
                + " project=" + project
                + (success ? ""
                        : " code=" + errorCode + " category=" + category
                                + " line=" + line + " message=\"" + message.replaceAll("\\s+", " "))
                + " at=" + timestamp;
    }
}