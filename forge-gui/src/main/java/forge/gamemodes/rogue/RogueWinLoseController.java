package forge.gamemodes.rogue;

import forge.LobbyPlayer;
import forge.game.Game;
import forge.game.GameView;
import forge.game.player.Player;
import forge.game.zone.ZoneType;
import forge.gamemodes.rogue.RogueRun.CarryCard;
import forge.gamemodes.rogue.RogueRun.CarryCardType;
import forge.gamemodes.rogue.effect.DefeatContext;
import forge.gamemodes.rogue.effect.MatchRewardContext;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Rogue Commander win/lose screen.
 * Handles reward logic after matches.
 */
public class RogueWinLoseController {
    private static final String BTN_CONTINUE_RUN = "Continue Run";
    private static final String BTN_WIN_RUN = "Finish Run";
    private static final String BTN_LOSE_RUN = "End Run";
    private static final String YOU_WON = "You won ";

    private final GameView lastGame;
    private final IWinLoseView<? extends IButton> view;
    private final boolean wonMatch;
    private final RogueRun currentRun;

    // Carry cards lost during the match (set by handleMatchData, shown by both victory/defeat)
    private List<CarryCard> lostCarryCards = List.of();

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
                handleMatchVictory();
            } else {
                handleMatchDefeat();
            }
        });
    }

    private void handleMatchVictory() {
        if (currentRun == null) {
            System.err.println("ERROR: No current run found in RogueWinLoseController");
            return;
        }

        // Record the victory (this also marks the node as completed)
        currentRun.recordMatchResult(true);

        // Persist life total and check carry card survival
        handleMatchData();

        // Check if this was the last node (run completed)
        boolean isLastNode = currentRun.getCurrentNodeIndex() >= currentRun.getPath().getNodeCount() - 1;

        // Apply match win effects (healing, etc.) — not for Boss/last node
        if (!isLastNode) {
            int lifeBefore = currentRun.getCurrentLife();
            RogueEffectComposite.INSTANCE.onMatchWin(currentRun);
            int gainedLife = currentRun.getCurrentLife() - lifeBefore;
            if (gainedLife > 0) {
                view.showMessage("Gained " + gainedLife + " life.", "Effect", FSkinProp.ICO_QUEST_CHARM);
            }

            // Won the match but life is still <= 0 (e.g. "can't lose the game" effect) — run is lost
            if (currentRun.getCurrentLife() <= 0) {
                RogueStats.fireOnMatchCompleted(currentRun, RogueMetaProgress.getInstance(), true);
                handleRunDefeat();
                return;
            }
        }

        // Get current node for rewards (applies to ALL nodes including Boss)
        RoguePathNode currentNode = currentRun.getCurrentNode();

        // Track gold and echo rewards for showing messages later
        int goldReward = 0;
        int echoReward = 0;

        var progress = RogueMetaProgress.getInstance();

        // Check if rewards should be skipped (e.g. Distortion effect)
        MatchRewardContext rewardCtx = new MatchRewardContext();
        RogueEffectComposite.INSTANCE.onBeforeRewards(rewardCtx, currentRun);

        NodePlanebound planeboundNode = resolvePlanebound(currentNode);

        // Award gold and echo rewards for ALL planebound nodes (including Boss)
        if (planeboundNode != null && !rewardCtx.skipRewards) {
            goldReward = Math.max(0, planeboundNode.getGoldReward() + rewardCtx.goldRewardAdjustment);
            echoReward = planeboundNode.getEchoReward();

            // Gold is run-specific (spent at Bazaar during the run)
            currentRun.addGold(goldReward);

            // Echoes are meta-progression currency - add directly to meta progress
            if (echoReward > 0) {
                progress.addEchoes(echoReward);
            }
        }

        if (isLastNode) {
            handleRunVictory(echoReward);
            return;
        }

        // Award card rewards (only for non-final nodes, skip if distortion)
        if (planeboundNode != null && !rewardCtx.skipRewards) {
            boolean isElite = planeboundNode.getPlaneboundType() == RoguePlaneboundType.ELITE;
            awardCardRewards(isElite, goldReward, echoReward, rewardCtx);
        }

        // Show lost carry card message
        showLostCarryCards();

        // Track meta progress for match
        RogueStats.fireOnMatchCompleted(currentRun, progress, true);

        // Evaluate run-level achievements after rewards
        RogueCommanderAchievements.instance.evaluateRunAchievements(currentRun);

        // Move to next node
        currentRun.nextNode();

        // Save run
        RogueIO.saveRun(currentRun);
    }

    private void handleRunVictory(int echoReward) {
        if (echoReward > 0) {
            view.showMessage(YOU_WON + echoReward + " Echoes.", "Echo Reward", FSkinProp.ICO_QUEST_GOLD);
        }

        currentRun.setRunWon(true);
        currentRun.getRunTimer().stop();

        // Record run history - find boss name from last NodePlanebound (BOSS type)
        String bossName = "";
        for (RoguePathNode node : currentRun.getPath().getNodes()) {
            if (node instanceof NodePlanebound pb && pb.getPlaneboundType() == RoguePlaneboundType.BOSS) {
                bossName = pb.getRoguePlanebound().planeboundName();
            }
        }

        var progress = RogueMetaProgress.getInstance();
        progress.addRunHistoryEntry(RogueRunHistoryEntry.fromRun(currentRun, "VICTORY", bossName));

        RogueStats.fireOnMatchCompleted(currentRun, progress, true);
        RogueStats.fireOnRunCompleted(currentRun, progress, true);

        RogueCommanderAchievements.instance.recordRunWon(
            currentRun.getCurrentCommanderName());

        int descLevel = currentRun.getDescensionLevel();
        if (descLevel > 0 && progress.recordDescensionWin(
                currentRun.getCurrentCommanderName(), descLevel)) {
            view.showMessage("You won 1 Spark!", "Spark Reward", FSkinProp.ICO_QUEST_ELIXIR);
        }
        RogueCommanderAchievements.instance.evaluateRunAchievements(currentRun);
        progress.notifyDescensionL1IfFirstWin(currentRun.getCurrentCommanderName());

        RogueIO.saveRun(currentRun);
        view.getBtnQuit().setText(BTN_WIN_RUN);
        view.showMessage("Congratulations! You have completed the run!", "Victory", FSkinProp.ICO_QUEST_CHARM);
    }

    private void awardCardRewards(boolean isElite, int goldReward, int echoReward, MatchRewardContext rewardCtx) {
        List<PaperCard> chosenCards = CardRewardHelper.runReward(currentRun,
                view::showCardRewardDialog, false, rewardCtx, null);

        // If Elite opponent, show second reward screen with mythic cards
        if (isElite) {
            List<PaperCard> chosenMythics = CardRewardHelper.runReward(currentRun,
                    view::showCardRewardDialog, true, rewardCtx, null);
            chosenCards.addAll(chosenMythics);
        }

        if (!chosenCards.isEmpty()) {
            view.showCards("Cards Added to Your Deck", chosenCards);
        }

        if (goldReward > 0) {
            view.showMessage(YOU_WON + goldReward + " Gold.", "Gold Reward", FSkinProp.ICO_QUEST_COIN);
        }

        if (echoReward > 0) {
            view.showMessage(YOU_WON + echoReward + " Echoes.", "Echo Reward", FSkinProp.ICO_QUEST_GOLD);
        }
    }

    private void handleMatchDefeat() {
        if (currentRun == null) {
            return;
        }

        // Record the match loss (node stays incomplete for retry)
        currentRun.recordMatchResult(false);

        // Persist life total and check carry card survival
        handleMatchData();

        // Check revive effects (e.g. Last Spark) BEFORE marking run as failed
        DefeatContext defeatCtx = new DefeatContext();
        RogueEffectComposite.INSTANCE.onDefeat(defeatCtx, currentRun);
        if (defeatCtx.revived) {
            currentRun.setCurrentLife(defeatCtx.reviveLife);
            RogueStats.fireOnMatchCompleted(currentRun, RogueMetaProgress.getInstance(), false);
            RogueIO.saveRun(currentRun);
            view.getBtnQuit().setText(BTN_CONTINUE_RUN);
            view.showMessage("Last Spark activated! You survived with " + defeatCtx.reviveLife + " life!", "Last Spark!", FSkinProp.ICO_QUEST_ELIXIR);
            return;
        }

        RogueStats.fireOnMatchCompleted(currentRun, RogueMetaProgress.getInstance(), false);
        handleRunDefeat();
    }

    private void handleRunDefeat() {
        finalizeRunDefeat(currentRun, getDefeatedByCurrentNode(currentRun));
        view.getBtnQuit().setText(BTN_LOSE_RUN);
        view.showMessage("You were defeated! Your Run has ended.", "Defeat", FSkinProp.ICO_QUEST_ZEP);
    }

    public static void finalizeRunDefeat(RogueRun run, String defeatedBy) {
        if (run == null) {
            return;
        }
        run.setRunFailed(true);
        run.getRunTimer().stop();

        var progress = RogueMetaProgress.getInstance();
        progress.addRunHistoryEntry(RogueRunHistoryEntry.fromRun(run, "DEFEAT",
                defeatedBy != null ? defeatedBy : ""));

        RogueStats.fireOnRunCompleted(run, progress, false);
        RogueCommanderAchievements.instance.evaluateRunAchievements(run);

        RogueIO.saveRun(run);
    }

    private static String getDefeatedByCurrentNode(RogueRun run) {
        String defeatedBy = "";
        RoguePathNode curNode = run.getCurrentNode();
        if (curNode instanceof NodePlanebound pb && pb.getRoguePlanebound() != null) {
            defeatedBy = pb.getRoguePlanebound().planeboundName();
        } else if (curNode instanceof NodeEvent ev && ev.getEventPlanebound() != null) {
            defeatedBy = ev.getEventPlanebound().planeboundName();
        }
        return defeatedBy;
    }

    private NodePlanebound resolvePlanebound(RoguePathNode node) {
        if (node instanceof NodePlanebound pb) {
            return pb;
        } else if (node instanceof NodeEvent ev && ev.getEventPlanebound() != null) {
            NodePlanebound pb = new NodePlanebound(ev.getEventPlanebound());
            pb.setRowIndex(node.getRowIndex());
            return pb;
        }
        return null;
    }

    private void showLostCarryCards() {
        if (lostCarryCards.isEmpty()) return;
        StringBuilder sb = new StringBuilder("You lost: ");
        for (int i = 0; i < lostCarryCards.size(); i++) {
            if (i > 0) sb.append(", ");
            RogueRun.CarryCard card = lostCarryCards.get(i);
            sb.append(card.cardName())
              .append(" (")
              .append(getCarryCardTypeLabel(card.type()))
              .append(")");
        }
        view.showMessage(sb.toString(), "Lost Carry Cards", FSkinProp.ICO_QUEST_MINUS);
    }

    private static String getCarryCardTypeLabel(CarryCardType type) {
        return switch (type) {
            case ITEM -> "item";
            case FELLOW -> "fellow";
            case SCROLL -> "scroll";
        };
    }

    private void handleMatchData() {
        Game game = lastGame.getGame();
        if (game == null) return;
        final LobbyPlayer humanLobbyPlayer = GamePlayerUtil.getGuiPlayer();
        for (Player p : game.getPlayers()) {
            if (p.getLobbyPlayer() == humanLobbyPlayer) {
                currentRun.persistMatchLife(p.getLife());
                currentRun.setLastMatchData(new RogueRun.LastMatchData(
                    p.getPlanarDieChaosThisGame(), p.getPlanarDiePlaneswalkThisGame()));
                checkCarryCardSurvival(p);
                break;
            }
        }
    }

    private void checkCarryCardSurvival(Player human) {
        List<CarryCard> lost = new ArrayList<>();
        for (CarryCard card : currentRun.getCarryCards()) {
            boolean survived = hasCarryCardSurvived(human, card);
            if (!survived) lost.add(card);
        }
        for (CarryCard card : lost) {
            currentRun.removeCarryCard(card.cardName());
        }
        this.lostCarryCards = lost;
    }

    private static boolean hasCarryCardSurvived(Player human, CarryCard card) {
        boolean inCommandZone = human.getZone(ZoneType.Command).getCards().stream()
            .anyMatch(c -> c.getName().equals(card.cardName()));
        return inCommandZone
            || human.getZone(ZoneType.Battlefield).getCards().stream()
                .anyMatch(c -> c.getName().equals(card.cardName()))
            || human.getCardsIn(ZoneType.Stack).stream()
                .anyMatch(c -> c.getName().equals(card.cardName()));
    }

    public void actionOnQuit() {
        // Any cleanup needed before quitting
        // Currently handled by RogueWinLose.actionOnQuit()
    }
}
