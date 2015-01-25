package unnamedBomber.highscore;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

public class HighscoreManager {

    private static final int MAXHIGHSCORES = 38;
    private static final int ABSTRACTVALUE = 9999;
    private static final String HSFILENAME = "highscore.dat";
    private ArrayList<Highscore> highscores;
    private File hsf;
    private ObjectOutputStream outStream = null;
    private ObjectInputStream inStream = null;

    public HighscoreManager() {
        highscores = new ArrayList<Highscore>();
        hsf = new File(HSFILENAME);
        if (!hsf.exists()) {
            createInitFile();
        }
        loadScores();
        sortScores();
    }

    public void reset() {
        createInitFile();
    }

    private void createInitFile() {
        try {
            hsf.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        highscores.clear();
        for (int i = 0; i < MAXHIGHSCORES; i++) {
            highscores.add(new Highscore("P" + (i + 1), ABSTRACTVALUE));
        }
        saveScores();
    }

    public ArrayList<Highscore> getHighscoreList() {
        return highscores;
    }

    private void sortScores() {
        Collections.sort(highscores, new HighscoreComparator());
    }

    public void addScore(String name, int score) {
        highscores.add(new Highscore(name, score));
        sortScores();
        saveScores();
    }

    public String getLowerScores() {
        return getScores((MAXHIGHSCORES / 2), MAXHIGHSCORES);
    }

    public String getUpperScores() {
        return getScores(0, (MAXHIGHSCORES / 2));
    }

    private String getScores(int i, int max) {
        String hsStr = "";

        while (i < highscores.size() && i < max) {
            hsStr += (i + 1);
            if (i >= 9) {
                hsStr += ".  ";
            } else {
                hsStr += ".   ";
            }
            hsStr += highscores.get(i).getTime() + " sec\t " + highscores.get(i).getPName() + "\n";
            i++;
        }
        return hsStr;
    }

    @SuppressWarnings("unchecked")
    private void loadScores() {
        try {
            inStream = new ObjectInputStream(new FileInputStream(HSFILENAME));
            highscores = (ArrayList<Highscore>) inStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveScores() {
        try {
            outStream = new ObjectOutputStream(new FileOutputStream(HSFILENAME));
            outStream.writeObject(highscores);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outStream != null) {
                    outStream.flush();
                    outStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
