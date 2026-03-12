package forge.gamemodes.rogue.path;

import forge.gamemodes.rogue.effect.ChestLoot;

/**
 * Represents a Loot node in a Rogue Commander path.
 * Loot nodes provide a random reward from the ChestLoot pool.
 */
public class NodeChest extends RoguePathNode {

    private ChestLoot loot;

    public NodeChest() {
        super();
    }

    public NodeChest(ChestLoot loot) {
        super();
        this.loot = loot;
    }

    public ChestLoot getLoot() { return loot; }
    public void setLoot(ChestLoot loot) { this.loot = loot; }

    @Override
    public String toString() {
        return loot != null ? "Loot (" + loot.getDisplayName() + ")" : "Loot (Treasure)";
    }
}
