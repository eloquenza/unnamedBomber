package unnamedBomber.sprites;

import unnamedBomber.sprites.ai.MoveEnemyThread;

import java.io.IOException;

@SuppressWarnings("serial")
public abstract class KI extends AbstractPlayer {
    private MoveEnemyThread emt;

    public KI(int startx, int starty, int pnr, String tname) throws IOException {
        super(startx, starty, pnr);
        emt = new MoveEnemyThread(this, tname);
    }

    public MoveEnemyThread getEnemyMoveThread() {
        return emt;
    }

    public void setEnemyMoveThread(MoveEnemyThread enemyMoveThread) {
        emt = enemyMoveThread;
    }

    public void startMoving() {
        if (!emt.isAlive()) {
            emt.start();
        }
    }

    public abstract void moverobot();
}
