package unnamedBomber.sprites;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("serial")
public class Shield extends PowerUp {
    private static final long SHIELDTIMER = 30L;

    public Shield(int x, int y, AbstractPlayer player) throws IOException {
        super(x, y, "res/Portal.png", player);
    }

    @Override
    public void activate() {
        player.setShield(true);
        startTimer(SHIELDTIMER);
    }

    public void startTimer(long time) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        try {
            scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    player.setShield(false);
                }
            }, time, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
