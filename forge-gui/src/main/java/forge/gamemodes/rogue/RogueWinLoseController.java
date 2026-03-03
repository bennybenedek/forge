package forge.gamemodes.rogue;

import forge.LobbyPlayer;
import forge.game.GameView;
import forge.game.player.PlayerView;
import forge.gui.interfaces.IButton;
import forge.gui.interfaces.IWinLoseView;
import forge.item.PaperCard;
import forge.localinstance.achievements.RogueCommanderAchievements;
import forge.localinstance.skin.FSkinProp;
import forge.player.GamePlayerUtil;
import forge.util.Localizer;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Rogue Commander win/lose screen.
 * Handles reward logic after matches.
 */
public class RogueWinLoseController {
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
            view.getBtnQuit().setText(Localizer.getInstance().getMessage("lblGreat") + "!");
        } else {
            view.getBtnQuit().setText(Localizer.getInstance().getMessage("lblOK"));
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

        // Persist life total from match (before meta progress so milestones see current life)
        persistLifeTotal();

        // Track meta progress for match
        RogueMetaProgress.getInstance().onMatchCompleted(currentRun, true);

        // Check if this was the last node (run completed)
        boolean isLastNode = currentRun.getCurrentNodeIndex() >= currentRun.getPath().getNodeCount() - 1;

        // Apply Lingering Aura healing (after life persistence, but not for Boss/last node)
        RogueMetaProgress progress = RogueMetaProgress.getInstance();
        if (!isLastNode) {
            int healAmount = progress.getPostMatchHealAmount(currentRun);
            if (healAmount > 0) {
                int lifeBefore = currentRun.getCurrentLife();
                currentRun.healLife(healAmount);
                int healedAmount = currentRun.getCurrentLife() - lifeBefore;
                if (healedAmount > 0) {
                    view.showMessage("Lingering Aura healed " + healedAmount + " life.", "Boon Effect", FSkinProp.ICO_QUEST_CHARM);
                }
            }
        }

        // Get current node for rewards (applies to ALL nodes including Boss)
        RoguePathNode currentNode = currentRun.getCurrentNode();

        // Track gold and echo rewards for showing messages later
        int goldReward = 0;
        int echoReward = 0;

        // Award gold and echo rewards for ALL planebound nodes (including Boss)
        if (currentNode instanceof NodePlanebound) {
            NodePlanebound planeboundNode = (NodePlanebound) currentNode;
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

            progress.onRunCompleted(currentRun, true);
            RogueCommanderAchievements.instance.recordRunWon(
                currentRun.getSelectedRogueDeck().getCommanderCardName());
            int descLevel = currentRun.getDescensionLevel();
            if (descLevel > 0 && progress.recordDescensionWin(
                    currentRun.getSelectedRogueDeck().getCommanderCardName(), descLevel)) {
                view.showMessage("You won 1 Spark!", "Spark Reward", FSkinProp.ICO_QUEST_ELIXIR);
            }
            RogueCommanderAchievements.instance.evaluateRunAchievements(currentRun);
            progress.checkForNewUnlocks();
            progress.notifyDescensionL1IfFirstWin(currentRun.getSelectedRogueDeck().getCommanderCardName());
            RogueIO.saveRun(currentRun);
            view.showMessage("Congratulations! You have completed the run!", "Victory", FSkinProp.ICO_QUEST_CHARM);
            return; // Skip card rewards and navigation
        }

        // Award card rewards (only for non-final nodes)
        if (currentNode instanceof NodePlanebound) {
            NodePlanebound planeboundNode = (NodePlanebound) currentNode;
            // Award card rewards (with Elite flag for mythic rewards)
            boolean isElite = planeboundNode.getPlaneboundType() == RoguePlaneboundType.ELITE;
            awardCardRewards(isElite, goldReward, echoReward);
        }

        // Evaluate run-level achievements after rewards
        RogueCommanderAchievements.instance.evaluateRunAchievements(currentRun);

        // Check for newly unlocked commanders
        progress.checkForNewUnlocks();

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
        // Get the rogue deck data to draw rewards from
        RogueDeck rogueDeck = currentRun.getSelectedRogueDeck();

        if (rogueDeck == null) {
            System.err.println("ERROR: Could not find rogue deck for current run.");
            return;
        }

        RogueMetaProgress progress = RogueMetaProgress.getInstance();
        int maxPicks = 3 + progress.getExtraCardChoices();
        int rerollsRemaining = progress.getRerollsPerNode(); // Fresh per node

        // Draw cards from reward pool (base: 6 non-mythic + 1 mythic, adjusted by Mythic Collector boon)
        int extraMythics = progress.getExtraMythicCards();
        int baseNonMythics = 6;
        int baseMythics = 1;
        int totalNonMythics = Math.max(0, baseNonMythics - extraMythics);
        int totalMythics = baseMythics + extraMythics;

        List<PaperCard> rewardOptions;
        List<PaperCard> chosenCards;
        do {
            List<PaperCard> nonMythicCards = rogueDeck.drawRewardOptions(totalNonMythics, forge.item.PaperCardPredicates.IS_MYTHIC_RARE.negate());
            List<PaperCard> mythicCards = rogueDeck.drawRewardOptions(totalMythics, forge.item.PaperCardPredicates.IS_MYTHIC_RARE);

            rewardOptions = new ArrayList<>();
            rewardOptions.addAll(nonMythicCards);
            rewardOptions.addAll(mythicCards);

            if (rewardOptions.isEmpty()) {
                view.showMessage("No more cards available in reward pool.", "No Rewards", FSkinProp.ADV_CLR_ACTIVE);
                return;
            }

            chosenCards = view.showCardRewardDialog("Choose Your Rewards", rewardOptions, maxPicks, rerollsRemaining > 0);

            if (chosenCards == null) {
                rerollsRemaining--; // Reroll clicked — don't remove current options from pool
            }
        } while (chosenCards == null && rerollsRemaining >= 0);

        // Remove only the final draw's options from pool
        rogueDeck.removeFromRewardPool(rewardOptions);

        if (chosenCards == null) {
            chosenCards = new ArrayList<>();
        }

        // If Elite opponent, show second reward screen with mythic cards
        if (isElite) {
            rerollsRemaining = progress.getRerollsPerNode(); // Fresh rerolls for this selection
            List<PaperCard> mythicOptions;
            List<PaperCard> chosenMythics = new ArrayList<>();
            do {
                mythicOptions = rogueDeck.drawRewardOptions(3, forge.item.PaperCardPredicates.IS_MYTHIC_RARE);
                if (mythicOptions.isEmpty()) break;

                chosenMythics = view.showCardRewardDialog(
                    "Choose Your Mythic Reward", mythicOptions, 1, rerollsRemaining > 0);

                if (chosenMythics == null) rerollsRemaining--;
            } while (chosenMythics == null && rerollsRemaining >= 0);

            if (!mythicOptions.isEmpty()) {
                rogueDeck.removeFromRewardPool(mythicOptions);
                if (chosenMythics != null && !chosenMythics.isEmpty()) {
                    chosenCards.addAll(chosenMythics);
                }
            }
        }

        if (!chosenCards.isEmpty()) {
            // Add chosen cards to the run's current deck and update counter
            currentRun.addCardsToRun(chosenCards);

            // Show confirmation
            view.showCards("Cards Added to Your Deck", chosenCards);
        }

        // Show gold and echo rewards after card rewards
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

        // Check Last Spark BEFORE marking run as failed
        RogueMetaProgress progress = RogueMetaProgress.getInstance();
        int reviveLife = progress.getLastSparkReviveLife();
        if (reviveLife > 0 && currentRun.canRevive()) {
            currentRun.useRevive();
            currentRun.setCurrentLife(reviveLife);
            progress.onMatchCompleted(currentRun, false);
            RogueIO.saveRun(currentRun);
            view.getBtnQuit().setText("Continue Run");
            view.showMessage("Last Spark activated! You survived with " + reviveLife + " life!", "Last Spark!", FSkinProp.ICO_QUEST_ELIXIR);
            return;
        }

        // Normal defeat: mark run as failed
        currentRun.setRunFailed(true);

        // Record run history - defeated by current node's planebound
        String defeatedBy = "";
        RoguePathNode curNode = currentRun.getCurrentNode();
        if (curNode instanceof NodePlanebound) {
            NodePlanebound pb = (NodePlanebound) curNode;
            if (pb.getRoguePlanebound() != null) {
                defeatedBy = pb.getRoguePlanebound().planeboundName();
            }
        }
        progress.addRunHistoryEntry(RogueRunHistoryEntry.fromRun(currentRun, "DEFEAT", defeatedBy));

        // Echoes are already added to meta progress after each match win, no transfer needed
        progress.onMatchCompleted(currentRun, false);
        progress.onRunCompleted(currentRun, false);
        RogueCommanderAchievements.instance.evaluateRunAchievements(currentRun);
        progress.checkForNewUnlocks();

        // Save run state
        RogueIO.saveRun(currentRun);

        view.showMessage("You were defeated! Your Run has ended.", "Defeat", FSkinProp.ICO_QUEST_ZEP);
    }

    public void actionOnQuit() {
        // Any cleanup needed before quitting
        // Currently handled by RogueWinLose.actionOnQuit()
    }
}
