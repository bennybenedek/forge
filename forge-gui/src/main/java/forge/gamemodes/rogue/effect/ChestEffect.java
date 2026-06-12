package forge.gamemodes.rogue.effect;

import forge.game.player.RegisteredPlayer;
import forge.gamemodes.rogue.RogueMetaProgress;
import forge.gamemodes.rogue.RogueRun;

public enum ChestEffect implements RogueEffect {

    // ONESHOT effects
    CARD_REWARD("card_reward", "Card Cache", "Gain a {{Card Reward}}.", EffectType.ONESHOT, null) {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            ctx.trigger = EffectResultContext.ActionTriggerType.CARD_REWARD;
        }
    },
    CHARM_OF_VITALITY("charm_of_vitality", "Charm Of Vitality", "Gain 10 {{Max. Life}}.", EffectType.ONESHOT, null) {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            run.addMaxLife(10);
        }
    },
    MYTHIC_CARD_REWARD("mythic_card_reward", "Mythic Card Cache", "Gain a mythic {{Card Reward}}.",
        EffectType.ONESHOT, null) {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            ctx.trigger = EffectResultContext.ActionTriggerType.MYTHIC_CARD_REWARD;
        }
    },
    TREASURE("treasure", "Treasure", "Gain 10 {{Gold}} and 10 {{Echoes}}.", EffectType.ONESHOT, null) {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            run.addGold(10);
            RogueMetaProgress.getInstance().addEchoes(10);
        }
    },

    // PERMANENT / CONSUME effects (Traits)
    RELIC_OF_STRENGTH("relic_of_strength", "Relic Of Strength",
        "Gain the {{Trait}} **Relic Of Strength**.",
        EffectType.PERMANENT, "Chest Trait - Relic Of Strength") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    RELIC_OF_AGILITY("relic_of_agility", "Relic Of Agility",
        "Gain the {{Trait}} **Relic Of Agility**.",
        EffectType.PERMANENT, "Chest Trait - Relic Of Agility") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    RELIC_OF_WEALTH("relic_of_wealth", "Relic Of Wealth",
        "Gain the {{Trait}} **Relic Of Wealth**.",
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
    RELIC_OF_WISDOM("relic_of_wisdom", "Relic Of Wisdom", "Gain the {{Trait}} **Relic Of Wisdom**.",
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
    public String getRawDescription() { return description; }

    @Override
    public String getEffectCardReference() { return effectCardReference; }

    public static ChestEffect fromId(String id) {
        for (ChestEffect cl : values())
            if (cl.id.equals(id)) return cl;
        return null;
    }
}
