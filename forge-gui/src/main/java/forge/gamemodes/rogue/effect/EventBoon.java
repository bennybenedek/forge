package forge.gamemodes.rogue.effect;

import forge.game.player.RegisteredPlayer;
import forge.gamemodes.rogue.*;
import forge.gamemodes.rogue.npc.NPC;
import forge.item.PaperCard;
import forge.util.MyRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum EventBoon implements RogueEffect {

    HEALERS_TOUCH("healers_touch", "Healer's Touch", "Gain 8 life, lose 5 gold.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            run.setCurrentLife(run.getCurrentLife() + 8);
            run.setCurrentGold(run.getCurrentGold() - 5);
        }
    },
    RIFT_ENERGY("rift_energy", "Rift Energy", "Gain 5 gold.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            run.setCurrentGold(run.getCurrentGold() + 5);
        }
    },
    CARAVAN_ROB("caravan_rob", "Caravan Plunder", "Lose 3 life, gain 8 gold.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            run.setCurrentLife(run.getCurrentLife() - 3);
            run.setCurrentGold(run.getCurrentGold() + 8);
        }
    },
    BROWSE_WARES("browse_wares", "Browse Wares", "Opens a bazaar.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            ctx.trigger = NodeResultContext.ActionTriggerType.BAZAAR;
        }
    },
    PLANAR_SHUFFLE("planar_shuffle", "Planar Shuffle",
            "Remove 3 random cards (excluding basic lands) and replace them with cards from your Reward Pool.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            RogueDeck rogueDeck = run.getSelectedRogueDeck();
            if (rogueDeck == null) return;

            // Get non-commander, non-basic-land cards from deck
            List<PaperCard> deckCards = run.getCurrentDeck().getMain().toFlatList();
            String commanderName = rogueDeck.getCommanderCardName();
            deckCards.removeIf(c -> c.getName().equals(commanderName)
                    || c.getRules().getType().isBasicLand());
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
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
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
            ctx.trigger = NodeResultContext.ActionTriggerType.PLANEBOUND;
        }
    },
    PLANAR_EXCHANGE("planar_exchange", "Planar Sacrifice",
            "Choose 3 cards to remove (excluding basic lands), then receive 3 random cards from your Reward Pool.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            ctx.trigger = NodeResultContext.ActionTriggerType.CARD_REMOVAL;
            ctx.removeCount = 3;
            ctx.drawCount = 3;
        }
    },
    PLANAR_SACRIFICE("planar_sacrifice", "Planar Sacrifice",
        "Choose 3 cards (excluding basic lands) to remove from your deck.",
        EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            ctx.trigger = NodeResultContext.ActionTriggerType.CARD_REMOVAL;
            ctx.removeCount = 3;
            ctx.drawCount = 0;
        }
    },
    GAIN_WOUND("gain_wound", "Gain Wound", "Gain a random Wound.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            List<Wound> available = new ArrayList<>(List.of(Wound.values()));
            List<RogueEffect> active = run.getActiveWounds();
            available.removeIf(w -> active.stream().anyMatch(a -> a == w));
            if (available.isEmpty()) return;
            Wound wound = available.get(MyRandom.getRandom().nextInt(available.size()));
            run.addWound(wound);
            ctx.gainedWound = wound;
        }
    },
    FIND_CHEST("find_chest", "Hidden Chest", "You find a hidden chest.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            ctx.trigger = NodeResultContext.ActionTriggerType.CHEST;
        }
    },
    FIND_SANCTUM("find_sanctum", "Hidden Sanctum", "You discover a hidden Sanctum.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            ctx.trigger = NodeResultContext.ActionTriggerType.SANCTUM;
        }
    },
    LOSE_ALL_GOLD("lose_all_gold", "Lose All Gold", "You lose all your gold.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            run.setCurrentGold(0);
        }
    },
    LOSE_ALL_ECHOES("lose_all_echoes", "Lose All Echoes", "You lose all your echoes.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            RogueMetaProgress.getInstance().setTotalEchoes(0);
        }
    },
    NOTHING("nothing", "Nothing", "No effect.",
            EffectType.ONESHOT),
    MEET_NPC_TYVAR("meet_npc_tyvar", "Meet Tyvar Kell", "Tyvar Kell offers to train your Commander.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            RogueMetaProgress progress = RogueMetaProgress.getInstance();
            if (progress.getNPCLevel(NPC.TYVAR.id) < 1) {
                progress.setNPCLevelIfHigher(NPC.TYVAR.id, 1);
            }
        }
    },

    // === PERMANENT effects (stored in run, dispatched via RogueEffectComposite) ===

    COMMANDER_BOOST("commander_boost", "Commander's Might", "Your Commander gets +1/+1 for the rest of the Run.",
            EffectType.PERMANENT) {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToCommandZone("Event - Commander's Might", human);
        }
    },

    // === CONSUME effects (stored in run, dispatched once, then removed) ===

    LOST_CONNECTION("lost_connection", "Lost Connection", "You may not cast your Commander in the next match.",
            EffectType.CONSUME) {
        @Override
        public int getChargesForRank(int rank) { return 1; }

        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToCommandZone("Event - Lost Connection", human);
            run.consumeEffect(getId());
        }
    },
    SKIP_REWARDS("skip_rewards", "Distortion", "You skip all rewards after your next match.",
            EffectType.CONSUME) {
        @Override
        public int getChargesForRank(int rank) { return 1; }

        @Override
        public void onBeforeRewards(RewardContext ctx, RogueRun run) {
            ctx.skipRewards = true;
            run.consumeEffect(getId());
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

    public void applyEffect(RogueRun run, NodeResultContext ctx) { /* Override in ONESHOT constants to apply immediate event effects. */}

    @Override
    public EffectType getEffectType() { return effectType; }

    @Override
    public String getId() { return id; }

    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getDescription() { return description; }

    public static EventBoon fromId(String id) {
        for (EventBoon eb : values())
            if (eb.id.equals(id)) return eb;
        return null;
    }
}
