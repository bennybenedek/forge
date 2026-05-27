package forge.gamemodes.rogue.effect;

import forge.deck.DeckSection;
import forge.game.player.RegisteredPlayer;
import forge.gamemodes.rogue.*;
import forge.gamemodes.rogue.RogueRun.CarryCardType;
import forge.gamemodes.rogue.npc.BazaarContext;
import forge.gamemodes.rogue.npc.NPC;
import forge.card.CardRulesPredicates;
import forge.item.PaperCard;
import forge.item.PaperCardPredicates;
import forge.util.MyRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

public enum EventEffect implements RogueEffect {

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
    HEALER_TREAT_WOUNDS("healer_treatment", "Healer's Treatment", "Clear all {{Wound}}s, lose 3 {{Gold}}.",
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
    HEALER_STRENGTHEN("healer_strengthen", "Healer's Strength", "Gain 10 {{Max. Life}}, lose 7 {{Gold}}.",
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
    PLANAR_RIFT_ENERGY("planar_rift_energy", "Planar Rift Energy", "Gain 6 {{Gold}}.",
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
            RogueEffect.addCardToCommandZone("Event Boon - Commander Boost", human);
        }
    },
    CARAVAN_ROB("caravan_rob", "Caravan Plunder", "Lose 3 life, gain 8 {{Gold}}.",
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
    DRIFTED_RESCUE("drifted_rescue", "Rescue Pilot", "Lose 3 Life. Gain a random legendary Human {{Fellow}}.",
            EffectType.ONESHOT) {
        @Override
        public Predicate<PaperCard> getCardFilter() {
            return PaperCardPredicates.fromRules(
                CardRulesPredicates.IS_CREATURE
                    .and(CardRulesPredicates.IS_LEGENDARY)
                    .and(CardRulesPredicates.subType("Human")));
        }

        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {

            run.loseLife(3);
            addCarryCards(run, ctx, getCardFilter(), 1, RogueRun.CarryCardType.FELLOW, List.of());
        }
    },
    DRIFTED_STEAL("drifted_steal", "Steal Vehicle", "Gain a random Vehicle {{Item}}.",
            EffectType.ONESHOT) {
        @Override
        public Predicate<PaperCard> getCardFilter() {
            return PaperCardPredicates.fromRules(
                CardRulesPredicates.IS_ARTIFACT.and(CardRulesPredicates.subType("Vehicle")));
        }

        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            addCarryCards(run, ctx, getCardFilter(), 1, RogueRun.CarryCardType.ITEM, List.of());
        }
    },
    BENDING_WALK("crossroads_walk", "Walk with a Companion", "Gain a random Ally {{Fellow}}.",
            EffectType.ONESHOT) {
        @Override
        public Predicate<PaperCard> getCardFilter() {
            return PaperCardPredicates.fromRules(
                CardRulesPredicates.IS_CREATURE.and(CardRulesPredicates.subType("Ally")));
        }

        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            addCarryCards(run, ctx, getCardFilter(), 1, RogueRun.CarryCardType.FELLOW, List.of());
        }
    },
    BENDING_STUDY("crossroads_study", "Study the Scrolls",
            "Gain 3 random Lesson {{Scroll}}s.",
            EffectType.ONESHOT) {
        @Override
        public Predicate<PaperCard> getCardFilter() {
            return PaperCardPredicates.fromRules(
                (CardRulesPredicates.IS_INSTANT
                    .or(CardRulesPredicates.IS_SORCERY))
                    .and(CardRulesPredicates.subType("Lesson")));
        }

        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            addCarryCards(run, ctx, getCardFilter(), 3, RogueRun.CarryCardType.SCROLL, List.of());
        }
    },
    GROUND_ZERO_SPECIAL("ground_zero_special", "You're S.P.E.C.I.A.L.", "Add all 7 Bobblehead artifacts to your deck. ![[Charisma Bobblehead|PIP|1]] ![[Intelligence Bobblehead|PIP|1]] ![[Strength Bobblehead|PIP|1]]",
            EffectType.ONESHOT) {
        @Override
        public Predicate<PaperCard> getCardFilter() {
            return PaperCardPredicates.fromRules(
                CardRulesPredicates.IS_ARTIFACT.and(CardRulesPredicates.subType("Bobblehead")));
        }

        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            String setCode = "PIP";
            int artIndex = 1;

            addCardsToDeck(run, ctx, getCardFilter(), null, null, List.of(
                new CardPrintOverride("Agility Bobblehead", setCode, artIndex),
                new CardPrintOverride("Charisma Bobblehead", setCode, artIndex),
                new CardPrintOverride("Endurance Bobblehead", setCode, artIndex),
                new CardPrintOverride("Intelligence Bobblehead", setCode, artIndex),
                new CardPrintOverride("Luck Bobblehead", setCode, artIndex),
                new CardPrintOverride("Perception Bobblehead", setCode, artIndex),
                new CardPrintOverride("Strength Bobblehead", setCode, artIndex)
            ));
        }
    },
    GROUND_ZERO_REPAIR("ground_zero_repair", "Use Workbench",
            "Remove all artifact cards from your deck and lose all {{Item}}s. Gain 3 random Robot {{Fellow}}s.",
            EffectType.ONESHOT) {
        @Override
        public Predicate<PaperCard> getCardFilter() {
            return PaperCardPredicates.fromRules(
                CardRulesPredicates.IS_CREATURE.and(CardRulesPredicates.subType("Robot")));
        }

        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            removeCardsFromDeck(
                run, ctx, PaperCardPredicates.fromRules(
                    CardRulesPredicates.IS_ARTIFACT), null);
            removeCarryCards(run, ctx, CarryCardType.ITEM);
            addCarryCards(run, ctx, getCardFilter(), 3, CarryCardType.FELLOW, List.of());
        }
    },
    GROUND_ZERO_MUTATE("ground_zero_mutate", "Explore Wasteland", "Remove 5 random creatures from your deck. For each creature removed this way, add a random radiation mutant to your deck.",
            EffectType.ONESHOT) {

        @Override
        public Predicate<PaperCard> getCardFilter() {
            Predicate<PaperCard> mutantFilter = PaperCardPredicates.fromRules(
                CardRulesPredicates.IS_CREATURE.and(CardRulesPredicates.subType("Mutant")));
            return card -> "PIP".equalsIgnoreCase(card.getEdition())
                && mutantFilter.test(card);
        }

        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            removeCardsFromDeck(run, ctx, PaperCardPredicates.fromRules(CardRulesPredicates.IS_CREATURE), 5);
            int removedCount = ctx.removedCards.size();
            addCardsToDeck(run, ctx, getCardFilter(), removedCount, null, List.of());
        }
    },
    CROOKED_COUNSEL_FELLOWSHIP("crooked_counsel_fellowship", "Rally the Free Peoples",
            "Remove all black cards from your deck. Choose up to 9 creatures from a set of legendary Halflings, Humans, Elves, Dwarves, and Wizards to add to your deck.",
            EffectType.ONESHOT) {
        @Override
        public Predicate<PaperCard> getCardFilter() {
            return PaperCardPredicates.fromRules(
                CardRulesPredicates.IS_CREATURE
                    .and(CardRulesPredicates.IS_LEGENDARY)
                    .and(CardRulesPredicates.subType("Halfling")
                        .or(CardRulesPredicates.subType("Human"))
                        .or(CardRulesPredicates.subType("Elf"))
                        .or(CardRulesPredicates.subType("Dwarf"))
                        .or(CardRulesPredicates.subType("Wizard"))));
        }

        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            removeCardsFromDeck(run, ctx, PaperCardPredicates.fromRules(CardRulesPredicates.IS_BLACK), null);
            List<PaperCard> fellowshipCards = run.getAllCardsForActiveCommander(getCardFilter());
            if (fellowshipCards.isEmpty()) {
                return;
            }

            selectCardsForDeck(run, ctx, getCardFilter(), 30, 0, 9, null);
        }
    },
    CROOKED_COUNSEL_RING("crooked_counsel_ring", "Keep to your own path",
            "Lose 5 life. Gain the legendary artifact {{Item}} [[The One Ring|LTR|2]].",
            EffectType.ONESHOT) {
        @Override
        public Predicate<PaperCard> getCardFilter() {
            return card -> card.getName().equals("The One Ring");
        }

        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            run.loseLife(5);
            addCarryCards(run, ctx, getCardFilter(), 1, RogueRun.CarryCardType.ITEM,
                List.of(new CardPrintOverride("The One Ring", "LTR", 2)));
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.canAddCardToDeck(RogueConfig.getCard("The One Ring", "LTR", 2));
        }

        @Override
        public String getUnavailableReason(RogueRun run) {
            return isChoiceAvailable(run) ? null : "You already have The One Ring.";
        }
    },
    CROOKED_COUNSEL_NAZGUL("crooked_counsel_nazgul", "Join with the dark lord",
        "Remove 9 random creatures from your deck. If you do, add 9 copies of [[Nazgûl]] to your deck.",
        EffectType.ONESHOT) {

        Predicate<PaperCard> getDeckCardFilter() {
            return PaperCardPredicates.fromRules(CardRulesPredicates.IS_CREATURE);
        }

        @Override
        public Predicate<PaperCard> getCardFilter() {
            return card -> card.getRules().getName().equals("Nazgûl");
        }

        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            if (run.getSelectableDeckCards(getDeckCardFilter()).size() < 9) {
                return;
            }
            removeCardsFromDeck(run, ctx, getDeckCardFilter(), 9);
            addCardsToDeck(run, ctx, getCardFilter(), 1, 9, List.of());
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.canAddCardToDeck(RogueConfig.getCard("Nazgûl", null, null));
        }

        @Override
        public String getUnavailableReason(RogueRun run) {
            return isChoiceAvailable(run) ? null : "Your commander or deck does not allow Nazgûl.";
        }
    },
    GAMECHANGER_TRUST("gamechanger_trust", "Trade for Gamechangers",
            "Remove 3 random cards from your deck. Choose 3 cards from the Gamechanger list to add to your deck.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            List<PaperCard> gamechangerCards = run.getGamechangerCardsForActiveCommander();
            int swapCount = Math.min(3, gamechangerCards.size());
            if (swapCount == 0) {
                return;
            }

            removeCardsFromDeck(run, ctx, null, swapCount);
            selectCardsForDeck(ctx, gamechangerCards, swapCount, swapCount);
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

            var priceOverrides = new HashMap<String, Integer>();
            for (PaperCard card : gamechangerCards) {
                priceOverrides.put(card.getName(),
                    BazaarPricing.getCardPrice(card) * 2);
            }

            openCustomBazaar(ctx, "Gamechanger Shop", gamechangerCards, priceOverrides);
        }
    },
    PLANAR_TRIBUTE_REPLACE("planar_tribute_replace", "Planar Shuffle",
            "Remove 3 random cards (excluding basic lands) from your deck. Add 3 random cards from your {{Reward Pool}}.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {

            RogueDeck rogueDeck = run.getSelectedRogueDeck();
            if (rogueDeck == null) return;

            int swapCount = 3;
            removeCardsFromDeck(run, ctx, PaperCardPredicates.fromRules(CardRulesPredicates.NOT_BASIC_LAND), swapCount);

            List<PaperCard> toAdd = rogueDeck.drawRewardOptions(swapCount, null);
            run.addCardsToDeck(toAdd, false);
            rogueDeck.removeFromCardPools(toAdd);
            ctx.addedCards = toAdd;
        }
    },
    PLANAR_TRIBUTE_REMOVE("planar_tribute_remove", "Planar Sacrifice",
        "Choose 3 cards (excluding basic lands) to remove from your deck.",
        EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            selectCardsFromDeck(ctx, 3, 0);
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
            "Choose 3 cards to remove (excluding basic lands), then receive 3 random cards from your {{Reward Pool}}.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            int exchangeCount = 3;
            selectCardsFromDeck(ctx, exchangeCount, exchangeCount);
        }
    },
    THORNS_ENDURE("thorns_endure", "Gain Wound", "Gain a random {{Wound}}.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            gainWound(run, ctx);
        }
    },
    THORNS_PRESS("thorns_press", "Lose 4 Life", "You lose 4 life.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            run.loseLife(4);
        }
    },
    AFTER_DUSK_INSANE("after_dusk_insane", "Turn Insane",
            "Add 3 random Nightmare cards to your deck.",
            EffectType.ONESHOT) {
        @Override
        public Predicate<PaperCard> getCardFilter() {
            return PaperCardPredicates.fromRules(
                CardRulesPredicates.IS_CREATURE.and(CardRulesPredicates.subType("Nightmare")));
        }

        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            addCardsToDeck(run, ctx, getCardFilter(), 3, null, List.of());
        }
    },
    AFTER_DUSK_EXPLORE("after_dusk_explore", "Explore Mansion",
            "Lose 3 life. Choose up to 5 Room cards to add to your deck.",
            EffectType.ONESHOT) {
        @Override
        public Predicate<PaperCard> getCardFilter() {
            return PaperCardPredicates.fromRules(CardRulesPredicates.subType("Room"));
        }

        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            run.loseLife(3);
            selectCardsForDeck(run, ctx, getCardFilter(), null, 0, 5, null);
        }
    },
    AFTER_DUSK_FEED("after_dusk_feed", "Feed Monsters",
            "Remove all white cards from your deck. Gain 2 random legendary Horror {{Fellow}}s.",
            EffectType.ONESHOT) {
        @Override
        public Predicate<PaperCard> getCardFilter() {
            return PaperCardPredicates.fromRules(
                CardRulesPredicates.IS_CREATURE
                    .and(CardRulesPredicates.IS_LEGENDARY)
                    .and(CardRulesPredicates.subType("Horror")));
        }

        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            removeCardsFromDeck(run, ctx, PaperCardPredicates.fromRules(CardRulesPredicates.IS_WHITE), null);
            addCarryCards(run, ctx, getCardFilter(), 2, CarryCardType.FELLOW, List.of());
        }
    },
    FIND_CHEST("find_chest", "Hidden Chest", "You find a hidden {{Chest}}.",
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
    LOSE_ALL_GOLD("lose_all_gold", "Lose All Gold", "You lose all your {{Gold}}.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            run.setCurrentGold(0);
        }
    },
    LOSE_ALL_ECHOES("lose_all_echoes", "Lose All Echoes", "You lose all your {{Echoes}}.",
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
    LOST_DEPART("lost_depart", "Lost Connection - Depart", "You may not cast your Commander in the next match.",
            EffectType.CONSUME) {
        @Override
        public int getChargesForRank(int rank) { return 1; }

        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToCommandZone("Event Boon - Depart", human);
            run.consumeEffect(getId());
        }
    },
    LOST_PERSIST("lost_weakened", "Lost Connection - Commander Weakened", "Gain the {{Boon}} **Commander Weakened**. ![[Event Boon - Commander Weakened]]",
        EffectType.PERMANENT) {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToCommandZone("Event Boon - Commander Weakened", human);
        }
    },
    LOST_REPLACE("lost_new_commander", "Lost Connection - Replace",
            "Choose a new Commander for your deck for the rest of the Run.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            int commanderColorIdentityMask = run.getCommanderColorIdentityMask();
            Predicate<PaperCard> commanderFilter = card ->
                    card.getRules().getType().isCreature()
                    && card.getRules().getType().isLegendary()
                    && card.getRules().getColorIdentity().getColor() == commanderColorIdentityMask;

            ctx.addSection = DeckSection.Commander;
            ctx.replaceCurrentCardsInAddSection = true;
            selectCardsForDeck(run, ctx, commanderFilter, 20, 1, 1, null);
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
            "After your next 2 matches, gain 1 less {{Gold}} and see 3 fewer non-mythic cards in Card Rewards.",
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
    EventEffect(String id, String displayName, String description, EffectType effectType) {
        this(id, displayName, description, effectType, 0);
    }

    EventEffect(String id, String displayName, String description, EffectType effectType, int goldCost) {
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

    protected String getInsufficientCardsReason() {
        return "Your deck doesn't contain enough cards.";
    }

    public static EventEffect fromId(String id) {
        for (EventEffect eb : values())
            if (eb.id.equals(id)) return eb;
        return null;
    }
}
