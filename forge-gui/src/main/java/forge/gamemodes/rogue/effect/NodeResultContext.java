package forge.gamemodes.rogue.effect;

import forge.deck.DeckSection;
import forge.gamemodes.rogue.RoguePlanebound;
import forge.gamemodes.rogue.npc.BazaarContext;
import forge.item.PaperCard;
import java.util.ArrayList;
import java.util.List;

/**
 * Mutable context for node result handling.
 * Set by ONESHOT boons/loots that trigger follow-up actions or card effects.
 */
public class NodeResultContext {
    public enum ActionTriggerType { BAZAAR, PLANEBOUND, CARD_REMOVAL, CARD_ADDITION, CARD_REWARD, MYTHIC_CARD_REWARD, CHEST, SANCTUM }

    /** Set by ONESHOT effects that trigger another action (e.g., "opens a bazaar"). Null = no follow-up. */
    public ActionTriggerType trigger;

    /** Set by PLANEBOUND trigger to pass the picked planebound to the UI layer. */
    public RoguePlanebound planebound;

    /** Set by card-selection triggers: which cards to show in CardSelectionDialog. */
    public List<PaperCard> candidateCards;

    /** Set by BAZAAR trigger: custom Bazaar-style shopping configuration. Null = ordinary Bazaar setup. */
    public BazaarContext bazaarContext;

    /** Set by card-selection triggers: how many cards to remove, add, or draw as replacements. */
    public int removeCount;
    public int addMinCount;
    public int addMaxCount;
    public int drawCount;
    public DeckSection addSection = DeckSection.Main;
    public boolean replaceCurrentCardsInAddSection;

    /** Cards removed/added by the effect, for result display. Null = not applicable. */
    public List<PaperCard> removedCards;
    public List<PaperCard> addedCards;

    /** Wound gained from this event, for result display. Null = not applicable. */
    public Wound gainedWound;

    public NodeResultContext() {
        removedCards = new ArrayList<>();
        addedCards = new ArrayList<>();
    }
}
