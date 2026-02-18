package org.example;

import java.util.*;

public class UNOCard {

    public enum Color {
        RED, YELLOW, GREEN, BLUE, WILD
    }

    public enum Type {
        NUMBER, SKIP, REVERSE, DRAW_TWO, WILD, WILD_DRAW_FOUR
    }

    private final Color color;
    private final Type type;
    private final int number; // Only used for number cards
    private final String asciiArt; // For UI rendering

    public UNOCard(Color color, Type type, int number, String asciiArt) {
        this.color = color;
        this.type = type;
        this.number = number;
        this.asciiArt = asciiArt;
    }

    public Color getColor() {
        return color;
    }

    public Type getType() {
        return type;
    }

    public int getNumber() {
        return number;
    }

    public String getAsciiArt() {
        return asciiArt;
    }

    public boolean isPlayableOn(UNOCard topCard, Color currentColor) {
        // Wild cards are always playable
        if (this.color == Color.WILD) {
            return true;
        }

        // No top card? Any card is playable (game start edge case)
        if (topCard == null) {
            return true;
        }

        // Match by color, type (if action), or number (if number)
        return this.color == currentColor ||
                (this.type == topCard.type && this.type != Type.NUMBER) ||
                (this.type == Type.NUMBER && topCard.type == Type.NUMBER && this.number == topCard.number);
    }


    @Override
    public String toString() {
        if (type == Type.NUMBER) {
            return color + " " + number;
        }
        return color + " " + type;
    }
}

class UNODeck {
    private final Queue<UNOCard> drawPile = new LinkedList<>();
    private final List<UNOCard> discardPile = new ArrayList<>();

    public UNODeck() {
        List<UNOCard> allCards = new ArrayList<>();

        for (UNOCard.Color color : UNOCard.Color.values()) {
            if (color == UNOCard.Color.WILD) continue;
            // Add number cards (0 once, 1-9 twice)
            allCards.add(makeCard(color, UNOCard.Type.NUMBER, 0));
            for (int i = 1; i <= 9; i++) {
                allCards.add(makeCard(color, UNOCard.Type.NUMBER, i));
                allCards.add(makeCard(color, UNOCard.Type.NUMBER, i));
            }
            // Add action cards x2
            for (int i = 0; i < 2; i++) {
                allCards.add(makeCard(color, UNOCard.Type.SKIP, -1));
                allCards.add(makeCard(color, UNOCard.Type.REVERSE, -1));
                allCards.add(makeCard(color, UNOCard.Type.DRAW_TWO, -1));
            }
        }

        // Add Wild and Wild Draw Four cards
        for (int i = 0; i < 4; i++) {
            allCards.add(makeCard(UNOCard.Color.WILD, UNOCard.Type.WILD, -1));
            allCards.add(makeCard(UNOCard.Color.WILD, UNOCard.Type.WILD_DRAW_FOUR, -1));
        }

        Collections.shuffle(allCards);
        drawPile.addAll(allCards);
    }

    private UNOCard makeCard(UNOCard.Color color, UNOCard.Type type, int number) {
        String art = generateAsciiArt(color, type, number);
        return new UNOCard(color, type, number, art);
    }

    private String generateAsciiArt(UNOCard.Color color, UNOCard.Type type, int number) {
        String centerText;
        if (type == UNOCard.Type.NUMBER) {
            centerText = String.valueOf(number);
        } else if (type == UNOCard.Type.WILD || type == UNOCard.Type.WILD_DRAW_FOUR) {
            centerText = "WILD";
        } else {
            centerText = type.name().replace("_", " ");
        }

        return
                colorize(".::::::::::::.\n", color) +
                        colorize("::.:....:.:.::.\n", color) +
                        colorize(":....:.      :.\n", color) +
                        colorize(":..:.   " + padCenter(centerText, 6) + ":\n", color) +
                        colorize(":.:          :.\n", color) +
                        colorize("::  " + padCenter(color.name(), 8) + " ::\n", color) +
                        colorize(":          ..:.\n", color) +
                        colorize("-        .:..:.\n", color) +
                        colorize(":      .:....:.\n", color) +
                        colorize("::.:.:....:.::.\n", color) +
                        colorize(".::::::::::::.", color);
    }

    private String padCenter(String text, int width) {
        int padding = Math.max(0, width - text.length());
        int left = padding / 2;
        int right = padding - left;
        return " ".repeat(left) + text + " ".repeat(right);
    }

    private String colorize(String text, UNOCard.Color color) {
        String ansi;
        switch (color) {
            case RED: ansi = "\u001B[31m"; break;
            case GREEN: ansi = "\u001B[32m"; break;
            case YELLOW: ansi = "\u001B[33m"; break;
            case BLUE: ansi = "\u001B[34m"; break;
            case WILD: ansi = "\u001B[35m"; break;
            default: ansi = "\u001B[0m"; break;
        }
        return ansi + text + "\u001B[0m"; // Reset after text
    }

    public UNOCard drawCard() {
        if (drawPile.isEmpty()) {
            reshuffle();
        }
        if (drawPile.isEmpty()) {
            return null;
        }
        return drawPile.poll();
    }

    private void reshuffle() {
        if (discardPile.size() <= 1) return;

        UNOCard top = discardPile.remove(discardPile.size() - 1);
        List<UNOCard> temp = new ArrayList<>(discardPile);
        Collections.shuffle(temp);
        drawPile.addAll(temp);
        drawPile.addAll(temp);
        discardPile.clear();
        discardPile.add(top);
    }

    public boolean isEmpty() {
        return drawPile.isEmpty();
    }

    public void addToDiscardPile(UNOCard card) {
        discardPile.add(card);
    }

    public void reshuffleDeck(List<UNOCard> toReshuffle, UNOCard topCard) {
        drawPile.clear();
        discardPile.clear();

        Collections.shuffle(toReshuffle);
        drawPile.addAll(toReshuffle);
        discardPile.add(topCard);
    }


    public boolean hasCards() {
        return !drawPile.isEmpty() || discardPile.size() > 1;
    }


    public int remainingCards() {
        return drawPile.size();
    }
}
