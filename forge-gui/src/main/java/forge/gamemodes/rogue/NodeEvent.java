package forge.gamemodes.rogue;

/**
 * Represents an Event node in a Rogue Commander path.
 * Events present the player with choices that have various outcomes.
 */
public class NodeEvent extends RoguePathNode {

    private RogueEvent event;

    public NodeEvent() {
        super();
    }

    public NodeEvent(RogueEvent event) {
        super();
        this.event = event;
    }

    public RogueEvent getEvent() { return event; }

    @Override
    public String toString() {
        return event != null ? "Event (" + event.getDisplayName() + ")" : "Event (???)";
    }
}
