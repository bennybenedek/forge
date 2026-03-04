package forge.gamemodes.rogue;

/**
 * Mutable context for onCardReward triggers (card reward nodes only).
 * Effects accumulate into this object; the caller reads the final state.
 */
public class CardRewardContext {
    public int maxPicks;

    public CardRewardContext(int basePicks) {
        this.maxPicks = basePicks;
    }
}
