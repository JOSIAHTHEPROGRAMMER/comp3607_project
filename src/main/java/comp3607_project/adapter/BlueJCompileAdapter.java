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
        if (!ce.isUserGeneratedCompilation()) {
            return;
        }
        tracker.compileStarted();
    }

    @Override
    public void compileError(CompileEvent ce) {
        if (!ce.isUserGeneratedCompilation()) {
            return;
        }
        try {
            tracker.compileError(projectOf(ce), fileOf(ce), ce.getErrorLineNumber(),
                    ce.getErrorMessage());
        } catch (RuntimeException e) {
            System.out.println("[Telemetry] compile error event failed: " + e);
        }
    }

    @Override
    public void compileWarning(CompileEvent ce) {
        // Warnings are not part of the telemetry scope.
    }

    @Override
    public void compileSucceeded(CompileEvent ce) {
        if (!ce.isUserGeneratedCompilation()) {
            return;
        }
        try {
            tracker.compileSucceeded(projectOf(ce), fileOf(ce));
        } catch (RuntimeException e) {
            System.out.println("[Telemetry] compile success event failed: " + e);
        }
    }

    @Override
    public void compileFailed(CompileEvent ce) {
        if (!ce.isUserGeneratedCompilation()) {
            return;
        }
        try {
            tracker.compileFailed(projectOf(ce));
        } catch (RuntimeException e) {
            System.out.println("[Telemetry] compile failed event failed: " + e);
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
        if (files == null || files.length == 0) {
            return null;
        }
        return files[0].getName();
    }
}