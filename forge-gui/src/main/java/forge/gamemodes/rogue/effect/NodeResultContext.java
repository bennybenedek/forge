package forge.gamemodes.rogue.effect;

import forge.gamemodes.rogue.RoguePlanebound;
import forge.item.PaperCard;
import java.util.List;

/**
 * Mutable context for node result handling.
 * Set by ONESHOT boons/loots that trigger follow-up actions or card effects.
 */
public class NodeResultContext {
    public enum ActionTriggerType { BAZAAR, PLANEBOUND, CARD_REMOVAL, CARD_REWARD, MYTHIC_CARD_REWARD, CHEST, SANCTUM }

    /** Set by ONESHOT effects that trigger another action (e.g., "opens a bazaar"). Null = no follow-up. */
    public ActionTriggerType trigger;

    /** Set by PLANEBOUND trigger to pass the picked planebound to the UI layer. */
    public RoguePlanebound planebound;

    /** Set by CARD_REMOVAL trigger: how many cards to remove and how many to draw as replacements. */
    public int removeCount;
    public int drawCount;

    /** Cards removed/added by the effect, for result display. Null = not applicable. */
    public List<PaperCard> removedCards;
    public List<PaperCard> addedCards;

    /** Wound gained from this event, for result display. Null = not applicable. */
    public Wound gainedWound;
}
