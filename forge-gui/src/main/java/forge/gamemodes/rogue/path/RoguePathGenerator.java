package forge.gamemodes.rogue.path;

import forge.gamemodes.rogue.*;
import forge.gamemodes.rogue.effect.ChestEffect;
import forge.gamemodes.rogue.effect.RogueEffectComposite;
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
     * Generate a random path with multiple planes per row.
    */
    public static void generateRandomPath(RogueRun run) {
        currentRowIndex = 0;
        normalPlaneboundIndex = 0;
        int elitePlaneboundIndex = 0;
        int eventIndex = 0;
        int lootIndex = 0;

        List<RoguePlanebound> availablePlanebounds = RogueConfig.loadPlanebounds();
        List<RogueEvent> events = new ArrayList<>();
        for (RogueEvent e : RogueEvent.values()) {
            if (e.isAvailable()) events.add(e);
        }
        List<ChestEffect> chestEffect = new ArrayList<>(List.of(ChestEffect.values()));

        // Split planebounds into normal, elite and boss lists
        List<RoguePlanebound> normalPlanebounds = getPlaneboundsOfType(
            availablePlanebounds, RoguePlaneboundType.NORMAL);
        List<RoguePlanebound> elitePlanebounds = getPlaneboundsOfType(
          availablePlanebounds, RoguePlaneboundType.ELITE);
        List<RoguePlanebound> bossPlanebounds = getPlaneboundsOfType(
            availablePlanebounds, RoguePlaneboundType.BOSS);

        // Validate we have enough unique planebounds of each type
        validateSize(13, normalPlanebounds.size());
        validateSize(2, elitePlanebounds.size());
        validateSize(1, bossPlanebounds.size());
        validateSize(4, events.size());
        validateSize(8, chestEffect.size());

        // Shuffle lists for randomization
        shufflePlanebounds(normalPlanebounds);
        shufflePlanebounds(elitePlanebounds);
        shufflePlanebounds(bossPlanebounds);
        shuffleEvents(events);
        shuffleChestLoot(chestEffect);

        // Create nodes for the path
        List<RoguePathNode> nodes = new ArrayList<>();

        // Row 0: NORMAL planes
        addNormalPlanebundRow(nodes, normalPlanebounds, createRandomNodeCount(2, 4));

        // Row 1: Special Nodes (with higher chance for Event)
        List<RoguePathNode> specialNodes = new ArrayList<>();
        specialNodes.add(new NodeSanctum());
        specialNodes.add(new NodeBazaar());
        specialNodes.add(new NodeEvent(events.get(eventIndex++)));
        specialNodes.add(new NodeEvent(events.get(eventIndex++)));
        specialNodes.add(new NodeChest(List.of(
            chestEffect.get(lootIndex++),
            chestEffect.get(lootIndex++))));

        addSpecialNodesRow(nodes, specialNodes, createRandomNodeCount(2, 4));

        // Row 2: NORMAL planes
        addNormalPlanebundRow(nodes, normalPlanebounds, createRandomNodeCount(2, 4));

        // Row 3: Special Nodes (with higher chance for Sanctum)
        specialNodes = new ArrayList<>();
        specialNodes.add(new NodeSanctum());
        specialNodes.add(new NodeSanctum());
        specialNodes.add(new NodeBazaar());
        specialNodes.add(new NodeEvent(events.get(eventIndex++)));
        specialNodes.add(new NodeChest(List.of(
            chestEffect.get(lootIndex++),
            chestEffect.get(lootIndex++))));

        addSpecialNodesRow(nodes, specialNodes, createRandomNodeCount(2, 4));

        // Row 4: NORMAL / ELITE planes
        List<RoguePlanebound> planeboundNodes = new ArrayList<>();
        planeboundNodes.add(normalPlanebounds.get(normalPlaneboundIndex++));
        planeboundNodes.add(elitePlanebounds.get(elitePlaneboundIndex++));
        planeboundNodes.add(normalPlanebounds.get(normalPlaneboundIndex++));

        if (MyRandom.getRandom().nextBoolean()) {
            planeboundNodes.add(elitePlanebounds.get(elitePlaneboundIndex));
        }
        addMixedPlanebundRow(nodes, planeboundNodes);

        // Row 5: Special Nodes (with higher chance for Bazaar)
        specialNodes = new ArrayList<>();
        specialNodes.add(new NodeSanctum());
        specialNodes.add(new NodeBazaar());
        specialNodes.add(new NodeBazaar());
        specialNodes.add(new NodeEvent(events.get(eventIndex++)));
        specialNodes.add(new NodeChest(List.of(
            chestEffect.get(lootIndex++),
            chestEffect.get(lootIndex++))));

        addSpecialNodesRow(nodes, specialNodes, createRandomNodeCount(2, 4));

        // Row 6: NORMAL planes
        addNormalPlanebundRow(nodes, normalPlanebounds, createRandomNodeCount(2, 3));

        // Row 7: Special Nodes (no higher chance for any type)
        specialNodes = new ArrayList<>();
        specialNodes.add(new NodeSanctum());
        specialNodes.add(new NodeBazaar());
        specialNodes.add(new NodeEvent(events.get(eventIndex)));
        specialNodes.add(new NodeChest(List.of(
            chestEffect.get(lootIndex++),
            chestEffect.get(lootIndex++))));

        addSpecialNodesRow(nodes, specialNodes, createRandomNodeCount(2, 3));

        // Row 8: BOSS plane
        addPlaneboundNode(nodes, bossPlanebounds.get(0), 0);

        // Create path and set on run
        run.setPath(new RoguePath(nodes));

        // Apply post-generation effects (e.g. swap Normal nodes to Elite)
        RogueEffectComposite.INSTANCE.afterPathGeneration(run);
    }

    private static void validateSize(int required, int size) {
        if (size < required) {
            throw new IllegalStateException(
                "Not enough available node content for Path (Planebounds / Events / Chest Loot).");
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
        //shuffle the planebound nodes to randomize their order
        Collections.shuffle(mixedPlanebounds, MyRandom.getRandom());

        for (int columnIndex = 0; columnIndex < mixedPlanebounds.size(); columnIndex++) {
            addPlaneboundNode(allNodes, mixedPlanebounds.get(columnIndex), columnIndex);
        }
        currentRowIndex++;
    }

    private static void addSpecialNodesRow(List<RoguePathNode> allNodes,
        List<RoguePathNode> specialNodes, int columnCount) {
        //shuffle the special nodes to randomize their order
        Collections.shuffle(specialNodes, MyRandom.getRandom());

        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
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

    private static void shufflePlanebounds(List<RoguePlanebound> planebounds) {
        Collections.shuffle(planebounds, MyRandom.getRandom());
    }

    private static void shuffleEvents(List<RogueEvent> events) {
        Collections.shuffle(events, MyRandom.getRandom());
    }

    private static void shuffleChestLoot(List<ChestEffect> loots) {
        Collections.shuffle(loots, MyRandom.getRandom());
    }
}
