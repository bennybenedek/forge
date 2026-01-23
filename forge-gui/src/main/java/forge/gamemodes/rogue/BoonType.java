package forge.gamemodes.rogue;

/**
 * Enum defining all available Boons (permanent upgrades) in Rogue Commander mode.
 * Boons can be unlocked and upgraded using Echoes in the Aether.
 */
public enum BoonType {
    VITAL_INFUSION("vital_infusion", "Vital Infusion",
        "Begin each Run with additional Max Life.",
        new int[]{2, 4, 6},     // Echo costs per rank
        new int[]{2, 4, 6},     // Effect values: +2/+4/+6 life
        3),

    AETHER_MARKET("aether_market", "Aether Market",
        "Gain additional starting Gold at the beginning of each Run.",
        new int[]{3, 6, 9},     // Echo costs per rank
        new int[]{3, 6, 9},     // Effect values: +3/+6/+9 gold
        3),

    LINGERING_AURA("lingering_aura", "Lingering Aura",
        "Heal Life after each Plane match victory.",
        new int[]{3, 5, 7},     // Echo costs per rank
        new int[]{2, 4, 6},     // Effect values: heal 2/4/6
        3),

    FORESIGHT("foresight", "Foresight",
        "Start each match with 1 additional opening hand card.",
        new int[]{8},           // Echo cost (single rank)
        new int[]{1},           // Effect value: +1 card
        1);

    private final String id;
    private final String displayName;
    private final String description;
    private final int[] echoCosts;
    private final int[] effectValues;
    private final int maxRank;

    BoonType(String id, String displayName, String description,
             int[] echoCosts, int[] effectValues, int maxRank) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.echoCosts = echoCosts;
        this.effectValues = effectValues;
        this.maxRank = maxRank;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getMaxRank() {
        return maxRank;
    }

    /**
     * Get the echo cost to upgrade from (rank-1) to (rank).
     * @param rank The target rank (1-indexed)
     * @return The cost in echoes, or 0 if invalid rank
     */
    public int getEchoCostForRank(int rank) {
        if (rank < 1 || rank > maxRank) {
            return 0;
        }
        return echoCosts[rank - 1];
    }

    /**
     * Get the effect value at a specific rank.
     * @param rank The current rank (1-indexed)
     * @return The effect magnitude, or 0 if not unlocked
     */
    public int getEffectValueAtRank(int rank) {
        if (rank < 1 || rank > maxRank) {
            return 0;
        }
        return effectValues[rank - 1];
    }

    /**
     * Get the description with current effect value for a given rank.
     */
    public String getDescriptionAtRank(int rank) {
        if (rank < 1) {
            return description;
        }
        int value = getEffectValueAtRank(rank);
        switch (this) {
            case VITAL_INFUSION:
                return "Begin each Run with +" + value + " Max Life.";
            case AETHER_MARKET:
                return "Gain +" + value + " starting Gold at the beginning of each Run.";
            case LINGERING_AURA:
                return "Heal " + value + " Life after each Plane match victory.";
            case FORESIGHT:
                return "Start each match with +" + value + " opening hand card.";
            default:
                return description;
        }
    }

    /**
     * Find a BoonType by its ID.
     */
    public static BoonType fromId(String id) {
        for (BoonType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return null;
    }
}
