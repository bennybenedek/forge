package forge.gamemodes.rogue;

import forge.deck.Deck;
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
    private List<String> visitedPlanes;
    private List<String> extraNodes;
    private int finalLife;
    private int finalGold;
    private String timestamp;
    private Deck deckSnapshot;

    public RogueRunHistoryEntry() {
    }

    public static RogueRunHistoryEntry fromRun(RogueRun run, String outcome, String bossOrDefeatedBy) {
        RogueRunHistoryEntry entry = new RogueRunHistoryEntry();
        entry.commanderName = run.getSelectedRogueDeck().getCommanderCardName();
        entry.avatarIndex = run.getSelectedRogueDeck().getAvatarIndex();
        entry.outcome = outcome;
        entry.bossOrDefeatedBy = bossOrDefeatedBy != null ? bossOrDefeatedBy : "";
        entry.finalLife = run.getCurrentLife();
        entry.finalGold = run.getCurrentGold();
        entry.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        entry.deckSnapshot = run.getCurrentDeck() != null ? new Deck(run.getCurrentDeck()) : null;

        entry.visitedPlanes = new ArrayList<>();
        entry.extraNodes = new ArrayList<>();

        if (run.getPath() != null) {
            RoguePathNode currentNode = run.getCurrentNode();
            for (RoguePathNode node : run.getPath().getNodes()) {
                if (!node.isCompleted() && node != currentNode) {
                    continue;
                }
                if (node instanceof NodePlanebound) {
                    NodePlanebound pb = (NodePlanebound) node;
                    if (pb.getRoguePlanebound() != null) {
                        entry.visitedPlanes.add(pb.getRoguePlanebound().planeName()
                                + " (" + pb.getRoguePlanebound().planeboundName() + ")");
                    }
                } else if (node instanceof NodeSanctum) {
                    entry.extraNodes.add("Sanctum");
                } else if (node instanceof NodeBazaar) {
                    entry.extraNodes.add("Bazaar");
                } else if (node instanceof NodeChest) {
                    entry.extraNodes.add("Loot");
                } else if (node instanceof NodeEvent) {
                    entry.extraNodes.add("Event");
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
    public List<String> getVisitedPlanes() { return visitedPlanes != null ? visitedPlanes : new ArrayList<>(); }
    public List<String> getExtraNodes() { return extraNodes != null ? extraNodes : new ArrayList<>(); }
    public int getFinalLife() { return finalLife; }
    public int getFinalGold() { return finalGold; }
    public String getTimestamp() { return timestamp; }
    public Deck getDeckSnapshot() { return deckSnapshot; }
}
