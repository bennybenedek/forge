package forge.screens.home.rogue;

import forge.ImageCache;
import forge.ImageKeys;
import forge.gamemodes.rogue.*;
import forge.gamemodes.rogue.effect.*;
import forge.gamemodes.rogue.npc.NPCContext;
import forge.gamemodes.rogue.npc.NPC;
import forge.gamemodes.rogue.npc.NPCEncounterComposite;
import forge.gamemodes.rogue.path.RoguePathGenerator;
import forge.gui.GuiBase;
import forge.gui.framework.EDocID;
import forge.gui.framework.ICDoc;
import forge.item.PaperCard;
import forge.localinstance.properties.ForgePreferences;
import forge.screens.home.CHomeUI;
import forge.toolbox.FOptionPane;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;

/**
 * Controls the Rogue Commander start screen. Handles commander selection via card grid and new run
 * creation.
 */
public enum CSubmenuRogueStart implements ICDoc {
  SINGLETON_INSTANCE;
  private static final String HIDDEN_CARD_KEY = ImageKeys.getTokenKey(ImageKeys.HIDDEN_CARD);

  private final VSubmenuRogueStart view = VSubmenuRogueStart.SINGLETON_INSTANCE;
  private RogueDeck selectedDeck;
  private int selectedDescensionLevel = 0;
  private boolean updatingDescensionUi;

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
    if (ForgePreferences.DEV_MODE) {
      view.getBtnDevUnlockAll().addActionListener(e -> devToggleUnlockAll());
      view.getBtnDevNPCProgress().addActionListener(e -> devEditNPCProgress());
    }
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

  private void devToggleUnlockAll() {
    RogueMetaProgress progress = RogueMetaProgress.getInstance();
    boolean newState = !progress.isDevUnlockAll();
    progress.setDevUnlockAll(newState);
    view.getBtnDevUnlockAll().setText(newState ? "[Dev] Lock Commanders" : "[Dev] Unlock All");
    loadAvailableCommanders();
  }

  private void devEditNPCProgress() {
    Map<NPC, Integer> updatedLevels = new NPCProgressDialog(RogueMetaProgress.getInstance()).show();
    if (updatedLevels == null) {
      return;
    }

    RogueMetaProgress progress = RogueMetaProgress.getInstance();
    for (Map.Entry<NPC, Integer> entry : updatedLevels.entrySet()) {
      progress.setNPCLevel(entry.getKey().id, entry.getValue());
    }

    FOptionPane.showMessageDialog("NPC progression levels updated.", "Dev NPC Progress");
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

    var progress = RogueMetaProgress.getInstance();

    // Show RUN_COMPLETE tutorial after first completed run
    if (progress.getTotalRunsCompleted() > 0) {
      RogueTutorialHelper.showIfNotSeen(RogueTutorial.RUN_COMPLETE);
    }

    // Show Descension tutorial once when it becomes unlocked
    if (progress.isDescensionModeUnlocked()) {
      RogueTutorialHelper.showIfNotSeen(RogueTutorial.DESCENSION_UNLOCKED);
    }
  }

