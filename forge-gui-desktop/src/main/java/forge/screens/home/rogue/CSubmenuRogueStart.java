package forge.screens.home.rogue;

import forge.gamemodes.rogue.*;
import forge.gui.framework.EDocID;
import forge.gui.framework.ICDoc;
import forge.screens.home.CHomeUI;
import java.util.Comparator;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 * Controls the Rogue Commander start screen. Handles commander selection via card grid and new run
 * creation.
 */
public enum CSubmenuRogueStart implements ICDoc {
  SINGLETON_INSTANCE;

  private final VSubmenuRogueStart view = VSubmenuRogueStart.SINGLETON_INSTANCE;
  private RogueDeck selectedDeck;
  private int selectedDescensionLevel = 0;

  @Override
  public void register() {
  }

  @Override
  public void initialize() {
    view.getBtnBeginRun().addActionListener(e -> beginNewRun());
    view.getBtnStats().addActionListener(e -> openStats());
    view.getBtnAether().addActionListener(e -> openAether());
    view.getBtnHistory().addActionListener(e -> openHistory());
    view.getChkDescension().addItemListener(e -> onDescensionToggled());
    view.getBtnDescensionDown().addActionListener(e -> changeDescensionLevel(-1));
    view.getBtnDescensionUp().addActionListener(e -> changeDescensionLevel(1));
  }

  private void openStats() {
    CHomeUI.SINGLETON_INSTANCE.itemClick(EDocID.HOME_ROGUESTATS);
  }

  private void openAether() {
    CHomeUI.SINGLETON_INSTANCE.itemClick(EDocID.HOME_ROGUEAETHER);
  }

  private void openHistory() {
    CHomeUI.SINGLETON_INSTANCE.itemClick(EDocID.HOME_ROGUEHISTORY);
  }

  @Override
  public void update() {
    loadAvailableCommanders();
    SwingUtilities.invokeLater(() -> {
      view.getBtnBeginRun().requestFocusInWindow();
      showTutorials();
    });
  }

  private void showTutorials() {
    RogueTutorialHelper.showIfNotSeen(RogueTutorial.WELCOME, RogueTutorial.COMMANDER_SELECTION);

    // Show RUN_COMPLETE tutorial after first completed run
    if (RogueMetaProgress.getInstance().getTotalRunsCompleted() > 0) {
      RogueTutorialHelper.showIfNotSeen(RogueTutorial.RUN_COMPLETE);
    }

    // Show Descension tutorial once when it becomes unlocked
    if (RogueMetaProgress.getInstance().isDescensionModeUnlocked()) {
      RogueTutorialHelper.showIfNotSeen(RogueTutorial.DESCENSION_UNLOCKED);
    }
  }

  private void loadAvailableCommanders() {
    List<RogueDeck> availableDecks = RogueConfig.loadRogueDecks();

    // Sort commanders: unlocked first (alphabetically), then locked (alphabetically)
    availableDecks.sort(Comparator
        .comparing((RogueDeck d) -> !d.isUnlocked())  // false (unlocked) before true (locked)
        .thenComparing(RogueDeck::getName));

    // Clear existing commander panels
    view.getCommanderGridPanel().clear();

    // Create card panel for each commander
    CommanderCardPanel firstUnlockedPanel = null;
    for (RogueDeck deck : availableDecks) {
      CommanderCardPanel cardPanel = new CommanderCardPanel(deck, view);

      // Set selection callback to update details and handle single-selection
      cardPanel.setSelectionCallback(this::onCommanderSelected);

      view.getCommanderGridPanel().addCommanderPanel(cardPanel);

      // Track first unlocked commander
      if (firstUnlockedPanel == null && !cardPanel.isLocked()) {
        firstUnlockedPanel = cardPanel;
      }
    }

    // Select first unlocked commander by default
    if (firstUnlockedPanel != null) {
      firstUnlockedPanel.setSelected(true);
      selectedDeck = firstUnlockedPanel.getCommander();
      updateCommanderDetails(firstUnlockedPanel);
      updateDescensionVisibility(firstUnlockedPanel);
      view.getBtnBeginRun().setEnabled(true);
    } else {
      view.getBtnBeginRun().setEnabled(false);
    }

    // Update button states - locked until first run completed
    boolean hasCompletedRuns = RogueMetaProgress.getInstance().getTotalRunsCompleted() > 0;
    view.getBtnAether().setEnabled(hasCompletedRuns);
    view.getBtnAether()
        .setToolTipText(hasCompletedRuns ? null : "Unlock the Aether by completing your first Run.");
    view.getBtnHistory().setEnabled(hasCompletedRuns);
    view.getBtnHistory()
        .setToolTipText(hasCompletedRuns ? null : "Unlock the Run History by completing your first Run.");
    view.getBtnStats().setEnabled(hasCompletedRuns);
    view.getBtnStats()
        .setToolTipText(hasCompletedRuns ? null : "Unlock Stats by completing your first Run.");

    // Refresh layout
    view.getCommanderGridPanel().revalidate();
    view.getCommanderGridPanel().repaint();
  }

