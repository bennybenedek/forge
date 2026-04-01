package forge.gamemodes.rogue.npc;

import forge.gamemodes.rogue.RogueEvent;

/**
 * Context passed to NPC encounters before an event starts.
 * Encounters may replace the event that will be shown to the player.
 */
public class EventContext {

    /** The event originally stored on the node. */
    public final RogueEvent originalEvent;

    /** Optional replacement chosen by NPC preprocessing. */
    public RogueEvent eventOverride;

    public EventContext(RogueEvent event) {
        this.originalEvent = event;
    }

    public RogueEvent getResolvedEvent() {
        return eventOverride != null ? eventOverride : originalEvent;
    }
}
