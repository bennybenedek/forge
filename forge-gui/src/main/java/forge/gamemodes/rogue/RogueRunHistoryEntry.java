package forge.gamemodes.rogue;

import forge.deck.Deck;
import forge.gamemodes.rogue.effect.EchoBoon;
import forge.gamemodes.rogue.effect.RogueEffect;
import forge.gamemodes.rogue.path.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Lightweight snapshot of a completed/abandoned Rogue Commander run for history display.
 */
public class RogueRunHistoryEntry {

    private String commanderName;
    private int avatarIndex;
    private String outcome; // "VICTORY", "DEFEAT", "ABANDONED"
    private String bossOrDefeatedBy;
    private List<String> path;
    private int finalLife;
    private int finalGold;
    private String timestamp;
    private Deck deckSnapshot;
    private int descensionLevel;          // 0 = not used; XStream defaults to 0 for old saves
    private List<String> activeBoonNames; // null for old saves

    public RogueRunHistoryEntry() {
    }

    public static RogueRunHistoryEntry fromRun(RogueRun run, String outcome, String bossOrDefeatedBy) {
        RogueRunHistoryEntry entry = new RogueRunHistoryEntry();
        entry.commanderName = run.getCurrentCommanderName();
        entry.avatarIndex = run.getSelectedRogueDeck().getAvatarIndex();
        entry.outcome = outcome;
        entry.bossOrDefeatedBy = bossOrDefeatedBy != null ? bossOrDefeatedBy : "";
        entry.finalLife = run.getCurrentLife();
        entry.finalGold = run.getCurrentGold();
        entry.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        entry.deckSnapshot = run.getCurrentDeck() != null ? new Deck(run.getCurrentDeck()) : null;

        entry.descensionLevel = run.getDescensionLevel();

        // Capture active boons from run snapshot
        entry.activeBoonNames = new ArrayList<>();
        for (RogueEffect effect : run.getActiveEchoBoons()) {
            if (effect instanceof EchoBoon boon) {
                entry.activeBoonNames.add(boon.getDisplayName());
            }
        }

        entry.path = new ArrayList<>();

        if (run.getPath() != null) {
            RoguePathNode currentNode = run.getCurrentNode();
            for (RoguePathNode node : run.getPath().getNodes()) {
                if (!node.isCompleted() && node != currentNode) {
                    continue;
                }
                if (node instanceof NodePlanebound) {
                    NodePlanebound pb = (NodePlanebound) node;
                    if (pb.getRoguePlanebound() != null) {
                        entry.path.add(pb.getRoguePlanebound().planeName()
                                + " (" + pb.getRoguePlanebound().planeboundName() + ")");
                    }
                } else if (node instanceof NodeSanctum) {
                    entry.path.add("Sanctum");
                } else if (node instanceof NodeBazaar) {
                    entry.path.add("Bazaar");
                } else if (node instanceof NodeChest) {
                    entry.path.add("Loot");
                } else if (node instanceof NodeEvent) {
                    entry.path.add("Event");
                }
            }
        }

        return entry;
    }

    // Getters
    public String getCommanderName() { return commanderName; }
    public int getAvatarIndex() { return avatarIndex; }
    public String getOutcome() { return outcome; }
    public String getBossOrDefeatedBy() { return bossOrDefeatedBy; }
    public List<String> getPath() { return path != null ? path : new ArrayList<>(); }
    public int getFinalLife() { return finalLife; }
    public int getFinalGold() { return finalGold; }
    public String getTimestamp() { return timestamp; }
    public Deck getDeckSnapshot() { return deckSnapshot; }
    public int getDescensionLevel() { return descensionLevel; }
    public List<String> getActiveBoonNames() { return activeBoonNames != null ? activeBoonNames : new ArrayList<>(); }
}
