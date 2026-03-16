package forge.gamemodes.rogue;

import forge.LobbyPlayer;
import forge.game.GameView;
import forge.game.player.PlayerView;
import forge.gamemodes.rogue.effect.DefeatContext;
import forge.gamemodes.rogue.effect.RewardContext;
import forge.gamemodes.rogue.effect.RogueEffectComposite;
import forge.gamemodes.rogue.path.NodeEvent;
import forge.gamemodes.rogue.path.NodePlanebound;
import forge.gamemodes.rogue.path.RoguePathNode;
import forge.gui.interfaces.IButton;
import forge.gui.interfaces.IWinLoseView;
import forge.item.PaperCard;
import forge.localinstance.achievements.RogueCommanderAchievements;
import forge.localinstance.skin.FSkinProp;
import forge.player.GamePlayerUtil;
import java.util.List;

/**
 * Controller for Rogue Commander win/lose screen.
 * Handles reward logic after matches.
 */
public class RogueWinLoseController {
    private static final String BTN_CONTINUE_RUN = "Continue Run";
    private static final String BTN_WIN_RUN = "Finish Run";
    private static final String BTN_LOSE_RUN = "End Run";

    private final GameView lastGame;
    private final IWinLoseView<? extends IButton> view;
    private final boolean wonMatch;
    private final RogueRun currentRun;

    public RogueWinLoseController(final GameView game0, final IWinLoseView<? extends IButton> view0, final RogueRun currentRun0) {
        this.lastGame = game0;
        this.view = view0;
        this.currentRun = currentRun0;

        // Determine if player won using GameOutcome (more reliable than isMatchWonBy)
        final LobbyPlayer humanLobbyPlayer = GamePlayerUtil.getGuiPlayer();
        if (lastGame.getOutcome() != null) {
            this.wonMatch = lastGame.getOutcome().isWinner(humanLobbyPlayer);
        } else {
            // Fallback if GameOutcome is null (shouldn't happen)
            this.wonMatch = lastGame.isMatchWonBy(humanLobbyPlayer);
        }
    }

    public void showRewards() {
        view.getBtnRestart().setVisible(false);

        // Setup buttons - we're in the win/lose screen, so match is effectively over
        view.getBtnContinue().setVisible(false);
        if (wonMatch) {
            view.getBtnQuit().setText(BTN_CONTINUE_RUN);
        } else {
            view.getBtnQuit().setText(BTN_LOSE_RUN);
        }

        // Show rewards on a separate thread
        view.showRewards(() -> {
            if (wonMatch) {
                handleVictory();
            } else {
                handleDefeat();
            }
        });
    }

