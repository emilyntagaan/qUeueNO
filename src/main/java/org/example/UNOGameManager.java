package org.example;

import org.example.util.SoundManager;
import org.jline.terminal.Terminal;

import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.example.UNOCard.Color.*;

public class UNOGameManager {
    private final Terminal terminal;
    private final PrintWriter pw;
    private final int height = 45;
    private final int width = 200;
    private final Deque<UNOPlayer> players;
    private final UNODeck deck;
    private final Stack<UNOCard> discardPile;
    private UNOCard topCard;
    private UNOCard.Color currentColor;
    private boolean reverseOrder = false;
    private final UNOGameUIHandler uiHandler;
    private UNOCard pendingSpecialCard = null;
    private UNOPlayer pendingEffectOwner = null;
    private UNOPlayer humanPlayer;

    private final List<UNOPlayer> leaderboard = new ArrayList<>();

    private static final long TURN_TIME_LIMIT = 10000; // 10 seconds
    private long remainingTime = TURN_TIME_LIMIT / 1000; // Track remaining time in seconds

    public UNOGameManager(List<UNOPlayer> playerList, UNOGameUIHandler uiHandler) throws Exception {
        this.players = new LinkedList<>(playerList);
        this.deck = new UNODeck();
        this.discardPile = new Stack<>();
        this.uiHandler = uiHandler;
        this.terminal = uiHandler.getTerminal();
        this.pw = new PrintWriter(terminal.writer(), false);

        for (UNOPlayer player : players) {
            if (player.isHuman()) {
                this.humanPlayer = player;
                break;
            }
        }

        initializeGame();
    }

    public UNOPlayer getHumanPlayer() {
        return humanPlayer;
    }

    private void initializeGame() {
        // Ensure a non-special top card
        do {
            topCard = deck.drawCard();
            discardPile.push(topCard);
        } while (topCard.getType() != UNOCard.Type.NUMBER);

        currentColor = (topCard.getColor() == WILD) ? RED : topCard.getColor();

        for (UNOPlayer player : players) {
            player.drawCard(deck, 7);
        }
    }

public void startGame() {
    while (players.size() > 1) {
        UNOPlayer currentPlayer = reverseOrder ? players.pollLast() : players.pollFirst();
        if (currentPlayer == null) break;

        if (pendingSpecialCard != null && currentPlayer != pendingEffectOwner) {
            List<UNOCard> hand = currentPlayer.getHand();
            boolean canCounter = false;
            for (UNOCard card : hand) {
                if (canPlayCard(card) && card.getType() == pendingSpecialCard.getType()) {
                    playCard(currentPlayer, card);
                    canCounter = true;
                    break;
                }
            }
            if (!canCounter) {
                applySpecialCardEffect(pendingSpecialCard, currentPlayer);
            }

            pendingSpecialCard = null;
            pendingEffectOwner = null;

            if (!currentPlayer.hasFinished()) {
                requeueCurrentPlayer(currentPlayer);
            } else {
                leaderboard.add(currentPlayer);
            }

            // ✅ Handle last-player-exit bug here:
            if (players.size() == 1) {
                leaderboard.add(players.poll());
                break;
            }

            continue;
        }

        uiHandler.renderGame(this, humanPlayer, currentPlayer.isHuman());

        AtomicBoolean finalMoveMade = new AtomicBoolean(false);
        Thread timerThread = new Thread(() -> {
            for (remainingTime = TURN_TIME_LIMIT / 1000; remainingTime > 0; remainingTime--) {
                if (finalMoveMade.get()) break;
                uiHandler.renderTimer(remainingTime);
                try { Thread.sleep(1000); } catch (InterruptedException e) { return; }
            }
            if (!finalMoveMade.get()) handlePlayerTimeout(currentPlayer, finalMoveMade);
        });
        timerThread.start();

        if (currentPlayer.isHuman()) {
            handlePlayerInput(currentPlayer, finalMoveMade);
        } else {
            try { Thread.sleep(1500); } catch (InterruptedException e) { e.printStackTrace(); }
            UNOCard aiCard = currentPlayer.autoChooseCard(topCard, currentColor);
            if (aiCard != null && canPlayCard(aiCard)) {
                playCard(currentPlayer, aiCard);
            } else {
                currentPlayer.drawCard(deck, 1);
            }
            finalMoveMade.set(true);
        }

        while (!finalMoveMade.get()) {
            try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
        }

        if (timerThread.isAlive()) timerThread.interrupt();

        if (currentPlayer.hasFinished()) {
            leaderboard.add(currentPlayer);

            // ✅ Handle last-player-exit bug here too:
            if (players.size() == 1) {
                leaderboard.add(players.poll());
                break;
            }

            continue;
        }

        requeueCurrentPlayer(currentPlayer);
    }

    // ✅ Final safety check (still useful)
    if (!players.isEmpty()) leaderboard.add(players.poll());

    renderLeaderboard();
}


