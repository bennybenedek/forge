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
import forge.gamemodes.rogue.effect.*;
import forge.gamemodes.rogue.path.*;
import forge.gui.GuiBase;
import forge.gui.SOverlayUtils;
import forge.gui.framework.EDocID;
import forge.gui.framework.FScreen;
import forge.gui.framework.ICDoc;
import forge.item.PaperCard;
import forge.localinstance.achievements.RogueCommanderAchievements;
import forge.localinstance.properties.ForgeConstants;
import forge.localinstance.properties.ForgePreferences;
import forge.player.GamePlayerUtil;
import forge.screens.deckeditor.CDeckEditorUI;
import forge.screens.deckeditor.controllers.CEditorRogue;
import forge.screens.home.CHomeUI;
import forge.toolbox.FOptionPane;
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
      } else if (node instanceof NodeEvent) {
        RogueTutorialHelper.showIfNotSeen(RogueTutorial.EVENT);
      } else if (node instanceof NodeChest) {
        RogueTutorialHelper.showIfNotSeen(RogueTutorial.CHEST);
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
    view.getBtnDevNextNode().addActionListener(e -> devNextNode());
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
      PathUpdateContext pathCtx = new PathUpdateContext();
      RogueEffectComposite.INSTANCE.onPathUpdate(pathCtx, currentRun);
      String planeName = pathCtx.hidePlanes ? "???" : ((NodePlanebound) node).getRoguePlanebound().planeName();
      return "Enter " + planeName;
    } else if (node instanceof NodeSanctum) {
      return "Enter Sanctum";
    } else if (node instanceof NodeBazaar) {
      return "Enter Bazaar";
    } else if (node instanceof NodeEvent) {
      return "Enter Event";
    } else if (node instanceof NodeChest) {
      return "Open Chest";
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
      handlePlaneboundNode(nodePlanebound);
    } else if (node instanceof NodeSanctum nodeSanctum) {
      handleSanctumNode(nodeSanctum);
    } else if (node instanceof NodeBazaar nodeBazaar) {
      handleBazaarNode(nodeBazaar);
    } else if (node instanceof NodeEvent nodeEvent) {
      handleEventNode(nodeEvent);
    } else if (node instanceof NodeChest nodeChest) {
      handleChestNode(nodeChest);
    }
  }

  private void handlePlaneboundNode(NodePlanebound node) {
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

      // Use the singleton lobbyPlayer for consistent player identification
      // This ensures isMatchWonBy() works correctly in RogueWinLoseController
      LobbyPlayer lobbyPlayer = GamePlayerUtil.getGuiPlayer();
      lobbyPlayer.setName(currentRun.getSelectedRogueDeck().getName());
      lobbyPlayer.setAvatarIndex(currentRun.getSelectedRogueDeck().getAvatarIndex());
      lobbyPlayer.setSleeveIndex(currentRun.getSelectedRogueDeck().getSleeveIndex());
      human.setPlayer(lobbyPlayer);

      // Apply all match start effects (boons + descension)
      RogueEffectComposite.INSTANCE.onMatchStart(human, currentRun);

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
      SwingUtilities.invokeLater(SOverlayUtils::hideOverlay);
    }

    SwingUtilities.invokeLater(SOverlayUtils::hideOverlay);
  }

  private void handleSanctumNode(NodeSanctum sanctumNode) {
    if (currentRun == null) {
      return;
    }

    // Get current and max life
    int healAmount = sanctumNode.getHealAmount();
    int freeRemoves = sanctumNode.getFreeRemoves();

    // Show Sanctum dialog
    SanctumDialog dialog = new SanctumDialog(healAmount, freeRemoves);
    SanctumDialog.SanctumChoice choice = dialog.show();

    // Handle player's choice
    switch (choice) {
      case HEAL:
        // Heal player and cure all wounds
        currentRun.gainLife(healAmount);
        currentRun.clearWounds();
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

    // Track stats and check for unlocks
    RogueStats.fireOnSideNodeCompleted(currentRun, RogueMetaProgress.getInstance());

    // Save run and update view (use update() to trigger tutorials for next row)
    RogueIO.saveRun(currentRun);
    update();
  }

  private void handleBazaarNode(NodeBazaar bazaarNode) {
    if (currentRun == null) {
      return;
    }

    runBazaarShopping();

    // Mark node as completed and move to next
    bazaarNode.setCompleted(true);
    currentRun.nextNode();

    // Evaluate achievements after Bazaar (deck/gold may have changed)
    RogueCommanderAchievements.instance.evaluateRunAchievements(currentRun);

    // Track stats and check for unlocks
    RogueStats.fireOnSideNodeCompleted(currentRun, RogueMetaProgress.getInstance());

    // Save run and update view (use update() to trigger tutorials for next row)
    RogueIO.saveRun(currentRun);
    update();
  }

  /** Run Bazaar shopping UI and apply purchases. Reusable by Event triggers. */
  private void runBazaarShopping() {
    RogueDeck rogueDeck = currentRun.getSelectedRogueDeck();
    if (rogueDeck == null) {
      System.err.println("ERROR: Could not find rogue deck for current run.");
      return;
    }

    CardSelectionContext bazaarCtx = new CardSelectionContext();
    RogueEffectComposite.INSTANCE.onCardSelection(bazaarCtx, currentRun);
    int baseNonMythics = 8;
    int baseMythics = 2;
    int totalNonMythics = Math.max(0, baseNonMythics - bazaarCtx.extraMythics);
    int totalMythics = baseMythics + bazaarCtx.extraMythics;

    int rerollsRemaining = bazaarCtx.rerolls;
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
        return;
      }

      String rerollLabel = rerollsRemaining > 0 ? "Reroll" : null;
      BazaarDialog dialog = new BazaarDialog(inventory, currentGold, rerollLabel);
      selectedCards = dialog.show();

      if (selectedCards == null) {
        rerollsRemaining--;
      }
    } while (selectedCards == null && rerollsRemaining >= 0);

    if (selectedCards == null) {
      selectedCards = new HashSet<>();
    }

    if (!selectedCards.isEmpty()) {
      currentRun.addCardsToRun(new ArrayList<>(selectedCards));
      int totalCost = BazaarPricing.calculateTotalCost(selectedCards);
      currentRun.setCurrentGold(currentGold - totalCost);
      rogueDeck.removeFromRewardPool(new ArrayList<>(selectedCards));
    }
  }

  private void handleEventNode(NodeEvent eventNode) {
    if (currentRun == null) return;

    RogueEvent event = eventNode.getEvent();
    if (event == null) {
      currentRun.nextNode();
      updateView();
      return;
    }

    // DEV: allow picking which event to test
    if (ForgePreferences.DEV_MODE) {
      RogueEvent picked = (RogueEvent) javax.swing.JOptionPane.showInputDialog(
          null, "Override event:", "[DEV] Pick Event",
          javax.swing.JOptionPane.PLAIN_MESSAGE, null,
          RogueEvent.values(), event);
      if (picked != null) event = picked;
    }

    EventDialog eventDialog = new EventDialog(event);
    RogueEvent.EventChoice choice = eventDialog.show();

    if (choice != null) {
      EventBoon boon = choice.effect();
      NodeResultContext ctx = new NodeResultContext();
      if (boon.getEffectType() == RogueEffect.EffectType.ONESHOT) {
        boon.applyEffect(currentRun, ctx);

        if (ctx.trigger != null) switch (ctx.trigger) {
          case BAZAAR:
            runBazaarShopping();
            break;
          case PLANEBOUND:
            if (ctx.planebound != null) {
              eventNode.setEventPlanebound(ctx.planebound);
              NodePlanebound tempNode = new NodePlanebound(ctx.planebound);
              tempNode.setRowIndex(eventNode.getRowIndex());
              handlePlaneboundNode(tempNode);
              return;  // win/lose controller handles completion
            }
            break;
          case CHEST:
            eventNode.setCompleted(true);
            handleChestNode(new NodeChest());
            return;
          case SANCTUM:
            eventNode.setCompleted(true);
            handleSanctumNode(new NodeSanctum());
            return;
          case CARD_REMOVAL:
            List<PaperCard> deckCards = currentRun.getCurrentDeck().getMain().toFlatList();
            String cmdName = currentRun.getSelectedRogueDeck().getCommanderCardName();
            deckCards.removeIf(c -> c.getName().equals(cmdName)
                || c.getRules().getType().isBasicLand());
            CardSelectionDialog cardSelectionDialog = new CardSelectionDialog(
                "Planar Sacrifice", "Choose " + ctx.removeCount + " cards to remove.",
                deckCards, ctx.removeCount);
            List<PaperCard> removed = cardSelectionDialog.show();
            for (PaperCard card : removed)
              currentRun.getCurrentDeck().getMain().remove(card);
            ctx.removedCards = removed;
            if (ctx.drawCount > 0) {
              RogueDeck rd = currentRun.getSelectedRogueDeck();
              List<PaperCard> added = rd.drawRewardOptions(ctx.drawCount, null);
              currentRun.addCardsToRun(added);
              rd.removeFromRewardPool(added);
              ctx.addedCards = added;
            }
            break;
          default:
            break;
        }

      } else {
        currentRun.addEventBoon(boon);
      }

      // Build result display with card sections if applicable
      List<NodeResultPanel.CardSection> sections = new ArrayList<>();
      if (ctx.removedCards != null && !ctx.removedCards.isEmpty())
        sections.add(new NodeResultPanel.CardSection("Cards removed:", ctx.removedCards));
      if (ctx.addedCards != null && !ctx.addedCards.isEmpty())
        sections.add(new NodeResultPanel.CardSection("Cards added:", ctx.addedCards));

      String resultText = choice.resultText();
      if (ctx.gainedWound != null) {
        resultText += ": " + ctx.gainedWound.getDisplayName()
            + " \u2014 " + ctx.gainedWound.getDescription();
      } else if (choice.effect() == EventBoon.GAIN_WOUND) {
        resultText = "You already bear all wounds.";
      }

      NodeResultPanel resultPanel = new NodeResultPanel(resultText, sections);
      FOptionPane optionPane = new FOptionPane(null, "Event Completed", null, resultPanel,
          List.of("OK"), 0);
      resultPanel.initZoom(optionPane);
      optionPane.setVisible(true);
      optionPane.dispose();
    }

    eventNode.setCompleted(true);
    currentRun.nextNode();
    RogueStats.fireOnSideNodeCompleted(currentRun, RogueMetaProgress.getInstance());
    RogueIO.saveRun(currentRun);
    update();
  }

  private void handleChestNode(NodeChest chestNode) {
    if (currentRun == null) return;

    // Get or assign random loot
    ChestLoot loot = chestNode.getLoot();
    if (loot == null) {
      ChestLoot[] allLoots = ChestLoot.values();
      loot = allLoots[new Random().nextInt(allLoots.length)];
      chestNode.setLoot(loot);
    }

    // DEV: allow picking which loot to test
    if (ForgePreferences.DEV_MODE) {
      ChestLoot picked = (ChestLoot) javax.swing.JOptionPane.showInputDialog(
          null, "Override chest loot:", "[DEV] Pick Loot",
          javax.swing.JOptionPane.PLAIN_MESSAGE, null,
          ChestLoot.values(), loot);
      if (picked != null) loot = picked;
    }

    // Show chest dialog (same structure as EventDialog)
    new ChestDialog(loot).show();

    // Apply effect after player acknowledges
    NodeResultContext ctx = new NodeResultContext();
    if (loot.getEffectType() == RogueEffect.EffectType.ONESHOT) {
      loot.applyEffect(currentRun, ctx);
      boolean mythicOnly = ctx.trigger == NodeResultContext.ActionTriggerType.MYTHIC_CARD_REWARD;
      if (ctx.trigger == NodeResultContext.ActionTriggerType.CARD_REWARD
          || ctx.trigger == NodeResultContext.ActionTriggerType.MYTHIC_CARD_REWARD) {
        CardRewardHelper.runReward(currentRun,
            (title, cards, max, reroll) -> new CardRewardDialog(title, cards, max, reroll).show(),
            mythicOnly);
      }
    } else {
      currentRun.addChestBoon(loot);
    }

    chestNode.setCompleted(true);
    currentRun.nextNode();
    RogueStats.fireOnSideNodeCompleted(currentRun, RogueMetaProgress.getInstance());
    RogueIO.saveRun(currentRun);
    update();
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

  private void devNextNode() {
    if (currentRun == null) return;
    RoguePathNode node = currentRun.getCurrentNode();
    if (node == null) return;
    // Last node — treat as win
    if (currentRun.getCurrentNodeIndex() >= currentRun.getPath().getNodeCount() - 1) {
      devWinRun();
      return;
    }
    node.setCompleted(true);
    currentRun.nextNode();
    RogueStats.fireOnSideNodeCompleted(currentRun, RogueMetaProgress.getInstance());
    RogueIO.saveRun(currentRun);
    update();
  }

  private void devWinRun() {
    if (currentRun == null) return;
    String commanderName = currentRun.getSelectedRogueDeck().getCommanderCardName();
    currentRun.setRunWon(true);
    RogueMetaProgress progress = RogueMetaProgress.getInstance();
    progress.addEchoes(20);
    progress.addRunHistoryEntry(RogueRunHistoryEntry.fromRun(currentRun, "VICTORY", "[DEV]"));
    RogueStats.fireOnRunCompleted(currentRun, progress, true);
    RogueCommanderAchievements.instance.recordRunWon(commanderName);
    int descLevel = currentRun.getDescensionLevel();
    if (descLevel > 0) {
      progress.recordDescensionWin(commanderName, descLevel);
    }
    RogueCommanderAchievements.instance.evaluateRunAchievements(currentRun);
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
}
