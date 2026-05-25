package forge.gamemodes.rogue.effect;

import forge.game.player.RegisteredPlayer;
import forge.gamemodes.rogue.RogueMetaProgress;
import forge.gamemodes.rogue.RogueRun;

public enum ChestEffect implements RogueEffect {

    FIND_GOLD("find_gold", "Treasure", "You found 10 gold.", EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            run.addGold(10);
        }
    },
    FIND_ECHOES("find_echoes", "Giant Soul", "You found 10 echoes.", EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, NodeResultContext ctx) {
            RogueMetaProgress.getInstance().addEchoes(10);
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
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToCommandZone("Chest Boon - Relic Of Strength", human);
        }
    },
    COST_REDUCTION("cost_reduction", "Relic Of Agility", "Permanent spells you cast cost {1} less for the rest of the Run.",
            EffectType.PERMANENT) {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToCommandZone("Chest Boon - Relic Of Agility", human);
        }
    },
    EXTRA_DRAW("extra_draw", "Relic Of Wisdom", "Draw 1 extra card at the start of each match for the rest of the Run.",
            EffectType.PERMANENT) {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            human.setStartingHand(human.getStartingHand() + 1);
        }
    };

    private final String id;
    private final String displayName;
    private final String description;
    private final EffectType effectType;

    ChestEffect(String id, String displayName, String description, EffectType effectType) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.effectType = effectType;
    }

    public void applyEffect(RogueRun run, NodeResultContext ctx) { /* Override in ONESHOT constants to apply immediate chest effects. */ }

    @Override
    public EffectType getEffectType() { return effectType; }

    @Override
    public String getId() { return id; }

    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getRawDescription() { return description; }

    public static ChestEffect fromId(String id) {
        for (ChestEffect cl : values())
            if (cl.id.equals(id)) return cl;
        return null;
    }
}
