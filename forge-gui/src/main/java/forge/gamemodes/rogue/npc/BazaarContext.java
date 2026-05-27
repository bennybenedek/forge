package forge.gamemodes.rogue.npc;

import forge.item.PaperCard;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Context for Bazaar-style shopping interactions.
 * Used by both ordinary Bazaar nodes and custom event inventories.
 */
public class BazaarContext {

    /** Optional custom dialog title. Null = default Bazaar title. */
    public String title;

    /** Custom inventory for Bazaar-style shopping. Ordinary Bazaar setup leaves this empty. */
    public List<PaperCard> inventory = new ArrayList<>();

    /** Extra cards to inject into the bazaar inventory (e.g. Gonti's items). */
    public final Set<PaperCard> injectedCards = new HashSet<>();

    /** Cards the player purchased (populated after shopping, before onAfterBazaarPurchase). */
    public final Set<PaperCard> purchasedCards = new HashSet<>();

    /** Card name → fixed price override. Used for both item pricing and card discounts. */
    public Map<String, Integer> priceOverrides = new HashMap<>();

    /** Number of random cards to discount (set by encounters, applied by bazaar code). */
    public int discountCount;

    /** Discount amount in gold per discounted card. */
    public int discountAmount;
}
