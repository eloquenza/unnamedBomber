package unnamedBomber.sprites;

import java.io.IOException;

/**
 * Die {@code Tile} Klasse repräsentiert unsere Basisflächen im Spiel.
 *
 * @author Unnamed#1
 */

@SuppressWarnings("serial")
public class Tile extends Sprite {

    public Tile(int startx, int starty) throws IOException {
        super(startx, starty, "res/tile.png");
    }
}
