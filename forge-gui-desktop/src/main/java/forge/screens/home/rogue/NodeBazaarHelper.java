package forge.screens.home.rogue;

import forge.card.CardRarity;
import forge.card.CardSplitType;
import forge.deck.DeckFormat;
import forge.gamemodes.rogue.BazaarPricing;
import forge.gamemodes.rogue.CardRewardHelper;
import forge.gamemodes.rogue.RogueConfig;
import forge.gamemodes.rogue.RogueDeck;
import forge.gamemodes.rogue.RogueMetaProgress;
import forge.gamemodes.rogue.RogueRun;
import forge.gamemodes.rogue.RogueTutorial;
import forge.gamemodes.rogue.effect.BazaarContext;
import forge.gamemodes.rogue.effect.BazaarItem;
import forge.gamemodes.rogue.effect.CardSelectionContext;
import forge.gamemodes.rogue.effect.ChestEffect;
import forge.gamemodes.rogue.effect.NPCEffect;
import forge.gamemodes.rogue.effect.RogueEffect;
import forge.gamemodes.rogue.effect.RogueEffectComposite;
import forge.gamemodes.rogue.npc.NPCContext;
import forge.gamemodes.rogue.npc.NPCEncounterComposite;
import forge.gamemodes.rogue.path.NodeBazaar;
import forge.item.PaperCard;
import forge.item.PaperCardPredicates;
import forge.localinstance.achievements.RogueCommanderAchievements;
import forge.util.MyRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import org.tinylog.Logger;

class NodeBazaarHelper {

    private final CSubmenuRogueMap map;

    private record BazaarDialogResult(Set<BazaarItem> selectedItems, int selectedTabIndex) {
        private boolean isReroll() {
            return selectedItems == null;
        }
    }

    NodeBazaarHelper(CSubmenuRogueMap map) {
        this.map = map;
    }

    void handleBazaarNode(NodeBazaar bazaarNode, RogueRun currentRun) {
        if (currentRun == null) {
            return;
        }

        runBazaarShopping(currentRun, null);
        RogueCommanderAchievements.instance.evaluateRunAchievements(currentRun);
        map.completeSideNode(bazaarNode);
    }

    List<PaperCard> runBazaarShopping(RogueRun currentRun, BazaarContext ctx) {
        RogueDeck rogueDeck = currentRun.getSelectedRogueDeck();
        if (rogueDeck == null) {
            Logger.error("Could not find rogue deck for current run.");
            return List.of();
        }

        return ctx != null
            ? runCustomBazaarShopping(currentRun, rogueDeck, ctx)
            : runOrdinaryBazaarShopping(currentRun, rogueDeck);
    }

    private List<PaperCard> runCustomBazaarShopping(RogueRun currentRun, RogueDeck rogueDeck,
                                                    BazaarContext bazaarCtx) {
        List<BazaarItem> inventory = buildCustomBazaarInventory(bazaarCtx);
        if (inventory.isEmpty()) {
            Logger.error("No cards available in Bazaar inventory.");
            return List.of();
        }

        BazaarDialogResult dialogResult = showBazaarDialog(inventory, currentRun, bazaarCtx, null, false, 0);
        return applyBazaarPurchases(currentRun, rogueDeck, bazaarCtx, dialogResult.selectedItems(), true);
    }

    private List<PaperCard> runOrdinaryBazaarShopping(RogueRun currentRun, RogueDeck rogueDeck) {
        BazaarContext bazaarCtx = createOrdinaryBazaarContext();
        RogueTutorialHelper.showIfNotSeen(RogueTutorial.BAZAAR);
        NPCEncounterComposite.INSTANCE.onBeforeBazaar(bazaarCtx, currentRun, RogueMetaProgress.getInstance());

        CardSelectionContext selCtx = createBazaarSelectionContext(currentRun);
        int freeRerolls = selCtx.freeRerolls;
        int rerollCount = 0;
        int selectedBazaarTab = 0;
        BazaarDialogResult dialogResult;
        do {
            List<BazaarItem> inventory = buildOrdinaryBazaarRollInventory(currentRun, rogueDeck, bazaarCtx, selCtx);
            if (inventory.isEmpty()) {
                Logger.error("No cards available in Bazaar inventory.");
                return List.of();
            }

            String rerollLabel = CardRewardHelper.buildRerollLabel(freeRerolls, rerollCount);
            boolean rerollEnabled =
                CardRewardHelper.canAffordReroll(freeRerolls, rerollCount, currentRun.getCurrentGold());
            dialogResult =
                showBazaarDialog(inventory, currentRun, bazaarCtx, rerollLabel, rerollEnabled, selectedBazaarTab);
            selectedBazaarTab = dialogResult.selectedTabIndex();
            rogueDeck.discardRewardOptions(getRewardPoolCards(inventory));

            if (dialogResult.isReroll()) {
                spendRerollCostIfNeeded(currentRun, freeRerolls, rerollCount);
                rerollCount++;
            }
        } while (dialogResult.isReroll());

        List<PaperCard> purchasedDeckCards = applyBazaarPurchases(currentRun, rogueDeck, bazaarCtx,
            dialogResult.selectedItems(), false);
        showNpcBazaarPurchaseDialogs(bazaarCtx);
        return purchasedDeckCards;
    }

