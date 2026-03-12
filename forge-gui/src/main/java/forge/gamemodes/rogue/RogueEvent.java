package forge.gamemodes.rogue;

import forge.gamemodes.rogue.effect.EventBoon;
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
        )),

    PLANAR_TRIBUTE("Planar Tribute",
        "The planes demand tribute.",
        List.of(
            new EventChoice("Sacrifice!", "The planes take what is yours.",
                EventBoon.PLANAR_SACRIFICE),
            new EventChoice("Give and take", "The planes reshape your arsenal.",
                EventBoon.PLANAR_SHUFFLE)
        )),

    AMBUSH("Ambush!",
        "Hostile forces materialize from a rift! Prepare for battle!",
        List.of(
            new EventChoice("Fight!", "You have no choice but to fight.",
                EventBoon.SURPRISE_FIGHT)
        )),

    PLANAR_EXCHANGE("Planar Exchange",
        "A shimmering portal offers to reshape your arsenal.",
        List.of(
            new EventChoice("Step Through", "The planes shift your deck...",
                EventBoon.PLANAR_EXCHANGE),
            new EventChoice("Stay Put", "You decide not to risk it.",
                EventBoon.NOTHING)
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

    @Override
    public String toString() { return displayName; }
}
