package forge.gamemodes.rogue;

import forge.card.CardRarity;
import forge.item.PaperCard;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Pricing utility for Bazaar nodes.
 * Centralized rarity-based gold pricing.
 */
public class BazaarPricing {
    // Rarity-based gold pricing
    private static final Map<CardRarity, Integer> RARITY_PRICES = new HashMap<>();

    static {
        RARITY_PRICES.put(CardRarity.Common, 1);
        RARITY_PRICES.put(CardRarity.Uncommon, 2);
        RARITY_PRICES.put(CardRarity.Rare, 3);
        RARITY_PRICES.put(CardRarity.MythicRare, 4);
        RARITY_PRICES.put(CardRarity.Special, 4);
    }

    /**
     * Get the gold price for a card based on its rarity.
     */
    public static int getCardPrice(PaperCard card) {
        return RARITY_PRICES.getOrDefault(card.getRarity(), 1);
    }

    /**
     * Calculate total gold cost for a set of cards.
     */
    public static int calculateTotalCost(Set<PaperCard> cards) {
        int total = 0;
        for (PaperCard card : cards) {
            total += getCardPrice(card);
        }
        return total;
    }
}
