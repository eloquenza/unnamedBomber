package unnamedBomber.engine;

import unnamedBomber.sound.Sound;
import unnamedBomber.sprites.*;
import unnamedBomber.sprites.ai.MoveEnemyThread;
import unnamedBomber.util.Configuration;

import javax.sound.sampled.FloatControl;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("serial")
public class GamePanel extends JPanel implements KeyListener, Runnable {
    private static final int NO_DELAYS_PER_YIELD = 16;
    private static final int DEFAULT_FPS = 120;
    // Attribute zur Erstellung aller Sprites sowie Zeichnen der Sprites, Übersicht der Sprites etc.
    public static int gamesize;
    public static int fieldsize;
    public volatile static List<GameObjectList> gameobjs;
    public volatile static List<ExplosionFlames> flames;
    // Attribute für SpielerErstellung / Player Event Processing
    public static String[] playerTypes = {"Spieler", "Spieler", "Spieler", "Spieler"};
    public static ConcurrentHashMap<Integer, AbstractPlayer> players;
    private static int MAX_FRAME_SKIPS = 5;
    private static ArrayList<Tile> tiles;
    private static BitSet keyset;
    private volatile static boolean gameOver;
    // Attribute für die Statistiken zur Berechnung und Angabe für Avg. FPS/UPS etc.
    private static long MAX_STATS_INTERVAL = 1000L;
    private static int NUM_FPS = 10;
    // boolean zum Ton ein/ ausschalten
    private static boolean soundOn = true;
    // Sound attribut
    Path path = Paths.get("").toAbsolutePath();
    Sound sound = new Sound(Paths.get(path.toString(),"res/testsound.wav").toString());
    Sound win = new Sound(Paths.get(path.toString(),"res/tada.wav").toString());
    private long period;
    // Attribute für die gameLoop() sowie Berechnung des Bildes
    private Thread animate;
    private volatile boolean running = false;
    private volatile boolean paused = false;
    private Graphics2D doubBuffGraphics2D;
    private Image doubBuffImage;
    private long statsInterval = 0L;
    private long prevStatsTime;
    private long gameStartTime;
    private int timeSpentInGame = 0;
    private long totalElapsedTime = 0L;
    private long frameCount = 0;
    private double fpsStore[];
    private double upsStore[];
    private long statsCount = 0;
    private double averageFPS = 0.0;
    private double averageUPS = 0.0;
    private DecimalFormat df = new DecimalFormat("0.##");
    // private DecimalFormat timedf = new DecimalFormat("0.####");
    private long framesSkipped = 0L;
    private long totalFramesSkipped = 0L;
    // attribute für die highscores
    private Board root;
    // attribute zur absicherung, dass die siegernachricht abgespielt wird
    private boolean endMsgDisplayed = false;

    public GamePanel(final Board root) throws IOException, AWTException {
        gamesize = Configuration.getSize();
        fieldsize = Configuration.getFieldsize();
        players = new ConcurrentHashMap<Integer, AbstractPlayer>(4);
        keyset = new BitSet(256);
        tiles = new ArrayList<Tile>();
        gameobjs = Collections.synchronizedList(new ArrayList<GameObjectList>());
        flames = Collections.synchronizedList(new ArrayList<ExplosionFlames>());
        gameOver = false;
        running = false;
        this.root = root;
        period = (long) (1000 / DEFAULT_FPS) * 1000000L;

        Configuration.setPrefW(fieldsize * Configuration.IMAGESIZE);
        Configuration.setPrefH(fieldsize * Configuration.IMAGESIZE);

        Dimension dim = new Dimension(Configuration.getPrefW(), Configuration.getPrefH());
        setPreferredSize(dim);

        setBackground(Color.BLACK);

        setFocusable(true);
        requestFocus();

        FloatControl gainControl = (FloatControl) sound.c.getControl(FloatControl.Type.MASTER_GAIN);
        double gain = .1D; // number between 0 and 1 (loudest)
        float dB = (float) (Math.log(gain) / Math.log(10.0) * 20.0);
        gainControl.setValue(dB);
        if (soundOn)
            sound.soundstartloop();

        addPauseKey();
        addKeyListener(this);

        initGameObjsList();
        createGameObjects();
        createTiles();

        startKI();

        fpsStore = new double[NUM_FPS];
        upsStore = new double[NUM_FPS];
        for (int i = 0; i < NUM_FPS; i++) {
            fpsStore[i] = 0.0;
            upsStore[i] = 0.0;
        }
    }

