package unnamedBomber.gui;

import unnamedBomber.util.Configuration;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Created by DrElo on 13.05.2015.
 */
public class ControlButtonObserver implements KeyListener {

    private static final int AMOUNTOFPLAYERS = 4;
    private static final int AMOUNTOFKEYS = 6;

    ControlButton obsButton;

    ControlButtonObserver(ControlButton j) {
        obsButton = j;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        boolean possible = true;
        for (int i = 0; i < AMOUNTOFPLAYERS; ++i) {
            for (int j = 0; j < AMOUNTOFKEYS; ++j) {
                if (Configuration.playerControls[i][j] == e.getKeyCode()) {
                    possible = false;
                }
            }
        }
        if (possible) {
            Configuration.playerControls[obsButton.getPlayernr()][obsButton.getKey()] = e.getKeyCode();
            obsButton.setText(KeyEvent.getKeyText(e.getKeyCode()));
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
