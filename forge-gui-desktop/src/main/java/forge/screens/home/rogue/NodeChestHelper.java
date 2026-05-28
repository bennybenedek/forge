package forge.screens.home.rogue;

import forge.gamemodes.rogue.CardRewardHelper;
import forge.gamemodes.rogue.RogueRun;
import forge.gamemodes.rogue.RogueTutorial;
import forge.gamemodes.rogue.effect.ChestEffect;
import forge.gamemodes.rogue.effect.NodeResultContext;
import forge.gamemodes.rogue.effect.RogueEffect;
import forge.gamemodes.rogue.path.NodeChest;
import forge.localinstance.properties.ForgePreferences;
import java.util.Random;
import javax.swing.JOptionPane;

class NodeChestHelper {

    private final CSubmenuRogueMap map;

    NodeChestHelper(CSubmenuRogueMap map) {
        this.map = map;
    }

    void handleChestNode(NodeChest chestNode, RogueRun currentRun) {
        if (currentRun == null) {
            return;
        }

        resolveChest(currentRun, chestNode);
        map.completeSideNode(chestNode);
    }

    void resolveChest(RogueRun currentRun, NodeChest chestNode) {
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

        NodeResultContext ctx = new NodeResultContext();
        if (chestEffect.getEffectType() == RogueEffect.EffectType.ONESHOT) {
            chestEffect.applyEffect(currentRun, ctx);
            boolean mythicOnly = ctx.trigger == NodeResultContext.ActionTriggerType.MYTHIC_CARD_REWARD;
            if (ctx.trigger == NodeResultContext.ActionTriggerType.CARD_REWARD
                || ctx.trigger == NodeResultContext.ActionTriggerType.MYTHIC_CARD_REWARD) {
                CardRewardHelper.runReward(currentRun,
                    (title, cards, max, label, enabled, gold) ->
                        new CardRewardDialog(title, cards, max, label, enabled, gold).show(),
                    mythicOnly);
            }
        } else {
            currentRun.addChestEffect(chestEffect);
        }
    }
}
