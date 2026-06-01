package forge.screens.home.rogue;

import forge.deck.DeckSection;
import forge.gamemodes.rogue.RogueEvent;
import forge.gamemodes.rogue.RogueMetaProgress;
import forge.gamemodes.rogue.RogueRun;
import forge.gamemodes.rogue.RogueTutorial;
import forge.gamemodes.rogue.effect.EventEffect;
import forge.gamemodes.rogue.effect.EffectResultContext;
import forge.gamemodes.rogue.effect.RogueEffect;
import forge.gamemodes.rogue.npc.EventContext;
import forge.gamemodes.rogue.npc.NPCEncounterComposite;
import forge.gamemodes.rogue.path.NodeChest;
import forge.gamemodes.rogue.path.NodeEvent;
import forge.gamemodes.rogue.path.NodePlanebound;
import forge.gamemodes.rogue.path.NodeSanctum;
import forge.item.PaperCard;
import forge.localinstance.properties.ForgePreferences;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

class NodeEventHelper {

    /** Outcome of event flow handling from trigger resolution up to node completion. */
    private enum EventFlowOutcome {
        COMPLETE_EVENT, // Current event should be completed by the caller.
        DO_NOT_COMPLETE_EVENT, // Current event must not be completed by the caller.
        EVENT_ALREADY_COMPLETED // Current event was already completed during nested trigger handling (e.g. other invoked Node)
    }

    private final CSubmenuRogueMap map;
    private final NodeBazaarHelper bazaarHelper;
    private final NodeChestHelper chestHelper;
    private final NodeSanctumHelper sanctumHelper;
    private final NodePlaneboundHelper planeboundHelper;

    NodeEventHelper(CSubmenuRogueMap map, NodeBazaarHelper bazaarHelper, NodeChestHelper chestHelper,
                    NodeSanctumHelper sanctumHelper, NodePlaneboundHelper planeboundHelper) {
        this.map = map;
        this.bazaarHelper = bazaarHelper;
        this.chestHelper = chestHelper;
        this.sanctumHelper = sanctumHelper;
        this.planeboundHelper = planeboundHelper;
    }

    void handleEventNode(NodeEvent eventNode, RogueRun currentRun) {
        if (currentRun == null) {
            return;
        }

        RogueEvent event = resolveEvent(eventNode);
        if (event == null) {
            currentRun.nextNode();
            map.updateView();
            return;
        }

        event = resolveDevEventOverride(event);
        RogueTutorialHelper.showIfNotSeen(RogueTutorial.EVENT);

        RogueEvent.EventChoice choice = new EventDialog(event, currentRun).show();
        if (choice == null) {
            return;
        }

        EventFlowOutcome choiceOutcome = handleEventChoice(eventNode, event, choice, currentRun);
        if (choiceOutcome != EventFlowOutcome.COMPLETE_EVENT) {
            return;
        }
        map.completeSideNode(eventNode);
    }

    private RogueEvent resolveEvent(NodeEvent eventNode) {
        EventContext npcCtx = new EventContext(eventNode.getEvent());
        NPCEncounterComposite.INSTANCE.onBeforeEvent(npcCtx, RogueMetaProgress.getInstance());

        RogueEvent event = npcCtx.getResolvedEvent();
        if (npcCtx.eventOverride != null) {
            eventNode.setEvent(event);
        }
        return event;
    }

    private RogueEvent resolveDevEventOverride(RogueEvent event) {
        if (!ForgePreferences.DEV_MODE) {
            return event;
        }

        RogueEvent picked = (RogueEvent) JOptionPane.showInputDialog(
            null, "Override event:", "[DEV] Pick Event",
            JOptionPane.PLAIN_MESSAGE, null,
            RogueEvent.values(), event);
        return picked != null ? picked : event;
    }

    private EventFlowOutcome handleEventChoice(NodeEvent eventNode, RogueEvent event,
                                               RogueEvent.EventChoice choice, RogueRun currentRun) {
        EventEffect effect = choice.effect();
        if (!effect.isChoiceAvailable(currentRun)) {
            return EventFlowOutcome.DO_NOT_COMPLETE_EVENT;
        }

        EffectResultContext ctx = new EffectResultContext();
        if (effect.getEffectType() == RogueEffect.EffectType.ONESHOT) {
            effect.applyEffect(currentRun, ctx);
            EventFlowOutcome triggerOutcome = handleEffectResultContext(eventNode, ctx, currentRun);
            if (triggerOutcome != EventFlowOutcome.COMPLETE_EVENT) {
                return triggerOutcome;
            }
        } else {
            currentRun.addEventEffect(effect);
        }

        if (map.checkSideNodeDefeat(event.getDisplayName())) {
            return EventFlowOutcome.DO_NOT_COMPLETE_EVENT;
        }

        showEventResult(choice, ctx);
        return EventFlowOutcome.COMPLETE_EVENT;
    }

