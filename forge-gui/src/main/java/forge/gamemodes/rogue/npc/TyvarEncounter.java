package forge.gamemodes.rogue.npc;

import forge.gamemodes.rogue.RogueEvent;
import forge.gamemodes.rogue.RogueRun;
import java.util.List;

/**
 * Tyvar Kell — Commander Trainer NPC.
 * Progresses by entering events, then replaces the reveal event before offering boons on future runs.
 */
public enum TyvarEncounter implements NPCEncounter {

    /** Hidden buildup phase: each entered event advances Tyvar's reveal progress. */
    BEFORE_REVEAL(0) {
        @Override
        public void onBeforeEvent(EventContext ctx) {
            incrementNpcLevel();
        }
    },

    /** On the third event, Tyvar replaces the planned event with his reveal event. */
    REVEAL(2) {
        @Override
        public void onBeforeEvent(EventContext ctx) {
            incrementNpcLevel();
            ctx.eventOverride = RogueEvent.MEET_TYVAR;
        }
    },

    /** After being met, Tyvar offers Commander training boons at the start of runs. */
    OFFERING_BOONS(3) {
        @Override
        public NPCContext onRunStart(RogueRun run) {
            return buildOfferingBoonsContext(run);
        }
    };

    private final int requiredLevel;

    TyvarEncounter(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    @Override
    public NPC getNpc() { return NPC.TYVAR; }

    @Override
    public int getRequiredLevel() { return requiredLevel; }

    @Override
    public List<String> getOfferingBoonMonologues() {
        return List.of(
            "Tyvar Kell steps from the shadows, his elven features lit by the glow of his Planeswalker spark. " +
                "\"I've watched you fight, and I see potential. Let me train your Commander.\"",
            "Tyvar cracks his knuckles and grins. \"A commander should lead from the front. Pick a lesson, " +
                "and I'll make sure yours hits harder than before.\"",
            "\"Raw strength is useful,\" Tyvar says, pacing beside your camp, \"but knowing where to spend it wins wars. " +
                "Choose your training.\""
        );
    }
}
