package forge.gamemodes.rogue.effect;

import forge.game.player.RegisteredPlayer;
import forge.gamemodes.rogue.RogueRun;

public enum Wound implements RogueEffect {

    LEG("leg", "Wounded Leg", "Every spell you cast costs {1} more.",
            EffectType.PERMANENT) {
        @Override
        public void onMatchStart(RegisteredPlayer human, RogueRun run) {
            RogueEffect.addCustomCardToCommandZone("Rogue - Wounded Leg", human);
        }
    },
    HEAD("head", "Wounded Head", "Start each match with 1 less card in hand.",
            EffectType.PERMANENT) {
        @Override
        public void onMatchStart(RegisteredPlayer human, RogueRun run) {
            human.setStartingHand(human.getStartingHand() - 1);
        }
    },
    EYE("eye", "Wounded Eye", "Planes on the path are not revealed when you reach them.",
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

    Wound(String id, String displayName, String description, EffectType effectType) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.effectType = effectType;
    }

    @Override
    public EffectType getEffectType() { return effectType; }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    public static Wound fromId(String id) {
        for (Wound w : values())
            if (w.id.equals(id)) return w;
        return null;
    }
}
