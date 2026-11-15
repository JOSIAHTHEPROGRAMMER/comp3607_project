package comp3607_project.adapter;

import bluej.extensions2.BClass;
import bluej.extensions2.BPackage;
import bluej.extensions2.ExtensionException;
import bluej.extensions2.event.ClassEvent;
import bluej.extensions2.event.ClassListener;
import bluej.extensions2.event.PackageEvent;
import bluej.extensions2.event.PackageListener;
import comp3607_project.lifecycle.LifecycleTracker;
import java.util.ArrayList;
import java.util.List;

public class BlueJLifecycleAdapter implements PackageListener, ClassListener {
    private final LifecycleTracker tracker;

    public BlueJLifecycleAdapter(LifecycleTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public void packageOpened(PackageEvent ev) {
        try {
            BPackage pkg = ev.getPackage();
            String project = pkg.getProject().getName();
            List<String> names = new ArrayList<>();
            for (BClass c : pkg.getClasses()) {
                names.add(c.getName());
            }
            tracker.projectOpened(project, names);
        } catch (ExtensionException e) {
            System.out.println("[Telemetry] could not read opened project: " + e.getMessage());
        }
    }

    @Override
    public void packageClosing(PackageEvent ev) {
        try {
            tracker.projectClosed(ev.getPackage().getProject().getName());
        } catch (ExtensionException e) {
            System.out.println("[Telemetry] could not read closing project: " + e.getMessage());
        }
    }

    @Override
    public void classStateChanged(ClassEvent ev) {
        try {
            tracker.classStateChanged(projectOf(ev), ev.getBClass().getName(), ev.isClassCompiled(), ev.hasError());
        } catch (ExtensionException e) {
            System.out.println("[Telemetry] class state event failed: " + e.getMessage());
        }
    }

    @Override
    public void classNameChanged(ClassEvent ev) {
        try {
            tracker.classRenamed(projectOf(ev), ev.getOldName(), ev.getBClass().getName());
        } catch (ExtensionException e) {
            System.out.println("[Telemetry] class rename event failed: " + e.getMessage());
        }
    }

    @Override
    public void classRemoved(ClassEvent ev) {
        try {
            tracker.classRemoved(projectOf(ev), ev.getBClass().getName());
        } catch (ExtensionException e) {
            System.out.println("[Telemetry] class removal event failed: " + e.getMessage());
        }
    }

    private String projectOf(ClassEvent ev) throws ExtensionException {
        return ev.getPackage().getProject().getName();
    }
}