package forge.gamemodes.rogue.effect;

import forge.game.player.RegisteredPlayer;
import forge.gamemodes.rogue.RogueRun;

public enum WoundEffect implements RogueEffect {

    LEG("leg", "Leg",
            "Every spell you cast costs {1} more.",
            EffectType.PERMANENT, "Wound - Leg") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human, run);
        }
    },
    HEAD("head", "Head", "Start each match with 1 less card in hand.",
            EffectType.PERMANENT) {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            human.setStartingHand(human.getStartingHand() - 1);
        }
    },
    EYE("eye", "Eye", "Planes on the path are not revealed when you reach them.",
            EffectType.PERMANENT) {
        @Override
        public void onPathUpdate(PathUpdateContext ctx, RogueRun run) {
            ctx.hidePlanes = true;
        }
    };

    private final String id;
    private final String displayName;
    private final String description;
    private final EffectType effectType;
    private final String effectCardName;

    WoundEffect(String id, String displayName, String description, EffectType effectType) {
        this(id, displayName, description, effectType, null);
    }

    WoundEffect(String id, String displayName, String description, EffectType effectType,
                String effectCardName) {
        this.id = id;
        this.displayName = displayName;
        this.description = RogueEffect.appendPreviewReference(description, effectCardName);
        this.effectType = effectType;
        this.effectCardName = effectCardName;
    }

    @Override
    public EffectType getEffectType() { return effectType; }

    @Override
    public String getId() { return id; }

    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getRawDescription() { return description; }

    @Override
    public String getEffectCardName(RogueRun run) { return effectCardName; }

    public static WoundEffect fromId(String id) {
        for (WoundEffect w : values())
            if (w.id.equals(id)) return w;
        return null;
    }
}
