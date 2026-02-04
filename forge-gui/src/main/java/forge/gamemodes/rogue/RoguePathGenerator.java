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
    private static int elitePlaneboundIndex;
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
        List<RoguePlanebound> normalPlanebounds = getPlaneboundsOfType(
            availablePlanebounds, RoguePlaneboundType.NORMAL);
        List<RoguePlanebound> elitePlanebounds = getPlaneboundsOfType(
            availablePlanebounds, RoguePlaneboundType.ELITE);
        List<RoguePlanebound> bossPlanebounds = getPlaneboundsOfType(
            availablePlanebounds, RoguePlaneboundType.BOSS);

        normalPlaneboundIndex = 0;
        elitePlaneboundIndex = 0;
        currentRowIndex = 0;

        // Validate we have enough unique planebounds of each type
        validateSize(13, normalPlanebounds.size());
        validateSize(2, elitePlanebounds.size());
        validateSize(1, bossPlanebounds.size());

        // Shuffle lists for randomization
        shufflePlanebounds(normalPlanebounds);
        shufflePlanebounds(elitePlanebounds);
        shufflePlanebounds(bossPlanebounds);

        // Create nodes for the path
        List<RoguePathNode> nodes = new ArrayList<>();

        // Row 0: 2-4 NORMAL planes
        addNormalPlanebundRow(nodes, normalPlanebounds, createRandomNodeCount(2, 4));

        // Row 1: 2-4 NORMAL planes
        addNormalPlanebundRow(nodes, normalPlanebounds, createRandomNodeCount(2, 4));

        // Row 2: 2-3 Special Nodes (Sanctum, Bazaar, Chance for additional Sanctum)
        List<RoguePathNode> specialNodes = new ArrayList<>();
        specialNodes.add(new NodeSanctum());
        specialNodes.add(new NodeBazaar());
        if (MyRandom.getRandom().nextBoolean()) {
            specialNodes.add(new NodeSanctum());
        }
        addSpecialNodesRow(nodes, specialNodes);

        // Row 3: 3 planes - NORMAL, NORMAL, ELITE, Chance for second ELITE)
        List<RoguePlanebound> planeboundNodes = new ArrayList<>();
        planeboundNodes.add(normalPlanebounds.get(normalPlaneboundIndex++));
        planeboundNodes.add(elitePlanebounds.get(elitePlaneboundIndex++));
        planeboundNodes.add(normalPlanebounds.get(normalPlaneboundIndex++));

        if (MyRandom.getRandom().nextBoolean()) {
            planeboundNodes.add(elitePlanebounds.get(elitePlaneboundIndex++));
        }
        addMixedPlanebundRow(nodes, planeboundNodes);

        // Row 4: 2-3 Special Nodes (Sanctum, Bazaar, Chance for additional Sanctum)
        specialNodes = new ArrayList<>();
        specialNodes.add(new NodeSanctum());
        specialNodes.add(new NodeBazaar());
        if (MyRandom.getRandom().nextBoolean()) {
            specialNodes.add(new NodeBazaar());
        }
        addSpecialNodesRow(nodes, specialNodes);

        // Row 5: 2 - 3 NORMAL planes
        addNormalPlanebundRow(nodes, normalPlanebounds, createRandomNodeCount(2, 3));

        // Row 6: Sanctum, Bazaar
        // Random columnIndices for the nodes
        specialNodes = new ArrayList<>();
        specialNodes.add(new NodeSanctum());
        specialNodes.add(new NodeBazaar());
        addSpecialNodesRow(nodes, specialNodes);

        // Row 7: 1 BOSS plane
        addPlaneboundNode(nodes, bossPlanebounds.get(0), 0);

        // Create path from nodes
        return RoguePath.createPath(nodes.toArray(new RoguePathNode[0]));
    }

    private static void validateSize(int required, int size) {
        if (size < required) {
            throw new IllegalStateException(
                "Not enough available Planebounds for Path.");
        }
    }

    private static void addNormalPlanebundRow(List<RoguePathNode> allNodes,
        List<RoguePlanebound> planebounds, int columnCount) {
        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
            addPlaneboundNode(allNodes, planebounds.get(normalPlaneboundIndex++), columnIndex);
        }
        currentRowIndex++;
    }

    private static void addMixedPlanebundRow(List<RoguePathNode> allNodes,
        List<RoguePlanebound> mixedPlanebounds) {
        //shuffle the special nodes to randomize their order
        Collections.shuffle(mixedPlanebounds, MyRandom.getRandom());

        for (int columnIndex = 0; columnIndex < mixedPlanebounds.size(); columnIndex++) {
            addPlaneboundNode(allNodes, mixedPlanebounds.get(columnIndex), columnIndex);
        }
        currentRowIndex++;
    }

    private static void addSpecialNodesRow(List<RoguePathNode> allNodes,
        List<RoguePathNode> specialNodes) {
        //shuffle the special nodes to randomize their order
        Collections.shuffle(specialNodes, MyRandom.getRandom());

        for (int columnIndex = 0; columnIndex < specialNodes.size(); columnIndex++) {
            addSpecialNode(allNodes, specialNodes.get(columnIndex), columnIndex);
        }
        currentRowIndex++;
    }

    private static void addPlaneboundNode(List<RoguePathNode> nodes,
        RoguePlanebound planebound, int columnIndex) {
        NodePlanebound node = new NodePlanebound(planebound);
        node.setRowIndex(currentRowIndex);
        node.setColumnIndex(columnIndex);
        nodes.add(node);
    }

    private static void addSpecialNode(List<RoguePathNode> nodes,
        RoguePathNode specialNode, int columnIndex) {
        specialNode.setRowIndex(currentRowIndex);
        specialNode.setColumnIndex(columnIndex);
        nodes.add(specialNode);
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

    private static List<RoguePlanebound> getPlaneboundsOfType(
        List<RoguePlanebound> allPlanebounds, RoguePlaneboundType type) {
        List<RoguePlanebound> filtered = new ArrayList<>();
        for (RoguePlanebound planebound : allPlanebounds) {
            if (planebound.type() == type) {
                filtered.add(planebound);
            }
        }
        return filtered;
    }

    private static int createRandomNodeCount(int min, int max) {
        return MyRandom.getRandom().nextInt(max - min + 1) + min;
    }

    private static Integer[] createRandomColumnList(int count) {
        Integer[] columns = new Integer[count];
        for (int i = 0; i < count; i++) {
            columns[i] = i;
        }
        Collections.shuffle(java.util.Arrays.asList(columns), MyRandom.getRandom());
        return columns;
    }

    private static void shufflePlanebounds(List<RoguePlanebound> planebounds) {
        Collections.shuffle(planebounds, MyRandom.getRandom());
    }
}
