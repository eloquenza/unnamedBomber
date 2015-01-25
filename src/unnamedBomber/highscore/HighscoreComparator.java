package unnamedBomber.highscore;

import java.util.Comparator;

public class HighscoreComparator implements Comparator<Highscore> {

    @Override
    public int compare(Highscore a, Highscore b) {
        int aTime = a.getTime();
        int bTime = b.getTime();

        if (aTime < bTime) {
            return -1;
        } else if (aTime > bTime) {
            return 1;
        }
        return 0;
    }
}
