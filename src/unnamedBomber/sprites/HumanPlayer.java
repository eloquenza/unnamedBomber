package unnamedBomber.sprites;

import unnamedBomber.engine.GamePanel;
import unnamedBomber.util.Calculations;
import unnamedBomber.util.Configuration;

import java.io.IOException;

@SuppressWarnings("serial")
public class HumanPlayer extends AbstractPlayer {

    public HumanPlayer(int startx, int starty, int pnr) throws IOException {
        super(startx, starty, pnr);
    }

    public void performAction() {
        if (GamePanel.isPressed(Configuration.playerControls[getPlayerNR()][0])) {
            move(-1);
        }
        if (GamePanel.isPressed(Configuration.playerControls[getPlayerNR()][1])) {
            move(-(Configuration.getFieldsize()));
        }
        if (GamePanel.isPressed(Configuration.playerControls[getPlayerNR()][2])) {
            move(1);
        }
        if (GamePanel.isPressed(Configuration.playerControls[getPlayerNR()][3])) {
            move(Configuration.getFieldsize());
        }
        if (GamePanel.isPressed(Configuration.playerControls[getPlayerNR()][4])) {
            placeNormalBomb();
        }
        if (GamePanel.isPressed(Configuration.playerControls[getPlayerNR()][5])) {
            if (isRemoBombPlaced()) {
                GamePanel.keyRelease(Configuration.playerControls[getPlayerNR()][5]);
                getRbomb().stopTimer();
                setRemoBombPlaced(false);
            } else {
                placeRemoteBomb();
            }
        }
    }

    @Override
    public String toString() {
        return "HumanPlayer-" + getPlayerNR() + " X: " + getX() + " Y: " + getY() + " pos: " + Calculations.listPos(this);
    }
}
