package forge.gamemodes.rogue;

/**
 * Descension levels for Rogue Commander — metadata only (names/descriptions for the UI).
 * Game effects are implemented via card scripts (Levels 2/3) or path generation logic (Level 1).
 */
public enum DescensionLevel {
    LEVEL_1(1, "Elite Paths",
        "2 random Normal Planes of the Path are replaced by Elite Planes."),
    LEVEL_2(2, "Bloodthirsty",
        "Whenever a creature an opponent controls deals damage to you, it deals 1 additional damage."),
    LEVEL_3(3, "Taxing Mana",
        "Every spell you cast costs {1} more to cast.");

    public final int level;
    public final String name;
    public final String description;

    DescensionLevel(int level, String name, String description) {
        this.level = level;
        this.name = name;
        this.description = description;
    }

    public static DescensionLevel forLevel(int level) {
        for (DescensionLevel dl : values()) {
            if (dl.level == level) return dl;
        }
        return null;
    }

    public static int getMaxLevel() {
        return values().length;
    }
}
