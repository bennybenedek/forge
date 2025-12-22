package forge.gamemodes.rogue;

import forge.LobbyPlayer;
import forge.game.GameView;
import forge.game.player.PlayerView;
import forge.gui.interfaces.IButton;
import forge.gui.interfaces.IWinLoseView;
import forge.item.PaperCard;
import forge.localinstance.skin.FSkinProp;
import forge.player.GamePlayerUtil;
import forge.util.Localizer;
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

        // Determine if player won
        final LobbyPlayer humanLobbyPlayer = GamePlayerUtil.getGuiPlayer();
        this.wonMatch = lastGame.isMatchWonBy(humanLobbyPlayer);
    }

    public void showRewards() {
        System.out.println("DEBUG: RogueWinLoseController.showRewards() - START");
        view.getBtnRestart().setVisible(false);

        final boolean matchIsNotOver = !lastGame.isMatchOver();
        System.out.println("DEBUG: matchIsNotOver = " + matchIsNotOver);
        System.out.println("DEBUG: wonMatch = " + wonMatch);

        // Note: We always assume match is over since we're in the win/lose screen
        // But we keep the matchIsNotOver check here for button setup in case of timing issues
        view.getBtnContinue().setVisible(false);
        if (wonMatch) {
            view.getBtnQuit().setText(Localizer.getInstance().getMessage("lblGreat") + "!");
        } else {
            view.getBtnQuit().setText(Localizer.getInstance().getMessage("lblOK"));
        }

        System.out.println("DEBUG: About to call view.showRewards()");
        // Show rewards on a separate thread
        view.showRewards(() -> {
            System.out.println("DEBUG: Inside showRewards runnable - START");

            // Re-check if player won, as game state might have updated since constructor
            final LobbyPlayer humanLobbyPlayer = GamePlayerUtil.getGuiPlayer();
            boolean playerWon = lastGame.isMatchWonBy(humanLobbyPlayer);

            System.out.println("DEBUG: Re-checking win condition...");
            System.out.println("DEBUG: wonMatch (from constructor) = " + wonMatch);
            System.out.println("DEBUG: playerWon (re-checked) = " + playerWon);
            System.out.println("DEBUG: isMatchOver (re-checked) = " + lastGame.isMatchOver());

            // Use the re-checked value (game state should be updated by now)
            if (playerWon) {
                System.out.println("DEBUG: Player won - calling handleVictory()");
                handleVictory();
            } else {
                System.out.println("DEBUG: Player lost - calling handleDefeat()");
                handleDefeat();
            }
            System.out.println("DEBUG: Inside showRewards runnable - END");
        });
        System.out.println("DEBUG: RogueWinLoseController.showRewards() - END");
    }

    private void handleVictory() {
        System.out.println("DEBUG: handleVictory() - START");
        if (currentRun == null) {
            System.err.println("ERROR: No current run found in RogueWinLoseController");
            return;
        }

        System.out.println("DEBUG: Recording match result");
        // Record the victory (this also marks the node as completed)
        currentRun.recordMatchResult(true);

        System.out.println("DEBUG: Persisting life total");
        // Persist life total from match
        persistLifeTotal();

        // Check if this was the last node (run completed)
        boolean isLastNode = currentRun.getCurrentNodeIndex() >= currentRun.getPath().getNodeCount() - 1;
        System.out.println("DEBUG: isLastNode = " + isLastNode);

        if (isLastNode) {
            System.out.println("DEBUG: Last node - marking run as won");
            // Run is complete - mark as won
            currentRun.setRunWon(true);
            RogueIO.saveRun(currentRun);
            view.showMessage("Congratulations! You have completed the run!", "Victory", FSkinProp.ICO_QUEST_CHARM);
            return; // Skip card rewards and navigation
        }

        System.out.println("DEBUG: Getting current node for rewards");
        // Award gold/echo rewards and card rewards (only for non-final nodes)
        RoguePathNode currentNode = currentRun.getCurrentNode();
        System.out.println("DEBUG: currentNode = " + currentNode);

        if (currentNode != null) {
            // Award card, gold and echo rewards (only planebound nodes have rewards)
            if (currentNode instanceof NodePlanebound) {
                System.out.println("DEBUG: Node is NodePlanebound - awarding rewards");
                NodePlanebound planeboundNode = (NodePlanebound) currentNode;
                int goldReward = planeboundNode.getGoldReward();
                int echoReward = planeboundNode.getEchoReward();
                System.out.println("DEBUG: Gold reward = " + goldReward + ", Echo reward = " + echoReward);
                currentRun.setCurrentGold(currentRun.getCurrentGold() + goldReward);
                currentRun.setCurrentEchoes(currentRun.getCurrentEchoes() + echoReward);

                // Award card rewards (with Elite flag for mythic rewards)
                boolean isElite = planeboundNode.getPlaneboundType() == RoguePlaneboundType.ELITE;
                System.out.println("DEBUG: isElite = " + isElite + ", calling awardCardRewards()");
                awardCardRewards(currentNode, isElite);
            } else {
                System.out.println("DEBUG: Node is NOT NodePlanebound, type = " + currentNode.getClass().getSimpleName());
            }
        } else {
            System.out.println("DEBUG: currentNode is null!");
        }

        System.out.println("DEBUG: Moving to next node");
        // Move to next node
        currentRun.nextNode();

        System.out.println("DEBUG: Saving run");
        // Save run
        RogueIO.saveRun(currentRun);
        System.out.println("DEBUG: handleVictory() - END");
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

    private void awardCardRewards(RoguePathNode currentNode, boolean isElite) {
        System.out.println("DEBUG: awardCardRewards() - START");
        // Get the rogue deck data to draw rewards from
        RogueDeck rogueDeck = currentRun.getSelectedRogueDeck();
        System.out.println("DEBUG: rogueDeck = " + rogueDeck);

        if (rogueDeck == null) {
            System.err.println("ERROR: Could not find rogue deck for current run.");
            return;
        }

        System.out.println("DEBUG: Drawing 7 reward options");
        // Draw 7 random cards from reward pool (always exclude mythics for first screen)
        List<PaperCard> rewardOptions = rogueDeck.drawRewardOptions(7, forge.item.PaperCardPredicates.IS_MYTHIC_RARE.negate());
        System.out.println("DEBUG: rewardOptions.size() = " + rewardOptions.size());

        if (rewardOptions.isEmpty()) {
            System.out.println("DEBUG: No reward options available");
            view.showMessage("No more cards available in reward pool.", "No Rewards", FSkinProp.ADV_CLR_ACTIVE);
            return;
        }

        // Get rewards earned from current node (only planebound nodes have rewards)
        int goldReward = 0;
        int echoReward = 0;
        if (currentNode instanceof NodePlanebound) {
            NodePlanebound planeboundNode = (NodePlanebound) currentNode;
            goldReward = planeboundNode.getGoldReward();
            echoReward = planeboundNode.getEchoReward();
        }
        System.out.println("DEBUG: Showing card reward dialog (gold=" + goldReward + ", echo=" + echoReward + ")");

        // Show visual card selection dialog
        List<PaperCard> chosenCards = view.showCardRewardDialog(
            "Choose Your Rewards",
            rewardOptions,
            0,
            3,
            goldReward,
            echoReward
        );
        System.out.println("DEBUG: Card reward dialog returned, chosenCards = " + (chosenCards == null ? "null" : chosenCards.size() + " cards"));

        // If Elite opponent, show second reward screen with mythic cards
        if (isElite) {
            List<PaperCard> mythicOptions = rogueDeck.drawRewardOptions(3, forge.item.PaperCardPredicates.IS_MYTHIC_RARE);

            if (!mythicOptions.isEmpty()) {
                // Show mythic card selection dialog
                List<PaperCard> chosenMythics = view.showCardRewardDialog(
                    "Choose Your Mythic Reward",
                    mythicOptions,
                    0,  // min selection (optional)
                    1,  // max selection
                    0,  // no additional gold
                    0   // no additional echoes
                );

                if (chosenMythics != null && !chosenMythics.isEmpty()) {
                    // Add chosen mythic to deck
                    chosenCards.addAll(chosenMythics);
                }

                // Remove all mythic options from pool
                rogueDeck.removeFromRewardPool(mythicOptions);
            }
        }

        if (chosenCards != null && !chosenCards.isEmpty()) {
            // Add chosen cards to the run's current deck and update counter
            currentRun.addCardsToRun(chosenCards);

            // Show confirmation
            view.showCards("Cards Added to Your Deck", chosenCards);
        }

        if (currentNode instanceof NodePlanebound planeboundNode) {
            view.showMessage("You won " + planeboundNode.getGoldReward() +" Gold.", "Gold Reward", FSkinProp.ICO_QUEST_COIN);
            view.showMessage("You won " + planeboundNode.getEchoReward() +" Echoes.", "Echo Reward", FSkinProp.ICO_QUEST_GOLD);
        }

        // Remove reward options (both chosen and unchosen) from the reward pool
        rogueDeck.removeFromRewardPool(rewardOptions);
    }

    private void handleDefeat() {
        if (currentRun == null) {
            return;
        }

        // Record the loss and mark run as failed
        currentRun.recordMatchResult(false);
        currentRun.setRunFailed(true);

        // Save run state
        RogueIO.saveRun(currentRun);

        view.showMessage("You were defeated! Your run has ended.", "Defeat", FSkinProp.ICO_QUEST_ZEP);
    }

    public void actionOnQuit() {
        // Any cleanup needed before quitting
        // Currently handled by RogueWinLose.actionOnQuit()
    }
}
