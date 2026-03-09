package forge.gamemodes.rogue;

import forge.game.player.RegisteredPlayer;
import java.util.List;

/**
 * Composite dispatcher implementing RogueRunEffect.
 * Iterates active effects from all relevant sources, firing triggers on each.
 */
public enum RogueEffectComposite implements RogueEffect {

    INSTANCE;

    @Override
    public void onMatchStart(RegisteredPlayer human, RogueRun run, RogueMetaProgress progress) {
        for (EchoBoon boon : progress.getActiveBoons())
            boon.onMatchStart(human, run, progress);
        int descLevel = run.getDescensionLevel();
        for (int l = 1; l <= descLevel; l++) {
            DescensionLevel dl = DescensionLevel.forLevel(l);
            if (dl != null) dl.onMatchStart(human, run, progress);
        }
    }

    @Override
    public void onRunStart(RogueRun run, RogueMetaProgress progress) {
        for (EchoBoon boon : progress.getActiveBoons())
            boon.onRunStart(run, progress);
    }

    @Override
    public void onMatchWin(RogueRun run, RogueMetaProgress progress) {
        for (EchoBoon boon : progress.getActiveBoons())
            boon.onMatchWin(run, progress);
    }

    @Override
    public void onDefeat(DefeatContext ctx, RogueRun run, RogueMetaProgress progress) {
        for (EchoBoon boon : progress.getActiveBoons()) {
            boon.onDefeat(ctx, run, progress);
            if (ctx.revived) return;
        }
    }

    @Override
    public void onCardReward(CardRewardContext ctx, RogueRun run, RogueMetaProgress progress) {
        for (EchoBoon boon : progress.getActiveBoons())
            boon.onCardReward(ctx, run, progress);
    }

    @Override
    public void onCardSelection(CardSelectionContext ctx, RogueRun run, RogueMetaProgress progress) {
        for (EchoBoon boon : progress.getActiveBoons())
            boon.onCardSelection(ctx, run, progress);
    }

    @Override
    public void afterPathGeneration(List<RoguePathNode> nodes, int descensionLevel) {
        for (int l = 1; l <= descensionLevel; l++) {
            DescensionLevel dl = DescensionLevel.forLevel(l);
            if (dl != null) dl.afterPathGeneration(nodes, descensionLevel);
        }
    }
}
