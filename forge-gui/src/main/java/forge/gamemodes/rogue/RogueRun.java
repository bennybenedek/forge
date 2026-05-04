package forge.gamemodes.rogue;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import forge.deck.Deck;
import forge.gamemodes.match.HostedMatch;
import forge.gamemodes.rogue.effect.ChestLoot;
import forge.gamemodes.rogue.effect.Cursed;
import forge.gamemodes.rogue.effect.EchoBoon;
import forge.gamemodes.rogue.effect.EventBoon;
import forge.gamemodes.rogue.effect.RogueEffect;
import forge.gamemodes.rogue.effect.Wound;
import forge.gamemodes.rogue.effect.Wrathful;
import forge.gamemodes.rogue.effect.NPCBoon;
import forge.gamemodes.rogue.path.RoguePath;
import forge.gamemodes.rogue.path.RoguePathNode;
import forge.item.PaperCard;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * Main container for a Rogue Commander run state.
 * Tracks deck evolution, life persistence, path progress, and match history.
 */
public class RogueRun {

    @XStreamOmitField
    private String name;  // Set based on filename on load

    // Run Configuration
    private RogueDeck selectedRogueDeck;        // Selected Rogue Deck identifier
    private String timestamp;                   // Creation timestamp

    // Run State
    private Deck currentDeck;                   // Player's evolving deck (starts as Start Deck copy)
    private int currentLife;                    // Persistent life total (starts at 20)
    private int startingLife;                   // Initial life (default: 20)
    private int currentGold;                    // Currency (for future Bazaar support)
    private RoguePath path;                     // The generated path
    private int currentNodeIndex;               // Current position on path
    private RogueRunState runState;             // Current state of the run

    // Descension
    private int descensionLevel;                // 0 = no descension; XStream defaults int to 0 for old saves

    // Echo boons (snapshotted from RogueMetaProgress at run creation)
    private List<RogueRunBoon> activeEchoBoons;

    // Event boons (gained from event nodes during the run)
    private List<RogueRunBoon> activeEventBoons;

    // Chest boons (gained from chest nodes during the run)
    private List<RogueRunBoon> activeChestBoons;

    // Wounds (permanent debuffs gained from events, descension, etc.)
    private List<RogueRunBoon> activeWounds;

    // Wrathful effects (consume debuffs from wrathful planebounds)
    private List<RogueRunBoon> activeWrathful;

    // Cursed effects (consume debuffs from cursed planebounds)
    private List<RogueRunBoon> activeCursed;

    // NPC boons (gained from NPC encounters at run start)
    private List<RogueRunBoon> activeNPCBoons;

    // Match History
    private int matchesWon;                     // Win counter
    private int matchesLost;                    // Loss counter
    private int removalCredits;                 // Credits for removing cards from deck (from rewards and Sanctum)

    /** Transient snapshot of data from the most recent match. */
    public record LastMatchData(int chaosCount, int planeswalkCount) {
        public static final LastMatchData EMPTY = new LastMatchData(0, 0);
    }

    /** Type of card carried between matches in the command zone. */
    public enum CarryCardType { ITEM, COMPANION }

    /** A card the player carries between matches (castable from command zone).
     *  sourceId links to the boon that granted it (null if purchased/rewarded). */
    public static class CarryCard {
        private String cardName;
        private CarryCardType type;
        private String sourceId;

        public CarryCard() {} // XStream

        public CarryCard(String cardName, CarryCardType type, String sourceId) {
            this.cardName = cardName;
            this.type = type;
            this.sourceId = sourceId;
        }

        public String cardName() { return cardName; }
        public CarryCardType type() { return type; }
        public String sourceId() { return sourceId; }
    }

    // Carry cards (items/companions that persist across matches in the command zone)
    private List<CarryCard> carryCards;

    // Transient (runtime only, not serialized)
    @XStreamOmitField
    private transient HostedMatch hostedMatch = null;
    @XStreamOmitField
    private transient LastMatchData lastMatchData = LastMatchData.EMPTY;
    @XStreamOmitField
    private transient Integer lastMatchRawLife = null;

    // Constructors
    public RogueRun() {
        this.setStartingLife(20);
        this.setCurrentGold(0);
        this.setCurrentNodeIndex(0);
        this.setRemovalCredits(0);
        this.setRunState(RogueRunState.STARTED);
        this.matchesWon = 0;
        this.matchesLost = 0;
        stamp();
    }

    public RogueRun(RogueDeck selectedRogueDeck) {
        this();
        this.selectedRogueDeck = selectedRogueDeck;
        // Create deep copy of start deck
        this.currentDeck = new Deck(selectedRogueDeck.getStartDeck());
    }

    // Timestamp management
    public void stamp() {
        final DateFormat dateFormat = new SimpleDateFormat("MM-dd-yy, H:m");
        timestamp = dateFormat.format(new Date());
    }

