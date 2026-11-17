package comp3607_project.editor_interactions;

import java.util.function.Predicate;
import java.util.function.Supplier;

import comp3607_project.model.EventSink;

/**
 * Wraps selected InteractionEvents as TelemetryEvents and sends them to the
 * sink.
 */
public final class TelemetryInteractionObserver implements InteractionObserver {
    private final EventSink sink;
    private final Supplier<String> projectSupplier;
    private final Predicate<InteractionEvent> filter;

    public TelemetryInteractionObserver(EventSink sink, Supplier<String> projectSupplier) {
        this(sink, projectSupplier, e -> true);
    }

    public TelemetryInteractionObserver(EventSink sink, Supplier<String> projectSupplier,
            Predicate<InteractionEvent> filter) {
        this.sink = sink;
        this.projectSupplier = projectSupplier;
        this.filter = filter;
    }

    /** Keeps file edits and idle gaps; drops ordinary keystrokes. */
    public static boolean editsAndIdleOnly(InteractionEvent e) {
        return e.kind() == InteractionEvent.Kind.EDIT || e.classifiedAsIdle();
    }

    @Override
    public void onInteraction(InteractionEvent event) {
        if (filter.test(event)) {
            sink.accept(new InteractionTelemetryEvent(event, projectSupplier.get()));
        }
    }
}