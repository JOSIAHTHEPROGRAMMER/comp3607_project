package comp3607_project.editor_interactions;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public final class InteractionMonitor {

    private final List<InteractionObserver> observers = new CopyOnWriteArrayList<>();

    public void addObserver(InteractionObserver observer) {
        if (observer != null) observers.add(observer);
    }

    public void removeObserver(InteractionObserver observer) {
        observers.remove(observer);
    }

    public void publish(InteractionEvent event) {
        for (InteractionObserver observer : observers) {
            try {
                observer.onInteraction(event);
            } catch (RuntimeException ex) {
                // Never let one bad observer break the rest.
                System.err.println("Observer failed: " + ex);
            }
        }
    }
}