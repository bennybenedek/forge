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
import forge.util.Aggregates;
import forge.util.MyRandom;
import java.util.ArrayList;
import java.util.Collections;
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
            RogueEffect.addCardToCommandZone("Planar Rift - Commander Boost", human);
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
    DRIFTED_RESCUE("drifted_rescue", "Rescue Pilot", "Gain a random Human {{Fellow}}.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            Predicate<PaperCard> humanFilter = PaperCardPredicates.fromRules(
                    CardRulesPredicates.IS_CREATURE.and(CardRulesPredicates.subType("Human")));
            List<PaperCard> fellows = run.getAllCardsForActiveCommander(humanFilter);
            PaperCard fellow = fellows.isEmpty() ? null : Aggregates.random(fellows);
            if (fellow == null) {
                return;
            }

            run.addCarryCard(fellow.getName(), RogueRun.CarryCardType.FELLOW, getId());
            ctx.addedCards = List.of(fellow);
        }
    },
    DRIFTED_STEAL("drifted_steal", "Steal Vehicle", "Gain a random Vehicle {{Item}}.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            Predicate<PaperCard> vehicleFilter = PaperCardPredicates.fromRules(
                    CardRulesPredicates.IS_ARTIFACT.and(CardRulesPredicates.subType("Vehicle")));
            List<PaperCard> vehicles = run.getAllCardsForActiveCommander(vehicleFilter);
            PaperCard vehicle = vehicles.isEmpty() ? null : Aggregates.random(vehicles);
            if (vehicle == null) {
                return;
            }

            run.addCarryCard(vehicle.getName(), RogueRun.CarryCardType.ITEM, getId());
            ctx.addedCards = List.of(vehicle);
        }
    },
    BENDING_WALK("crossroads_walk", "Walk with a Companion", "Gain a random Ally {{Fellow}}.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            Predicate<PaperCard> allyFilter = card -> "TLA".equalsIgnoreCase(card.getEdition())
                    && PaperCardPredicates.fromRules(
                        CardRulesPredicates.IS_CREATURE.and(CardRulesPredicates.subType("Ally")))
                    .test(card);
            List<PaperCard> allies = run.getAllCardsForActiveCommander(allyFilter);
            PaperCard ally = allies.isEmpty() ? null : Aggregates.random(allies);
            if (ally == null) {
                return;
            }

            run.addCarryCard(ally.getName(), RogueRun.CarryCardType.FELLOW, getId());
            ctx.addedCards = List.of(ally);
        }
    },
    BENDING_STUDY("crossroads_study", "Study the Scrolls",
            "Gain 3 random Lesson {{Scroll}}s.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            Predicate<PaperCard> lessonFilter = card -> "TLA".equalsIgnoreCase(card.getEdition())
                    && (card.getRules().getType().isInstant() || card.getRules().getType().isSorcery())
                    && card.getRules().getType().hasSubtype("Lesson");
            List<PaperCard> lessons = run.getAllCardsForActiveCommander(lessonFilter);
            if (lessons.isEmpty()) {
                return;
            }

            Collections.shuffle(lessons, MyRandom.getRandom());
            List<PaperCard> added = new ArrayList<>(lessons.subList(0, Math.min(3, lessons.size())));
            for (PaperCard card : added) {
                run.addCarryCard(card.getName(), RogueRun.CarryCardType.SCROLL, getId());
            }
            ctx.addedCards = added;
        }
    },
    GROUND_ZERO_SPECIAL("ground_zero_special", "You're S.P.E.C.I.A.L.", "Add all 7 Bobblehead artifacts to your deck. ![[Charisma Bobblehead]] ![[Intelligence Bobblehead]] ![[Strength Bobblehead]]",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            Predicate<PaperCard> bobbleheadFilter = PaperCardPredicates.fromRules(
                    CardRulesPredicates.IS_ARTIFACT.and(CardRulesPredicates.subType("Bobblehead")));
            List<PaperCard> bobbleheads = run.getAllCardsForActiveCommander(bobbleheadFilter);
            if (bobbleheads.isEmpty()) {
                return;
            }

            run.addCardsToDeck(bobbleheads, false);
            ctx.addedCards = bobbleheads;
        }
    },
    GROUND_ZERO_REPAIR("ground_zero_repair", "Use Workbench",
            "Remove all artifact cards from your deck and lose all {{Item}}s. Gain 3 random PIP Robot {{Fellow}}s.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            List<PaperCard> removedArtifacts = run.removeCardsFromDeck(
                    card -> card.getRules().getType().isArtifact());
            List<PaperCard> removedItems = removeCarryCards(run, RogueRun.CarryCardType.ITEM);
            if (!removedArtifacts.isEmpty() || !removedItems.isEmpty()) {
                List<PaperCard> removedCards = new ArrayList<>(removedArtifacts);
                removedCards.addAll(removedItems);
                ctx.removedCards = removedCards;
            }

            Predicate<PaperCard> robotFilter = card -> "PIP".equalsIgnoreCase(card.getEdition())
                    && PaperCardPredicates.fromRules(
                        CardRulesPredicates.IS_CREATURE.and(CardRulesPredicates.subType("Robot")))
                    .test(card);
            List<PaperCard> robots = run.getAllCardsForActiveCommander(robotFilter);
            if (robots.isEmpty()) {
                return;
            }

            Collections.shuffle(robots, MyRandom.getRandom());
            List<PaperCard> added = new ArrayList<>(robots.subList(0, Math.min(3, robots.size())));

            for (PaperCard card : added) {
                run.addCarryCard(card.getName(), CarryCardType.FELLOW, getId());
            }

            ctx.addedCards = added;
        }
    },
    GROUND_ZERO_MUTATE("ground_zero_mutate", "Explore Wasteland", "Replace 5 random creatures in your deck with radiation mutants.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            List<PaperCard> deckCardsToRemove = run.removeRandomCardsFromDeck(
                    5, card -> card.getRules().getType().isCreature());
            if (deckCardsToRemove.isEmpty()) {
                return;
            }
            ctx.removedCards = deckCardsToRemove;

            Predicate<PaperCard> mutantFilter = card -> "PIP".equalsIgnoreCase(card.getEdition())
                    && PaperCardPredicates.fromRules(
                        CardRulesPredicates.IS_CREATURE.and(CardRulesPredicates.subType("Mutant")))
                    .test(card);
            List<PaperCard> mutants = run.getAllCardsForActiveCommander(mutantFilter);
            if (mutants.isEmpty()) {
                return;
            }

            Collections.shuffle(mutants, MyRandom.getRandom());
            List<PaperCard> added = new ArrayList<>(mutants.subList(0, Math.min(deckCardsToRemove.size(), mutants.size())));
            run.addCardsToDeck(added, false);
            ctx.addedCards = added;
        }
    },
    CROOKED_COUNSEL_FELLOWSHIP("crooked_counsel_fellowship", "Rally the Free Peoples",
            "Remove all black cards from your deck. Choose up to 9 creatures from a set of legendary Halflings, Humans, Elves, Dwarves, and Wizards to add to your deck.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            List<PaperCard> removed = run.removeCardsFromDeck(card -> card.getRules().getColor().hasBlack());
            if (!removed.isEmpty()) {
                ctx.removedCards = removed;
            }

            Predicate<PaperCard> fellowshipFilter = card -> {
                if (!("LTR".equalsIgnoreCase(card.getEdition()) || "LTC".equalsIgnoreCase(card.getEdition()))) {
                    return false;
                }
                if (!card.getRules().getType().isCreature() || !card.getRules().getType().isLegendary()) {
                    return false;
                }
                return card.getRules().getType().hasSubtype("Halfling")
                        || card.getRules().getType().hasSubtype("Human")
                        || card.getRules().getType().hasSubtype("Elf")
                        || card.getRules().getType().hasSubtype("Dwarf")
                        || card.getRules().getType().hasSubtype("Wizard");
            };
            List<PaperCard> fellowshipCards = run.getAllCardsForActiveCommander(fellowshipFilter);
            if (fellowshipCards.isEmpty()) {
                return;
            }

            Collections.shuffle(fellowshipCards, MyRandom.getRandom());
            List<PaperCard> candidates = new ArrayList<>(fellowshipCards.subList(0, Math.min(30, fellowshipCards.size())));
            ctx.candidateCards = candidates;
            ctx.addMinCount = 0;
            ctx.addMaxCount = Math.min(9, candidates.size());
            if (ctx.addMaxCount > 0) {
                ctx.trigger = NodeResultContext.ActionTriggerType.CARD_ADDITION;
            }
        }
    },
    CROOKED_COUNSEL_RING("crooked_counsel_ring", "Keep to your own path",
            "Gain the legendary artifact {{Item}} '[[The One Ring|LTR|2]]'. Lose 5 life.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            PaperCard ring = RogueConfig.getCard("The One Ring", null);
            if (ring == null) {
                return;
            }

            run.addCarryCard(ring.getName(), RogueRun.CarryCardType.ITEM, getId());
            run.loseLife(5);
            ctx.addedCards = List.of(ring);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.canAddCardToDeck(RogueConfig.getCard("The One Ring", null));
        }

        @Override
        public String getUnavailableReason(RogueRun run) {
            return isChoiceAvailable(run) ? null : "You already have The One Ring.";
        }
    },
    CROOKED_COUNSEL_NAZGUL("crooked_counsel_nazgul", "Join with the dark lord",
            "Remove 9 random creatures from your deck and replace them with 9 copies of [[Nazgûl]].",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            List<PaperCard> removed = run.removeRandomCardsFromDeck(9,
                    card -> card.getRules().getType().isCreature());
            if (!removed.isEmpty()) {
                ctx.removedCards = removed;
            }

            PaperCard nazgul = RogueConfig.getCard("Nazgûl", null);
            if (nazgul == null) {
                return;
            }

            List<PaperCard> added = new ArrayList<>(Collections.nCopies(9, nazgul));
            run.addCardsToDeck(added, false);
            ctx.addedCards = added;
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.canAddCardToDeck(RogueConfig.getCard("Nazgûl", null));
        }

        @Override
        public String getUnavailableReason(RogueRun run) {
            return isChoiceAvailable(run) ? null : "Your commander or deck does not allow Nazgûl.";
        }
    },
    GAMECHANGER_TRUST("gamechanger_trust", "Trade for Gamechangers",
            "Remove 3 random cards from your deck and replace them with chosen cards from the Gamechanger list.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            List<PaperCard> gamechangerCards = run.getGamechangerCardsForActiveCommander();
            int swapCount = Math.min(3, gamechangerCards.size());
            if (swapCount <= 0) {
                return;
            }

            List<PaperCard> removed = run.removeRandomCardsFromDeck(swapCount, null);
            if (removed.isEmpty()) {
                return;
            }
            ctx.removedCards = removed;
            ctx.candidateCards = gamechangerCards;
            ctx.addMinCount = Math.min(swapCount, gamechangerCards.size());
            ctx.addMaxCount = ctx.addMinCount;
            if (ctx.addMaxCount > 0) {
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
            "Remove 3 random cards (excluding basic lands) and replace them with cards from your {{Reward Pool}}.",
            EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            RogueDeck rogueDeck = run.getSelectedRogueDeck();
            if (rogueDeck == null) return;

            List<PaperCard> removed = run.removeRandomCardsFromDeck(3, null);
            if (removed.isEmpty()) return;
            int swapCount = removed.size();

            // Draw same count from reward pool and add to deck
            List<PaperCard> added = rogueDeck.drawRewardOptions(swapCount, null);
            run.addCardsToDeck(added, false);
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
            "Choose 3 cards to remove (excluding basic lands), then receive 3 random cards from your {{Reward Pool}}.",
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
            RogueEffect.addCardToCommandZone("Lost Connection - Depart", human);
            run.consumeEffect(getId());
        }
    },
    LOST_PERSIST("lost_weakened", "Lost Connection - Commander Weakened", "Your Commander gets -1/-1 for the rest of the Run.",
        EffectType.PERMANENT) {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToCommandZone("Lost Connection - Commander Weakened", human);
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
            List<PaperCard> commanderChoices = run.getAllCardsForActiveCommander(commanderFilter);
            if (commanderChoices.isEmpty()) {
                return;
            }

            Collections.shuffle(commanderChoices, MyRandom.getRandom());
            ctx.candidateCards = new ArrayList<>(commanderChoices.subList(0, Math.min(20, commanderChoices.size())));
            ctx.addMinCount = 1;
            ctx.addMaxCount = 1;
            ctx.addSection = DeckSection.Commander;
            ctx.replaceCurrentCardsInAddSection = true;
            ctx.trigger = NodeResultContext.ActionTriggerType.CARD_ADDITION;
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

    private static List<PaperCard> removeCarryCards(RogueRun run, RogueRun.CarryCardType type) {
        if (!run.hasCarryCardOfType(type)) {
            return List.of();
        }

        List<RogueRun.CarryCard> removedCarryCards = new ArrayList<>(run.getCarryCards().stream()
                .filter(card -> card.type() == type)
                .toList());
        run.getCarryCards().removeIf(card -> card.type() == type);

        List<PaperCard> removedCards = new ArrayList<>();
        for (RogueRun.CarryCard carryCard : removedCarryCards) {
            PaperCard card = RogueConfig.getCard(carryCard.cardName(), null);
            if (card != null) {
                removedCards.add(card);
            }
        }
        return removedCards;
    }

    public static EventEffect fromId(String id) {
        for (EventEffect eb : values())
            if (eb.id.equals(id)) return eb;
        return null;
    }
}
