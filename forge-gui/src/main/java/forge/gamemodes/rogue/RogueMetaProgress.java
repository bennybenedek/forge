package forge.gamemodes.rogue;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;
import forge.gamemodes.rogue.effect.DescensionLevel;
import forge.gamemodes.rogue.effect.EchoEffect;
import forge.gui.GuiBase;
import forge.item.PaperCard;
import forge.localinstance.properties.ForgeConstants;
import forge.localinstance.skin.FSkinProp;
import forge.localinstance.skin.ISkinImage;
import forge.model.FModel;
import forge.util.IgnoringXStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Tracks persistent meta-progression for Rogue Commander mode.
 * This data persists across all runs and tracks unlocks, achievements, and statistics.
 */
public class RogueMetaProgress {

    private static final String META_PROGRESS_FILE = "meta_progress.dat";
    private static RogueMetaProgress instance;

    // Per-commander tracking
    private Map<String, Integer> runsStartedPerCommander;
    private Map<String, Integer> runsWonPerCommander;

    // Stat tracking (unified map, keyed by RogueStats.conditionKey)
    private Map<String, Integer> statValues;

    // Per-commander descension tracking
    private Map<String, Integer> maxDescensionWonPerCommander = new HashMap<>();

    // Aether system - persistent echoes, sparks, and boons
    private int totalEchoes;                      // Persistent echo currency
    private int totalSparks;                      // Earned from Descension wins
    private int aetherUpgradeLevel = 0;           // XStream defaults int to 0 for old saves
    private Map<String, Integer> boonRanks;       // Boon ID -> current rank (0 = not unlocked)
    private Set<String> activeEchoBoons;          // Currently equipped Echo boon IDs (max slots via getActiveBoonSlots())

    // NPC progression levels (npcId -> level)
    private Map<String, Integer> npcLevels;

    // Unlock notification tracking - which unlocks have been shown to the player
    private Set<String> notifiedCommanderUnlocks;

    // Tutorial tracking - which tutorials have been shown
    private Set<String> seenTutorials;

    // Run history
    private List<RogueRunHistoryEntry> runHistory;

    // Dev-only: bypass all commander unlock checks (not serialized)
    private transient boolean devUnlockAll;

    // Private constructor for singleton
    private RogueMetaProgress() {
        runsStartedPerCommander = new HashMap<>();
        runsWonPerCommander = new HashMap<>();
        statValues = new HashMap<>();

        // Initialize Aether system
        totalEchoes = 0;
        boonRanks = new HashMap<>();
        activeEchoBoons = new HashSet<>();

        // Initialize unlock notification tracking
        notifiedCommanderUnlocks = new HashSet<>();

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
        runsStartedPerCommander = new HashMap<>();
        runsWonPerCommander = new HashMap<>();
        statValues = new HashMap<>();
        maxDescensionWonPerCommander = new HashMap<>();

        // Reset Aether system
        totalEchoes = 0;
        totalSparks = 0;
        aetherUpgradeLevel = 0;
        boonRanks = new HashMap<>();
        activeEchoBoons = new HashSet<>();
        notifiedCommanderUnlocks = new HashSet<>();
        npcLevels = new HashMap<>();

        // Reset run history
        runHistory = new ArrayList<>();

        // Note: Tutorials are NOT reset here - use resetTutorials() separately
        save();
    }

    // ==================== Stat System ====================

    /**
     * Get the stored value for a stat key.
     */
    public int getStatValue(String key) {
        if (statValues == null) statValues = new HashMap<>();
        return statValues.getOrDefault(key, 0);
    }

    /**
     * Update a stat value if the new value exceeds the stored value.
     * Works for both counters (pass current + 1) and max-value tracking (pass snapshot).
     * Checks decks with matching unlock conditions for new unlocks.
     */
    public void updateStat(RogueStats stat, int value) {
        if (statValues == null) statValues = new HashMap<>();
        String key = stat.getConditionKey();
        if (value > statValues.getOrDefault(key, 0)) {
            statValues.put(key, value);
        }
        checkForNewUnlocks(stat);
    }

    // ==================== Per-Commander Tracking ====================

    /**
     * Track per-commander run start count.
     */
    public void trackCommanderStarted(String commanderName) {
        runsStartedPerCommander.merge(commanderName, 1, Integer::sum);
    }

