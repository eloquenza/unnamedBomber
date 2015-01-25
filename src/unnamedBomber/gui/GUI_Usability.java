package unnamedBomber.gui;

import unnamedBomber.engine.Board;
import unnamedBomber.engine.GamePanel;
import unnamedBomber.highscore.HighscoreManager;
import unnamedBomber.util.Configuration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

@SuppressWarnings("serial")
public class GUI_Usability extends JFrame implements GUI {
    private static final int PREF_W = 576;
    private static final int PREF_H = 576;
    private static String[] types = {"Spieler", "Spieler", "Spieler", "Spieler"};
    private static Font textFont;
    private static String textAnleitung = "Sie starten als \"Bomberman\" in einer der 4 Ecken und muessen sich ihren Weg freibomben. Legen sie Bomben in die Naehe der grauen Bloecke, damit sie sie sprengen koennen.\n\n"
            + "Es gibt ferner 2 Bombentypen, anfangs koennen sie nur eine legen:\n"
            + "  - Normale Bombe: explodiert nach 3\n"
            + "    Sekunden,\n"
            + "  - Remotebomb: explodiert erst durch\n"
            + "    Knopfdruck oder 25 Sekunden;\n"
            + "    maximal eine pro Spieler\n\n"
            + "Gelegentlich koennen aus den Bloecken eins von 2 \"PowerUps\" erscheinen:\n"
            + "  - Multibomb: dauerhaft eine weitere\n"
            + "    Bombe legen,\n"
            + "  - Schutzschild: fuer 30 Sekunden von\n"
            + "    einer Explosion geschuetzt\n\n"
            + "Ziel ist es, als einziger Spieler zu ueberleben und deine Gegner mit Bomben zu toeten!";
    private static String textAbout = "UnnamedBomber ist ein Klon des\nberuehmten Arcade-Spiels Bomberman,\n"
            + "realisiert in einem Programmierprojekt im dritten Semester an der Hochschule  Hannover.\n\n"
            + "Sprites: Jacob Zinman-Jeanes, jeanes.co/\n"
            + "Ta Da Sound: Mike Koenig\n"
            + "Scream-Sound: www.freesfx.co.uk\n\n"
            + "Hiermit danken wir all denen, die uns\nMaterial zur Verfuegung gestellt haben.\n\n"
            + "Projektmitarbeiter:\n"
            + "\t- Dennis Grabowski,\n"
            + "\t- Jan Weiss,\n"
            + "\t- Maren Sandner,\n"
            + "\t- Jean Chung";
    private static String textInputSize = "Bitte geben sie eine gewünschte Größe - ungerade, ganze Zahl zwischen 5 und 20, ein.\n"
            + "Momentane Größe: " + Configuration.getSize() + " Felder";
    private static String textHighscoreBroken = "s!\n" + "Highscore gebrochen! " + "Tragen sie bitte ihren Namen ein.";
    private static String textInputHighscore = "Im obigen Feld können sie nach einem Spiel ihren neuen Highscore eintragen, sofern sie ihn geknackt haben. Max. 3 Zeichen werden aufgenommen.";
    private JPanel main;
    private JTabbedPane tabPane;
    private JTextArea areaUpperScores;
    private JTextArea areaLowerScores;
    private JLabel highscoreText;
    private JLabel sizeText;
    private HighscoreManager hsm;
    private JTextField nameField;
    private ImageIcon buttonBackground;
    private ImageIcon aiControlBackground;
    private ImageIcon noPlayerControlBackground;
    private JButton startButton;
    private ImageIcon[] controlButtonBackground;
    private JPanel[] controlPanels;
    // Attribute für den neuen Highscore
    private String winner = "";
    private int winnerTime;
    private boolean highscoreEntered;
    // Attribute fürs ändern der Spielfeldgröße
    private String enteredNewSize = "Größe angepasst! Die neue Größe ist nun: ";
    private String wrongNewSize = "Bitte setzen sie die Größe nach den gegebenen Richtlinien!";
    private String nanNewSize = "Bitte geben sie eine Zahl ein!"; // not a number

