package forge.screens.home.rogue;

import forge.LobbyPlayer;
import forge.Singletons;
import forge.deck.CardPool;
import forge.deck.Deck;
import forge.deck.io.DeckSerializer;
import forge.game.GameType;
import forge.game.player.RegisteredPlayer;
import forge.gamemodes.match.HostedMatch;
import forge.gamemodes.rogue.*;
import forge.gui.GuiBase;
import forge.gui.SOverlayUtils;
import forge.gui.framework.EDocID;
import forge.gui.framework.FScreen;
import forge.gui.framework.ICDoc;
import forge.item.IPaperCard;
import forge.item.PaperCard;
import forge.localinstance.achievements.RogueCommanderAchievements;
import forge.model.FModel;
import forge.localinstance.properties.ForgeConstants;
import forge.player.GamePlayerUtil;
import forge.screens.deckeditor.CDeckEditorUI;
import forge.screens.deckeditor.controllers.CEditorRogue;
import forge.screens.home.CHomeUI;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;
import javax.swing.SwingUtilities;

/**
 * Controls the "rogue map" submenu in the home UI.
 *
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 */
public enum CSubmenuRogueMap implements ICDoc {
  SINGLETON_INSTANCE;

  private final ActionListener actEnterNode = arg0 -> enterNode();
  private final ActionListener actEditDeck = arg0 -> editDeck();
  private final VSubmenuRogueMap view = VSubmenuRogueMap.SINGLETON_INSTANCE;

  // Test run data (for MVP - will be replaced with proper loading later)
  private RogueRun currentRun;

  @Override
  public void update() {
    // If no run exists in memory, try to load from saved runs
    if (currentRun == null) {
      currentRun = loadMostRecentContinuableRun();
    }

    // If still no run (no saved runs or all completed/failed), navigate to start screen
    if (currentRun == null) {
      CHomeUI.SINGLETON_INSTANCE.itemClick(EDocID.HOME_ROGUESTART);
      return;
    }

    updateView();
    SwingUtilities.invokeLater(() -> {
      view.getBtnEnterNode().requestFocusInWindow();
      showTutorials();
    });
  }

  private void showTutorials() {
    RogueTutorialHelper.showIfNotSeen(RogueTutorial.MAP_NAVIGATION);

    if (currentRun == null || currentRun.getCurrentNode() == null) {
      return;
    }

    // Check if player just won their first battle and show post-battle tutorial if so
    List<RoguePathNode> completedNodes = currentRun.getPath().getNodes().stream()
        .filter(n -> n instanceof NodePlanebound && n.isCompleted())
        .toList();
    if (!completedNodes.isEmpty()) {
      RogueTutorialHelper.showIfNotSeen(RogueTutorial.POST_BATTLE);
    }

    // Check node types of current row and show tutorials if not seen yet
    int currentRow = currentRun.getCurrentNode().getRowIndex();
    for (RoguePathNode node : currentRun.getPath().getNodesInRow(currentRow)) {
      if (node instanceof NodePlanebound planebound
          && planebound.getPlaneboundType() == RoguePlaneboundType.ELITE) {
        RogueTutorialHelper.showIfNotSeen(RogueTutorial.ELITE_ENCOUNTER);
      } else if (node instanceof NodePlanebound) {
        RogueTutorialHelper.showIfNotSeen(RogueTutorial.PRE_BATTLE);
      } else if (node instanceof NodeSanctum) {
        RogueTutorialHelper.showIfNotSeen(RogueTutorial.SANCTUM);
      } else if (node instanceof NodeBazaar) {
        RogueTutorialHelper.showIfNotSeen(RogueTutorial.BAZAAR);
      }
    }
  }

  /**
   * Load the most recent continuable run from saved files. A continuable run is one that is in
   * STARTED state (not WON or LOST).
   *
   * @return The most recent continuable run, or null if none exist.
   */
  private RogueRun loadMostRecentContinuableRun() {
    List<RogueRun> allRuns = RogueIO.loadAllRuns();
    if (allRuns.isEmpty()) {
      return null;
    }

    // Filter for continuable runs (STARTED state, not failed)
    List<RogueRun> continuableRuns = allRuns.stream()
        .filter(run -> run.getRunState() == RogueRunState.STARTED && !run.isRunFailed())
        .toList();

    if (continuableRuns.isEmpty()) {
      return null;
    }

    // If only one continuable run, use it
    if (continuableRuns.size() == 1) {
      return continuableRuns.get(0);
    }

    // Multiple continuable runs - pick the most recent by filename timestamp
    // Filename format: {DeckName}_{timestamp} where timestamp is System.currentTimeMillis()
    return continuableRuns.stream()
        .max((r1, r2) -> {
          // Extract timestamp from filename (after last underscore)
          String name1 = r1.getName();
          String name2 = r2.getName();
          long ts1 = extractTimestamp(name1);
          long ts2 = extractTimestamp(name2);
          return Long.compare(ts1, ts2);
        })
        .orElse(continuableRuns.get(0));
  }