    private List<BazaarItem> buildOrdinaryBazaarRollInventory(RogueRun currentRun, RogueDeck rogueDeck,
                                                              BazaarContext bazaarCtx,
                                                              CardSelectionContext selCtx) {
        List<BazaarItem> inventory = buildOrdinaryBazaarInventory(currentRun, rogueDeck, selCtx);
        applyBazaarDiscounts(bazaarCtx, inventory);
        addContextItemsToOrdinaryInventory(bazaarCtx, inventory);
        addGeneratedSpecialItems(currentRun, bazaarCtx, inventory);
        return inventory;
    }

    private BazaarDialogResult showBazaarDialog(List<BazaarItem> inventory, RogueRun currentRun,
                                                BazaarContext bazaarCtx, String rerollLabel,
                                                boolean rerollEnabled, int selectedBazaarTab) {
        BazaarDialog dialog = new BazaarDialog(
            inventory,
            currentRun.getCurrentGold(),
            bazaarCtx.title,
            rerollLabel,
            selectedBazaarTab);
        dialog.setRerollEnabled(rerollEnabled);
        Set<BazaarItem> selectedItems = dialog.show();
        return new BazaarDialogResult(selectedItems, dialog.getSelectedTabIndex());
    }

    private void spendRerollCostIfNeeded(RogueRun currentRun, int freeRerolls, int rerollCount) {
        if (rerollCount < freeRerolls) {
            return;
        }

        int cost = CardRewardHelper.getRerollCost(rerollCount - freeRerolls);
        currentRun.spendGold(cost);
    }

    private void showNpcBazaarPurchaseDialogs(BazaarContext bazaarCtx) {
        for (NPCContext npcContext : NPCEncounterComposite.INSTANCE.onAfterBazaarPurchase(
            bazaarCtx, RogueMetaProgress.getInstance())) {
            new NPCDialog(npcContext).show();
        }
    }

    private BazaarContext createOrdinaryBazaarContext() {
        BazaarContext bazaarCtx = new BazaarContext();
        bazaarCtx.title = "Bazaar";
        return bazaarCtx;
    }

    private CardSelectionContext createBazaarSelectionContext(RogueRun currentRun) {
        CardSelectionContext selCtx = new CardSelectionContext();
        RogueEffectComposite.INSTANCE.onCardSelection(selCtx, currentRun);
        return selCtx;
    }

    private List<BazaarItem> buildOrdinaryBazaarInventory(RogueRun currentRun, RogueDeck rogueDeck,
                                                         CardSelectionContext selCtx) {
        int baseNonMythics = 8;
        int baseMythics = 2;
        int totalNonMythics = Math.max(0, baseNonMythics - selCtx.extraMythics);
        int totalMythics = baseMythics + selCtx.extraMythics;

        Predicate<PaperCard> notAlreadyOwned = currentRun.getNotAlreadyInDeckPredicate();
        List<PaperCard> nonMythicCards = rogueDeck.drawRewardOptions(totalNonMythics,
            CardRewardHelper.combineFilters(PaperCardPredicates.IS_MYTHIC_RARE.negate(), notAlreadyOwned));
        List<PaperCard> mythicCards = rogueDeck.drawRewardOptions(totalMythics,
            CardRewardHelper.combineFilters(PaperCardPredicates.IS_MYTHIC_RARE, notAlreadyOwned));

        List<BazaarItem> inventory = new ArrayList<>();
        for (PaperCard card : nonMythicCards) {
            inventory.add(BazaarItem.forCard(card));
        }
        for (PaperCard card : mythicCards) {
            inventory.add(BazaarItem.forCard(card));
        }
        return inventory;
    }

