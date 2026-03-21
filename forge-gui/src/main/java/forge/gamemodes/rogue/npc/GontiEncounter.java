package forge.gamemodes.rogue.npc;

import forge.gamemodes.rogue.RogueConfig;
import forge.gamemodes.rogue.RogueMetaProgress;
import forge.item.PaperCard;
import forge.model.FModel;
import forge.util.MyRandom;
import java.util.List;

/**
 * Gonti, Lord of Luxury — Bazaar Shop Owner NPC.
 * LEVEL_0: With some probability, injects a purchasable item into the bazaar. On purchase, progresses to level 1.
 * LEVEL_1: Applies a random discount to 2 bazaar cards.
 */
public enum GontiEncounter implements NPCEncounter {

    LEVEL_0(0) {
        private static final String CURIO_CARD_NAME = "Tarnished Relic";
        private static final int CURIO_PRICE = 10;
        private static final double CURIO_SPAWN_CHANCE = 0.5;

        @Override
        public void onBeforeBazaar(BazaarContext ctx) {
            if (MyRandom.getRandom().nextDouble() >= CURIO_SPAWN_CHANCE) return;
            RogueConfig.loadRogueCards();
            PaperCard curio = FModel.getMagicDb().getCommonCards().getCard(CURIO_CARD_NAME);
            if (curio == null) return;
            ctx.injectedCards.add(curio);
            ctx.priceOverrides.put(CURIO_CARD_NAME, CURIO_PRICE);
        }

        @Override
        public NPCContext onAfterBazaarPurchase(BazaarContext ctx) {
            boolean boughtCurio = ctx.purchasedCards.stream()
                    .anyMatch(c -> c.getName().equals(CURIO_CARD_NAME));
            if (!boughtCurio) return null;
            RogueMetaProgress.getInstance().setNPCLevel(NPC.GONTI.id, 1);
            return buildContext(
                "A figure steps out from behind the bazaar stall, eyes wide with both disbelief and fascination. " +
                "\"You... you actually bought it? Ha!! That thing has been sitting there for ages! " +
                "Nobody ever even looks at it twice!\" The figure composes itself, extending a hand. " +
                "\"I'm Gonti. And anyone willing to pay ten gold for that worthless piece of junk " +
                "deserves my personal attention. How about this...from now on you'll get some special " +
                "discounts whenever you set foot in here again. Oh, and feel free to buy other stuff " +
                "nobody else wants, I'll promise you won't regret it.\"",
                List.of(new NPCContext.NPCChoice("Accept", null))
            );
        }
    },

    LEVEL_1(1) {
        private static final int DISCOUNT_AMOUNT = 4;

        @Override
        public void onBeforeBazaar(BazaarContext ctx) {
            // Discount is applied by the bazaar code after cards are drawn,
            // since we don't know the card names at this point.
            // We store the discount info on the context for the bazaar to apply.
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
