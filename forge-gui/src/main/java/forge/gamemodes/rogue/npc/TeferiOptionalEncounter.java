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
                    "killed, or the expedition is lost, I'll revert the time and bring you back here as you were before you left. " +
                    "It sounds weirder than it is, trust me. That's how I brought you back this time. We'll both remember what happened.\"",
                "And what's even more important: You'll keep the Echoes gained in all Runs, Win or Loss. Use these memories of the Aether "
                + "to rebuild lost Aetherworks that make you stronger in future Runs.",
                "He lowers his hand. \"Well, at least now you know why I have to stay here. Someone has to be here to " +
                    "bring you back. Just in case you were wondering why I just keep talking instead of coming with you.\""
            );
        }

        @Override
        public boolean isTriggerConditionMet(RogueMetaProgress progress) {
            return progress.getTotalRunsCompleted() > progress.getTotalRunsWon();
        }
    },
    SECOND_RUN_LOSS("teferi:second_run_loss") {
        @Override
        public List<String> getStoryTextChunks() {
            return List.of(
                "Back already? I meant...keep it up, Commander. You're getting stronger every second that I am watching you.",
                "Oh, and did you know that I try to keep track of your memories ad findings during your Runs?\n"
                + "Open the 'Codex' to view and reset your overall game progress, stats, unlocked cards and tutorials.\n"
                + "View all your past Runs and Rogue Decks in the 'History'."
            );
        }

        @Override
        public boolean isTriggerConditionMet(RogueMetaProgress progress) {
            return progress.getTotalRunsCompleted() > progress.getTotalRunsWon() + 1;
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
