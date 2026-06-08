package forge.gamemodes.rogue.effect;

import forge.deck.CardPool;
import forge.gamemodes.rogue.*;
import forge.game.player.RegisteredPlayer;
import forge.gamemodes.rogue.npc.BazaarContext;
import forge.item.IPaperCard;
import forge.item.PaperCard;
import forge.util.Aggregates;
import forge.util.MyRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Interface for effects that trigger at specific points during a Rogue Commander run.
 * All methods are no-ops by default — override only the triggers relevant to each effect.
 * All state is read from RogueRun (echo boons, event traits, descension are snapshotted there).
 */
public interface RogueEffect {

    /**
     * Lifecycle type for effects.
     * ONESHOT: fire-and-forget, applied once immediately, never stored or dispatched.
     * PERMANENT: stored in run, dispatched at trigger points, persists for the whole run (=Trait)
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

    /** Tooltip text for long-lived effect displays such as the RogueMap header. */
    default String getTooltipText() {
        if (!getRawDescription().contains("{{Trait}}")) {
            return getDescription();
        }

        List<PreviewReference> cardReferences = getPreviewReferences().stream()
            .filter(reference -> reference.type() == PreviewReferenceType.CARD)
            .toList();
        if (cardReferences.size() != 1) {
            return getDescription();
        }

        PaperCard card = RogueConfig.getCard(cardReferences.get(0).token(), null, null);
        if (card == null || card.getRules() == null || card.getRules().getOracleText() == null
            || card.getRules().getOracleText().isBlank()) {
            return getDescription();
        }
        return card.getRules().getOracleText();
    }

    /** Preview references extracted from the raw description. */
    default List<PreviewReference> getPreviewReferences() { return TextHelper.extractPreviewReferences(getRawDescription()); }

    /** Optional preview card reference extracted from the raw description. */
    default String getPreviewCardName() { return TextHelper.extractFirstCardName(getRawDescription()); }

    /** Optional shared card-pool filter for effects that repeatedly use one DB-backed card selection rule. */
    default Predicate<PaperCard> getDBCardsFilter() { return null; }

    /** Optional shared deck filter for effects that repeatedly use one deck-card selection rule. */
    default Predicate<PaperCard> getDeckCardFilter() { return card -> true; }

    /** Number of charges for CONSUME effects at the given rank. -1 = permanent (default). */
    default int getChargesForRank(int rank) { return -1; }

    /** Fired once immediately for ONESHOT effects. May also grant or mutate run state. */
    default void applyEffect(RogueRun run, EffectResultContext ctx) {}

    /** Fired once when a new run is created. Adjust starting life, gold, etc. */
    default void onRunStart(RogueRun run) {}

    /** Fired after path generation. Modify nodes via run.getPath().getNodes(). */
    default void afterPathGeneration(RogueRun run) {}

    /** Fired when the path visualizer updates. Use to modify plane visibility. */
    default void onPathUpdate(PathUpdateContext ctx, RogueRun run) {}

    /** Fired once per match start. Add command zone cards, adjust hand size, etc. */
    default void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {}

    /** Fired before explicit Rogue-side life gain is applied. */
    default void onBeforeGainLife(GainLifeContext ctx, RogueRun run) {}

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

    default void addCarryCards(RogueRun run, EffectResultContext ctx, Predicate<PaperCard> filter,
                               int count, RogueRun.CarryCardType type, List<CardPrintOverride> printOverrides) {
        List<PaperCard> added = getAllCards(run, filter, count, printOverrides);
        for (PaperCard card : added) {
            run.addCarryCard(card, type, getId());
        }
        ctx.addedCards.addAll(added);
    }

    default void addCardsToDeck(RogueRun run, EffectResultContext ctx, Predicate<PaperCard> filter,
                                Integer cardCount, Integer copyCount, List<CardPrintOverride> printOverrides) {
        List<PaperCard> added = getAllCards(run, filter, cardCount, printOverrides);
        if (added.isEmpty()) {
            return;
        }

        if (copyCount != null) {
            ArrayList<PaperCard> copies = new ArrayList<>();
            for (PaperCard card : added) {
                copies.addAll(Collections.nCopies(copyCount, card));
            }

            added = copies;
        }

        run.addCardsToDeck(added, false);
        ctx.addedCards.addAll(added);
    }

