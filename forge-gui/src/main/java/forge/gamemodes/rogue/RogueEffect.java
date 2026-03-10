package forge.gamemodes.rogue;

import forge.deck.CardPool;
import forge.game.player.RegisteredPlayer;
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
     * ONESHOT: fire-and-forget, applied once at choice time, never stored or dispatched.
     * PERMANENT: stored in run, dispatched at trigger points, persists for the whole run.
     * CONSUME: stored in run, dispatched at trigger points, removed after triggering.
     */
    enum EffectType { ONESHOT, PERMANENT, CONSUME }

    default EffectType getEffectType() { return EffectType.PERMANENT; }

    /** Number of charges for CONSUME effects at the given rank. -1 = permanent (default). */
    default int getChargesForRank(int rank) { return -1; }

    /** Fired once per match start. Add command zone cards, adjust hand size, etc. */
    default void onMatchStart(RegisteredPlayer human, RogueRun run) {}

    /** Fired once when a new run is created. Adjust starting life, gold, etc. */
    default void onRunStart(RogueRun run) {}

    /** Fired after winning a match (non-final node). Heal life, etc. */
    default void onMatchWin(RogueRun run) {}

    /** Fired when the player would lose the run. */
    default void onDefeat(DefeatContext ctx, RogueRun run) {}

    /** Fired for card reward node selections. */
    default void onCardReward(CardRewardContext ctx, RogueRun run) {}

    /** Fired for both card reward and bazaar selections. */
    default void onCardSelection(CardSelectionContext ctx, RogueRun run) {}

    /** Fired after path generation. Modify nodes via run.getPath().getNodes(). */
    default void afterPathGeneration(RogueRun run) {}

    /**
     * Loads custom rogue card scripts and adds the named card to the command zone.
     * Use this instead of calling FModel.getMagicDb() directly for rogue-specific cards.
     */
    static void addCustomCardToCommandZone(String cardName, RegisteredPlayer human) {
        RogueConfig.loadRogueCards();
        PaperCard card = FModel.getMagicDb().getCommonCards().getCard(cardName);
        if (card != null) human.addExtraCardsInCommandZone(Collections.singletonList(card));
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
