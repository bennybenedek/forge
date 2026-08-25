package forge.screens.home.rogue;

import forge.gamemodes.rogue.RogueRun;
import forge.gamemodes.rogue.RogueTutorial;
import forge.gamemodes.rogue.effect.ChestEffect;
import forge.gamemodes.rogue.effect.EffectResultContext;
import forge.gamemodes.rogue.effect.RogueEffect;
import forge.gamemodes.rogue.path.NodeChest;
import forge.localinstance.properties.ForgePreferences;
import forge.util.MyRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class NodeChestHelper {
    private static final int CHEST_LOOT_CHOICE_COUNT = 2;

    private final CSubmenuRogueMap map;
    private NodeEffectResultHelper effectResultHelper;

    NodeChestHelper(CSubmenuRogueMap map) {
        this.map = map;
    }

    void setEffectResultHelper(NodeEffectResultHelper effectResultHelper) {
        this.effectResultHelper = effectResultHelper;
    }

    void handleChestNode(NodeChest chestNode, RogueRun currentRun) {
        if (currentRun == null) {
            return;
        }

        NodeFlowOutcome chestOutcome = resolveChest(currentRun, chestNode);
        if (chestOutcome == NodeFlowOutcome.COMPLETE_NODE) {
            map.completeSideNode(chestNode);
        }
    }

    NodeFlowOutcome resolveChest(RogueRun currentRun, NodeChest chestNode) {
        List<ChestEffect> chestEffects = chestNode.getChestEffects();
        if (chestEffects.size() < CHEST_LOOT_CHOICE_COUNT) {
            chestEffects = generateChestLootChoices();
            chestNode.setChestEffects(chestEffects);
        }

        if (ForgePreferences.DEV_MODE) {
            chestEffects = new ChestChoiceOverrideDialog(chestEffects).show();
            chestNode.setChestEffects(chestEffects);
        }

        RogueTutorialHelper.showIfNotSeen(RogueTutorial.CHEST);
        ChestEffect chestEffect = new ChestDialog(chestEffects, currentRun).show();
        if (chestEffect == null) {
            return NodeFlowOutcome.COMPLETE_NODE;
        }

        EffectResultContext ctx = new EffectResultContext();
        if (chestEffect.getEffectType() == RogueEffect.EffectType.ONESHOT) {
            chestEffect.applyEffect(currentRun, ctx);
            if (effectResultHelper == null) {
                return NodeFlowOutcome.COMPLETE_NODE;
            }
            return effectResultHelper.handleEffectTrigger(chestNode, ctx, currentRun);
        } else {
            currentRun.addChestEffect(chestEffect);
        }
        return NodeFlowOutcome.COMPLETE_NODE;
    }

    private List<ChestEffect> generateChestLootChoices() {
        List<ChestEffect> chestEffects = new ArrayList<>(List.of(ChestEffect.values()));
        Collections.shuffle(chestEffects, MyRandom.getRandom());
        return chestEffects.subList(0, Math.min(CHEST_LOOT_CHOICE_COUNT, chestEffects.size()));
    }
}
