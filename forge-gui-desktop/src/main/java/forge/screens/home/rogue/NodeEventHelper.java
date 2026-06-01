package forge.screens.home.rogue;

import forge.gamemodes.rogue.RogueEvent;
import forge.gamemodes.rogue.RogueMetaProgress;
import forge.gamemodes.rogue.RogueRun;
import forge.gamemodes.rogue.RogueTutorial;
import forge.gamemodes.rogue.effect.EventEffect;
import forge.gamemodes.rogue.effect.EffectResultContext;
import forge.gamemodes.rogue.effect.RogueEffect;
import forge.gamemodes.rogue.npc.EventContext;
import forge.gamemodes.rogue.npc.NPCEncounterComposite;
import forge.gamemodes.rogue.path.NodeEvent;
import forge.localinstance.properties.ForgePreferences;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

class NodeEventHelper {

    private final CSubmenuRogueMap map;
    private final EffectResultHelper effectResultHelper;

    NodeEventHelper(CSubmenuRogueMap map, EffectResultHelper effectResultHelper) {
        this.map = map;
        this.effectResultHelper = effectResultHelper;
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

        NodeFlowOutcome choiceOutcome = handleEventChoice(eventNode, event, choice, currentRun);
        if (choiceOutcome != NodeFlowOutcome.COMPLETE_NODE) {
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

    private NodeFlowOutcome handleEventChoice(NodeEvent eventNode, RogueEvent event,
                                              RogueEvent.EventChoice choice, RogueRun currentRun) {
        EventEffect effect = choice.effect();
        if (!effect.isChoiceAvailable(currentRun)) {
            return NodeFlowOutcome.DO_NOT_COMPLETE_NODE;
        }

        EffectResultContext ctx = new EffectResultContext();
        if (effect.getEffectType() == RogueEffect.EffectType.ONESHOT) {
            effect.applyEffect(currentRun, ctx);
            NodeFlowOutcome nodeFlowOutcome = effectResultHelper.handleEffectTrigger(eventNode, ctx, currentRun);
            if (nodeFlowOutcome != NodeFlowOutcome.COMPLETE_NODE) {
                return nodeFlowOutcome;
            }
        } else {
            currentRun.addEventEffect(effect);
        }

        if (map.checkSideNodeDefeat(event.getDisplayName())) {
            return NodeFlowOutcome.DO_NOT_COMPLETE_NODE;
        }

        showEventResult(choice, ctx);
        return NodeFlowOutcome.COMPLETE_NODE;
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
