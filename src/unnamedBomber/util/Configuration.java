package unnamedBomber.util;

import java.awt.event.KeyEvent;

/**
 * Die Configuration Klasse hält zurzeit Konfigurationen zu dem Spiel bereit.
 * <p>
 * Zum Beispiel die eingestellten Tasten für die jeweiligen Spieler sowie die Größe des Spielfelds.
 *
 * @author Unnamed#1
 */
public class Configuration {
    public static final int IMAGESIZE = 64;
    // KeyEvent.VK_X => KeyStroke Objekt von KeyEvent.VK_X und Modifier 0. Modifier ist Bitmaske und kann verodert werden.
    public static int[][] playerControls = {
            {KeyEvent.VK_UP, KeyEvent.VK_LEFT, KeyEvent.VK_DOWN, KeyEvent.VK_RIGHT, KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT},
            {KeyEvent.VK_W, KeyEvent.VK_A, KeyEvent.VK_S, KeyEvent.VK_D, KeyEvent.VK_Q, KeyEvent.VK_E},
            {KeyEvent.VK_I, KeyEvent.VK_J, KeyEvent.VK_K, KeyEvent.VK_L, KeyEvent.VK_U, KeyEvent.VK_O},
            {KeyEvent.VK_NUMPAD8, KeyEvent.VK_NUMPAD4, KeyEvent.VK_NUMPAD5, KeyEvent.VK_NUMPAD6, KeyEvent.VK_NUMPAD7, KeyEvent.VK_NUMPAD9}};
    private static int pref_w = 1088;
    private static int pref_h = 1088;
    private static int size = 15;

    public static int getSize() {
        return size;
    }

    public static void setSize(int newsize) {
        size = newsize;
    }

    public static int getFieldsize() {
        return (size + 2);
    }

    public static int getPrefW() {
        return pref_w;
    }

    public static void setPrefW(int width) {
        pref_w = width;
    }

    public static int getPrefH() {
        return pref_h;
    }

    public static void setPrefH(int height) {
        pref_h = height;
    }
}
