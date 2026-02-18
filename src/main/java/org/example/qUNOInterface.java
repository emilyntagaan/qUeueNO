package org.example;

import org.example.util.SoundManager;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import org.example.MainUNOGame;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class qUNOInterface {

    private static final int FRAME_DELAY = 70;
    private static final String BACKGROUND_PATH = "src/main/resources/ConsoleImages/qUNOBackground.png";

    private static final String[] LOGO_PATHS = {
            "src/main/resources/ConsoleImages/color1.png", "src/main/resources/ConsoleImages/color2.png", "src/main/resources/ConsoleImages/color3.png",
            "src/main/resources/ConsoleImages/color4.png", "src/main/resources/ConsoleImages/color5.png", "src/main/resources/ConsoleImages/color6.png",
            "src/main/resources/ConsoleImages/color7.png", "src/main/resources/ConsoleImages/color8.png", "src/main/resources/ConsoleImages/color9.png",
            "src/main/resources/ConsoleImages/color10.png", "src/main/resources/ConsoleImages/color11.png", "src/main/resources/ConsoleImages/color12.png",
            "src/main/resources/ConsoleImages/color13.png", "src/main/resources/ConsoleImages/color14.png", "src/main/resources/ConsoleImages/color15.png",
            "src/main/resources/ConsoleImages/color16.png", "src/main/resources/ConsoleImages/color17.png", "src/main/resources/ConsoleImages/color18.png",
            "src/main/resources/ConsoleImages/color19.png", "src/main/resources/ConsoleImages/color20.png", "src/main/resources/ConsoleImages/color21.png",
            "src/main/resources/ConsoleImages/color22.png", "src/main/resources/ConsoleImages/color23.png", "src/main/resources/ConsoleImages/color24.png",
            "src/main/resources/ConsoleImages/color25.png", "src/main/resources/ConsoleImages/color26.png", "src/main/resources/ConsoleImages/color27.png",
            "src/main/resources/ConsoleImages/color28.png"
    };

    private static final String[] HOVERED_MENU_PATHS = {
            "src/main/resources/ConsoleImages/HowToPlay.png",
            "src/main/resources/ConsoleImages/Play.png",
            "src/main/resources/ConsoleImages/Quit.png"
    };

    public static void main(String[] args) {
        run();
    }

    public static void run() {
        try {
            Terminal terminal = TerminalBuilder.terminal();
            PrintWriter pw = new PrintWriter(terminal.writer(), false);

            byte[][][] background = loadImage(BACKGROUND_PATH);
            List<byte[][][]> logoFrames = loadFrames(LOGO_PATHS);

            int height = background.length;
            int width = background[0].length;

            byte[][][] howToPlayImage = loadImage("src/main/resources/ConsoleImages/HowToPlayMenu.png");

            // --- Initial animation ---
            for (int frame = 0; frame < logoFrames.size(); frame++) {
                renderFrame(pw, background, logoFrames.get(frame), null, height, width);
                Thread.sleep(FRAME_DELAY);
            }

            // --- Menu interaction ---
            int selectedIndex = 1;
            String[] menuLabels = {"Guide", "Play", "Quit"};
            boolean running = true;

            SoundManager soundManager = new SoundManager();

            while (running) {
                byte[][][] hoveredMenu = loadImage(HOVERED_MENU_PATHS[selectedIndex]);

                renderFrame(pw, background, logoFrames.get(logoFrames.size() - 1), hoveredMenu, height, width);

                int key = terminal.reader().read();
                switch (key) {
                    case 68: // Left arrow
                        selectedIndex = Math.max(0, selectedIndex - 1);
                        soundManager.playSFX("src/main/resources/Sounds/nav.wav");
                        break;
                    case 67: // Right arrow
                        selectedIndex = Math.min(menuLabels.length - 1, selectedIndex + 1);
                        soundManager.playSFX("src/main/resources/Sounds/nav.wav");
                        break;
                    case 10: case 13: // Enter
                        soundManager.playSFX("src/main/resources/Sounds/nav.wav");
                        switch (selectedIndex) {
                            case 0:
                                showHTP.run(terminal);
                                // Don’t exit the loop after showing instructions!
                                break;
                            case 1:
                                MainUNOGame.start(terminal);
                                running = false;
                            case 2:
                                pw.print("\033[H\033[2J"); // Clear screen
                                pw.flush();
                                System.exit(0);
                        }
                        break;
                }

                Thread.sleep(80);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void run(Terminal terminal) {
        try {
            PrintWriter pw = new PrintWriter(terminal.writer(), false);

            byte[][][] background = loadImage(BACKGROUND_PATH);
            List<byte[][][]> logoFrames = loadFrames(LOGO_PATHS);
            int height = background.length;
            int width = background[0].length;

            byte[][][] howToPlayImage = loadImage("src/main/resources/ConsoleImages/HowToPlayMenu.png");

            for (int frame = 0; frame < logoFrames.size(); frame++) {
                renderFrame(pw, background, logoFrames.get(frame), null, height, width);
                Thread.sleep(FRAME_DELAY);
            }

            int selectedIndex = 1;
            String[] menuLabels = {"Guide", "Play", "Quit"};
            boolean running = true;

            SoundManager soundManager = new SoundManager();

            while (running) {
                byte[][][] hoveredMenu = loadImage(HOVERED_MENU_PATHS[selectedIndex]);
                renderFrame(pw, background, logoFrames.get(logoFrames.size() - 1), hoveredMenu, height, width);

                int key = terminal.reader().read();
                switch (key) {
                    case 68: // Left arrow
                        selectedIndex = Math.max(0, selectedIndex - 1);
                        soundManager.playSFX("src/main/resources/Sounds/nav.wav");
                        break;
                    case 67: // Right arrow
                        selectedIndex = Math.min(menuLabels.length - 1, selectedIndex + 1);
                        soundManager.playSFX("src/main/resources/Sounds/nav.wav");
                        break;
                    case 10: case 13: // Enter
                        soundManager.playSFX("src/main/resources/Sounds/nav.wav");
                        switch (selectedIndex) {
                            case 0:
                                showHTP.run(terminal);
                                break;
                            case 1:
                                MainUNOGame.start(terminal);
                                running = false;
                                break;
                            case 2:
                                pw.print("\033[H\033[2J"); // Clear screen
                                pw.flush();
                                System.exit(0);
                        }
                        break;
                }

                Thread.sleep(80);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void renderFrame(PrintWriter pw, byte[][][] bg, byte[][][] logo, byte[][][] menu, int height, int width) {
        StringBuilder frameBuilder = new StringBuilder("\033[H");

        for (int y = 0; y < height - 1; y += 2) {
            for (int x = 0; x < width; x++) {
                byte[] top = getPixel(bg, logo, menu, y, x);
                byte[] bottom = getPixel(bg, logo, menu, y + 1, x);

                frameBuilder.append(String.format(
                        "\033[38;2;%d;%d;%d;48;2;%d;%d;%dm▀",
                        uByte(top[0]), uByte(top[1]), uByte(top[2]),
                        uByte(bottom[0]), uByte(bottom[1]), uByte(bottom[2])
                ));
            }
            frameBuilder.append("\033[0m\n");
        }

        pw.print(frameBuilder);
        pw.flush();
    }

    private static List<byte[][][]> loadFrames(String[] paths) throws Exception {
        List<byte[][][]> frames = new ArrayList<>();
        for (String path : paths) {
            frames.add(loadImage(path));
        }
        return frames;
    }

    private static byte[][][] loadImage(String path) throws Exception {
        BufferedImage img = ImageIO.read(new File(path));
        int width = img.getWidth();
        int height = img.getHeight();
        boolean hasAlpha = img.getColorModel().hasAlpha();
        byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        int pixelLength = hasAlpha ? 4 : 3;

        byte[][][] result = new byte[height][width][4];

        for (int px = 0, row = 0, col = 0; px + pixelLength - 1 < pixels.length; px += pixelLength) {
            result[row][col][0] = pixels[px + pixelLength - 1]; // R
            result[row][col][1] = pixels[px + pixelLength - 2]; // G
            result[row][col][2] = pixels[px + pixelLength - 3]; // B
            result[row][col][3] = pixelLength == 4 ? pixels[px] : (byte) 0xFF; // A

            col++;
            if (col == width) {
                col = 0;
                row++;
            }
        }

        return result;
    }

    private static byte[] getPixel(byte[][][] bg, byte[][][] logo, byte[][][] menu, int y, int x) {
        byte[] pixel = bg[y][x];

        if (logo != null && y < logo.length && x < logo[0].length && uByte(logo[y][x][3]) > 127) {
            pixel = logo[y][x];
        }

        if (menu != null && y < menu.length && x < menu[0].length && uByte(menu[y][x][3]) > 127) {
            pixel = menu[y][x];
        }

        return pixel;
    }

    private static int uByte(byte b) {
        return b & 0xFF;
    }

}
