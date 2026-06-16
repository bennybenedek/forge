package forge.gamemodes.rogue.effect;

import forge.game.player.RegisteredPlayer;
import forge.gamemodes.rogue.RogueMetaProgress;
import forge.gamemodes.rogue.RogueRun;

public enum ChestEffect implements RogueEffect {



    // ONESHOT effects
    CARD_CACHE("card_cache", "Card Cache", "Gain a {{Card Reward}}.", EffectType.ONESHOT,
        null) {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            ctx.trigger = EffectResultContext.ActionTriggerType.CARD_REWARD;
        }
    },
    CARD_CACHE_MYTHIC("mythic_card_reward", "Mythic Card Cache", "Gain a mythic {{Card Reward}}.",
        EffectType.ONESHOT, null) {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            ctx.trigger = EffectResultContext.ActionTriggerType.MYTHIC_CARD_REWARD;
        }
    },
    POTION_OF_VITALITY("potion_of_vitality", "Potion Of Vitality", "Gain 10 {{Max. Life}}.",
        EffectType.ONESHOT, null) {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            run.addMaxLife(10);
        }
    },
    TREASURE("treasure", "Treasure", "Gain 10 {{Gold}} and 10 {{Echoes}}.", EffectType.ONESHOT,
        null) {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            run.addGold(10);
            RogueMetaProgress.getInstance().addEchoes(10);
        }
    },

    // PERMANENT / CONSUME effects (Traits)
    IDOL_OF_EMBERS("idol_of_embers", "Idol Of Embers",
        TRAIT_GAIN_DESCRIPTION,
        EffectType.PERMANENT, "Chest Trait - Idol Of Embers") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    IDOL_OF_MANY_EYES("idol_of_many_eyes", "Idol Of Many Eyes",
        TRAIT_GAIN_DESCRIPTION,
        EffectType.PERMANENT, "Chest Trait - Idol Of Many Eyes") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    IDOL_OF_DREAD("idol_of_dread", "Idol Of Dread",
        TRAIT_GAIN_DESCRIPTION,
        EffectType.PERMANENT, "Chest Trait - Idol Of Dread") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    IDOL_OF_THE_RINGCALLER("idol_of_the_ringcaller", "Idol Of The Ringcaller",
        TRAIT_GAIN_DESCRIPTION,
        EffectType.PERMANENT, "Chest Trait - Idol Of The Ringcaller") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    IDOL_OF_THE_COLOSSUS("idol_of_the_colossus", "Idol Of The Colossus",
        TRAIT_GAIN_DESCRIPTION,
        EffectType.PERMANENT, "Chest Trait - Idol Of The Colossus") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    IDOL_OF_LUCK("idol_of_luck", "Idol Of Luck",
        TRAIT_GAIN_DESCRIPTION,
        EffectType.PERMANENT, "Chest Trait - Idol Of Luck") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    IDOL_OF_DARKNESS("idol_of_darkness", "Idol Of Darkness",
        TRAIT_GAIN_DESCRIPTION,
        EffectType.PERMANENT, "Chest Trait - Idol Of Darkness") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    LANTERN_OF_THE_DEAD("lantern_of_the_dead", "Lantern Of The Dead",
        TRAIT_GAIN_DESCRIPTION,
        EffectType.PERMANENT, "Chest Trait - Lantern Of The Dead") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    LANTERN_OF_HIGH_SCORE("lantern_of_high_score", "Lantern Of High Score",
        TRAIT_GAIN_DESCRIPTION,
        EffectType.PERMANENT, "Chest Trait - Lantern Of High Score") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    LANTERN_OF_EXCAVATION("lantern_of_excavation", "Lantern Of Excavation",
        TRAIT_GAIN_DESCRIPTION,
        EffectType.PERMANENT, "Chest Trait - Lantern Of Excavation") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    LANTERN_OF_THE_TRAVELER("lantern_of_the_traveler", "Lantern Of The Traveler",
        TRAIT_GAIN_DESCRIPTION,
        EffectType.PERMANENT, "Chest Trait - Lantern Of The Traveler") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    LANTERN_OF_THE_OCELOT("lantern_of_the_ocelot", "Lantern Of The Ocelot",
        TRAIT_GAIN_DESCRIPTION,
        EffectType.PERMANENT, "Chest Trait - Lantern Of The Ocelot") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    LANTERN_OF_ASCENDANCY("lantern_of_ascendancy", "Lantern Of Ascendancy",
        TRAIT_GAIN_DESCRIPTION,
        EffectType.PERMANENT, "Chest Trait - Lantern Of Ascendancy") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    LANTERN_OF_WILDERNESS("lantern_of_wilderness", "Lantern Of Wilderness",
        TRAIT_GAIN_DESCRIPTION,
        EffectType.PERMANENT, "Chest Trait - Lantern Of Wilderness") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    RELIC_OF_AGILITY("relic_of_agility", "Relic Of Agility",
        TRAIT_GAIN_DESCRIPTION,
        EffectType.PERMANENT, "Chest Trait - Relic Of Agility") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    RELIC_OF_STRENGTH("relic_of_strength", "Relic Of Strength",
        TRAIT_GAIN_DESCRIPTION,
        EffectType.PERMANENT, "Chest Trait - Relic Of Strength") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    RELIC_OF_WEALTH("relic_of_wealth", "Relic Of Wealth",
        "Gain the {{Trait}} %s. !{{Gold}}",
        EffectType.PERMANENT, "Chest Trait - Relic Of Wealth") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }

        @Override
        public void onBeforeRewards(MatchRewardContext ctx, RogueRun run) {
            ctx.goldRewardAdjustment += 4;
        }
    },
    RELIC_OF_WISDOM("relic_of_wisdom", "Relic Of Wisdom",
            TRAIT_GAIN_DESCRIPTION,
            EffectType.PERMANENT, "Chest Trait - Relic Of Wisdom") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            human.setStartingHand(human.getStartingHand() + 1);
            addEffectCardToCommandZone(human);
        }
    };

    private final String id;
    private final String displayName;
    private final String description;
    private final EffectType effectType;
    private final String effectCardReference;

    ChestEffect(String id, String displayName, String description, EffectType effectType,
                String effectCardReference) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.effectType = effectType;
        this.effectCardReference = effectCardReference;
    }

    @Override
    public void applyEffect(RogueRun run, EffectResultContext ctx) { /* Override in ONESHOT constants to apply immediate chest effects. */ }

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

    public static ChestEffect fromId(String id) {
        for (ChestEffect cl : values())
            if (cl.id.equals(id)) return cl;
        return null;
    }
}