    default void removeCardsFromDeck(RogueRun run, EffectResultContext ctx, Predicate<PaperCard> filter,
        Integer count) {
        List<PaperCard> removedCards;

        if (count == null) {
            removedCards = run.removeCardsFromDeck(filter);
        } else {
            removedCards = run.removeRandomCardsFromDeck(count, filter);
        }

        if (!removedCards.isEmpty()) {
            ctx.removedCards.addAll(removedCards);
        }
    }

    default void removeCarryCards(RogueRun run, EffectResultContext ctx, RogueRun.CarryCardType type) {
        if (!run.hasCarryCardOfType(type)) {
            return;
        }

        List<RogueRun.CarryCard> removedCarryCards = new ArrayList<>(run.getCarryCards().stream()
            .filter(card -> card.type() == type)
            .toList());
        run.getCarryCards().removeIf(card -> card.type() == type);

        for (RogueRun.CarryCard carryCard : removedCarryCards) {
            PaperCard card = carryCard.toPaperCard();
            if (card != null) {
                ctx.removedCards.add(card);
            }
        }
    }

    default void selectCardsForDeck(RogueRun run, EffectResultContext ctx, Predicate<PaperCard> filter,
        Integer collectionCount, Integer minCount, Integer maxCount, List<CardPrintOverride> printOverrides) {

        List<PaperCard> collection = getAllCards(run, filter, null, printOverrides);
        if (collection.isEmpty()) {
            return;
        }

        setCandidateCardsFromCollection(ctx, collection, minCount, maxCount, collectionCount);
    }

    default void selectCardsForDeck(EffectResultContext ctx, List<PaperCard> collection,
        Integer minCount, Integer maxCount) {

        setCandidateCardsFromCollection(ctx, collection, minCount, maxCount, null);
    }

    default void selectCardsFromDeck(RogueRun run, EffectResultContext ctx, Predicate<PaperCard> filter,
        int removeMinCount, int removeMaxCount, List<PaperCard> replacementCards,
        EffectResultContext.CarryCardReward replacementCarryCard) {
        List<PaperCard> candidateCards = run.getSelectableDeckCards(filter);
        if (candidateCards.isEmpty()) {
            return;
        }

        ctx.candidateCards = candidateCards;
        ctx.cardSelectionMinCount = Math.min(removeMinCount, candidateCards.size());
        ctx.cardSelectionMaxCount = Math.min(removeMaxCount, candidateCards.size());
        ctx.replacementCards = replacementCards;
        ctx.replacementCarryCard = replacementCarryCard;
        if (ctx.cardSelectionMaxCount > 0) {
            ctx.trigger = EffectResultContext.ActionTriggerType.CARD_REMOVAL;
        }
    }

    default void triggerCustomBazaar(EffectResultContext ctx, String title, List<PaperCard> inventory, Map<String, Integer> priceOverrides) {
        BazaarContext bazaarContext = new BazaarContext();
        bazaarContext.title = title;
        bazaarContext.inventory.addAll(inventory);

        if (priceOverrides != null) {
            bazaarContext.priceOverrides = priceOverrides;
        }

        ctx.bazaarContext = bazaarContext;
        ctx.trigger = EffectResultContext.ActionTriggerType.BAZAAR;
    }

    default void triggerChest(EffectResultContext ctx) {
        ctx.trigger = EffectResultContext.ActionTriggerType.CHEST;
    }

    default void triggerSanctum(EffectResultContext ctx) {
        ctx.trigger = EffectResultContext.ActionTriggerType.SANCTUM;
    }

    default void triggerPlanebound(EffectResultContext ctx, RoguePlaneboundType type) {
        List<RoguePlanebound> all = RogueConfig.loadPlanebounds();
        all.removeIf(p -> p.type() != type);
        Collections.shuffle(all);
        if (all.isEmpty()) {
            return;
        }

        RoguePlanebound opponent = all.get(0);
        List<PaperCard> planes = RogueConfig.getAllPlanes().toFlatList();
        Collections.shuffle(planes);
        String randomPlaneName = planes.isEmpty() ? opponent.planeName() : planes.get(0).getName();

        ctx.planebound = new RoguePlanebound(randomPlaneName, opponent.planeboundName(),
            opponent.deckPath(), opponent.avatarIndex(), opponent.type());
        ctx.trigger = EffectResultContext.ActionTriggerType.PLANEBOUND;
    }

