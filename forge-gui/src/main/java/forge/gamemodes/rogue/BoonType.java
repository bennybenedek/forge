package forge.gamemodes.rogue;

/**
 * Enum defining all available Boons (permanent upgrades) in Rogue Commander mode.
 * Boons can be unlocked and upgraded using Echoes in the Aether.
 * Some Boons are available from the start, while others require unlocking Aether Upgrades.
 */
public enum BoonType {
    // Base boons (requiredUpgradeLevel=0) — always visible
    VITAL_INFUSION("vital_infusion", "Vital Infusion",
        "Begin each Run with additional Max Life.",
        new int[]{3, 6, 9, 12},     // Echo costs per rank (rank 1-4)
        new int[]{3, 6, 9, 12},     // Effect values: +2/+4/+6/+9 life
        3, 0),

    AETHER_MARKET("aether_market", "Aether Market",
        "Gain additional starting Gold at the beginning of each Run.",
        new int[]{3, 6, 9, 12},    // Echo costs per rank
        new int[]{3, 6, 9, 12},    // Effect values: +3/+6/+9/+12 gold
        3, 0),

    LINGERING_AURA("lingering_aura", "Lingering Aura",
        "Heal Life after each Plane match victory.",
        new int[]{2, 4, 8, 16},     // Echo costs per rank
        new int[]{2, 4, 6, 8},     // Effect values: heal 2/4/6/8
        3, 0),

    SPECTRAL_RECALIBRATION("spectral_recalibration", "Spectral Recalibration",
        "Gain rerolls per Card Reward or Bazaar selection.",
        new int[]{6, 12, 18},      // Echo costs (rank 1-3)
        new int[]{1, 2, 3},        // Effect values: 1/2/3 rerolls per selection
        2, 0),

    MYTHIC_COLLECTOR("mythic_collector", "Mythic Collector",
        "More cards from Card Rewards and Bazaar will be mythic rarity.",
        new int[]{3, 6, 9, 12},    // Echo costs per rank
        new int[]{1, 2, 3, 4},     // Effect values: +1/+2/+3/+4 extra mythics
        3, 0),

    LAST_SPARK("last_spark", "Last Spark",
        "Revive when you would lose the run.",
        new int[]{10, 20},         // Echo costs (rank 1-2)
        new int[]{5, 10},          // Effect values: survive with 5/10 life
        1, 0),

    //  Aether Upgrade 1

    FORESIGHT("foresight", "Foresight",
        "Start each match with 1 additional opening hand card.",
        new int[]{8, 12},          // Echo costs (rank 1-2)
        new int[]{1, 2},           // Effect values: +1/+2 cards
        1, 1),

    EXPANDED_MIND("expanded_mind", "Expanded Mind",
        "Keep additional cards from Card Rewards.",
        new int[]{8, 12},       // Echo costs (rank 1-3)
        new int[]{1, 2},        // Effect values: +1/+2/+3 extra picks
        1, 1),

    SPARK_KINDLE("spark_kindle", "Spark Kindle",
        "Begin each match with basic lands from your deck already on the battlefield.",
        new int[]{5, 10, 20},      // Echo costs (rank 1-3)
        new int[]{1, 2, 3},        // Effect values: 1/2/3 tapped lands
        2, 1),

    FRACTURED_BINDING("fractured_binding", "Fractured Binding",
        "Reduce the Mana Cost for casting your Commander.",
        new int[]{4, 8, 12, 16},   // Echo costs (rank 1-4)
        new int[]{1, 2, 3, 4},     // Effect values: {1}/{2}/{3}/{4} less
        3, 1);

    private final String id;
    private final String displayName;
    private final String description;
    private final int[] echoCosts;
    private final int[] effectValues;
    private final int maxRank;
    private final int requiredUpgradeLevel; // 0 = always accessible; 1 = requires Aether Upgrade 1

