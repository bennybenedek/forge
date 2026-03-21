package forge.gamemodes.rogue.npc;

import forge.item.PaperCard;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Context passed to NPC encounters before and after bazaar shopping.
 * Encounters modify this to inject items, override prices, and react to purchases.
 */
public class BazaarContext {

    /** Card name → fixed price override. Used for both item pricing and card discounts. */
    public final Map<String, Integer> priceOverrides = new HashMap<>();

    /** Extra cards to inject into the bazaar inventory (e.g. Gonti's items). */
    public final Set<PaperCard> injectedCards = new HashSet<>();

    /** Cards the player purchased (populated after shopping, before onAfterBazaarPurchase). */
    public final Set<PaperCard> purchasedCards = new HashSet<>();

    /** Number of random cards to discount (set by encounters, applied by bazaar code). */
    public int discountCount;

    /** Discount amount in gold per discounted card. */
    public int discountAmount;
}