    public GUI_Usability() {
        super("UnnamedBomber");
        Path path = Paths.get("").toAbsolutePath();
        System.out.println(path.toString());
        buttonBackground = new ImageIcon(Paths.get(path.toString(),"res/buttonBlock.png").toString());
        aiControlBackground = new ImageIcon(Paths.get(path.toString(),"res/ai_icon.png").toString());
        noPlayerControlBackground = new ImageIcon(Paths.get(path.toString(),"res/no_player.png").toString());
        hsm = new HighscoreManager();
        highscoreEntered = false;
        main = new JPanel(new GridLayout(1, 1));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                        if ("Nimbus".equals(info.getName())) {
                            UIManager.setLookAndFeel(info.getClassName());
                            break;
                        }
                    }
                } catch (Exception e) {
                    try {
                        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    } catch (ClassNotFoundException | UnsupportedLookAndFeelException | InstantiationException | IllegalAccessException e1) {
                        e1.printStackTrace();
                    }
                }
                new GUI_Usability().createAndShowGUI();
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(PREF_W, PREF_H);
    }

    /**
     * Hilfsroutine beim Hinzufügen einer Komponente zu einem Container im GridBagLayout. Die Parameter sind Constraints beim Hinzufügen.
     *
     * @param x       x-Position
     * @param y       y-Position
     * @param width   Breite in Zellen
     * @param height  Höhe in Zellen
     * @param weightx Gewicht
     * @param weighty Gewicht
     * @param cont    Container
     * @param comp    Hinzuzufügende Komponente
     * @param insets  Abstände rund um die Komponente
     */
    private void addComponent(int x, int y, int width, int height, double weightx, double weighty, Container cont, Component comp, Insets insets) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbc.insets = insets;
        cont.add(comp, gbc);
    }

    public JComponent createBackgroundJLabel() {
        Path path = Paths.get("").toAbsolutePath();
        final ImageIcon background = new ImageIcon(Paths.get(path.toString(),"res/menubackground.jpg").toString());
        final JLabel mainpanel = new JLabel(background);
        mainpanel.setLayout(new GridBagLayout());

        return mainpanel;
    }

    private void setupTabbedPane() {
        tabPane = new JTabbedPane();

        tabPane.addTab("<html><body><div width='122' style=\"text-align:center\">Spiel</div></body></html>", null, startGameUI(), "Von hier aus kann das Spiel kontrolliert, Einstellungen geändert und gestartet werden.");
        tabPane.addTab("<html><body><div width='122' style=\"text-align:center\">Highscore</div></body></html>", null, createHighscoreUI(), "Hier kann der Highscore eingesehen, zurückgesetzt und nach einem Spiel eingegeben werden.");
        tabPane.addTab("<html><body><div width='122' style=\"text-align:center\">Anleitung</div></body></html>", null, createManualUI(), "Lesen sie hier nach, falls sie sich unsicher sind, wie das Spiel funktioniert.");
        tabPane.addTab("<html><body><div width='122' style=\"text-align:center\">Über uns</div></body></html>", null, createAboutUI(), "Hier können sie Informationen zu den Entwicklern und dem genutzten Material einsehen");

        main.add(tabPane);
        add(main);
        tabPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    private JComponent createAboutUI() {
        JComponent panel = createBackgroundJLabel();
        panel.setLayout(new BorderLayout());
        panel.add(createTextUI(textAbout), BorderLayout.CENTER);
        return panel;
    }

    private JComponent createManualUI() {
        JComponent panel = createBackgroundJLabel();
        panel.setLayout(new BorderLayout());
        panel.add(createTextUI(textAnleitung), BorderLayout.CENTER);
        return panel;
    }

    private JComponent createHighscoreUI() {
        JComponent panel = createBackgroundJLabel();
        areaUpperScores = (JTextArea) createTextUI(hsm.getUpperScores());
        areaLowerScores = (JTextArea) createTextUI(hsm.getLowerScores());
        addComponent(0, 0, 1, 1, 1.0, 1.0, panel, areaUpperScores, new Insets(0, 0, 0, 0));
        addComponent(1, 0, 1, 1, 1.0, 1.0, panel, areaLowerScores, new Insets(0, 0, 0, 0));
        addComponent(0, 1, 2, 2, 2.0, 2.0, panel, createHighscoreControlPanel(resetHighscores(areaUpperScores, areaLowerScores)), new Insets(0, 0, 0, 0));
        return panel;
    }

    private void updateScores() {
        areaUpperScores.setText(hsm.getUpperScores());
        areaLowerScores.setText(hsm.getLowerScores());
    }

    private JComponent createHighscoreControlPanel(JComponent resetButton) {
        JComponent panel = new JPanel(new GridBagLayout());
        highscoreText = createTextPane(textInputHighscore);

        addComponent(0, 0, 1, 1, 1.0, 1.0, panel, getPlayerName(), new Insets(3, 3, 0, 3));
        addComponent(0, 1, 1, 1, 1.0, 1.0, panel, highscoreText, new Insets(0, 3, 3, 3));
        addComponent(1, 0, 1, 2, 1.0, 1.0, panel, new JSeparator(SwingConstants.VERTICAL), new Insets(0, 0, 0, 0));
        addComponent(2, 0, 1, 2, 1.0, 1.0, panel, resetButton, new Insets(3, 3, 3, 3));

        return panel;
    }

    private JTextField getPlayerName() {
        nameField = new JTextField();
        KeyListener playerNameListener = new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                // DO NOTHING
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (!highscoreEntered) {
                        winner = nameField.getText().substring(0, 3);
                        nameField.setText("Name eingereicht!");
                        nameField.setEditable(false);
                        nameField.setEnabled(false);
                        hsm.addScore(winner, winnerTime);
                        updateScores();
                        highscoreEntered = true;
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // DO NOTHING
            }
        };
        nameField.setText("Auf auf, so knacke mich, vorher dürfen sie keinen Namen eintragen!");
        nameField.setEditable(false);
        nameField.setEnabled(false);
        nameField.addKeyListener(playerNameListener);
        return nameField;
    }

    private JComponent resetHighscores(JTextArea upper, JTextArea lower) {
        JButton reset = new JButton("Reset Highscores");
        reset.setHorizontalTextPosition(SwingConstants.CENTER);
        reset.setForeground(Color.BLACK);
        reset.setPreferredSize(new Dimension(576, 200));
        reset.setBorderPainted(false);
        reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ev) {
                hsm.reset();
                upper.setText(hsm.getUpperScores());
                lower.setText(hsm.getLowerScores());
            }
        });
        reset.setMargin(new Insets(0, -2, 0, -2));
        return reset;
    }

    private JComponent createTextUI(String text) {
        JComponent textArea = createJTextArea(text);
        textArea.setFont(textFont);
        textArea.setBackground(new Color(0, 0, 0, 112));
        textArea.setBorder(null);
        textArea.setForeground(new Color(226, 226, 226, 255));
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return textArea;
    }

    private JComponent startGameUI() {
        JPanel game = new JPanel();
//           Image img = new ImageIcon(this.getClass().getResource("res/menubackground.jpg")).getImage();
//
//            @Override public void paintComponent(Graphics g) {
//                super.paintComponent(g);
//                g.drawImage(img, 0, 0, img.getWidth(null), img.getHeight(null), null);
//            }}
        game.setLayout(new GridBagLayout());

        addComponent(0, 0, 1, 1, 1.0, 1.0, game, createPlayerPanel(0), new Insets(16, 20, 0, 10)); // player1
        addComponent(0, 1, 1, 1, 1.0, 1.0, game, createPlayerPanel(1), new Insets(16, 20, 16, 10)); // player2
        addComponent(1, 0, 1, 1, 1.0, 1.0, game, createPlayerPanel(2), new Insets(16, 10, 0, 20)); // player3
        addComponent(1, 1, 1, 1, 1.0, 1.0, game, createPlayerPanel(3), new Insets(16, 10, 16, 20)); // player4
        addComponent(0, 2, 2, 1, 2.0, 2.0, game, createStartGameControls(), new Insets(0, 0, 0, 0)); // controls

        validate();
        return game;
    }

    private JComponent createStartGameControls() {
        JPanel gameControls = new JPanel();
        gameControls.setLayout(new GridBagLayout());

        String[] buttonTexts = {"Start"};
        JButton[] menuButtons = new JButton[buttonTexts.length];

        startButton = new JButton(buttonTexts[0]);
        startButton.setHorizontalTextPosition(SwingConstants.CENTER);
        startButton.setForeground(Color.BLACK);
        startButton.setPreferredSize(new Dimension(120, 80));
        startButton.setMinimumSize(new Dimension(120, 80));
        startButton.setMaximumSize(new Dimension(120, 80));

        final GUI t = this;
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GamePanel.playerTypes = types;

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            highscoreEntered = false;
                            nameField.setEditable(true);
                            new Board(t);
                        } catch (IOException | AWTException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        sizeText = createTextPane(textInputSize);

        addComponent(0, 0, 1, 1, 1.0, 1.0, gameControls, getNewSize(), new Insets(3, 3, 0, 0));
        addComponent(0, 1, 1, 1, 1.0, 1.0, gameControls, sizeText, new Insets(0, 3, 3, 0));
        addComponent(1, 0, 1, 2, 1.0, 1.0, gameControls, new JSeparator(SwingConstants.VERTICAL), new Insets(0, 0, 0, 0));
        addComponent(2, 0, 2, 2, 1.0, 1.0, gameControls, startButton, new Insets(3, 0, 3, 3));
        return gameControls;
    }

    private void initiateControlPanels() {
        controlPanels = new JPanel[4];

        for (int i = 0; i < 4; i++) {
            controlPanels[i] = (JPanel) createImageControlPanel(i);
        }
    }

    private JComponent createPlayerPanel(int playernr) {
        JPanel playerPanel = new JPanel();
        playerPanel.setLayout(new BorderLayout(0, 0));
        playerPanel.add(createPlayerDropDown(playernr), BorderLayout.NORTH);
        playerPanel.add(controlPanels[playernr], BorderLayout.CENTER);
        validate();
        return playerPanel;
    }

    private JComponent createImageControlPanel(int playernr) {
        JPanel imageControlPanel = new JPanel();
        imageControlPanel.setLayout(new GridBagLayout());

        createPlayerControls(imageControlPanel, playernr);

        return imageControlPanel;
    }

    private void createAIControls(JPanel imageControlPanel) {
        JButton aiControl = new JButton(aiControlBackground);
        aiControl.setSize(new Dimension(205, 130));
        aiControl.setContentAreaFilled(false);
        aiControl.setBorderPainted(false);
        aiControl.setBorder(null);
        addComponent(0, 0, 1, 1, 1.0, 1.0, imageControlPanel, aiControl, new Insets(3, 3, 3, 3));
        revalidate();
    }

    private void createNoControls(JPanel imageControlPanel) {
        JButton noControl = new JButton(noPlayerControlBackground);
        noControl.setSize(new Dimension(205, 130));
        noControl.setContentAreaFilled(false);
        noControl.setBorderPainted(false);
        noControl.setBorder(null);
        addComponent(0, 0, 1, 1, 1.0, 1.0, imageControlPanel, noControl, new Insets(3, 3, 3, 3));
        revalidate();
    }

    private void createPlayerControls(JPanel imageControlPanel, int playernr) {
        JButton[] menuButtons = new JButton[6];

        for (int i = 0; i < 6; ++i) {
            String buttontext = KeyEvent.getKeyText(Configuration.playerControls[playernr][i]);
            if (buttontext.contains("NumPad-")) {
                buttontext = buttontext.replace("NumPad-", "NP ");
            }
            menuButtons[i] = new ControlButton(buttontext, controlButtonBackground[i], playernr, i);
            menuButtons[i].setForeground(new Color(255, 255, 255, 255));
        }

        addComponent(1, 0, 1, 1, 0.0, 0.0, imageControlPanel, menuButtons[0], new Insets(3, 3, 3, 3));
        addComponent(0, 1, 1, 1, 0.0, 0.0, imageControlPanel, menuButtons[1], new Insets(3, 3, 3, 3));
        addComponent(1, 1, 1, 1, 0.0, 0.0, imageControlPanel, menuButtons[2], new Insets(3, 3, 3, 3));
        addComponent(2, 1, 1, 1, 0.0, 0.0, imageControlPanel, menuButtons[3], new Insets(3, 3, 3, 3));
        addComponent(0, 0, 1, 1, 0.0, 0.0, imageControlPanel, menuButtons[4], new Insets(3, 3, 3, 3));
        addComponent(2, 0, 1, 1, 0.0, 0.0, imageControlPanel, menuButtons[5], new Insets(3, 3, 3, 3));
        revalidate();
    }

    private JComponent createPlayerDropDown(int playernr) {
        JComboBox<String> dropdown = new JComboBox<String>();
        DefaultComboBoxModel<String> dcbm = new DefaultComboBoxModel<String>();
        dcbm.addElement("Spieler " + (playernr + 1));
        dcbm.addElement("Bot - einfach");
        dcbm.addElement("Bot - schwer");
        dcbm.addElement("freier Slot");
        dropdown.setModel(dcbm);

        dropdown.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                types[playernr] = e.getItem().toString();

                controlPanels[playernr].removeAll();
                switch (types[playernr]) {
                    case "Bot - einfach":
                    case "Bot - schwer":
                        createAIControls(controlPanels[playernr]);
                        break;
                    case "freier Slot":
                        createNoControls(controlPanels[playernr]);
                        break;
                    default:
                        createPlayerControls(controlPanels[playernr], playernr);
                        break;
                }

                int amountOfPlayers = 0;
                for (int i = 0; i < types.length; ++i) {
                    if (!types[i].contains("freier")) {
                        amountOfPlayers++;
                    }
                }

                startButton.setEnabled((amountOfPlayers > 1));
            }
        });

        return dropdown;
    }

    private void resetPlayerTypes() {
        for (int i = 0; i < types.length; i++) {
            types[i] = "Spieler";
        }
    }

    private JComponent createJTextArea(String txt) {
        JTextArea textArea = new JTextArea(3, 60);
        textArea.setLineWrap(true);
        textArea.setText(txt);
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        return textArea;
    }

    private JComponent getNewSize() {
        JTextField sizeField = new JTextField();
        KeyListener sizeListener = new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                // DO NOTHING
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        int newSize = Integer.parseInt(sizeField.getText());
                        if (newSize >= 5 && newSize < 20 && newSize % 2 == 1) {
                            Configuration.setSize(newSize);
                            sizeField.setText(enteredNewSize + newSize);
                            sizeText.setText(htmlString("Bitte geben sie eine gewünschte Größe - ungerade, ganze Zahl zwischen 5 und 20, ein.\n"
                                    + "Momentane Größe: " + Configuration.getSize() + " Felder"));
                            sizeField.selectAll();
                        } else {
                            sizeField.setText(wrongNewSize);
                            sizeField.selectAll();
                        }
                    } catch (NumberFormatException excep) {
                        sizeField.setText(nanNewSize);
                        sizeField.selectAll();
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // DO NOTHING
            }
        };
        sizeField.addKeyListener(sizeListener);
        return sizeField;
    }

    private void createAndShowGUI() {
        setPreferredSize(getPreferredSize());
        setResizable(false);
        setIgnoreRepaint(false);
        createCustomFont();
        addExitListener();
        createControlButtonBackgrounds();
        initiateControlPanels();
        setupTabbedPane();
        pack();
        setLocationRelativeTo(null); // Fenster zentrieren
        setVisible(true);
        main.setVisible(true);
    }

    private void addExitListener() {
        JFrame t = this;
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        WindowListener exit = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (
                        JOptionPane.showOptionDialog(t, "Möchten sie diese Applikation wirklich schließen?", "Bestätigen sie bitte ihre Wahl.", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null) == JOptionPane.YES_OPTION
                        ) {
                    System.exit(0);
                }
            }
        };
        addWindowListener(exit);
    }

    private void createCustomFont() {
        try {
            Path path = Paths.get("").toAbsolutePath();
            textFont = Font.createFont(Font.TRUETYPE_FONT, new File(Paths.get(path.toString(),"res/Bomberman.ttf").toString())).deriveFont(20f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(textFont);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
    }

    private void createControlButtonBackgrounds() {
        Path path = Paths.get("").toAbsolutePath();
        controlButtonBackground = new ImageIcon[6];
        controlButtonBackground[0] = new ImageIcon(Paths.get(path.toString(),"res/up.png").toString());
        controlButtonBackground[1] = new ImageIcon(Paths.get(path.toString(),"res/left.png").toString());
        controlButtonBackground[2] = new ImageIcon(Paths.get(path.toString(),"res/down.png").toString());
        controlButtonBackground[3] = new ImageIcon(Paths.get(path.toString(),"res/right.png").toString());
        controlButtonBackground[4] = new ImageIcon(Paths.get(path.toString(),"res/bomb.png").toString());
        controlButtonBackground[5] = new ImageIcon(Paths.get(path.toString(),"res/remotebomb.png").toString());
    }

    private JLabel createTextPane(String txt) {
        JLabel panel = new JLabel(htmlString(txt)) {
            public Dimension getPreferredSize() {
                return new Dimension(424, 50);
            }

            public Dimension getMinimumSize() {
                return new Dimension(424, 50);
            }

            public Dimension getMaximumSize() {
                return new Dimension(424, 50);
            }
        };
        panel.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        return panel;
    }

    private String htmlString(String txt) {
        return "<html>\n"
                + "<div style=\"white-space: pre-line\">\n"
                + txt
                + "</div>\n"
                + "</html>\n";
    }

    public void getHighscoreInfo(int time) {
        winnerTime = time;
        tabPane.setSelectedIndex(1);
        highscoreText.setText(htmlString(winnerTime + textHighscoreBroken));
        nameField.setEnabled(true);
        nameField.setText("Gib deinen Namen ein, maximal 3 Zeichen werden aufgenommen.");
        nameField.selectAll();
    }
}
