package forge.gamemodes.rogue;

import forge.gamemodes.rogue.effect.CardRewardContext;
import forge.gamemodes.rogue.effect.CardSelectionContext;
import forge.gamemodes.rogue.effect.MatchRewardContext;
import forge.gamemodes.rogue.effect.RogueEffectComposite;
import forge.item.PaperCard;
import forge.item.PaperCardPredicates;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Shared card reward logic used by RogueWinLoseController (post-match rewards)
 * and CSubmenuRogueMap (chest node rewards).
 */
public class CardRewardHelper {

    public static final int REROLL_BASE_COST = 2;
    public static final int REROLL_COST_INCREMENT = 2;

    /**
     * Platform-agnostic callback for showing the card reward dialog.
     * Returns selected cards, or null if reroll was clicked.
     */
    @FunctionalInterface
    public interface RewardDialog {
        List<PaperCard> show(String title, List<PaperCard> cards, int maxSelections,
                             String rerollLabel, boolean rerollEnabled, int gold);
    }

    /**
     * Calculate reroll gold cost for a given paid reroll index (0-indexed).
     */
    public static int getRerollCost(int paidRerollIndex) {
        return REROLL_BASE_COST + paidRerollIndex * REROLL_COST_INCREMENT;
    }

    /**
     * Build the reroll button label showing gold cost, or "Reroll (free)" if free.
     */
    public static String buildRerollLabel(int freeRerolls, int rerollCount) {
        if (rerollCount < freeRerolls) {
            return "Reroll: 0";
        }
        return "Reroll: " + getRerollCost(rerollCount - freeRerolls);
    }

    /**
     * Check if the player can afford the next reroll.
     */
    public static boolean canAffordReroll(int freeRerolls, int rerollCount, int gold) {
        if (rerollCount < freeRerolls) return true;
        return gold >= getRerollCost(rerollCount - freeRerolls);
    }

    /**
     * Run a card reward selection as part of post-match rewards.
     * MatchRewardContext applies match-scoped reward adjustments before generic card-reward effects.
     */
    public static List<PaperCard> runReward(RogueRun run, RewardDialog dialog, boolean mythicOnly,
                                            MatchRewardContext matchRewardCtx,
                                            CardRewardContext cardRewardCtx) {
        RogueDeck rogueDeck = run.getSelectedRogueDeck();
        if (rogueDeck == null) return null;

        boolean customReward = cardRewardCtx != null;
        CardRewardContext rewardCtx = cardRewardCtx != null
            ? cardRewardCtx
            : createDefaultRewardContext(mythicOnly, matchRewardCtx);
        RogueEffectComposite.INSTANCE.onCardReward(rewardCtx, run);
        CardSelectionContext selCtx = new CardSelectionContext();
        RogueEffectComposite.INSTANCE.onCardSelection(selCtx, run);
        int maxPicks = rewardCtx.maxPicks;
        int freeRerolls = customReward ? 0 : selCtx.freeRerolls;
        String title = getRewardTitle(rewardCtx, mythicOnly);

        List<PaperCard> rewardOptions;
        List<PaperCard> chosenCards;
        int rerollCount = 0;
        do {
            rewardOptions = customReward
                ? buildCustomRewardOptions(run, rewardCtx)
                : buildNormalRewardOptions(run, rogueDeck, mythicOnly, rewardCtx, selCtx);

            if (rewardOptions.isEmpty()) return null;

            String rerollLabel = customReward ? null : buildRerollLabel(freeRerolls, rerollCount);
            boolean rerollEnabled = !customReward
                && canAffordReroll(freeRerolls, rerollCount, run.getCurrentGold());
            chosenCards = dialog.show(title, rewardOptions, maxPicks, rerollLabel, rerollEnabled, run.getCurrentGold());
            if (!customReward) {
                rogueDeck.discardRewardOptions(rewardOptions);
            }

            // chosenCards null -> reroll was selected
            if (!customReward && chosenCards == null) {
                // Deduct gold for paid rerolls
                if (rerollCount >= freeRerolls) {
                    int cost = getRerollCost(rerollCount - freeRerolls);
                    run.spendGold(cost);
                }
                rerollCount++;
            }
        } while (!customReward && chosenCards == null);

        if (chosenCards == null) chosenCards = new ArrayList<>();

        if (!chosenCards.isEmpty()) {
            if (!customReward) {
                rogueDeck.removeFromCardPools(chosenCards);
            }
            run.addCardsToDeck(chosenCards, true);
        }

        return chosenCards;
    }

    private static CardRewardContext createDefaultRewardContext(boolean mythicOnly,
                                                                MatchRewardContext matchRewardCtx) {
        CardRewardContext rewardCtx = new CardRewardContext(mythicOnly ? 1 : 3);
        if (matchRewardCtx != null) {
            rewardCtx.nonMythicCardCountAdjustment = matchRewardCtx.nonMythicCardCountAdjustment;
        }
        return rewardCtx;
    }

    private static String getRewardTitle(CardRewardContext rewardCtx, boolean mythicOnly) {
        if (rewardCtx.title != null && !rewardCtx.title.isBlank()) {
            return rewardCtx.title;
        }
        return mythicOnly ? "Choose Your Mythic Reward" : "Choose Your Rewards";
    }

    private static List<PaperCard> buildNormalRewardOptions(RogueRun run, RogueDeck rogueDeck,
                                                            boolean mythicOnly, CardRewardContext rewardCtx,
                                                            CardSelectionContext selCtx) {
        int baseNonMythics;
        int baseMythics;
        if (mythicOnly) {
            baseNonMythics = 0;
            baseMythics = 3;
        } else {
            baseNonMythics = Math.max(0, 6 - selCtx.extraMythics + rewardCtx.nonMythicCardCountAdjustment);
            baseMythics = 1 + selCtx.extraMythics;
        }

        Predicate<PaperCard> notAlreadyOwned = run.getNotAlreadyInDeckPredicate();
        List<PaperCard> nonMythicCards = baseNonMythics > 0
            ? rogueDeck.drawRewardOptions(baseNonMythics,
                combineFilters(PaperCardPredicates.IS_MYTHIC_RARE.negate(), notAlreadyOwned))
            : new ArrayList<>();
        List<PaperCard> mythicCards = rogueDeck.drawRewardOptions(baseMythics,
            combineFilters(PaperCardPredicates.IS_MYTHIC_RARE, notAlreadyOwned));

        List<PaperCard> rewardOptions = new ArrayList<>();
        rewardOptions.addAll(nonMythicCards);
        rewardOptions.addAll(mythicCards);
        return rewardOptions;
    }

    private static List<PaperCard> buildCustomRewardOptions(RogueRun run, CardRewardContext rewardCtx) {
        if (rewardCtx.rewardCards == null || rewardCtx.rewardCards.isEmpty()) {
            return List.of();
        }
        Predicate<PaperCard> notAlreadyOwned = run.getNotAlreadyInDeckPredicate();
        return rewardCtx.rewardCards.stream()
            .filter(notAlreadyOwned)
            .toList();
    }

    public static <T> Predicate<T> combineFilters(Predicate<T> baseFilter,
                                                  Predicate<T> extraFilter) {
        return extraFilter == null ? baseFilter : baseFilter.and(extraFilter);
    }
}
