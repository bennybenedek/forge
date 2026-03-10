package forge.gamemodes.rogue;

import forge.game.player.RegisteredPlayer;

public enum EventBoon implements RogueEffect {

    HEALERS_TOUCH("healers_touch", "Healer's Touch", "Gain 8 life, lose 5 gold.",
            EffectType.ONESHOT) {
        @Override
        public void consume(RogueRun run, EventChoiceContext ctx) {
            run.setCurrentLife(run.getCurrentLife() + 8);
            run.setCurrentGold(run.getCurrentGold() - 5);
        }
    },
    RIFT_ENERGY("rift_energy", "Rift Energy", "Gain 10 gold.",
            EffectType.ONESHOT) {
        @Override
        public void consume(RogueRun run, EventChoiceContext ctx) {
            run.setCurrentGold(run.getCurrentGold() + 10);
        }
    },
    CARAVAN_ROB("caravan_rob", "Caravan Plunder", "Lose 3 life, gain 8 gold.",
            EffectType.ONESHOT) {
        @Override
        public void consume(RogueRun run, EventChoiceContext ctx) {
            run.setCurrentLife(run.getCurrentLife() - 3);
            run.setCurrentGold(run.getCurrentGold() + 8);
        }
    },
    BROWSE_WARES("browse_wares", "Browse Wares", "Opens a bazaar.",
            EffectType.ONESHOT) {
        @Override
        public void consume(RogueRun run, EventChoiceContext ctx) {
            ctx.trigger = EventChoiceContext.NodeTriggerType.BAZAAR;
        }
    },
    NOTHING("nothing", "Nothing", "No effect.",
            EffectType.ONESHOT),

    // === PERMANENT effects (stored in run, dispatched via RogueEffectComposite) ===

    COMMANDER_BOOST("commander_boost", "Commander's Might", "Your Commander gets +1/+1.",
            EffectType.PERMANENT) {
        @Override
        public void onMatchStart(RegisteredPlayer human, RogueRun run) {
            RogueEffect.addCustomCardToCommandZone("Rogue - Commander Boost", human);
        }
    };

    private final String id;
    private final String displayName;
    private final String description;
    private final EffectType effectType;

    EventBoon(String id, String displayName, String description, EffectType effectType) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.effectType = effectType;
    }

    /** Override in ONESHOT constants to apply immediate effects. */
    public void consume(RogueRun run, EventChoiceContext ctx) {}

    @Override
    public EffectType getEffectType() { return effectType; }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    public static EventBoon fromId(String id) {
        for (EventBoon eb : values())
            if (eb.id.equals(id)) return eb;
        return null;
    }
}
