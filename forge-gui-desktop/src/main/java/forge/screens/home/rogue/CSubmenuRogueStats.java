package forge.screens.home.rogue;

import forge.gamemodes.rogue.RogueMetaProgress;
import forge.gui.framework.EDocID;
import forge.gui.framework.ICDoc;
import forge.screens.home.CHomeUI;
import forge.toolbox.FOptionPane;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Controls the Rogue Commander statistics screen. Displays meta progression statistics from
 * RogueMetaProgress.
 */
public enum CSubmenuRogueStats implements ICDoc {
  SINGLETON_INSTANCE;

  private final VSubmenuRogueStats view = VSubmenuRogueStats.SINGLETON_INSTANCE;

  @Override
  public void register() {
  }

  @Override
  public void initialize() {
    view.getBtnBack().addActionListener(e -> goBack());
    view.getBtnReset().addActionListener(e -> confirmReset());
    view.getBtnResetTutorials().addActionListener(e -> confirmResetTutorials());
  }

  @Override
  public void update() {
    loadStatistics();
  }

  private void loadStatistics() {
    RogueMetaProgress progress = RogueMetaProgress.getInstance();

    view.updateDisplay(
        progress.getTotalRunsStarted(),
        progress.getTotalRunsCompleted(),
        progress.getTotalRunsWon(),
        progress.getTotalMatchesWon(),
        progress.getTotalMatchesLost(),
        progress.getMaxLifeInRun(),
        progress.getMaxGoldInRun(),
        progress.getMaxCreatureTypesInDeck(),
        progress.getMaxLegendaryPermanentsInDeck()
    );

    // Load per-commander statistics
    Map<String, int[]> commanderStats = new HashMap<>();
    Set<String> commandersUsed = progress.getCommandersUsed();
    for (String commander : commandersUsed) {
      int runsStarted = progress.getRunsStartedWithCommander(commander);
      int runsWon = progress.getRunsWonWithCommander(commander);
      commanderStats.put(commander, new int[]{runsStarted, runsWon});
    }
    view.updateCommanderStats(commanderStats);
  }

  private void goBack() {
    CHomeUI.SINGLETON_INSTANCE.itemClick(EDocID.HOME_ROGUESTART);
  }

  private void confirmReset() {
    boolean confirmed = FOptionPane.showConfirmDialog(
        "Are you sure you want to reset all Game progression?\nThis will delete all progress, history and stats.\nThis action cannot be undone.",
        "Reset Game Progression",
        "Reset",
        "Cancel",
        false
    );

    if (confirmed) {
      RogueMetaProgress.getInstance().reset();
      loadStatistics();
    }
  }

  private void confirmResetTutorials() {
    boolean confirmed = FOptionPane.showConfirmDialog(
        "Are you sure you want to reset all tutorials?\nThey will be shown again when you visit each screen.",
        "Reset Tutorials",
        "Reset",
        "Cancel",
        false
    );

    if (confirmed) {
      RogueMetaProgress.getInstance().resetTutorials();
    }
  }
}
