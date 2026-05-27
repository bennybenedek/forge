package forge.gamemodes.rogue.path;

import forge.gamemodes.rogue.effect.ChestEffect;

/**
 * Represents a Chest node in a Rogue Commander path.
 * Chest nodes provide a random reward ('loot') from the ChestEffect pool.
 */
public class NodeChest extends RoguePathNode {

    private ChestEffect chestEffect;

    public NodeChest() {
        super();
    }

    public NodeChest(ChestEffect chestEffect) {
        super();
        this.chestEffect = chestEffect;
    }

    public ChestEffect getChestEffect() { return chestEffect; }
    public void setChestEffect(ChestEffect chestEffect) { this.chestEffect = chestEffect; }

    @Override
    public String toString() {
        return chestEffect != null ? "Loot (" + chestEffect.getDisplayName() + ")" : "Loot (Treasure)";
    }
}
