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
     * Generate a random linear path with the specified number of nodes.
     * Planebounds are randomly selected based on their type: normal encounters for rows 1-4,
     * boss encounter for row 5.
     *
     * @param nodeCount Number of nodes in the path (typically 5)
     * @return PathData with randomized plane encounters
     */
    public static RoguePath generateRandomLinearPath(int nodeCount) {
        List<RoguePlanebound> availablePlanebounds = RogueConfig.loadPlanebounds();

        if (availablePlanebounds.isEmpty()) {
            throw new IllegalStateException("No planebounds available for path generation");
        }

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

        // Calculate required counts for each type
        // With nodeCount=5: 1 elite (middle), 1 boss (last), 3 normal (others)
        int requiredElite = 1;
        int requiredBoss = 1;
        int requiredNormal = nodeCount - requiredElite - requiredBoss;

        // Validate we have enough unique planebounds of each type
        if (normalPlanebounds.size() < requiredNormal) {
            throw new IllegalStateException(
                String.format("Not enough normal planebounds: need %d, have %d",
                    requiredNormal, normalPlanebounds.size()));
        }
        if (elitePlanebounds.size() < requiredElite) {
            throw new IllegalStateException(
                String.format("Not enough elite planebounds: need %d, have %d",
                    requiredElite, elitePlanebounds.size()));
        }
        if (bossPlanebounds.size() < requiredBoss) {
            throw new IllegalStateException(
                String.format("Not enough boss planebounds: need %d, have %d",
                    requiredBoss, bossPlanebounds.size()));
        }

        // Shuffle lists for randomization
        Collections.shuffle(normalPlanebounds, MyRandom.getRandom());
        Collections.shuffle(elitePlanebounds, MyRandom.getRandom());
        Collections.shuffle(bossPlanebounds, MyRandom.getRandom());

        // Create nodes from randomly selected planebounds (shuffled lists ensure no duplicates)
        // Use separate counters to track how many of each type we've used
        int normalIndex = 0;
        int eliteIndex = 0;
        int bossIndex = 0;

        List<RoguePathNode> nodes = new ArrayList<>();
        int rowIndex = 0; // Track actual row index (excluding side nodes)

        for (int i = 0; i < nodeCount; i++) {
            RoguePlanebound roguePlanebound;

            // Middle node (row 3) is always elite
            if (i == nodeCount / 2) {
                roguePlanebound = elitePlanebounds.get(eliteIndex);
                eliteIndex++;
            }
            // Last node (row 5) is always a boss, others are normal
            else if (i == nodeCount - 1) {
                roguePlanebound = bossPlanebounds.get(bossIndex);
                bossIndex++;
            } else {
                roguePlanebound = normalPlanebounds.get(normalIndex);
                normalIndex++;
            }

            NodePlanebound node = new NodePlanebound(roguePlanebound);

            // Set row index for life scaling: Row 0 = 5 life, Row 1 = 10 life, etc.
            node.setRowIndex(rowIndex);
            nodes.add(node);

            // Add Sanctum after 2nd plane (index 1)
            if (i == 1) {
                NodeSanctum sanctum = new NodeSanctum(5, 2); // Heal 5, Remove 2 cards
                sanctum.setRowIndex(rowIndex); // Same row as preceding plane
                nodes.add(sanctum);
                // Note: Sanctum doesn't increment rowIndex (doesn't count as a row)
            }

            // Add Bazaar after ELITE plane (middle node, index 2)
            if (i == nodeCount / 2) {
                NodeBazaar bazaar = new NodeBazaar();
                bazaar.setRowIndex(rowIndex); // Same row as preceding plane
                nodes.add(bazaar);
                // Note: Bazaar doesn't increment rowIndex (doesn't count as a row)
            }

            // Add second Sanctum after 4th plane (index 3), right before boss
            if (i == 3) {
                NodeSanctum sanctum = new NodeSanctum(5, 2); // Heal 5, Remove 2 cards
                sanctum.setRowIndex(rowIndex); // Same row as preceding plane
                nodes.add(sanctum);
                // Note: Sanctum doesn't increment rowIndex (doesn't count as a row)
            }

            rowIndex++; // Increment row index for next plane node
        }

        // Create linear path from nodes
        return RoguePath.createLinearPath(nodes.toArray(new RoguePathNode[0]));
    }
}
