package forge.screens.home.rogue;

import forge.deck.DeckSection;
import forge.gamemodes.rogue.RogueEvent;
import forge.gamemodes.rogue.RogueMetaProgress;
import forge.gamemodes.rogue.RogueRun;
import forge.gamemodes.rogue.RogueTutorial;
import forge.gamemodes.rogue.effect.EventEffect;
import forge.gamemodes.rogue.effect.NodeResultContext;
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

    private final CSubmenuRogueMap map;
    private final NodeBazaarHelper bazaarHelper;
    private final NodeChestHelper chestHelper;

    NodeEventHelper(CSubmenuRogueMap map, NodeBazaarHelper bazaarHelper, NodeChestHelper chestHelper) {
        this.map = map;
        this.bazaarHelper = bazaarHelper;
        this.chestHelper = chestHelper;
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
        boolean handleEventWithoutCompletion = handleEventChoice(eventNode, event, choice, currentRun);
        if (handleEventWithoutCompletion) {
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

    private boolean handleEventChoice(NodeEvent eventNode, RogueEvent event,
                                      RogueEvent.EventChoice choice, RogueRun currentRun) {
        EventEffect effect = choice.effect();
        if (!effect.isChoiceAvailable(currentRun)) {
            return true;
        }

        NodeResultContext ctx = new NodeResultContext();
        if (effect.getEffectType() == RogueEffect.EffectType.ONESHOT) {
            effect.applyEffect(currentRun, ctx);
            if (handleEventTrigger(eventNode, ctx, currentRun)) {
                return true;
            }
        } else {
            currentRun.addEventEffect(effect);
        }

        if (map.checkSideNodeDefeat(event.getDisplayName())) {
            return true;
        }

        showEventResult(choice, ctx);
        return false;
    }

    private boolean handleEventTrigger(NodeEvent eventNode, NodeResultContext ctx, RogueRun currentRun) {
        if (ctx.trigger == null) {
            return false;
        }

        switch (ctx.trigger) {
            case BAZAAR:
                ctx.addedCards = bazaarHelper.runBazaarShopping(currentRun, ctx.bazaarContext);
                return false;
            case PLANEBOUND:
                return handleEventPlanebound(eventNode, ctx);
            case CHEST:
                chestHelper.resolveChest(currentRun, new NodeChest());
                map.completeSideNode(eventNode);
                return true;
            case SANCTUM:
                eventNode.setCompleted(true);
                map.handleSanctumNode(new NodeSanctum());
                return true;
            case CARD_REMOVAL:
                handleEventCardRemoval(ctx, currentRun);
                return false;
            case CARD_ADDITION:
                handleEventCardAddition(ctx, currentRun);
                return false;
            default:
                return false;
        }
    }

    private boolean handleEventPlanebound(NodeEvent eventNode, NodeResultContext ctx) {
        if (ctx.planebound == null) {
            return false;
        }

        eventNode.setEventPlanebound(ctx.planebound);
        NodePlanebound tempNode = new NodePlanebound(ctx.planebound);
        tempNode.setRowIndex(eventNode.getRowIndex());
        map.handlePlaneboundNode(tempNode);
        return true;
    }

    private void handleEventCardRemoval(NodeResultContext ctx, RogueRun currentRun) {
        List<PaperCard> candidateCards = currentRun.getSelectableDeckCards(null);
        int removeCount = Math.min(ctx.removeCount, candidateCards.size());
        if (removeCount <= 0) {
            return;
        }

        List<PaperCard> removed = new CardSelectionDialog(
            "Card Selection",
            "Choose " + removeCount + " cards to remove.",
            candidateCards, removeCount, removeCount).show();
        if (removed.isEmpty()) {
            return;
        }

        for (PaperCard card : removed) {
            currentRun.getCurrentDeck().getMain().remove(card);
        }
        ctx.removedCards = removed;

        if (ctx.drawCount > 0) {
            List<PaperCard> added = currentRun.getSelectedRogueDeck().drawRewardOptions(ctx.drawCount, null);
            currentRun.addCardsToDeck(added, false);
            currentRun.getSelectedRogueDeck().removeFromCardPools(added);
            ctx.addedCards = added;
        }
    }

    private void handleEventCardAddition(NodeResultContext ctx, RogueRun currentRun) {
        int addMaxCount = Math.min(ctx.addMaxCount, getCandidateCardCount(ctx));
        int addMinCount = Math.min(ctx.addMinCount, addMaxCount);
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

    private int getCandidateCardCount(NodeResultContext ctx) {
        return ctx.candidateCards == null ? 0 : ctx.candidateCards.size();
    }

    private String getCardAdditionSubtitle(int addMinCount, int addMaxCount) {
        if (addMinCount == addMaxCount) {
            return addMaxCount == 1 ? "Choose 1 card to add." : "Choose " + addMaxCount + " cards to add.";
        }
        return "Choose up to " + addMaxCount + " cards to add.";
    }

    private void showEventResult(RogueEvent.EventChoice choice, NodeResultContext ctx) {
        map.showNodeResultDialog("Event Completed",
            getEventResultText(choice, ctx), buildNodeResultSections(ctx));
    }

    private List<NodeResultPanel.CardSection> buildNodeResultSections(NodeResultContext ctx) {
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

    private String getEventResultText(RogueEvent.EventChoice choice, NodeResultContext ctx) {
        if (ctx.resultTextOverride != null) {
            return ctx.resultTextOverride;
        }
        return choice.resultText();
    }
}
