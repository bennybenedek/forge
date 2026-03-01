package forge.gamemodes.rogue;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;
import forge.util.IgnoringXStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Tracks persistent meta-progression for Rogue Commander mode.
 * This data persists across all runs and tracks unlocks, achievements, and statistics.
 */
public class RogueMetaProgress {

    private static final String META_PROGRESS_FILE = "meta_progress.dat";
    private static RogueMetaProgress instance;

    // Basic counters
    private int totalRunsStarted;
    private int totalRunsCompleted;  // Finished (win or lose)
    private int totalRunsWon;
    private int totalMatchesWon;
    private int totalMatchesLost;

    // Per-commander tracking
    private Map<String, Integer> runsStartedPerCommander;
    private Map<String, Integer> runsWonPerCommander;
    private Set<String> commandersUsed;

    // Milestone tracking (captured during/after runs)
    private int maxCreatureTypesInDeck;
    private int maxLegendaryPermanentsInDeck;
    private int maxLifeInRun;
    private int maxGoldInRun;

    // Explicit unlocks (for manually unlocked commanders)
    private Set<String> unlockedCommanders;

    // Aether system - persistent echoes and boons
    private int totalEchoes;                      // Persistent echo currency
    private Map<String, Integer> boonRanks;       // Boon ID -> current rank (0 = not unlocked)
    private Set<String> activeBoons;              // Currently equipped boon IDs (max 3)

    // Unlock notification tracking - which unlocks have been shown to the player
    private Set<String> notifiedUnlocks;

    // Tutorial tracking - which tutorials have been shown
    private Set<String> seenTutorials;

    // Run history
    private List<RogueRunHistoryEntry> runHistory;

    // Private constructor for singleton
    private RogueMetaProgress() {
        totalRunsStarted = 0;
        totalRunsCompleted = 0;
        totalRunsWon = 0;
        totalMatchesWon = 0;
        totalMatchesLost = 0;
        runsStartedPerCommander = new HashMap<>();
        runsWonPerCommander = new HashMap<>();
        commandersUsed = new HashSet<>();
        maxCreatureTypesInDeck = 0;
        maxLegendaryPermanentsInDeck = 0;
        maxLifeInRun = 0;
        maxGoldInRun = 0;
        unlockedCommanders = new HashSet<>();

        // Initialize Aether system
        totalEchoes = 0;
        boonRanks = new HashMap<>();
        activeBoons = new HashSet<>();

        // Initialize unlock notification tracking
        notifiedUnlocks = new HashSet<>();

        // Initialize tutorial tracking
        seenTutorials = new HashSet<>();
    }

    /**
     * Get the singleton instance, loading from disk if needed.
     */
    public static synchronized RogueMetaProgress getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    /**
     * Force reload from disk (useful after external changes).
     */
    public static synchronized void reload() {
        instance = load();
    }

    /**
     * Reset all statistics to initial values and save.
     */
    public void reset() {
        totalRunsStarted = 0;
        totalRunsCompleted = 0;
        totalRunsWon = 0;
        totalMatchesWon = 0;
        totalMatchesLost = 0;
        runsStartedPerCommander = new HashMap<>();
        runsWonPerCommander = new HashMap<>();
        commandersUsed = new HashSet<>();
        maxCreatureTypesInDeck = 0;
        maxLegendaryPermanentsInDeck = 0;
        maxLifeInRun = 0;
        maxGoldInRun = 0;
        unlockedCommanders = new HashSet<>();

        // Reset Aether system
        totalEchoes = 0;
        boonRanks = new HashMap<>();
        activeBoons = new HashSet<>();
        notifiedUnlocks = new HashSet<>();

        // Reset run history
        runHistory = new ArrayList<>();

        // Note: Tutorials are NOT reset here - use resetTutorials() separately
        save();
    }

    // ==================== Progress Tracking Methods ====================