  /**
   * Extract timestamp from run filename. Format: {DeckName}_{timestamp}
   */
  private long extractTimestamp(String filename) {
    int lastUnderscore = filename.lastIndexOf('_');
    if (lastUnderscore >= 0 && lastUnderscore < filename.length() - 1) {
      try {
        return Long.parseLong(filename.substring(lastUnderscore + 1));
      } catch (NumberFormatException e) {
        return 0;
      }
    }
    return 0;
  }

  @Override
  public void register() {
    // TODO document why this method is empty
  }

  @Override
  public void initialize() {
    view.getBtnEnterNode().addActionListener(actEnterNode);
    view.getBtnEditDeck().addActionListener(actEditDeck);
    view.getBtnDevWinRun().addActionListener(e -> devWinRun());
    view.getPathVisualizer().setNodeClickHandler(this::handleNodeClick);
  }

  /**
   * Handle click on a node panel.
   */
  private void handleNodeClick(NodePanel panel) {
    if (currentRun == null) {
      return;
    }

    // Get the index of the clicked node
    int nodeIndex = view.getPathVisualizer().getNodePanels().indexOf(panel);
    if (nodeIndex < 0) {
      return;
    }

    RoguePathNode clickedNode = currentRun.getPath().getNode(nodeIndex);
    if (clickedNode == null) {
      return;
    }

    RoguePathNode currentNode = currentRun.getCurrentNode();
    if (currentNode == null) {
      return;
    }

    // Only allow selecting nodes in the current row
    if (clickedNode.getRowIndex() != currentNode.getRowIndex()) {
      return;
    }

    // Only allow selecting reachable nodes in the current row
    List<Integer> visibleInCurrentRow = currentRun.getPath()
        .getVisibleNodesInCurrentRow(currentNode.getRowIndex());
    if (!visibleInCurrentRow.contains(nodeIndex)) {
      return; // Not reachable, ignore click
    }

    // Check if this is a multi-node row - only allow selection changes if multiple nodes
    List<RoguePathNode> currentRowNodes = currentRun.getPath()
        .getNodesInRow(currentNode.getRowIndex());
    if (currentRowNodes.size() == 1) {
      return; // Single node row - no selection needed, already auto-selected
    }

    // Allow clicking any reachable node in multi-node row to select it
    // Set as current node and update view
    currentRun.setCurrentNodeIndex(nodeIndex);
    updateViewWithSelection();
  }

  private void updateView() {
    view.updateDisplay(currentRun);
    updateViewWithSelection();
  }

  /**
   * Update the view with selection state and button text.
   */
  private void updateViewWithSelection() {
    if (currentRun == null) {
      return;
    }

    // Update path visualizer selection
    view.getPathVisualizer().setSelectedNode(currentRun.getCurrentNodeIndex());

    // Update button state based on current node
    RoguePathNode currentNode = currentRun.getCurrentNode();

    if (currentNode == null) {
      view.getBtnEnterNode().setEnabled(false);
      view.getBtnEnterNode().setText("No Node Available");
      return;
    }

    // Disable button if match already in progress (prevents duplicate match tabs)
    boolean matchInProgress = currentRun.getHostedMatch() != null;
    view.getBtnEnterNode().setEnabled(!matchInProgress);
    view.getBtnEnterNode().setText(getEnterButtonText(currentNode));
  }

  /**
   * Get the appropriate button text for entering a node.
   */
  private String getEnterButtonText(RoguePathNode node) {
    if (node instanceof NodePlanebound) {
      return "Enter " + ((NodePlanebound) node).getRoguePlanebound().planeName();
    } else if (node instanceof NodeSanctum) {
      return "Enter Sanctum";
    } else if (node instanceof NodeBazaar) {
      return "Enter Bazaar";
    } else {
      return "Enter Node";
    }
  }

  private void enterNode() {
    if (currentRun == null) {
      return;
    }

    // Use current node
    RoguePathNode node = currentRun.getCurrentNode();

    if (node == null) {
      return;
    }

    // Handle different node types
    if (node instanceof NodePlanebound nodePlanebound) {
      startMatch(nodePlanebound);
    } else if (node instanceof NodeSanctum nodeSanctum) {
      handleSanctumNode(nodeSanctum);
    } else if (node instanceof NodeBazaar nodeBazaar) {
      handleBazaarNode(nodeBazaar);
    } else if (node instanceof NodeEvent || node instanceof NodeChest) {
      // TODO: Implement these node types
      currentRun.nextNode();
      updateView();
    }
  }

