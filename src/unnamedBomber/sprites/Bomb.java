package unnamedBomber.sprites;

import unnamedBomber.engine.GamePanel;
import unnamedBomber.sound.Sound;
import unnamedBomber.util.Calculations;

import javax.sound.sampled.FloatControl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Interface für unsere Bomben, momentan nur genutzt für die gemeinsame BLASTRADIUS-Variable.
 */
@SuppressWarnings("serial")
public abstract class Bomb extends Sprite {
    public static final int NORMALBOMB = 0;
    public static final int REMOTEBOMB = 1;
    private static int calcCount = 0;
    final int BLASTRADIUS = 1;
    AbstractPlayer player;
    ScheduledExecutorService scheduler;
    ScheduledExecutorService schedulerFlame;
    Sound explosion = new Sound("res/Bang.wav");
    // private static int callCount = 0;
    private Bomb bomb;

    public Bomb(int startx, int starty, String file, AbstractPlayer player) throws IOException {
        super(startx, starty, file);
        this.player = player;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        schedulerFlame = Executors.newSingleThreadScheduledExecutor();
    }

    public Bomb getBomb() {
        return bomb;
    }

    public void setBomb(Bomb b) {
        bomb = b;
    }

    /**
     * Berechnet die "Kollision" der Explosion und entfernt ggf. getroffene, zerstörbare Blöcke rings um den Bombspot
     */
    public void collision(Bomb bomb) {
        int pos = Calculations.listPos(bomb);
        int posarray[] = Calculations.getOwnAndSurroundedFields();
        for (int i = 0; i < 5; i++) {
            iterateCollision(GamePanel.getSprites(pos + posarray[i]), pos + posarray[i]);
        }
        FloatControl gainControl = (FloatControl) explosion.c.getControl(FloatControl.Type.MASTER_GAIN);
        double gain = .5D; // number between 0 and 1 (loudest)
        float dB = (float) (Math.log(gain) / Math.log(10.0) * 20.0);
        gainControl.setValue(dB);
        explosion.start();
    }

    public void iterateCollision(List<Sprite> list, int pos) {
        List<Sprite> toRemoveList = new ArrayList<Sprite>();
        synchronized (list) {
            RemovableBlock block = null;
            Bomb b = null;
            Iterator<Sprite> iter = list.iterator();
            while (iter.hasNext()) {
                Sprite curr = iter.next();
                if (curr instanceof AbstractPlayer) {
                    if (!((AbstractPlayer) curr).getShield()) {
                        toRemoveList.add(curr);
                        GamePanel.kill(curr);
                    } else {
                        // System.out.println("Spieler " + ((AbstractPlayer) curr).getPlayerNR() + ": Schild verbraucht");
                        ((AbstractPlayer) curr).setShield(false);
                    }
                } else if (curr instanceof RemovableBlock) {
                    toRemoveList.add(curr);
                    // falls Bombe, dann speichern um danach an dieser Position ein Powerup zu erstellen
                    // während der Iteration führt dies zu Fehlern
                    block = (RemovableBlock) curr;
                } else if (curr instanceof PowerUp) {
                    toRemoveList.add(curr);
                } else if (curr instanceof Bomb) {
                    toRemoveList.add(curr);
                    if (!curr.equals(bomb)) {
                        b = (Bomb) curr;
                    }
                }
            }
            list.removeAll(toRemoveList);
            if (block != null) {
                createPowerUp(block.getX(), block.getY(), pos);
            }
            if (b != null) {
                b.stopTimer();
            }
        }
    }

    private void createPowerUp(int x, int y, int pos) {
        double wschl = Math.random();
        if (wschl < 0.15) {
            try {
                Multibomb mBomb = new Multibomb(x, y, this.player);
                GamePanel.getSprites(pos).add(mBomb);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (wschl > 0.85) {
            try {
                Shield shield = new Shield(x, y, this.player);
                GamePanel.getSprites(pos).add(shield);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Startet den Zünder der Bomber, indem ein Thread nach 2 Sekunden gestartet wird. Der Thread entfernt daraufhin die Bombe, führt eine Kollisionsabfrage
     * durch und gibt dem Spieler eine Bombe.
     *
     */
    public void startTimer(long time) {
        try {
            scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    explode();
                }
            }, time, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void explode() {
        Thread calcCollision = new Thread() {
            @Override
            public void run() {
                collision(bomb);
                showFlames(bomb);

                if (bomb instanceof RemoteBomb) {
                    player.setRemoBombPlaced(false);
                }
                setBomb(null);
                // System.out.println("count: " + (++callCount) + " called by: " + this);
                player.decrBombCounter();
                if (player instanceof AStarKI) {
                    ((AStarKI) player).scanForBombs();
                }
            }
        };
        calcCollision.setName("calcCollision-" + (++calcCount));
        calcCollision.start();
    }

    /**
     * Anzeigen von Flammen nach Bombenexplosion
     */
    private void showFlames(Bomb bomb) {
        int[] xWerte = {0, -64, 64, 0, 0}; // eigene pos, links, rechts, oben, unten
        int[] yWerte = {0, 0, 0, -64, 64};
        List<ExplosionFlames> removing = new ArrayList<ExplosionFlames>();

        for (int i = 0; i < 5; i++) {
            ExplosionFlames f = null;
            try {
                f = new ExplosionFlames(bomb.getX() + xWerte[i], bomb.getY() + yWerte[i]);
                synchronized (GamePanel.flames) {
                    GamePanel.flames.add(f);
                }
                removing.add(f);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        try {
            schedulerFlame.schedule(new Runnable() {
                @Override
                public void run() {
                    synchronized (GamePanel.flames) {
                        GamePanel.flames.removeAll(removing);
                    }
                }
            }, 250, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopTimer() {
        scheduler.shutdownNow();
        explode();
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        return getClass() == obj.getClass();
    }
}