    public void mergeRunsWonPerCommander(String commanderName) {
        runsWonPerCommander.merge(commanderName, 1, Integer::sum);
    }

    // ==================== Unlock Checking ====================

    /**
     * Check for new unlocks relevant to the given stat.
     */
    void checkForNewUnlocks(RogueStats stat) {
        if (notifiedCommanderUnlocks == null) notifiedCommanderUnlocks = new HashSet<>();
        String key = stat.getConditionKey();

        boolean changed = false;
        for (RogueDeck deck : RogueConfig.loadRogueDecks()) {
            if (deck.getUnlockCondition() == null || deck.getUnlockCondition().isDefault()) continue;
            if (!deck.getUnlockCondition().hasCondition(key)) continue;

            String name = deck.getCommanderCardName();
            if (deck.isUnlocked() && !notifiedCommanderUnlocks.contains(name)) {
                notifiedCommanderUnlocks.add(name);
                changed = true;

                PaperCard card = FModel.getMagicDb().getCommonCards().getCard(name);
                ISkinImage image = GuiBase.getInterface()
                    .createLayeredImage(card, FSkinProp.IMG_SPECIAL_TROPHY,
                        ForgeConstants.CACHE_ACHIEVEMENTS_DIR
                            + "/unlock_" + name.replace(" ", "_") + ".png", 1f);
                String unlockDesc = deck.getUnlockCondition().getDescription();
                GuiBase.getInterface().showImageDialog(image,
                    name + "\n" + unlockDesc,
                    "Commander Unlocked!");
            }
        }

        // Check global Descension Mode unlock when runs are won
        if (stat == RogueStats.RUNS_WON) {
            final String descensionKey = "DESCENSION_MODE";
            if (isDescensionModeUnlocked() && !notifiedCommanderUnlocks.contains(descensionKey)) {
                notifiedCommanderUnlocks.add(descensionKey);
                changed = true;
                GuiBase.getInterface().showImageDialog(null,
                    "You have won Runs with 3 different Commanders!\n" +
                        "Descension Mode is now unlocked.",
                    "Descension Mode Unlocked!");
            }
        }

        if (changed) {
            save();
        }
    }

    // ==================== Getters for Unlock Condition Evaluation ====================

    public int getTotalRunsStarted() {
        return getStatValue(RogueStats.RUNS_STARTED.getConditionKey());
    }

    public int getTotalRunsCompleted() {
        return getStatValue(RogueStats.RUNS_COMPLETED.getConditionKey());
    }

    public int getTotalRunsWon() {
        return getStatValue(RogueStats.RUNS_WON.getConditionKey());
    }

    public int getTotalMatchesWon() {
        return getStatValue(RogueStats.MATCHES_WON.getConditionKey());
    }

    public int getTotalMatchesLost() {
        return getStatValue(RogueStats.MATCHES_LOST.getConditionKey());
    }

    public int getRunsWonWithCommander(String commanderName) {
        return runsWonPerCommander.getOrDefault(commanderName, 0);
    }

    public boolean hasWonWithCommander(String commanderName) {
        return runsWonPerCommander.getOrDefault(commanderName, 0) > 0;
    }

    public int getMaxCreatureTypesInDeck() {
        return getStatValue(RogueStats.CREATURE_TYPES.getConditionKey());
    }

    public int getMaxSharedCreatureTypeInDeck() {
        return getStatValue(RogueStats.MAX_SHARED_CREATURE_TYPE.getConditionKey());
    }

    public int getMaxLegendaryPermanentsInDeck() {
        return getStatValue(RogueStats.LEGENDARY_PERMANENTS.getConditionKey());
    }

    public int getMaxLifeInRun() {
        return getStatValue(RogueStats.MAX_LIFE.getConditionKey());
    }

    public int getMaxGoldInRun() {
        return getStatValue(RogueStats.MAX_GOLD.getConditionKey());
    }

    public Set<String> getCommandersUsed() {
        return new HashSet<>(runsStartedPerCommander.keySet());
    }

    public int getRunsStartedWithCommander(String commanderName) {
        return runsStartedPerCommander.getOrDefault(commanderName, 0);
    }

    public int getDistinctCommandersWon() {
        int count = 0;
        for (int wins : runsWonPerCommander.values()) {
            if (wins > 0) count++;
        }
        return count;
    }

    // ==================== Descension Management ====================

