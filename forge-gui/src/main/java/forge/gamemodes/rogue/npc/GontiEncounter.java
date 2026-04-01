package forge.gamemodes.rogue.npc;

import forge.gamemodes.rogue.RogueConfig;
import forge.item.PaperCard;
import forge.model.FModel;
import java.util.List;

/**
 * Gonti, Lord of Luxury — Bazaar Shop Owner NPC.
 *
 * LEVEL_0 (requiredLevel=0): Increment Gonti's level on each bazaar entry.
 * LEVEL_2 (requiredLevel=2): Always inject the Tarnished Relic; if the relic is bought, promote to level 3.
 * LEVEL_3 (requiredLevel=3): Apply a discount after the relic has been purchased.
 */
public enum GontiEncounter implements NPCEncounter {

    /** Increment level on every bazaar entry (no relic, no discount). */
    LEVEL_0(0) {
        @Override
        public void onBeforeBazaar(BazaarContext ctx) {
            // Increment Gonti's level each time the bazaar opens.
            incrementNpcLevel();
        }
    },

    /** Level 2 – always inject the Tarnished Relic. Promotion occurs only if the relic is bought. */
    LEVEL_2(2) {
        private static final String CURIO_CARD_NAME = "Tarnished Relic";
        private static final int CURIO_PRICE = 10;

        @Override
        public void onBeforeBazaar(BazaarContext ctx) {
            // Guaranteed relic injection while at level 2.
            RogueConfig.loadRogueCards();
            PaperCard curio = FModel.getMagicDb().getCommonCards().getCard(CURIO_CARD_NAME);
            if (curio != null) {
                ctx.injectedCards.add(curio);
                ctx.priceOverrides.put(CURIO_CARD_NAME, CURIO_PRICE);
            }
        }

        @Override
        public NPCContext onAfterBazaarPurchase(BazaarContext ctx) {
            boolean boughtCurio = ctx.purchasedCards.stream()
                    .anyMatch(c -> c.getName().equals(CURIO_CARD_NAME));
            if (!boughtCurio) {
                return null; // relic not bought – stay at level 2, no dialog.
            }
            // Relic bought – promote to level 3 so discount becomes available next bazaar.
            incrementNpcLevel();
            return buildContext(
                "A figure steps out from behind the bazaar stall, eyes wide with both disbelief and fascination. " +
                "\"You... you actually bought it? Ha!! That thing has been sitting there for ages! " +
                "Nobody ever even looks at it twice!\" The figure composes itself, extending a hand. " +
                "\"I'm Gonti. And anyone willing to pay ten gold for that worthless piece of junk " +
                "deserves my personal attention. How about this...from now on you'll get some special " +
                "discounts whenever you set foot in here again. Oh, and feel free to buy other stuff " +
                 "nobody else wants, I'll promise you won't regret it.",

                List.of(new NPCContext.NPCChoice("Accept", null))
            );
        }
    },

    /** Level 3 – apply discount after the relic has been purchased. */
    LEVEL_3(3) {
        private static final int DISCOUNT_AMOUNT = 4;

        @Override
        public void onBeforeBazaar(BazaarContext ctx) {
            // Apply discount after the relic purchase.
            ctx.discountCount = 2;
            ctx.discountAmount = DISCOUNT_AMOUNT;
        }
    };

    private final int requiredLevel;

    GontiEncounter(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    @Override
    public NPC getNpc() { return NPC.GONTI; }

    @Override
    public int getRequiredLevel() { return requiredLevel; }
}
