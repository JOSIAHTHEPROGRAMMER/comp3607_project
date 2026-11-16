package comp3607_project.editor_interactions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.AWTEvent;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests KeyboardSource without BlueJ. Real key presses cannot be produced in a
 * unit test, so a KeyEvent is handed straight to the source's private
 * handleKey method using reflection.
 */
class KeyboardSourceTest {

    private final List<InteractionEvent> received = new ArrayList<>();
    private final InteractionMonitor monitor = new InteractionMonitor();

    KeyboardSourceTest() {
        monitor.addObserver(received::add);
    }

    private static int keyListenerCount() {
        return Toolkit.getDefaultToolkit().getAWTEventListeners(AWTEvent.KEY_EVENT_MASK).length;
    }

    private static void pressKey(KeyboardSource source, int keyCode) throws Exception {
        KeyEvent keyEvent = new KeyEvent(new Container(), KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(), 0, keyCode, KeyEvent.CHAR_UNDEFINED);
        Method handleKey = KeyboardSource.class.getDeclaredMethod("handleKey", KeyEvent.class);
        handleKey.setAccessible(true);
        handleKey.invoke(source, keyEvent);
    }

    @Test
    void startRegistersOneListenerEvenWhenCalledTwice() {
        int before = keyListenerCount();
        KeyboardSource source = new KeyboardSource(monitor, gap -> false);
        try {
            source.start();
            source.start();

            assertEquals(before + 1, keyListenerCount());
        } finally {
            source.close();
        }
    }

    @Test
    void closeRemovesTheListener() {
        int before = keyListenerCount();
        KeyboardSource source = new KeyboardSource(monitor, gap -> false);
        source.start();

        source.close();

        assertEquals(before, keyListenerCount());
    }

    @Test
    void closeWithoutStartIsHarmless() {
        KeyboardSource source = new KeyboardSource(monitor, gap -> false);

        assertDoesNotThrow(source::close);
    }

    @Test
    void closeTwiceIsHarmless() {
        KeyboardSource source = new KeyboardSource(monitor, gap -> false);
        source.start();
        source.close();

        assertDoesNotThrow(source::close);
    }

    @Test
    void firstKeyHasZeroGapAndIsNeverIdle() throws Exception {
        // Even a classifier that says "always idle" must not flag the first key.
        KeyboardSource source = new KeyboardSource(monitor, gap -> true);

        pressKey(source, KeyEvent.VK_A);

        assertEquals(1, received.size());
        InteractionEvent first = received.get(0);
        assertEquals(0, first.gapSincePreviousMs());
        assertFalse(first.classifiedAsIdle());
    }

    @Test
    void laterKeyIsIdleWhenTheClassifierSaysSo() throws Exception {
        KeyboardSource source = new KeyboardSource(monitor, gap -> true);

        pressKey(source, KeyEvent.VK_A);
        pressKey(source, KeyEvent.VK_B);

        assertEquals(2, received.size());
        assertTrue(received.get(1).classifiedAsIdle());
        assertTrue(received.get(1).gapSincePreviousMs() >= 0);
    }

    @Test
    void laterKeyIsNotIdleWhenTheClassifierSaysNo() throws Exception {
        KeyboardSource source = new KeyboardSource(monitor, gap -> false);

        pressKey(source, KeyEvent.VK_A);
        pressKey(source, KeyEvent.VK_B);

        assertFalse(received.get(1).classifiedAsIdle());
    }

    @Test
    void eventCarriesTheKeyKindTextAndTimestamp() throws Exception {
        KeyboardSource source = new KeyboardSource(monitor, gap -> false);

        pressKey(source, KeyEvent.VK_A);

        InteractionEvent event = received.get(0);
        assertEquals(InteractionEvent.Kind.KEY, event.kind());
        assertEquals("A", event.detail());
        assertNotNull(event.timestamp());
    }

    @Test
    void specialKeysUseTheirReadableName() throws Exception {
        KeyboardSource source = new KeyboardSource(monitor, gap -> false);

        pressKey(source, KeyEvent.VK_ENTER);

        assertEquals(KeyEvent.getKeyText(KeyEvent.VK_ENTER), received.get(0).detail());
    }
}