  private void onCommanderSelected(CommanderCardPanel clickedPanel) {
    // For locked commanders, just show details but don't select
    if (clickedPanel.isLocked()) {
      // Deselect and unhighlight all panels, then highlight clicked
      for (CommanderCardPanel panel : view.getCommanderPanels()) {
        panel.setSelected(false);
        panel.setHighlighted(panel == clickedPanel);
      }
      selectedDeck = null;
      view.getBtnBeginRun().setEnabled(false);
      updateCommanderDetails(clickedPanel);
      view.getChkDescension().setVisible(false);
      view.getPnlDescensionLevel().setVisible(false);
      return;
    }

    // Clear highlights and deselect other panels (single-selection mode)
    for (CommanderCardPanel panel : view.getCommanderPanels()) {
      panel.setHighlighted(false);
      if (panel != clickedPanel) {
        panel.setSelected(false);
      }
    }

    // Toggle the clicked panel
    boolean newState = !clickedPanel.isSelected();
    clickedPanel.setSelected(newState);

    // Update selected deck and button state
    if (newState) {
      selectedDeck = clickedPanel.getCommander();
      view.getBtnBeginRun().setEnabled(true);
      updateCommanderDetails(clickedPanel);
      updateDescensionVisibility(clickedPanel);
    } else {
      // If deselecting, clear details
      selectedDeck = null;
      view.getBtnBeginRun().setEnabled(false);
      view.getLblCommanderName().setText("");
      view.getLblDescriptionLabel().setText("Description:");
      view.getLblDescriptionLock().setVisible(false);
      view.getTxtDescription().setText("");
      view.getTxtTheme().setText("");
      view.getChkDescension().setVisible(false);
      view.getPnlDescensionLevel().setVisible(false);
      view.getPnlDescensionLock().setVisible(false);
    }
  }

  private void updateCommanderDetails(CommanderCardPanel panel) {
    RogueDeck deck = panel.getCommander();

    if (panel.isLocked()) {
      // Show unlock condition for locked commanders
      view.getLblCommanderName().setText("???");
      view.getLblDescriptionLabel().setText("Unlock:");
      view.getLblDescriptionLock().setVisible(true);
      String unlockDesc = deck.getUnlockDescription();
      if (unlockDesc != null && !unlockDesc.isEmpty()) {
        view.getTxtDescription().setText(unlockDesc);
      } else {
        view.getTxtDescription().setText("Locked");
      }
      // Hide theme row for locked commanders
      view.getLblThemeLabel().setVisible(false);
      view.getScrollTheme().setVisible(false);
    } else {
      // Show normal details for unlocked commanders
      view.getLblCommanderName().setText(deck.getCommanderCardName());
      view.getLblDescriptionLabel().setText("Description:");
      view.getLblDescriptionLock().setVisible(false);
      view.getTxtDescription().setText(deck.getDescription());
      view.getTxtTheme().setText(deck.getThemeDescription());
      // Show theme row for unlocked commanders
      view.getLblThemeLabel().setVisible(true);
      view.getScrollTheme().setVisible(true);
    }

    // Force UI refresh for text areas
    view.getLblDescriptionLabel().revalidate();
    view.getLblDescriptionLabel().repaint();
    view.getTxtDescription().revalidate();
    view.getTxtDescription().repaint();
    view.getTxtTheme().revalidate();
    view.getTxtTheme().repaint();
  }