    /**
     * Called when a new run is started.
     */
    public void onRunStarted(String commanderName) {
        totalRunsStarted++;
        commandersUsed.add(commanderName);
        runsStartedPerCommander.merge(commanderName, 1, Integer::sum);
        save();
    }

    /**
     * Called after each match in a run.
     */
    public void onMatchCompleted(RogueRun run, boolean won) {
        if (won) {
            totalMatchesWon++;
        } else {
            totalMatchesLost++;
        }

        // Track milestones during the run
        updateMilestones(run);
        save();
    }

    /**
     * Called when a run is completed (won or lost).
     */
    public void onRunCompleted(RogueRun run, boolean won) {
        totalRunsCompleted++;

        String commanderName = run.getSelectedRogueDeck().getCommanderCardName();

        if (won) {
            totalRunsWon++;
            runsWonPerCommander.merge(commanderName, 1, Integer::sum);
        }

        // Final milestone update
        updateMilestones(run);
        save();
    }

    /**
     * Update milestone tracking based on current run state.
     */
    private void updateMilestones(RogueRun run) {
        // Track max life
        if (run.getCurrentLife() > maxLifeInRun) {
            maxLifeInRun = run.getCurrentLife();
        }

        // Track max gold
        if (run.getCurrentGold() > maxGoldInRun) {
            maxGoldInRun = run.getCurrentGold();
        }

        // Track creature types in deck
        int creatureTypes = countCreatureTypesInDeck(run.getCurrentDeck());
        if (creatureTypes > maxCreatureTypesInDeck) {
            maxCreatureTypesInDeck = creatureTypes;
        }

        // Track legendary permanents in deck
        int legendaryPermanents = countLegendaryPermanentsInDeck(run.getCurrentDeck());
        if (legendaryPermanents > maxLegendaryPermanentsInDeck) {
            maxLegendaryPermanentsInDeck = legendaryPermanents;
        }
    }

    /**
     * Count unique creature types in a deck.
     */
    private int countCreatureTypesInDeck(forge.deck.Deck deck) {
        if (deck == null || deck.getMain() == null) {
            return 0;
        }

        Set<String> creatureTypes = new HashSet<>();
        for (forge.item.PaperCard card : deck.getMain().toFlatList()) {
            if (card.getRules().getType().isCreature()) {
                creatureTypes.addAll(card.getRules().getType().getCreatureTypes());
            }
        }
        return creatureTypes.size();
    }

