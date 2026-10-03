package forge.gamemodes.rogue.npc;

import forge.gamemodes.rogue.RogueMetaProgress;
import java.util.List;

/**
 * Teferi's linear, between-run main-story progression.
 * Unwritten story milestones retain empty story chunks.
 */
public enum TeferiEncounter implements NPCEncounter {

    BEFORE_FIRST_RUN(0) {
        @Override
        protected List<String> getStoryTextChunks() {
            return List.of(
                "A dark-skinned man in blue robes looks up from a dismantled machine. The tool in his hand hangs forgotten for a moment; " +
                    "then he sets it down and comes to meet you, smiling. \"Commander. You came.\" He offers his " +
                    "hand. \"I'm Teferi. Forgive me, I keep forgetting we haven't met.\"",
                "\"Come in. Welcome to the Aether.\" He leads you past a broken archway, keeping to the cleared " +
                    "side of the passage. \"I built it outside ordinary space and time. Thought I could give people " +
                    "somewhere the phyrexian invasion wouldn't reach them.\" He stops beside a scar in the stone. " +
                    "\"Well, I was wrong. The invasion is over, but the Aether was heavily damaged despite fighting the Phyrexians off from here. Don't worry. I think we can put the place back together.\"",
                "\"But to be honest, that's not why I asked you here. I'm worried about the Omenpaths, the passages " +
                    "that let us travel between worlds. The leaders of those worlds are turning on people who " +
                    "cross their lands, and the paths themselves are behaving...strangely. " +
                    "I've found the same disturbances along Omenpaths leading to entirely different worlds.. I wanted to see " +
                    "where this was leading. So I looked ahead.\"",
                "Teferi turns to face you. \"I've seen streets full of bodies, Commander. People who survived " +
                    "the invasion, killed outside their own homes. Whole populations of the Multiverse wiped out. I kept looking for a future where they lived. " +
                    "And I found some. You kept turning up in them.\" He lets you take that in. \"Now you know why " +
                    "I'm glad to see you. I don't know what you did to help those people. That's something we'll " +
                    "have to work out together.\"",
                "\"We can start with the leaders of the Planes, the ones we call Planebounds. Follow the Trail of Omenpaths " +
                    "and see what's happened to them. Bring back anything you think I should see. " +
                    "And Commander... Take care of yourself out there. When you feel ready, press 'Start Run'.\""
            );
        }
    },
    FIRST_EVIDENCE(1) {
        @Override
        protected List<String> getStoryTextChunks() {
            return List.of(
                "Teferi pulls a chair up to the table. \"Sit down. You've earned it.\" He " +
                    "listens as you tell him what happened along the Trail, marking each encounter " +
                    "in his notes.At the last, he puts down his pen. " +
                    "\"Then this reaches beyond a single hostile ruler.\"",
                "Among the things you've brought back, a black residue clings to a fragment recovered after " +
                    "one of the battles. Teferi lifts it with a pair of tongs and turns it under the lamp. " +
                    "\"You found this near one of the affected Planebounds?\" He studies it a while longer, then " +
                    "places it in a glass vessel. \"I can't tell you what it is yet. Leave it with me.\"",
                "He moves his notes aside to make room for the vessel. \"For the next expedition, we need a " +
                    "different Trail, through Omenpaths leading to other Planes. Look for more of this substance " +
                    "around the Planebounds. We need to know how far it's spread. I'll examine what we have " +
                    "while you prepare.\""
            );
        }
    },
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
        if (!storyTextChunks.isEmpty()) {
            if (this == BEFORE_FIRST_RUN && progress.getTotalRunsStarted() > 0) {
                progress.setNPCLevel(getNpc().id, getFirstFutureStoryLevel(progress));
                return null;
            }

            if (isMilestoneReached(progress)) {
                NPCContext context = buildContext(storyTextChunks, List.of());
                incrementNpcLevel();
                return context;
            }
        }

        return null;
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
