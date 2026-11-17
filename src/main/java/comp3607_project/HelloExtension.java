package comp3607_project;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

import bluej.extensions2.BlueJ;
import bluej.extensions2.Extension;
import comp3607_project.adapter.BlueJCompileAdapter;
import comp3607_project.adapter.BlueJExecutionAdapter;
import comp3607_project.adapter.BlueJLifecycleAdapter;
import comp3607_project.adapter.BlueJProjectDirAdapter;
import comp3607_project.compile.CompileTracker;
import comp3607_project.execution.ExecutionTracker;
import comp3607_project.json.EventCollector;
import comp3607_project.json.ProjectDirectoryTracker;
import comp3607_project.json.SessionExporter;
import comp3607_project.json.TelemetryJsonSerialiser;
import comp3607_project.lifecycle.LifecycleTracker;
import comp3607_project.model.CompositeSink;
import comp3607_project.model.ConsoleSink;
import comp3607_project.model.EventSink;

public class HelloExtension extends Extension {
    private final ProjectDirectoryTracker dirTracker = new ProjectDirectoryTracker();
    private SessionExporter exporter;

    @Override
    public void startup(BlueJ bluej) {
        EventCollector collector = new EventCollector();
        EventSink sink = new CompositeSink(new ConsoleSink(), collector);
        long sessionStart = System.currentTimeMillis();
        exporter = new SessionExporter(collector, new TelemetryJsonSerialiser(),
                "STUDENT_ID_PLACEHOLDER", sessionStart, System::currentTimeMillis);

        LifecycleTracker lifecycleTracker = new LifecycleTracker(sink, System::currentTimeMillis);
        BlueJLifecycleAdapter lifecycleAdapter = new BlueJLifecycleAdapter(lifecycleTracker);
        bluej.addPackageListener(lifecycleAdapter);
        bluej.addClassListener(lifecycleAdapter);

        ExecutionTracker executionTracker = new ExecutionTracker(sink, System::currentTimeMillis);
        bluej.addInvocationFinishedListener(new BlueJExecutionAdapter(executionTracker));
        CompileTracker compileTracker = new CompileTracker(sink, System::currentTimeMillis);
        bluej.addCompileListener(new BlueJCompileAdapter(compileTracker));

        bluej.addPackageListener(new BlueJProjectDirAdapter(dirTracker, this::exportSession));
        System.out.println("[Telemetry] extension started");
    }

    @Override
    public boolean isCompatible() {
        return getExtensionsAPIVersionMajor() >= 3;
    }

    @Override
    public String getVersion() {
        return "0.3";
    }

    @Override
    public String getName() {
        return "Telemetry Hello";
    }

    @Override
    public String getDescription() {
        return "Logs project, class lifecycle and execution events";
    }

    @Override
    public URL getURL() {
        try {
            return URI.create("https://github.com/Reon-new/comp3607_project").toURL();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    @Override
    public void terminate() {
        exportSession(null);
        System.out.println("[Telemetry] extension terminated");
    }

    private void exportSession(Path dir) {
        if (exporter == null) {
            return;
        }
        Path target = dir != null ? dir
                : dirTracker.getLastKnownDir().orElse(Path.of(System.getProperty("user.home")));
        try {
            exporter.exportIfNew(target)
                    .ifPresent(p -> System.out.println("[Telemetry] wrote " + p));
        } catch (IOException | RuntimeException e) {
            System.out.println("[Telemetry] export failed: " + e);
        }
    }
}
