package unnamedBomber.sprites;

import java.io.IOException;

/**
 * Die {@code UndestructableBlock} Klasse repräsentiert unzerstörbare Blöcke im Spiel.
 *
 * @author Unnamed#1
 */

@SuppressWarnings("serial")
public class UndestructableBlock extends Sprite {

    public UndestructableBlock(int startx, int starty) throws IOException {
        super(startx, starty, "res/block.png");
    }

    @Override
    public String toString() {
        return "UndBlk: " + this.getX() + ", " + this.getY() + " (" + this.getX() / 64 + ", " + this.getY() / 64 + ")  \t";
    }
}