    // Path navigation
    public void nextNode() {
        if (currentNodeIndex >= path.getNodeCount() - 1) {
            return; // Already at last node
        }

        RoguePathNode currentNode = path.getNode(currentNodeIndex);
        if (currentNode == null) {
            currentNodeIndex++;
            return;
        }

        // Get reachable nodes in next row from current completed node
        List<Integer> reachableIndices = path.getReachableNodeIndices(currentNodeIndex);

        if (!reachableIndices.isEmpty()) {
            // Set to first reachable node in next row
            currentNodeIndex = reachableIndices.get(0);
        } else {
            // No reachable nodes (end of path or error), just increment
            currentNodeIndex++;
        }
    }

    public RoguePathNode getCurrentNode() {
        return path.getNode(currentNodeIndex);
    }

    public boolean isRunComplete() {
        return currentNodeIndex >= path.getNodeCount() - 1 &&
               (getCurrentNode() == null || getCurrentNode().isCompleted());
    }

    public boolean isRunFailed() {
        return runState == RogueRunState.LOST || currentLife <= 0;
    }

    public void setRunFailed(boolean failed) {
        if (failed) {
            this.runState = RogueRunState.LOST;
        }
    }

    public boolean isRunWon() {
        return runState == RogueRunState.WON;
    }

    public void setRunWon(boolean won) {
        if (won) {
            this.runState = RogueRunState.WON;
        }
    }

    public RogueRunState getRunState() {
        return runState;
    }

    public void setRunState(RogueRunState state) {
        this.runState = state;
    }

    /**
     * Called after deserialization to ensure state is initialized.
     * Handles backward compatibility with old save files.
     * Is used by framework even though looks unused.
     */
    private Object readResolve() {
        // Initialize state if it's null (old save files)
        if (runState == null) {
            if (currentLife <= 0) {
                runState = RogueRunState.LOST;
            } else if (isRunComplete()) {
                runState = RogueRunState.WON;
            } else {
                runState = RogueRunState.STARTED;
            }
        }
        clampCurrentLifeToMax();
        return this;
    }

    // Match result tracking
    public void recordMatchResult(boolean won) {
        if (won) {
            matchesWon++;
            // Mark current node as completed
            if (getCurrentNode() != null) {
                getCurrentNode().setCompleted(true);
            }
        } else {
            matchesLost++;
        }
    }

    // Deck management
    public void addCardsToRun(List<PaperCard> cards) {
        if (currentDeck != null && cards != null) {
            for (PaperCard card : cards) {
                currentDeck.getMain().add(card);
            }
            removalCredits += cards.size();
        }
    }

    /**
     * Add removal credits without adding cards to deck.
     * Used by Sanctum and other sources of free card removals.
     */
    public void addRemovalCredits(int count) {
        removalCredits += count;
    }

    // Carry card management (items/companions in command zone)
    public void addCarryCard(String cardName, CarryCardType type, String sourceId) {
        if (carryCards == null) carryCards = new ArrayList<>();
        carryCards.add(new CarryCard(cardName, type, sourceId));
    }

    public void removeCarryCard(String cardName) {
        if (carryCards == null) return;
        carryCards.removeIf(c -> c.cardName().equals(cardName));
    }

    public void removeCarryCardsBySource(String sourceId) {
        if (carryCards == null || sourceId == null) return;
        carryCards.removeIf(c -> sourceId.equals(c.sourceId()));
    }

    public List<CarryCard> getCarryCards() {
        return carryCards != null ? carryCards : List.of();
    }

    public boolean hasCarryCardOfType(CarryCardType type) {
        return getCarryCards().stream().anyMatch(c -> c.type() == type);
    }

    // Life management
    public void gainLifeUpToMax(int amount) {
        currentLife = Math.min(currentLife + amount, startingLife);
    }

    public void clampCurrentLifeToMax() {
        if (startingLife > 0) {
            currentLife = Math.min(currentLife, startingLife);
        }
    }

    public void persistMatchLife(int life) {
        lastMatchRawLife = life;
        currentLife = life;
        clampCurrentLifeToMax();
    }

    public int getLastMatchRawLife() {
        return lastMatchRawLife != null ? lastMatchRawLife : currentLife;
    }

    // Match hosting (transient)
    public void setHostedMatch(HostedMatch match) {
        this.hostedMatch = match;
    }

    public HostedMatch getHostedMatch() {
        return hostedMatch;
    }

    public LastMatchData getLastMatchData() {
        return lastMatchData != null ? lastMatchData : LastMatchData.EMPTY;
    }

    public void setLastMatchData(LastMatchData data) {
        this.lastMatchData = data != null ? data : LastMatchData.EMPTY;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        if (name != null && name.startsWith(RogueIO.PREFIX_LOCKED)) {
            return name.substring(RogueIO.PREFIX_LOCKED.length());
        }
        return name;
    }

