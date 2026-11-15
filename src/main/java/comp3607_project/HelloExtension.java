package comp3607_project;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import bluej.extensions2.BlueJ;
import bluej.extensions2.Extension;
import comp3607_project.adapter.BlueJCompileAdapter;
import comp3607_project.adapter.BlueJExecutionAdapter;
import comp3607_project.adapter.BlueJLifecycleAdapter;
import comp3607_project.compile.CompileTracker;
import comp3607_project.execution.ExecutionTracker;
import comp3607_project.lifecycle.LifecycleTracker;
import comp3607_project.model.ConsoleSink;
import comp3607_project.model.EventSink;

public class HelloExtension extends Extension {

    @Override
    public void startup(BlueJ bluej) {
        EventSink sink = new ConsoleSink();

        LifecycleTracker lifecycleTracker = new LifecycleTracker(sink, System::currentTimeMillis);
        BlueJLifecycleAdapter lifecycleAdapter = new BlueJLifecycleAdapter(lifecycleTracker);
        bluej.addPackageListener(lifecycleAdapter);
        bluej.addClassListener(lifecycleAdapter);

        ExecutionTracker executionTracker = new ExecutionTracker(sink, System::currentTimeMillis);
        bluej.addInvocationFinishedListener(new BlueJExecutionAdapter(executionTracker));
        CompileTracker compileTracker = new CompileTracker(sink, System::currentTimeMillis);
        bluej.addCompileListener(new BlueJCompileAdapter(compileTracker));
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
        System.out.println("[Telemetry] extension terminated");
    }
}
