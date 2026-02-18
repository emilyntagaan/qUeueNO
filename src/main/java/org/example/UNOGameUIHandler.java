package org.example;

import org.jline.terminal.Terminal;
import org.jline.terminal.Terminal.SignalHandler;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class UNOGameUIHandler {

    private final Terminal terminal;
    private final PrintWriter pw;
    private final int height = 45; // ANSI rows for 200x90 background
    private final int width = 200; // ANSI columns for 200x90 background

    public UNOGameUIHandler(Terminal terminal) {
        this.terminal = terminal;
        this.pw = new PrintWriter(terminal.writer(), false);
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public void renderGame(UNOGameManager gameManager, UNOPlayer currentPlayer, boolean isHumanTurn) {
        try {
            byte[][][] background = loadImage("src/main/resources/ConsoleImages/kipi.png");
            renderFrame(background, currentPlayer, gameManager);
            renderTimer(gameManager.getRemainingTime());

            if (isHumanTurn) {
                String text = "It's your turn!";
                int x = width / 2 - text.length() / 2;
                int y = height - 4;

                pw.print(String.format("\033[%d;%dH\033[32m%s\033[0m", y, x, text));
            }

            pw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void renderLeaderboard(List<String> leaderboard) {
        try {
            // Load background and clear screen
            byte[][][] background = loadImage("src/main/resources/ConsoleImages/Leaderboard.png");
            pw.print("\033[2J\033[H"); // Clear screen
            renderBackgroundOnly(background);

            // Centered leaderboard
            int centerY = height / 2 - leaderboard.size() / 2;
            int centerX = width / 2;

            for (int i = 0; i < leaderboard.size(); i++) {
                String line = String.format(" %2d. %s", i + 1, leaderboard.get(i));
                int x = centerX - line.length() / 2;
                pw.print(String.format("\033[%d;%dH%s", centerY + i, x, line));
            }

            // Instruction message
            String instructions = "Leaderboard complete! Press 'Q' to return to Main Menu, or wait 20 seconds.";
            int instrX = centerX - instructions.length() / 2;
            pw.print(String.format("\033[%d;%dH%s", centerY + leaderboard.size() + 2, instrX, instructions));

            pw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderBackgroundOnly(byte[][][] bg) {
        StringBuilder frameBuilder = new StringBuilder("\033[H");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                byte[] topPixel = bg[y * 2][x];
                byte[] bottomPixel = bg[y * 2 + 1][x];
                frameBuilder.append(String.format(
                        "\033[38;2;%d;%d;%d;48;2;%d;%d;%dm▀",
                        uByte(topPixel[0]), uByte(topPixel[1]), uByte(topPixel[2]), // foreground
                        uByte(bottomPixel[0]), uByte(bottomPixel[1]), uByte(bottomPixel[2]) // background
                ));
            }
            frameBuilder.append("\033[0m\n");
        }
        pw.print(frameBuilder);
        pw.flush();
    }

    private void renderFrame(byte[][][] bg, UNOPlayer currentPlayer, UNOGameManager gameManager) {
        StringBuilder frameBuilder = new StringBuilder("\033[H");

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                byte[] topPixel = bg[y * 2][x];
                byte[] bottomPixel = bg[y * 2 + 1][x];
                frameBuilder.append(String.format(
                        "\033[38;2;%d;%d;%d;48;2;%d;%d;%dm▀",
                        uByte(topPixel[0]), uByte(topPixel[1]), uByte(topPixel[2]), // foreground
                        uByte(bottomPixel[0]), uByte(bottomPixel[1]), uByte(bottomPixel[2]) // background
                ));
            }
            frameBuilder.append("\033[0m\n");
        }

        renderPlayers(frameBuilder, gameManager.getPlayersSnapshot(), gameManager.getHumanPlayer());
        renderDeckAndDiscard(frameBuilder, gameManager);
        renderPlayerHand(frameBuilder, currentPlayer.getHand(), currentPlayer.getSelectedIndex());

        pw.print(frameBuilder);
        pw.flush();
    }

    private void renderPlayers(StringBuilder frameBuilder, List<UNOPlayer> players, UNOPlayer humanPlayer) {
        // Define individual coordinates for each of the 7 AI players
        int[][] aiCardCountPositions = new int[][]{
                {7, 30},   // AI 1
                {20, 14},  // AI 2
                {30, 30},  // AI 3
                {7, 171},  // AI 4
                {20, 187}, // AI 5
                {30, 171}, // AI 6
                {4, 90}    // AI 7 (top center)
        };

        List<UNOPlayer> aiPlayers = new ArrayList<>();
        for (UNOPlayer p : players) {
            if (!p.equals(humanPlayer)) {
                aiPlayers.add(p);
            }
        }

        for (int i = 0; i < Math.min(aiPlayers.size(), aiCardCountPositions.length); i++) {
            int y = aiCardCountPositions[i][0];
            int x = aiCardCountPositions[i][1];
            frameBuilder.append(String.format("\033[%d;%dH- %d", y, x, aiPlayers.get(i).getCardCount()));
        }
    }


//    private void drawCardBack(StringBuilder frameBuilder, int startY, int startX) {
//        for (int i = 0; i < aiCardBack.length; i++) {
//            frameBuilder.append(String.format("\033[%d;%dH%s", startY + i, startX, aiCardBack[i]));
//        }
//    }

    private void renderDeckAndDiscard(StringBuilder frameBuilder, UNOGameManager gameManager) {
        UNOCard top = gameManager.getTopCard();
        if (top != null) {
            String[] lines = top.getAsciiArt().split("\\n");
            int baseX = 113, baseY = 16;  // Adjust these to match your new background layout
            for (int i = 0; i < lines.length; i++) {
                frameBuilder.append(String.format("\033[%d;%dH%s", baseY + i, baseX, lines[i]));
            }
        }
    }

    private void renderPlayerHand(StringBuilder frameBuilder, List<UNOCard> hand, int selectedIndex) {
        int startX = width / 4;
        int baseY = height - 12;
        int liftOffset = 5; // Number of lines to shift the selected card up

        for (int i = 0; i < hand.size(); i++) {
            String[] lines = hand.get(i).getAsciiArt().split("\\n");
            int x = startX + i * 8;
            int y = (i == selectedIndex) ? baseY - liftOffset : baseY;

            for (int j = 0; j < lines.length; j++) {
                frameBuilder.append(String.format("\033[%d;%dH%s", y + j, x, lines[j]));
            }
        }
    }


    public void renderTimer(long remaining) {
        String text = String.format("Time Left: %2ds", remaining);
        int x = width / 2 - text.length() / 2;
        int y = height - 2;
        pw.print(String.format("\033[%d;%dH%s", y, x, text));
        pw.flush();
    }

    private byte[][][] loadImage(String path) {
        try {
            BufferedImage img = ImageIO.read(new File(path));
            int w = img.getWidth(), h = img.getHeight();
            boolean alpha = img.getColorModel().hasAlpha();
            byte[] data = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
            byte[][][] buf = new byte[h][w][4];
            int p = 0;
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int a = 255, r, g, b;
                    if (alpha) a = data[p++] & 0xFF;
                    b = data[p++] & 0xFF;
                    g = data[p++] & 0xFF;
                    r = data[p++] & 0xFF;
                    buf[y][x] = new byte[]{(byte) r, (byte) g, (byte) b, (byte) a};
                }
            }
            return buf;
        } catch (Exception e) {
            byte[][][] black = new byte[height][width][4];
            return black;
        }
    }

    private List<byte[][][]> loadFrames() {
        return new ArrayList<>();
    }

    private int uByte(byte b) {
        return b & 0xFF;
    }
}
