package forge.gamemodes.rogue.npc;

import forge.gamemodes.rogue.RogueRun;
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
            incrementNpcLevel();
            return null;
        }
    },

    /** Level 2: Narset reveals herself on the 3rd chaos match. */
    REVEAL(2) {
        @Override
        public NPCContext onAfterMatch(RogueRun run) {
            if (run.getLastMatchData().chaosCount() <= 0) return null;
            incrementNpcLevel();
            return buildContext(
                List.of(
                    "Out of nowhere, an Omenpath opens at the edge of the battlefield. A woman in travel-worn Jeskai " +
                        "robes steps through, braces one hand against the ground, and waits for the passage to close before rising.",
                    "She studies the distortion still flickering above the battlefield, then opens a scroll filled " +
                        "with sketches. \"I've seen this before, on another Plane. It behaved almost the same there.\"",
                    "She looks from the scroll to the Commander. \"It's gone. I arrived too late. I'm Narset. " +
                        "Tell me what you saw here. After that, bring me what you notice on each Trail. I'll compare " +
                        "it with my records and help you prepare for the next.\""
                ),
                List.of());
        }
    },

    /** Level 3+: offer boons at run start. */
    OFFERING_BOONS(3) {
        @Override
        public NPCContext onRunStart(RogueRun run) {
            return buildOfferingBoonsContext(run);
        }
    };

    private final int requiredLevel;
    NarsetEncounter(int requiredLevel) { this.requiredLevel = requiredLevel; }
    @Override public NPC getNpc() { return NPC.NARSET; }
    @Override public int getRequiredLevel() { return requiredLevel; }

    @Override
    public List<String> getOfferingBoonMonologues() {
        return List.of(
            "Narset has arranged her notes beside a map marked with recent Omenpaths. One route ends in a dense knot " +
                "of corrections. \"The Plane changed while I was still recording the first change. Afterward, I wrote " +
                "down how I should have prepared.\"",
            "Narset weighs down the corners of a map with smooth stones. The same symbol appears beside two distant " +
                "Planes. \"The distortion looked the same in both places. Each Plane responded differently. " +
                "But these preparations should help for both of them.\"",
            "Narset draws a new line across an Omenpath chart, then sets down her charcoal. \"This route breaks the " +
                "earlier pattern. Good. I'll try to alter your preparations accordingly. Choose one.\""
        );
    }
}