    public int getMaxDescensionWon(String commanderName) {
        if (maxDescensionWonPerCommander == null) maxDescensionWonPerCommander = new HashMap<>();
        return maxDescensionWonPerCommander.getOrDefault(commanderName, 0);
    }

    /** Returns max descension level available to START. Normal win = Level 1 unlocked. */
    public int getMaxDescensionUnlocked(String commanderName) {
        if (!hasWonWithCommander(commanderName)) return 0;
        return 1 + getMaxDescensionWon(commanderName);
    }

    public void notifyDescensionL1IfFirstWin(String commanderName) {
        if (!isDescensionModeUnlocked() || getRunsWonWithCommander(commanderName) != 1) return;
        forge.item.PaperCard card = forge.model.FModel.getMagicDb().getCommonCards().getCard(commanderName);
        forge.localinstance.skin.ISkinImage image = forge.gui.GuiBase.getInterface()
            .createLayeredImage(card, forge.localinstance.skin.FSkinProp.IMG_SPECIAL_TROPHY,
                forge.localinstance.properties.ForgeConstants.CACHE_ACHIEVEMENTS_DIR
                    + "/descension_" + commanderName.replace(" ", "_") + "_1.png", 1f);
        forge.gui.GuiBase.getInterface().showImageDialog(image,
            "You unlocked Descension Mode for " + commanderName + "!",
            "Descension Level 1 Unlocked!");
    }

    public boolean recordDescensionWin(String commanderName, int level) {
        if (maxDescensionWonPerCommander == null) maxDescensionWonPerCommander = new HashMap<>();
        if (level > getMaxDescensionWon(commanderName)) {
            maxDescensionWonPerCommander.put(commanderName, level);
            totalSparks++;
            int unlockedLevel = level + 1;
            if (unlockedLevel <= DescensionLevel.getMaxLevel()) {
                DescensionLevel descensionLevel = DescensionLevel.forLevel(unlockedLevel);
                forge.item.PaperCard card = forge.model.FModel.getMagicDb()
                    .getCommonCards().getCard(commanderName);
                forge.localinstance.skin.ISkinImage image = forge.gui.GuiBase.getInterface()
                    .createLayeredImage(card, forge.localinstance.skin.FSkinProp.IMG_SPECIAL_TROPHY,
                        forge.localinstance.properties.ForgeConstants.CACHE_ACHIEVEMENTS_DIR
                            + "/descension_" + commanderName.replace(" ", "_") + "_" + unlockedLevel + ".png", 1f);
                forge.gui.GuiBase.getInterface().showImageDialog(image,
                    "You unlocked Descension Level " + unlockedLevel + ": " + descensionLevel.name + " for " + commanderName + "!",
                    "Descension Level " + unlockedLevel + " Unlocked!");
            }
            save();
            return true;
        }
        return false;
    }

    public boolean isDescensionModeUnlocked() {
        return getDistinctCommandersWon() >= 3;
    }

    public boolean isDevUnlockAll() { return devUnlockAll; }
    public void setDevUnlockAll(boolean value) { devUnlockAll = value; }

    // ==================== Aether System - Echo Management ====================

    public int getTotalEchoes() {
        return totalEchoes;
    }

    public void setTotalEchoes(int echoes) {
        this.totalEchoes = Math.max(0, echoes);
    }

    public int getTotalSparks() {
        return totalSparks;
    }

    public int getAetherUpgradeLevel() {
        return aetherUpgradeLevel;
    }

    public void setAetherUpgradeLevel(int level) {
        aetherUpgradeLevel = level;
        save();
    }

    /**
     * Purchase the next Aether Upgrade in sequence (must be purchased in order).
     * @param level The level to purchase (must equal current level + 1)
     * @return true if purchase succeeded
     */
    public boolean purchaseAetherUpgrade(int level) {
        AetherUpgrade upgrade = AetherUpgrade.forLevel(level);
        if (upgrade == null || aetherUpgradeLevel >= level || level != aetherUpgradeLevel + 1) return false;
        if (totalSparks < upgrade.sparkCost) return false;
        totalSparks -= upgrade.sparkCost;
        aetherUpgradeLevel = level;
        save();
        return true;
    }

