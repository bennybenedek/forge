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
 */
public interface RogueEffect {
    /** Fired once per match start. Add command zone cards, adjust hand size, etc. */
    default void onMatchStart(RegisteredPlayer human, RogueRun run, RogueMetaProgress progress) {}

    /** Fired once when a new run is created. Adjust starting life, gold, etc. */
    default void onRunStart(RogueRun run, RogueMetaProgress progress) {}

    /** Fired after winning a match (non-final node). Heal life, etc. */
    default void onMatchWin(RogueRun run, RogueMetaProgress progress) {}

    /** Fired when the player would lose the run. Set ctx.revived=true to prevent run failure. */
    default void onDefeat(DefeatContext ctx, RogueRun run, RogueMetaProgress progress) {}

    /** Fired for card reward node selections. Adjust maxPicks. */
    default void onCardReward(CardRewardContext ctx, RogueRun run, RogueMetaProgress progress) {}

    /** Fired for both card reward and bazaar selections. Adjust extraMythics, rerolls. */
    default void onCardSelection(CardSelectionContext ctx, RogueRun run, RogueMetaProgress progress) {}

    /** Fired after path generation. Swap/replace nodes based on descension level. */
    default void afterPathGeneration(List<RoguePathNode> nodes, int descensionLevel) {}

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
