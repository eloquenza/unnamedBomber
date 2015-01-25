package unnamedBomber.util;

import unnamedBomber.engine.GameObjectList;
import unnamedBomber.engine.GamePanel;
import unnamedBomber.sprites.Sprite;

public class Calculations {
    public static int listPos(Sprite sprite) {
        return (sprite.getX() / 64) * Configuration.getFieldsize() + (sprite.getY() / 64);
    }

    public static GameObjectList getCurrList(Sprite sprite) {
        return GamePanel.gameobjs.get(listPos(sprite));
    }

    public static int[] getOwnAndSurroundedFields() {
        return new int[]{0, 1, -1, Configuration.getFieldsize(), -Configuration.getFieldsize()};
    }
}
