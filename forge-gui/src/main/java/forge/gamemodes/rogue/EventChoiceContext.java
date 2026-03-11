package forge.gamemodes.rogue;

import forge.item.PaperCard;
import java.util.List;

/**
 * Mutable context for event choice handling.
 * Set by ONESHOT boons that trigger follow-up node types or card effects.
 */
public class EventChoiceContext {
    public enum NodeTriggerType { BAZAAR, PLANEBOUND, CARD_REMOVAL }

    /** Set by ONESHOT boons that trigger another node type (e.g., "opens a bazaar"). Null = no follow-up. */
    public NodeTriggerType trigger;

    /** Set by PLANEBOUND trigger to pass the picked planebound to the UI layer. */
    public RoguePlanebound planebound;

    /** Set by CARD_REMOVAL trigger: how many cards to remove and how many to draw as replacements. */
    public int removeCount;
    public int drawCount;

    /** Cards removed/added by the effect, for result display. Null = not applicable. */
    public List<PaperCard> removedCards;
    public List<PaperCard> addedCards;
}
