package comp3607_project;

import bluej.extensions2.BlueJ;
import bluej.extensions2.Extension;
import bluej.extensions2.event.ClassListener;
import bluej.extensions2.event.CompileListener;
import bluej.extensions2.event.InvocationFinishedListener;
import bluej.extensions2.event.PackageListener;
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
import comp3607_project.pdf_exporter.TelemetryReportFacade;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class HelloExtension extends Extension {
    private static final String STUDENT_ID = "STUDENT_ID_PLACEHOLDER";

    private final ProjectDirectoryTracker dirTracker = new ProjectDirectoryTracker();
    private EventCollector collector;
    private SessionExporter exporter;
    private EditorTelemetry editor;
    private TelemetryReportFacade reportFacade;
    private long sessionStartedAtMillis;

    @Override
    public void startup(BlueJ bluej) {
        debugLog("startup begin; java=" + System.getProperty("java.version")
                + " home=" + System.getProperty("user.home"));
        this.sessionStartedAtMillis = System.currentTimeMillis();
        this.reportFacade = new TelemetryReportFacade(this.sessionStartedAtMillis, new ConsoleSink());
        this.collector = new EventCollector();
        CompositeSink sink = new CompositeSink(this.reportFacade.sinkForTrackers(), this.collector);
        this.exporter = new SessionExporter(this.collector, new TelemetryJsonSerialiser(),
                STUDENT_ID, this.sessionStartedAtMillis, System::currentTimeMillis);

        LifecycleTracker lifecycleTracker = new LifecycleTracker(sink, System::currentTimeMillis);
        BlueJLifecycleAdapter lifecycleAdapter = new BlueJLifecycleAdapter(lifecycleTracker);
        bluej.addPackageListener((PackageListener) lifecycleAdapter);
        bluej.addClassListener((ClassListener) lifecycleAdapter);

        ExecutionTracker executionTracker = new ExecutionTracker(sink, System::currentTimeMillis);
        bluej.addInvocationFinishedListener((InvocationFinishedListener) new BlueJExecutionAdapter(executionTracker));

        CompileTracker compileTracker = new CompileTracker(sink, System::currentTimeMillis);
        bluej.addCompileListener((CompileListener) new BlueJCompileAdapter(compileTracker));

        this.editor = new EditorTelemetry(sink, () -> this.dirTracker.getLastKnownDir()
                .map(p -> p.getFileName() == null ? null : p.getFileName().toString())
                .orElse(null));
        this.editor.start();

        bluej.addPackageListener((PackageListener) new BlueJProjectDirAdapter(
                this.dirTracker,
                this.editor::projectOpened,
                dir -> {
                    // PDF first, so a JSON failure can never prevent it.
                    this.exportSummaryPdf(dir);
                    this.exportSession(dir);
                    this.editor.projectClosed(dir);
                }));

        new Thread(() -> {
            try {
                PDType1Font.HELVETICA.getName();
            } catch (Throwable ignored) {
                // warm-up only
            }
        }, "telemetry-font-warmup").start();

        debugLog("startup complete (extension v" + getVersion() + ")");
        System.out.println("[Telemetry] extension started");
    }

    /** Builds the PDF from the same collected events (and SessionSummary) the JSON uses. */
    private void exportSummaryPdf(Path projectDir) {
        debugLog("summary PDF export starting");
        try {
            Path outputDir = projectDir != null ? projectDir
                    : this.dirTracker.getLastKnownDir().orElse(Path.of(System.getProperty("user.home")));
            Path out = outputDir.resolve("session-" + this.sessionStartedAtMillis + ".pdf");
            this.reportFacade.export(out.toFile());
            debugLog("summary PDF written: " + out.toAbsolutePath());
            System.out.println("[Telemetry] session report written to " + out.toAbsolutePath());
        } catch (Throwable t) {
            debugLog("summary PDF FAILED: " + t);
            System.err.println("[Telemetry] PDF export failed: " + t);
            t.printStackTrace();
        }
    }

    private static void debugLog(String msg) {
        try {
            Path d = Path.of(System.getProperty("user.home"), "BlueJ", "telemetry-reports");
            Files.createDirectories(d);
            Files.writeString(d.resolve("telemetry-debug.log"),
                    Instant.now() + " " + msg + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Throwable ignored) {
            // logging must never break the extension
        }
    }

    @Override
    public boolean isCompatible() {
        return getExtensionsAPIVersionMajor() >= 3;
    }

    @Override
    public String getVersion() {
        return "0.5";
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

    @Override
    public void terminate() {
        debugLog("terminate called");
        if (this.reportFacade == null) {
            return;
        }
        try {
            if (this.editor != null) {
                this.editor.close();
            }
        } catch (Throwable t) {
            debugLog("editor close failed: " + t);
        }
        try {
            this.reportFacade.endSession(System.currentTimeMillis());
        } catch (Throwable t) {
            debugLog("endSession failed: " + t);
        }
        this.exportSummaryPdf(null);
        this.exportSession(null);
        System.out.println("[Telemetry] extension terminated");
    }

    private void exportSession(Path dir) {
        if (this.exporter == null) {
            return;
        }
        Path target = dir != null ? dir
                : this.dirTracker.getLastKnownDir().orElse(Path.of(System.getProperty("user.home")));
        try {
            this.exporter.exportIfNew(target)
                    .ifPresent(p -> System.out.println("[Telemetry] wrote " + p));
        } catch (IOException | RuntimeException e) {
            debugLog("JSON export failed: " + e);
            System.out.println("[Telemetry] export failed: " + e);
        } catch (Throwable t) {
            // Errors (e.g. NoClassDefFoundError) must not escape into BlueJ's listener chain.
            debugLog("JSON export ERROR: " + t);
        }
    }
}