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
                    "in his notes. At the last, he puts down his pen. " +
                    "\"Then this reaches beyond a single hostile leader.\"",
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
    PATTERN_SPREADS(2) {
        @Override
        protected List<String> getStoryTextChunks() {
            return List.of(
                "\"Here, let me take that.\" Teferi sets the black material you've brought back beside the first " +
                    "sample. He listens as you describe the Planebounds you fought. \"You went through different " +
                    "worlds and found the same thing. I was hoping you'd come back and tell me we'd picked a " +
                    "particularly unpleasant Trail the first time.\"",
                "He carries the new sample to an Omenpath and draws a little of the passage's energy toward " +
                    "the glass. The black residue gathers into beads, pressing against the side nearest the " +
                    "Omenpath. Teferi draws the vessel back. \"Did you see that? This came from a Planebound. " +
                    "What does it have to do with an Omenpath?\"",
                "Teferi fits a stopper into the vessel, keeping his eyes on the black beads. \"It reminds me " +
                    "of something.\" He pauses before looking back at you. \"I'd like another sample before I " +
                    "put a name to it. Take a different Trail when you're ready. If you find this around the " +
                    "Planebounds there as well, bring some back. And keep it off your skin, Commander. Until " +
                    "I know what we're handling, take a pair of tongs with you.\""
            );
        }
    },
    PHYREXIA(3) {
        @Override
        protected List<String> getStoryTextChunks() {
            return List.of(
                "Teferi sets your sample beside the other two. He examines it under the lamp, then puts down " +
                    "the tongs. \"It's glistening oil, Commander. Phyrexian. I suspected it before you left. " +
                    "With the same signs coming back from a third Trail, I can't keep putting off that answer.\"",
                "\"New Phyrexia is cut off from the worlds we can reach. Imprisoned basically. When that happened, the Phyrexians " +
                    "left behind stopped moving. Their oil went inert with them.\" He watches the black material " +
                    "shift inside the glass. \"This should be inert too. Something is still giving it instructions. " +
                    "I need to find out how.\"",
                "He leaves the samples on the table and faces you. \"There's something else I should have told " +
                    "you. After the fighting here, I found signs that something in the Aether had survived. Perhaps " +
                    "even escaped. I couldn't find it, or prove it was still active. I thought I could settle " +
                    "that myself before involving anyone else.\" He keeps his eyes on yours. \"Then I asked you " +
                    "to go out there without telling you what I was afraid of. I'm sorry, Commander. You should " +
                    "have known.\"",
                "At the Omenpath, Teferi holds the samples close to the opening. The oil in all three vessels " +
                    "contracts at the same instant. He follows the pulse with his free hand, tracing it into " +
                    "the passage. \"There. I can pick it out now. The same pulse, further along the Omenpaths. " +
                    "We have something to follow.\" He lowers the vessels. \"The paths are more distorted in " +
                    "that direction. It'll be a harder journey, but it gives us a chance to find what's keeping " +
                    "this oil alive. Take time to prepare. I'll work out where you can enter.\""
            );
        }
    },
    RELAY(4) {
        @Override
        protected List<String> getStoryTextChunks() {
            return List.of(
                "Teferi draws the lamp closer to what you've brought back. Black filaments hang from a split " +
                    "metal casing, their ends torn. \"This was inside the Planebound?\" He lifts one of the " +
                    "filaments with his tongs and follows it into the casing. \"Phyrexian work. Someone put this " +
                    "in them and let it take root.\" He sets the filament down carefully. \"How much of what " +
                    "they did was even their own choice?\"",
                "\"There's still some of the Planebound's mana caught in here.\" He turns the casing so you can " +
                    "see the channels beneath it. \"It was drawing power from them, and through them, from the " +
                    "Plane itself.\" He looks up as you describe how the nearby Omenpaths settled after the " +
                    "battle. \"And that happened when you defeated them? Then this stopped working when it lost " +
                    "its host. You cut off whatever it was doing to those paths.\"",
                "Teferi lowers the lamp. \"They're using the Planebounds to power these things. All that mana, " +
                    "taken from someone who has to fight anyone trying to get near them.\" He studies the torn " +
                    "filaments again. \"I want to know what they're spending it on. Another piece like this " +
                    "would help us find out, if you can recover one along the next Trail. For now, tell me " +
                    "what happened when this one stopped. Did the paths settle straight away?\""
            );
        }
    },
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

            if (isTriggerConditionMet(progress)) {
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

    @Override
    public boolean isTriggerConditionMet(RogueMetaProgress progress) {
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
