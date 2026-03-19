package forge.gamemodes.rogue.effect;

import forge.game.player.RegisteredPlayer;
import forge.gamemodes.rogue.RogueRun;
import forge.util.MyRandom;

public enum Wrathful implements RogueEffect {

    COMMANDER_BOOST("wrathful_commander_boost", "Might",
            "Planebound Commander gets +2/+2.",
            "Wrathful - Might") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RogueRun run) {
            RogueEffect.addCustomCardToCommandZone(cardName, human);
            run.consumeEffect(getId());
        }
    },
    UPKEEP_HEAL("wrathful_upkeep_heal", "Resilience",
            "Planebound gains 1 life at the beginning of each of their upkeeps.",
            "Wrathful - Resilience") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RogueRun run) {
            RogueEffect.addCustomCardToCommandZone(cardName, human);
            run.consumeEffect(getId());
        }
    },
    COMMANDER_DISCOUNT("wrathful_commander_discount", "Haste",
            "Planebound Commander costs {1} less to cast.",
            "Wrathful - Haste") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RogueRun run) {
            RogueEffect.addCustomCardToCommandZone(cardName, human);
            run.consumeEffect(getId());
        }
    };

    private final String id;
    private final String displayName;
    private final String description;
    protected final String cardName;

    Wrathful(String id, String displayName, String description, String cardName) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.cardName = cardName;
    }

    @Override
    public EffectType getEffectType() { return EffectType.CONSUME; }

    @Override
    public int getChargesForRank(int rank) { return 1; }

    public String getId() { return id; }

    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getDescription() { return description; }

    public static Wrathful getRandom() {
        Wrathful[] vals = values();
        return vals[MyRandom.getRandom().nextInt(vals.length)];
    }

    public static Wrathful getRandomExcluding(java.util.Set<Wrathful> exclude) {
        java.util.List<Wrathful> candidates = new java.util.ArrayList<>();
        for (Wrathful w : values()) {
            if (!exclude.contains(w)) candidates.add(w);
        }
        if (candidates.isEmpty()) return getRandom();
        return candidates.get(MyRandom.getRandom().nextInt(candidates.size()));
    }

    public static Wrathful fromId(String id) {
        for (Wrathful w : values())
            if (w.id.equals(id)) return w;
        return null;
    }
}
