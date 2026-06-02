package forge.screens.home.rogue;

import forge.deck.DeckSection;
import forge.gamemodes.rogue.CardRewardHelper;
import forge.gamemodes.rogue.RogueRun;
import forge.gamemodes.rogue.effect.EffectResultContext;
import forge.gamemodes.rogue.path.NodeChest;
import forge.gamemodes.rogue.path.NodeEvent;
import forge.gamemodes.rogue.path.NodePlanebound;
import forge.gamemodes.rogue.path.NodeSanctum;
import forge.gamemodes.rogue.path.RoguePathNode;
import forge.item.PaperCard;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class EffectResultHelper {

    private final CSubmenuRogueMap map;
    private final NodeBazaarHelper bazaarHelper;
    private final NodeChestHelper chestHelper;
    private final NodeSanctumHelper sanctumHelper;
    private final NodePlaneboundHelper planeboundHelper;

    EffectResultHelper(CSubmenuRogueMap map, NodeBazaarHelper bazaarHelper, NodeChestHelper chestHelper,
                       NodeSanctumHelper sanctumHelper, NodePlaneboundHelper planeboundHelper) {
        this.map = map;
        this.bazaarHelper = bazaarHelper;
        this.chestHelper = chestHelper;
        this.sanctumHelper = sanctumHelper;
        this.planeboundHelper = planeboundHelper;
    }

    NodeFlowOutcome handleEffectTrigger(RoguePathNode sourceNode, EffectResultContext ctx, RogueRun currentRun) {
        if (ctx.trigger == null) {
            return NodeFlowOutcome.COMPLETE_NODE;
        }

        switch (ctx.trigger) {
            case BAZAAR:
                ctx.addedCards = bazaarHelper.runBazaarShopping(currentRun, ctx.bazaarContext);
                return NodeFlowOutcome.COMPLETE_NODE;
            case PLANEBOUND:
                return handlePlaneboundTrigger(sourceNode, ctx, currentRun);
            case CHEST:
                return handleChestTrigger(sourceNode, currentRun);
            case SANCTUM:
                sanctumHelper.resolveSanctum(currentRun, new NodeSanctum());
                map.completeSideNode(sourceNode);
                return NodeFlowOutcome.NODE_ALREADY_COMPLETED;
            case MOVE:
                return handleMoveTrigger(ctx, currentRun);
            case CARD_REMOVAL:
                return handleCardRemovalTrigger(ctx, currentRun);
            case CARD_ADDITION:
                handleCardAdditionTrigger(ctx, currentRun);
                return NodeFlowOutcome.COMPLETE_NODE;
            case CARD_REWARD, MYTHIC_CARD_REWARD:
                return handleCardRewardTrigger(ctx, currentRun);
            default:
                return NodeFlowOutcome.COMPLETE_NODE;
        }
    }

    private NodeFlowOutcome handleChestTrigger(RoguePathNode sourceNode, RogueRun currentRun) {
        NodeFlowOutcome chestOutcome = chestHelper.resolveChest(currentRun, new NodeChest());
        if (chestOutcome == NodeFlowOutcome.COMPLETE_NODE) {
            map.completeSideNode(sourceNode);
            return NodeFlowOutcome.NODE_ALREADY_COMPLETED;
        }
        return chestOutcome;
    }

    private NodeFlowOutcome handleMoveTrigger(EffectResultContext ctx, RogueRun currentRun) {
        if (currentRun.getPath() == null || ctx.moveNodeIndex < 0) {
            return NodeFlowOutcome.COMPLETE_NODE;
        }

        if (ctx.moveNodeIndex >= currentRun.getPath().getNodeCount()) {
            return NodeFlowOutcome.COMPLETE_NODE;
        }

        if (currentRun.getPath().getNode(ctx.moveNodeIndex) == null) {
            return NodeFlowOutcome.COMPLETE_NODE;
        }

        int targetRow = currentRun.getPath().getNode(ctx.moveNodeIndex).getRowIndex();
        int resetFromRow = Math.max(0, targetRow - 1);
        currentRun.getPath().updateNodes(
            node -> node.getRowIndex() >= resetFromRow,
            node -> node.setCompleted(false));
        currentRun.setCurrentNodeIndex(ctx.moveNodeIndex);
        map.enterNode();
        return NodeFlowOutcome.DO_NOT_COMPLETE_NODE;
    }

    private NodeFlowOutcome handlePlaneboundTrigger(RoguePathNode sourceNode, EffectResultContext ctx,
                                                    RogueRun currentRun) {
        if (ctx.planebound == null) {
            return NodeFlowOutcome.COMPLETE_NODE;
        }

        if (sourceNode instanceof NodeEvent eventNode) {
            eventNode.setEventPlanebound(ctx.planebound);
        }

        NodePlanebound tempNode = new NodePlanebound(ctx.planebound);
        tempNode.setRowIndex(sourceNode.getRowIndex());
        planeboundHelper.handlePlaneboundNode(tempNode, currentRun);
        return NodeFlowOutcome.DO_NOT_COMPLETE_NODE;
    }

    private NodeFlowOutcome handleCardRemovalTrigger(EffectResultContext ctx, RogueRun currentRun) {
        int removeMaxCount = Math.min(ctx.cardSelectionMaxCount, getCandidateCardCount(ctx));
        int removeMinCount = Math.min(ctx.cardSelectionMinCount, removeMaxCount);
        if (removeMaxCount <= 0) {
            return NodeFlowOutcome.COMPLETE_NODE;
        }

        List<PaperCard> removed = new CardSelectionDialog(
            "Card Selection",
            getCardRemovalSubtitle(removeMinCount, removeMaxCount),
            ctx.candidateCards, removeMinCount, removeMaxCount).show();
        if (removed.size() < removeMinCount) {
            return NodeFlowOutcome.DO_NOT_COMPLETE_NODE;
        }

        for (PaperCard card : removed) {
            currentRun.getCurrentDeck().getMain().remove(card);
        }
        ctx.removedCards = removed;

        if (ctx.replacementCards != null && !ctx.replacementCards.isEmpty()) {
            currentRun.addCardsToDeck(ctx.replacementCards, false);
            if (currentRun.getSelectedRogueDeck() != null) {
                currentRun.getSelectedRogueDeck().removeFromCardPools(ctx.replacementCards);
            }
            ctx.addedCards.addAll(ctx.replacementCards);
        }
        if (ctx.replacementCarryCard != null && ctx.replacementCarryCard.card() != null) {
            currentRun.addCarryCard(ctx.replacementCarryCard.card(), ctx.replacementCarryCard.type(),
                ctx.replacementCarryCard.sourceId());
            ctx.addedCards.add(ctx.replacementCarryCard.card());
        }
        return NodeFlowOutcome.COMPLETE_NODE;
    }

    private void handleCardAdditionTrigger(EffectResultContext ctx, RogueRun currentRun) {
        int addMaxCount = Math.min(ctx.cardSelectionMaxCount, getCandidateCardCount(ctx));
        int addMinCount = Math.min(ctx.cardSelectionMinCount, addMaxCount);
        if (addMaxCount <= 0) {
            return;
        }

        List<PaperCard> added = new CardSelectionDialog(
            "Card Selection",
            getCardAdditionSubtitle(addMinCount, addMaxCount),
            ctx.candidateCards, addMinCount, addMaxCount).show();
        if (added.isEmpty() && addMinCount > 0) {
            return;
        }

        if (ctx.addSection == DeckSection.Commander) {
            if (ctx.replaceCurrentCardsInAddSection) {
                List<PaperCard> removedCommanders = new ArrayList<>(currentRun.getCurrentDeck().getCommanders());
                if (!removedCommanders.isEmpty()) {
                    for (PaperCard commander : removedCommanders) {
                        currentRun.getCurrentDeck().getOrCreate(DeckSection.Commander).remove(commander);
                    }
                    ctx.removedCards = removedCommanders;
                }
            }
            for (PaperCard card : added) {
                currentRun.getCurrentDeck().getOrCreate(DeckSection.Commander).add(card);
            }
        } else {
            if (ctx.cardSelectionCopyCount > 1) {
                List<PaperCard> copiedCards = new ArrayList<>();
                for (PaperCard card : added) {
                    copiedCards.addAll(Collections.nCopies(ctx.cardSelectionCopyCount, card));
                }
                added = copiedCards;
            }
            currentRun.addCardsToDeck(added, false);
        }
        ctx.addedCards = added;
    }

    private NodeFlowOutcome handleCardRewardTrigger(EffectResultContext ctx, RogueRun currentRun) {
        boolean mythicOnly = ctx.trigger == EffectResultContext.ActionTriggerType.MYTHIC_CARD_REWARD;
        CardRewardHelper.runReward(currentRun,
            (title, cards, max, label, enabled, gold) ->
                new CardRewardDialog(title, cards, max, label, enabled, gold).show(),
            mythicOnly);
        return NodeFlowOutcome.COMPLETE_NODE;
    }

    private int getCandidateCardCount(EffectResultContext ctx) {
        return ctx.candidateCards == null ? 0 : ctx.candidateCards.size();
    }

    private String getCardAdditionSubtitle(int addMinCount, int addMaxCount) {
        if (addMinCount == addMaxCount) {
            return addMaxCount == 1 ? "Choose 1 card to add." : "Choose " + addMaxCount + " cards to add.";
        }
        return "Choose up to " + addMaxCount + " cards to add.";
    }

    private String getCardRemovalSubtitle(int removeMinCount, int removeMaxCount) {
        if (removeMinCount == removeMaxCount) {
            return removeMaxCount == 1 ? "Choose 1 card to remove." : "Choose " + removeMaxCount + " cards to remove.";
        }
        return "Choose up to " + removeMaxCount + " cards to remove.";
    }
}
