package forge.gamemodes.rogue.npc;

import forge.gamemodes.rogue.RogueEvent;
import forge.gamemodes.rogue.RogueMetaProgress;
import forge.gamemodes.rogue.RogueRun;
import forge.gamemodes.rogue.effect.EventEffect;
import forge.gamemodes.rogue.path.NodeEvent;
import forge.gamemodes.rogue.path.RoguePathNode;
import forge.localinstance.achievements.RogueCommanderAchievements;
import forge.util.MyRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Henzie "Toolbox" Torre - Capenna contract NPC.
 * Presents a contract every third Event until accepting one unlocks his boons.
 */
public enum HenzieEncounter implements NPCEncounter {

    BEFORE_REVEAL(0),

    REVEAL(333),

    OFFERING_BOONS(666) {
        @Override
        public NPCContext onRunStart(RogueRun run) {
            return buildOfferingBoonsContext(run);
        }
    };

    private static final List<RogueEvent> CONTRACT_EVENTS = List.of(
        RogueEvent.STREET_OF_CONCEALMENT,
        RogueEvent.STREET_OF_GREED,
        RogueEvent.STREET_OF_FORCEFULNESS
    );

    private final int requiredLevel;

    HenzieEncounter(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    @Override
    public NPC getNpc() {
        return NPC.HENZIE;
    }

    @Override
    public int getRequiredLevel() {
        return requiredLevel;
    }

    @Override
    public RogueEvent onBeforeEvent(RogueEvent event, RogueRun run) {
        int level = RogueMetaProgress.getInstance().getNPCLevel(getNpc().id);
        if (level >= OFFERING_BOONS.requiredLevel || (level + 1) % 3 != 0) {
            return event;
        }

        List<RogueEvent> availableContracts = new ArrayList<>(CONTRACT_EVENTS);
        for (RoguePathNode node : run.getPath().getNodes()) {
            if (node instanceof NodeEvent eventNode) {
                availableContracts.remove(eventNode.getEvent());
            }
        }
        List<RogueEvent> contractPool = availableContracts.isEmpty() ? CONTRACT_EVENTS : availableContracts;
        return contractPool.get(MyRandom.getRandom().nextInt(contractPool.size()));
    }

    @Override
    public NPCContext onAfterEventChoice(RogueEvent event, RogueEvent.EventChoice choice,
                                         EventEffect effect, RogueRun run) {
        RogueMetaProgress progress = RogueMetaProgress.getInstance();
        int level = progress.getNPCLevel(getNpc().id);
        if (level >= OFFERING_BOONS.requiredLevel) {
            return null;
        }

        if (isAcceptedContract(effect)) {
            progress.setNPCLevel(getNpc().id, OFFERING_BOONS.requiredLevel);
            RogueCommanderAchievements.instance.evaluateNpcBoonUnlockAchievements(progress);
            return buildAcceptedContractContext();
        }

        if (level < REVEAL.requiredLevel && CONTRACT_EVENTS.contains(event)) {
            progress.setNPCLevel(getNpc().id, REVEAL.requiredLevel);
            return buildDeclinedContractContext();
        }

        incrementNpcLevel();
        if (level + 1 == OFFERING_BOONS.requiredLevel) {
            return buildAutomaticUnlockContext();
        }
        return null;
    }

    @Override
    public List<String> getOfferingBoonMonologues() {
        return List.of(
            "Henzie leans back with a contract tucked into his sleeve and a case of stolen tools at his feet. " +
                "\"You made me richer. I like people who do that. Take one and try not to waste it.\"",
            "\"Good clients get good options,\" Henzie says, tapping a claw against a sealed case. " +
                "\"Bad clients get invoices. Lucky for you, today you're the first kind.\"",
            "Henzie fans out a few suspiciously clean contracts. \"No fine print this time. Well, less fine print. " +
                "Pick something useful before I reconsider the price.\""
        );
    }

    private NPCContext buildAcceptedContractContext() {
        return buildContext(
            List.of(
                "The signed contract does not vanish with the others. A devil in a tailored coat plucks it from the air, " +
                    "grins, and gives a shallow bow.",
                "\"Efficient. Name's Henzie. You seem to know when a good deal presents itself. " +
                    "I might have a few tools for your next run.\""
            ),
            List.of()
        );
    }

    private NPCContext buildDeclinedContractContext() {
        return buildContext(
            "???", getNpc().avatarIndex,
            List.of(
                "The unsigned contract begins to fade, but the devil catches it between two claws and studies you " +
                    "with theatrical disappointment.",
                "\"Bad choice. Would've guessed you Commanders were a little smarter than that. All right, I guess " +
                    "no meta-progression for this one. No unlocked devil at the start " +
                    "of each Run offering crazy starting boons until a contract gets signed. You do you, pal.\""
            ),
            List.of()
        );
    }

    private NPCContext buildAutomaticUnlockContext() {
        return buildContext(
            List.of(
                "Henzie appears with a stack of unsigned contracts tucked beneath one arm and an expression caught " +
                    "between disbelief and professional admiration.",
                "\"You really dragged this out all the way to six-six-six without signing a thing. That's almost " +
                    "impressive. Fine. You win: unlocked devil, crazy starting boons at the start of each Run. " +
                    "Apparently stubbornness counts as a contract now.\""
            ),
            List.of()
        );
    }

    private static boolean isAcceptedContract(EventEffect effect) {
        return effect == EventEffect.STREET_OF_CONCEALMENT_ACCEPT
            || effect == EventEffect.STREET_OF_GREED_ACCEPT
            || effect == EventEffect.STREET_OF_FORCEFULNESS_ACCEPT;
    }
}
