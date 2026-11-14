package comp3607_project;

import bluej.extensions2.BlueJ;
import bluej.extensions2.Extension;
import bluej.extensions2.ExtensionException;
import bluej.extensions2.event.PackageEvent;
import bluej.extensions2.event.PackageListener;
import java.net.URI;
import java.net.URL;

public class HelloExtension extends Extension implements PackageListener {

    @Override
    public void startup(BlueJ bluej) {
        bluej.addPackageListener(this);
        System.out.println("[Telemetry] extension started");
    }

    @Override
    public void packageOpened(PackageEvent ev) {
        try {
            System.out.println("[Telemetry] project opened: " + ev.getPackage().getProject().getName());
        } catch (ExtensionException e) {
            System.out.println("[Telemetry] project opened (name unavailable)");
        }
    }

    @Override
    public void packageClosing(PackageEvent ev) {
        try {
            System.out.println("[Telemetry] project closing: " + ev.getPackage().getProject().getName());
        } catch (ExtensionException e) {
            System.out.println("[Telemetry] project closing (name unavailable)");
        }
    }

    @Override
    public boolean isCompatible() {
        return getExtensionsAPIVersionMajor() >= 3;
    }

    @Override
    public String getVersion() {
        return "0.1";
    }

    @Override
    public String getName() {
        return "Telemetry Hello";
    }

    @Override
    public String getDescription() {
        return "Logs project open and close events";
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