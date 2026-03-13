package forge.gamemodes.rogue.effect;

import forge.game.player.RegisteredPlayer;
import forge.gamemodes.rogue.RogueMetaProgress;
import forge.gamemodes.rogue.RogueRun;

public enum ChestLoot implements RogueEffect {

    FIND_GOLD("find_gold", "Treasure", "You found 5 gold.", EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            run.setCurrentGold(run.getCurrentGold() + 5);
        }
    },
    FIND_ECHOES("find_echoes", "Giant Soul", "You found 5 echoes.", EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            RogueMetaProgress.getInstance().addEchoes(5);
        }
    },
    CARD_REWARD("card_reward", "Card Cache", "Gain a Card Reward.", EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            ctx.trigger = NodeResultContext.ActionTriggerType.CARD_REWARD;
        }
    },
    MYTHIC_CARD_REWARD("mythic_card_reward", "Mythic Card Cache", "Gain a Mythic Card Reward.", EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            ctx.trigger = NodeResultContext.ActionTriggerType.MYTHIC_CARD_REWARD;
        }
    },

    // PERMANENT loots
    COMMANDER_STRENGTH("commander_strength", "Relic Of Strength", "Your commander gets +2/+2 for the rest of the Run.",
            EffectType.PERMANENT) {
        @Override
        public void onMatchStart(RegisteredPlayer human, RogueRun run) {
            RogueEffect.addCustomCardToCommandZone("Chest - Commander Strength", human);
        }
    },
    COST_REDUCTION("cost_reduction", "Relic Of Agility", "Permanent spells you cast cost {1} less for the rest of the Run.",
            EffectType.PERMANENT) {
        @Override
        public void onMatchStart(RegisteredPlayer human, RogueRun run) {
            RogueEffect.addCustomCardToCommandZone("Chest - Planar Discount", human);
        }
    },
    EXTRA_DRAW("extra_draw", "Relic Of Wisdom", "Draw 1 extra card at the start of each match for the rest of the Run.",
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
