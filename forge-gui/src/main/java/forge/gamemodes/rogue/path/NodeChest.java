package forge.gamemodes.rogue.path;

import forge.gamemodes.rogue.effect.ChestEffect;

/**
 * Represents a Loot node in a Rogue Commander path.
 * Loot nodes provide a random reward from the ChestLoot pool.
 */
public class NodeChest extends RoguePathNode {

    private ChestEffect loot;

    public NodeChest() {
        super();
    }

    public NodeChest(ChestEffect loot) {
        super();
        this.loot = loot;
    }

    public ChestEffect getLoot() { return loot; }
    public void setLoot(ChestEffect loot) { this.loot = loot; }

    @Override
    public String toString() {
        return loot != null ? "Loot (" + loot.getDisplayName() + ")" : "Loot (Treasure)";
    }
}