    private List<BazaarItem> buildCustomBazaarInventory(BazaarContext bazaarCtx) {
        List<BazaarItem> inventory = new ArrayList<>(bazaarCtx.inventory);
        if (inventory.size() <= BazaarDialog.MAX_DISPLAY_CARDS) {
            return inventory;
        }

        Collections.shuffle(inventory);
        return new ArrayList<>(inventory.subList(0, BazaarDialog.MAX_DISPLAY_CARDS));
    }

    private void applyBazaarDiscounts(BazaarContext bazaarCtx, List<BazaarItem> inventory) {
        if (bazaarCtx.discountCount <= 0 || inventory.isEmpty()) {
            return;
        }

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).type() == BazaarItem.Type.CARD) {
                indices.add(i);
            }
        }
        Collections.shuffle(indices);
        for (int i = 0; i < Math.min(bazaarCtx.discountCount, indices.size()); i++) {
            BazaarItem item = inventory.get(indices.get(i));
            PaperCard card = item.card();
            int basePrice = BazaarPricing.getCardPrice(card);
            int discounted = calculateDiscountedPrice(basePrice, bazaarCtx.discountAmount);
            inventory.set(indices.get(i), item.withPriceOverride(discounted));
        }
    }

    private int calculateDiscountedPrice(int basePrice, int discountAmount) {
        return Math.max(0, basePrice - discountAmount);
    }

    private void addContextItemsToOrdinaryInventory(BazaarContext bazaarCtx, List<BazaarItem> inventory) {
        for (BazaarItem item : bazaarCtx.inventory) {
            if (item.type() == BazaarItem.Type.CURIO) {
                removeLastCardItem(inventory);
            }
            inventory.add(item);
        }
    }

    private void removeLastCardItem(List<BazaarItem> inventory) {
        for (int i = inventory.size() - 1; i >= 0; i--) {
            if (inventory.get(i).type() == BazaarItem.Type.CARD) {
                inventory.remove(i);
                return;
            }
        }
    }

    private void addGeneratedSpecialItems(RogueRun currentRun, BazaarContext bazaarCtx,
                                          List<BazaarItem> inventory) {
        int traitOffers = 5;
        int carryCardOffers = 5;
        int traitGoldCost = 6;

        if (bazaarCtx.offersTraits) {
            addTraitOffers(currentRun, inventory, traitOffers, traitGoldCost);
        }
        if (bazaarCtx.offersCarryCards) {
            addCarryCardOffers(currentRun, inventory, carryCardOffers);
        }
        applySpecialBazaarDiscounts(bazaarCtx, inventory);
    }

    private void applySpecialBazaarDiscounts(BazaarContext bazaarCtx, List<BazaarItem> inventory) {
        if (bazaarCtx.specialDiscountCount <= 0 || inventory.isEmpty()) {
            return;
        }

        List<Integer> traitIndices = new ArrayList<>();
        List<Integer> carryCardIndices = new ArrayList<>();
        for (int i = 0; i < inventory.size(); i++) {
            BazaarItem item = inventory.get(i);
            if (item.type() == BazaarItem.Type.TRAIT) {
                traitIndices.add(i);
            } else if (item.type() == BazaarItem.Type.CARRY_CARD) {
                carryCardIndices.add(i);
            }
        }
        Collections.shuffle(traitIndices, MyRandom.getRandom());
        Collections.shuffle(carryCardIndices, MyRandom.getRandom());

        for (int i = 0; i < bazaarCtx.specialDiscountCount; i++) {
            boolean preferTraits = MyRandom.getRandom().nextBoolean();
            int selectedIndex = preferTraits
                ? drawSpecialDiscountIndex(traitIndices, carryCardIndices)
                : drawSpecialDiscountIndex(carryCardIndices, traitIndices);
            if (selectedIndex < 0) {
                return;
            }
            inventory.set(selectedIndex, getDiscountedSpecialItem(bazaarCtx, inventory.get(selectedIndex)));
        }
    }

    private int drawSpecialDiscountIndex(List<Integer> preferredIndices, List<Integer> fallbackIndices) {
        if (!preferredIndices.isEmpty()) {
            return preferredIndices.remove(preferredIndices.size() - 1);
        }
        if (!fallbackIndices.isEmpty()) {
            return fallbackIndices.remove(fallbackIndices.size() - 1);
        }
        return -1;
    }

    private BazaarItem getDiscountedSpecialItem(BazaarContext bazaarCtx, BazaarItem item) {
        if (item.type() == BazaarItem.Type.TRAIT) {
            int traitDiscountPrice = 3;
            return item.withPriceOverride(traitDiscountPrice);
        }
        if (item.type() == BazaarItem.Type.CARRY_CARD) {
            int discounted = calculateDiscountedPrice(item.getBasePrice(), bazaarCtx.discountAmount);
            return item.withPriceOverride(discounted);
        }
        return item;
    }

    private void addTraitOffers(RogueRun currentRun, List<BazaarItem> inventory, int offerCount, int price) {
        List<BazaarItem> offers = new ArrayList<>();
        Set<String> activeChestEffectIds = getActiveEffectIds(currentRun.getActiveChestEffects());
        for (ChestEffect effect : ChestEffect.values()) {
            if (effect.getEffectType() != RogueEffect.EffectType.PERMANENT
                || activeChestEffectIds.contains(effect.getId())) {
                continue;
            }
            addTraitOffer(offers, effect, price);
        }

        Set<String> activeNpcEffectIds = getActiveEffectIds(currentRun.getActiveNPCEffects());
        for (NPCEffect effect : NPCEffect.values()) {
            if (effect.getEffectType() != RogueEffect.EffectType.PERMANENT
                || activeNpcEffectIds.contains(effect.getId())) {
                continue;
            }
            addTraitOffer(offers, effect, price);
        }

        Collections.shuffle(offers, MyRandom.getRandom());
        for (int i = 0; i < Math.min(offerCount, offers.size()); i++) {
            inventory.add(offers.get(i));
        }
    }

    private void addTraitOffer(List<BazaarItem> offers, RogueEffect effect, int price) {
        PaperCard effectCard = effect.getEffectCard();
        if (effectCard != null) {
            offers.add(BazaarItem.forTrait(effectCard, effect, price));
        }
    }

    private Set<String> getActiveEffectIds(List<RogueEffect> activeEffects) {
        Set<String> activeIds = new HashSet<>();
        for (RogueEffect activeEffect : activeEffects) {
            activeIds.add(activeEffect.getId());
        }
        return activeIds;
    }

    private void addCarryCardOffers(RogueRun currentRun, List<BazaarItem> inventory, int offerCount) {
        List<PaperCard> candidates = new ArrayList<>(RogueConfig.getAllCards(
            NodeBazaarHelper::isCarryCardCandidate).stream()
            .filter(card -> !card.hasFlavorName())
            .toList());
        candidates.removeIf(card -> !currentRun.canAddCardAsCarryCard(card));

        // Bucket first so the final offer mix is not dominated by the much larger creature pool.
        Map<RogueRun.CarryCardType, List<PaperCard>> candidatesByType =
            new EnumMap<>(RogueRun.CarryCardType.class);
        for (RogueRun.CarryCardType type : RogueRun.CarryCardType.values()) {
            candidatesByType.put(type, new ArrayList<>());
        }
        for (PaperCard card : candidates) {
            candidatesByType.get(getCarryCardType(card)).add(card);
        }
        for (List<PaperCard> typedCandidates : candidatesByType.values()) {
            Collections.shuffle(typedCandidates, MyRandom.getRandom());
        }

        List<PaperCard> selectedCards = selectCarryCardOffers(candidatesByType, offerCount);
        for (PaperCard card : selectedCards) {
            PaperCard preferredPrint = RogueConfig.getPreferredPrint(card);
            if (preferredPrint != null) {
                inventory.add(BazaarItem.forCarryCard(preferredPrint, getCarryCardType(preferredPrint)));
            }
        }
    }

    private List<PaperCard> selectCarryCardOffers(
        Map<RogueRun.CarryCardType, List<PaperCard>> candidatesByType, int offerCount) {
        RogueRun.CarryCardType[] types = {
            RogueRun.CarryCardType.SCROLL,
            RogueRun.CarryCardType.ITEM,
            RogueRun.CarryCardType.FELLOW
        };
        RogueRun.CarryCardType oneCardType = types[MyRandom.getRandom().nextInt(types.length)];

        // Randomize which bucket receives one slot; the other two receive two each.
        List<PaperCard> selectedCards = new ArrayList<>();
        for (RogueRun.CarryCardType type : types) {
            int count = type == oneCardType ? 1 : 2;
            drawCarryCards(selectedCards, candidatesByType.get(type), count);
        }

        // If commander legality leaves a bucket short, backfill from any remaining eligible carry cards.
        if (selectedCards.size() < offerCount) {
            List<PaperCard> remainingCards = new ArrayList<>();
            for (List<PaperCard> typedCandidates : candidatesByType.values()) {
                remainingCards.addAll(typedCandidates);
            }
            Collections.shuffle(remainingCards, MyRandom.getRandom());
            drawCarryCards(selectedCards, remainingCards, offerCount - selectedCards.size());
        }
        return selectedCards;
    }

    private void drawCarryCards(List<PaperCard> selectedCards, List<PaperCard> candidates, int count) {
        Iterator<PaperCard> candidateIterator = candidates.iterator();
        for (int i = 0; i < count && candidateIterator.hasNext(); i++) {
            selectedCards.add(candidateIterator.next());
            candidateIterator.remove();
        }
    }

    private static boolean isCarryCardCandidate(PaperCard card) {
        if (card == null || !PaperCardPredicates.IS_RARE_OR_MYTHIC.test(card)) {
            return false;
        }

        if (!DeckFormat.RogueCommander.isLegalCard(card)) {
            return false;
        }

        CardRarity rarity = card.getRarity();
        if (rarity != CardRarity.Rare && rarity != CardRarity.MythicRare) {
            return false;
        }
        if (card.getRules().getSplitType() != CardSplitType.None) {
            return false;
        }

        boolean isCreature = card.getRules().getType().isCreature();
        boolean isArtifact = card.getRules().getType().isArtifact();
        if (isCreature || isArtifact) {
            return card.getRules().getType().isLegendary();
        }

        return card.getRules().getType().isInstant()
            || card.getRules().getType().isSorcery();
    }

    private static RogueRun.CarryCardType getCarryCardType(PaperCard card) {
        if (card.getRules().getType().isCreature()) {
            return RogueRun.CarryCardType.FELLOW;
        }
        if (card.getRules().getType().isArtifact()) {
            return RogueRun.CarryCardType.ITEM;
        }
        return RogueRun.CarryCardType.SCROLL;
    }

    private List<PaperCard> applyBazaarPurchases(RogueRun currentRun, RogueDeck rogueDeck,
                                                 BazaarContext bazaarCtx, Set<BazaarItem> selectedItems,
                                                 boolean customBazaar) {
        bazaarCtx.purchasedItems.clear();
        if (selectedItems == null || selectedItems.isEmpty()) {
            return List.of();
        }

        bazaarCtx.purchasedItems.addAll(selectedItems);

        List<PaperCard> realCards = applyPurchasedItems(currentRun, selectedItems);

        if (!realCards.isEmpty()) {
            currentRun.addCardsToDeck(realCards, true);
            if (!customBazaar) {
                rogueDeck.removeFromCardPools(realCards);
            }
        }

        currentRun.spendGold(calculateTotalCost(selectedItems));
        return realCards;
    }

    private static List<PaperCard> applyPurchasedItems(RogueRun currentRun, Set<BazaarItem> selectedItems) {
        List<PaperCard> realCards = new ArrayList<>();
        for (BazaarItem item : selectedItems) {
            if (item.type() == BazaarItem.Type.CARD) {
                realCards.add(item.card());
                continue;
            }

            RogueEffect traitEffect = item.traitEffect();
            if (item.type() == BazaarItem.Type.TRAIT && traitEffect instanceof ChestEffect chestEffect) {
                currentRun.addChestEffect(chestEffect);
            } else if (item.type() == BazaarItem.Type.TRAIT && traitEffect instanceof NPCEffect npcEffect) {
                currentRun.addNPCEffect(npcEffect);
            } else if (item.type() == BazaarItem.Type.CARRY_CARD) {
                currentRun.addCarryCard(item.card(), item.carryCardType(), null);
            }
        }
        return realCards;
    }

    private static int calculateTotalCost(Set<BazaarItem> selectedItems) {
        int totalCost = 0;
        for (BazaarItem item : selectedItems) {
            totalCost += item.getPrice();
        }
        return totalCost;
    }

    private static List<PaperCard> getRewardPoolCards(Collection<BazaarItem> items) {
        List<PaperCard> rewardPoolCards = new ArrayList<>();
        for (BazaarItem item : items) {
            if (item.type() == BazaarItem.Type.CARD) {
                rewardPoolCards.add(item.card());
            }
        }
        return rewardPoolCards;
    }
}