    private void handlePlayerInput(UNOPlayer currentPlayer, AtomicBoolean finalMoveMade) {
        SoundManager soundManager = new SoundManager(); // ✅ SFX

        Thread inputThread = new Thread(() -> {
            try {
                while (!finalMoveMade.get()) {
                    if (terminal.reader().ready()) {
                        int key = terminal.reader().read();

                        switch (key) {
                            case 68: // Left arrow
                                currentPlayer.moveLeft();
                                uiHandler.renderGame(this, currentPlayer, true);
                                soundManager.playSFX("src/main/resources/Sounds/nav.wav"); // ✅ SFX
                                break;

                            case 67: // Right arrow
                                currentPlayer.moveRight();
                                uiHandler.renderGame(this, currentPlayer, true);
                                soundManager.playSFX("src/main/resources/Sounds/nav.wav"); // ✅ SFX
                                break;

                            case 102: case 70: // 'f' or 'F' key
                                currentPlayer.drawCard(deck, 1);
                                soundManager.playSFX("src/main/resources/Sounds/nav.wav"); // ✅ SFX
                                pw.println("You drew a card and skipped your turn.");
                                pw.flush();
                                finalMoveMade.set(true); // ✅ End player's turn
                                break;

                            case 10: case 13: // Enter key
                                UNOCard selectedCard = currentPlayer.selectCard();
                                if (canPlayCard(selectedCard)) {
                                    soundManager.playSFX("src/main/resources/Sounds/nav.wav"); // ✅ SFX
                                    finalMoveMade.set(true);
                                    playCard(currentPlayer, selectedCard);
                                } else {
                                    pw.println("You can't play this card!");
                                    pw.flush();
                                }
                                break;

                            case 113: // 'q' key
                                System.exit(0);
                                break;
                        }
                    } else {
                        Thread.sleep(50);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        inputThread.start();
    }

    private boolean canPlayCard(UNOCard selectedCard) {
        return selectedCard != null && selectedCard.isPlayableOn(topCard, currentColor);
    }

    private void playCard(UNOPlayer currentPlayer, UNOCard selectedCard) {
        topCard = selectedCard;
        currentPlayer.removeCardFromHand(selectedCard);
        discardPile.push(selectedCard);

        if (selectedCard.getType() == UNOCard.Type.WILD || selectedCard.getType() == UNOCard.Type.WILD_DRAW_FOUR) {
            if (currentPlayer.isHuman()) chooseColor();
            else currentColor = new UNOCard.Color[]{RED, YELLOW, GREEN, BLUE}[new Random().nextInt(4)];
        } else {
            currentColor = selectedCard.getColor();
        }

        if (selectedCard.getType() == UNOCard.Type.DRAW_TWO ||
                selectedCard.getType() == UNOCard.Type.WILD_DRAW_FOUR ||
                selectedCard.getType() == UNOCard.Type.SKIP) {
            pendingSpecialCard = selectedCard;
            pendingEffectOwner = currentPlayer;
        }
    }

    private void requeueCurrentPlayer(UNOPlayer currentPlayer) {
        if (reverseOrder) players.offerFirst(currentPlayer);
        else players.offerLast(currentPlayer);
    }

    private void applySpecialCardEffect(UNOCard card, UNOPlayer victim) {
        switch (card.getType()) {
            case SKIP:
                pw.println(victim.getName() + " was skipped!"); pw.flush(); break;
            case DRAW_TWO:
                pw.println(victim.getName() + " draws 2 cards!"); pw.flush();
                drawCardWithReshuffleIfNeeded(victim, 2); break;
            case WILD_DRAW_FOUR:
                pw.println(victim.getName() + " draws 4 cards!"); pw.flush();
                drawCardWithReshuffleIfNeeded(victim, 4); break;
        }
    }

    private void chooseColor() {
        try {
            while (true) {
                pw.println("Choose a color: (r)ed, (y)ellow, (g)reen, (b)lue"); pw.flush();
                int k = terminal.reader().read();
                switch (Character.toLowerCase((char) k)) {
                    case 'r': currentColor = RED; return;
                    case 'y': currentColor = YELLOW; return;
                    case 'g': currentColor = GREEN; return;
                    case 'b': currentColor = BLUE; return;
                    default:
                        pw.println("Invalid input. Please try again."); pw.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); currentColor = RED;
        }
    }

    private void handlePlayerTimeout(UNOPlayer currentPlayer, AtomicBoolean finalMoveMade) {
        System.out.println(currentPlayer.getName() + " took too long!");
        drawCardWithReshuffleIfNeeded(currentPlayer, 1);


//        requeueCurrentPlayer(currentPlayer);
        finalMoveMade.set(true);
    }

    private void drawCardWithReshuffleIfNeeded(UNOPlayer player, int count) {
        for (int i = 0; i < count; i++) {
            if (deck.isEmpty()) reshuffleDeck();
            player.drawCard(deck, 1);
        }
    }

    private void reshuffleDeck() {
        if (discardPile.size() <= 1) return;

        UNOCard currentTop = discardPile.pop();
        List<UNOCard> toReshuffle = new ArrayList<>(discardPile);
        discardPile.clear();
        discardPile.push(currentTop);

        // Properly pass reshuffled cards into deck
        deck.reshuffleDeck(toReshuffle, currentTop);
    }

    public UNOCard getTopOfDiscardPile() {
        if (discardPile.isEmpty()) return null;
        return discardPile.get(discardPile.size() - 1);
    }

    public List<UNOCard> getDiscardPileExcludingTop() {
        if (discardPile.size() <= 1) return new ArrayList<>();
        return new ArrayList<>(discardPile.subList(0, discardPile.size() - 1));
    }

    private void renderLeaderboard() {
        List<String> namesOnly = leaderboard.stream()
                .map(UNOPlayer::getName)
                .toList(); // or .collect(Collectors.toList()) for Java 8

        uiHandler.renderLeaderboard(namesOnly);

        AtomicBoolean exitTriggered = new AtomicBoolean(false);

        // Listener thread for 'q' or 'Q'
        Thread inputThread = new Thread(() -> {
            try {
                while (!exitTriggered.get()) {
                    if (terminal.reader().ready()) {
                        int key = terminal.reader().read();
                        if (key == 113 || key == 81) { // 'q' or 'Q'
                            exitTriggered.set(true);
                            break;
                        }
                    } else {
                        Thread.sleep(50);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        inputThread.start();

        // 20-second timeout loop
        long startTime = System.currentTimeMillis();
        while (!exitTriggered.get() && (System.currentTimeMillis() - startTime < 20000)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        exitTriggered.set(true); // Ensure thread exits

        try {
            inputThread.join(); // Wait for thread to stop
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Return to main menu using same terminal
        qUNOInterface.run(terminal);  // <<<<<< FIXED: reuse terminal here
    }


    public UNOCard getTopCard() { return topCard; }
    public List<UNOPlayer> getPlayersSnapshot() { return new ArrayList<>(players); }
    public long getRemainingTime() { return remainingTime; }

}