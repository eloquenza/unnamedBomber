package unnamedBomber.sprites;

import unnamedBomber.engine.GamePanel;
import unnamedBomber.util.Calculations;
import unnamedBomber.util.Configuration;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Die Player-Klasse repräsentiert einen Spieler.
 * <p>
 * Ein Spieler kann sich bewegen durch die jeweiligen Bewegungstasten, abhängig von seiner Spielernummer. Ferner kann er eine Bombe legen.
 *
 * @author Unnamed#1
 */
@SuppressWarnings("serial")
public abstract class AbstractPlayer extends Sprite {
    private static String[] playerImages = {"res/player1.png", "res/player2.png", "res/player3.png", "res/player4.png"};
    private int volacity = 1;
    private int pos;
    private int playernr;
    private volatile int bombcounter;
    private volatile int maxBombs;
    private boolean shield = false;
    private boolean remoBombPlaced = false;
    private RemoteBomb rbomb;
    private boolean moving = false;

    public AbstractPlayer(int startx, int starty, int pnr) throws IOException {
        super(startx, starty, playerImages[pnr]);
        playernr = pnr;
        bombcounter = 0;
        maxBombs = 1;
    }

    boolean getShield() {
        return shield;
    }

    /**
     * setzt Schild für entsprechenden Spieler oder löscht wieder
     *
     * @param b true - Schild setzen, false - löschen
     */
    void setShield(boolean b) {
        String[] playerShieldedImages = {"res/player1Shielded.png", "res/player2Shielded.png", "res/player3Shielded.png", "res/player4Shielded.png"};
        if (b) {
            setImage(playerShieldedImages[playernr]);
        } else {
            setImage(playerImages[playernr]);
        }
        shield = b;
    }

    boolean getBombSet() {
        return bombcounter != 0;
    }

    public boolean isRemoBombPlaced() {
        return remoBombPlaced;
    }

    public void setRemoBombPlaced(boolean remoBombPlaced) {
        this.remoBombPlaced = remoBombPlaced;
    }

    /**
     * Bewegt Spieler in die übergebene Richtung, falls das Feld frei ist
     *
     * @param direction Richtung - (listposition im Verhältnis zu eigener Position)
     */
    public void move(int direction) {
        if (!collision(direction)) {
            if (!moving) {
                AbstractPlayer p = this;
                moving = true;
                new Thread() {
                    @Override
                    public void run() {
                        int listpos = Calculations.listPos(p);
                        int moved = 0;
                        while (moved < 64) {
                            if (direction == 1) {
                                setY(getY() + volacity);
                            } else if (direction == -1) {
                                setY(getY() - volacity);
                            } else if (direction == Configuration.getFieldsize()) {
                                setX(getX() + volacity);
                            } else if (direction == -Configuration.getFieldsize()) {
                                setX(getX() - volacity);
                            }
                            moved++;
                            try {
                                Thread.sleep(3);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                        synchronized (GamePanel.getSprites(listpos)) {
                            if (GamePanel.getSprites(listpos).remove(p)) {
                                listpos += direction;
                                GamePanel.getSprites(listpos).add(p);
                                pickUpPowerUp(GamePanel.getSprites(listpos));
                            }
                        }
                        moving = false;
                        // System.out.println("ich bin in liste: " + Calculations.listPos(p));
                        // System.out.println((float) (System.nanoTime() - start) / 100000.0);
                    }
                }.start();
            }
        }
    }

    /**
     * versucht Bombe zu legen. Beachtet Anzahl maximal zu legender Bomben
     *
     * @param bombDecision - switch-case bombdecision | 0 = normalbomb | 1 = remotebomb
     */
    private void placeBomb(int bombDecision) {
        List<Sprite> list = GamePanel.getSprites(Calculations.listPos(this));
        Iterator<Sprite> listIter = list.iterator();
        boolean bombPlaced = false;
        while (listIter.hasNext()) {
            if (listIter.next() instanceof Bomb) {
                bombPlaced = true;
            }
        }
        if (!bombPlaced && bombcounter >= 0 && bombcounter < maxBombs) {
            Bomb bomb = null;
            switch (bombDecision) {
                case Bomb.NORMALBOMB:
                    try {
                        bomb = new NormalBomb(((this.getX() + 16) / 64) * 64, ((this.getY()) / 64) * 64, this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case Bomb.REMOTEBOMB:
                    try {
                        rbomb = new RemoteBomb(((this.getX() + 16) / 64) * 64, ((this.getY()) / 64) * 64, this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    bomb = rbomb;
                    remoBombPlaced = true;
                    break;
            }
            bomb.setBomb(bomb);
            list.add(bomb);
            bombcounter++;
        }
    }

    public void placeNormalBomb() {
        GamePanel.keyRelease(Configuration.playerControls[playernr][4]);
        placeBomb(Bomb.NORMALBOMB);
    }

    /**
     * legt remoteBomb nur, wenn noch keine selbst gelegt
     */
    public void placeRemoteBomb() {
        GamePanel.keyRelease(Configuration.playerControls[playernr][5]);

        if (!remoBombPlaced) {
            placeBomb(Bomb.REMOTEBOMB);
        }
    }

    public RemoteBomb getRbomb() {
        return rbomb;
    }

    public int getPlayerNR() {
        return playernr;
    }

    public void incrMaxBombs() {
        maxBombs++;
        // System.out.println("maxBombs now max. : " + maxBombs);
    }

    public void decrBombCounter() {
        bombcounter--;
        // System.out.println("decreased bombcounter by one, now having " + bombcounter);
    }

    public int getBombcounter() {
        return bombcounter;
    }

    public int getPosChange() {
        return pos;
    }

    public int getMaxBombs() {
        return maxBombs;
    }

    /**
     * Berechnet Spielerposition voraus und schaut, ob diese dann mit einem Block sich schneidet.
     *
     * @return true, wenn Kollision vorhanden, false, falls keine.
     */
    public boolean collision(int pos) {
        int listpos = Calculations.listPos(this);
        return iterateCollision(GamePanel.getSprites(listpos + pos), listpos + pos);
    }

    public boolean iterateCollision(List<Sprite> list, int pos) {
        synchronized (list) {
            Iterator<Sprite> iter = list.iterator();
            while (iter.hasNext()) {
                Sprite curr = iter.next();
                if (curr instanceof UndestructableBlock || curr instanceof RemovableBlock || curr instanceof Bomb) {
                    return true;
                }
                if (Calculations.listPos(this) == Calculations.listPos(curr) && curr instanceof PowerUp) {
                    ((PowerUp) curr).activate();
                    iter.remove();
                }
            }
        }
        return false;
    }

    private void pickUpPowerUp(List<Sprite> list) {
        synchronized (list) {
            Iterator<Sprite> iter = list.iterator();
            while (iter.hasNext()) {
                Sprite curr = iter.next();
                if (curr instanceof PowerUp) {
                    ((PowerUp) curr).activate();
                    iter.remove();
                    break;
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Player: " + this.getX() + ", " + this.getY() + " (" + this.getX() / 64 + ", " + this.getY() / 64 + ")  \t";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + playernr;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractPlayer other = (AbstractPlayer) obj;
        return playernr == other.playernr;
    }
}