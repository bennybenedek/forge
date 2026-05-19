package forge.gamemodes.rogue.effect;

import forge.deck.CardPool;
import forge.gamemodes.rogue.PreviewReference;
import forge.game.player.RegisteredPlayer;
import forge.gamemodes.rogue.RogueConfig;
import forge.gamemodes.rogue.RogueRun;
import forge.gamemodes.rogue.TextHelper;
import forge.item.IPaperCard;
import forge.item.PaperCard;
import forge.model.FModel;
import java.util.Collections;
import java.util.List;

/**
 * Interface for effects that trigger at specific points during a Rogue Commander run.
 * All methods are no-ops by default — override only the triggers relevant to each effect.
 * All state is read from RogueRun (echo boons, event boons, descension are snapshotted there).
 */
public interface RogueEffect {

    /**
     * Lifecycle type for effects.
     * ONESHOT: fire-and-forget, applied once immediately, never stored or dispatched.
     * PERMANENT: stored in run, dispatched at trigger points, persists for the whole run.
     * CONSUME: stored in run, dispatched at trigger points, removed after all charges consumed
     */
    enum EffectType { ONESHOT, PERMANENT, CONSUME }

    default EffectType getEffectType() { return EffectType.PERMANENT; }

    /** Unique identifier for serialization and lookup. Override in concrete types. */
    default String getId() { return ""; }

    /** Display name for UI. Override in concrete types. */
    default String getDisplayName() { return ""; }

    /** Raw description for preview-aware UI. Override in concrete types. */
    default String getRawDescription() { return ""; }

    /** Description for tooltips and display text. */
    default String getDescription() { return TextHelper.stripPreviewMarkers(getRawDescription()); }

    /** Preview references extracted from the raw description. */
    default List<PreviewReference> getPreviewReferences() { return TextHelper.extractPreviewReferences(getRawDescription()); }

    /** Optional preview card reference extracted from the raw description. */
    default String getPreviewCardName() { return TextHelper.extractFirstCardName(getRawDescription()); }

    /** Number of charges for CONSUME effects at the given rank. -1 = permanent (default). */
    default int getChargesForRank(int rank) { return -1; }

    /** Fired when this effect is added to a run. Use to initialize carry cards, etc. */
    default void onGranted(RogueRun run) {}

    /** Fired once when a new run is created. Adjust starting life, gold, etc. */
    default void onRunStart(RogueRun run) {}

    /** Fired after path generation. Modify nodes via run.getPath().getNodes(). */
    default void afterPathGeneration(RogueRun run) {}

    /** Fired when the path visualizer updates. Use to modify plane visibility. */
    default void onPathUpdate(PathUpdateContext ctx, RogueRun run) {}

    /** Fired once per match start. Add command zone cards, adjust hand size, etc. */
    default void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {}

    /** Fired after winning a match (non-final node). Heal life, etc. */
    default void onMatchWin(RogueRun run) {}

    /** Fired when the player would lose the run. */
    default void onDefeat(DefeatContext ctx, RogueRun run) {}

    /** Fired before match rewards are given after a win. Use to modify or skip them. */
    default void onBeforeRewards(MatchRewardContext ctx, RogueRun run) {}

    /** Fired for card reward node selections. */
    default void onCardReward(CardRewardContext ctx, RogueRun run) {}

    /** Fired for both card reward and bazaar selections. */
    default void onCardSelection(CardSelectionContext ctx, RogueRun run) {}

    /** Resolves a card by name (ensures rogue card scripts are loaded). */
    static PaperCard loadCard(String cardName) {
        RogueConfig.loadRogueCards();
        return FModel.getMagicDb().getCommonCards().getCard(cardName);
    }

    static void addCardToCommandZone(String cardName, RegisteredPlayer human) {
        PaperCard card = loadCard(cardName);
        if (card != null) human.addExtraCardsInCommandZone(Collections.singletonList(card));
    }

    static void addCardToBattlefield(String cardName, RegisteredPlayer human) {
        PaperCard card = loadCard(cardName);
        if (card != null) human.addExtraCardsOnBattlefield(Collections.singletonList(card));
    }

    /**
     * Moves cards from the player's match deck to the starting battlefield.
     * Removes each card from the deck's main section so it doesn't also appear in the library.
     * Always use this instead of addExtraCardsOnBattlefield directly.
     */
    static void moveCardsFromDeckToBattlefield(List<IPaperCard> cards, RegisteredPlayer human) {
        CardPool main = human.getDeck().getMain();
        for (IPaperCard card : cards)
            main.remove((PaperCard) card, 1);
        human.addExtraCardsOnBattlefield(cards);
    }
}
