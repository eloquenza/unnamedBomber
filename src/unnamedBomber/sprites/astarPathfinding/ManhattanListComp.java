package unnamedBomber.sprites.astarPathfinding;

import java.util.Comparator;

public class ManhattanListComp implements Comparator<ManhattanList> {
    @Override
    public int compare(ManhattanList a, ManhattanList b) {
        int adist = a.getDistance();
        int bdist = b.getDistance();

        if (adist < bdist) {
            return -1;
        } else if (adist > bdist) {
            return 1;
        }
        return 0;
    }
}
