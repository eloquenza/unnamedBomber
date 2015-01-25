package unnamedBomber.sprites;

import java.io.IOException;

@SuppressWarnings("serial")
public class Multibomb extends PowerUp {
    public Multibomb(int x, int y, AbstractPlayer player) throws IOException {
        super(x, y, "res/BombPowerup.png", player);
    }

    @Override
    public void activate() {
        player.incrMaxBombs();
    }
}
