package forge.gamemodes.rogue.effect;

import forge.game.player.RegisteredPlayer;
import forge.gamemodes.rogue.*;
import forge.gamemodes.rogue.npc.BazaarContext;
import forge.gamemodes.rogue.npc.NPC;
import forge.item.PaperCard;
import forge.util.MyRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum EventBoon implements RogueEffect {

    HEALER_POTION("healer_potion", "Healer's Potion", "Gain 8 life, lose 5 gold.",
            EffectType.ONESHOT, 5) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            run.gainLifeUpToMax(8);
            run.spendGold(getGoldCost());
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.hasEnoughGold(getGoldCost()) && run.getCurrentLife() < run.getMaxLife();
        }

        @Override
        public String getUnavailableReason(RogueRun run) {
            if (!run.hasEnoughGold(getGoldCost())) {
                return getInsufficientGoldReason();
            }
            if (run.getCurrentLife() >= run.getMaxLife()) {
                return "You are already at maximum life.";
            }
            return null;
        }
    },
    HEALER_TREAT_WOUNDS("healer_treatment", "Healer's Treatment", "Clear all {{Wound}}s, lose 3 gold.",
        EffectType.ONESHOT, 3) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            run.clearWounds();
            run.spendGold(getGoldCost());
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.hasEnoughGold(getGoldCost()) && !run.getActiveWounds().isEmpty();
        }

        @Override
        public String getUnavailableReason(RogueRun run) {
            if (!run.hasEnoughGold(getGoldCost())) {
                return getInsufficientGoldReason();
            }
            if (run.getActiveWounds().isEmpty()) {
                return "You have no active wounds.";
            }
            return null;
        }
    },
    HEALER_STRENGTHEN("healer_strengthen", "Healer's Strength", "Gain 10 {{Max. Life}}, lose 7 Gold.",
        EffectType.ONESHOT, 7) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            run.setMaxLife(run.getMaxLife() + 10);
            run.spendGold(getGoldCost());
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.hasEnoughGold(getGoldCost());
        }

        @Override
        public String getUnavailableReason(RogueRun run) {
            return run.hasEnoughGold(getGoldCost()) ? null : getInsufficientGoldReason();
        }
    },
    PLANAR_RIFT_ENERGY("planar_rift_energy", "Planar Rift Energy", "Gain 6 gold.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            run.addGold(5);
        }
    },
    PLANAR_RIFT_BOOST("planar_rift_boost", "Planar Rift - Commander Boost", "Your Commander gets +1/+1 for the rest of the Run.",
        EffectType.PERMANENT) {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToCommandZone("Planar Rift - Commander Boost", human);
        }
    },
    CARAVAN_ROB("caravan_rob", "Caravan Plunder", "Lose 3 life, gain 8 gold.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            run.loseLife(3);
            run.addGold(8);
        }
    },
    CARAVAN_BROWSE("caravan_browse", "Browse Wares", "Opens a {{Bazaar}}.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            ctx.trigger = NodeResultContext.ActionTriggerType.BAZAAR;
        }
    },
    GAMECHANGER_TRUST("gamechanger_trust", "Trade for Gamechangers",
            "Remove 3 random cards from your deck and replace them with chosen cards from the Gamechanger list.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            List<PaperCard> gamechangerCards = run.getGamechangerCardsForActiveCommander();
            List<PaperCard> deckCards = run.getSelectableDeckCards();
            int swapCount = Math.min(3, Math.min(deckCards.size(), gamechangerCards.size()));
            if (swapCount <= 0) {
                return;
            }

            Collections.shuffle(deckCards, MyRandom.getRandom());
            List<PaperCard> removed = new ArrayList<>(deckCards.subList(0, swapCount));
            for (PaperCard card : removed) {
                run.getCurrentDeck().getMain().remove(card);
            }

            ctx.removedCards = removed;
            ctx.candidateCards = gamechangerCards;
            ctx.addCount = Math.min(swapCount, gamechangerCards.size());
            if (ctx.addCount > 0) {
                ctx.trigger = NodeResultContext.ActionTriggerType.CARD_ADDITION;
            }
        }
    },
    GAMECHANGER_CHOOSE("gamechanger_choose", "Browse Gamechangers",
            "Shop from a selection of Gamechanger cards at doubled prices.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            List<PaperCard> gamechangerCards = run.getGamechangerCardsForActiveCommander();
            if (gamechangerCards.isEmpty()) {
                return;
            }

            BazaarContext bazaarContext = new BazaarContext();
            bazaarContext.title = "Gamechanger Shop";
            bazaarContext.inventory.addAll(gamechangerCards);
            for (PaperCard card : gamechangerCards) {
                bazaarContext.priceOverrides.put(card.getName(),
                    BazaarPricing.getCardPrice(card) * 2);
            }

            ctx.bazaarContext = bazaarContext;
            ctx.trigger = NodeResultContext.ActionTriggerType.BAZAAR;
        }
    },
    PLANAR_TRIBUTE_REPLACE("planar_shuffle", "Planar Shuffle",
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
            run.addCardsToRun(added, false);
            rogueDeck.removeFromCardPools(added);

            // Store for result display
            ctx.removedCards = removed;
            ctx.addedCards = added;
        }
    },
    PLANAR_TRIBUTE_REMOVE("planar_sacrifice", "Planar Sacrifice",
        "Choose 3 cards (excluding basic lands) to remove from your deck.",
        EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            ctx.trigger = NodeResultContext.ActionTriggerType.CARD_REMOVAL;
            ctx.removeCount = 3;
            ctx.drawCount = 0;
        }
    },
    AMBUSH_FIGHT("ambush_fight", "Fight!", "Fight a random Planebound on a random Plane!",
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
    AMBUSH_BRIBE("ambush_bribe", "Lose 4 Gold", "You lose 4 gold.",
        EffectType.ONESHOT, 4) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            run.spendGold(getGoldCost());
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.hasEnoughGold(getGoldCost());
        }

        @Override
        public String getUnavailableReason(RogueRun run) {
            return run.hasEnoughGold(getGoldCost()) ? null : getInsufficientGoldReason();
        }
    },
    PLANAR_EXCHANGE("planar_exchange", "Planar Exchange",
            "Choose 3 cards to remove (excluding basic lands), then receive 3 random cards from your Reward Pool.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            ctx.trigger = NodeResultContext.ActionTriggerType.CARD_REMOVAL;
            ctx.removeCount = 3;
            ctx.drawCount = ctx.removeCount;
        }
    },
    THORNS_ENDURE("thorns_endure", "Gain Wound", "Gain a random {{Wound}}.",
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
    THORNS_PRESS("thorns_press", "Lose 4 Life", "You lose 4 life.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            run.loseLife(4);
        }
    },
    FIND_CHEST("find_chest", "Hidden Chest", "You find a hidden chest.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            ctx.trigger = NodeResultContext.ActionTriggerType.CHEST;
        }
    },
    FIND_SANCTUM("find_sanctum", "Hidden Sanctum", "You discover a hidden {{Sanctum}}.",
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

    LOST_CANNOT_CAST("lost_cannot_cast", "Lost Connection - Cannot Cast Commander", "You may not cast your Commander in the next match.",
            EffectType.CONSUME) {
        @Override
        public int getChargesForRank(int rank) { return 1; }

        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToCommandZone("Event - Lost Connection", human);
            run.consumeEffect(getId());
        }
    },
    LOST_WEAKENED("lost_weakened", "Lost Connection - Commander Weakened", "Your Commander gets -1/-1 for the rest of the Run.",
        EffectType.PERMANENT) {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToCommandZone("Lost Connection - Commander Weakened", human);
        }
    },
    DISTORTION_SKIP_REWARDS("skip_rewards", "Distortion", "You skip all rewards after your next match.",
            EffectType.CONSUME) {
        @Override
        public int getChargesForRank(int rank) { return 1; }

        @Override
        public void onBeforeRewards(MatchRewardContext ctx, RogueRun run) {
            ctx.skipRewards = true;
            run.consumeEffect(getId());
        }
    },
    DISTORTION_FADED_REWARDS("faded_rewards", "Distortion - Faded Rewards",
            "After your next 2 matches, gain 1 less gold and see 3 fewer non-mythic cards in Card Rewards.",
            EffectType.CONSUME) {
        @Override
        public int getChargesForRank(int rank) { return 2; }

        @Override
        public void onBeforeRewards(MatchRewardContext ctx, RogueRun run) {
            ctx.goldRewardAdjustment -= 1;
            ctx.nonMythicCardCountAdjustment -= 3;
            run.consumeEffect(getId());
        }
    };

    private final String id;
    private final String displayName;
    private final String description;
    private final EffectType effectType;
    private final int goldCost;

    EventBoon(String id, String displayName, String description, EffectType effectType) {
        this(id, displayName, description, effectType, 0);
    }

    EventBoon(String id, String displayName, String description, EffectType effectType, int goldCost) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.effectType = effectType;
        this.goldCost = goldCost;
    }

    public void applyEffect(RogueRun run, NodeResultContext ctx) { /* Override in ONESHOT constants to apply immediate event effects. */}

    @Override
    public EffectType getEffectType() { return effectType; }

    @Override
    public String getId() { return id; }

    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getRawDescription() { return description; }

    public int getGoldCost() { return goldCost; }

    public boolean isChoiceAvailable(RogueRun run) { return true; }

    public String getUnavailableReason(RogueRun run) {
        return null;
    }

    protected String getInsufficientGoldReason() {
        return goldCost > 0 ? "You need " + goldCost + " Gold." : null;
    }

    public static EventBoon fromId(String id) {
        for (EventBoon eb : values())
            if (eb.id.equals(id)) return eb;
        return null;
    }
}
