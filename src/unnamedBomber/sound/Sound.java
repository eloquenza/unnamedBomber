package unnamedBomber.sound;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Sound {
    public Clip c;

    public Sound(String filename) {

        try {
            AudioInputStream sound = AudioSystem.getAudioInputStream(new File(filename));
            c = AudioSystem.getClip();
            c.open(sound);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("Sound: Malformed URL: " + e);
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
            throw new RuntimeException("Sound: Unsupported Audio File: " + e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Sound: Input/Output Error: " + e);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            throw new RuntimeException("Sound: Line Unavailable Exception Error: " + e);
        }

    }

    public void start() {
        c.start();
    }

    public void soundstartloop() {
        c.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void soundstop() {
        if (c.isRunning()) {
            c.stop();
            c.setFramePosition(0);
        }
    }
}
