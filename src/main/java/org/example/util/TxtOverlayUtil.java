package org.example.util;

public class TxtOverlayUtil {
    public static void drawCenteredTextOverlay(String[] lines, String[][] overlay, int imageWidth, int imageHeight) {
        int charHeight = imageHeight / 2;
        int charWidth = imageWidth;
        int startRow = (charHeight - lines.length) / 2;

        for (int i = 0; i < lines.length; i++) {
            int row = startRow + i;
            String line = lines[i];
            int startCol = (charWidth - line.length()) / 2;

            for (int j = 0; j < line.length(); j++) {
                overlay[row * 2][startCol + j] = String.valueOf(line.charAt(j));
            }
        }
    }
}