    /**
     * Get the number of active Boon slots (base 3, +1 from Aether Upgrade 2).
     */
    public int getActiveBoonSlots() {
        int slots = 3;
        for (int l = 1; l <= aetherUpgradeLevel; l++) {
            AetherUpgrade u = AetherUpgrade.forLevel(l);
            if (u != null) slots += u.extraBoonSlots;
        }
        return slots;
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
    public int getBoonRank(EchoEffect type) {
        if (boonRanks == null) {
            boonRanks = new HashMap<>();
        }
        return boonRanks.getOrDefault(type.getId(), 0);
    }

    /**
     * Attempt to upgrade a boon to the next rank.
     * @return true if upgrade was successful, false if not enough echoes or already max rank
     */
    public boolean upgradeBoon(EchoEffect boon) {
        if (boonRanks == null) {
            boonRanks = new HashMap<>();
        }

        if (!boon.isAccessibleAt(aetherUpgradeLevel)) {
            return false;
        }

        int currentRank = getBoonRank(boon);
        if (currentRank >= boon.getEffectiveMaxRank(aetherUpgradeLevel)) {
            return false; // Already max rank
        }

        int cost = boon.getEchoCostForRank(currentRank + 1);
        if (totalEchoes < cost) {
            return false; // Not enough echoes
        }

        totalEchoes -= cost;
        boonRanks.put(boon.getId(), currentRank + 1);
        save();
        return true;
    }

    /**
     * Check if a boon is currently active.
     */
    public boolean isBoonActive(EchoEffect boon) {
        if (activeEchoBoons == null) {
            activeEchoBoons = new HashSet<>();
        }
        return activeEchoBoons.contains(boon.getId());
    }

    /**
     * Get all currently active boons.
     */
    public Set<EchoEffect> getActiveEchoBoons() {
        Set<EchoEffect> active = new HashSet<>();
        if (activeEchoBoons == null) {
            activeEchoBoons = new HashSet<>();
            return active;
        }
        for (String id : activeEchoBoons) {
            EchoEffect type = EchoEffect.fromId(id);
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
        if (activeEchoBoons == null) {
            activeEchoBoons = new HashSet<>();
        }
        return activeEchoBoons.size();
    }

    /**
     * Activate a boon (max slots determined by getActiveBoonSlots()).
     */
    public void activateBoon(EchoEffect type) {
        if (activeEchoBoons == null) {
            activeEchoBoons = new HashSet<>();
        }

        // Must be accessible at current upgrade level
        if (!type.isAccessibleAt(aetherUpgradeLevel)) {
            return;
        }

        // Must be unlocked (rank > 0)
        if (getBoonRank(type) == 0) {
            return;
        }

        // If already active, nothing to do
        if (activeEchoBoons.contains(type.getId())) {
            return;
        }

        // Check active boon slot limit
        if (activeEchoBoons.size() >= getActiveBoonSlots()) {
            return;
        }

        activeEchoBoons.add(type.getId());
        save();
    }

    /**
     * Deactivate a boon.
     */
    public void deactivateBoon(EchoEffect boon) {
        if (activeEchoBoons == null) {
            activeEchoBoons = new HashSet<>();
        }
        activeEchoBoons.remove(boon.getId());
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
        if (activeEchoBoons == null) {
            activeEchoBoons = new HashSet<>();
        }

        // Calculate total echoes spent on all boons
        int refund = 0;
        for (EchoEffect boon : EchoEffect.values()) {
            int rank = getBoonRank(boon);
            // Sum costs for each rank from 1 to current rank
            for (int r = 1; r <= rank; r++) {
                refund += boon.getEchoCostForRank(r);
            }
        }

        // Refund echoes
        totalEchoes += refund;

        // Clear all boon data
        boonRanks.clear();
        activeEchoBoons.clear();

        save();
        return refund;
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

    // ==================== NPC Progression ====================

    public int getNPCLevel(String id) {
        if (npcLevels == null) npcLevels = new HashMap<>();
        return npcLevels.getOrDefault(id, 0);
    }

    public void setNPCLevelIfHigher(String id, int level) {
        if (npcLevels == null) npcLevels = new HashMap<>();
        if (level > npcLevels.getOrDefault(id, 0)) {
            npcLevels.put(id, level);
            save();
        }
    }

    /**
     * Sets the stored NPC progression level directly.
     */
    public void setNPCLevel(String id, int level) {
        if (npcLevels == null) npcLevels = new HashMap<>();
        if (level <= 0) {
            npcLevels.remove(id);
        } else {
            npcLevels.put(id, level);
        }
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