    BoonType(String id, String displayName, String description,
             int[] echoCosts, int[] effectValues, int maxRank, int requiredUpgradeLevel) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.echoCosts = echoCosts;
        this.effectValues = effectValues;
        this.maxRank = maxRank;
        this.requiredUpgradeLevel = requiredUpgradeLevel;
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
     * Whether this boon is accessible given the current Aether Upgrade level.
     */
    public boolean isAccessibleAt(int upgradeLevel) {
        return requiredUpgradeLevel <= upgradeLevel;
    }

    /**
     * Get the effective maximum rank, including any bonus from Aether Upgrades.
     * Aether Upgrade 3 adds +1 max rank to all boons.
     */
    public int getEffectiveMaxRank(int upgradeLevel) {
        int bonus = 0;
        for (int l = 1; l <= upgradeLevel; l++) {
            AetherUpgrade u = AetherUpgrade.forLevel(l);
            if (u != null) bonus += u.extraBoonRanks;
        }
        return maxRank + bonus;
    }

    /**
     * Get the echo cost to upgrade from (rank-1) to (rank).
     * Bounds use echoCosts.length to allow the extra rank from Aether Upgrade 3.
     * @param rank The target rank (1-indexed)
     * @return The cost in echoes, or 0 if invalid rank
     */
    public int getEchoCostForRank(int rank) {
        if (rank < 1 || rank > echoCosts.length) {
            return 0;
        }
        return echoCosts[rank - 1];
    }

    /**
     * Get the effect value at a specific rank.
     * Bounds use effectValues.length to allow the extra rank from Aether Upgrade 3.
     * @param rank The current rank (1-indexed)
     * @return The effect magnitude, or 0 if not unlocked
     */
    public int getEffectValueAtRank(int rank) {
        if (rank < 1 || rank > effectValues.length) {
            return 0;
        }
        return effectValues[rank - 1];
    }

    /**
     * Get the description showing all rank values with the current rank highlighted.
     * Uses HTML formatting. Shows values up to the effective max rank for the given upgrade level.
     * @param currentRank The current rank (0 = not unlocked)
     * @param upgradeLevel The current Aether Upgrade level
     * @return HTML-formatted description string
     */
    public String getDescriptionWithAllRanks(int currentRank, int upgradeLevel) {
        String allValues = buildAllValuesString(currentRank, upgradeLevel);

        switch (this) {
            case VITAL_INFUSION:
                return "<html>Begin each Run with +" + allValues + " Max Life.</html>";
            case AETHER_MARKET:
                return "<html>Gain +" + allValues + " starting Gold.</html>";
            case LINGERING_AURA:
                return "<html>Heal " + allValues + " Life after each match victory.</html>";
            case FORESIGHT:
                return "<html>Start each match with +" + allValues + " opening hand card.</html>";
            case MYTHIC_COLLECTOR:
                return "<html>+" + allValues + " more mythic cards in Rewards and Bazaar.</html>";
            case LAST_SPARK:
                return "<html>Once per run: survive defeat with " + allValues + " life.</html>";
            case EXPANDED_MIND:
                return "<html>Keep +" + allValues + " extra cards from Card Rewards.</html>";
            case SPARK_KINDLE:
                return "<html>Begin each match with " + allValues + " basic land(s) on battlefield.</html>";
            case FRACTURED_BINDING:
                return "<html>Your Commander costs " + allValues + " less to cast.</html>";
            case SPECTRAL_RECALIBRATION:
                return "<html>Gain " + allValues + " reroll(s) per Card Reward or Bazaar selection.</html>";
            default:
                return "<html>" + description + "</html>";
        }
    }

    /**
     * Build the all-values string showing values up to effectiveMaxRank(upgradeLevel).
     * The current rank value is highlighted with bold+underline.
     */
    private String buildAllValuesString(int currentRank, int upgradeLevel) {
        int effectiveMax = getEffectiveMaxRank(upgradeLevel);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < effectValues.length && (i + 1) <= effectiveMax; i++) {
            if (i > 0) {
                sb.append(" / ");
            }
            int rankForThisValue = i + 1;
            if (rankForThisValue == currentRank) {
                sb.append("<b><u>").append(effectValues[i]).append("</u></b>");
            } else {
                sb.append(effectValues[i]);
            }
        }
        return sb.toString();
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
