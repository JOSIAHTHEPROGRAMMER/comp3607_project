package comp3607_project.editor_interactions;

public final class ConsoleLoggingObserver implements InteractionObserver {

    @Override
    public void onInteraction(InteractionEvent event) {
        System.out.println(event);
    }
}