package unnamedBomber.sprites;

import unnamedBomber.engine.GameObjectList;
import unnamedBomber.engine.GamePanel;
import unnamedBomber.sprites.astarPathfinding.ManhattanList;
import unnamedBomber.sprites.astarPathfinding.ManhattanListComp;
import unnamedBomber.util.Calculations;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

@SuppressWarnings("serial")
public class AStarKI extends KI {

    private static final int ABSTRACTVALUE = 9999;
    private Map<ManhattanList, Boolean> bombsPlaced; // BombSpot, bombPlaced
    private ArrayDeque<GameObjectList> path; // berechneter weg
    private AbstractPlayer nearestPlayer; // nächster/anvisierter Spieler
    private ConcurrentSkipListSet<ManhattanList> manhattanList; // Liste zur Berechnung von Aktionen, enthält alle erreichbaren Spots mit distanz zum
    // nearestPlayer
    private ManhattanList bombSpot;
    private GameObjectList safeSpot;
    private ScheduledExecutorService scheduler;

    public AStarKI(int startx, int starty, int pnr) throws IOException {
        super(startx, starty, pnr, "AStarKI Player" + pnr);
        bombsPlaced = new ConcurrentHashMap<ManhattanList, Boolean>();
        path = new ArrayDeque<GameObjectList>();
        manhattanList = new ConcurrentSkipListSet<ManhattanList>();
        scheduler = Executors.newScheduledThreadPool(1);
        calcNewDistances();
    }

    @Override
    public void moverobot() {
        getNearestPlayer();
        getAccessibleTiles();
        // System.out.println("Ich bin bei: " + Calculations.listPos(this));
        // System.out.println(this + " BombSpots: " + bombsPlaced);
        if (!GamePanel.isOutOfBombRadius(Calculations.listPos(this))) {
            // System.out.println("RUNNING FROM BOMB!");
            escapePath();
        } else {
            scanForBombs();
            if (!playerPath()) {
                calcBombSpots();
                if (!bombsPlaced.isEmpty()) {
                    findBombPath();
                }
            } else {
                bombsPlaced.clear();
                calcBombSpots();
                bombSpot = getShortestBombSpot();
            }
            // System.out.println(path);
            findPowerUp(); // nur ausführen, wenn nicht gerade in extra-Schleife von aStar, weil dort auch einfach ein Schritt manuell an die erste Stelle des
            // Paths hinzugefügt wird? Hab gerade mal Überprüfung in findPowerUp hinzugefügt, vielleicht reicht das, needs testing
        }
        moveThroughPath();
        if (bombSpot != null) {
            placeBomb();
        }
    }

    private ManhattanList getShortestBombSpot() {
        int lowestDis = ABSTRACTVALUE;
        ManhattanList bSpot = null;
        for (ManhattanList mList : bombsPlaced.keySet()) {
            int mListDis = mList.getDistance();
            // System.out.println("LOWEST: " + lowestDis + " mList: " + mList + " mListDis: " + mListDis);
            if (mListDis < lowestDis) {
                lowestDis = mListDis;
                bSpot = mList;
            }
        }
        return bSpot;
    }

    private void getAccessibleTiles() {
        if (!(manhattanList.size() == (Math.pow(GamePanel.gamesize, 2) - Math.pow(GamePanel.gamesize / 2, 2)))) {
            new Thread() {
                @Override
                public void run() {
                    for (ManhattanList mList : manhattanList) {
                        for (GameObjectList gObjList : mList.gameobj.getAdjacentPositions().keySet()) {
                            if (GamePanel.isAccessible(gObjList.getListPos())) {
                                ManhattanList newList = new ManhattanList(gObjList, 0);
                                if (!manhattanList.contains(newList)) {
                                    newList.setDistance(Calculations.getCurrList(nearestPlayer));
                                    manhattanList.add(newList);
                                }
                            }
                        }
                    }
                }
            }.start();
        }
    }

    private void calcNewDistances() {
        Runnable calc = new Runnable() {
            @Override
            public void run() {
                // System.out.println("I AM CALCULATING THE DISTANCES");
                for (ManhattanList mList : manhattanList) {
                    mList.setDistance(Calculations.getCurrList(nearestPlayer));
                }
            }
        };
        scheduler.scheduleAtFixedRate(calc, 500L, 500L, TimeUnit.MILLISECONDS);
    }

