package forge.gamemodes.rogue.npc;

import forge.gamemodes.rogue.RogueMetaProgress;
import forge.gamemodes.rogue.RogueEvent;
import forge.gamemodes.rogue.RogueRun;
import forge.gamemodes.rogue.effect.NPCEffect;
import java.util.Collections;
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
            int currentLevel = getProgress().getNPCLevel(getNpc().id);
            if (currentLevel >= REVEAL.getRequiredLevel()) {
                return;
            }
            incrementNpcLevel();
        }
    },

    /** On the third event, Tyvar replaces the planned event with his reveal event. */
    REVEAL(2) {
        @Override
        public void onBeforeEvent(EventContext ctx) {
            int currentLevel = getProgress().getNPCLevel(getNpc().id);
            if (currentLevel > getRequiredLevel()) {
                return;
            }
            incrementNpcLevel();
            ctx.eventOverride = RogueEvent.MEET_TYVAR;
        }
    },

    /** After being met, Tyvar offers Commander training boons at the start of runs. */
    OFFERING_BOONS(3) {
        @Override
        public NPCContext onRunStart(RogueRun run) {
            List<NPCEffect> pool = NPCEffect.getEffectsForNpc(getNpc(), run);
            Collections.shuffle(pool);
            return buildContext(
                "Tyvar Kell steps from the shadows, his elven features lit by the glow of his Planeswalker spark. " +
                "\"I've watched you fight, and I see potential. Let me train your Commander.\"",
                pool.subList(0, 3).stream()
                    .map(effect -> new NPCContext.NPCChoice(effect.getDisplayName(), effect))
                    .toList()
            );
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

    private static RogueMetaProgress getProgress() {
        return RogueMetaProgress.getInstance();
    }
}
