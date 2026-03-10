package forge.gamemodes.rogue;

import java.util.List;

/**
 * Catalog of all events in Rogue Commander mode.
 * Each event has a description and a list of choices with associated effects.
 */
public enum RogueEvent {

    WANDERING_HEALER("Wandering Healer",
        "A mysterious healer offers their services... for a price.",
        List.of(
            new EventChoice("Accept", "The healer mends your wounds.",
                EventBoon.HEALERS_TOUCH),
            new EventChoice("Decline", "You wave the healer away.",
                EventBoon.NOTHING)
        )),

    PLANAR_RIFT("Planar Rift",
        "A rift in the planes tears open before you.",
        List.of(
            new EventChoice("Enter the Rift", "The rift's energy empowers your Commander!",
                EventBoon.COMMANDER_BOOST),
            new EventChoice("Harvest the Energy", "You siphon raw mana from the rift.",
                EventBoon.RIFT_ENERGY),
            new EventChoice("Walk Away", "Discretion is the better part of valor.",
                EventBoon.NOTHING)
        )),

    MERCHANT_CARAVAN("Merchant Caravan",
        "A caravan of planar merchants sets up shop.",
        List.of(
            new EventChoice("Browse Their Wares", "The merchant thanks you for the business.",
                EventBoon.BROWSE_WARES),
            new EventChoice("Rob Them", "You take what you want by force.",
                EventBoon.CARAVAN_ROB)
        ));

    public record EventChoice(String label, String resultText, EventBoon effect) {}

    private final String displayName;
    private final String description;
    private final List<EventChoice> choices;

    RogueEvent(String displayName, String description, List<EventChoice> choices) {
        this.displayName = displayName;
        this.description = description;
        this.choices = choices;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public List<EventChoice> getChoices() { return choices; }
}
