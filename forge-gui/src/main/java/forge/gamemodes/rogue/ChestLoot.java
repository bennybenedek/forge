package forge.gamemodes.rogue;

import forge.game.player.RegisteredPlayer;

public enum ChestLoot implements RogueEffect {

    FIND_GOLD("find_gold", "Find Gold", "You found 5 gold.", EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            run.setCurrentGold(run.getCurrentGold() + 5);
        }
    },
    FIND_ECHOES("find_echoes", "Find Echoes", "You found 5 echoes.", EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            RogueMetaProgress.getInstance().addEchoes(5);
        }
    },
    CARD_REWARD("card_reward", "Card Cache", "You gained cards from the Reward Pool.", EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            ctx.trigger = NodeResultContext.ActionTriggerType.CARD_REWARD;
        }
    },
    MYTHIC_CARD_REWARD("mythic_card_reward", "Mythic Relic", "You gained mythic cards from the Reward Pool.", EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            ctx.trigger = NodeResultContext.ActionTriggerType.MYTHIC_CARD_REWARD;
        }
    },

    // PERMANENT loots
    COMMANDER_STRENGTH("commander_strength", "Commander Strength", "You gained a Boon: Your commander gets +2/+2 for the rest of the Run.",
            EffectType.PERMANENT) {
        @Override
        public void onMatchStart(RegisteredPlayer human, RogueRun run) {
            RogueEffect.addCustomCardToCommandZone("Rogue - Commander Strength", human);
        }
    },
    COST_REDUCTION("cost_reduction", "Planar Discount", "You gained a Boon: Permanent spells you cast cost {1} less for the rest of the Run.",
            EffectType.PERMANENT) {
        @Override
        public void onMatchStart(RegisteredPlayer human, RogueRun run) {
            RogueEffect.addCustomCardToCommandZone("Rogue - Planar Discount", human);
        }
    },
    EXTRA_DRAW("extra_draw", "Extra Draw", "You gained a Boon: Draw 1 extra card at the start of each match for the rest of the Run.",
            EffectType.PERMANENT) {
        @Override
        public void onMatchStart(RegisteredPlayer human, RogueRun run) {
            human.setStartingHand(human.getStartingHand() + 1);
        }
    };

    private final String id;
    private final String displayName;
    private final String description;
    private final EffectType effectType;

    ChestLoot(String id, String displayName, String description, EffectType effectType) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.effectType = effectType;
    }

    /** Override in ONESHOT constants to apply immediate chest effects. */
    public void applyEffect(RogueRun run, NodeResultContext ctx) { /* TODO document why this method is empty */ }

    @Override
    public EffectType getEffectType() { return effectType; }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    public static ChestLoot fromId(String id) {
        for (ChestLoot cl : values())
            if (cl.id.equals(id)) return cl;
        return null;
    }
}
