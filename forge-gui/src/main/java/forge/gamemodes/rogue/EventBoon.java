package forge.gamemodes.rogue;

import forge.game.player.RegisteredPlayer;
import forge.item.PaperCard;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum EventBoon implements RogueEffect {

    HEALERS_TOUCH("healers_touch", "Healer's Touch", "Gain 8 life, lose 5 gold.",
            EffectType.ONESHOT) {
        @Override
        public void consume(RogueRun run, EventChoiceContext ctx) {
            run.setCurrentLife(run.getCurrentLife() + 8);
            run.setCurrentGold(run.getCurrentGold() - 5);
        }
    },
    RIFT_ENERGY("rift_energy", "Rift Energy", "Gain 10 gold.",
            EffectType.ONESHOT) {
        @Override
        public void consume(RogueRun run, EventChoiceContext ctx) {
            run.setCurrentGold(run.getCurrentGold() + 10);
        }
    },
    CARAVAN_ROB("caravan_rob", "Caravan Plunder", "Lose 3 life, gain 8 gold.",
            EffectType.ONESHOT) {
        @Override
        public void consume(RogueRun run, EventChoiceContext ctx) {
            run.setCurrentLife(run.getCurrentLife() - 3);
            run.setCurrentGold(run.getCurrentGold() + 8);
        }
    },
    BROWSE_WARES("browse_wares", "Browse Wares", "Opens a bazaar.",
            EffectType.ONESHOT) {
        @Override
        public void consume(RogueRun run, EventChoiceContext ctx) {
            ctx.trigger = EventChoiceContext.NodeTriggerType.BAZAAR;
        }
    },
    DECK_SWAP("deck_swap", "Planar Shuffle",
            "Remove 3 random cards and replace them with cards from your Reward Pool.",
            EffectType.ONESHOT) {
        @Override
        public void consume(RogueRun run, EventChoiceContext ctx) {
            RogueDeck rogueDeck = run.getSelectedRogueDeck();
            if (rogueDeck == null) return;

            // Get non-commander cards from deck
            List<PaperCard> deckCards = run.getCurrentDeck().getMain().toFlatList();
            String commanderName = rogueDeck.getCommanderCardName();
            deckCards.removeIf(c -> c.getName().equals(commanderName));
            if (deckCards.isEmpty()) return;

            // Pick up to 3 random cards to remove
            Collections.shuffle(deckCards);
            int swapCount = Math.min(3, deckCards.size());
            List<PaperCard> removed = new ArrayList<>(deckCards.subList(0, swapCount));

            // Remove from deck
            for (PaperCard card : removed) {
                run.getCurrentDeck().getMain().remove(card);
            }

            // Draw same count from reward pool and add to deck
            List<PaperCard> added = rogueDeck.drawRewardOptions(swapCount, null);
            run.addCardsToRun(added);
            rogueDeck.removeFromRewardPool(added);

            // Store for result display
            ctx.removedCards = removed;
            ctx.addedCards = added;
        }
    },
    SURPRISE_FIGHT("surprise_fight", "Ambush!", "Fight a random Planebound on a random Plane!",
            EffectType.ONESHOT) {
        @Override
        public void consume(RogueRun run, EventChoiceContext ctx) {
            List<RoguePlanebound> all = RogueConfig.loadPlanebounds();
            all.removeIf(p -> p.type() != RoguePlaneboundType.NORMAL);
            Collections.shuffle(all);
            if (all.isEmpty()) return;
            RoguePlanebound opponent = all.get(0);

            List<PaperCard> planes = RogueConfig.getAllPlanes().toFlatList();
            Collections.shuffle(planes);
            String randomPlaneName = planes.isEmpty() ? opponent.planeName() : planes.get(0).getName();

            ctx.planebound = new RoguePlanebound(randomPlaneName, opponent.planeboundName(),
                    opponent.deckPath(), opponent.avatarIndex(), opponent.type());
            ctx.trigger = EventChoiceContext.NodeTriggerType.PLANEBOUND;
        }
    },
    NOTHING("nothing", "Nothing", "No effect.",
            EffectType.ONESHOT),

    // === PERMANENT effects (stored in run, dispatched via RogueEffectComposite) ===

    COMMANDER_BOOST("commander_boost", "Commander's Might", "Your Commander gets +1/+1 for the rest of the Run.",
            EffectType.PERMANENT) {
        @Override
        public void onMatchStart(RegisteredPlayer human, RogueRun run) {
            RogueEffect.addCustomCardToCommandZone("Rogue - Commander Boost", human);
        }
    };

    private final String id;
    private final String displayName;
    private final String description;
    private final EffectType effectType;

    EventBoon(String id, String displayName, String description, EffectType effectType) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.effectType = effectType;
    }

    /** Override in ONESHOT constants to apply immediate effects. */
    public void consume(RogueRun run, EventChoiceContext ctx) {}

    @Override
    public EffectType getEffectType() { return effectType; }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    public static EventBoon fromId(String id) {
        for (EventBoon eb : values())
            if (eb.id.equals(id)) return eb;
        return null;
    }
}
