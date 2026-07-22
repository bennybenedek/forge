package forge.gamemodes.rogue.effect;

import forge.card.CardRulesPredicates;
import forge.card.CardSplitType;
import forge.game.player.RegisteredPlayer;
import forge.gamemodes.rogue.RogueConfig;
import forge.gamemodes.rogue.RogueDeck;
import forge.gamemodes.rogue.RogueRun;
import forge.gamemodes.rogue.RogueRun.CarryCardType;
import forge.gamemodes.rogue.npc.NPC;
import forge.item.IPaperCard;
import forge.item.PaperCard;
import forge.item.PaperCardPredicates;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import forge.util.MyRandom;

/**
 * NPC effects granted during run start encounters.
 * Each constant defines its own behavior explicitly.
 */
public enum NPCEffect implements RogueEffect {

    // Tyvar effects
    TYVAR_APPRENTICE("npc_tyvar_apprentice", "Tyvar's Apprentice",
        FELLOW_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.ONESHOT, "Loyal Apprentice") {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addEffectCardAsCarryCard(run, ctx, CarryCardType.FELLOW);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.canAddCardAsCarryCard(getEffectCard());
        }
    },
    TYVAR_DRAKES("npc_tyvar_drake", "Tyvar's Drake",
        FELLOW_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.ONESHOT, "Loyal Drake") {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addEffectCardAsCarryCard(run, ctx, CarryCardType.FELLOW);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.canAddCardAsCarryCard(getEffectCard());
        }
    },
    TYVAR_GUARDIAN("npc_tyvar_guardian", "Tyvar's  Guardian",
        FELLOW_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.ONESHOT, "Loyal Guardian") {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addEffectCardAsCarryCard(run, ctx, CarryCardType.FELLOW);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.canAddCardAsCarryCard(getEffectCard());
        }
    },
    TYVAR_SUBORDINATE("npc_tyvar_subordinate", "Tyvar's Subordinate",
        FELLOW_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.ONESHOT, "Loyal Subordinate") {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addEffectCardAsCarryCard(run, ctx, CarryCardType.FELLOW);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.canAddCardAsCarryCard(getEffectCard());
        }
    },
    TYVAR_UNICORN("npc_tyvar_lieutenant_of_unicorns", "Tyvar's Unicorn",
        "Gain the {{Fellow}} %s.", NPC.TYVAR,
        EffectType.ONESHOT, "Loyal Unicorn") {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addEffectCardAsCarryCard(run, ctx, CarryCardType.FELLOW);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.canAddCardAsCarryCard(getEffectCard());
        }
    },
    TYVAR_CAMP("npc_tyvar_camp", "Tyvar's Camp",
        ITEM_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.ONESHOT, "Campfire") {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addEffectCardAsCarryCard(run, ctx, CarryCardType.ITEM);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.canAddCardAsCarryCard(getEffectCard());
        }
    },
    TYVAR_SPHERE("npc_tyvar_sphere", "Tyvar's Sphere",
        ITEM_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.ONESHOT, "Commander's Sphere") {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addEffectCardAsCarryCard(run, ctx, CarryCardType.ITEM);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.canAddCardAsCarryCard(getEffectCard());
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
    TYVAR_TOME("npc_tyvar_tome", "Tyvar's Tome",
        ITEM_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.ONESHOT, "Tome of Legends|MKC|1") {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addEffectCardAsCarryCard(run, ctx, CarryCardType.ITEM);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.canAddCardAsCarryCard(getEffectCard());
        }
    },
    TYVAR_AUGMENTED("npc_tyvar_augmented", "Augmented Commander",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Augmented") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_BASTION("npc_tyvar_bastion", "Bastion Commander",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Bastion") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_BLOODSWORN("npc_tyvar_bloodsworn", "Bloodsworn Commander",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Bloodsworn") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_BRAWLER("npc_tyvar_brawler", "Brawler Commander",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Brawler") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_CHARLATAN("npc_tyvar_charlatan", "Charlatan Commander",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Charlatan") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_CHEF("npc_tyvar_chef", "Chef Commander",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Chef") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_CRIMINAL("npc_tyvar_criminal", "Criminal Commander",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Criminal") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_CULTIST("npc_tyvar_cultist", "Cultist Commander",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Cultist") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_FLAME("npc_tyvar_flame", "Flame Commander",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Flame") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_SAGE("npc_tyvar_sage", "Sage Commander",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Sage") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_BEACON("npc_tyvar_beacon", "Tyvar's Beacon",
        TRAIT_GAIN_DESCRIPTION + " ![[Command Beacon]]", NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Beacon") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            RogueEffect.addCardToBattlefield("Command Beacon", human);
        }
    },
    TYVAR_WAR_ROOM("npc_tyvar_war_room", "Tyvar's War Room",
        TRAIT_GAIN_DESCRIPTION + " ![[War Room]]", NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - War Room") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            RogueEffect.addCardToBattlefield("War Room", human);
        }
    },
    TYVAR_PALACE("npc_tyvar_palace", "Tyvar's Palace",
        TRAIT_GAIN_DESCRIPTION + " ![[Opal Palace]]", NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Palace") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            RogueEffect.addCardToBattlefield("Opal Palace", human);
        }
    },

    // Narset effects
    NARSET_LANDS_OF_LEGENDS("npc_narset_lands_of_legends", "Lands Of Legends",
        "Choose up to 3 out of 20 legendary lands to add to your deck.", NPC.NARSET,
        EffectType.ONESHOT, null) {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            List<PaperCard> candidateCards = getLegendaryLandCandidates(run);
            if (candidateCards.isEmpty()) {
                return;
            }

            Collections.shuffle(candidateCards, MyRandom.getRandom());
            candidateCards = new ArrayList<>(candidateCards.subList(0, Math.min(20, candidateCards.size())));
            selectCardsForDeck(ctx, candidateCards, 0, 3);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return !getLegendaryLandCandidates(run).isEmpty();
        }
    },
    NARSET_CAPSULE("npc_narset_capsule", "Narset's Capsule",
        ITEM_GAIN_DESCRIPTION, NPC.NARSET,
        EffectType.ONESHOT, "Narset Item - Chaos Capsule") {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addEffectCardAsCarryCard(run, ctx, CarryCardType.ITEM);
        }
    },
    NARSET_ELIXIR("npc_narset_elixir", "Narset's Elixir",
        ITEM_GAIN_DESCRIPTION, NPC.NARSET,
        EffectType.ONESHOT, "Ichor Elixir") {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addEffectCardAsCarryCard(run, ctx, CarryCardType.ITEM);
        }
    },
    NARSET_POWERSTONE("npc_narset_powerstone", "Narset's Powerstone",
        ITEM_GAIN_DESCRIPTION, NPC.NARSET,
        EffectType.ONESHOT, "Fractured Powerstone") {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addEffectCardAsCarryCard(run, ctx, CarryCardType.ITEM);
        }
    },
    NARSET_PASSAGE("npc_narset_passage", "Narset's Passage",
        TRAIT_GAIN_DESCRIPTION + " ![[Rogue's Passage]]", NPC.NARSET,
        EffectType.PERMANENT, "Narset Trait - Passage") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            RogueEffect.addCardToBattlefield("Rogue's Passage", human);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.canAddCardToDeck(RogueConfig.getCard("Rogue's Passage", null, null));
        }
    },
    NARSET_TOWER("npc_narset_tower", "Narset's Tower",
        TRAIT_GAIN_DESCRIPTION + " ![[Reliquary Tower]]", NPC.NARSET,
        EffectType.PERMANENT, "Narset Trait - Tower") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            RogueEffect.addCardToBattlefield("Reliquary Tower", human);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.canAddCardToDeck(RogueConfig.getCard("Reliquary Tower", null, null));
        }
    },
    NARSET_VAULT("npc_narset_vault", "Narset's Vault",
        TRAIT_GAIN_DESCRIPTION + " ![[Vault of the Archangel]]", NPC.NARSET,
        EffectType.PERMANENT, "Narset Trait - Vault") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            RogueEffect.addCardToBattlefield("Vault of the Archangel", human);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.canAddCardToDeck(RogueConfig.getCard("Vault of the Archangel", null, null));
        }
    },
    NARSET_WOLF_RUN("npc_narset_wolf_run", "Narset's Wolf Run",
        TRAIT_GAIN_DESCRIPTION + " ![[Kessig Wolf Run]]", NPC.NARSET,
        EffectType.PERMANENT, "Narset Trait - Wolf Run") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            RogueEffect.addCardToBattlefield("Kessig Wolf Run", human);
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
    NARSET_GOD_OF_CHAOS("npc_narset_god_of_chaos", "God of Chaos",
        TRAIT_GAIN_DESCRIPTION, NPC.NARSET,
        EffectType.PERMANENT, "Narset Trait - God of Chaos") {
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

    // Henzie effects
    HENZIE_CRYPT("npc_henzie_crypt", "Henzie's Crypt",
        ITEM_GAIN_DESCRIPTION, NPC.HENZIE,
        EffectType.ONESHOT, "Mana Crypt|2XM|1") {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addEffectCardAsCarryCard(run, ctx, CarryCardType.ITEM);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.canAddCardAsCarryCard(getEffectCard());
        }
    },
    HENZIE_DIAMOND("npc_henzie_diamond", "Henzie's Diamond",
        ITEM_GAIN_DESCRIPTION, NPC.HENZIE,
        EffectType.ONESHOT, "Lion's Eye Diamond|VMA|1") {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addEffectCardAsCarryCard(run, ctx, CarryCardType.ITEM);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.canAddCardAsCarryCard(getEffectCard());
        }
    },
    HENZIE_VAULT("npc_henzie_vault", "Henzie's Vault",
        ITEM_GAIN_DESCRIPTION, NPC.HENZIE,
        EffectType.ONESHOT, "Mana Vault|2X2|1") {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addEffectCardAsCarryCard(run, ctx, CarryCardType.ITEM);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.canAddCardAsCarryCard(getEffectCard());
        }
    },
    HENZIE_HAUL("npc_henzie_haul", "Henzie's Haul",
        ITEM_GAIN_DESCRIPTION, NPC.HENZIE,
        EffectType.ONESHOT, "Bandit's Haul") {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addEffectCardAsCarryCard(run, ctx, CarryCardType.ITEM);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.canAddCardAsCarryCard(getEffectCard());
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
    HENZIE_MYTHICS("npc_henzie_mythics", "Henzie's Mythics",
        "Choose 3 cards to remove (excluding basic lands), then receive 3 random mythic rare cards from your {{Reward Pool}}.", NPC.HENZIE,
        EffectType.ONESHOT, null) {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            int exchangeCount = 3;
            RogueDeck rogueDeck = run.getSelectedRogueDeck();
            List<PaperCard> replacementCards = rogueDeck == null
                ? List.of()
                : rogueDeck.drawRewardOptions(exchangeCount, PaperCardPredicates.IS_MYTHIC_RARE);
            selectCardsFromDeck(run, ctx, PaperCardPredicates.fromRules(CardRulesPredicates.NOT_BASIC_LAND),
                exchangeCount, exchangeCount, replacementCards, null);
        }
    },
    HENZIE_EXQUISITE_TRAITS("npc_henzie_exquisite_traits", "Exquisite Traits",
        "Gain 2 random Chest {{Traits}}.", NPC.HENZIE,
        EffectType.ONESHOT, null) {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            List<ChestEffect> chestTraits = getAvailableChestTraits(run);
            Collections.shuffle(chestTraits, MyRandom.getRandom());
            for (int i = 0; i < Math.min(2, chestTraits.size()); i++) {
                run.addChestEffect(chestTraits.get(i));
            }
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return !getAvailableChestTraits(run).isEmpty();
        }
    },
    HENZIE_CITY("npc_henzie_city", "Henzie's City",
        TRAIT_GAIN_DESCRIPTION + " ![[City of Brass|2X2|1]]", NPC.HENZIE,
        EffectType.PERMANENT, "Henzie Trait - City") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            RogueEffect.addCardToBattlefield("City of Brass|2X2|1", human);
        }
    },
    HENZIE_FIELD("npc_henzie_field", "Henzie's Field",
        TRAIT_GAIN_DESCRIPTION + " ![[Field of the Dead|M20|1]]", NPC.HENZIE,
        EffectType.PERMANENT, "Henzie Trait - Field") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            RogueEffect.addCardToBattlefield("Field of the Dead|M20|1", human);
        }
    },
    HENZIE_TOMB("npc_henzie_tomb", "Henzie's Tomb",
        TRAIT_GAIN_DESCRIPTION + " ![[Ancient Tomb|UMA|1]]", NPC.HENZIE,
        EffectType.PERMANENT, "Henzie Trait - Tomb") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            RogueEffect.addCardToBattlefield("Ancient Tomb|UMA|1", human);
        }
    },
    HENZIE_ZONE("npc_henzie_zone", "Henzie's Zone",
        TRAIT_GAIN_DESCRIPTION + " ![[Blast Zone]]", NPC.HENZIE,
        EffectType.PERMANENT, "Henzie Trait - Zone") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            RogueEffect.addCardToBattlefield("Blast Zone", human);
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
    HENZIE_DISSIPATION("npc_henzie_dissipation", "Dissipation",
        TRAIT_GAIN_DESCRIPTION, NPC.HENZIE,
        EffectType.PERMANENT, "Henzie Trait - Dissipation") {
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
    HENZIE_LOREMASTER("npc_henzie_loremaster", "Loremaster",
        TRAIT_GAIN_DESCRIPTION, NPC.HENZIE,
        EffectType.PERMANENT, "Henzie Trait - Loremaster") {
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
    HENZIE_MODERATE("npc_henzie_moderate", "Moderate",
        TRAIT_GAIN_DESCRIPTION, NPC.HENZIE,
        EffectType.PERMANENT, "Henzie Trait - Moderate") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
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

    private static List<PaperCard> getLegendaryLandCandidates(RogueRun run) {
        return run.getAllCardsForActiveCommander(card -> card.getRules().getType().isLand()
            && card.getRules().getType().isLegendary()
            && card.getRules().getSplitType() == CardSplitType.None);
    }

    public boolean isChoiceAvailable(RogueRun run) { return true; }

    private static List<ChestEffect> getAvailableChestTraits(RogueRun run) {
        List<String> activeChestEffectIds = run.getActiveChestEffects().stream()
            .map(RogueEffect::getId)
            .toList();

        List<ChestEffect> chestTraits = new ArrayList<>();
        for (ChestEffect chestEffect : ChestEffect.values()) {
            if (chestEffect.getEffectType() == EffectType.PERMANENT
                && !activeChestEffectIds.contains(chestEffect.getId())) {
                chestTraits.add(chestEffect);
            }
        }
        return chestTraits;
    }

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
