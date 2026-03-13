package forge.gamemodes.rogue;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import forge.deck.Deck;
import forge.gamemodes.match.HostedMatch;
import forge.gamemodes.rogue.effect.ChestLoot;
import forge.gamemodes.rogue.effect.EchoBoon;
import forge.gamemodes.rogue.effect.EventBoon;
import forge.gamemodes.rogue.effect.RogueEffect;
import forge.gamemodes.rogue.effect.Wound;
import forge.gamemodes.rogue.path.RoguePath;
import forge.gamemodes.rogue.path.RoguePathNode;
import forge.item.PaperCard;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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

    // Match History
    private int matchesWon;                     // Win counter
    private int matchesLost;                    // Loss counter
    private int removalCredits;                 // Credits for removing cards from deck (from rewards and Sanctum)

    // Transient (runtime only, not serialized)
    @XStreamOmitField
    private transient HostedMatch hostedMatch = null;

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

    // Life management
    public void healLife(int amount) {
        currentLife = Math.min(currentLife + amount, startingLife);
    }

    public void gainLife(int amount) {
        currentLife = currentLife + amount;
    }

    // Match hosting (transient)
    public void setHostedMatch(HostedMatch match) {
        this.hostedMatch = match;
    }

    public HostedMatch getHostedMatch() {
        return hostedMatch;
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

    /**
     * Get indices of nodes reachable from the current node.
     * @return List of reachable node indices
     */
    public List<Integer> getReachableNodeIndices() {
        if (path == null) {
            return new java.util.ArrayList<>();
        }
        return path.getReachableNodeIndices(currentNodeIndex);
    }

    public int getMatchesWon() {
        return matchesWon;
    }

    public int getMatchesLost() {
        return matchesLost;
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

    public List<RogueEffect> getActiveEchoBoons() {
        if (activeEchoBoons == null) activeEchoBoons = new ArrayList<>();
        List<RogueEffect> result = new ArrayList<>();
        for (RogueRunBoon rb : activeEchoBoons) {
            EchoBoon eb = EchoBoon.fromId(rb.getId());
            if (eb != null) result.add(eb);
        }
        return result;
    }

    // Event boon management
    public List<RogueEffect> getActiveEventBoons() {
        if (activeEventBoons == null) activeEventBoons = new ArrayList<>();
        List<RogueEffect> result = new ArrayList<>();
        for (RogueRunBoon rb : activeEventBoons) {
            EventBoon eb = EventBoon.fromId(rb.getId());
            if (eb != null) result.add(eb);
        }
        return result;
    }

    public void addEventBoon(EventBoon boon) {
        if (activeEventBoons == null) activeEventBoons = new ArrayList<>();
        int charges = boon.getChargesForRank(0);
        activeEventBoons.add(new RogueRunBoon(boon.getId(), 0, charges));
    }

    // Chest boon management
    public List<RogueEffect> getActiveChestBoons() {
        if (activeChestBoons == null) activeChestBoons = new ArrayList<>();
        List<RogueEffect> result = new ArrayList<>();
        for (RogueRunBoon rb : activeChestBoons) {
            ChestLoot cl = ChestLoot.fromId(rb.getId());
            if (cl != null) result.add(cl);
        }
        return result;
    }

    public void addChestBoon(ChestLoot loot) {
        if (activeChestBoons == null) activeChestBoons = new ArrayList<>();
        int charges = loot.getChargesForRank(0);
        activeChestBoons.add(new RogueRunBoon(loot.getId(), 0, charges));
    }

    // Wound management
    public List<RogueEffect> getActiveWounds() {
        if (activeWounds == null) activeWounds = new ArrayList<>();
        List<RogueEffect> result = new ArrayList<>();
        for (RogueRunBoon rb : activeWounds) {
            Wound w = Wound.fromId(rb.getId());
            if (w != null) result.add(w);
        }
        return result;
    }

    public void addWound(Wound wound) {
        if (activeWounds == null) activeWounds = new ArrayList<>();
        activeWounds.add(new RogueRunBoon(wound.getId(), 0, -1));
    }

    public void clearWounds() {
        if (activeWounds != null) activeWounds.clear();
    }

    // Boon queries
    public int getRunBoonRank(String id) {
        RogueRunBoon rb = findRunBoon(id);
        return rb != null ? rb.getRank() : 0;
    }

    // Effect consumption
    public void consumeEffect(String id) {
        consumeFromList(activeEchoBoons, id);
        consumeFromList(activeEventBoons, id);
        consumeFromList(activeChestBoons, id);
        consumeFromList(activeWounds, id);
    }

    private void consumeFromList(List<RogueRunBoon> list, String id) {
        if (list == null) return;
        Iterator<RogueRunBoon> it = list.iterator();
        while (it.hasNext()) {
            RogueRunBoon rb = it.next();
            if (rb.getId().equals(id) && rb.consumeCharge()) {
                it.remove();
                return;
            }
        }
    }

    private RogueRunBoon findRunBoon(String id) {
        if (activeEchoBoons != null)
            for (RogueRunBoon rb : activeEchoBoons)
                if (rb.getId().equals(id)) return rb;
        if (activeEventBoons != null)
            for (RogueRunBoon rb : activeEventBoons)
                if (rb.getId().equals(id)) return rb;
        if (activeChestBoons != null)
            for (RogueRunBoon rb : activeChestBoons)
                if (rb.getId().equals(id)) return rb;
        if (activeWounds != null)
            for (RogueRunBoon rb : activeWounds)
                if (rb.getId().equals(id)) return rb;
        return null;
    }

    @Override
    public String toString() {
        return getDisplayName() + " (" + matchesWon + "-" + matchesLost + ", " + currentLife + " life)";
    }
}
