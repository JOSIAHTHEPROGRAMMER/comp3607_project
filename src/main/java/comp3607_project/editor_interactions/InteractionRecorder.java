package comp3607_project.editor_interactions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import bluej.extensions2.BProject;
import bluej.extensions2.BlueJ;
import bluej.extensions2.Extension;
import bluej.extensions2.ProjectNotOpenException;

public class InteractionRecorder extends Extension {

    private static final long IDLE_THRESHOLD_MS = 5_000;

    private InteractionMonitor monitor;
    private final List<InteractionSource> sources = new ArrayList<>();

    @Override
    public void startup(BlueJ bluej) {
        monitor = new InteractionMonitor();
        monitor.addObserver(new ConsoleLoggingObserver());

        IdleClassifier classifier = new ThresholdIdleClassifier(IDLE_THRESHOLD_MS);

        KeyboardSource keyboard = new KeyboardSource(monitor, classifier);
        sources.add(keyboard);
        keyboard.start();

        File projectDir = resolveProjectDir(bluej);
        FileWatchSource fileWatcher = new FileWatchSource(projectDir.toPath(), monitor);
        sources.add(fileWatcher);
        fileWatcher.start();

        System.out.println("InteractionRecorder started on: " + projectDir.getAbsolutePath());
    }

    private File resolveProjectDir(BlueJ bluej) {
        try {
            BProject[] projects = bluej.getOpenProjects();
            if (projects.length > 0)
                return projects[0].getDir();
        } catch (ProjectNotOpenException ex) {
            System.err.println("InteractionRecorder: couldn't read project dir: " + ex);
        }
        return new File(System.getProperty("user.dir"));
    }

    @Override
    public void terminate() {

        for (int i = sources.size() - 1; i >= 0; i--) {
            try {
                sources.get(i).close();
            } catch (Exception ignored) {
            }
        }
        sources.clear();
        System.out.println("InteractionRecorder stopped.");
    }

    @Override
    public boolean isCompatible() {
        return true;
    }

    @Override
    public String getName() {
        return "Interaction Recorder";
    }

    @Override
    public String getDescription() {
        return "Records typing cadence, idle time, and edit frequency.";
    }

    @Override
    public String getVersion() {
        return "2.0";
    }
}
