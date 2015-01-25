package unnamedBomber.engine;

import unnamedBomber.gui.GUI;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Die Board-Klasse repräsentiert unser JFrame. Ausserdem implementiert sie unsere Main Methode.
 *
 * @author Unnamed#1
 */
@SuppressWarnings("serial")
public class Board extends JFrame {
    private GUI root;
    private GamePanel gp;

    public Board(GUI root) throws IOException, AWTException {
        super("UnnamedBomber");
        this.root = root;
        gp = new GamePanel(this);
        createAndShowGUI();
    }

    public void gameOver(int time, boolean winnerIsHuman) {
        dispose();
        gp.endGame();
        gp = null;
        if (winnerIsHuman) {
            root.getHighscoreInfo(time);
        }
    }

    public void createAndShowGUI() throws IOException, AWTException {
        setSize(Toolkit.getDefaultToolkit().getScreenSize());
        setBackground(Color.BLACK);
        setLayout(new BorderLayout());
        add(gp, BorderLayout.CENTER);

        setUndecorated(true);
        setIgnoreRepaint(true);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null); // Fenster zentrieren MUSS NACH PACK AUFGERUFEN WERDEN.
    }
}
