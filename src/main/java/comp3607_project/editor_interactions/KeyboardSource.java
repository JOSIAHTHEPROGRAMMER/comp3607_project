package comp3607_project.editor_interactions;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;


public final class KeyboardSource implements InteractionSource {

    private final InteractionMonitor monitor;
    private final IdleClassifier classifier;

    private final AtomicLong lastKeyTime = new AtomicLong(0);
    private final AtomicBoolean started = new AtomicBoolean(false);

    private AWTEventListener listener;

    public KeyboardSource(InteractionMonitor monitor, IdleClassifier classifier) {
        this.monitor = monitor;
        this.classifier = classifier;
    }

    @Override
    public void start() {
        if (!started.compareAndSet(false, true)) return; // idempotent

        listener = new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent event) {
                if (event instanceof KeyEvent
                        && event.getID() == KeyEvent.KEY_PRESSED) {
                    handleKey((KeyEvent) event);
                }
            }
        };
        Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.KEY_EVENT_MASK);
    }

    private void handleKey(KeyEvent e) {
        long now = System.currentTimeMillis();
        long previous = lastKeyTime.getAndSet(now);

        long gap = (previous == 0) ? 0 : now - previous;
        boolean idle = previous != 0 && classifier.isIdle(gap);

        monitor.publish(new InteractionEvent(
                InteractionEvent.Kind.KEY,
                Instant.ofEpochMilli(now),
                gap,
                KeyEvent.getKeyText(e.getKeyCode()),
                idle));
    }

    @Override
    public void close() {
        if (listener != null && started.compareAndSet(true, false)) {
            Toolkit.getDefaultToolkit().removeAWTEventListener(listener);
            listener = null;
        }
    }
}