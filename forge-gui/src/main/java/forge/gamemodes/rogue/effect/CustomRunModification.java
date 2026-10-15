package forge.gamemodes.rogue.effect;

import forge.gamemodes.rogue.CodexHelper;
import forge.gamemodes.rogue.RogueCustomDeckGenerator;
import forge.gamemodes.rogue.RogueDeck;
import forge.gamemodes.rogue.RogueRun;
import forge.gamemodes.rogue.path.NodeBazaar;
import forge.gamemodes.rogue.path.NodeChest;
import forge.item.PaperCard;
import forge.util.MyRandom;
import java.util.Collections;
import java.util.List;

/** Optional run modifiers, including generated decks and persistent Rogue effects. */
public enum CustomRunModification implements RogueEffect {
    MANIA("custom:random_rogue_commander", "Mania",
        "A random legendary creature will be chosen as your Commander.", false) {
        @Override
        public RogueDeck createRogueDeck(RogueDeck selectedDeck, PaperCard commander) {
            return RogueCustomDeckGenerator.generate();
        }
    },
    CHOSEN_ONE("custom:choose_rogue_commander", "Chosen One",
        "Choose any legendary creature as your Commander.", false) {
        @Override
        public RogueDeck createRogueDeck(RogueDeck selectedDeck, PaperCard commander) {
            return RogueCustomDeckGenerator.generateForChosenCommander(commander);
        }
    },
    RANDOM_START("custom:random_start", "Random Start",
        "Start with an automatically created Start Deck according to synergy and color identity for the selected Commander instead of its default deck.", true) {
        @Override
        public RogueDeck createRogueDeck(RogueDeck selectedDeck, PaperCard commander) {
            return RogueCustomDeckGenerator.generateStartingDeck(selectedDeck);
        }
    },
    RANDOM_REWARDS("custom:random_rewards", "Random Rewards",
        "Start with an automatically created Reward Pool according to synergy and color identity for the selected Commander instead of its default pool.", false) {
        @Override
        public RogueDeck createRogueDeck(RogueDeck selectedDeck, PaperCard commander) {
            return RogueCustomDeckGenerator.generateRewardPool(selectedDeck);
        }
    },
    PATHFINDER("custom:pathfinder", "Pathfinder",
        "Ignore Paths when choosing the next Node.", true) {
        @Override
        public void onPathUpdate(PathUpdateContext ctx, RogueRun run) {
            ChestEffect.RELIC_OF_PATHFINDING.onPathUpdate(ctx, run);
        }
    },
    RICH("custom:rich", "Rich",
        "Gain 5 additional Gold after each victorious match. All Side Nodes are Bazaars.", true) {
        @Override
        public void onBeforeRewards(MatchRewardContext ctx, RogueRun run) {
            ctx.goldRewardAdjustment += 5;
        }

        @Override
        public void afterPathGeneration(RogueRun run) {
            run.getPath().replaceNodes(node -> node.isSideNode(), NodeBazaar::new);
        }
    },
    USURPER("custom:usurper", "Usurper",
        "Start with 2 random Chest Traits. All Planebounds gain +1 Cursed.", true) {
        @Override
        public void onRunStart(RogueRun run) {
            List<ChestEffect> traits = ChestEffect.getAvailableEffects(run, EffectType.PERMANENT);
            Collections.shuffle(traits, MyRandom.getRandom());
            for (int i = 0; i < Math.min(2, traits.size()); i++) {
                run.addChestEffect(traits.get(i));
                CodexHelper.recordTraitAcquired(traits.get(i));
            }
        }

        @Override
        public void afterPathGeneration(RogueRun run) {
            run.getPath().updatePlanebounds(node -> true,
                node -> node.setCursedCount(node.getCursedCount() + 1));
        }
    },
    BORROWED_HEALTH("custom:borrowed_health", "Borrowed Health",
        "Resting at a Sanctum heals all Life but costs 3 Max. Life.", true) {
        @Override
        public void onBeforeSanctum(SanctumContext ctx, RogueRun run) {
            if (!ctx.restLifeGainDisabled) {
                ctx.restFullHeal = true;
                ctx.restMaxLifeCost = 3;
            }
        }
    },
    VOID("custom:void", "Void",
        "No Planes are added for any battle.", true) {
        @Override
        public void onBeforeMatchSetup(MatchSetupContext ctx, RogueRun run) {
            ctx.includePlanes = false;
        }
    },
    PATH_OF_THE_BANNED("custom:path_of_the_banned", "Path of the Banned",
        "All Side Nodes are Chests offering only cards from the Commander Banlist.", true) {
        @Override
        public void afterPathGeneration(RogueRun run) {
            run.getPath().replaceNodes(node -> node.isSideNode(), NodeChest::new);
        }

        @Override
        public void onBeforeChestLoot(ChoiceRerollContext ctx, RogueRun run) {
            ctx.forcedChestChoices = List.of(ChestEffect.CARD_CACHE_BAN);
            ctx.remainingRerolls = 0;
        }
    };

    private final String id;
    private final String displayName;
    private final String description;
    private final boolean recordsCuratedCommanderRewards;

    CustomRunModification(String id, String displayName, String description,
            boolean recordsCuratedCommanderRewards) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.recordsCuratedCommanderRewards = recordsCuratedCommanderRewards;
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getRawDescription() { return description; }

    public RogueDeck createRogueDeck(RogueDeck selectedDeck, PaperCard commander) {
        return selectedDeck;
    }

    public boolean recordsCuratedCommanderRewards() {
        return recordsCuratedCommanderRewards;
    }

    public boolean isGeneratedCommander() {
        return this == MANIA || this == CHOSEN_ONE;
    }

    public boolean isIncludedWithGeneratedCommander() {
        return this == RANDOM_START || this == RANDOM_REWARDS;
    }

    public boolean conflictsWith(CustomRunModification other) {
        return this != other && ((isGeneratedCommander() && other.isGeneratedCommander())
            || (this == RICH && other == PATH_OF_THE_BANNED)
            || (this == PATH_OF_THE_BANNED && other == RICH));
    }

    public static CustomRunModification fromId(String id) {
        for (CustomRunModification modification : values()) {
            if (modification.id.equals(id)) return modification;
        }
        return null;
    }
}
