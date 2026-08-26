package forge.screens.home.rogue;

import forge.gamemodes.rogue.RogueRun;
import forge.gamemodes.rogue.RogueTutorial;
import forge.gamemodes.rogue.effect.ChestEffect;
import forge.gamemodes.rogue.effect.ChoiceRerollContext;
import forge.gamemodes.rogue.effect.EffectResultContext;
import forge.gamemodes.rogue.effect.RogueEffect;
import forge.gamemodes.rogue.effect.RogueEffectComposite;
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

        chestEffects = maybeOverrideChestChoices(chestEffects);
        chestNode.setChestEffects(chestEffects);

        RogueTutorialHelper.showIfNotSeen(RogueTutorial.CHEST);
        ChestDialog.DialogResult result;
        boolean rerollRequested;
        do {
            ChoiceRerollContext rerollCtx = new ChoiceRerollContext();
            RogueEffectComposite.INSTANCE.onBeforeChestLoot(rerollCtx, currentRun);
            result = new ChestDialog(chestEffects, currentRun, rerollCtx).show();
            rerollRequested = result.rerollRequested();

            if (rerollRequested) {
                RogueEffectComposite.INSTANCE.onChoiceReroll(rerollCtx, currentRun);
                chestEffects = generateChestLootChoices(chestEffects);
                chestEffects = maybeOverrideChestChoices(chestEffects);
                chestNode.setChestEffects(chestEffects);
            }
        } while (rerollRequested);

        ChestEffect chestEffect = result.choice();
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
        return generateChestLootChoices(List.of());
    }

    private List<ChestEffect> generateChestLootChoices(List<ChestEffect> excludedChoices) {
        List<ChestEffect> chestEffects = new ArrayList<>(List.of(ChestEffect.values()));
        if (chestEffects.size() - excludedChoices.size() >= CHEST_LOOT_CHOICE_COUNT) {
            chestEffects.removeAll(excludedChoices);
        }
        Collections.shuffle(chestEffects, MyRandom.getRandom());
        return chestEffects.subList(0, Math.min(CHEST_LOOT_CHOICE_COUNT, chestEffects.size()));
    }

    private List<ChestEffect> maybeOverrideChestChoices(List<ChestEffect> chestEffects) {
        if (!ForgePreferences.DEV_MODE) {
            return chestEffects;
        }
        return new ChestChoiceOverrideDialog(chestEffects).show();
    }
}
