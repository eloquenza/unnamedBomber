package unnamedBomber.engine;

import unnamedBomber.sprites.Bomb;
import unnamedBomber.sprites.Sprite;
import unnamedBomber.util.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class GameObjectList {
    private List<Sprite> gameobjects;
    private int listPos;
    private int x;
    private int y;

    public GameObjectList(int x, int y, int pos) {
        gameobjects = Collections.synchronizedList(new ArrayList<Sprite>());
        listPos = pos;
        this.x = x;
        this.y = y;
    }

    public List<Sprite> getGameObjects() {
        return gameobjects;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + listPos;
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
        GameObjectList other = (GameObjectList) obj;
        return listPos == other.listPos;
    }

    public boolean bombOnTile() {
        for (Sprite sprite : gameobjects) {
            if (sprite instanceof Bomb) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        int pos = GamePanel.gameobjs.indexOf(this);
        return pos + " ";
    }

    public HashMap<GameObjectList, Integer> getAdjacentPositions() {
        int fieldsize = Configuration.getFieldsize();
        int pos = GamePanel.gameobjs.indexOf(this);
        int[] adjacentPos = {pos, pos - 1, pos + 1, pos - fieldsize, pos + fieldsize};
        HashMap<GameObjectList, Integer> ret = new HashMap<GameObjectList, Integer>(4);

        for (int i = 0; i < adjacentPos.length; i++) {
            int position = adjacentPos[i];
            if (position < 0 || position > Math.pow(fieldsize, 2)) {
                continue;
            }
            ret.put(GamePanel.gameobjs.get(position), position);
        }
        return ret;
    }

    public int getListPos() {
        return listPos;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
