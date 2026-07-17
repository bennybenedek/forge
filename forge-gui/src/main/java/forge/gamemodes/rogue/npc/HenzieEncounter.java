package forge.gamemodes.rogue.npc;

import forge.gamemodes.rogue.RogueEvent;
import forge.gamemodes.rogue.RogueRun;
import forge.gamemodes.rogue.effect.EventEffect;
import forge.gamemodes.rogue.effect.NPCEffect;
import java.util.Collections;
import java.util.List;

/**
 * Henzie "Toolbox" Torre - Capenna contract NPC.
 * Reveals himself after the second accepted Capenna contract, then offers placeholder boons.
 */
public enum HenzieEncounter implements NPCEncounter {

    BEFORE_REVEAL(0) {
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

    REVEAL(1) {
        @Override
        public NPCContext onAfterEventChoice(RogueEvent event, RogueEvent.EventChoice choice,
                                             EventEffect effect, RogueRun run) {
            if (!isAcceptedContract(effect)) {
                return null;
            }
            incrementNpcLevel();
            return buildContext(
                "The next contract does not vanish with the others. A devil in a tailored coat plucks it from the air, " +
                "grins, and gives a shallow bow. \"Two signatures already. Efficient. Name's Henzie. " +
                "You seem like know when a good deal presents itself. I might have a few tools for your next run.\"",
                List.of()
            );
        }
    },

    OFFERING_BOONS(2) {
        @Override
        public NPCContext onRunStart(RogueRun run) {
            List<NPCEffect> pool = NPCEffect.getEffectsForNpc(getNpc(), run);
            if (pool.isEmpty()) {
                return null;
            }
            Collections.shuffle(pool);
            int choiceCount = Math.min(3, pool.size());
            return buildContext(
                "Henzie leans back with a contract tucked into his sleeve and a case of stolen tools at his feet. " +
                "\"You made me richer. I like people who do that. Take one and try not to waste it.\"",
                pool.subList(0, choiceCount).stream()
                    .map(effect -> new NPCContext.NPCChoice(effect.getDisplayName(), effect))
                    .toList()
            );
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

    private static boolean isAcceptedContract(EventEffect effect) {
        return effect == EventEffect.STREET_OF_CONCEALMENT_ACCEPT
            || effect == EventEffect.STREET_OF_GREED_ACCEPT
            || effect == EventEffect.STREET_OF_FORCEFULNESS_ACCEPT;
    }
}
