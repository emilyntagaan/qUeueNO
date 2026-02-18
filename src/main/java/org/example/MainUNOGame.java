package org.example;

import org.jline.terminal.Terminal;

import java.util.ArrayList;
import java.util.List;

public class MainUNOGame {

    public static void start(Terminal terminal) throws Exception {
        // Initialize players
        List<UNOPlayer> players = new ArrayList<>();
        players.add(new UNOPlayer("You", true)); // Human player

        for (int i = 1; i <= 6; i++) {
            players.add(new UNOPlayer("AI " + i, false)); // AI players
        }

        // Create UI handler with existing terminal
        UNOGameUIHandler uiHandler = new UNOGameUIHandler(terminal);

        // Create game manager with players and UI handler
        UNOGameManager gameManager = new UNOGameManager(players, uiHandler);

        // Start the game
        gameManager.startGame();
    }
}
