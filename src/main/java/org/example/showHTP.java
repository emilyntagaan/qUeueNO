package org.example;

import org.example.util.TxtOverlayUtil;
import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Terminal;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.PrintWriter;

public class showHTP {
    public static void run(Terminal terminal) {
        try {
            terminal.enterRawMode(); // Enable raw mode for immediate key reading
            PrintWriter pw = new PrintWriter(terminal.output(), false);
            BindingReader reader = new BindingReader(terminal.reader());
            KeyMap<String> keyMap = new KeyMap<>();
            keyMap.bind("exit", "q");
            keyMap.bind("exit", "Q");

            BufferedImage backgroundImage = ImageIO.read(new File("src/main/resources/ConsoleImages/qUNOBackground.png"));
            BufferedImage titleImage = ImageIO.read(new File("src/main/resources/ConsoleImages/HowToPlayMenu.png"));

            int width = backgroundImage.getWidth();
            int height = backgroundImage.getHeight();

            byte[][][] background = extractPixelData(backgroundImage);
            byte[][][] titlePixels = extractPixelData(titleImage);

            int titleWidth = titleImage.getWidth();
            int titleHeight = titleImage.getHeight();
            int titleStartX = 0;
            int titleStartY = 2;

            String[][] textOverlay = new String[height][width];

            String[] guideText = {
                    "How to Play QueueNO: You’ll be playing against 6 computer opponents, and everyone starts with 7 cards. ",
                    "A random card goes in the center to kick things off, and players take turns one by one, like a line—first in, first out.",
                    "After your turn, you go to the back of the line, and the next player goes.",
                    "",
                    "To play, just match the card in the center by color or number.",
                    " If you can't, draw a card—if it matches, play it! If not, your turn’s over.",
                    "Watch out for action cards like Skip (skips the next player), Reverse (flips the turn order), ",
                    "Draw +2 and Draw +4 (ouch), and Wild cards where you pick the next color.",
                    "Oh, and there’s a timer—don’t take too long!",
                    "The game ends when a player gets rid of all their cards.",
                    "The first to do so wins. Simple, fast, and fun!",
                    "",
                    "Press [←] or [Esc] to return to main menu"
            };

            TxtOverlayUtil.drawCenteredTextOverlay(guideText, textOverlay, width, height);

            // Build the entire screen first
            StringBuilder sb = new StringBuilder();
            sb.append("\u001b[H"); // Move to top-left

            for (int y = 0; y < height - 1; y += 2) {
                for (int x = 0; x < width; x++) {
                    byte[] top;
                    byte[] bottom;

                    boolean inTitle =
                            y >= titleStartY &&
                                    y < titleStartY + titleHeight - 1 &&
                                    x >= titleStartX &&
                                    x < titleStartX + titleWidth;

                    if (inTitle && titleImage.getColorModel().hasAlpha()) {
                        int topY = y - titleStartY;
                        int bottomY = y - titleStartY + 1;
                        int xOffset = x - titleStartX;

                        int pixelLength = 4;
                        int titleStride = titleWidth * pixelLength;
                        byte[] titleData = ((DataBufferByte) titleImage.getRaster().getDataBuffer()).getData();

                        int topIndex = topY * titleStride + xOffset * pixelLength;
                        int bottomIndex = bottomY * titleStride + xOffset * pixelLength;

                        int topAlpha = titleData[topIndex] & 0xFF;
                        int bottomAlpha = titleData[bottomIndex] & 0xFF;

                        if (topAlpha != 0) {
                            top = new byte[]{
                                    titleData[topIndex + 3],
                                    titleData[topIndex + 2],
                                    titleData[topIndex + 1]
                            };
                        } else {
                            top = background[y][x];
                        }

                        if (bottomAlpha != 0) {
                            bottom = new byte[]{
                                    titleData[bottomIndex + 3],
                                    titleData[bottomIndex + 2],
                                    titleData[bottomIndex + 1]
                            };
                        } else {
                            bottom = background[y + 1][x];
                        }
                    } else {
                        top = background[y][x];
                        bottom = background[y + 1][x];
                    }

                    String topText = textOverlay[y][x];
                    if (topText != null) {
                        sb.append(String.format(
                                "\033[38;2;255;255;255;48;2;%d;%d;%dm%s",
                                bottom[0] & 0xFF, bottom[1] & 0xFF, bottom[2] & 0xFF,
                                topText
                        ));
                    } else {
                        sb.append(String.format(
                                "\033[38;2;%d;%d;%d;48;2;%d;%d;%dm\u2580",
                                top[0] & 0xFF, top[1] & 0xFF, top[2] & 0xFF,
                                bottom[0] & 0xFF, bottom[1] & 0xFF, bottom[2] & 0xFF
                        ));
                    }
                }
                sb.append("\033[0m\n");
            }

            pw.print(sb.toString());
            pw.flush();

            keyMap.bind("back", "\033", "\u001b", "q", "Q", "\u001b[D"); // Esc, q, Q, Left arrow

            while (true) {
                String key = reader.readBinding(keyMap, null, true);
                if ("back".equals(key)) {
                    return; // go back to main menu
                }
                Thread.sleep(50); // debounce
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[][][] extractPixelData(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        boolean hasAlpha = img.getColorModel().hasAlpha();
        byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        int pixelLength = hasAlpha ? 4 : 3;

        byte[][][] result = new byte[height][width][3];

        for (int px = 0, row = 0, col = 0; px + pixelLength - 1 < pixels.length; px += pixelLength) {
            result[row][col][0] = pixels[px + pixelLength - 1];
            result[row][col][1] = pixels[px + pixelLength - 2];
            result[row][col][2] = pixels[px + pixelLength - 3];

            col++;
            if (col == width) {
                col = 0;
                row++;
            }
        }

        return result;
    }
}
