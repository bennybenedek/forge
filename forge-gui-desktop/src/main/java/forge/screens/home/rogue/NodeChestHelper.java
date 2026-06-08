package forge.screens.home.rogue;

import forge.gamemodes.rogue.RogueRun;
import forge.gamemodes.rogue.RogueTutorial;
import forge.gamemodes.rogue.effect.ChestEffect;
import forge.gamemodes.rogue.effect.EffectResultContext;
import forge.gamemodes.rogue.effect.RogueEffect;
import forge.gamemodes.rogue.path.NodeChest;
import forge.localinstance.properties.ForgePreferences;
import java.util.Random;
import javax.swing.JOptionPane;

class NodeChestHelper {

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
        ChestEffect chestEffect = chestNode.getChestEffect();
        if (chestEffect == null) {
            ChestEffect[] allChestEffects = ChestEffect.values();
            chestEffect = allChestEffects[new Random().nextInt(allChestEffects.length)];
            chestNode.setChestEffect(chestEffect);
        }

        if (ForgePreferences.DEV_MODE) {
            ChestEffect picked = (ChestEffect) JOptionPane.showInputDialog(
                null, "Override chest loot:", "[DEV] Pick Loot",
                JOptionPane.PLAIN_MESSAGE, null,
                ChestEffect.values(), chestEffect);
            if (picked != null) {
                chestEffect = picked;
            }
        }

        RogueTutorialHelper.showIfNotSeen(RogueTutorial.CHEST);
        new ChestDialog(chestEffect).show();

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
}
