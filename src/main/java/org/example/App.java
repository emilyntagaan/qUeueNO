package org.example;

import org.example.util.SoundManager;

public class App {
    public static void main(String[] args) {
        SoundManager soundManager = new SoundManager();

        // Play startup sound asynchronously (non-blocking)
        new Thread(() -> {
            soundManager.playOpeningSoundBlocking("src/main/resources/Sounds/startup.wav", 0);

            // After startup sound finishes, start BGM
            soundManager.playBackgroundMusic("src/main/resources/Sounds/qUNOMusic.wav");
        }).start();

        // Immediately launch interface while startup sound plays
        qUNOInterface.run();

        // Cleanup BGM when program ends
        soundManager.stopBackgroundMusic();
    }
}
