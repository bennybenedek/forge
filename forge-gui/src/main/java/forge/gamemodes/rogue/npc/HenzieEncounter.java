package forge.gamemodes.rogue.npc;

import forge.gamemodes.rogue.RogueEvent;
import forge.gamemodes.rogue.RogueRun;
import forge.gamemodes.rogue.effect.EventEffect;
import java.util.List;

/**
 * Henzie "Toolbox" Torre - Capenna contract NPC.
 * Reveals himself after the first accepted Capenna contract, then offers boons.
 */
public enum HenzieEncounter implements NPCEncounter {

    BEFORE_REVEAL(-1) {
        @Override
        public NPCContext onAfterEventChoice(RogueEvent event, RogueEvent.EventChoice choice,
                                             EventEffect effect, RogueRun run) {
            if (!isAcceptedContract(effect)) {
                return null;
            }
            incrementNpcLevel();
            return null;
        }
    },

    REVEAL(0) {
        @Override
        public NPCContext onAfterEventChoice(RogueEvent event, RogueEvent.EventChoice choice,
                                             EventEffect effect, RogueRun run) {
            if (!isAcceptedContract(effect)) {
                return null;
            }
            incrementNpcLevel();
            return buildContext(
                List.of(
                    "The next contract does not vanish with the others. A devil in a tailored coat plucks it from the air, " +
                        "grins, and gives a shallow bow.",
                    "\"Efficient. Name's Henzie. " +
                        "You seem like know when a good deal presents itself. I might have a few tools for your next run.\""
                ),
                List.of()
            );
        }
    },

    OFFERING_BOONS(1) {
        @Override
        public NPCContext onRunStart(RogueRun run) {
            return buildOfferingBoonsContext(run);
        }
    };

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

    private static boolean isAcceptedContract(EventEffect effect) {
        return effect == EventEffect.STREET_OF_CONCEALMENT_ACCEPT
            || effect == EventEffect.STREET_OF_GREED_ACCEPT
            || effect == EventEffect.STREET_OF_FORCEFULNESS_ACCEPT;
    }
}
