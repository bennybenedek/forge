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

    private static int normalPlaneboundIndex;
    private static int currentRowIndex;

    /**
     * Generate a random branched path with multiple planes per row.
     * Path structure:
     * Row 0: 3 NORMAL planes (columns 0, 1, 2)
     * Row 1: 2 NORMAL planes (columns 0, 1)
     * Row 2: 1 Sanctum (column 0)
     * Row 3: 3 planes - NORMAL, ELITE, NORMAL (columns 0, 1, 2)
     * Row 4: 1 Bazaar (column 0)
     * Row 5: 2 NORMAL planes (columns 0, 1)
     * Row 6: 1 Sanctum (column 0)
     * Row 7: 1 BOSS plane (column 0)
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

        normalPlaneboundIndex = 0;
        currentRowIndex = 0;

        // Validate we have enough unique planebounds of each type
        validateSize(requiredNormal, normalPlanebounds.size());
        validateSize(requiredElite, elitePlanebounds.size());
        validateSize(requiredBoss, bossPlanebounds.size());

        // Shuffle lists for randomization
        Collections.shuffle(normalPlanebounds, MyRandom.getRandom());
        Collections.shuffle(elitePlanebounds, MyRandom.getRandom());
        Collections.shuffle(bossPlanebounds, MyRandom.getRandom());

        List<RoguePathNode> nodes = new ArrayList<>();

        // Row 0: 3 NORMAL planes
        addNormalPlanebundRow(nodes, normalPlanebounds, 3);

        // Row 1: 2 NORMAL planes
        addNormalPlanebundRow(nodes, normalPlanebounds, 2);

        // Row 2: Sanctum (single node row)
        addSingleSanctumRow(nodes);

        // Row 3: 3 planes - Normal, Elite, Normal
        addNode(nodes, normalPlanebounds.get(normalPlaneboundIndex++), currentRowIndex, 0);
        addNode(nodes, elitePlanebounds.get(0), currentRowIndex, 1);
        addNode(nodes, normalPlanebounds.get(normalPlaneboundIndex++), currentRowIndex, 2);
        currentRowIndex++;

        // Row 4: Bazaar (single node row)
        addSingleBazaarRow(nodes);

        // Row 5: 2 NORMAL planes
        addNormalPlanebundRow(nodes, normalPlanebounds, 2);

        //Row 6: Sanctum (single node row)
        addSingleSanctumRow(nodes);

        // Row 6: 1 BOSS plane
        addNode(nodes, bossPlanebounds.get(0), currentRowIndex, 0);

        // Create path from nodes
        return RoguePath.createPath(nodes.toArray(new RoguePathNode[0]));
    }

    private static void validateSize(int required, int size) {
        if (size < required) {
            throw new IllegalStateException(
                "Not enough available Planebounds for Path.");
        }
    }

    private static void addNormalPlanebundRow(List<RoguePathNode> nodes,
        List<RoguePlanebound> planebounds, int columnCount) {
        for (int col = 0; col < columnCount; col++) {
            addNode(nodes, planebounds.get(normalPlaneboundIndex++), currentRowIndex, col);
        }
        currentRowIndex++;
    }

    private static void addSingleSanctumRow(List<RoguePathNode> nodes) {
        NodeSanctum sanctum = new NodeSanctum(5, 2);
        sanctum.setRowIndex(currentRowIndex++);
        sanctum.setColumnIndex(0);
        nodes.add(sanctum);
    }

    private static void addSingleBazaarRow(List<RoguePathNode> nodes) {
        NodeBazaar bazaar = new NodeBazaar();
        bazaar.setRowIndex(currentRowIndex++);
        bazaar.setColumnIndex(0);
        nodes.add(bazaar);
    }

    private static void addNode(List<RoguePathNode> nodes,
        RoguePlanebound planebound, int rowIndex, int columnIndex) {
        NodePlanebound node = new NodePlanebound(planebound);
        node.setRowIndex(rowIndex);
        node.setColumnIndex(columnIndex);
        nodes.add(node);
    }
}
