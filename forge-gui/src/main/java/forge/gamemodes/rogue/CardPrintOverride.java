package forge.gamemodes.rogue;

import forge.item.PaperCard;

public record CardPrintOverride(String cardName, String setCode, Integer artIndex) {

    public boolean matches(PaperCard card) {
        return card != null && card.getName().equals(cardName);
    }
}
