package unnamedBomber.highscore;

import java.io.Serializable;

public class Highscore implements Serializable {
    private static final long serialVersionUID = 1L;
    private int time;
    private String pName;

    public Highscore(String pm, int t) {
        time = t;
        pName = pm;
    }

    public int getTime() {
        return time;
    }

    public String getPName() {
        return pName;
    }
}
