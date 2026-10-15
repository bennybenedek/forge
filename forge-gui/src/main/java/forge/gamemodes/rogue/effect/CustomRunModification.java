package forge.gamemodes.rogue.effect;

import forge.gamemodes.rogue.RogueCustomDeckGenerator;
import forge.gamemodes.rogue.RogueDeck;
import forge.item.PaperCard;

/** A run-start modification selected independently of the curated Commander deck. */
public enum CustomRunModification implements RogueEffect {
    MANIA("custom:random_rogue_commander", "Mania",
        "A random legendary creature will be chosen as your Commander. "
            + "Your starting deck and Reward Pool will be automatically created according to synergy "
            + "and color identity.", false) {
        @Override
        public RogueDeck createRogueDeck(RogueDeck selectedDeck, PaperCard commander) {
            return RogueCustomDeckGenerator.generate();
        }
    },
    CHOSEN_ONE("custom:choose_rogue_commander", "Chosen One",
        "Choose any legal legendary creature as your Commander. "
            + "Your starting deck and Reward Pool will be automatically created according to synergy "
            + "and color identity.", false) {
        @Override
        public RogueDeck createRogueDeck(RogueDeck selectedDeck, PaperCard commander) {
            return RogueCustomDeckGenerator.generateForChosenCommander(commander);
        }
    };

    private final String id;
    private final String displayName;
    private final String description;
    private final boolean recordsCuratedCommanderRewards;

    CustomRunModification(String id, String displayName, String description,
            boolean recordsCuratedCommanderRewards) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.recordsCuratedCommanderRewards = recordsCuratedCommanderRewards;
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getRawDescription() { return description; }

    public RogueDeck createRogueDeck(RogueDeck selectedDeck, PaperCard commander) {
        return selectedDeck;
    }

    public boolean recordsCuratedCommanderRewards() {
        return recordsCuratedCommanderRewards;
    }

    public static CustomRunModification fromId(String id) {
        for (CustomRunModification modification : values()) {
            if (modification.id.equals(id)) return modification;
        }
        return null;
    }
}
