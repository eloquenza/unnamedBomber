package unnamedBomber.sprites;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Die GameObjectSprite Klasse ist die Superklasse aller Spielobjekte.
 *
 * @author Unnamed#1
 */

@SuppressWarnings("serial")
public class Sprite extends JPanel {
    protected int PREF_W = 64;
    protected int PREF_H = 64;
    private Image image;
    private int x;
    private int y;

    /**
     * Initialisiert das Spielobjekt mit den übergebenen Koordinaten und dem jeweiligen Sprite.
     *
     * @param startx X-Koordinate
     * @param starty Y-Koordinate
     * @param file   Sprite
     * @throws IOException
     */
    public Sprite(int startx, int starty, String file) throws IOException {
        Path path = Paths.get("").toAbsolutePath();
        ImageIcon ii = new ImageIcon(Paths.get(path.toString(), file).toString());
        image = ii.getImage();
        x = startx;
        y = starty;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(String name) {
        Path path = Paths.get("").toAbsolutePath();
        ImageIcon i = new ImageIcon(Paths.get(path.toString(), name).toString());
        image = i.getImage();
    }

    public int getX() {
        return x;
    }
    /**
     * Setzt X-Koordinate auf den übergebenen Parameter.
     *
     * @param x neue X-Koordinate
     */
    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    /**
     * Setzt Y-Koordinate auf den übergebenen Parameter.
     *
     * @param y neue Y-Koordinate
     */
    public void setY(int y) {
        this.y = y;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, PREF_W, PREF_H);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + PREF_H;
        result = prime * result + PREF_W;
        result = prime * result + x;
        result = prime * result + y;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Sprite other = (Sprite) obj;
        if (PREF_H != other.PREF_H)
            return false;
        if (PREF_W != other.PREF_W)
            return false;
        if (x != other.x)
            return false;
        return y == other.y;
    }
}
