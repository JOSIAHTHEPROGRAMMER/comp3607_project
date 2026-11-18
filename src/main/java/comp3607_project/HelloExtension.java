package comp3607_project;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import bluej.extensions2.BlueJ;
import bluej.extensions2.Extension;
import comp3607_project.adapter.BlueJCompileAdapter;
import comp3607_project.adapter.BlueJExecutionAdapter;
import comp3607_project.adapter.BlueJLifecycleAdapter;
import comp3607_project.adapter.BlueJProjectDirAdapter;
import comp3607_project.compile.CompileTracker;
import comp3607_project.editor_interactions.EditorTelemetry;
import comp3607_project.execution.ExecutionTracker;
import comp3607_project.json.EventCollector;
import comp3607_project.json.ProjectDirectoryTracker;
import comp3607_project.json.SessionExporter;
import comp3607_project.json.TelemetryJsonSerialiser;
import comp3607_project.lifecycle.LifecycleTracker;
import comp3607_project.model.CompositeSink;
import comp3607_project.model.ConsoleSink;
import comp3607_project.model.EventSink;
import comp3607_project.pdf_exporter.TelemetryReportFacade;

public class HelloExtension extends Extension {
    private final ProjectDirectoryTracker dirTracker = new ProjectDirectoryTracker();
    private SessionExporter exporter;
    private EditorTelemetry editor;

    private TelemetryReportFacade reportFacade;
    private long sessionStartedAtMillis;

    @Override
    public void startup(BlueJ bluej) {

        sessionStartedAtMillis = System.currentTimeMillis();
        reportFacade = new TelemetryReportFacade(sessionStartedAtMillis, new ConsoleSink());
        EventCollector collector = new EventCollector();
        EventSink sink = new CompositeSink(reportFacade.sinkForTrackers(), collector);
        exporter = new SessionExporter(collector, new TelemetryJsonSerialiser(),
                "STUDENT_ID_PLACEHOLDER", sessionStartedAtMillis, System::currentTimeMillis);

        LifecycleTracker lifecycleTracker = new LifecycleTracker(sink, System::currentTimeMillis);
        BlueJLifecycleAdapter lifecycleAdapter = new BlueJLifecycleAdapter(lifecycleTracker);
        bluej.addPackageListener(lifecycleAdapter);
        bluej.addClassListener(lifecycleAdapter);

        ExecutionTracker executionTracker = new ExecutionTracker(sink, System::currentTimeMillis);
        bluej.addInvocationFinishedListener(new BlueJExecutionAdapter(executionTracker));
        CompileTracker compileTracker = new CompileTracker(sink, System::currentTimeMillis);
        bluej.addCompileListener(new BlueJCompileAdapter(compileTracker));

        editor = new EditorTelemetry(sink, () -> dirTracker.getLastKnownDir()
                .map(p -> p.getFileName() == null ? null : p.getFileName().toString())
                .orElse(null), reportFacade.interactionObserver());

        editor.start();

        bluej.addPackageListener(new BlueJProjectDirAdapter(dirTracker,
                editor::projectOpened,
                dir -> {
                    exportSession(dir);
                    exportPdf(dir);
                    editor.projectClosed(dir);
                }));
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
        return "Records compile, execution and lifecycle telemetry and exports a PDF report when the extension terminates.";
    }

    @Override
    public URL getURL() {
        try {
            return URI.create("https://github.com/Reon-new/comp3607_project").toURL();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private void exportPdf(Path dir) {
        if (reportFacade == null) {
            return;
        }
        try {
            reportFacade.endSession(System.currentTimeMillis());
            Path targetDir = dir != null ? dir
                    : dirTracker.getLastKnownDir().orElse(
                            Path.of(System.getProperty("user.home"), "BlueJ", "telemetry-reports"));
            Files.createDirectories(targetDir);
            File report = targetDir.resolve("session-" + sessionStartedAtMillis + ".pdf").toFile();
            reportFacade.export(report);
            System.out.println("[Telemetry] session report written to " + report.getAbsolutePath());
        } catch (IOException | RuntimeException t) {
            System.out.println("[Telemetry] PDF export failed: " + t);
            try {
                Files.writeString(Path.of(System.getProperty("user.home"), "telemetry-pdf-error.txt"),
                        t + System.lineSeparator(), java.nio.file.StandardOpenOption.CREATE,
                        java.nio.file.StandardOpenOption.APPEND);
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public void terminate() {
        if (editor != null) {
            editor.close();
        }
        exportSession(null);
        exportPdf(null);
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
