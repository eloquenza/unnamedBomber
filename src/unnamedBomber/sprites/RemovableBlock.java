package unnamedBomber.sprites;

import java.io.IOException;

/**
 * Die RemovableBlock Klasse repräsentiert die zerstörbaren Blöcke im Spiel. Zerstörbare Blöcke versperren den Weg im Labyrinth und können durch Bomben zerstört
 * werden.
 *
 * @author Unnamed#1
 */

@SuppressWarnings("serial")
public class RemovableBlock extends Sprite {

    public RemovableBlock(int startx, int starty) throws IOException {
        super(startx, starty, "res/removblock.png");
    }

    @Override
    public String toString() {
        return "RemBlk: " + this.getX() + ", " + this.getY() + " (" + this.getX() / 64 + ", " + this.getY() / 64 + ")  \t";
    }
}