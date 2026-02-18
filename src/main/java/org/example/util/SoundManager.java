package org.example.util;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundManager {
    private Clip backgroundClip;

    // Play a one-time sound with optional delay (in milliseconds)
    public void playOpeningSound(String filePath, long delayMillis) {
        new Thread(() -> {
            try {
                Thread.sleep(delayMillis); // Adjustable delay
                Clip clip = loadClip(filePath);
                clip.start();
                // Wait until finished playing
                clip.drain();
                clip.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Blocking version that really waits until the sound ends
    public void playOpeningSoundBlocking(String filePath, long delayMillis) {
        try {
            Thread.sleep(delayMillis); // Optional pre-delay

            Clip clip = loadClip(filePath);

            final Object lock = new Object();

            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    synchronized (lock) {
                        lock.notify(); // Signal that sound has stopped
                    }
                }
            });

            clip.start();

            // Block until playback finishes
            synchronized (lock) {
                lock.wait(); // Wait until notified
            }

            clip.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Play looping background music
    public void playBackgroundMusic(String filePath) {
        new Thread(() -> {
            try {
                backgroundClip = loadClip(filePath);
                backgroundClip.loop(Clip.LOOP_CONTINUOUSLY); // Infinite loop
                backgroundClip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Play short SFX without blocking UI
    public void playSFX(String path) {
        new Thread(() -> {
            try {
                // Load once per call to avoid locking issues
                AudioInputStream ais = AudioSystem.getAudioInputStream(new File(path));
                DataLine.Info info = new DataLine.Info(Clip.class, ais.getFormat());

                if (!AudioSystem.isLineSupported(info)) {
                    System.err.println("Line not supported for: " + path);
                    return;
                }

                Clip clip = (Clip) AudioSystem.getLine(info);
                clip.open(ais);
                clip.start();

                // Drain till end to avoid GC cutoff
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // soundManager.playSFX("src/main/resources/Sounds/nav.wav");

    // Stop background music (to prevent overlaps)
    public void stopBackgroundMusic() {
        if (backgroundClip != null && backgroundClip.isRunning()) {
            backgroundClip.stop();
            backgroundClip.close();
        }
    }

    // Helper method to load a Clip from file path
    private Clip loadClip(String filePath) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        File audioFile = new File(filePath);
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
        Clip clip = AudioSystem.getClip();
        clip.open(audioStream);
        return clip;
    }
}
