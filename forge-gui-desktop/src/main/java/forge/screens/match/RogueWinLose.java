package forge.screens.match;

import forge.game.GameView;
import forge.gamemodes.rogue.RogueMetaProgress;
import forge.gamemodes.rogue.RogueWinLoseController;
import forge.gamemodes.rogue.effect.ChoiceRerollContext;
import forge.gamemodes.rogue.npc.NPCContext;
import forge.gamemodes.rogue.npc.NPCEncounterComposite;
import forge.gui.framework.EDocID;
import forge.localinstance.skin.FSkinProp;
import forge.screens.home.CHomeUI;
import forge.screens.home.rogue.CSubmenuRogueMap;
import forge.screens.home.rogue.NPCDialog;
import forge.toolbox.FOptionPane;

/**
 * Win/Lose handler for Rogue Commander mode.
 * Shows rewards after victory and handles navigation back to the Rogue Map.
 */
public class RogueWinLose extends ControlWinLose {
    private final RogueWinLoseController controller;

    /**
     * Instantiates a new rogue commander win/lose handler.
     *
     * @param view0 ViewWinLose object
     * @param game0 GameView object
     * @param matchUI CMatchUI object
     */
    public RogueWinLose(final ViewWinLose view0, final GameView game0, final CMatchUI matchUI) {
        super(view0, game0, matchUI);

        // Get current run from the map controller and pass it to the controller
        var currentRun = CSubmenuRogueMap.SINGLETON_INSTANCE.getCurrentRun();
        controller = new RogueWinLoseController(game0, view0, currentRun);
    }

    /**
     * Populates the custom panel with rewards if the player won.
     *
     * @return true if rewards are shown
     */
    @Override
    public final boolean populateCustomPanel() {
        if (controller == null) {
            System.err.println("ERROR: Controller is null in populateCustomPanel!");
            return false;
        }
        controller.showRewards();
        var currentRun = CSubmenuRogueMap.SINGLETON_INSTANCE.getCurrentRun();
        if (controller.hasWonRun() && currentRun != null && !currentRun.isRunFailed()) {
            boolean unlocked = RogueMetaProgress.getInstance().isEndlessModeUnlocked();
            getView().getBtnContinue().setText("Complete Run");
            getView().getBtnContinue().setVisible(true);
            getView().getBtnContinue().setEnabled(true);
            getView().getBtnQuit().setText("Go Endless");
            getView().getBtnQuit().setEnabled(unlocked);
            if (!unlocked) {
                getView().showQuitButtonHint("<html><center>Win a Custom Run<br>to unlock Endless Mode.</center></html>",
                    FSkinProp.ICO_LOCK);
            }
        }
        return true;
    }

    @Override
    public String getOutcomeTitleOverride() {
        if (controller.hasFailedRun()) {
            return "Run Lost!";
        }
        if (!controller.hasWonMatch()) {
            return "Battle Lost!";
        }
        return controller.hasWonRun() ? "Run Completed!" : "Battle Won!";
    }

    @Override
    public boolean shouldShowPlayerScores() {
        return false;
    }

    @Override
    public final void actionOnQuit() {
        var currentRun = CSubmenuRogueMap.SINGLETON_INSTANCE.getCurrentRun();
        if (controller.hasWonRun() && currentRun != null && !currentRun.isRunFailed()) {
            if (currentRun.isRunWon() || !RogueMetaProgress.getInstance().isEndlessModeUnlocked()) {
                return;
            }
            getView().getBtnQuit().setEnabled(false);
            leaveWinLose(true);
            return;
        }
        leaveWinLose(false);
    }

    @Override
    public final void actionOnContinue() {
        var currentRun = CSubmenuRogueMap.SINGLETON_INSTANCE.getCurrentRun();
        if (currentRun == null || !controller.hasWonRun() || currentRun.isRunFailed()
                || currentRun.isRunWon()) {
            return;
        }
        getView().getBtnContinue().setEnabled(false);
        leaveWinLose(false);
    }

    private void leaveWinLose(boolean goEndless) {
        controller.actionOnQuit();

        var currentRun = CSubmenuRogueMap.SINGLETON_INSTANCE.getCurrentRun();

        // Clear hosted match so new matches can be started
        if (currentRun != null) {
            currentRun.setHostedMatch(null);
        }

        // Check NPC after-match progression (e.g. Narset chaos tracking)
        if (currentRun != null) {
            for (NPCContext ctx : NPCEncounterComposite.INSTANCE.onAfterMatch(currentRun)) {
                new NPCDialog(ctx, new ChoiceRerollContext()).show();
            }
        }

        if (controller.hasWonRun() && currentRun != null && !currentRun.isRunFailed()) {
            if (goEndless) {
                CSubmenuRogueMap.SINGLETON_INSTANCE.continueEndlessly();
            } else if (RogueWinLoseController.finalizeRunVictory(currentRun, null)) {
                FOptionPane.showMessageDialog("You won 1 Spark!", "Spark Reward");
            }
        }

        if (currentRun != null && (currentRun.isRunFailed() || currentRun.isRunWon())) {
            // Run is over (won or lost) - send player back to Commander selection
            CSubmenuRogueMap.SINGLETON_INSTANCE.setCurrentRun(null);
            CHomeUI.SINGLETON_INSTANCE.itemClick(EDocID.HOME_ROGUESTART);
        } else {
            // Continue run - return to map
            CSubmenuRogueMap.SINGLETON_INSTANCE.update();
            CHomeUI.SINGLETON_INSTANCE.itemClick(EDocID.HOME_ROGUEMAP);
        }

        super.actionOnQuit();
    }
}
