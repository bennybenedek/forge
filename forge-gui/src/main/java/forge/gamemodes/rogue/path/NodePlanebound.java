package forge.gamemodes.rogue.path;

import forge.gamemodes.rogue.RoguePlanebound;
import forge.gamemodes.rogue.RoguePlaneboundType;

/**
 * Represents a planebound encounter node in a Rogue Commander path.
 * This node type involves combat against a planebound opponent on their plane.
 * The type (NORMAL/ELITE/BOSS) is now defined in the RoguePlanebound itself.
 */
public class NodePlanebound extends RoguePathNode {

    private RoguePlanebound roguePlanebound;
    private int wrathfulCount;
    private int cursedCount;
    private int startingLifeModification;
    private boolean revealed;

    public NodePlanebound() {
        super();
    }

    public NodePlanebound(RoguePlanebound roguePlanebound) {
        super();
        this.roguePlanebound = roguePlanebound;
    }

    // Getters and Setters
    public RoguePlanebound getRoguePlanebound() {
        return roguePlanebound;
    }

    public void setRoguePlanebound(RoguePlanebound roguePlanebound) {
        this.roguePlanebound = roguePlanebound;
    }

    public int getWrathfulCount() {
        return wrathfulCount;
    }

    public void setWrathfulCount(int wrathfulCount) {
        this.wrathfulCount = wrathfulCount;
    }

    public int getCursedCount() {
        return cursedCount;
    }

    public void setCursedCount(int cursedCount) {
        this.cursedCount = cursedCount;
    }

    public int getStartingLifeModification() {
        return startingLifeModification;
    }

    public void setStartingLifeModification(int startingLifeModification) {
        this.startingLifeModification = startingLifeModification;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
    }

    public RoguePlaneboundType getPlaneboundType() {
        return roguePlanebound != null ? roguePlanebound.type() : RoguePlaneboundType.NORMAL;
    }

    // set life based on Planebound rows + Planebound type
    public int getPlaneboundLife(int pathRowCount) {
        int rowLife = pathRowCount * 5;
        int planeboundTypeBaseLife;

        if (getPlaneboundType().equals(RoguePlaneboundType.BOSS)) {
            return 30 + startingLifeModification;
        }
        else if (getPlaneboundType().equals(RoguePlaneboundType.ELITE)) {
            planeboundTypeBaseLife = 5;
        }
        else {
            planeboundTypeBaseLife = 0;
        }

        return rowLife + planeboundTypeBaseLife + startingLifeModification;
    }

    // Convenience methods for rewards
    public int getGoldReward() {
        return getPlaneboundType().getGoldReward();
    }

    public int getEchoReward() {
        return getPlaneboundType().getEchoReward();
    }

    @Override
    public String toString() {
        RoguePlaneboundType type = getPlaneboundType();
        String typeStr = type == RoguePlaneboundType.BOSS ? "Boss" :
                        type == RoguePlaneboundType.ELITE ? "Elite" : "Plane";
        return typeStr + ": " + roguePlanebound.planeName() + " (vs " + roguePlanebound.planeboundName() + ")";
    }
}