    /**
     * Wenn an gegebener Position ein SpielerObjekt, dann true. Sonst false.
     *
     * @param listpos
     * @return
     */
    public static boolean isPlayer(int listpos) {
        for (Sprite obj : getSprites(listpos)) {
            if (obj instanceof AbstractPlayer)
                return true;
        }
        return false;
    }

    public static boolean isTile(int listpos) {
        for (Sprite sprite : getSprites(listpos)) {
            if (sprite instanceof UndestructableBlock) {
                return true;
            } else {
                System.out.println("Sprite : " + sprite);
            }
        }
        return false;
    }

    /**
     * Accessible true, wenn nächster Platz leer / PowerUp ist. Sonst false.
     */
    public static boolean isAccessible(int listpos) {
        if (getSprites(listpos).isEmpty()) {
            return true;
        } else {
            for (Sprite obj : getSprites(listpos)) {
                if (obj instanceof AbstractPlayer || obj instanceof PowerUp) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * überprüft, ob im Radius der übergebenen Position keine Bombe liegt
     *
     * @param position zu überprüfende Position
     * @return true, falls keine Bombe in der Nähe
     */
    public static boolean isOutOfBombRadius(int position) {
        int[] directions = {-1, 0, 1, -fieldsize, fieldsize};
        for (int i = 0; i < directions.length; i++) {
            int newPos = position + directions[i];
            if (newPos < 0 || newPos >= Math.pow(fieldsize, 2)) {
                System.out.println("NOT A VALID POSITION!");
                continue;
            }
            for (Sprite obj : getSprites(newPos)) {
                if (obj instanceof Bomb) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Destroyable true, wenn Player/Removeableblock ist, sonst false
     */
    public static boolean isDestroyable(int listpos) {
        for (Sprite obj : getSprites(listpos)) {
            if (obj instanceof RemovableBlock || obj instanceof AbstractPlayer) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDestroyableOtherPlayer(int listpos, Sprite sprite) {
        for (Sprite obj : getSprites(listpos)) {
            if (!obj.equals(sprite) && obj instanceof AbstractPlayer) {
                return true;
            }
            if (obj instanceof RemovableBlock) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasPowerUp(int listpos) {
        for (Sprite obj : getSprites(listpos)) {
            if (obj instanceof PowerUp) {
                return true;
            }
        }
        return false;
    }

    public static BitSet getKeySet() {
        return keyset;
    }

    public static void killsound() {
        Sound scream = new Sound("res/scream.wav");
        FloatControl gainControl = (FloatControl) scream.c.getControl(FloatControl.Type.MASTER_GAIN);
        double gain = 1.0D; // number between 0 and 1 (loudest)
        float dB = (float) (Math.log(gain) / Math.log(10.0) * 20.0);
        gainControl.setValue(dB);
        scream.start();
    }

    public static void kill(Sprite gobj) {
        if (gobj instanceof KI) {
            killKIThread((KI) gobj);
        }
        // hoffentlich threadsafe variante spieler aus der list zu löschen
        synchronized (players) {
            Iterator<AbstractPlayer> pIter = players.values().iterator();
            while (pIter.hasNext()) {
                AbstractPlayer p = pIter.next();
                if (p.equals(gobj)) {
                    pIter.remove();
                    killsound();
                    break;
                }
            }
        }
        if (players.size() <= 1) {
            performGameOver();
        }
    }

    private static void performGameOver() {
        gameOver = true;
        //
        // // nur möglich, da wir bereits players.size() == 1 abfragen, bevor wir hier reingehen
        // endAllKIThreads();
    }

    private static void endAllKIThreads() {
        Iterator<AbstractPlayer> pIter = players.values().iterator();
        while (pIter.hasNext()) {
            AbstractPlayer p = pIter.next();
            if (p instanceof KI) {
                killKIThread((KI) p);
            }
        }
    }

    private static void killKIThread(KI ki) {
        MoveEnemyThread met = ki.getEnemyMoveThread();
        met.setRobot(null);
        met.interrupt();
        try {
            met.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ki.setEnemyMoveThread(null);
    }

    public static void setSound(boolean sound) {
        soundOn = sound;
    }

    public static boolean isPressed(final int keycode) {
        return keyset.get(keycode);
    }

    public static void keyPress(int keycode) {
        keyset.set(keycode);
    }

    public static void keyRelease(int keycode) {
        keyset.clear(keycode);
    }

    public static List<Sprite> getSprites(int index) {
        return GamePanel.gameobjs.get(index).getGameObjects();
    }

    /**
     * Beenden der GamePanel beschleunigen.
     */
    public void endGame() {
        sound.soundstop();
        running = false;
        gameOver = true;
        paused = false;
        doubBuffGraphics2D = null;
        doubBuffImage = null;
        players = null;
        gameobjs = null;
        tiles = null;
        keyset = null;
        root = null;
        animate = null;
    }

    private void addPauseKey() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_ESCAPE) {
                    stopping();
                }
            }
        });
    }

    public void pauseGame() {
        paused = true;
    }

    public void resumeGame() {
        paused = false;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        starting();
    }

    private void starting() {
        if (animate == null || !running) {
            animate = new Thread(this);
            animate.setName("AnimationThread");
            animate.start();
        }
    }

    public void stopping() {
        running = false;
    }

    @Override
    public void run() {
        running = true;
        long before, after, diff, sleep;
        long overSleep = 0L;
        int noDelays = 0;
        long excess = 0L;

        gameStartTime = System.nanoTime();
        prevStatsTime = gameStartTime;
        before = gameStartTime;

        while (running) {
            updateGame();
            renderGame();
            painting();

            after = System.nanoTime();
            diff = after - before;
            sleep = (period - diff) - overSleep;

            if (sleep > 0) {
                try {
                    Thread.sleep(sleep / 1000000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                overSleep = (System.nanoTime() - after) - sleep;
            } else {
                excess -= sleep;
                overSleep = 0L;

                if (++noDelays >= NO_DELAYS_PER_YIELD) {
                    Thread.yield();
                    noDelays = 0;
                }
            }
            before = System.nanoTime();

            int skips = 0;
            while ((excess > period) && (skips < MAX_FRAME_SKIPS)) {
                excess -= period;
                updateGame();
                skips++;
            }
            framesSkipped += skips;
            storeStats();
            if (gameOver) {
                break;
            }
        }
        if (!endMsgDisplayed) {
            renderGame();
            painting();
        }
        endKIDistanceCalcs();
        endAllKIThreads();
        printStats();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                boolean winnerIsHuman = (players.get(players.keys().nextElement()) instanceof HumanPlayer) ? true : false;
                root.gameOver(timeSpentInGame, winnerIsHuman);
            }
        }, 5L, TimeUnit.SECONDS);
    }

    private void endKIDistanceCalcs() {
        for (AbstractPlayer player : players.values()) {
            if (player instanceof AStarKI) {
                ((AStarKI) player).stopDistanceCalcs();
            }
        }
    }

    @SuppressWarnings("unused")
    private void printStats() {
        System.out.println("Frame Count/Loss: " + frameCount + " / " + totalFramesSkipped);
        System.out.println("Average FPS: " + df.format(averageFPS));
        System.out.println("Average UPS: " + df.format(averageUPS));
        System.out.println("Time Spent: " + timeSpentInGame + " secs");
    }

    private void storeStats() {
        frameCount++;
        statsInterval += period;

        if (statsInterval >= MAX_STATS_INTERVAL) {
            long timeNow = System.nanoTime();
            timeSpentInGame = (int) ((timeNow - gameStartTime) / 1000000000L);

            long realElaspedTime = timeNow - prevStatsTime;
            totalElapsedTime += realElaspedTime;
            totalFramesSkipped += framesSkipped;

            double actualFPS = 0;
            double actualUPS = 0;
            if (totalElapsedTime > 0) {
                actualFPS = (((double) frameCount / totalElapsedTime) * 1000000000L);
                actualUPS = (((double) (frameCount + totalFramesSkipped) / totalElapsedTime) * 1000000000L);
            }
            fpsStore[(int) statsCount % NUM_FPS] = actualFPS;
            upsStore[(int) statsCount % NUM_FPS] = actualUPS;
            statsCount = statsCount + 1;

            double totalFPS = 0.0;
            double totalUPS = 0.0;
            for (int i = 0; i < NUM_FPS; i++) {
                totalFPS += fpsStore[i];
                totalUPS += upsStore[i];
            }
            if (statsCount < NUM_FPS) {
                averageFPS = totalFPS / statsCount;
                averageUPS = totalUPS / statsCount;
            } else {
                averageFPS = totalFPS / NUM_FPS;
                averageUPS = totalUPS / NUM_FPS;
            }

            // System.out.println(
            // timedf.format((double) statsInterval/1000000000L) + " " +
            // timedf.format((double) realElaspedTime/1000000000L) + "s " +
            // df.format(timingError) + "% " +
            // frameCount + "c " +
            // framesSkipped + "/" + totalFramesSkipped + " skip; " +
            // df.format(actualFPS) + " " + df.format(averageFPS) + " afps; " +
            // df.format(actualUPS) + " " + df.format(averageUPS) + " aups");

            framesSkipped = 0;
            prevStatsTime = timeNow;
            statsInterval = 0L;
        }
    }

    /**
     * wenn nicht pausiert oder beendet werden die Eingaben der Spieler ausgewertet. KIs können nicht gesteuert werden.
     */
    private void updateGame() {
        if (!paused && !gameOver) {
            processPlayerKeys();
        }
    }

    private void processPlayerKeys() {
        synchronized (players) {
            Iterator<AbstractPlayer> pIter = players.values().iterator();
            while (pIter.hasNext()) {
                AbstractPlayer p = pIter.next();
                if (p instanceof HumanPlayer) {
                    ((HumanPlayer) p).performAction();
                }
            }
        }
    }

    /**
     * Zeichnet alle unseren Spielobjekte
     */
    private void renderGame() {
        if (doubBuffImage == null) {
            doubBuffImage = createImage(Configuration.getPrefW(), Configuration.getPrefH());
            if (doubBuffImage == null) {
                System.out.println("doubBuffImage == NULL!");
                return;
            } else {
                doubBuffGraphics2D = (Graphics2D) doubBuffImage.getGraphics();
            }
        }

        doubBuffGraphics2D.setColor(Color.BLACK);
        doubBuffGraphics2D.fillRect(0, 0, Configuration.getPrefW(), Configuration.getPrefH());

        for (Tile tile : tiles) {
            doubBuffGraphics2D.drawImage(tile.getImage(), tile.getX(), tile.getY(), tile);
        }

        Iterator<GameObjectList> gmobjIter = gameobjs.iterator();
        while (gmobjIter.hasNext()) {
            List<Sprite> spriteList = gmobjIter.next().getGameObjects();
            synchronized (spriteList) {
                Iterator<Sprite> spriteListIter = spriteList.iterator();
                while (spriteListIter.hasNext()) {
                    Sprite cur = spriteListIter.next();
                    if (!(cur instanceof AbstractPlayer)) {
                        doubBuffGraphics2D.drawImage(cur.getImage(), cur.getX(), cur.getY(), cur);
                    }
                }
            }
        }

        doubBuffGraphics2D.setColor(Color.GREEN);

        gmobjIter = gameobjs.iterator();
        while (gmobjIter.hasNext()) {
            List<Sprite> spriteList = gmobjIter.next().getGameObjects();
            synchronized (spriteList) {
                Iterator<Sprite> spriteListIter = spriteList.iterator();
                while (spriteListIter.hasNext()) {
                    Sprite cur = spriteListIter.next();
                    if (cur instanceof AbstractPlayer) {
                        doubBuffGraphics2D.drawImage(cur.getImage(), cur.getX(), cur.getY() - 64, cur);
                    }
                }
            }
        }

        Iterator<ExplosionFlames> flamesIter = flames.iterator();
        synchronized (flames) {
            while (flamesIter.hasNext()) {
                Sprite curr = flamesIter.next();
                doubBuffGraphics2D.drawImage(curr.getImage(), curr.getX(), curr.getY(), curr);
            }
        }

        if (gameOver) {
            endMessage();
        }
    }

    private void painting() {
        Graphics g;
        try {
            g = this.getGraphics();
            if (g != null && doubBuffImage != null) {
                g.drawImage(doubBuffImage, 0, 0, null);
            }

            Toolkit.getDefaultToolkit().sync();
            g.dispose();
        } catch (Exception e) {
            System.out.println("Error in painting(): " + e);
        }
    }

    private void endMessage() {
        endMsgDisplayed = true;
        win.start();

        // Textlayout
        double basefontsize = 60.0;
        double basevalue = 1000.0;
        double factor = ((double) Configuration.getPrefH() / basevalue);
        int fontsize = (int) (basefontsize * factor);
        Font f = new Font("Arial", Font.BOLD, fontsize);
        String text = "";
        try {
            text = "SPIELER " + players.get(players.keys().nextElement()).getPlayerNR() + " HAT GEWONNEN!";
        } catch (NoSuchElementException e) {
            text = "KEINER HAT GEWONNEN!";
        }

        TextLayout textLayout = new TextLayout(text, f, doubBuffGraphics2D.getFontRenderContext());

        Rectangle2D bounds = textLayout.getBounds();
        int x = (getWidth() - (int) bounds.getWidth()) / 2;
        int y = (int) ((getHeight() - (bounds.getHeight() - textLayout.getDescent())) / 2);
        y += textLayout.getAscent() - textLayout.getDescent();

        doubBuffGraphics2D.setPaint(new Color(146, 9, 33, 150)); // Schattenfarbe
        textLayout.draw(doubBuffGraphics2D, x + 3, y + 3);
        doubBuffGraphics2D.setPaint(new Color(238, 15, 54, 200)); // schriftfarbe
        textLayout.draw(doubBuffGraphics2D, x, y);
    }

    @SuppressWarnings("unused")
    private void printGameObjsList() {
        for (int i = 0; i < fieldsize * fieldsize; i++) {
            if (i % fieldsize == 0 && i != 0) {
                System.out.println(i + ": " + gameobjs.get(i));
            }
            System.out.print(i + ": " + gameobjs.get(i));
        }
        System.out.println();
    }

    private void initGameObjsList() {
        for (int i = 0; i < fieldsize * fieldsize; i++) {
            gameobjs.add(new GameObjectList((i / fieldsize) * 64, (i % fieldsize) * 64, i));
        }
    }

    /**
     * Im Konstruktoraufruf ausgeführt. Füllt alle Spielobjektbezogene ArrayLists.
     *
     * @throws IOException
     * @throws AWTException
     */
    private void createGameObjects() throws IOException, AWTException {
        int pnr = 0;

        for (int i = 0; i < fieldsize * fieldsize; i++) {
            if (((i / fieldsize) % 2 == 0 && (i % fieldsize) % 2 == 0 || i / fieldsize == 0 || i % fieldsize == 0 || i / fieldsize == gamesize + 1 || i
                    % fieldsize == gamesize + 1)) {
                getSprites(i).add(new UndestructableBlock((i / fieldsize) * 64, (i % fieldsize) * 64));
            } else if ((i / fieldsize == 1 || i / fieldsize == gamesize)
                    && (i % fieldsize == 1 || i % fieldsize == 2 || i % fieldsize == gamesize - 1 || i % fieldsize == gamesize)
                    || (i / fieldsize == 2 || i / fieldsize == gamesize - 1) && (i % fieldsize == 1 || i % fieldsize == gamesize)) {
                // nichts tun, da diese Plätze zu 100% frei sein müssen
            } else {
                if (Math.random() < 0.8) {
                    getSprites(i).add(new RemovableBlock((i / fieldsize) * 64, (i % fieldsize) * 64));
                }
            }
            if (i / fieldsize == 1 && i % fieldsize == 1 || i / fieldsize == 1 && i % fieldsize == gamesize || i / fieldsize == gamesize && i % fieldsize == 1
                    || i / fieldsize == gamesize && i % fieldsize == gamesize) {
                createPlayers(i, pnr);
                pnr++;
            }
        }
    }

    private void createPlayers(int i, int pnr) throws IOException, AWTException {
        AbstractPlayer p = null;
        switch (playerTypes[pnr]) {
            case "Spieler":
                p = new HumanPlayer((i / fieldsize) * 64, ((i % fieldsize)) * 64, pnr);
                break;
            case "Bot - einfach":
                p = new EasyKI((i / fieldsize) * 64, ((i % fieldsize)) * 64, pnr);
                break;
            case "Bot - schwer":
                p = new AStarKI((i / fieldsize) * 64, ((i % fieldsize)) * 64, pnr);
                break;
            case "freier Slot":
                break;
        }
        if (p != null) {
            getSprites(i).add(p);
            players.put(pnr, p);
        }
    }

    private void createTiles() throws IOException {
        for (int i = 0; i < gamesize + 2; i++) {
            for (int j = 0; j < gamesize + 2; j++) {
                if (!(i % 2 == 0 && j % 2 == 0 || i == 0 || j == 0 || i == gamesize + 1 || j == gamesize + 1)) {
                    tiles.add(new Tile(i * 64, j * 64));
                }
            }
        }
    }

    /**
     * Startet die Spieler als KI, die im GameMenu ausgewählt wurden
     */
    void startKI() {
        synchronized (players) {
            for (AbstractPlayer player : players.values()) {
                if (player instanceof EasyKI) {
                    ((EasyKI) player).setStartDirection();
                }
                if (player instanceof KI) {
                    ((KI) player).startMoving();
                }
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keyPress(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keyRelease(e.getKeyCode());
    }
}
