package forge.gamemodes.rogue.effect;

import forge.card.CardSplitType;
import forge.game.player.RegisteredPlayer;
import forge.gamemodes.rogue.CardReference;
import forge.gamemodes.rogue.RogueConfig;
import forge.gamemodes.rogue.RogueRun;
import forge.gamemodes.rogue.RogueRun.CarryCardType;
import forge.gamemodes.rogue.npc.NPC;
import forge.item.IPaperCard;
import forge.item.PaperCard;
import forge.item.PaperCardPredicates;
import forge.util.MyRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * NPC effects granted during run start encounters.
 * Each constant defines its own behavior explicitly.
 */
public enum NPCEffect implements RogueEffect {

    // Henzie effects
    HENZIE_CONTAINMENT("npc_henzie_containment", "Henzie's Containment",
        "Gain the {{Item}}s [[Mana Vault|2X2|1]] and [[Emrakul, the Aeons Torn]].", NPC.HENZIE,
        EffectType.ONESHOT, null) {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addCarryCards(run, ctx, List.of("Mana Vault|2X2|1", "Emrakul, the Aeons Torn"),
                CarryCardType.ITEM);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return canAddAllCarryCards(run, List.of("Mana Vault|2X2|1", "Emrakul, the Aeons Torn"));
        }
    },
    HENZIE_CONTRABAND("npc_henzie_contraband", "Henzie's Contraband",
        "Lose 3 {{Max. Life}}. Remove 3 random cards from your deck. Choose 3 out of 20 cards from the Commander Banlist to add to your deck.", NPC.HENZIE,
        EffectType.ONESHOT, null) {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            run.loseMaxLife(3);
            List<PaperCard> banlistCards = run.getBanlistCardsForActiveCommander();
            swapDeckCards(run, ctx, banlistCards);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.getMaxLife() > 3;
        }
    },
    HENZIE_PRECIOUS("npc_henzie_precious", "Henzie's Precious",
        "Gain the {{Item}}s [[Mana Crypt|2XM|1]] and [[The One Ring|LTR|2]].", NPC.HENZIE,
        EffectType.ONESHOT, null) {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addCarryCards(run, ctx, List.of("Mana Crypt|2XM|1", "The One Ring|LTR|2"),
                CarryCardType.ITEM);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return canAddAllCarryCards(run, List.of("Mana Crypt|2XM|1", "The One Ring"));
        }
    },
    HENZIE_EXQUISITE_TRAITS("npc_henzie_exquisite_traits", "Exquisite Traits",
        "Gain 2 random Chest {{Traits}}.", NPC.HENZIE,
        EffectType.ONESHOT, null) {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            List<ChestEffect> chestTraits = ChestEffect.getAvailableEffects(run, EffectType.PERMANENT);
            Collections.shuffle(chestTraits, MyRandom.getRandom());
            for (int i = 0; i < Math.min(2, chestTraits.size()); i++) {
                run.addChestEffect(chestTraits.get(i));
            }
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return !ChestEffect.getAvailableEffects(run, EffectType.PERMANENT).isEmpty();
        }
    },
    HENZIE_GAMECHANGERS("npc_henzie_gamechangers", "Henzie's Gamechangers",
        "Remove 3 random cards from your deck. Choose 3 out of 20 cards from the Gamechanger list to add to your deck.", NPC.HENZIE,
        EffectType.ONESHOT, null) {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            List<PaperCard> gamechangerCards = run.getGamechangerCardsForActiveCommander();
            swapDeckCards(run, ctx, gamechangerCards);
        }
    },
    HENZIE_MYTHICS("npc_henzie_mythics", "Henzie's Mythics",
        "Gain 3 random mythic rare cards from your {{Reward Pool}}.", NPC.HENZIE,
        EffectType.ONESHOT, null) {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addCardsFromCardRewardPool(run, ctx, 3, PaperCardPredicates.IS_MYTHIC_RARE);
        }
    },
    HENZIE_CITY("npc_henzie_city", "Henzie's City",
        TRAIT_GAIN_DESCRIPTION + " ![[City of Brass|2X2|1]]", NPC.HENZIE,
        EffectType.PERMANENT, "Henzie Trait - Henzie's City") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            RogueEffect.addCardToBattlefield("City of Brass|2X2|1", human);
        }
    },
    HENZIE_FIELD("npc_henzie_field", "Henzie's Field",
        TRAIT_GAIN_DESCRIPTION + " ![[Field of the Dead|M20|1]]", NPC.HENZIE,
        EffectType.PERMANENT, "Henzie Trait - Henzie's Field") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            RogueEffect.addCardToBattlefield("Field of the Dead|M20|1", human);
        }
    },
    HENZIE_ZONE("npc_henzie_zone", "Henzie's Zone",
        TRAIT_GAIN_DESCRIPTION + " ![[Blast Zone|CMM|1]]", NPC.HENZIE,
        EffectType.PERMANENT, "Henzie Trait - Henzie's Zone") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            RogueEffect.addCardToBattlefield("Blast Zone|CMM|1", human);
        }
    },
    HENZIE_DISSIPATION("npc_henzie_dissipation", "Dissipation",
        TRAIT_GAIN_DESCRIPTION, NPC.HENZIE,
        EffectType.PERMANENT, "Henzie Trait - Dissipation") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    HENZIE_DRAMATIC_ENTRANCE("npc_henzie_dramatic_entrance", "Dramatic Entrance",
        TRAIT_GAIN_DESCRIPTION, NPC.HENZIE,
        EffectType.PERMANENT, "Henzie Trait - Dramatic Entrance") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            List<PaperCard> permanents = new ArrayList<>();
            for (PaperCard card : human.getDeck().getMain().toFlatList()) {
                if (card.getRules().getType().isPermanent()) {
                    permanents.add(card);
                }
            }
            if (permanents.isEmpty()) {
                return;
            }
            Collections.shuffle(permanents, MyRandom.getRandom());
            List<IPaperCard> toMove = new ArrayList<>();
            for (int i = 0; i < Math.min(2, permanents.size()); i++) {
                toMove.add(permanents.get(i));
            }
            RogueEffect.moveCardsFromDeckToBattlefield(toMove, human);
        }
    },
    HENZIE_LOREMASTER("npc_henzie_loremaster", "Loremaster",
        TRAIT_GAIN_DESCRIPTION, NPC.HENZIE,
        EffectType.PERMANENT, "Henzie Trait - Loremaster") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    HENZIE_MODERATE("npc_henzie_moderate", "Moderate",
        TRAIT_GAIN_DESCRIPTION, NPC.HENZIE,
        EffectType.PERMANENT, "Henzie Trait - Moderate") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    HENZIE_TOMB("npc_henzie_tomb", "Henzie's Tomb",
        TRAIT_GAIN_DESCRIPTION + " ![[Ancient Tomb|UMA|1]]", NPC.HENZIE,
        EffectType.PERMANENT, "Henzie Trait - Henzie's Tomb") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            RogueEffect.addCardToBattlefield("Ancient Tomb|UMA|1", human);
        }
    },
    HENZIE_TORMENTOR("npc_henzie_tormentor", "Tormentor",
        TRAIT_GAIN_DESCRIPTION, NPC.HENZIE,
        EffectType.PERMANENT, "Henzie Trait - Tormentor") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    HENZIE_WELL_CONNECTED("npc_henzie_well_connected", "Well Connected",
        TRAIT_GAIN_DESCRIPTION, NPC.HENZIE,
        EffectType.PERMANENT, "Henzie Trait - Well Connected") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },

    // Narset effects
    NARSET_LANDS_OF_LEGENDS("npc_narset_lands_of_legends", "Lands Of Legends",
        "Choose up to 3 out of 20 legendary lands to add to your deck.", NPC.NARSET,
        EffectType.ONESHOT, null) {
        @Override
        public Predicate<PaperCard> getDBCardsFilter() {
            return PaperCardPredicates.fromRules(rules -> rules.getType().isLand()
                && rules.getType().isLegendary()
                && rules.getSplitType() == CardSplitType.None);
        }

        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            List<PaperCard> candidateCards = run.getAllCardsForActiveCommander(getDBCardsFilter()).stream()
                .map(RogueConfig::getRulesNamePrint)
                .filter(Objects::nonNull)
                .toList();
            candidateCards = run.filterDuplicateCards(candidateCards);
            if (candidateCards.isEmpty()) {
                return;
            }

            List<CardReference> landsOfLegendsPrintOverrides = List.of(
                new CardReference("Gemstone Caverns", "PLST", 1),
                new CardReference("Inventors' Fair", "KLD", 1),
                new CardReference("Urborg, Tomb of Yawgmoth", "UMA", 1),
                new CardReference("Miren, the Moaning Well", "SOK", 1),
                new CardReference("Eye of Ugin", "MM2", 1),
                new CardReference("Boseiju, Who Shelters All", "CHK", 1),
                new CardReference("Urborg, Tomb of Yawgmoth", "UMA", 1)
            );
            candidateCards = applyCardPrintOverrides(candidateCards, landsOfLegendsPrintOverrides);
            Collections.shuffle(candidateCards, MyRandom.getRandom());
            candidateCards = new ArrayList<>(candidateCards.subList(0, Math.min(20, candidateCards.size())));
            selectCardsForDeck(ctx, candidateCards, 0, 3);
        }
    },
    NARSET_SINS("npc_narset_sins", "Narset's Sins",
        "Gain the {{Item}}s [[Corrupted Powerstone]] and [[Chaos Capsule]]. ![[Narset Item - Corrupted Powerstone]] ![[Narset Item - Chaos Capsule]]", NPC.NARSET,
        EffectType.ONESHOT, null) {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addCarryCards(run, ctx, List.of("Narset Item - Corrupted Powerstone", "Narset Item - Chaos Capsule"),
                CarryCardType.ITEM);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return canAddAllCarryCards(run,
                List.of("Narset Item - Corrupted Powerstone", "Narset Item - Chaos Capsule"));
        }
    },
    NARSET_UTENSILS("npc_narset_utensils", "Narset's Utensils",
        "Gain the {{Item}}s [[Fractured Powerstone|MOC|1]] and [[Ichor Elixir|MOC|1]].", NPC.NARSET,
        EffectType.ONESHOT, null) {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addCarryCards(run, ctx, List.of("Fractured Powerstone|MOC|1", "Ichor Elixir|MOC|1"),
                CarryCardType.ITEM);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return canAddAllCarryCards(run, List.of("Fractured Powerstone|MOC|1", "Ichor Elixir|MOC|1"));
        }
    },
    NARSET_PASSAGE("npc_narset_passage", "Narset's Passage",
        TRAIT_GAIN_DESCRIPTION + " ![[Rogue's Passage|FDN|1]]", NPC.NARSET,
        EffectType.PERMANENT, "Narset Trait - Narset's Passage") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            RogueEffect.addCardToBattlefield("Rogue's Passage|CMM|1", human);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.canAddCardToDeck(RogueConfig.getCard("Rogue's Passage", null, null));
        }
    },
    NARSET_TOWER("npc_narset_tower", "Narset's Tower",
        TRAIT_GAIN_DESCRIPTION + " ![[Reliquary Tower|TDC|1]]", NPC.NARSET,
        EffectType.PERMANENT, "Narset Trait - Narset's Tower") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            RogueEffect.addCardToBattlefield("Reliquary Tower|CMM|1", human);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.canAddCardToDeck(RogueConfig.getCard("Reliquary Tower", null, null));
        }
    },
    NARSET_VAULT("npc_narset_vault", "Narset's Vault",
        TRAIT_GAIN_DESCRIPTION + " ![[Vault of the Archangel|TDC|1]]", NPC.NARSET,
        EffectType.PERMANENT, "Narset Trait - Narset's Vault") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            RogueEffect.addCardToBattlefield("Vault of the Archangel|TDC|1", human);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.canAddCardToDeck(RogueConfig.getCard("Vault of the Archangel", null, null));
        }
    },
    NARSET_WOLF_RUN("npc_narset_wolf_run", "Narset's Wolf Run",
        TRAIT_GAIN_DESCRIPTION + " ![[Kessig Wolf Run|TDC|1]]", NPC.NARSET,
        EffectType.PERMANENT, "Narset Trait - Narset's Wolf Run") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            RogueEffect.addCardToBattlefield("Kessig Wolf Run|TDC|1", human);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.canAddCardToDeck(RogueConfig.getCard("Kessig Wolf Run", null, null));
        }
    },
    NARSET_CHAOSWALKER("npc_chaoswalker", "Chaoswalker",
        TRAIT_GAIN_DESCRIPTION, NPC.NARSET,
        EffectType.PERMANENT, "Narset Trait - Chaoswalker") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    NARSET_DARKWALKER("npc_narset_darkwalker", "Darkwalker",
        TRAIT_GAIN_DESCRIPTION, NPC.NARSET,
        EffectType.PERMANENT, "Narset Trait - Darkwalker") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    NARSET_LIGHTWALKER("npc_narset_lightwalker", "Lightwalker",
        TRAIT_GAIN_DESCRIPTION, NPC.NARSET,
        EffectType.PERMANENT, "Narset Trait - Lightwalker") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    NARSET_MINDWALKER("npc_narset_mindwalker", "Mindwalker",
        TRAIT_GAIN_DESCRIPTION, NPC.NARSET,
        EffectType.PERMANENT, "Narset Trait - Mindwalker") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    NARSET_MATTERWALKER("npc_narset_matterwalker", "Matterwalker",
        TRAIT_GAIN_DESCRIPTION, NPC.NARSET,
        EffectType.PERMANENT, "Narset Trait - Matterwalker") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    NARSET_CHAOSS_PROTECTION("npc_narset_chaoss_protection", "Chaos's Protection",
        TRAIT_GAIN_DESCRIPTION, NPC.NARSET,
        EffectType.PERMANENT, "Narset Trait - Chaos's Protection") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    NARSET_CHAOTIC_TUTOR("npc_narset_chaotic_tutor", "Chaotic Tutor",
        TRAIT_GAIN_DESCRIPTION, NPC.NARSET,
        EffectType.PERMANENT, "Narset Trait - Chaotic Tutor") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    NARSET_WRATH_OF_CHAOS("npc_narset_wrath_of_chaos", "Wrath of Chaos",
        TRAIT_GAIN_DESCRIPTION, NPC.NARSET,
        EffectType.PERMANENT, "Narset Trait - Wrath of Chaos") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    NARSET_NEGATION_OF_CHAOS("npc_narset_negation_of_chaos", "Negation of Chaos",
        TRAIT_GAIN_DESCRIPTION, NPC.NARSET,
        EffectType.PERMANENT, "Narset Trait - Negation of Chaos") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    NARSET_TITHE_OF_CHAOS("npc_narset_tithe_of_chaos", "Tithe of Chaos",
        TRAIT_GAIN_DESCRIPTION, NPC.NARSET,
        EffectType.PERMANENT, "Narset Trait - Tithe of Chaos") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },

    // Tyvar effects
    TYVAR_COMPANIONS("npc_tyvar_companions", "Tyvar's Companions",
        "Gain the {{Fellow}}s [[Loyal Apprentice|CMM|1]] and [[Loyal Unicorn|CMM|1]].", NPC.TYVAR,
        EffectType.ONESHOT, null) {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addCarryCards(run, ctx, List.of("Loyal Apprentice|CMM|1", "Loyal Unicorn|CMM|1"), CarryCardType.FELLOW);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return canAddAllCarryCards(run, List.of("Loyal Apprentice|CMM|1", "Loyal Unicorn|CMM|1"));
        }
    },
    TYVAR_FOLLOWERS("npc_tyvar_followers", "Tyvar's Followers",
        "Gain the {{Fellow}}s [[Loyal Guardian|CMM|1]] and [[Loyal Subordinate|CMM|1]].", NPC.TYVAR,
        EffectType.ONESHOT, null) {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addCarryCards(run, ctx, List.of("Loyal Guardian|CMM|1", "Loyal Subordinate|CMM|1"), CarryCardType.FELLOW);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return canAddAllCarryCards(run, List.of("Loyal Guardian|CMM|1", "Loyal Subordinate|CMM|1"));
        }
    },
    TYVAR_PETS("npc_tyvar_pets", "Tyvar's Pets",
        "Gain the {{Fellow}}s [[Loyal Drake|CMM|1]] and [[Loyal Unicorn|CMM|1]].", NPC.TYVAR,
        EffectType.ONESHOT, null) {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addCarryCards(run, ctx, List.of("Loyal Drake|CMM|1", "Loyal Unicorn|CMM|1"), CarryCardType.FELLOW);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return canAddAllCarryCards(run, List.of("Loyal Drake|CMM|1", "Loyal Unicorn|CMM|1"));
        }
    },
    TYVAR_ARMORY("npc_tyvar_armory", "Tyvar's Armory",
        "Gain the {{Item}}s [[Commander's Sphere|CMM|1]] and [[Commander's Plate|CMR|1]].", NPC.TYVAR,
        EffectType.ONESHOT, null) {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addCarryCards(run, ctx, List.of("Commander's Sphere|CMM|1", "Commander's Plate|CMR|1"), CarryCardType.ITEM);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return canAddAllCarryCards(run, List.of("Commander's Sphere|CMM|1", "Commander's Plate|CMR|1"));
        }
    },
    TYVAR_RETREAT("npc_tyvar_retreat", "Tyvar's Retreat",
        "Gain the {{Item}}s [[Campfire|CMM|1]] and [[Tome of Legends|MKC|1]].", NPC.TYVAR,
        EffectType.ONESHOT, null) {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addCarryCards(run, ctx, List.of("Campfire|CMM|1", "Tome of Legends|MKC|1"), CarryCardType.ITEM);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return canAddAllCarryCards(run, List.of("Campfire|CMM|1", "Tome of Legends|MKC|1"));
        }
    },
    TYVAR_STAPLES("npc_tyvar_staples", "Tyvar's Staples",
        "Add a [[Sol Ring|CMM|2]] and [[Arcane Signet|CMM|2]] to your deck.", NPC.TYVAR,
        EffectType.ONESHOT, null) {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addCardsToDeck(run, ctx, List.of("Sol Ring|CMM|2", "Arcane Signet|CMM|2"));
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            PaperCard solRing = RogueConfig.getCard("Sol Ring", null, null);
            PaperCard arcaneSignet = RogueConfig.getCard("Arcane Signet", null, null);
            return run.canAddCardToDeck(solRing) || run.canAddCardToDeck(arcaneSignet);
        }
    },
    TYVAR_AUGMENTED("npc_tyvar_augmented_commander", "Augmented Commander",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Augmented Commander") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_BASTION("npc_tyvar_bastion_commander", "Bastion Commander",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Bastion Commander") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_BLOODSWORN("npc_tyvar_bloodsworn_commander", "Bloodsworn Commander",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Bloodsworn Commander") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_BRAWLER("npc_tyvar_brawler_commander", "Brawler Commander",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Brawler Commander") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_CHARLATAN("npc_tyvar_charlatan_commander", "Charlatan Commander",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Charlatan Commander") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_CHEF("npc_tyvar_chef_commander", "Master Chef Commander",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Master Chef Commander") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_CRIMINAL("npc_tyvar_criminal_commander", "Criminal Commander",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Criminal Commander") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_CULTIST("npc_tyvar_cultist_commander", "Cultist Commander",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Cultist Commander") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_FLAME("npc_tyvar_flame_commander", "Flame Commander",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Flame Commander") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_SAGE("npc_tyvar_sage_commander", "Sage Commander",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Sage Commander") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_BEACON("npc_tyvar_beacon", "Tyvar's Beacon",
        TRAIT_GAIN_DESCRIPTION + " ![[Command Beacon]]", NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Tyvar's Beacon") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            RogueEffect.addCardToBattlefield("Command Beacon", human);
        }
    },
    TYVAR_PALACE("npc_tyvar_palace", "Tyvar's Palace",
        TRAIT_GAIN_DESCRIPTION + " ![[Opal Palace]]", NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Tyvar's Palace") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            RogueEffect.addCardToBattlefield("Opal Palace", human);
        }
    },
    TYVAR_SANCTUM("npc_tyvar_sanctum", "Tyvar's Sanctum",
        TRAIT_GAIN_DESCRIPTION + " ![[Sanctum of Eternity]]", NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Tyvar's Sanctum") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            RogueEffect.addCardToBattlefield("Sanctum of Eternity", human);
        }
    },
    TYVAR_WAR_ROOM("npc_tyvar_war_room", "Tyvar's War Room",
        TRAIT_GAIN_DESCRIPTION + " ![[War Room]]", NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Tyvar's War Room") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            RogueEffect.addCardToBattlefield("War Room", human);
        }
    };

    private final String id;
    private final String displayName;
    private final String description;
    private final NPC ownerNpc;
    private final EffectType effectType;
    private final String effectCardReference;

    NPCEffect(String id, String displayName, String description, NPC ownerNpc, EffectType effectType,
              String effectCardReference) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.ownerNpc = ownerNpc;
        this.effectType = effectType;
        this.effectCardReference = effectCardReference;
    }

    @Override
    public EffectType getEffectType() { return effectType; }

    @Override
    public String getId() { return id; }

    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getRawDescription() { return formatEffectCardDescription(description); }

    @Override
    public String getEffectCardReference() { return effectCardReference; }

    public NPC getOwnerNpc() { return ownerNpc; }

    public boolean isChoiceAvailable(RogueRun run) { return true; }

    public static List<NPCEffect> getEffectsForNpc(NPC npc, RogueRun run) {
        List<NPCEffect> effects = new ArrayList<>();
        for (NPCEffect effect : values()) {
            if (effect.ownerNpc == npc && (run == null || effect.isChoiceAvailable(run))) {
                effects.add(effect);
            }
        }
        return effects;
    }

    public static NPCEffect fromId(String id) {
        for (NPCEffect b : values())
            if (b.id.equals(id)) return b;
        return null;
    }
}
