package forge.gamemodes.rogue;

/**
 * Mutable context for event choice handling.
 * Set by CONSUME boons that trigger follow-up node types.
 */
public class EventChoiceContext {
    /** Set by CONSUME boons that trigger another node type (e.g., "opens a bazaar"). Null = no follow-up. */
    public String triggerNodeType;
}
