package forge.gamemodes.rogue;

/**
 * Defines the three progressive Aether Upgrades purchasable with Sparks.
 * Upgrades must be purchased in order (1 → 2 → 3).
 */
public enum AetherUpgrade {
    //                      lvl  name                                   description                                           spark  extraSlots  extraRanks
    LEVEL_1(1, "Aether Upgrade 1", "Unlock new Boons in the Aether.", 1, 0, 0),
    LEVEL_2(2, "Aether Upgrade 2", "Gain +1 active Boon slot.", 2, 1, 0),
    LEVEL_3(3, "Aether Upgrade 3", "Boons gain +1 maximum rank.", 3, 0, 1),
    LEVEL_4(4, "Aether Upgrade 4", "Gain +1 active Boon slot.", 4, 1, 0);

    public final int level;
    public final String name;
    public final String description;
    public final int sparkCost;
    public final int extraBoonSlots;  // How many extra active slots this upgrade adds
    public final int extraBoonRanks;  // How many extra max ranks this upgrade adds to all boons

    AetherUpgrade(int level, String name, String description, int sparkCost,
                  int extraBoonSlots, int extraBoonRanks) {
        this.level = level;
        this.name = name;
        this.description = description;
        this.sparkCost = sparkCost;
        this.extraBoonSlots = extraBoonSlots;
        this.extraBoonRanks = extraBoonRanks;
    }

    public static AetherUpgrade forLevel(int level) {
        for (AetherUpgrade u : values()) {
            if (u.level == level) return u;
        }
        return null;
    }

    public static int getMaxLevel() {
        return values().length;
    }
}