    public void stopDistanceCalcs() {
        scheduler.shutdown();
    }

    /**
     * Scanned alle selbstgelegten bomben ab, ob sie bereits explodiert sind und löscht diese dann aus der Liste.
     */
    public void scanForBombs() {
        for (ManhattanList gObjList : bombsPlaced.keySet()) {
            try {
                if (!gObjList.gameobj.bombOnTile() && bombsPlaced.get(gObjList)) {
                    // System.out.println("deleting bombSpot!");
                    bombsPlaced.remove(gObjList);
                }
            } catch (NullPointerException excep) {
                excep.printStackTrace();
                System.out.println("bombsPlaced: " + bombsPlaced);
                System.out.println("gObjList: " + gObjList);
                System.out.println("bombsPlaced.get(ObjList): " + bombsPlaced.get(gObjList));
                System.out.println("gObjList.gameobj.bombOnTile() " + !gObjList.gameobj.bombOnTile());
            }
        }
    }

    /**
     * guckt, ob PowerUp auf einem benachbarten Feld und fügt das Feld dem Path hinzu, wenn nicht durch Bombe gefährdet Maximal ein PowerUp
     */
    private void findPowerUp() {
        GameObjectList curr = GamePanel.gameobjs.get(Calculations.listPos(this));
        for (GameObjectList adjacent : curr.getAdjacentPositions().keySet()) {
            if (GamePanel.hasPowerUp(adjacent.getListPos()) && GamePanel.isOutOfBombRadius(adjacent.getListPos())) {
                if (!path.contains(adjacent)) {
                    path.addFirst(adjacent);
                    // System.out.println("FOUND POWERUP NEAR ME: " + path);
                    break;
                }
            }
        }
    }

    /**
     * Sucht sich einen safeSpot anhand der kürzesten Distanz zum Spieler, um in erreichbarer Nähe zu sein, um die nächste Bombe schneller legen zu können.
     */
    private void escapePath() {
        ArrayList<ManhattanList> modList = new ArrayList<ManhattanList>();
        modList.addAll(manhattanList);
        modList.sort(new ManhattanListComp());
        do {
            GameObjectList safeSpot = modList.get(0).gameobj;
            this.safeSpot = safeSpot;
            if (GamePanel.isOutOfBombRadius(safeSpot.getListPos())) {
                if (aStarSearch(GamePanel.gameobjs.get(Calculations.listPos(this)), safeSpot, false) == 0) {
                    // System.out.println("The safespot is at " + safeSpot);
                    break;
                }
            }
            modList.remove(modList.get(0));
        } while (!modList.isEmpty());
    }

    /**
     * Berechnet den Path über aStarSearch zum nächsten Bombenspot
     */
    private void findBombPath() {
        ArrayList<ManhattanList> tosort = new ArrayList<ManhattanList>();
        tosort.addAll(manhattanList);
        tosort.sort(new ManhattanListComp());
        for (ManhattanList bombSpot : tosort) {
            if (bombsPlaced.get(bombSpot) != null && !bombsPlaced.get(bombSpot)) {
                if (aStarSearch(GamePanel.gameobjs.get(Calculations.listPos(this)), bombSpot.gameobj, false) == -1) {
                    continue;
                } else {
                    // System.out.println("Going to my bombSpot: " + bombSpot);
                    this.bombSpot = bombSpot;
                    break;
                }
            }
        }
    }

    /**
     * Berechnet alle Bombenspots, um sich einen Path zu einem Spieler freizubomben, anhand der kürzesten Manhattandistanz zum Spieler.
     */
    private void calcBombSpots() {
        ArrayList<ManhattanList> modList = new ArrayList<ManhattanList>();
        modList.addAll(manhattanList);
        modList.sort(new ManhattanListComp());
        // System.out.println("modListCalcBombs: " + modList);
        ManhattanList bombSpot = null;
        while (!modList.isEmpty() && bombsPlaced.size() < getMaxBombs()) {
            bombSpot = modList.get(0);
            for (GameObjectList adjacent : bombSpot.gameobj.getAdjacentPositions().keySet()) {
                if (GamePanel.isDestroyableOtherPlayer(adjacent.getListPos(), this)) {
                    if (!bombsPlaced.containsKey(bombSpot)) {
                        bombsPlaced.put(bombSpot, false);
                    }
                }
            }
            modList.remove(bombSpot);
        }
    }

