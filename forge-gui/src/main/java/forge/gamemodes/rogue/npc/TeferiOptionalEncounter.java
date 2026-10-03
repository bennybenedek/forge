package forge.gamemodes.rogue.npc;

import forge.gamemodes.rogue.RogueMetaProgress;
import java.util.List;

/**
 * Teferi encounters that can occur independently of his linear story progression.
 */
public enum TeferiOptionalEncounter implements NPCOptionalEncounter {

    FIRST_RUN_LOSS("teferi:first_run_loss") {
        @Override
        public List<String> getStoryTextChunks() {
            return List.of(
                "Teferi offers you his hand. \"Easy, Commander. You're back in the Aether. Take a moment.\"",
                "He traces a small circle in the air between you. For a moment, its light holds still against " +
                    "your skin. \"A temporal link, anchored here. I establish it just before you leave. If you're " +
                    "killed, or the expedition is lost, I can bring you back here as you were before you left. " +
                    "That's how I brought you back this time. We'll both remember what happened. Time on the " +
                    "other worlds keeps moving forward.\"",
                "He lowers his hand. \"That's why I stay here to maintain the link. Someone has to be here to " +
                    "bring you back. Just in case you were wondering why I just keep talking instead of coming with you.\""
            );
        }

        @Override
        public boolean isTriggerConditionMet(RogueMetaProgress progress) {
            return progress.getTotalRunsCompleted() > progress.getTotalRunsWon();
        }
    };

    private final String id;

    TeferiOptionalEncounter(String id) {
        this.id = id;
    }

    @Override
    public NPC getNpc() {
        return NPC.TEFERI;
    }

    @Override
    public String getId() {
        return id;
    }
}
