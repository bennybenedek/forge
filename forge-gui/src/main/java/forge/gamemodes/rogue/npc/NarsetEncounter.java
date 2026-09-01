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
                    "A woman in travel-worn Jeskai robes steps out of the planar turbulence, calm and composed despite " +
                        "the chaos still breaking around her.",
                    "One hand rests near a scroll case at her hip while her " +
                        "steady gaze studies you for a long moment. \"Listen. The worlds ahead are not scattering at " +
                        "random. To stop the Phyrexian Invasion, you must understand what moves between them and why.",
                    "You seem to improve in using Planeswalking and Chaos to your advantage. But you are far from mastering it. Call Me Narset. And let me help you with your task from now on.\""
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
            "Narset greets you with a knowing smile. \"Choose a gift from the planes.\"",
            "\"Every plane leaves a pattern behind,\" Narset says, unrolling a map of impossible angles. " +
                "\"Take the one that will teach you most.\"",
            "Narset studies the horizon as the planar wind pulls at her robes. \"The path ahead will shift again. " +
                "Prepare before it does.\""
        );
    }
}
