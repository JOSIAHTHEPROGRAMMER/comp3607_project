package comp3607_project;

import bluej.extensions2.BlueJ;
import bluej.extensions2.Extension;
import comp3607_project.adapter.BlueJLifecycleAdapter;
import comp3607_project.lifecycle.LifecycleTracker;
import comp3607_project.model.ConsoleSink;
import comp3607_project.model.EventSink;
import java.net.URI;
import java.net.URL;

public class HelloExtension extends Extension {

    @Override
    public void startup(BlueJ bluej) {
        EventSink sink = new ConsoleSink();
        LifecycleTracker tracker = new LifecycleTracker(sink, System::currentTimeMillis);
        BlueJLifecycleAdapter adapter = new BlueJLifecycleAdapter(tracker);
        bluej.addPackageListener(adapter);
        bluej.addClassListener(adapter);
        System.out.println("[Telemetry] extension started");
    }

    @Override
    public boolean isCompatible() {
        return getExtensionsAPIVersionMajor() >= 3;
    }

    @Override
    public String getVersion() {
        return "0.2";
    }

    @Override
    public String getName() {
        return "Telemetry Hello";
    }

    @Override
    public String getDescription() {
        return "Logs project and class lifecycle events";
    }

    @Override
    public URL getURL() {
        try {
            return URI.create("https://github.com/Reon-new/comp3607_project").toURL();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void terminate() {
        System.out.println("[Telemetry] extension terminated");
    }
}