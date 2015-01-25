package unnamedBomber.sprites;

import unnamedBomber.engine.GamePanel;
import unnamedBomber.util.Calculations;
import unnamedBomber.util.Configuration;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

/**
 * leichtere KI, die die Laufrichtung zufällig auswählt und ändert, sobald sie nicht mehr weiter kommt. Flieht vor Bomben, nutzt PowerUps, versucht Gegner zu
 * sprengen. Wechselt manchmal zufällig die Richtung, um häufiger auch in die Mitte des Spielfelds zu laufen
 *
 * @author Unnamed#1
 */
@SuppressWarnings("serial")
public class EasyKI extends KI {
    public String Direction;
    int countDirection = 0;
    private int fieldsize = Configuration.getFieldsize();

    public EasyKI(int startx, int starty, int pnr) throws IOException, AWTException {
        super(startx, starty, pnr, "EasyAI Player" + pnr);
    }

    /**
     * setzt eine Startrichtung zum Laufen aus den möglichen Richtungen
     */
    public void setStartDirection() {
        int rand = (int) (Math.random() * (getPosssibleDirections(Calculations.listPos(this)).size()));
        System.out.println(" RandomDirection at beginning :" + rand + " possible directions : " + getPosssibleDirections(Calculations.listPos(this)).size());
        Direction = getPosssibleDirections(Calculations.listPos(this)).get(rand);
    }

    /**
     * geht, wenn möglich, in die gegebene Richtung. Versucht ansonsten zu sprengen. Wartet ansonsten, mit neuer Richtung aufgerufen zu werden
     */
    @Override
    public void moverobot() {
        countDirection++;
        int newDirection = getIntMovingDirection(Direction);
        int position = Calculations.listPos(this);
        if ((!GamePanel.isPlayer(position + newDirection) && GamePanel.isAccessible(position + newDirection))
                && (getShield() || GamePanel.isOutOfBombRadius(position + newDirection))) {
            moveDirection(Direction);

        }
        // ansonsten versuchen zu sprengen
        else if (GamePanel.isDestroyable(position + newDirection)) {
            placeNormalBomb();
            try {
                escapeBomb();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // letzte Möglichkeit: in andere Richtung weglaufen
        else {
            Direction = setDirection(Direction);
        }

        if (countDirection % 35 == 0) {
            Direction = setDirection(Direction);
            countDirection = 0;
        }
    }

    /**
     * Setzt eine neue Richtung, in die die KI zufällig geht aus den möglichen Richtungen
     *
     * @param oldDirection aktuelle Richtung, die möglichst nicht wieder gewählt werden soll
     * @return neue Richtung als String
     */
    public String setDirection(String oldDirection) {
        ArrayList<String> direction = getPosssibleAndSaveDirections(Calculations.listPos(this));

        // möglichst nicht in die gleiche Richtung, hier soll sie ja geändert werden
        if (direction.size() > 1 && direction.contains(oldDirection)) {
            direction.remove(oldDirection);
        }

        if (!direction.isEmpty()) {
            int rand = (int) (Math.random() * direction.size());
            return direction.get(rand);
        }
        return "Up";
    }

    /**
     * erstellt Liste der Richtungen, in die ein Schritt möglich (kein Block oder Bombe) ist als Strings. Beachtet nicht die Bomben in anliegenden Feldern!
     *
     * @param currPos Position, von der aus überprüft wird
     * @return ArrayList mit Strings, die die möglichen Richtungen angeben
     */
    ArrayList<String> getPosssibleDirections(int currPos) {
        ArrayList<String> direction = new ArrayList<String>();
        if (GamePanel.isAccessible(currPos - 1)) {
            direction.add("Up");
        }
        if (GamePanel.isAccessible(currPos + 1)) {
            direction.add("Down");
        }
        if (GamePanel.isAccessible(currPos + fieldsize)) {
            direction.add("Right");
        }
        if (GamePanel.isAccessible(currPos - fieldsize)) {
            direction.add("Left");
        }
        return direction;
    }

    /**
     * Identisch mit getPossibleDirections, prüft aber auch, ob Bomben im Umkreis liegen und beachtet evtl. vorhandenes Schild
     *
     * @param currPos zu überprüfende Position
     * @return ArrayList mit Strings, die die möglichen und sicheren Richtungen angeben
     */
    ArrayList<String> getPosssibleAndSaveDirections(int currPos) {
        ArrayList<String> direction = new ArrayList<String>();
        if (GamePanel.isAccessible(currPos - 1) && (getShield() || GamePanel.isOutOfBombRadius(currPos - 1))) {
            direction.add("Up");
        }
        if (GamePanel.isAccessible(currPos + 1) && (getShield() || GamePanel.isOutOfBombRadius(currPos + 1))) {
            direction.add("Down");
        }
        if (GamePanel.isAccessible(currPos + fieldsize) && (getShield() || GamePanel.isOutOfBombRadius(currPos + fieldsize))) {
            direction.add("Right");
        }
        if (GamePanel.isAccessible(currPos - fieldsize) && (getShield() || GamePanel.isOutOfBombRadius(currPos - fieldsize))) {
            direction.add("Left");
        }
        return direction;
    }

    /**
     * Bewegt den Spieler in die ihm übergebene Richtung
     *
     * @param s Richtung als String
     */
    void moveDirection(String s) {
        move(getIntMovingDirection(s));
    }

    /**
     * Gibt den Zahlenwert der Koordianten zurück, in die gegangen werden muss
     *
     * @param direction Richtung als String
     * @return Richtung als Integer
     */
    int getIntMovingDirection(String direction) {
        int IntDirechtion = 0;
        switch (direction) {
            case "Up":
                IntDirechtion = -1;
                break;
            case "Down":
                IntDirechtion = 1;
                break;
            case "Left":
                IntDirechtion = -fieldsize;
                break;
            case "Right":
                IntDirechtion = fieldsize;
                break;
        }
        return IntDirechtion;
    }

    /**
     * Geht nach Legen der Bombe zu sicherer Position. Wenn dieser erreich ist, wird gewartet.
     *
     * @throws InterruptedException
     */
    public void escapeBomb() throws InterruptedException {
        int currPos = Calculations.listPos(this);
        ArrayList<String> possibleDirections = getPosssibleDirections(currPos);

        // überprüft für alle möglichen Richtungen, ob es danach noch einen weiteren Schritt weiter weg gibt
        // (um dem Bombenradius zu entkommen). Wählt zufällig Richtungen aus (aus den möglichen, sicheren)
        for (int i = 0; i < possibleDirections.size(); i++) {
            int random = (int) (Math.random() * possibleDirections.size());
            String direction = possibleDirections.get(random);
            int newCurrPos = getIntMovingDirection(direction);

            // zweiter Schritt, bricht for-Schleife ab, falls Möglichkeit gefunden
            ArrayList<String> newPossibleDirections = getPosssibleAndSaveDirections(currPos + newCurrPos);
            if (!newPossibleDirections.isEmpty()) {
                moveDirection(direction);
                getEnemyMoveThread().sleeping();

                // neu laden, da evtl. Shield eingesammelt
                newPossibleDirections = getPosssibleAndSaveDirections(currPos + newCurrPos);
                if (!newPossibleDirections.isEmpty()) {
                    int newRandom = (int) (Math.random() * newPossibleDirections.size());
                    String newDirection = newPossibleDirections.get(newRandom);
                    moveDirection(newDirection);
                }
                break;
            }
        }
    }

    @Override
    public String toString() {
        return "EasyKI Player-" + getPlayerNR() + " X: " + getX() + " Y: " + getY() + " pos: " + Calculations.listPos(this);
    }
}