  private void startMatch(NodePlanebound node) {
    // Prevent starting a new match if one is already in progress this session
    if (currentRun.getHostedMatch() != null) {
      return;
    }

    // Show loading overlay
    SwingUtilities.invokeLater(() -> {
      SOverlayUtils.startGameOverlay();
      SOverlayUtils.showOverlay();
    });

    try {
      // Get all plane cards from the centralized cache
      CardPool allPlanes = forge.gamemodes.rogue.RogueConfig.getAllPlanes();

      // Find the designated plane for this node
      String cardPlaneName = node.getRoguePlanebound().planeName();
      PaperCard designatedPlane = null;
      for (PaperCard card : allPlanes.toFlatList()) {
        if (cardPlaneName.equalsIgnoreCase(card.getName())) {
          designatedPlane = card;
          break;
        }
      }

      // Create shared plane deck with the designated plane
      // In Rogue Commander, planeswalk triggers fire but we stay on the same plane
      List<PaperCard> sharedPlaneDeck = new java.util.ArrayList<>();
      if (designatedPlane != null) {
        sharedPlaneDeck.add(designatedPlane);
      } else {
        System.err.println("Warning: Could not find plane card: " + cardPlaneName);
      }

      // Configure Commander + Planechase variants
      Set<GameType> appliedVariants = EnumSet.of(GameType.Commander, GameType.Planechase);

      // Create human player with persistent life
      RegisteredPlayer human = RegisteredPlayer.forVariants(
          2,                   // player count
          appliedVariants,                // applied variants
          currentRun.getCurrentDeck(),    // player's deck
          null,                           // schemes (not used)
          false,                          // is archenemy
          sharedPlaneDeck,                // shared plane deck
          null                            // vanguard avatar
      );

      // Override starting life with persistent life from run
      human.setStartingLife(currentRun.getCurrentLife());

      // Apply boon effects
      RogueMetaProgress progress = RogueMetaProgress.getInstance();

      // Foresight: +1 starting hand card
      int extraCards = progress.getExtraStartingCards();
      if (extraCards > 0) {
        human.setStartingHand(human.getStartingHand() + extraCards);
      }

      // Use the singleton lobbyPlayer for consistent player identification
      // This ensures isMatchWonBy() works correctly in RogueWinLoseController
      LobbyPlayer lobbyPlayer = GamePlayerUtil.getGuiPlayer();
      lobbyPlayer.setName(currentRun.getSelectedRogueDeck().getName());
      lobbyPlayer.setAvatarIndex(currentRun.getSelectedRogueDeck().getAvatarIndex());
      lobbyPlayer.setSleeveIndex(currentRun.getSelectedRogueDeck().getSleeveIndex());
      human.setPlayer(lobbyPlayer);

      // Add descension command zone cards for active levels
      RogueConfig.loadRogueCards();
      int descLevel = currentRun.getDescensionLevel();
      if (descLevel >= 2) {
        List<IPaperCard> descCards = new ArrayList<>();
        PaperCard bloodthirst = FModel.getMagicDb().getCommonCards()
            .getCard("Descension - Bloodthirst");
        if (bloodthirst != null) descCards.add(bloodthirst);
        if (descLevel >= 3) {
          PaperCard taxingMana = FModel.getMagicDb().getCommonCards()
              .getCard("Descension - Taxing Mana");
          if (taxingMana != null) descCards.add(taxingMana);
        }
        if (!descCards.isEmpty()) {
          human.addExtraCardsInCommandZone(descCards);
        }
      }

      // Add Spark Kindle and Fractured Binding command zone cards from boons
      List<IPaperCard> boonCmdCards = new ArrayList<>();
      int kindleLands = progress.getSparkKindleLands();
      if (kindleLands > 0) {
        PaperCard kindle = FModel.getMagicDb().getCommonCards()
            .getCard("Rogue - Spark Kindle " + kindleLands);
        if (kindle != null) boonCmdCards.add(kindle);
      }
      int taxReduction = progress.getCommanderTaxReduction();
      if (taxReduction > 0) {
        PaperCard binding = FModel.getMagicDb().getCommonCards()
            .getCard("Rogue - Fractured Binding " + taxReduction);
        if (binding != null) boonCmdCards.add(binding);
      }
      if (!boonCmdCards.isEmpty()) {
        human.addExtraCardsInCommandZone(boonCmdCards);
      }

      // Load Planebound deck
      Deck planeboundDeck = loadPlaneboundDeck(node.getRoguePlanebound().deckPath());

      // Create AI Planebound opponent
      RegisteredPlayer ai = RegisteredPlayer.forVariants(
          2,                                    // player count
          appliedVariants,                      // applied variants
          planeboundDeck,                       // Planebound deck
          null,                                  // schemes (not used)
          false,                                 // is archenemy
          sharedPlaneDeck,                      // shared plane deck
          null                                   // vanguard avatar
      );
      ai.setPlayer(GamePlayerUtil.createAiPlayer(
          node.getRoguePlanebound().planeboundName(),
          node.getRoguePlanebound().avatarIndex(),
          0));

      // Calculate life based on Planebound rows (not total rows - Sanctum/Bazaar don't count)
      int planeboundRowCount = currentRun.getPath().countPlaneboundRowsUpTo(node.getRowIndex());
      int planeboundLife = 5 * planeboundRowCount;
      ai.setStartingLife(planeboundLife);

      // Start match
      List<RegisteredPlayer> players = Arrays.asList(human, ai);
      HostedMatch hostedMatch = GuiBase.getInterface().hostMatch();
      currentRun.setHostedMatch(hostedMatch);

      hostedMatch.startMatch(
          GameType.RogueCommander,
          appliedVariants,
          players,
          human,
          GuiBase.getInterface().getNewGuiGame()
      );

    } catch (Exception e) {
      e.printStackTrace();
      SwingUtilities.invokeLater(() -> {
        SOverlayUtils.hideOverlay();
        // TODO: Show error message to user
      });
    }

    SwingUtilities.invokeLater(SOverlayUtils::hideOverlay);
  }

