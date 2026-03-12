package forge.gamemodes.rogue;

import forge.gamemodes.rogue.effect.CardRewardContext;
import forge.gamemodes.rogue.effect.CardSelectionContext;
import forge.gamemodes.rogue.effect.RogueEffectComposite;
import forge.item.PaperCard;
import forge.item.PaperCardPredicates;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared card reward logic used by RogueWinLoseController (post-match rewards)
 * and CSubmenuRogueMap (chest node rewards).
 */
public class CardRewardHelper {

    /**
     * Platform-agnostic callback for showing the card reward dialog.
     * Returns selected cards, or null if reroll was clicked.
     */
    @FunctionalInterface
    public interface RewardDialog {
        List<PaperCard> show(String title, List<PaperCard> cards, int maxSelections, boolean showReroll);
    }

    /**
     * Run a card reward selection with boon adjustments, reroll loop, pool removal, and deck addition.
     *
     * @param run        current run
     * @param dialog     platform-specific dialog callback
     * @param mythicOnly true for mythic-only reward (3 mythics, pick 1), false for standard (6+1 mix, pick 3)
     * @return chosen cards (empty if player chose nothing), or null if reward pool was empty
     */
    public static List<PaperCard> runReward(RogueRun run, RewardDialog dialog, boolean mythicOnly) {
        RogueDeck rogueDeck = run.getSelectedRogueDeck();
        if (rogueDeck == null) return null;


        CardRewardContext rewardCtx = new CardRewardContext(mythicOnly ? 1 : 3);
        RogueEffectComposite.INSTANCE.onCardReward(rewardCtx, run);
        CardSelectionContext selCtx = new CardSelectionContext();
        RogueEffectComposite.INSTANCE.onCardSelection(selCtx, run);
        int maxPicks = rewardCtx.maxPicks;
        int rerollsRemaining = selCtx.rerolls;

        int baseNonMythics;
        int baseMythics;
        String title;
        if (mythicOnly) {
            baseNonMythics = 0;
            baseMythics = 3;
            title = "Choose Your Mythic Reward";
        } else {
            baseNonMythics = Math.max(0, 6 - selCtx.extraMythics);
            baseMythics = 1 + selCtx.extraMythics;
            title = "Choose Your Rewards";
        }

        List<PaperCard> rewardOptions;
        List<PaperCard> chosenCards;
        do {
            List<PaperCard> nonMythicCards = baseNonMythics > 0
                    ? rogueDeck.drawRewardOptions(baseNonMythics, PaperCardPredicates.IS_MYTHIC_RARE.negate())
                    : new ArrayList<>();
            List<PaperCard> mythicCards = rogueDeck.drawRewardOptions(baseMythics,
                    PaperCardPredicates.IS_MYTHIC_RARE);

            rewardOptions = new ArrayList<>();
            rewardOptions.addAll(nonMythicCards);
            rewardOptions.addAll(mythicCards);

            if (rewardOptions.isEmpty()) return null;

            chosenCards = dialog.show(title, rewardOptions, maxPicks, rerollsRemaining > 0);

            if (chosenCards == null) rerollsRemaining--;
        } while (chosenCards == null && rerollsRemaining >= 0);

        // Remove only the final draw's options from pool
        rogueDeck.removeFromRewardPool(rewardOptions);

        if (chosenCards == null) chosenCards = new ArrayList<>();

        if (!chosenCards.isEmpty()) {
            run.addCardsToRun(chosenCards);
        }

        return chosenCards;
    }
}