    default int rollD20() {
        return MyRandom.getRandom().nextInt(20) + 1;
    }

    default void gainWound(RogueRun run, EffectResultContext ctx) {
        List<WoundEffect> available = new ArrayList<>(List.of(WoundEffect.values()));
        List<RogueEffect> active = run.getActiveWoundEffects();
        available.removeIf(w -> active.stream().anyMatch(a -> a == w));
        if (available.isEmpty()) return;
        WoundEffect woundEffect = available.get(MyRandom.getRandom().nextInt(available.size()));
        run.addWound(woundEffect);
        ctx.gainedWoundEffect = woundEffect;
    }

    static void addCardToCommandZone(String cardName, RegisteredPlayer human) {
        PaperCard card = RogueConfig.getCard(cardName, null, null);
        addCardToCommandZone(card, human);
    }

    static void addCardToCommandZone(PaperCard card, RegisteredPlayer human) {
        if (card != null) human.addExtraCardsInCommandZone(Collections.singletonList(card));
    }

    static void addCardToBattlefield(String cardName, RegisteredPlayer human) {
        PaperCard card = RogueConfig.getCard(cardName, null, null);
        addCardToBattlefield(card, human);
    }

    static void addCardToBattlefield(PaperCard card, RegisteredPlayer human) {
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

    // Load all cards needed for effect
    private static List<PaperCard> getAllCards(RogueRun run, Predicate<PaperCard> filter,
        Integer count, List<CardPrintOverride> printOverrides) {

        List<PaperCard> result = run.getAllCardsForActiveCommander(filter);
        if (result.isEmpty()) {
            return List.of();
        }

        List<PaperCard> selectedCards = getSubsetFromCollection(result, count);
        if (selectedCards.isEmpty()) {
            return List.of();
        }

        selectedCards = setExactPrints(selectedCards, printOverrides);
        return selectedCards;
    }

    // Get random subset of cards if specific count was passed, otherwise return ALL cards
    private static List<PaperCard> getSubsetFromCollection(List<PaperCard> cards, Integer count) {
        if (cards.isEmpty()) {
            return List.of();
        }

        if (count == null) {
            return new ArrayList<>(cards);
        }

        if (count <= 0) {
            return List.of();
        }

        if (count == 1) {
            PaperCard selected = Aggregates.random(cards);
            return selected == null ? List.of() : new ArrayList<>(List.of(selected));
        }

        Collections.shuffle(cards, MyRandom.getRandom());
        return new ArrayList<>(cards.subList(0, Math.min(count, cards.size())));
    }

    private static void setCandidateCardsFromCollection(EffectResultContext ctx, List<PaperCard> collection,
        Integer minCount, Integer maxCount, Integer collectionCount) {

        Collections.shuffle(collection, MyRandom.getRandom());

        List<PaperCard> candidates;
        if (collectionCount != null) {
            candidates = new ArrayList<>(collection.subList(0, Math.min(collectionCount, collection.size())));
        } else {
            candidates = collection;
        }

        ctx.candidateCards = candidates;
        ctx.cardSelectionMinCount = Math.min(minCount, candidates.size());
        ctx.cardSelectionMaxCount = Math.min(maxCount, candidates.size());
        if (ctx.cardSelectionMaxCount > 0) {
            ctx.trigger = EffectResultContext.ActionTriggerType.CARD_ADDITION;
        }
    }

    // Override found cards with specific set / art if requested
    private static List<PaperCard> setExactPrints(List<PaperCard> cards, List<CardPrintOverride> printOverrides) {
        if (cards == null || cards.isEmpty() || printOverrides == null || printOverrides.isEmpty()) {
            return cards;
        }

        List<PaperCard> exactPrints = new ArrayList<>(cards.size());
        for (PaperCard card : cards) {
            PaperCard exactCard = card;
            for (CardPrintOverride printOverride : printOverrides) {
                if (!printOverride.matches(card)) {
                    continue;
                }

                PaperCard resolved = RogueConfig.getCard(printOverride.cardName(),
                    printOverride.setCode(), printOverride.artIndex());
                if (resolved != null) {
                    exactCard = resolved;
                }
                break;
            }
            exactPrints.add(exactCard);
        }
        return exactPrints;
    }
}
