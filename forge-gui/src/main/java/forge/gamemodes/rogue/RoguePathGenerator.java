package forge.gamemodes.rogue;

import forge.util.MyRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generates paths for Rogue Commander runs.
 * Creates randomized encounters from available Planebound configurations.
 */
public class RoguePathGenerator {

    /**
     * Generate a random branched path with multiple planes per row.
     * Path structure:
     * Row 0: 3 NORMAL planes (columns 0, 1, 2)
     * Row 1: 2 NORMAL planes (columns 0, 1)
     * Row 2: 1 Sanctum (column 0)
     * Row 3: 3 planes - NORMAL, ELITE, NORMAL (columns 0, 1, 2)
     * Row 4: 1 Bazaar (column 0)
     * Row 5: 2 NORMAL planes (columns 0, 1)
     * Row 6: 1 BOSS plane (column 0)
     * Total: 9 planes (7 NORMAL + 1 ELITE + 1 BOSS) + 2 service nodes
     *
     * @return RoguePath with branched structure
     */
    public static RoguePath generateRandomBranchedPath() {
        List<RoguePlanebound> availablePlanebounds = RogueConfig.loadPlanebounds();

        // Split planebounds into normal, elite and boss lists
        List<RoguePlanebound> normalPlanebounds = new ArrayList<>();
        List<RoguePlanebound> elitePlanebounds = new ArrayList<>();
        List<RoguePlanebound> bossPlanebounds = new ArrayList<>();

        for (RoguePlanebound planebound : availablePlanebounds) {
            if (planebound.type() == RoguePlaneboundType.BOSS) {
                bossPlanebounds.add(planebound);
            } else if (planebound.type() == RoguePlaneboundType.ELITE) {
                elitePlanebounds.add(planebound);
            } else {
                normalPlanebounds.add(planebound);
            }
        }

        // required amounts of Planebounds for branched path
        int requiredNormal = 9;
        int requiredElite = 1;
        int requiredBoss = 1;

        // Validate we have enough unique planebounds of each type
        validateSize(requiredNormal, normalPlanebounds.size());
        validateSize(requiredElite, elitePlanebounds.size());
        validateSize(requiredBoss, bossPlanebounds.size());

        // Shuffle lists for randomization
        Collections.shuffle(normalPlanebounds, MyRandom.getRandom());
        Collections.shuffle(elitePlanebounds, MyRandom.getRandom());
        Collections.shuffle(bossPlanebounds, MyRandom.getRandom());

        List<RoguePathNode> nodes = new ArrayList<>();
        int normalIndex = 0;

        // Row 0: 3 NORMAL planes
        for (int col = 0; col < 3; col++) {
            addNode(nodes, normalPlanebounds.get(normalIndex++), 0, col);
        }

        // Row 1: 2 NORMAL planes
        for (int col = 0; col < 2; col++) {
            addNode(nodes, normalPlanebounds.get(normalIndex++), 1, col);
        }

        // Row 2: Sanctum (single node row)
        NodeSanctum sanctum = new NodeSanctum(5, 2);
        sanctum.setRowIndex(2);
        sanctum.setColumnIndex(0);
        nodes.add(sanctum);

        // Row 3: 3 planes - Normal, Elite, Normal
        addNode(nodes, normalPlanebounds.get(normalIndex++), 3, 0);
        addNode(nodes, elitePlanebounds.get(0), 3, 1);
        addNode(nodes, normalPlanebounds.get(normalIndex++), 3, 2);

        // Row 4: Bazaar (single node row)
        NodeBazaar bazaar = new NodeBazaar();
        bazaar.setRowIndex(4);
        bazaar.setColumnIndex(0);
        nodes.add(bazaar);

        // Row 5: 2 NORMAL planes
        for (int col = 0; col < 2; col++) {
            addNode(nodes, normalPlanebounds.get(normalIndex++), 5, col);
        }

        // Row 6: 1 BOSS plane
        addNode(nodes, bossPlanebounds.get(0), 6, 0);

        // Create path from nodes
        return RoguePath.createPath(nodes.toArray(new RoguePathNode[0]));
    }

    private static void validateSize(int required, int size) {
        if (size < required) {
            throw new IllegalStateException(
                "Not enough available Planebounds for Path.");
        }
    }

    private static void addNode(List<RoguePathNode> nodes,
        RoguePlanebound planebound, int rowIndex, int columnIndex) {
        NodePlanebound node = new NodePlanebound(planebound);
        node.setRowIndex(rowIndex);
        node.setColumnIndex(columnIndex);
        nodes.add(node);
    }
}
