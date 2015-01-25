package unnamedBomber.sprites;

import java.io.IOException;

@SuppressWarnings("serial")
public abstract class PowerUp extends Sprite {
    AbstractPlayer player;

    public PowerUp(int x, int y, String file, AbstractPlayer player) throws IOException {
        super(x, y, file);
        this.player = player;
    }

    public abstract void activate();
}
