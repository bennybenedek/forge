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
                    "metal casing, torn at their ends. \"This was inside the Planebound?!\" He lifts one of the " +
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
                    "would help us find out, if you can recover one along the next Trail. We should lose no further time.\""
            );
        }
    },
    NETWORK(5) {
        @Override
        protected List<String> getStoryTextChunks() {
            return List.of(
                "\"Come and look at this, Commander.\" Teferi lays the recovered implants side by side, lining " +
                    "up the channels inside their casings. He feeds a little mana into one. Its filaments " +
                    "twitch, and a moment later those of its neighbor twitch in reply. \"They answer each other. " +
                    "See how the channels match? They're built to pass power and instructions between them. " +
                    "Relays...Spread across the Planes, with the Planebounds keeping them supplied.\n"
                + "And you've been breaking them. That matters.\"",
                "\"I've been watching the pulses through the Omenpaths while you were out\" Teferi sits back. \"Something is " +
                    "working to keep this running, making adjustments as you damage it. I'd like to know who's " +
                    "taking such an interest in your work.\"",
                "He spreads out his chart of the Omenpaths and makes room for you beside him. \"We know how " +
                    "they're drawing power now, and how they're passing it around. We still need to find out " +
                    "what it's all for. And we should do it quickly...\""
            );
        }
    },
    ARTIFICIAL_ROUTE(6) {
        @Override
        protected List<String> getStoryTextChunks() {
            return List.of(
                "\"This gets more and more awkward. Someone is using the Planebounds to build a road between worlds.\" Teferi draws a line " +
                    "across the charts on the wall, through the places you've fought your way across. " +
                    "\"The relays steal their mana and use it to force Omenpaths open where they would never " +
                    "normally lead. One passage after another, all the way to somewhere this creature wants " +
                    "to reach.\" His chalk stops at the edge of the chart. \"I don't know what's on the other " +
                    "end. But every relay you've broken has torn a piece out of that road. You've been " +
                    "holding it back, Commander. People are still rebuilding their homes after the last " +
                    "invasion. I won't stand here and watch something Phyrexian open a path into their lives again.\"",
                "\"It's clear to me now what's causing all of this. A Conductor! A nasty Phyrexian creature, bred to keep armies " +
                    "moving between worlds. Realmbreaker, the tree they used to invade us, opened the way. " +
                    "Conductors kept those passages working together." +
                    "Teferi puts down the chalk. \"Now it's doing the same job with the Omenpaths.\"",
                "His eyes turn to the shattered archway. \"One of them survived here. While the Phyrexians " +
                    "outside fell still, it was sheltered in the Aether, beyond ordinary space and time. " +
                    "That's why it could keep moving. That's how it got away.\" He walks to the archway and " +
                    "presses his palm against the broken stone. \"I built this place to keep people safe from " +
                    "Phyrexia. And when the invasion ended, my refuge protected one of the creatures that " +
                    "are responsible for all of this. It's...my fault that this is happening.\" He turns back to you. \"You've already damaged its work, " +
                    "Commander. Now we know what we're hunting. I'll help you find it.\""
            );
        }
    },
    DIRECTIVE(7) {
        @Override
        protected List<String> getStoryTextChunks() {
            return List.of(
                "\"Wait. That part's still receiving something.\" Teferi holds a hand above the open relay " +
                    "you brought back. Deep inside its casing, a knot of black fibers pulses. " +
                    "\"You kept enough of it intact. Give me a moment.\"",
                "Teferi studies the repeating pulses for a moment, then translates: " +
                    "\"SOURCE LOST. RESTORE CONNECTION TO NEW PHYREXIA.\" He looks up at you. " +
                    "\"That's where the road leads. Back to " +
                    "New Phyrexia. Good God... The Conductor lost contact when we cut that world off, and it's been trying " +
                    "to open a way back ever since. Every Planebound it took, all that stolen mana... " +
                    "it's using them to break through!\"",
                "A fresh pulse runs through the fibers. \"It's still sending that order.\" " +
                    "He draws his hand away. \"We survived that invasion because New Phyrexia was cut off. " +
                    "If this creature opens a passage, we could face it all again. I don't know whether " +
                    "it can reach that far. Let's find out immediately!\""
            );
        }
    },
    FIRST_BREACH(8) {
        @Override
        protected List<String> getStoryTextChunks() {
            return List.of(
                "Teferi lets you finish describing the tear that opened along the Trail. The relays flared " +
                    "together, and for a few moments you could see another world: white towers rising from " +
                    "black sinew, with metal branches stretched across the sky. Then the opening crumpled " +
                    "and vanished. \"I've seen that place, Commander. That was New Phyrexia.\" He sits down " +
                    "slowly. \"It reached them.\"",
                "\"I hoped the distance would defeat it. That no amount of stolen mana could reach a world " +
                    "cut off from us like that.\" He looks back at you. \"But those relays worked together " +
                    "long enough to tear a way beyond the Multiverse. It collapsed because they couldn't hold it open. " +
                    "Next time, it may last longer. Long enough for something to cross.\"",
                "For a moment Teferi says nothing. Then he leans forward. \"I'm glad you came straight back. " +
                    "If you see it open again, stay on this side. I know how much I'm asking of you, but " +
                    "I won't ask you to set foot in that world. We should focus on the Planebounds instead. Let's break their network before it's too late!\""
            );
        }
    },
    SYNCHRONIZATION(9) {
        @Override
        protected List<String> getStoryTextChunks() {
            return List.of(
                "Teferi meets you before you've had time to set down your things. \"The breach is still open. " +
                    "I've been watching it since you left. I can see New Phyrexia through it, and each time " +
                    "the remaining relays send their power together, the tear widens.\" He waits until he has " +
                    "your full attention. \"Its edges are still breaking apart. That's all that's keeping " +
                    "the Conductor from giving them a permanent way out.\"",
                "\"One Planebound is holding that opening in place. Every surge from its relay forces the " +
                    "tear wider, and the others are gathering their power around it. That's the one you " +
                    "have to reach. Defeat that Planebound and break its relay before they can hold the whole " +
                    "passage steady. Once they do, we'll have an open road to New Phyrexia.\"",
                "\"The breach has exposed a Trail to that last Planebound. I can point you to its entrance. " +
                    "You'll be fighting on our side of the tear, with New Phyrexia just beyond it.\" Teferi " +
                    "steps aside to let you pass. \"We only got one last chance, Commander.\""
            );
        }
    },
    NEMESIS(10) {
        @Override
        protected List<String> getStoryTextChunks() {
            return List.of(
                "The captive Conductor stirs as you set it down. Pale porcelain plates unfold around a " +
                    "knot of black flesh. Long, rootlike limbs strain against its bonds, and something " +
                    "pulses between its metal ribs. Teferi steps in front of you. \"You broke the last relay. " +
                    "The breach is closing.\" His eyes stay on the creature. \"And you brought it back. " +
                    "All right. Step away, Commander.\"",
                "The Conductor claws at the stone, trying to drag itself toward the ruins. Teferi raises " +
                    "his hand. Time races through the creature. Its limbs wither beneath cracking armor. " +
                    "The pulse in its chest falters as the " +
                    "metal ribs crumble inward. Teferi keeps his hand raised until the last of the black " +
                    "tissue and oil has dried to dust. Only then does he let the spell go.",
                "He waits beside the remains, watching for any movement. \"The relays have stopped. " +
                    "The breach is shut.\" He looks back at you. \"New Phyrexia is cut off again. This time, " +
                    "the thing trying to bring it back is gone.\"",
                "Teferi comes over and clasps your hand in both of his. For a moment he has no words. " +
                    "\"Thank you. I asked you to face something I let escape, and you came back with it. " +
                    "You've given people a chance to live without facing another invasion.\" He releases " +
                    "your hand and looks around the Aether. \"You still have a home here, Commander. " +
                    "I think I'd like to spend tomorrow fixing something.\"",
                "Oh, and by the way, this concludes the current state of the game. Our story is not finished though. Rogue Commander is still in development, with more updates and content planned, \" +\n"
                    + "\"including Challenges and a second Trail beyond the final boss. Stay tuned for what comes next. Thank you so much for playing and supporting the game!!\""
            );
        }
    };

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
        if (progress.getNPCLevel(getNpc().id) > NEMESIS.requiredLevel) {
            return false;
        }
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
        };
    }

    private static int getFirstFutureStoryLevel(RogueMetaProgress progress) {
        int highestDescensionWon = progress.getHighestDescensionWon();
        if (highestDescensionWon > 0) {
            return Math.min(NEMESIS.requiredLevel + 1, RELAY.requiredLevel + highestDescensionWon);
        }
        return FIRST_EVIDENCE.requiredLevel + Math.min(progress.getDistinctCommandersWon(), 3);
    }
}
