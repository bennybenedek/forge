package forge.gamemodes.rogue.npc;

import forge.gamemodes.rogue.RogueMetaProgress;
import java.util.List;

/**
 * Teferi's linear, between-run main-story progression.
 * Story chunks remain empty until the corresponding narrative scenes are implemented.
 */
public enum TeferiEncounter implements NPCEncounter {

    BEFORE_FIRST_RUN(0),
    FIRST_EVIDENCE(1),
    PATTERN_SPREADS(2),
    PHYREXIA(3),
    RELAY(4),
    NETWORK(5),
    ARTIFICIAL_ROUTE(6),
    DIRECTIVE(7),
    FIRST_BREACH(8),
    SYNCHRONIZATION(9),
    NEMESIS(10),
    STORY_COMPLETE(11);

    private final int requiredLevel;

    TeferiEncounter(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    @Override
    public NPC getNpc() {
        return NPC.TEFERI;
    }

    @Override
    public int getRequiredLevel() {
        return requiredLevel;
    }

    @Override
    public NPCContext onBetweenRuns(RogueMetaProgress progress) {
        List<String> storyTextChunks = getStoryTextChunks();
        if (storyTextChunks.isEmpty()) {
            return null;
        }

        if (this == BEFORE_FIRST_RUN && progress.getTotalRunsStarted() > 0) {
            progress.setNPCLevel(getNpc().id, getFirstFutureStoryLevel(progress));
            return null;
        }

        if (!isMilestoneReached(progress)) {
            return null;
        }

        NPCContext context = buildContext(storyTextChunks, List.of());
        incrementNpcLevel();
        return context;
    }

    protected List<String> getStoryTextChunks() {
        return List.of();
    }

    private boolean isMilestoneReached(RogueMetaProgress progress) {
        return switch (this) {
            case BEFORE_FIRST_RUN -> progress.getTotalRunsStarted() == 0;
            case FIRST_EVIDENCE -> progress.getDistinctCommandersWon() >= 1;
            case PATTERN_SPREADS -> progress.getDistinctCommandersWon() >= 2;
            case PHYREXIA -> progress.getDistinctCommandersWon() >= 3;
            case RELAY -> progress.getHighestDescensionWon() >= 1;
            case NETWORK -> progress.getHighestDescensionWon() >= 2;
            case ARTIFICIAL_ROUTE -> progress.getHighestDescensionWon() >= 3;
            case DIRECTIVE -> progress.getHighestDescensionWon() >= 4;
            case FIRST_BREACH -> progress.getHighestDescensionWon() >= 5;
            case SYNCHRONIZATION -> progress.getHighestDescensionWon() >= 6;
            case NEMESIS -> progress.getHighestDescensionWon() >= 7;
            case STORY_COMPLETE -> false;
        };
    }

    private static int getFirstFutureStoryLevel(RogueMetaProgress progress) {
        int highestDescensionWon = progress.getHighestDescensionWon();
        if (highestDescensionWon > 0) {
            return Math.min(STORY_COMPLETE.requiredLevel, RELAY.requiredLevel + highestDescensionWon);
        }
        return FIRST_EVIDENCE.requiredLevel + Math.min(progress.getDistinctCommandersWon(), 3);
    }
}