  private void loadAvailableCommanders() {
    List<RogueDeck> availableDecks = RogueConfig.loadRogueDecks();
    String previousCommanderName = selectedDeck != null ? selectedDeck.getCommanderCardName() : null;
    int previousDescensionLevel = selectedDescensionLevel;

    // Sort commanders: unlocked first (alphabetically), then locked (alphabetically)
    availableDecks.sort(Comparator
        .comparing((RogueDeck d) -> !d.isUnlocked())  // false (unlocked) before true (locked)
        .thenComparing(RogueDeck::getName));

    // Clear existing commander panels
    view.getCommanderGridPanel().clear();

    // Create card panel for each commander
    CommanderCardPanel firstUnlockedPanel = null;
    CommanderCardPanel previousCommanderPanel = null;
    for (RogueDeck deck : availableDecks) {
      CommanderCardPanel cardPanel = new CommanderCardPanel(deck, view);

      // Set selection callback to update details and handle single-selection
      cardPanel.setSelectionCallback(this::onCommanderSelected);

      view.getCommanderGridPanel().addCommanderPanel(cardPanel);

      // Track first unlocked commander
      if (firstUnlockedPanel == null && !cardPanel.isLocked()) {
        firstUnlockedPanel = cardPanel;
      }
      if (previousCommanderPanel == null && !cardPanel.isLocked()
          && previousCommanderName != null
          && previousCommanderName.equals(cardPanel.getCommander().getCommanderCardName())) {
        previousCommanderPanel = cardPanel;
      }
    }

    requestHiddenCardImageForLockedPanels();

    // Restore previous commander selection when returning to this view; otherwise use first unlocked.
    CommanderCardPanel panelToSelect = previousCommanderPanel != null ? previousCommanderPanel : firstUnlockedPanel;
    if (panelToSelect != null) {
      panelToSelect.setSelected(true);
      selectedDeck = panelToSelect.getCommander();
      selectedDescensionLevel = previousDescensionLevel;
      updateCommanderDetails(panelToSelect);
      updateDescensionVisibility(panelToSelect, previousCommanderPanel != null);
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
        .setToolTipText(hasCompletedRuns ? null : "Unlock Codex by completing your first Run.");

    // Refresh layout
    view.getCommanderGridPanel().revalidate();
    view.getCommanderGridPanel().repaint();
  }

  private void requestHiddenCardImageForLockedPanels() {
    boolean hasLockedPanels = view.getCommanderPanels().stream().anyMatch(CommanderCardPanel::isLocked);
    if (!hasLockedPanels) {
      return;
    }

    if (ImageCache.getOriginalImage(HIDDEN_CARD_KEY, false, null) != null) {
      refreshLockedCommanderPanels();
      return;
    }

    GuiBase.getInterface().getImageFetcher().fetchImage(HIDDEN_CARD_KEY, this::refreshLockedCommanderPanels);
  }

  private void refreshLockedCommanderPanels() {
    for (CommanderCardPanel panel : view.getCommanderPanels()) {
      panel.refreshHiddenCardImage();
    }
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
      updateDescensionVisibility(clickedPanel, false);
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

  private void updateDescensionVisibility(CommanderCardPanel panel, boolean preserveDescension) {
    int previousDescensionLevel = preserveDescension ? selectedDescensionLevel : 0;
    selectedDescensionLevel = 0;
    updatingDescensionUi = true;
    view.getChkDescension().setSelected(false);
    updatingDescensionUi = false;
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
      if (previousDescensionLevel > 0) {
        selectedDescensionLevel = Math.min(previousDescensionLevel,
            Math.min(maxUnlocked, DescensionLevel.getMaxLevel()));
        updatingDescensionUi = true;
        view.getChkDescension().setSelected(true);
        updatingDescensionUi = false;
        view.getPnlDescensionLevel().setVisible(true);
        updateDescensionDisplay();
      }
    }
  }

  private void onDescensionToggled() {
    if (updatingDescensionUi) {
      return;
    }
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

    String levelName = dl != null
        ? String.format("Level %d - %s", selectedDescensionLevel, dl.name)
        : String.format("Level %d", selectedDescensionLevel);
    view.getLblDescensionLevel().setText(levelName);
    view.getLblDescensionLock().setVisible(locked);
    view.getBtnDescensionDown().setVisible(selectedDescensionLevel > 1);
    view.getBtnDescensionUp().setVisible(!locked && selectedDescensionLevel < DescensionLevel.getMaxLevel());

    if (!locked) {
      view.getLblDescensionDesc().setText(dl != null ? dl.description : "");
      view.getBtnBeginRun().setEnabled(true);
    } else {
      view.getLblDescensionDesc().setText(String.format(
          "Win a Run with %s in Descension Level %d to unlock Level %d.",
          commanderName, selectedDescensionLevel - 1, selectedDescensionLevel));
      view.getBtnBeginRun().setEnabled(false);
    }
  }

  private void beginNewRun() {
    if (selectedDeck == null) {
      System.err.println("Error: No commander selected");
      return;
    }

    var progress = RogueMetaProgress.getInstance();

    // Warn if there's an active run that will be abandoned
    RogueRun existingRun = CSubmenuRogueMap.SINGLETON_INSTANCE.getCurrentRun();
    if (existingRun != null && existingRun.getRunState() == RogueRunState.STARTED) {
      boolean confirmed = FOptionPane.showConfirmDialog(
          "You have an active Run in progress.\nStarting a new Run will abandon it. Continue?",
          "Abandon Current Run",
          "Start New Run",
          "Cancel",
          false
      );
      if (!confirmed) return;

      existingRun.getRunTimer().stop();
      progress.addRunHistoryEntry(
          RogueRunHistoryEntry.fromRun(existingRun, "ABANDONED", ""));
    }

    // Delete all old run save files — history is preserved in RogueMetaProgress
    for (RogueRun old : RogueIO.loadAllRuns()) {
      RogueIO.deleteRun(old);
    }

    // Create new run and snapshot active echo boons
    RogueRun newRun = new RogueRun(selectedDeck);
    newRun.getRunTimer().start();
    newRun.setDescensionLevel(selectedDescensionLevel);
    newRun.snapshotEchoBoons(progress);

    // Generate path and apply run start effects
    RoguePathGenerator.generateRandomPath(newRun);
    RogueEffectComposite.INSTANCE.onRunStart(newRun);

    // Show NPC encounter dialogs (e.g. Tyvar offering npcEffect choices)
    for (NPCContext ctx : NPCEncounterComposite.INSTANCE.onRunStart(progress, newRun)) {
      NPCEffect chosen = showRunStartNpcDialog(progress, newRun, ctx);
      if (chosen == null) {
        continue;
      }

      if (chosen.getEffectType() == RogueEffect.EffectType.ONESHOT) {
        EffectResultContext effectCtx = new EffectResultContext();
        chosen.applyEffect(newRun, effectCtx);
        if (!EffectResultHelper.handleTrigger(effectCtx, newRun)) {
          continue;
        }
        CodexHelper.recordAcquiredCards(newRun, effectCtx.addedCards);
        CodexHelper.recordTraitAcquired(effectCtx.gainedWoundEffect);
        showNpcResultIfNeeded(chosen, effectCtx);
      } else {
        newRun.addNPCEffect(chosen);
      }
    }
    CodexHelper.recordTraitsAcquired(newRun.getActiveWoundEffects());

    // Generate unique name for the run (used as filename)
    // Format: DeckName_Timestamp (e.g., "MeriaRogueCommander_12-11-25_143022")
    String runName = selectedDeck.getName() + "_" + System.currentTimeMillis();
    newRun.setName(runName);

    // Track meta progress
    RogueStats.fireOnRunStarted(newRun, progress);

    // Save the run
    RogueIO.saveRun(newRun);

    // Set as current run in the map controller
    CSubmenuRogueMap.SINGLETON_INSTANCE.setCurrentRun(newRun);

    // Navigate to the Rogue Map
    CHomeUI.SINGLETON_INSTANCE.itemClick(EDocID.HOME_ROGUEMAP);
  }

  private NPCEffect showRunStartNpcDialog(RogueMetaProgress progress, RogueRun newRun, NPCContext ctx) {
    NPCContext effectiveCtx = maybeOverrideNpcChoices(ctx);
    NPCDialog.DialogResult result;
    boolean rerollRequested;
    do {
      ChoiceRerollContext rerollCtx = new ChoiceRerollContext();
      RogueEffectComposite.INSTANCE.onBeforeNpcBoons(rerollCtx, newRun);
      CodexHelper.recordTraitChoices(effectiveCtx.choices().stream()
          .map(NPCContext.NPCChoice::npcEffect)
          .toList());
      result = new NPCDialog(effectiveCtx, rerollCtx).show();
      rerollRequested = result.rerollRequested();

      if (rerollRequested) {
        RogueEffectComposite.INSTANCE.onChoiceReroll(rerollCtx, newRun);
        List<NPCContext> rerolledContexts = NPCEncounterComposite.INSTANCE.onRunStart(progress, newRun);
        if (!rerolledContexts.isEmpty()) {
          effectiveCtx = maybeOverrideNpcChoices(rerolledContexts.get(0));
        }
      }
    } while (rerollRequested);

    NPCEffect choice = result.choice();
    CodexHelper.recordTraitAcquired(choice);
    return choice;
  }

  private NPCContext maybeOverrideNpcChoices(NPCContext ctx) {
    if (!ForgePreferences.DEV_MODE || ctx == null || ctx.choices().size() != 3) {
      return ctx;
    }
    if (ctx.choices().stream().anyMatch(choice -> choice == null || choice.npcEffect() == null)) {
      return ctx;
    }
    return new NPCChoiceOverrideDialog(ctx).show();
  }

  private void showNpcResultIfNeeded(NPCEffect effect, EffectResultContext ctx) {
    List<NodeResultPanel.CardSection> sections = buildNpcResultSections(effect, ctx);
    if (sections.isEmpty()) {
      return;
    }

    CSubmenuRogueMap.SINGLETON_INSTANCE.showNodeResultDialog(
        "NPC Reward Gained", "You gained a reward from " + effect.getDisplayName() + ".", sections);
  }

  private List<NodeResultPanel.CardSection> buildNpcResultSections(NPCEffect effect, EffectResultContext ctx) {
    Set<String> excludedNames = new HashSet<>(getPreviewedCardNames(effect));
    excludedNames.addAll(getCardNames(ctx.candidateCards));

    List<NodeResultPanel.CardSection> sections = new ArrayList<>();
    List<PaperCard> removedCards = filterResultCards(ctx.removedCards, excludedNames);
    if (!removedCards.isEmpty()) {
      sections.add(new NodeResultPanel.CardSection("Cards removed:", removedCards));
    }

    List<PaperCard> addedCards = filterResultCards(ctx.addedCards, excludedNames);
    if (!addedCards.isEmpty()) {
      sections.add(new NodeResultPanel.CardSection("Cards added:", addedCards));
    }
    return sections;
  }

  private Set<String> getPreviewedCardNames(NPCEffect effect) {
    Set<String> cardNames = new HashSet<>();
    for (PreviewReference reference : effect.getPreviewReferences()) {
      if (reference.type() == PreviewReferenceType.CARD) {
        String cardName = TextHelper.extractCardNameFromReference(reference.token());
        if (!cardName.isBlank()) {
          cardNames.add(cardName);
        }
      }
    }
    return cardNames;
  }

  private Set<String> getCardNames(List<PaperCard> cards) {
    Set<String> cardNames = new HashSet<>();
    if (cards == null) {
      return cardNames;
    }

    for (PaperCard card : cards) {
      if (card != null) {
        cardNames.add(card.getName());
      }
    }
    return cardNames;
  }

  private List<PaperCard> filterResultCards(List<PaperCard> cards, Set<String> excludedNames) {
    if (cards == null || cards.isEmpty()) {
      return List.of();
    }

    List<PaperCard> resultCards = new ArrayList<>();
    for (PaperCard card : cards) {
      if (card != null && !excludedNames.contains(card.getName())) {
        resultCards.add(card);
      }
    }
    return resultCards;
  }
}