    public RogueDeck getSelectedRogueDeck() {
        return selectedRogueDeck;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Deck getCurrentDeck() {
        return currentDeck;
    }

    public void setCurrentDeck(Deck currentDeck) {
        this.currentDeck = currentDeck;
    }

    public int getCurrentLife() {
        return currentLife;
    }

    public void setCurrentLife(int currentLife) {
        this.currentLife = currentLife;
    }

    public int getStartingLife() {
        return startingLife;
    }

    public void setStartingLife(int startingLife) {
        this.startingLife = startingLife;
        this.setCurrentLife(this.startingLife);
    }

    public int getCurrentGold() {
        return currentGold;
    }

    public void setCurrentGold(int currentGold) {
        this.currentGold = currentGold;
    }

    public RoguePath getPath() {
        return path;
    }

    public void setPath(RoguePath path) {
        this.path = path;
    }

    public int getCurrentNodeIndex() {
        return currentNodeIndex;
    }

    public void setCurrentNodeIndex(int currentNodeIndex) {
        this.currentNodeIndex = currentNodeIndex;
    }

    public int getRemovalCredits() {
        return removalCredits;
    }

    public void setRemovalCredits(int removalCredits) {
        this.removalCredits = removalCredits;
    }

    public int getDescensionLevel() {
        return descensionLevel;
    }

    public void setDescensionLevel(int level) {
        this.descensionLevel = level;
    }

    // Echo boon management
    public void snapshotEchoBoons(RogueMetaProgress progress) {
        activeEchoBoons = new ArrayList<>();
        for (EchoBoon boon : progress.getActiveEchoBoons()) {
            int rank = progress.getBoonRank(boon);
            int charges = boon.getChargesForRank(rank);
            activeEchoBoons.add(new RogueRunBoon(boon.getId(), rank, charges));
        }
    }

    // Generic effect list helpers
    private List<RogueRunBoon> ensureList(List<RogueRunBoon> list) {
        return list != null ? list : new ArrayList<>();
    }

    private List<RogueEffect> mapEffects(List<RogueRunBoon> list, Function<String, ? extends RogueEffect> lookup) {
        List<RogueEffect> result = new ArrayList<>();
        for (RogueRunBoon rb : ensureList(list)) {
            RogueEffect e = lookup.apply(rb.getId());
            if (e != null) result.add(e);
        }
        return result;
    }

    public List<RogueEffect> getActiveEchoBoons()  { return mapEffects(activeEchoBoons, EchoBoon::fromId); }
    public List<RogueEffect> getActiveEventBoons() { return mapEffects(activeEventBoons, EventBoon::fromId); }
    public List<RogueEffect> getActiveChestBoons() { return mapEffects(activeChestBoons, ChestLoot::fromId); }
    public List<RogueEffect> getActiveWounds()     { return mapEffects(activeWounds, Wound::fromId); }
    public List<RogueEffect> getActiveWrathful()   { return mapEffects(activeWrathful, Wrathful::fromId); }
    public List<RogueEffect> getActiveCursed()    { return mapEffects(activeCursed, Cursed::fromId); }
    public List<RogueEffect> getActiveNPCBoons() { return mapEffects(activeNPCBoons, NPCBoon::fromId); }

    public void addEventBoon(EventBoon boon)   { activeEventBoons = addEffect(activeEventBoons, boon); }
    public void addChestBoon(ChestLoot loot)    { activeChestBoons = addEffect(activeChestBoons, loot); }
    public void addWound(Wound wound)           { activeWounds = addEffect(activeWounds, wound); }
    public void addWrathful(Wrathful wrathful)  { activeWrathful = addEffect(activeWrathful, wrathful); }
    public void addCursed(Cursed cursed)        { activeCursed = addEffect(activeCursed, cursed); }
    public void addNPCBoon(NPCBoon boon)       { activeNPCBoons = addEffect(activeNPCBoons, boon); boon.onGranted(this); }

    private List<RogueRunBoon> addEffect(List<RogueRunBoon> list, RogueEffect effect) {
        if (list == null) list = new ArrayList<>();
        list.add(new RogueRunBoon(effect.getId(), 0, effect.getChargesForRank(0)));
        return list;
    }

    public void clearWounds() {
        if (activeWounds != null) activeWounds.clear();
    }

    // Boon queries
    public int getRunBoonRank(String id) {
        RogueRunBoon rb = findRunBoon(id);
        return rb != null ? rb.getRank() : 0;
    }

    @SuppressWarnings("unchecked")
    private List<RogueRunBoon>[] allBoonLists() {
        return new List[]{activeEchoBoons, activeEventBoons, activeChestBoons,
                activeWounds, activeWrathful, activeCursed, activeNPCBoons};
    }

    // Effect consumption
    public void consumeEffect(String id) {
        for (List<RogueRunBoon> list : allBoonLists())
            consumeFromList(list, id);
    }

    private void consumeFromList(List<RogueRunBoon> list, String id) {
        if (list == null) return;
        Iterator<RogueRunBoon> it = list.iterator();
        while (it.hasNext()) {
            RogueRunBoon rb = it.next();
            if (rb.getId().equals(id) && rb.consumeCharge()) {
                it.remove();
                removeCarryCardsBySource(id);
                return;
            }
        }
    }

    private RogueRunBoon findRunBoon(String id) {
        for (List<RogueRunBoon> list : allBoonLists()) {
            if (list == null) continue;
            for (RogueRunBoon rb : list)
                if (rb.getId().equals(id)) return rb;
        }
        return null;
    }

    @Override
    public String toString() {
        return getDisplayName() + " (" + matchesWon + "-" + matchesLost + ", " + currentLife + " life)";
    }
}
