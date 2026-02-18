package org.example;

import java.util.ArrayList;
import java.util.List;

public class UNOPlayer {
    private final String name;
    private final boolean isHuman;
    private final List<UNOCard> hand;
    private boolean hasFinished;
    private int selectedIndex;

    public UNOPlayer(String name, boolean isHuman) {
        this.name = name;
        this.isHuman = isHuman;
        this.hand = new ArrayList<>();
        this.hasFinished = false;
        this.selectedIndex = 0;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public String getName() {
        return name;
    }

    public boolean isHuman() {
        return isHuman;
    }

    public List<UNOCard> getHand() {
        return hand;
    }

    public boolean hasFinished() {
        return hasFinished;
    }

    public void removeCardFromHand(UNOCard card) {
        hand.remove(card);
        if (hand.isEmpty()) {
            hasFinished = true;
            selectedIndex = 0;
        } else if (selectedIndex >= hand.size()) {
            selectedIndex = hand.size() - 1;
        }
    }


    public void drawCard(UNODeck deck, int amount) {
        for (int i = 0; i < amount; i++) {
            UNOCard card = deck.drawCard();
            if (card != null) {
                hand.add(card);
            }
        }
    }


    public void moveLeft() {
        if (!hand.isEmpty()) {
            selectedIndex = (selectedIndex - 1 + hand.size()) % hand.size();
        }
    }

    public void moveRight() {
        if (!hand.isEmpty()) {
            selectedIndex = (selectedIndex + 1) % hand.size();
        }
    }


    public UNOCard selectCard() {
        if (hand.isEmpty()) return null;
        return hand.get(selectedIndex);
    }


    public UNOCard autoPlayTurn(UNOCard topCard, UNOCard.Color currentColor) {
        List<UNOCard> playableCards = getPlayableCards(topCard, currentColor);
        if (!playableCards.isEmpty()) {
            UNOCard chosenCard = playableCards.get(0);
            removeCardFromHand(chosenCard);
            return chosenCard;
        } else {
            return null; // No card to play, must draw
        }
    }


    public UNOCard autoChooseCard(UNOCard topCard, UNOCard.Color currentColor) {
        List<UNOCard> playableCards = getPlayableCards(topCard, currentColor);
        if (!playableCards.isEmpty()) {
            return playableCards.get(0);
        }
        return null;
    }

    public List<UNOCard> getPlayableCards(UNOCard topCard, UNOCard.Color currentColor) {
        List<UNOCard> playable = new ArrayList<>();
        for (UNOCard card : hand) {
            if (card.isPlayableOn(topCard, currentColor)) {
                playable.add(card);
            }
        }
        return playable;
    }



    public int getCardCount() {
        return hand.size();
    }

    public String getDisplayText() {
        return "[Back] - " + getCardCount();
    }
}

