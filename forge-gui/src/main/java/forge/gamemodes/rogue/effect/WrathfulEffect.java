package forge.gamemodes.rogue.effect;

import forge.game.player.RegisteredPlayer;
import forge.gamemodes.rogue.RogueRun;
import java.util.Set;

public enum WrathfulEffect implements PlaneboundEffect {

    AGGRESSION("wrathful_aggression", "Aggression",
        "Whenever one or more creatures Planebound controls deal combat damage to you, Planebound draws a card.",
        "Wrathful - Aggression") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            run.consumeEffect(getId());
        }
    },
    EFFICIENCY("wrathful_efficiency", "Efficiency",
        "Planebound Commander costs {1} less to cast.",
        "Wrathful - Haste") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            run.consumeEffect(getId());
        }
    },
    FORESIGHT("wrathful_foresight", "Foresight",
        "At the beginning of each of Planebound's upkeeps, they scry 1.",
        "Wrathful - Foresight") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            run.consumeEffect(getId());
        }
    },
    MIGHT("wrathful_might", "Might",
            "Planebound Commander gets +2/+2.",
        "Wrathful - Might") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            run.consumeEffect(getId());
        }
    },
    RESILIENCE("wrathful_resilience", "Resilience",
            "Planebound gains 1 life at the beginning of each of their upkeeps.",
        "Wrathful - Resilience") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            run.consumeEffect(getId());
        }
    };

    private final String id;
    private final String displayName;
    private final String description;
    private final String effectCardReference;

    WrathfulEffect(String id, String displayName, String description, String effectCardReference) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.effectCardReference = effectCardReference;
    }

    @Override
    public PlaneboundEffectCategory getCategory() { return PlaneboundEffectCategory.WRATHFUL; }

    @Override
    public String getId() { return id; }

    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getRawDescription() { return description; }

    @Override
    public String getEffectCardReference() { return effectCardReference; }

    public static WrathfulEffect getRandomExcluding(Set<WrathfulEffect> exclude) {
        return PlaneboundEffect.getRandomExcluding(values(), exclude);
    }

    public static WrathfulEffect fromId(String id) {
        for (WrathfulEffect w : values())
            if (w.id.equals(id)) return w;
        return null;
    }
}