    private void handleVictory() {
        if (currentRun == null) {
            System.err.println("ERROR: No current run found in RogueWinLoseController");
            return;
        }

        // Record the victory (this also marks the node as completed)
        currentRun.recordMatchResult(true);

        // Persist life total from match (before meta progress so stats see current life)
        persistLifeTotal();

        // Check if this was the last node (run completed)
        boolean isLastNode = currentRun.getCurrentNodeIndex() >= currentRun.getPath().getNodeCount() - 1;

        // Apply match win effects (healing, etc.) — not for Boss/last node
        if (!isLastNode) {
            int lifeBefore = currentRun.getCurrentLife();
            RogueEffectComposite.INSTANCE.onMatchWin(currentRun);
            int healed = currentRun.getCurrentLife() - lifeBefore;
            if (healed > 0) {
                view.showMessage("Healed " + healed + " life.", "Boon Effect", FSkinProp.ICO_QUEST_CHARM);
            }
        }

        // Get current node for rewards (applies to ALL nodes including Boss)
        RoguePathNode currentNode = currentRun.getCurrentNode();

        // Track gold and echo rewards for showing messages later
        int goldReward = 0;
        int echoReward = 0;

        var progress = RogueMetaProgress.getInstance();

        // Check if rewards should be skipped (e.g. Distortion effect)
        RewardContext rewardCtx = new RewardContext();
        RogueEffectComposite.INSTANCE.onBeforeRewards(rewardCtx, currentRun);

        // Resolve planebound: either from the node directly, or from an event-triggered fight
        NodePlanebound planeboundNode = null;
        if (currentNode instanceof NodePlanebound pb) {
            planeboundNode = pb;
        } else if (currentNode instanceof NodeEvent ev && ev.getEventPlanebound() != null) {
            planeboundNode = new NodePlanebound(ev.getEventPlanebound());
            planeboundNode.setRowIndex(currentNode.getRowIndex());
        }

        // Award gold and echo rewards for ALL planebound nodes (including Boss)
        if (planeboundNode != null && !rewardCtx.skipRewards) {
            goldReward = planeboundNode.getGoldReward();
            echoReward = planeboundNode.getEchoReward();

            // Gold is run-specific (spent at Bazaar during the run)
            currentRun.setCurrentGold(currentRun.getCurrentGold() + goldReward);

            // Echoes are meta-progression currency - add directly to meta progress
            if (echoReward > 0) {
                progress.addEchoes(echoReward);
            }
        }

        if (isLastNode) {
            // Show echo rewards for Boss before victory message
            if (echoReward > 0) {
                view.showMessage("You won " + echoReward + " Echoes.", "Echo Reward", FSkinProp.ICO_QUEST_GOLD);
            }

            // Run is complete - mark as won
            currentRun.setRunWon(true);

            // Record run history - find boss name from last NodePlanebound (BOSS type)
            String bossName = "";
            for (RoguePathNode node : currentRun.getPath().getNodes()) {
                if (node instanceof NodePlanebound) {
                    NodePlanebound pb = (NodePlanebound) node;
                    if (pb.getPlaneboundType() == RoguePlaneboundType.BOSS) {
                        bossName = pb.getRoguePlanebound().planeboundName();
                    }
                }
            }
            progress.addRunHistoryEntry(RogueRunHistoryEntry.fromRun(currentRun, "VICTORY", bossName));

            RogueStats.fireOnMatchCompleted(currentRun, progress, true);
            RogueStats.fireOnRunCompleted(currentRun, progress, true);
            RogueCommanderAchievements.instance.recordRunWon(
                currentRun.getSelectedRogueDeck().getCommanderCardName());
            int descLevel = currentRun.getDescensionLevel();
            if (descLevel > 0 && progress.recordDescensionWin(
                    currentRun.getSelectedRogueDeck().getCommanderCardName(), descLevel)) {
                view.showMessage("You won 1 Spark!", "Spark Reward", FSkinProp.ICO_QUEST_ELIXIR);
            }
            RogueCommanderAchievements.instance.evaluateRunAchievements(currentRun);
            progress.notifyDescensionL1IfFirstWin(currentRun.getSelectedRogueDeck().getCommanderCardName());
            RogueIO.saveRun(currentRun);
            view.getBtnQuit().setText(BTN_WIN_RUN);
            view.showMessage("Congratulations! You have completed the run!", "Victory", FSkinProp.ICO_QUEST_CHARM);
            return; // Skip card rewards and navigation
        }

        // Award card rewards (only for non-final nodes, skip if distortion)
        if (planeboundNode != null && !rewardCtx.skipRewards) {
            boolean isElite = planeboundNode.getPlaneboundType() == RoguePlaneboundType.ELITE;
            awardCardRewards(isElite, goldReward, echoReward);
        }

        // Track meta progress for match
        RogueStats.fireOnMatchCompleted(currentRun, progress, true);

        // Evaluate run-level achievements after rewards
        RogueCommanderAchievements.instance.evaluateRunAchievements(currentRun);

        // Move to next node
        currentRun.nextNode();

        // Save run
        RogueIO.saveRun(currentRun);
    }

