package forge.gamemodes.rogue.effect;

import forge.game.player.RegisteredPlayer;
import forge.gamemodes.rogue.RogueRun;
import java.util.Set;

public enum WrathfulEffect implements PlaneboundEffect {

    COMMANDER_BOOST("wrathful_commander_boost", "Might",
            "Planebound Commander gets +2/+2.",
            "Wrathful - Might") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToCommandZone(cardName, human);
            run.consumeEffect(getId());
        }
    },
    UPKEEP_HEAL("wrathful_upkeep_heal", "Resilience",
            "Planebound gains 1 life at the beginning of each of their upkeeps.",
            "Wrathful - Resilience") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToCommandZone(cardName, human);
            run.consumeEffect(getId());
        }
    },
    COMMANDER_DISCOUNT("wrathful_commander_discount", "Haste",
            "Planebound Commander costs {1} less to cast.",
            "Wrathful - Haste") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToCommandZone(cardName, human);
            run.consumeEffect(getId());
        }
    };

    private final String id;
    private final String displayName;
    private final String description;
    protected final String cardName;

    WrathfulEffect(String id, String displayName, String description, String cardName) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.cardName = cardName;
    }

    @Override
    public PlaneboundEffectCategory getCategory() { return PlaneboundEffectCategory.WRATHFUL; }

    @Override
    public String getId() { return id; }

    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getRawDescription() { return description; }

    public static WrathfulEffect getRandomExcluding(Set<WrathfulEffect> exclude) {
        return PlaneboundEffect.getRandomExcluding(values(), exclude);
    }

    public static WrathfulEffect fromId(String id) {
        for (WrathfulEffect w : values())
            if (w.id.equals(id)) return w;
        return null;
    }
}
