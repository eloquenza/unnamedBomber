package unnamedBomber.sprites.ai;

import unnamedBomber.sprites.AStarKI;
import unnamedBomber.sprites.KI;

public class MoveEnemyThread extends Thread {
    public static int STEPTIME;
    private KI robot;

    public MoveEnemyThread(KI ki, String tname) {
        super(tname);
        if (ki instanceof AStarKI) {
            STEPTIME = 100;
        } else {
            STEPTIME = 150;
        }
        this.robot = ki;
    }

    public KI getRobot() {
        return robot;
    }

    public void setRobot(KI robot) {
        this.robot = robot;
    }

    public boolean sleeping() {
        try {
            sleep(STEPTIME);
            return true;
        } catch (InterruptedException e) {
            robot = null;
            return false;
        }
    }

    @Override
    public void run() {
        while (!interrupted()) {
            if (robot != null) {
                robot.moverobot();
            }
            if (!sleeping()) {
                break;
            }
        }
    }
}