  private void updateDescensionVisibility(CommanderCardPanel panel) {
    // Always reset level state when switching commanders
    selectedDescensionLevel = 0;
    view.getChkDescension().setSelected(false);
    view.getPnlDescensionLevel().setVisible(false);

    if (panel == null || panel.isLocked()) {
      view.getChkDescension().setVisible(false);
      view.getPnlDescensionLock().setVisible(false);
      return;
    }

    RogueMetaProgress progress = RogueMetaProgress.getInstance();
    String commanderName = panel.getCommander().getCommanderCardName();

    if (!progress.isDescensionModeUnlocked()) {
      view.getLblDescensionLockText().setText(
          "Descension Mode - Win a Run with 3 different Commanders to unlock.");
      view.getPnlDescensionLock().setVisible(true);
      view.getChkDescension().setVisible(false);
      return;
    }

    int maxUnlocked = progress.getMaxDescensionUnlocked(commanderName);
    if (maxUnlocked == 0) {
      view.getLblDescensionLockText().setText(
          "Win a Run with " + commanderName + " to unlock Descension Mode for this Commander.");
      view.getPnlDescensionLock().setVisible(true);
      view.getChkDescension().setVisible(false);
    } else {
      view.getPnlDescensionLock().setVisible(false);
      view.getChkDescension().setVisible(true);
    }
  }

  private void onDescensionToggled() {
    if (view.getChkDescension().isSelected()) {
      selectedDescensionLevel = 1;
      view.getLblDescensionLock().setVisible(false);
      view.getPnlDescensionLevel().setVisible(true);
      updateDescensionDisplay();
    } else {
      selectedDescensionLevel = 0;
      view.getPnlDescensionLevel().setVisible(false);
      view.getBtnBeginRun().setEnabled(selectedDeck != null);
    }
  }

  private void changeDescensionLevel(int delta) {
    int newLevel = selectedDescensionLevel + delta;
    newLevel = Math.max(1, Math.min(DescensionLevel.getMaxLevel(), newLevel));
    selectedDescensionLevel = newLevel;
    updateDescensionDisplay();
  }

  private void updateDescensionDisplay() {
    if (selectedDeck == null) return;
    String commanderName = selectedDeck.getCommanderCardName();
    int maxUnlocked = RogueMetaProgress.getInstance().getMaxDescensionUnlocked(commanderName);
    DescensionLevel dl = DescensionLevel.forLevel(selectedDescensionLevel);
    boolean locked = selectedDescensionLevel > maxUnlocked;

    String levelName = dl != null ? "Level " + selectedDescensionLevel + " \u2014 " + dl.name : "Level " + selectedDescensionLevel;
    view.getLblDescensionLevel().setText(levelName);
    view.getLblDescensionLock().setVisible(locked);
    view.getBtnDescensionDown().setVisible(selectedDescensionLevel > 1);
    view.getBtnDescensionUp().setVisible(!locked && selectedDescensionLevel < DescensionLevel.getMaxLevel());

    if (!locked) {
      view.getLblDescensionDesc().setText(dl != null ? dl.description : "");
      view.getBtnBeginRun().setEnabled(true);
    } else {
      view.getLblDescensionDesc().setText(
          "Win a Run with " + commanderName + " in Descension Level "
          + (selectedDescensionLevel - 1) + " to unlock Level " + selectedDescensionLevel + ".");
      view.getBtnBeginRun().setEnabled(false);
    }
  }

  private void beginNewRun() {
    if (selectedDeck == null) {
      System.err.println("Error: No commander selected");
      return;
    }

    // Record abandoned run history if there's an active run
    RogueRun existingRun = CSubmenuRogueMap.SINGLETON_INSTANCE.getCurrentRun();
    if (existingRun != null && existingRun.getRunState() == RogueRunState.STARTED) {
      RogueMetaProgress.getInstance().addRunHistoryEntry(
          RogueRunHistoryEntry.fromRun(existingRun, "ABANDONED", ""));
    }

    // Create new run and snapshot active echo boons
    RogueRun newRun = new RogueRun(selectedDeck);
    newRun.setDescensionLevel(selectedDescensionLevel);
    newRun.snapshotEchoBoons(RogueMetaProgress.getInstance());

    // Generate path and apply run start effects
    RoguePathGenerator.generateRandomPath(newRun);
    RogueEffectComposite.INSTANCE.onRunStart(newRun);

    // Generate unique name for the run (used as filename)
    // Format: DeckName_Timestamp (e.g., "MeriaRogueCommander_12-11-25_143022")
    String runName = selectedDeck.getName() + "_" + System.currentTimeMillis();
    newRun.setName(runName);

    // Track meta progress
    progress.onRunStarted(selectedDeck.getCommanderCardName());

    // Save the run
    RogueIO.saveRun(newRun);

    // Set as current run in the map controller
    CSubmenuRogueMap.SINGLETON_INSTANCE.setCurrentRun(newRun);

    // Navigate to the Rogue Map
    CHomeUI.SINGLETON_INSTANCE.itemClick(EDocID.HOME_ROGUEMAP);
  }
}
