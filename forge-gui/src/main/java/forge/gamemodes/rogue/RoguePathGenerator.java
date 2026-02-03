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
     * Row 0: 2-4 NORMAL planes (columns 0, 1, 2, 3)
     * Row 1: 2-4 NORMAL planes (columns 0, 1, 2, 3)
     * Row 2: 1 Sanctum, 1 Bazaar, 1 Sanctum (column 0, 1, 2)
     * Row 3: 3-4 planes - NORMAL, ELITE, (2. ELITE), NORMAL (columns 0, 1, 2, 3)
     * Row 4: 1 Bazaar, 1 Sanctum, 1 Bazaar (column 0, 1)
     * Row 5: 2 NORMAL planes (columns 0, 1)
     * Row 6: 1 Sanctum, 1 Bazaar (column 0)
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
        int requiredNormal = 12;
        int requiredElite = 2;
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
        Integer[] randomColumns = new Integer[] {0, 1, 2};
        int randomPlaneCount;

        // Row 0: 2-4 NORMAL planes
        randomPlaneCount = MyRandom.getRandom().nextInt(3) + 2;
        addNormalPlanebundRow(nodes, normalPlanebounds, randomPlaneCount);

        // Row 1: 2-4 NORMAL planes
        randomPlaneCount = MyRandom.getRandom().nextInt(3) + 2;
        addNormalPlanebundRow(nodes, normalPlanebounds, randomPlaneCount);

        // Row 2: Sanctum, Bazaar, Sanctum
        // random columnIndices for the nodes
        Collections.shuffle(java.util.Arrays.asList(randomColumns), MyRandom.getRandom());
        addSanctumNode(nodes, randomColumns[0]);
        addBazaarNode(nodes, randomColumns[1]);
        addSanctumNode(nodes, randomColumns[2]);
        currentRowIndex++;

        // Row 3: 3 planes - Normal, Elite, (Chance for second Elite), Normal
        int columnIndex = 0;
        addPlaneboundNode(nodes, normalPlanebounds.get(normalPlaneboundIndex++), currentRowIndex, columnIndex++);
        addPlaneboundNode(nodes, elitePlanebounds.get(0), currentRowIndex, columnIndex++);
        if (MyRandom.getRandom().nextBoolean()) {
            addPlaneboundNode(nodes, elitePlanebounds.get(1), currentRowIndex, columnIndex++);
        }
        addPlaneboundNode(nodes, normalPlanebounds.get(normalPlaneboundIndex++), currentRowIndex, columnIndex);
        currentRowIndex++;

        // Row 4: Bazaar, Sanctum, Bazaar
        // Random columnIndices for the nodes
        Collections.shuffle(java.util.Arrays.asList(randomColumns), MyRandom.getRandom());
        addBazaarNode(nodes, randomColumns[0]);
        addSanctumNode(nodes, randomColumns[1]);
        addBazaarNode(nodes, randomColumns[2]);
        currentRowIndex++;

        // Row 5: 2 NORMAL planes
        addNormalPlanebundRow(nodes, normalPlanebounds, 2);

        // Row 6: Sanctum, Bazaar
        // Random columnIndices for the nodes
        randomColumns = new Integer[] {0, 1};
        Collections.shuffle(java.util.Arrays.asList(randomColumns), MyRandom.getRandom());
        addSanctumNode(nodes, randomColumns[0]);
        addBazaarNode(nodes, randomColumns[1]);
        currentRowIndex++;

        // Row 7: 1 BOSS plane
        addPlaneboundNode(nodes, bossPlanebounds.get(0), currentRowIndex, 0);

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
        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
            addPlaneboundNode(nodes, planebounds.get(normalPlaneboundIndex++), currentRowIndex, columnIndex);
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
        addBazaarNode(nodes, 0);
        currentRowIndex++;
    }

    private static void addPlaneboundNode(List<RoguePathNode> nodes,
        RoguePlanebound planebound, int rowIndex, int columnIndex) {
        NodePlanebound node = new NodePlanebound(planebound);
        node.setRowIndex(rowIndex);
        node.setColumnIndex(columnIndex);
        nodes.add(node);
    }

    private static void addSanctumNode(List<RoguePathNode> nodes, int columnIndex) {
        NodeSanctum sanctum = new NodeSanctum();
        sanctum.setRowIndex(currentRowIndex);
        sanctum.setColumnIndex(columnIndex);
        nodes.add(sanctum);
    }

    private static void addBazaarNode(List<RoguePathNode> nodes, int columnIndex) {
        NodeBazaar bazaar = new NodeBazaar();
        bazaar.setRowIndex(currentRowIndex);
        bazaar.setColumnIndex(columnIndex);
        nodes.add(bazaar);
    }
}
