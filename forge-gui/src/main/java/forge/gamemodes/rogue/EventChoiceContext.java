package forge.gamemodes.rogue;

import forge.item.PaperCard;
import java.util.List;

/**
 * Mutable context for event choice handling.
 * Set by ONESHOT boons that trigger follow-up node types or card effects.
 */
public class EventChoiceContext {
    public enum NodeTriggerType { BAZAAR, PLANEBOUND }

    /** Set by ONESHOT boons that trigger another node type (e.g., "opens a bazaar"). Null = no follow-up. */
    public NodeTriggerType trigger;

    /** Set by PLANEBOUND trigger to pass the picked planebound to the UI layer. */
    public RoguePlanebound planebound;

    /** Cards removed/added by the effect, for result display. Null = not applicable. */
    public List<PaperCard> removedCards;
    public List<PaperCard> addedCards;
}
