package unnamedBomber.sprites;

import java.io.IOException;

@SuppressWarnings("serial")
public class ExplosionFlames extends Sprite {

    public ExplosionFlames(int startx, int starty) throws IOException {
        super(startx, starty, "res/Flame_Animation.gif");
    }

    public String toString() {
        return "ExplosionFlames: " + this.getX() + ", " + this.getY() + " (" + this.getX() / 64 + ", " + this.getY() / 64 + ") ";
    }
}
