package forge.gamemodes.rogue.effect;

/**
 * Mutable context for onCardReward triggers.
 * Effects accumulate into this object; the caller reads the final state.
 */
public class CardRewardContext {
    public int maxPicks;
    public int nonMythicCardCountAdjustment;

    public CardRewardContext(int basePicks) {
        this.maxPicks = basePicks;
    }
}
