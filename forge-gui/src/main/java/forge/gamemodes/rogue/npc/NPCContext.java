package forge.gamemodes.rogue.npc;

import forge.gamemodes.rogue.effect.NPCEffect;
import java.util.List;

/**
 * Data bag for NPC dialog display. Holds NPC identity, flavor text, and available choices.
 */
public record NPCContext(NPC npc, String flavorText, List<NPCChoice> choices, String displayNameOverride,
                         Integer avatarIndexOverride) {

    public NPCContext(NPC npc, String flavorText, List<NPCChoice> choices) {
        this(npc, flavorText, choices, null, null);
    }

    public String displayName() {
        return displayNameOverride != null ? displayNameOverride : npc.name;
    }

    public int avatarIndex() {
        return avatarIndexOverride != null ? avatarIndexOverride : npc.avatarIndex;
    }

    public record NPCChoice(String label, NPCEffect npcEffect) {}
}
