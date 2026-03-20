package forge.gamemodes.rogue.npc;

import java.util.List;

/**
 * Interface for NPC encounters that trigger at specific points during a Rogue Commander run.
 * Each trigger returns an NPCContext if the NPC wants to interact, or null to skip.
 */
public interface NPCEncounter {

    /** The NPC this encounter belongs to. */
    NPC getNpc();

    /** Minimum NPC level required for this encounter to fire. */
    int getRequiredLevel();

    /** Builds an NPCContext using shared NPC identity and the given per-level data. */
    default NPCContext buildContext(String flavorText, List<NPCContext.NPCChoice> choices) {
        return new NPCContext(getNpc(), flavorText, choices);
    }

    /** Fired once when a new run is created. Return non-null to show NPC dialog. */
    default NPCContext onRunStart() { return null; }
}
