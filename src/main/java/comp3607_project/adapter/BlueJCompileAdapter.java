package comp3607_project.adapter;

import java.io.File;

import bluej.extensions2.event.CompileEvent;
import bluej.extensions2.event.CompileListener;
import comp3607_project.compile.CompileTracker;

/** Translates BlueJ compile callbacks into CompileTracker calls. */
public class BlueJCompileAdapter implements CompileListener {
    private final CompileTracker tracker;

    public BlueJCompileAdapter(CompileTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public void compileStarted(CompileEvent ce) {
        forward(ce, "compile start", tracker::compileStarted);
    }

    @Override
    public void compileError(CompileEvent ce) {
        forward(ce, "compile error", () -> tracker.compileError(projectOf(ce), fileOf(ce),
                ce.getErrorLineNumber(), ce.getErrorMessage()));
    }

    @Override
    public void compileWarning(CompileEvent ce) {
        // Warnings are not part of the telemetry scope.
    }

    @Override
    public void compileSucceeded(CompileEvent ce) {
        forward(ce, "compile success", () -> tracker.compileSucceeded(projectOf(ce), fileOf(ce)));
    }

    @Override
    public void compileFailed(CompileEvent ce) {
        forward(ce, "compile failed", () -> tracker.compileFailed(projectOf(ce)));
    }

    /**
     * Runs the action for user-triggered compiles only, and never lets an error
     * reach BlueJ.
     */
    private void forward(CompileEvent ce, String label, Runnable action) {
        if (!ce.isUserGeneratedCompilation()) {
            return;
        }
        try {
            action.run();
        } catch (RuntimeException e) {
            System.out.println("[Telemetry] " + label + " event failed: " + e);
        }
    }

    /**
     * BlueJ's event has no project name, so use the folder holding the source
     * files.
     */
    private String projectOf(CompileEvent ce) {
        File[] files = ce.getFiles();
        if (files == null || files.length == 0 || files[0].getParentFile() == null) {
            return "unknown";
        }
        return files[0].getParentFile().getName();
    }

    private String fileOf(CompileEvent ce) {
        File[] files = ce.getFiles();
        return files == null || files.length == 0 ? null : files[0].getName();
    }
}