  private Deck loadPlaneboundDeck(String deckPath) {
    // Load deck from file path
    // The deckPath is relative to the res directory, e.g., "rogue/planebounds/meria.dck"
    File deckFile = new File(ForgeConstants.RES_DIR, deckPath);

    if (!deckFile.exists()) {
      throw new RuntimeException(
          "Planebound deck not found: " + deckPath + " (full path: " + deckFile.getAbsolutePath()
              + ")");
    }

    return DeckSerializer.fromFile(deckFile);
  }

  public RogueRun getCurrentRun() {
    return currentRun;
  }

  public void setCurrentRun(RogueRun run) {
    this.currentRun = run;
  }

  private void devWinRun() {
    if (currentRun == null) return;
    String commanderName = currentRun.getSelectedRogueDeck().getCommanderCardName();
    currentRun.setRunWon(true);
    RogueMetaProgress progress = RogueMetaProgress.getInstance();
    progress.addEchoes(20);
    progress.addRunHistoryEntry(RogueRunHistoryEntry.fromRun(currentRun, "VICTORY", "[DEV]"));
    progress.onRunCompleted(currentRun, true);
    RogueCommanderAchievements.instance.recordRunWon(commanderName);
    int descLevel = currentRun.getDescensionLevel();
    if (descLevel > 0) {
      progress.recordDescensionWin(commanderName, descLevel);
    }
    RogueCommanderAchievements.instance.evaluateRunAchievements(currentRun);
    progress.checkForNewUnlocks();
    progress.notifyDescensionL1IfFirstWin(commanderName);
    RogueIO.saveRun(currentRun);
    currentRun = null;
    CHomeUI.SINGLETON_INSTANCE.itemClick(EDocID.HOME_ROGUESTART);
  }

  private void editDeck() {
    if (currentRun == null) {
      return;
    }

    // Switch to deck editor screen first (this loads saved layout/tab)
    Singletons.getControl().setCurrentScreen(FScreen.DECK_EDITOR_CONSTRUCTED);

    // Then create and set the Rogue deck editor (this should override whatever tab was loaded)
    CEditorRogue rogueEditor = new CEditorRogue(
        currentRun,
        FScreen.DECK_EDITOR_CONSTRUCTED,
        CDeckEditorUI.SINGLETON_INSTANCE.getCDetailPicture()
    );
    CDeckEditorUI.SINGLETON_INSTANCE.setEditorController(rogueEditor);

    // Now select the Card Catalog tab to show our "Basic Lands Only" catalog
    javax.swing.SwingUtilities.invokeLater(() -> {
      forge.screens.deckeditor.views.VCardCatalog catalog = forge.screens.deckeditor.views.VCardCatalog.SINGLETON_INSTANCE;
      if (catalog.getParentCell() != null) {
        catalog.getParentCell().setSelected(catalog);
      }
    });
  }

