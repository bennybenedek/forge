package forge.gamemodes.rogue;

import forge.gamemodes.rogue.effect.EventBoon;
import forge.gamemodes.rogue.npc.NPC;
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
            new EventChoice("Sacrifice!", "The planes took what is yours.",
                EventBoon.PLANAR_SACRIFICE),
            new EventChoice("Give and take", "The planes reshaped your arsenal.",
                EventBoon.PLANAR_SHUFFLE)
        )),

    AMBUSH("Ambush!",
        "Hostile forces materialize from a rift! Prepare for battle!",
        List.of(
            new EventChoice("Fight!", "You fought your way through.",
                EventBoon.SURPRISE_FIGHT)
        )),

    PLANAR_EXCHANGE("Planar Exchange",
        "A shimmering portal offers to reshape your arsenal.",
        List.of(
            new EventChoice("Step Through", "The planes shift your deck...",
                EventBoon.PLANAR_EXCHANGE),
            new EventChoice("Stay Put", "You decide not to risk it.",
                EventBoon.NOTHING)
        )),

    THORNS("Thorns",
        "The land itself rejects your presence. Thorns rise from the soil, leaving your footsteps stained with blood.",
        List.of(
            new EventChoice("Endure", "You suffer a wound.",
                EventBoon.GAIN_WOUND)
        )),

    HORROR("Horror",
        "A glistening horror clings to your soul, invisible but insatiable. It drinks deep, of your vitality, your future, and your fortune.",
        List.of(
            new EventChoice("Surrender", "You lose all your gold.",
                EventBoon.LOSE_ALL_GOLD),
            new EventChoice("Resist", "You lose all your echoes.",
                EventBoon.LOSE_ALL_ECHOES)
        )),

    LOST("Lost",
        "Your connection to your spark flickers. You reach for your Commander's presence, but the link has gone cold.",
        List.of(
            new EventChoice("Depart", "Your Commander disappeared into the void.",
                EventBoon.LOST_CONNECTION)
        )),

    SATCHEL("Satchel",
        "Hidden beneath a crumbled pillar, you find a satchel of cards bound in leather; remnants of a Planeswalker who walked here before you.",
        List.of(
            new EventChoice("Open the Satchel", "You find a hidden chest.",
                EventBoon.FIND_CHEST)
        )),

    SHRINE("Shrine",
        "A crumbling shrine pulses faintly with restorative energy. As you kneel, you immediately feel its soothing powers.",
        List.of(
            new EventChoice("Kneel", "You discover a hidden Sanctum.",
                EventBoon.FIND_SANCTUM)
        )),

    DISTORTION("Distortion",
        "Time lurches sideways. When the battle ends, there's nothing left to salvage. No spoils. No memory. Only silence.",
        List.of(
            new EventChoice("Suffer", "The silence consumed you.",
                EventBoon.SKIP_REWARDS)
        )),

    MEET_TYVAR("Tyvar Kell",
        "An elf Planeswalker emerges from a rift, his spark blazing with raw energy. " +
        "\"I am Tyvar Kell, and I know talent when I see it. Let me become your Commander's trainer.\"",
        List.of(
            new EventChoice("Accept", "Tyvar Kell will appear at the start of future Runs to train your Commander.",
                EventBoon.MEET_NPC_TYVAR)
        )) {
        @Override
        public boolean isAvailable() {
            return RogueMetaProgress.getInstance().getNPCLevel(NPC.TYVAR.id) < 1;
        }
    };

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

    /** Whether this event should appear in the event pool. Override for one-time events. */
    public boolean isAvailable() { return true; }

    @Override
    public String toString() { return displayName; }
}