    private void persistLifeTotal() {
        // Get player's life total at end of match
        final LobbyPlayer humanLobbyPlayer = GamePlayerUtil.getGuiPlayer();
        PlayerView humanPlayer = null;
        for (final PlayerView p : lastGame.getPlayers()) {
            if (p.isLobbyPlayer(humanLobbyPlayer)) {
                humanPlayer = p;
                break;
            }
        }

        if (humanPlayer != null) {
            int endingLife = humanPlayer.getLife();
            currentRun.setCurrentLife(endingLife);
        }
    }

    private void awardCardRewards(boolean isElite, int goldReward, int echoReward) {
        List<PaperCard> chosenCards = CardRewardHelper.runReward(currentRun,
                view::showCardRewardDialog, false);

        if (chosenCards == null) {
            view.showMessage("No more cards available in reward pool.", "No Rewards", FSkinProp.ADV_CLR_ACTIVE);
            return;
        }

        // If Elite opponent, show second reward screen with mythic cards
        if (isElite) {
            List<PaperCard> chosenMythics = CardRewardHelper.runReward(currentRun,
                    view::showCardRewardDialog, true);
            if (chosenMythics != null) {
                chosenCards.addAll(chosenMythics);
            }
        }

        if (!chosenCards.isEmpty()) {
            view.showCards("Cards Added to Your Deck", chosenCards);
        }

        if (goldReward > 0) {
            view.showMessage("You won " + goldReward + " Gold.", "Gold Reward", FSkinProp.ICO_QUEST_COIN);
        }

        if (echoReward > 0) {
            view.showMessage("You won " + echoReward + " Echoes.", "Echo Reward", FSkinProp.ICO_QUEST_GOLD);
        }
    }

    private void handleDefeat() {
        if (currentRun == null) {
            return;
        }

        // Record the match loss (node stays incomplete for retry)
        currentRun.recordMatchResult(false);

        // Persist life total from the lost match
        persistLifeTotal();

        var progress = RogueMetaProgress.getInstance();

        // Check revive effects (e.g. Last Spark) BEFORE marking run as failed
        DefeatContext defeatCtx = new DefeatContext();
        RogueEffectComposite.INSTANCE.onDefeat(defeatCtx, currentRun);
        if (defeatCtx.revived) {
            currentRun.setCurrentLife(defeatCtx.reviveLife);
            RogueStats.fireOnMatchCompleted(currentRun, progress, false);
            RogueIO.saveRun(currentRun);
            view.getBtnQuit().setText(BTN_CONTINUE_RUN);
            view.showMessage("Last Spark activated! You survived with " + defeatCtx.reviveLife + " life!", "Last Spark!", FSkinProp.ICO_QUEST_ELIXIR);
            return;
        }

        // Normal defeat: mark run as failed
        currentRun.setRunFailed(true);

        // Record run history - defeated by current node's planebound
        String defeatedBy = "";
        RoguePathNode curNode = currentRun.getCurrentNode();
        if (curNode instanceof NodePlanebound pb && pb.getRoguePlanebound() != null) {
            defeatedBy = pb.getRoguePlanebound().planeboundName();
        } else if (curNode instanceof NodeEvent ev && ev.getEventPlanebound() != null) {
            defeatedBy = ev.getEventPlanebound().planeboundName();
        }

        progress.addRunHistoryEntry(RogueRunHistoryEntry.fromRun(currentRun, "DEFEAT", defeatedBy));

        // Echoes are already added to meta progress after each match win, no transfer needed
        RogueStats.fireOnMatchCompleted(currentRun, progress, false);
        RogueStats.fireOnRunCompleted(currentRun, progress, false);
        RogueCommanderAchievements.instance.evaluateRunAchievements(currentRun);

        // Save run state
        RogueIO.saveRun(currentRun);

        view.showMessage("You were defeated! Your Run has ended.", "Defeat", FSkinProp.ICO_QUEST_ZEP);
    }

    public void actionOnQuit() {
        // Any cleanup needed before quitting
        // Currently handled by RogueWinLose.actionOnQuit()
    }
}
