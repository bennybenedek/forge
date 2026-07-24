package forge.screens.home.rogue;

import forge.gamemodes.rogue.BazaarPricing;
import forge.gamemodes.rogue.CardRewardHelper;
import forge.gamemodes.rogue.RogueDeck;
import forge.gamemodes.rogue.RogueMetaProgress;
import forge.gamemodes.rogue.RogueRun;
import forge.gamemodes.rogue.RogueTutorial;
import forge.gamemodes.rogue.effect.CardSelectionContext;
import forge.gamemodes.rogue.effect.RogueEffectComposite;
import forge.gamemodes.rogue.effect.BazaarContext;
import forge.gamemodes.rogue.npc.NPCContext;
import forge.gamemodes.rogue.npc.NPCEncounterComposite;
import forge.gamemodes.rogue.path.NodeBazaar;
import forge.item.PaperCard;
import forge.item.PaperCardPredicates;
import forge.localinstance.achievements.RogueCommanderAchievements;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

class NodeBazaarHelper {

    private final CSubmenuRogueMap map;

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
        boolean customBazaar = ctx != null;
        RogueDeck rogueDeck = currentRun.getSelectedRogueDeck();
        if (rogueDeck == null) {
            System.err.println("ERROR: Could not find rogue deck for current run.");
            return List.of();
        }

        BazaarContext bazaarCtx = customBazaar ? ctx : createOrdinaryBazaarContext();
        if (!customBazaar) {
            RogueTutorialHelper.showIfNotSeen(RogueTutorial.BAZAAR);
            NPCEncounterComposite.INSTANCE.onBeforeBazaar(bazaarCtx, RogueMetaProgress.getInstance());
        }

        CardSelectionContext selCtx = customBazaar ? null : createBazaarSelectionContext(currentRun);
        int freeRerolls = customBazaar ? 0 : selCtx.freeRerolls;
        int rerollCount = 0;
        Set<PaperCard> selectedCards;
        do {
            List<PaperCard> inventory = customBazaar
                ? buildCustomBazaarInventory(bazaarCtx)
                : buildOrdinaryBazaarInventory(currentRun, rogueDeck, selCtx);

            applyBazaarDiscounts(bazaarCtx, inventory);
            injectBazaarItems(bazaarCtx, inventory);

            if (inventory.isEmpty()) {
                System.err.println("ERROR: No cards available in Bazaar inventory.");
                return List.of();
            }

            String rerollLabel = customBazaar ? null
                : CardRewardHelper.buildRerollLabel(freeRerolls, rerollCount);
            boolean rerollEnabled = !customBazaar
                && CardRewardHelper.canAffordReroll(freeRerolls, rerollCount, currentRun.getCurrentGold());
            BazaarDialog dialog = new BazaarDialog(
                inventory,
                currentRun.getCurrentGold(),
                bazaarCtx.title,
                rerollLabel,
                bazaarCtx.priceOverrides.isEmpty() ? null : bazaarCtx.priceOverrides);
            dialog.setRerollEnabled(rerollEnabled);
            selectedCards = dialog.show();

            if (!customBazaar) {
                rogueDeck.discardRewardOptions(getRewardPoolCards(inventory, bazaarCtx.injectedCards));
            }

            if (!customBazaar && selectedCards == null) {
                if (rerollCount >= freeRerolls) {
                    int cost = CardRewardHelper.getRerollCost(rerollCount - freeRerolls);
                    currentRun.spendGold(cost);
                }
                rerollCount++;
                bazaarCtx.priceOverrides.clear();
            }
        } while (!customBazaar && selectedCards == null);

        List<PaperCard> purchasedCards = applyBazaarPurchases(currentRun, rogueDeck, bazaarCtx, selectedCards, customBazaar);
        if (!customBazaar) {
            for (NPCContext npcContext : NPCEncounterComposite.INSTANCE.onAfterBazaarPurchase(
                bazaarCtx, RogueMetaProgress.getInstance())) {
                new NPCDialog(npcContext).show();
            }
        }
        return purchasedCards;
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

    private List<PaperCard> buildOrdinaryBazaarInventory(RogueRun currentRun, RogueDeck rogueDeck,
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

        List<PaperCard> inventory = new ArrayList<>();
        inventory.addAll(nonMythicCards);
        inventory.addAll(mythicCards);
        return inventory;
    }

    private List<PaperCard> buildCustomBazaarInventory(BazaarContext bazaarCtx) {
        List<PaperCard> inventory = new ArrayList<>(bazaarCtx.inventory);
        if (inventory.size() <= BazaarDialog.MAX_DISPLAY_CARDS) {
            return inventory;
        }

        Collections.shuffle(inventory);
        return new ArrayList<>(inventory.subList(0, BazaarDialog.MAX_DISPLAY_CARDS));
    }

    private void applyBazaarDiscounts(BazaarContext bazaarCtx, List<PaperCard> inventory) {
        if (bazaarCtx.discountCount <= 0 || inventory.isEmpty()) {
            return;
        }

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < inventory.size(); i++) {
            indices.add(i);
        }
        Collections.shuffle(indices);
        for (int i = 0; i < Math.min(bazaarCtx.discountCount, inventory.size()); i++) {
            PaperCard card = inventory.get(indices.get(i));
            int basePrice = BazaarPricing.getCardPrice(card);
            int discounted = Math.max(0, basePrice - bazaarCtx.discountAmount);
            if (discounted == 0 && basePrice > 2) {
                discounted = 1;
            }
            bazaarCtx.priceOverrides.put(card.getName(), discounted);
        }
    }

    private void injectBazaarItems(BazaarContext bazaarCtx, List<PaperCard> inventory) {
        if (bazaarCtx.injectedCards.isEmpty()) {
            return;
        }

        for (PaperCard injected : bazaarCtx.injectedCards) {
            if (!inventory.isEmpty()) {
                inventory.remove(inventory.size() - 1);
            }
            inventory.add(injected);
        }
    }

    private List<PaperCard> applyBazaarPurchases(RogueRun currentRun, RogueDeck rogueDeck,
                                                 BazaarContext bazaarCtx, Set<PaperCard> selectedCards,
                                                 boolean customBazaar) {
        if (selectedCards == null || selectedCards.isEmpty()) {
            return List.of();
        }

        List<PaperCard> realCards = customBazaar
            ? new ArrayList<>(selectedCards)
            : getRewardPoolCards(selectedCards, bazaarCtx.injectedCards);
        bazaarCtx.purchasedCards.clear();
        bazaarCtx.purchasedCards.addAll(selectedCards);

        if (!realCards.isEmpty()) {
            currentRun.addCardsToDeck(realCards, true);
            if (!customBazaar) {
                rogueDeck.removeFromCardPools(realCards);
            }
        }

        int totalCost = BazaarPricing.calculateTotalCost(selectedCards,
            bazaarCtx.priceOverrides.isEmpty() ? null : bazaarCtx.priceOverrides);
        currentRun.spendGold(totalCost);
        return realCards;
    }

    private static List<PaperCard> getRewardPoolCards(Collection<PaperCard> cards,
                                                      Set<PaperCard> injectedCards) {
        List<PaperCard> rewardPoolCards = new ArrayList<>();
        for (PaperCard card : cards) {
            if (!injectedCards.contains(card)) {
                rewardPoolCards.add(card);
            }
        }
        return rewardPoolCards;
    }
}
