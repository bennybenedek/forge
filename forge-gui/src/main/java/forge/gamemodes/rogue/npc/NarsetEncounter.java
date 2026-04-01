package forge.gamemodes.rogue.npc;

import forge.gamemodes.rogue.RogueMetaProgress;
import forge.gamemodes.rogue.RogueRun;
import forge.gamemodes.rogue.effect.NPCBoon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Narset, Planeshard Collector — Planechase-themed NPC.
 * Unlocks organically by rolling Chaos on the planar die across multiple matches.
 * Once fully unlocked (level 3), offers Planechase-themed boons at run start.
 */
public enum NarsetEncounter implements NPCEncounter {

    /** Levels 0–1: silently increment NPC level when chaos was rolled. */
    BEFORE_REVEAL(0) {
        @Override
        public NPCContext onAfterMatch(RogueRun run) {
            if (run.getLastMatchData().chaosCount() <= 0) return null;
            int currentLevel = RogueMetaProgress.getInstance().getNPCLevel(getNpc().id);
            if (currentLevel >= 2) return null;
            incrementNpcLevel();
            return null;
        }
    },

    /** Level 2: Narset reveals herself on the 3rd chaos match. */
    REVEAL(2) {
        @Override
        public NPCContext onAfterMatch(RogueRun run) {
            if (run.getLastMatchData().chaosCount() <= 0) return null;
            int currentLevel = RogueMetaProgress.getInstance().getNPCLevel(getNpc().id);
            if (currentLevel > 2) return null; // already revealed
            incrementNpcLevel();
            return buildContext(
                "Narset steps forward. \"You've proven yourself. Let me share my knowledge of the planes.\"",
                List.of());
        }
    },

    /** Level 3+: offer boons at run start. */
    OFFERING_BOONS(3) {
        @Override
        public NPCContext onRunStart() {
            List<NPCBoon> pool = new ArrayList<>(List.of(
                NPCBoon.NARSET_TRAVELER, NPCBoon.NARSET_ALCHEMIST,
                NPCBoon.NARSET_CHAOSBOUND, NPCBoon.NARSET_GOD_OF_CHAOS
            ));
            Collections.shuffle(pool);
            List<NPCContext.NPCChoice> choices = pool.subList(0, 3).stream()
                .map(b -> new NPCContext.NPCChoice(b.getDisplayName(), b))
                .toList();
            return buildContext(
                "Narset greets you with a knowing smile. \"Choose a gift from the planes.\"",
                choices
            );
        }
    };

    private final int requiredLevel;
    NarsetEncounter(int requiredLevel) { this.requiredLevel = requiredLevel; }
    @Override public NPC getNpc() { return NPC.NARSET; }
    @Override public int getRequiredLevel() { return requiredLevel; }
}
