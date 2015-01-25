package unnamedBomber.sprites.astarPathfinding;

import unnamedBomber.engine.GameObjectList;

public class ManhattanList implements Comparable<ManhattanList> {
    public GameObjectList gameobj;
    private int distance;

    public ManhattanList(GameObjectList gameobj, int distance) {
        this.gameobj = gameobj;
        this.distance = distance;
    }

    public ManhattanList() {
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(GameObjectList other) {
        distance = Math.abs(gameobj.getX() - other.getX()) + Math.abs(gameobj.getY() - other.getY());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((gameobj == null) ? 0 : gameobj.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ManhattanList other = (ManhattanList) obj;
        if (gameobj == null) {
            if (other.gameobj != null)
                return false;
        } else if (!gameobj.equals(other.gameobj))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "distance: " + distance + " | pos: " + gameobj/* + " | " + hashCode() */;
    }

    @Override
    public int compareTo(ManhattanList o) {
        int thisPos = gameobj.getListPos();
        int otherPos = o.gameobj.getListPos();
        if (thisPos < otherPos) {
            return -1;
        } else if (thisPos > otherPos) {
            return 1;
        }
        return 0;
    }
}