    /**
     * geht die Liste der BombSpots durch und guckt, ob auf diesem Spot. Legt dann die Bombe und markiert sie als gelegt.
     */
    private void placeBomb() {
        for (ManhattanList gObjList : bombsPlaced.keySet()) {
            if (gObjList.gameobj.getListPos() == Calculations.listPos(this)) {
                bombsPlaced.replace(gObjList, true);
                placeNormalBomb();
            }
        }
    }

    /**
     * Berechnet Pfad zum NÄCHSTEN Spieler.
     *
     * @return boolean, je nachdem ob der path gefunden wurde.
     */
    private boolean playerPath() {
        return aStarSearch(GamePanel.gameobjs.get(Calculations.listPos(this)), GamePanel.gameobjs.get(Calculations.listPos(nearestPlayer)), true) == 0;
    }

    /**
     * Baut einen Path von startPos zu curr auf über die Liste cameFrom, die in aStarSearch berechnet wird
     *
     * @param curr     Endposition - gewünsches Ziel; Spieler oder BombSpot
     * @param startPos Startposition des Spielers
     * @param cameFrom Map von Liste zu Liste
     */
    private void buildPath(GameObjectList curr, GameObjectList startPos, Map<GameObjectList, GameObjectList> cameFrom) {
        // System.out.println("came from: " + cameFrom);
        while (!curr.equals(startPos)) {
            path.addFirst(curr);
            curr = cameFrom.get(curr);
        }
        // System.out.println("path: " + path);
    }

    /**
     * Berechnet das niedrigste F - siehe f() A* algorithm - und gibt dem entsprechend die Liste ist.
     *
     * @param set Set, aus dem die Listen gewählt werden sollen.
     * @param f   Heuristicfunktion, hier Manhattandistanz, der Listen zum gewünschten Ziel
     * @return GameObjectlist mit kürzester Distanz zum gewünschen Objekt
     */
    private GameObjectList getLowestF(Set<GameObjectList> set, Map<GameObjectList, Integer> f) {
        GameObjectList ret = null;
        int fscore = -1;
        // System.out.println("getLowestF set: " + set);
        // System.out.println("getLowestF f: " + f);
        for (GameObjectList gol : set) {
            if (!f.containsKey(gol)) {
                continue;
            }
            if (fscore == -1) {
                fscore = f.get(gol);
                ret = gol;
            } else {
                int tmp = f.get(gol);
                if (tmp < fscore) {
                    fscore = tmp;
                    ret = gol;
                }
            }
        }
        // System.out.println("this is the lowest i could find: " + ret);
        return ret;
    }

    private int aStarSearch(GameObjectList startPos, GameObjectList finalPos, boolean lookingForPlayer) {
        int path = aStarSearchB(startPos, finalPos, lookingForPlayer);

        // wenn kein Path vom aktuellen Feld gefunden (weil z.B. alle eigenen Nachbarfelder bedroht), von den Nachbarfeldern aus nach einem sicheren Ort suchen
        // und dann möglichst zwei Schritte gehen.
        // Allerdings nur, wenn nicht schon auf einem safe Spot, denn dann würde die KI nur unnötig kurz in unsicheres Feld laufen
        if (path == -1 && !lookingForPlayer && (safeSpot.getListPos() != Calculations.listPos(this))) {
            for (GameObjectList adjac : startPos.getAdjacentPositions().keySet()) {
                if (GamePanel.isAccessible(adjac.getListPos())) {
                    if (aStarSearchB(adjac, finalPos, lookingForPlayer) == 0) {
                        this.path.addFirst(adjac);
                        return 0;
                    }
                }
            }
            return -1;
        }
        return path;
    }

