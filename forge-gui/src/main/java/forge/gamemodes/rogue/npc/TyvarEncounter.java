package forge.gamemodes.rogue.npc;

import java.util.List;

/**
 * Tyvar Kell — Commander Trainer NPC.
 * Each enum constant represents a progression level with its own flavor text and choices.
 */
public enum TyvarEncounter implements NPCEncounter {

    LEVEL_1(1,
            "Tyvar Kell steps from the shadows, his elven features lit by the glow of his Planeswalker spark. " +
            "\"I've watched you fight, and I see potential. Let me train your Commander.\"",
            List.of(
                new NPCContext.NPCChoice("Might", NPCBoon.TYVAR_MIGHT),
                new NPCContext.NPCChoice("Efficiency", NPCBoon.TYVAR_DISCOUNT),
                new NPCContext.NPCChoice("Fury", NPCBoon.TYVAR_HASTE)
            ));

    private final int requiredLevel;
    private final String flavorText;
    private final List<NPCContext.NPCChoice> choices;

    TyvarEncounter(int requiredLevel, String flavorText, List<NPCContext.NPCChoice> choices) {
        this.requiredLevel = requiredLevel;
        this.flavorText = flavorText;
        this.choices = choices;
    }

    @Override
    public NPC getNpc() { return NPC.TYVAR; }

    @Override
    public int getRequiredLevel() { return requiredLevel; }

    @Override
    public NPCContext onRunStart() {
        return buildContext(flavorText, choices);
    }
}
