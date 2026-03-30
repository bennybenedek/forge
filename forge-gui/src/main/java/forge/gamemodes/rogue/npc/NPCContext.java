package forge.gamemodes.rogue.npc;

import forge.gamemodes.rogue.effect.NPCBoon;
import java.util.List;

/**
 * Data bag for NPC dialog display. Holds NPC identity, flavor text, and available choices.
 */
public record NPCContext(NPC npc, String flavorText, List<NPCChoice> choices) {

    public record NPCChoice(String label, NPCBoon boon) {}
}
