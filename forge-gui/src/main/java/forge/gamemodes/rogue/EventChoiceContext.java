package forge.gamemodes.rogue;

/**
 * Mutable context for event choice handling.
 * Set by ONESHOT boons that trigger follow-up node types.
 */
public class EventChoiceContext {
    public enum NodeTriggerType { BAZAAR }

    /** Set by ONESHOT boons that trigger another node type (e.g., "opens a bazaar"). Null = no follow-up. */
    public NodeTriggerType trigger;
}
