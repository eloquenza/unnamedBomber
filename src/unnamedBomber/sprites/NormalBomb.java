package unnamedBomber.sprites;

import java.io.IOException;

/**
 * Die NormalBomb Klasse repräsentiert die normale Bombe im Spiel. Jeder Spieler kann durch Tastendruck eine normale Bombe legen. Durch das Platzieren der Bombe
 * wird ein (momentan) 3 Sekunden Zünder gestartet, nach welchem die Bombe explodiert. Nach der Explosion werden alle Spieler/zerstörbaren Blöcke um die Bombe
 * zerstört. Somit öffnen sich weitere Wege im Labyrinth.
 *
 * @author Unnamed#1
 */

@SuppressWarnings("serial")
public class NormalBomb extends Bomb {

    private static final long BOMBTIMER = 3L;

    /**
     * Initialisiert das NormalBomb() Objekt an den gegegeben X- und Y-Koordinaten mit dem dazugehörigen AbstractPlayer-Objekt. Ruft ferner startTimer() auf, um
     * den Zünder zu starten
     *
     * @param startx X-Koordinaten
     * @param starty Y-Koordinaten
     * @param player repräsentiert den Player, der die Bombe platzierte.
     * @throws IOException
     */
    public NormalBomb(int startx, int starty, AbstractPlayer player) throws IOException {
        super(startx, starty, "res/bomb.png", player);
        startTimer(BOMBTIMER);
    }

    @Override
    public String toString() {
        return "NoBomb: " + this.getX() + ", " + this.getY() + " (" + this.getX() / 64 + ", " + this.getY() / 64 + ") " + this.hashCode() + "\t";
    }
}
