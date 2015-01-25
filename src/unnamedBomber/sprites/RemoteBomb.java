package unnamedBomber.sprites;

import java.io.IOException;

/**
 * Die RemoteBomb Klasse repräsentiert sogenannte "Remote Bombs" im Spiel. Remote Bombs sollen durch weiteren Tastendruck zündbar sein, anstatt wie die normale
 * Bombe nach einem festen Timer zu explodieren.
 *
 * @author Unnamed#1
 */

@SuppressWarnings("serial")
public class RemoteBomb extends Bomb {

    private static final long BOMBTIMER = 25L;

    public RemoteBomb(int startx, int starty, AbstractPlayer player) throws IOException {
        super(startx, starty, "res/remotebomb.png", player);
        startTimer(BOMBTIMER);
    }
}