  private void handleSanctumNode(NodeSanctum sanctumNode) {
    if (currentRun == null) {
      return;
    }

    // Get current and max life
    int currentLife = currentRun.getCurrentLife();
    int maxLife = currentRun.getStartingLife();
    int healAmount = sanctumNode.getHealAmount();
    int freeRemoves = sanctumNode.getFreeRemoves();

    // Show Sanctum dialog
    SanctumDialog dialog = new SanctumDialog(currentLife, maxLife, healAmount, freeRemoves);
    SanctumDialog.SanctumChoice choice = dialog.show();

    // Handle player's choice
    switch (choice) {
      case HEAL:
        // Heal player by healAmount, capped at maximum life
        currentRun.healLife(healAmount);
        break;

      case REMOVE_CARDS:
        // Add removal credits (allows removing cards from deck later)
        currentRun.addRemovalCredits(freeRemoves);
        break;

      case SKIP:
        // Do nothing
        break;
    }

    // Mark node as completed and move to next
    sanctumNode.setCompleted(true);
    currentRun.nextNode();

    // Save run and update view (use update() to trigger tutorials for next row)
    RogueIO.saveRun(currentRun);
    update();
  }

  private void handleBazaarNode(NodeBazaar bazaarNode) {
    if (currentRun == null) {
      return;
    }

    // Get the rogue deck to draw cards from reward pool
    RogueDeck rogueDeck = currentRun.getSelectedRogueDeck();
    if (rogueDeck == null) {
      System.err.println("ERROR: Could not find rogue deck for current run.");
      currentRun.nextNode();
      updateView();
      return;
    }

    // Generate Bazaar inventory: Draw 8 non-mythic + 2 mythic (base, adjusted by Mythic Collector boon)
    RogueMetaProgress bazaarProgress = RogueMetaProgress.getInstance();
    int extraMythics = bazaarProgress.getExtraMythicCards();
    int baseNonMythics = 8;
    int baseMythics = 2;
    int totalNonMythics = Math.max(0, baseNonMythics - extraMythics);
    int totalMythics = baseMythics + extraMythics;

    int rerollsRemaining = bazaarProgress.getRerollsPerNode(); // Fresh per node
    int currentGold = currentRun.getCurrentGold();
    Set<PaperCard> selectedCards;
    do {
      List<PaperCard> nonMythicCards = rogueDeck.drawRewardOptions(totalNonMythics,
          forge.item.PaperCardPredicates.IS_MYTHIC_RARE.negate());
      List<PaperCard> mythicCards = rogueDeck.drawRewardOptions(totalMythics,
          forge.item.PaperCardPredicates.IS_MYTHIC_RARE);

      List<PaperCard> inventory = new ArrayList<>();
      inventory.addAll(nonMythicCards);
      inventory.addAll(mythicCards);

      if (inventory.isEmpty()) {
        System.err.println("ERROR: No cards available in reward pool for Bazaar.");
        currentRun.nextNode();
        updateView();
        return;
      }

      String rerollLabel = rerollsRemaining > 0 ? "Reroll" : null;
      BazaarDialog dialog = new BazaarDialog(inventory, currentGold, rerollLabel);
      selectedCards = dialog.show();

      if (selectedCards == null) {
        rerollsRemaining--; // Reroll clicked — unchosen cards stay in pool
      }
    } while (selectedCards == null && rerollsRemaining >= 0);

    if (selectedCards == null) {
      selectedCards = new HashSet<>();
    }

    // If player bought cards, add them to deck and deduct gold
    if (!selectedCards.isEmpty()) {
      // Add bought cards to player's deck (same method as card rewards)
      currentRun.addCardsToRun(new ArrayList<>(selectedCards));

      // Calculate and deduct gold cost using shared pricing
      int totalCost = BazaarPricing.calculateTotalCost(selectedCards);
      currentRun.setCurrentGold(currentGold - totalCost);

      // Remove ONLY the purchased cards from reward pool (not all inventory)
      rogueDeck.removeFromRewardPool(new ArrayList<>(selectedCards));
    }
    // If nothing purchased, don't remove anything - cards stay in pool for future Bazaars

    // Mark node as completed and move to next
    bazaarNode.setCompleted(true);
    currentRun.nextNode();

    // Evaluate achievements after Bazaar (deck/gold may have changed)
    RogueCommanderAchievements.instance.evaluateRunAchievements(currentRun);

    // Save run and update view (use update() to trigger tutorials for next row)
    RogueIO.saveRun(currentRun);
    update();
  }
}
