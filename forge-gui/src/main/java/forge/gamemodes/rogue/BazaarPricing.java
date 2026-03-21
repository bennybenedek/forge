package forge.gamemodes.rogue;

import forge.card.CardRarity;
import forge.item.PaperCard;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Pricing utility for Bazaar nodes.
 * Centralized rarity-based gold pricing with support for price overrides.
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
     * Get the gold price for a card, respecting price overrides.
     */
    public static int getCardPrice(PaperCard card, Map<String, Integer> priceOverrides) {
        if (priceOverrides != null) {
            Integer override = priceOverrides.get(card.getName());
            if (override != null) return override;
        }
        return getCardPrice(card);
    }

    /**
     * Calculate total gold cost for a set of cards.
     */
    public static int calculateTotalCost(Set<PaperCard> cards) {
        return calculateTotalCost(cards, null);
    }

    /**
     * Calculate total gold cost for a set of cards, respecting price overrides.
     */
    public static int calculateTotalCost(Set<PaperCard> cards, Map<String, Integer> priceOverrides) {
        int total = 0;
        for (PaperCard card : cards) {
            total += getCardPrice(card, priceOverrides);
        }
        return total;
    }
}
