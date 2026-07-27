package forge.gamemodes.rogue.effect;

import forge.gamemodes.rogue.BazaarPricing;
import forge.gamemodes.rogue.RogueRun;
import forge.item.PaperCard;

/** A purchasable Bazaar entry with explicit display and purchase semantics. */
public record BazaarItem(PaperCard card, Type type, RogueEffect traitEffect,
                         RogueRun.CarryCardType carryCardType, Integer priceOverride) {
    public enum Type {
        CARD,
        CURIO,
        TRAIT,
        CARRY_CARD
    }

    public static BazaarItem forCard(PaperCard card) {
        return forCard(card, null);
    }

    public static BazaarItem forCard(PaperCard card, Integer priceOverride) {
        return new BazaarItem(card, Type.CARD, null, null, priceOverride);
    }

    public static BazaarItem forCurio(PaperCard card, int price) {
        return new BazaarItem(card, Type.CURIO, null, null, price);
    }

    public static BazaarItem forTrait(PaperCard card, RogueEffect traitEffect, int price) {
        return new BazaarItem(card, Type.TRAIT, traitEffect, null, price);
    }

    public static BazaarItem forCarryCard(PaperCard card, RogueRun.CarryCardType carryCardType) {
        return new BazaarItem(card, Type.CARRY_CARD, null, carryCardType, null);
    }

    public BazaarItem withPriceOverride(Integer price) {
        return new BazaarItem(card, type, traitEffect, carryCardType, price);
    }

    public int getPrice() {
        return priceOverride != null ? priceOverride : BazaarPricing.getCardPrice(card);
    }

    public int getBasePrice() {
        return BazaarPricing.getCardPrice(card);
    }

    public boolean isDiscounted() {
        return getPrice() < getBasePrice();
    }
}
