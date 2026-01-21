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
import java.util.HashMap;
import java.util.HashSet;
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
    private int maxLifeInRun;
    private int maxGoldInRun;

    // Explicit unlocks (for manually unlocked commanders)
    private Set<String> unlockedCommanders;

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
        maxLifeInRun = 0;
        maxGoldInRun = 0;
        unlockedCommanders = new HashSet<>();
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
        maxLifeInRun = 0;
        maxGoldInRun = 0;
        unlockedCommanders = new HashSet<>();
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
        xStream.allowTypeHierarchy(Map.class);
        xStream.allowTypeHierarchy(Set.class);
        xStream.allowTypeHierarchy(RogueMetaProgress.class);
        xStream.allowTypesByWildcard(new String[] {
            RogueMetaProgress.class.getPackage().getName() + ".*",
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
