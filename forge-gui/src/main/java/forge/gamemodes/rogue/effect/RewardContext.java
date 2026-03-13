package forge.gamemodes.rogue.effect;

/**
 * Mutable context for onBeforeRewards triggers.
 * Effects can modify reward behavior after a match win.
 */
public class RewardContext {
    public boolean skipRewards;
}
