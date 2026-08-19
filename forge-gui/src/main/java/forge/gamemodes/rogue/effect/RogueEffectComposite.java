package forge.gamemodes.rogue.effect;

import forge.game.player.RegisteredPlayer;
import forge.gamemodes.rogue.RogueRun;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Composite dispatcher implementing RogueEffect.
 * Iterates active effects from all sources (e.g. echo, descension, event).
 */
public enum RogueEffectComposite implements RogueEffect {

    INSTANCE;

    /** Returns all active effects from all sources */
    public static List<RogueEffect> getAllEffects(RogueRun run) {
      List<RogueEffect> effects = new ArrayList<>(run.getActiveEchoEffects());
        int descLevel = run.getDescensionLevel();
        for (int l = 1; l <= descLevel; l++) {
            DescensionLevel dl = DescensionLevel.forLevel(l);
            if (dl != null) effects.add(dl);
        }
        effects.addAll(run.getActiveEventEffects());
        effects.addAll(run.getActiveChestEffects());
        effects.addAll(run.getActiveWoundEffects());
        effects.addAll(run.getActiveWrathfulEffects());
        effects.addAll(run.getActiveCursedEffects());
        effects.addAll(run.getActiveNPCEffects());
        return effects;
    }

    private static void forEachEffect(RogueRun run, Consumer<RogueEffect> action) {
        for (RogueEffect e : getAllEffects(run))
            action.accept(e);
    }

    @Override
    public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
        forEachEffect(run, e -> e.onMatchStart(human, opponent, run));
    }

    @Override
    public void onRunStart(RogueRun run) {
        forEachEffect(run, e -> e.onRunStart(run));
    }

    @Override
    public void onBeforeGainLife(GainLifeContext ctx, RogueRun run) {
        forEachEffect(run, e -> e.onBeforeGainLife(ctx, run));
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

    @Override
    public void onBeforeRewards(MatchRewardContext ctx, RogueRun run) {
        forEachEffect(run, e -> e.onBeforeRewards(ctx, run));
    }

    @Override
    public void onPathUpdate(PathUpdateContext ctx, RogueRun run) {
        forEachEffect(run, e -> e.onPathUpdate(ctx, run));
    }

    @Override
    public void onBeforeSanctum(SanctumContext ctx, RogueRun run) {
        forEachEffect(run, e -> e.onBeforeSanctum(ctx, run));
    }

    @Override
    public void onBeforeBazaar(BazaarContext ctx, RogueRun run) {
        forEachEffect(run, e -> e.onBeforeBazaar(ctx, run));
    }

    @Override
    public void onSanctumChoice(SanctumContext.SanctumChoice choice, RogueRun run) {
        forEachEffect(run, e -> e.onSanctumChoice(choice, run));
    }
}