    /**
     * aStarSearch berechnet einen Path von startPos zu finalPos, mithilfe des a* algorithm. Dabei werden die benachbarten Tiles besucht, nach open / closed
     * sortiert, je nach dem, ob sie bereits besucht wurde oder nicht und dementsprechend alle Tiles abgegangen, bis path gefunden oder nicht.
     *
     * @param startPos
     * @param finalPos
     * @param lookingForPlayer
     * @return Integer, ob Path berechnet wurde oder nicht.
     */
    private int aStarSearchB(GameObjectList startPos, GameObjectList finalPos, boolean lookingForPlayer) {
        // System.out.println("I am at: " + startPos + " and looking for a way to " + finalPos);
        HashMap<GameObjectList, GameObjectList> cameFrom = new HashMap<GameObjectList, GameObjectList>();
        HashMap<GameObjectList, Integer> g = new HashMap<GameObjectList, Integer>();
        HashMap<GameObjectList, Integer> f = new HashMap<GameObjectList, Integer>();
        HashSet<GameObjectList> closed = new HashSet<GameObjectList>();
        HashSet<GameObjectList> open = new HashSet<GameObjectList>();
        path = new ArrayDeque<GameObjectList>();

        open.add(startPos);
        g.put(startPos, 0);
        f.put(startPos, getManhattanDistance(startPos, finalPos));
        if (lookingForPlayer) {
            manhattanList.add(new ManhattanList(startPos, getManhattanDistance(startPos, finalPos)));
        }

        while (!open.isEmpty()) {
            GameObjectList curr = getLowestF(open, f);

            if (curr.equals(finalPos) && GamePanel.isOutOfBombRadius(curr.getListPos())) {
                buildPath(curr, startPos, cameFrom);
                return 0;
            }

            open.remove(curr);
            closed.add(curr);
            for (GameObjectList adjacent : curr.getAdjacentPositions().keySet()) {
                if (!GamePanel.isAccessible(adjacent.getListPos()) || !GamePanel.isOutOfBombRadius(adjacent.getListPos())) {
                    closed.add(adjacent);
                }
                if (closed.contains(adjacent)) {
                    continue;
                }
                int pastPathCost = getManhattanDistance(curr, adjacent);
                g.put(adjacent, pastPathCost);
                int prelimG = g.get(curr) + pastPathCost;
                // System.out.println(curr + " ist von " + adjacent + " " + prelimG + "entfernt");
                // System.out.println("prelimG: " + prelimG + " manhattandistance: " + getManhattanDistance(curr, adjacent));
                boolean trigger = false;
                if (!open.contains(adjacent)) {
                    if (lookingForPlayer) {
                        manhattanList.add(new ManhattanList(adjacent, getManhattanDistance(adjacent, finalPos)));
                    }
                    open.add(adjacent);
                    trigger = true;
                } else if (prelimG < g.get(adjacent)) {
                    trigger = true;
                }
                if (trigger) {
                    cameFrom.put(adjacent, curr);
                    g.put(adjacent, prelimG);
                    f.put(adjacent, prelimG + getManhattanDistance(adjacent, finalPos));
                }
            }
        }
        // System.out.println("HALLO ICH FINDE KEINEN PATH!");
        return -1;
    }

    /**
     * Returned den nächsten Spieler, berechnet über Manhattan.
     *
     * @return nächstliegender Spieler
     */
    private void getNearestPlayer() {
        Iterator<AbstractPlayer> playerIter = GamePanel.players.values().iterator();
        int nearest = ABSTRACTVALUE;
        nearestPlayer = null;
        while (playerIter.hasNext()) {
            AbstractPlayer curr = playerIter.next();
            if (!curr.equals(this)) {
                int tmp = getManhattanDistance(curr);
                if (tmp < nearest) {
                    nearest = tmp;
                    nearestPlayer = curr;
                }
            }
        }
    }

    /**
     * Bewegt sich durch den Path, der berechnet wurde, um jeweils ein Feld durch einen path.poll() (das selbe wie push());
     */
    private void moveThroughPath() {
        if (!path.isEmpty()) {
            GameObjectList nextSpot = path.poll();
            move(GamePanel.gameobjs.indexOf(nextSpot) - Calculations.listPos(this));
        }
    }

    public int getManhattanDistance(GameObjectList a, GameObjectList b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    public int getManhattanDistance(AbstractPlayer other) {
        return Math.abs(this.getX() - other.getX()) + Math.abs(this.getY() - other.getY());
    }

    @Override
    public String toString() {
        return "AStarKI Player-" + getPlayerNR() + " X: " + getX() + " Y: " + getY() + " pos: " + Calculations.listPos(this);
    }
}