    /**
     * Count legendary permanents in a deck.
     */
    private int countLegendaryPermanentsInDeck(forge.deck.Deck deck) {
        if (deck == null || deck.getMain() == null) {
            return 0;
        }

        int count = 0;
        for (forge.item.PaperCard card : deck.getMain().toFlatList()) {
            if (card.getRules().getType().isLegendary() && card.getRules().getType().isPermanent()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Check all commanders for new unlocks and show a popup for each.
     * Called after meta progress updates (onMatchCompleted, onRunCompleted).
     */
    public void checkForNewUnlocks() {
        if (notifiedUnlocks == null) {
            notifiedUnlocks = new HashSet<>();
        }

        boolean changed = false;
        for (RogueDeck deck : RogueConfig.loadRogueDecks()) {
            String name = deck.getCommanderCardName();

            // Skip commanders that are always available (no unlock condition)
            if (deck.getUnlockCondition() == null || deck.getUnlockCondition().isDefault()) {
                continue;
            }

            if (deck.isUnlocked() && !notifiedUnlocks.contains(name)) {
                notifiedUnlocks.add(name);
                changed = true;

                // Show unlock popup with commander card image
                forge.item.PaperCard card = forge.model.FModel.getMagicDb()
                    .getCommonCards().getCard(name);
                forge.localinstance.skin.ISkinImage image = forge.gui.GuiBase.getInterface()
                    .createLayeredImage(card, forge.localinstance.skin.FSkinProp.IMG_SPECIAL_TROPHY,
                        forge.localinstance.properties.ForgeConstants.CACHE_ACHIEVEMENTS_DIR
                            + "/unlock_" + name.replace(" ", "_") + ".png", 1f);
                String unlockDesc = deck.getUnlockCondition() != null
                    ? deck.getUnlockCondition().getDescription() : "";
                forge.gui.GuiBase.getInterface().showImageDialog(image,
                    name + "\n" + unlockDesc,
                    "Commander Unlocked!");
            }
        }
        if (changed) {
            save();
        }
    }

    /**
     * Manually unlock a commander (for special unlocks).
     */
    public void unlockCommander(String commanderName) {
        unlockedCommanders.add(commanderName);
        save();
    }

    /**
     * Check if a commander has been manually unlocked.
     */
    public boolean isCommanderManuallyUnlocked(String commanderName) {
        return unlockedCommanders.contains(commanderName);
    }

    // ==================== Getters for Unlock Condition Evaluation ====================

    public int getTotalRunsStarted() {
        return totalRunsStarted;
    }

    public int getTotalRunsCompleted() {
        return totalRunsCompleted;
    }

    public int getTotalRunsWon() {
        return totalRunsWon;
    }

    public int getTotalMatchesWon() {
        return totalMatchesWon;
    }

    public int getTotalMatchesLost() {
        return totalMatchesLost;
    }

    public int getRunsWonWithCommander(String commanderName) {
        return runsWonPerCommander.getOrDefault(commanderName, 0);
    }

    public boolean hasUsedCommander(String commanderName) {
        return commandersUsed.contains(commanderName);
    }

    public boolean hasWonWithCommander(String commanderName) {
        return runsWonPerCommander.getOrDefault(commanderName, 0) > 0;
    }

    public int getMaxCreatureTypesInDeck() {
        return maxCreatureTypesInDeck;
    }

    public int getMaxLegendaryPermanentsInDeck() {
        return maxLegendaryPermanentsInDeck;
    }

    public int getMaxLifeInRun() {
        return maxLifeInRun;
    }

    public int getMaxGoldInRun() {
        return maxGoldInRun;
    }

    public Set<String> getCommandersUsed() {
        return new HashSet<>(commandersUsed);
    }

    public int getRunsStartedWithCommander(String commanderName) {
        return runsStartedPerCommander.getOrDefault(commanderName, 0);
    }

    // ==================== Aether System - Echo Management ====================

    public int getTotalEchoes() {
        return totalEchoes;
    }

    public void addEchoes(int amount) {
        if (amount > 0) {
            totalEchoes += amount;
            save();
        }
    }

    public boolean spendEchoes(int amount) {
        if (amount > 0 && totalEchoes >= amount) {
            totalEchoes -= amount;
            save();
            return true;
        }
        return false;
    }

    // ==================== Aether System - Boon Management ====================

    /**
     * Get the current rank of a boon (0 = not unlocked).
     */
    public int getBoonRank(BoonType type) {
        if (boonRanks == null) {
            boonRanks = new HashMap<>();
        }
        return boonRanks.getOrDefault(type.getId(), 0);
    }

    /**
     * Attempt to upgrade a boon to the next rank.
     * @return true if upgrade was successful, false if not enough echoes or already max rank
     */
    public boolean upgradeBoon(BoonType type) {
        if (boonRanks == null) {
            boonRanks = new HashMap<>();
        }

        int currentRank = getBoonRank(type);
        if (currentRank >= type.getMaxRank()) {
            return false; // Already max rank
        }

        int cost = type.getEchoCostForRank(currentRank + 1);
        if (totalEchoes < cost) {
            return false; // Not enough echoes
        }

        totalEchoes -= cost;
        boonRanks.put(type.getId(), currentRank + 1);
        save();
        return true;
    }

    /**
     * Check if a boon is currently active.
     */
    public boolean isBoonActive(BoonType type) {
        if (activeBoons == null) {
            activeBoons = new HashSet<>();
        }
        return activeBoons.contains(type.getId());
    }

    /**
     * Get all currently active boons.
     */
    public Set<BoonType> getActiveBoons() {
        Set<BoonType> active = new HashSet<>();
        if (activeBoons == null) {
            activeBoons = new HashSet<>();
            return active;
        }
        for (String id : activeBoons) {
            BoonType type = BoonType.fromId(id);
            if (type != null) {
                active.add(type);
            }
        }
        return active;
    }

    /**
     * Get the count of currently active boons.
     */
    public int getActiveBoonCount() {
        if (activeBoons == null) {
            activeBoons = new HashSet<>();
        }
        return activeBoons.size();
    }

    /**
     * Activate a boon (max 3 can be active).
     * @return true if activated, false if not unlocked or already 3 active
     */
    public boolean activateBoon(BoonType type) {
        if (activeBoons == null) {
            activeBoons = new HashSet<>();
        }

        // Must be unlocked (rank > 0)
        if (getBoonRank(type) == 0) {
            return false;
        }

        // If already active, nothing to do
        if (activeBoons.contains(type.getId())) {
            return true;
        }

        // Check max 3 active limit
        if (activeBoons.size() >= 3) {
            return false;
        }

        activeBoons.add(type.getId());
        save();
        return true;
    }

    /**
     * Deactivate a boon.
     */
    public void deactivateBoon(BoonType type) {
        if (activeBoons == null) {
            activeBoons = new HashSet<>();
        }
        activeBoons.remove(type.getId());
        save();
    }

    /**
     * Reset all boons to rank 0 and refund all spent echoes.
     * @return The amount of echoes refunded
     */
    public int resetBoons() {
        if (boonRanks == null) {
            boonRanks = new HashMap<>();
        }
        if (activeBoons == null) {
            activeBoons = new HashSet<>();
        }

        // Calculate total echoes spent on all boons
        int refund = 0;
        for (BoonType type : BoonType.values()) {
            int rank = getBoonRank(type);
            // Sum costs for each rank from 1 to current rank
            for (int r = 1; r <= rank; r++) {
                refund += type.getEchoCostForRank(r);
            }
        }

        // Refund echoes
        totalEchoes += refund;

        // Clear all boon data
        boonRanks.clear();
        activeBoons.clear();

        save();
        return refund;
    }

    // ==================== Aether System - Boon Effect Getters ====================

    /**
     * Get the starting life bonus from Vital Infusion.
     */
    public int getStartingLifeBonus() {
        if (!isBoonActive(BoonType.VITAL_INFUSION)) {
            return 0;
        }
        return BoonType.VITAL_INFUSION.getEffectValueAtRank(getBoonRank(BoonType.VITAL_INFUSION));
    }

    /**
     * Get the starting gold bonus from Aether Market.
     */
    public int getStartingGoldBonus() {
        if (!isBoonActive(BoonType.AETHER_MARKET)) {
            return 0;
        }
        return BoonType.AETHER_MARKET.getEffectValueAtRank(getBoonRank(BoonType.AETHER_MARKET));
    }

    /**
     * Get the post-match healing amount from Lingering Aura.
     */
    public int getPostMatchHealAmount(RogueRun currentRun) {
        if (!isBoonActive(BoonType.LINGERING_AURA) || currentRun.getCurrentLife() >= currentRun.getStartingLife()) {
            return 0;
        }
        return BoonType.LINGERING_AURA.getEffectValueAtRank(getBoonRank(BoonType.LINGERING_AURA));
    }

    /**
     * Get the extra starting hand cards from Foresight.
     */
    public int getExtraStartingCards() {
        if (!isBoonActive(BoonType.FORESIGHT)) {
            return 0;
        }
        return BoonType.FORESIGHT.getEffectValueAtRank(getBoonRank(BoonType.FORESIGHT));
    }

    /**
     * Get the extra mythic cards count from Mythic Collector.
     */
    public int getExtraMythicCards() {
        if (!isBoonActive(BoonType.MYTHIC_COLLECTOR)) {
            return 0;
        }
        return BoonType.MYTHIC_COLLECTOR.getEffectValueAtRank(getBoonRank(BoonType.MYTHIC_COLLECTOR));
    }

    // ==================== Run History ====================

    public List<RogueRunHistoryEntry> getRunHistory() {
        if (runHistory == null) {
            runHistory = new ArrayList<>();
        }
        return runHistory;
    }

    public void addRunHistoryEntry(RogueRunHistoryEntry entry) {
        if (runHistory == null) {
            runHistory = new ArrayList<>();
        }
        runHistory.add(entry);
        save();
    }

    public void clearRunHistory() {
        runHistory = new ArrayList<>();
        save();
    }

    // ==================== Tutorial Tracking ====================

    /**
     * Check if a tutorial has been seen.
     */
    public boolean hasSeenTutorial(RogueTutorial tutorial) {
        if (seenTutorials == null) {
            seenTutorials = new HashSet<>();
        }
        return seenTutorials.contains(tutorial.getId());
    }

    /**
     * Mark a tutorial as seen.
     */
    public void markTutorialSeen(RogueTutorial tutorial) {
        if (seenTutorials == null) {
            seenTutorials = new HashSet<>();
        }
        seenTutorials.add(tutorial.getId());
        save();
    }

    /**
     * Reset all tutorials to unseen state.
     */
    public void resetTutorials() {
        if (seenTutorials == null) {
            seenTutorials = new HashSet<>();
        }
        seenTutorials.clear();
        save();
    }

    // ==================== Persistence ====================

    private static File getMetaProgressFile() {
        return new File(RogueIO.ROGUE_SAVE_DIR, META_PROGRESS_FILE);
    }

    private static XStream getSerializer() {
        XStream xStream = new IgnoringXStream();
        xStream.addPermission(NoTypePermission.NONE);
        xStream.addPermission(NullPermission.NULL);
        xStream.addPermission(PrimitiveTypePermission.PRIMITIVES);
        xStream.allowTypeHierarchy(String.class);
        xStream.allowTypeHierarchy(HashMap.class);
        xStream.allowTypeHierarchy(HashSet.class);
        xStream.allowTypeHierarchy(ArrayList.class);
        xStream.allowTypeHierarchy(Map.class);
        xStream.allowTypeHierarchy(Set.class);
        xStream.allowTypeHierarchy(List.class);
        xStream.allowTypeHierarchy(RogueMetaProgress.class);
        xStream.allowTypesByWildcard(new String[] {
            RogueMetaProgress.class.getPackage().getName() + ".*",
            "forge.deck.*",
            "forge.item.*",
            "forge.card.*",
            "java.util.*",
            "java.lang.*"
        });
        xStream.ignoreUnknownElements();
        xStream.autodetectAnnotations(true);
        return xStream;
    }

    private static RogueMetaProgress load() {
        File file = getMetaProgressFile();
        if (!file.exists()) {
            return new RogueMetaProgress();
        }

        try (GZIPInputStream zin = new GZIPInputStream(Files.newInputStream(file.toPath()));
             InputStreamReader reader = new InputStreamReader(zin)) {
            return (RogueMetaProgress) getSerializer().fromXML(reader);
        } catch (Exception e) {
            System.err.println("Error loading meta progress, starting fresh: " + e.getMessage());
            return new RogueMetaProgress();
        }
    }

    public void save() {
        File dir = new File(RogueIO.ROGUE_SAVE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (BufferedOutputStream bout = new BufferedOutputStream(Files.newOutputStream(getMetaProgressFile().toPath()));
             GZIPOutputStream zout = new GZIPOutputStream(bout)) {
            getSerializer().toXML(this, zout);
            zout.flush();
        } catch (Exception e) {
            System.err.println("Error saving meta progress: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