    private EventFlowOutcome handleEffectResultContext(NodeEvent eventNode, EffectResultContext ctx, RogueRun currentRun) {
        if (ctx.trigger == null) {
            return EventFlowOutcome.COMPLETE_EVENT;
        }

        switch (ctx.trigger) {
            case BAZAAR:
                ctx.addedCards = bazaarHelper.runBazaarShopping(currentRun, ctx.bazaarContext);
                return EventFlowOutcome.COMPLETE_EVENT;
            case PLANEBOUND:
                return handleEventPlanebound(eventNode, ctx, currentRun);
            case CHEST:
                chestHelper.resolveChest(currentRun, new NodeChest());
                map.completeSideNode(eventNode);
                return EventFlowOutcome.EVENT_ALREADY_COMPLETED;
            case SANCTUM:
                sanctumHelper.resolveSanctum(currentRun, new NodeSanctum());
                map.completeSideNode(eventNode);
                return EventFlowOutcome.EVENT_ALREADY_COMPLETED;
            case MOVE:
                return handleEventMove(ctx, currentRun);
            case CARD_REMOVAL:
                return handleEventCardRemoval(ctx, currentRun);
            case CARD_ADDITION:
                handleEventCardAddition(ctx, currentRun);
                return EventFlowOutcome.COMPLETE_EVENT;
            default:
                return EventFlowOutcome.COMPLETE_EVENT;
        }
    }

    private EventFlowOutcome handleEventMove(EffectResultContext ctx, RogueRun currentRun) {
        if (currentRun.getPath() == null || ctx.moveNodeIndex < 0) {
            return EventFlowOutcome.COMPLETE_EVENT;
        }

        if (ctx.moveNodeIndex >= currentRun.getPath().getNodeCount()) {
            return EventFlowOutcome.COMPLETE_EVENT;
        }

        if (currentRun.getPath().getNode(ctx.moveNodeIndex) == null) {
            return EventFlowOutcome.COMPLETE_EVENT;
        }

        int targetRow = currentRun.getPath().getNode(ctx.moveNodeIndex).getRowIndex();
        int resetFromRow = Math.max(0, targetRow - 1);
        currentRun.getPath().updateNodes(
            node -> node.getRowIndex() >= resetFromRow,
            node -> node.setCompleted(false));
        currentRun.setCurrentNodeIndex(ctx.moveNodeIndex);
        map.enterNode();
        return EventFlowOutcome.DO_NOT_COMPLETE_EVENT;
    }

    private EventFlowOutcome handleEventPlanebound(NodeEvent eventNode, EffectResultContext ctx, RogueRun currentRun) {
        if (ctx.planebound == null) {
            return EventFlowOutcome.COMPLETE_EVENT;
        }

        eventNode.setEventPlanebound(ctx.planebound);
        NodePlanebound tempNode = new NodePlanebound(ctx.planebound);
        tempNode.setRowIndex(eventNode.getRowIndex());
        planeboundHelper.handlePlaneboundNode(tempNode, currentRun);
        return EventFlowOutcome.DO_NOT_COMPLETE_EVENT;
    }

    private EventFlowOutcome handleEventCardRemoval(EffectResultContext ctx, RogueRun currentRun) {
        int removeMaxCount = Math.min(ctx.cardSelectionMaxCount, getCandidateCardCount(ctx));
        int removeMinCount = Math.min(ctx.cardSelectionMinCount, removeMaxCount);
        if (removeMaxCount <= 0) {
            return EventFlowOutcome.COMPLETE_EVENT;
        }

        List<PaperCard> removed = new CardSelectionDialog(
            "Card Selection",
            getCardRemovalSubtitle(removeMinCount, removeMaxCount),
            ctx.candidateCards, removeMinCount, removeMaxCount).show();
        if (removed.size() < removeMinCount) {
            return EventFlowOutcome.DO_NOT_COMPLETE_EVENT;
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
        return EventFlowOutcome.COMPLETE_EVENT;
    }

    private void handleEventCardAddition(EffectResultContext ctx, RogueRun currentRun) {
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
            currentRun.addCardsToDeck(added, false);
        }
        ctx.addedCards = added;
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

    private void showEventResult(RogueEvent.EventChoice choice, EffectResultContext ctx) {
        map.showNodeResultDialog("Event Completed",
            getEventResultText(choice, ctx), buildNodeResultSections(ctx));
    }

    private List<NodeResultPanel.CardSection> buildNodeResultSections(EffectResultContext ctx) {
        List<NodeResultPanel.CardSection> sections = new ArrayList<>();
        if (ctx.gainedWound != null) {
            sections.add(new NodeResultPanel.CardSection("Wound gained:",
                ctx.gainedWound.getDisplayName() + " - " + ctx.gainedWound.getDescription()));
        }
        if (ctx.removedCards != null && !ctx.removedCards.isEmpty()) {
            sections.add(new NodeResultPanel.CardSection("Cards removed:", ctx.removedCards));
        }
        if (ctx.addedCards != null && !ctx.addedCards.isEmpty()) {
            sections.add(new NodeResultPanel.CardSection("Cards added:", ctx.addedCards));
        }
        return sections;
    }

    private String getEventResultText(RogueEvent.EventChoice choice, EffectResultContext ctx) {
        if (ctx.resultTextOverride != null) {
            return ctx.resultTextOverride;
        }
        return choice.resultText();
    }
}
