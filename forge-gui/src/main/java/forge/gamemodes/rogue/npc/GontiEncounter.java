package forge.gamemodes.rogue.npc;

import forge.gamemodes.rogue.RogueConfig;
import forge.gamemodes.rogue.RogueRun;
import forge.gamemodes.rogue.effect.BazaarContext;
import forge.gamemodes.rogue.effect.BazaarItem;
import forge.item.PaperCard;
import forge.model.FModel;
import java.util.List;

/**
 * Gonti, Lord of Luxury - Bazaar Shop Owner NPC.
 *
 * LEVEL_0 (requiredLevel=0): Increment Gonti's level on each bazaar entry.
 * LEVEL_2 (requiredLevel=2): Offer the Tarnished Relic; if bought, promote to level 3.
 * LEVEL_3 (requiredLevel=3): Apply discounts and count two Bazaar visits toward Gonti's next item.
 * LEVEL_5 (requiredLevel=5): Offer another Gonti curio; if bought, promote to level 6.
 * LEVEL_6 (requiredLevel=6): Add Gonti's special selection to future Bazaars.
 */
public enum GontiEncounter implements NPCEncounter {

    /** Increment level on every bazaar entry before the first curio offer. */
    BEFORE_REVEAL(0) {
        @Override
        public void onBeforeBazaar(BazaarContext ctx, RogueRun run) {
            incrementNpcLevel();
        }
    },

    /** Level 2 - always inject the Gonti Curio - Tarnished Relic. */
    OFFERING_TARNISHED_RELIC(2) {
        private static final String CURIO_CARD_NAME = "Gonti Curio - Tarnished Relic";
        private static final int CURIO_PRICE = 6;

        @Override
        public void onBeforeBazaar(BazaarContext ctx, RogueRun run) {
            PaperCard curio = getRogueCard(CURIO_CARD_NAME);
            if (curio != null) {
                ctx.inventory.add(BazaarItem.forCurio(curio, CURIO_PRICE));
            }
        }

        @Override
        public NPCContext onAfterBazaarPurchase(BazaarContext ctx) {
            if (!boughtCurio(ctx, CURIO_CARD_NAME)) {
                return null;
            }

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

    /** Level 3 and 4 - apply discounts while counting Bazaar visits toward Gonti's next item. */
    OFFERING_DISCOUNT(3) {
        @Override
        public void onBeforeBazaar(BazaarContext ctx, RogueRun run) {
            applyDiscount(ctx);
            incrementNpcLevel();
        }
    },

    /** Level 5 - offer another worthless item. Promotion occurs only if the item is bought. */
    OFFERING_DUSTY_BAUBLE(5) {
        private static final String CURIO_CARD_NAME = "Gonti Curio - Dusty Bauble";
        private static final int CURIO_PRICE = 8;

        @Override
        public void onBeforeBazaar(BazaarContext ctx, RogueRun run) {
            applyDiscount(ctx);
            PaperCard curio = getRogueCard(CURIO_CARD_NAME);
            if (curio != null) {
                ctx.inventory.add(BazaarItem.forCurio(curio, CURIO_PRICE));
            }
        }

        @Override
        public NPCContext onAfterBazaarPurchase(BazaarContext ctx) {
            if (!boughtCurio(ctx, CURIO_CARD_NAME)) {
                return null;
            }

            incrementNpcLevel();
            return buildContext(
                "Gonti freezes when the bauble changes hands, then leans over the stall with a grin " +
                "that is far too sharp for such a harmless sale. \"Again? You bought another one? " +
                "I was certain even you would notice that this one does even less than the last. " +
                "Remarkable. Truly remarkable.\" He lowers his voice. \"Very well. Next time you find " +
                "me at a bazaar, ask for the special selection. I keep the stranger pieces away from " +
                "ordinary customers.\"",

                List.of(new NPCContext.NPCChoice("Accept", null))
            );
        }
    },

    /** Level 6 - apply discounts and add Gonti's special selection to the Bazaar. */
    OFFERING_SPECIAL_SELECTION(6) {
        @Override
        public void onBeforeBazaar(BazaarContext ctx, RogueRun run) {
            applyDiscount(ctx);
            ctx.offersTraits = true;
            ctx.offersCarryCards = true;
        }
    };

    private static final int DISCOUNT_AMOUNT = 4;

    private final int requiredLevel;

    GontiEncounter(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    @Override
    public NPC getNpc() { return NPC.GONTI; }

    @Override
    public int getRequiredLevel() { return requiredLevel; }

    private static PaperCard getRogueCard(String cardName) {
        RogueConfig.loadRogueCards();
        return FModel.getMagicDb().getCommonCards().getCard(cardName);
    }

    private static void applyDiscount(BazaarContext ctx) {
        ctx.discountCount = 2;
        ctx.discountAmount = DISCOUNT_AMOUNT;
    }

    private static boolean boughtCurio(BazaarContext ctx, String cardName) {
        return ctx.purchasedItems.stream()
            .anyMatch(item -> item.type() == BazaarItem.Type.CURIO
                && item.card().getName().equals(cardName));
    }
}
