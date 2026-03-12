package forge.gamemodes.rogue.effect;

import forge.game.player.RegisteredPlayer;
import forge.gamemodes.rogue.RogueRun;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Composite dispatcher implementing RogueEffect.
 * Iterates active effects from all sources (echo boons, descension levels, event boons).
 */
public enum RogueEffectComposite implements RogueEffect {

    INSTANCE;

    private static List<RogueEffect> getAllEffects(RogueRun run) {
        List<RogueEffect> effects = new ArrayList<>();
        effects.addAll(run.getActiveEchoBoons());
        int descLevel = run.getDescensionLevel();
        for (int l = 1; l <= descLevel; l++) {
            DescensionLevel dl = DescensionLevel.forLevel(l);
            if (dl != null) effects.add(dl);
        }
        effects.addAll(run.getActiveEventBoons());
        effects.addAll(run.getActiveChestBoons());
        return effects;
    }

    private static void forEachEffect(RogueRun run, Consumer<RogueEffect> action) {
        for (RogueEffect e : getAllEffects(run))
            action.accept(e);
    }

    @Override
    public void onMatchStart(RegisteredPlayer human, RogueRun run) {
        forEachEffect(run, e -> e.onMatchStart(human, run));
    }

    @Override
    public void onRunStart(RogueRun run) {
        forEachEffect(run, e -> e.onRunStart(run));
    }

    @Override
    public void onMatchWin(RogueRun run) {
        forEachEffect(run, e -> e.onMatchWin(run));
    }

    @Override
    public void onDefeat(DefeatContext ctx, RogueRun run) {
        for (RogueEffect e : getAllEffects(run)) {
            e.onDefeat(ctx, run);
            if (ctx.revived) return;
        }
    }

    @Override
    public void onCardReward(CardRewardContext ctx, RogueRun run) {
        forEachEffect(run, e -> e.onCardReward(ctx, run));
    }

    @Override
    public void onCardSelection(CardSelectionContext ctx, RogueRun run) {
        forEachEffect(run, e -> e.onCardSelection(ctx, run));
    }

    @Override
    public void afterPathGeneration(RogueRun run) {
        forEachEffect(run, e -> e.afterPathGeneration(run));
    }
}
