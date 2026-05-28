package forge.screens.home.rogue;

import forge.LobbyPlayer;
import forge.Singletons;
import forge.deck.CardPool;
import forge.deck.Deck;
import forge.deck.DeckSection;
import forge.deck.io.DeckSerializer;
import forge.game.GameType;
import forge.game.player.RegisteredPlayer;
import forge.gamemodes.match.HostedMatch;
import forge.gamemodes.rogue.*;
import forge.gamemodes.rogue.RogueRun.CarryCard;
import forge.gamemodes.rogue.RogueRun.CarryCardType;
import forge.gamemodes.rogue.effect.*;
import forge.gamemodes.rogue.npc.BazaarContext;
import forge.gamemodes.rogue.npc.EventContext;
import forge.gamemodes.rogue.npc.NPCContext;
import forge.gamemodes.rogue.npc.NPCEncounterComposite;
import forge.gamemodes.rogue.path.*;
import forge.gui.GuiBase;
import forge.gui.SOverlayUtils;
import forge.gui.framework.EDocID;
import forge.gui.framework.FScreen;
import forge.gui.framework.ICDoc;
import forge.card.CardRulesPredicates;
import forge.item.PaperCard;
import forge.item.PaperCardPredicates;
import forge.localinstance.achievements.RogueCommanderAchievements;
import forge.localinstance.properties.ForgeConstants;
import forge.localinstance.properties.ForgePreferences;
import forge.player.GamePlayerUtil;
import forge.screens.deckeditor.CDeckEditorUI;
import forge.screens.deckeditor.controllers.CEditorRogue;
import forge.screens.home.CHomeUI;
import forge.toolbox.FOptionPane;
import forge.toolbox.FScrollPane;
import forge.toolbox.FSkin;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import forge.util.Aggregates;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;

/**
 * Controls the "rogue map" submenu in the home UI.
 *
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 */
public enum CSubmenuRogueMap implements ICDoc {
  SINGLETON_INSTANCE;

  private static final String SANCTUM_COOK_SOURCE_ID = "sanctum_cook";

  private final ActionListener actEnterNode = arg0 -> enterNode();
  private final ActionListener actEditDeck = arg0 -> editDeck();
  private final VSubmenuRogueMap view = VSubmenuRogueMap.SINGLETON_INSTANCE;
  private final NodeEventHelper nodeEventHelper = new NodeEventHelper(this);

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

    // Check node types of current row and show battle tutorials if not seen yet
    int currentRow = currentRun.getCurrentNode().getRowIndex();
    for (RoguePathNode node : currentRun.getPath().getNodesInRow(currentRow)) {
      if (node instanceof NodePlanebound planebound
          && planebound.getPlaneboundType() == RoguePlaneboundType.ELITE) {
        RogueTutorialHelper.showIfNotSeen(RogueTutorial.ELITE_ENCOUNTER);
      } else if (node instanceof NodePlanebound) {
        RogueTutorialHelper.showIfNotSeen(RogueTutorial.PRE_BATTLE);
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

  void updateView() {
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
      nodeEventHelper.handleEventNode(nodeEvent, currentRun);
    } else if (node instanceof NodeChest nodeChest) {
      handleChestNode(nodeChest);
    }
  }

  void handlePlaneboundNode(NodePlanebound node) {
    // Prevent starting a new match if one is already in progress this session
    if (currentRun.getHostedMatch() != null) {
      return;
    }

    // Roll and apply wrathful/cursed effects, show combined dialog
    handlePlaneboundBoons(node);

    // Show loading overlay
    SwingUtilities.invokeLater(() -> {
      SOverlayUtils.startGameOverlay();
      SOverlayUtils.showOverlay();
    });

    // DEV: allow picking which planebound to test
    if (ForgePreferences.DEV_MODE) {
      RoguePlanebound picked = (RoguePlanebound) JOptionPane.showInputDialog(
          null, "Override planebound:", "[DEV] Pick Planebound",
          JOptionPane.PLAIN_MESSAGE, null,
          RogueConfig.loadPlanebounds().toArray(), node.getRoguePlanebound());
      if (picked != null) node.setRoguePlanebound(picked);
    }

    try {
      // Get all plane cards from the centralized cache
      CardPool allPlanes = RogueConfig.getAllPlanes();

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
          2, appliedVariants, currentRun.getCurrentDeck(),
          null, false, sharedPlaneDeck, null
      );
      human.setStartingLife(currentRun.getCurrentLife());

      LobbyPlayer lobbyPlayer = GamePlayerUtil.getGuiPlayer();
      lobbyPlayer.setName(currentRun.getCurrentCommanderName());
      lobbyPlayer.setAvatarIndex(currentRun.getSelectedRogueDeck().getAvatarIndex());
      lobbyPlayer.setSleeveIndex(currentRun.getSelectedRogueDeck().getSleeveIndex());
      human.setPlayer(lobbyPlayer);

      // Add carry cards (items/fellows/scrolls) to command zone with their enablers
      if (!currentRun.getCarryCards().isEmpty()) {
        RogueTutorialHelper.showIfNotSeen(RogueTutorial.CARRY_CARDS);
        RogueEffect.addCardToCommandZone("Rogue - Carry Card Enabler", human);
      }
      for (CarryCard card : currentRun.getCarryCards()) {
        RogueEffect.addCardToCommandZone(card.toPaperCard(), human);
      }

      // Load Planebound deck
      Deck planeboundDeck = loadPlaneboundDeck(node.getRoguePlanebound().deckPath());

      // Create AI Planebound opponent
      RegisteredPlayer ai = RegisteredPlayer.forVariants(
          2, appliedVariants, planeboundDeck,
          null, false, sharedPlaneDeck, null
      );
      ai.setPlayer(GamePlayerUtil.createAiPlayer(
          node.getRoguePlanebound().planeboundName(),
          node.getRoguePlanebound().avatarIndex(),
          0));

      int planeboundRowCount = currentRun.getPath().countPlaneboundRowsUpTo(node.getRowIndex());
      ai.setStartingLife(node.getPlaneboundLife(planeboundRowCount));

      // Apply all match start effects AFTER AI creation (cursed effects need opponent)
      RogueEffectComposite.INSTANCE.onMatchStart(human, ai, currentRun);

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

  private void handlePlaneboundBoons(NodePlanebound node) {
    int wrathfulCount = node.getWrathfulCount();
    int cursedCount = node.getCursedCount();
    if (wrathfulCount == 0 && cursedCount == 0) return;

    ImageIcon flameIcon = NodePlaneboundPanel.createFlameIcon(14, 18);
    ImageIcon pentagramIcon = NodePlaneboundPanel.createPentagramIcon(14, 18);
    FSkin.SkinnedPanel effectsPanel = new FSkin.SkinnedPanel(
        new MigLayout("insets 5, gap 0, wrap", "[grow]"));
    effectsPanel.setOpaque(false);

    Set<Wrathful> usedWrathful = new HashSet<>();
    for (int i = 0; i < wrathfulCount; i++) {
      Wrathful w = Wrathful.getRandomExcluding(usedWrathful);
      usedWrathful.add(w);
      currentRun.addWrathful(w);
      JLabel lbl = new JLabel(w.getDisplayName() + " - " + w.getDescription(), flameIcon, SwingConstants.LEFT);
      lbl.setFont(FSkin.getRelativeFont(12).getBaseFont());
      lbl.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT).getColor());
      lbl.setIconTextGap(5);
      lbl.setOpaque(false);
      effectsPanel.add(lbl, "growx, h 24px!, wrap");
    }

    Set<Cursed> usedCursed = new HashSet<>();
    for (int i = 0; i < cursedCount; i++) {
      Cursed c = Cursed.getRandomExcluding(usedCursed);
      usedCursed.add(c);
      currentRun.addCursed(c);
      JLabel lbl = new JLabel(c.getDisplayName() + " - " + c.getDescription(), pentagramIcon, SwingConstants.LEFT);
      lbl.setFont(FSkin.getRelativeFont(12).getBaseFont());
      lbl.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT).getColor());
      lbl.setIconTextGap(5);
      lbl.setOpaque(false);
      effectsPanel.add(lbl, "growx, h 24px!, wrap");
    }

    boolean hasWrathful = wrathfulCount > 0;
    boolean hasCursed = cursedCount > 0;
    String title = hasWrathful && hasCursed ? "Wrathful & Cursed Planebound"
        : hasCursed ? "Cursed Planebound" : "Wrathful Planebound";
    String message = hasWrathful && hasCursed ? "This Planebound is Wrathful and Cursed!"
        : hasCursed ? "This Planebound is Cursed!" : "This Planebound is Wrathful!";
    FOptionPane.showOptionDialog(message, title, null, effectsPanel, List.of("OK"), 0);
  }

  void handleSanctumNode(NodeSanctum sanctumNode) {
    if (currentRun == null) {
      return;
    }

    RogueTutorialHelper.showIfNotSeen(RogueTutorial.SANCTUM);

    int baseHealAmount = sanctumNode.getHealAmount();
    int missingLife = Math.max(0, currentRun.getMaxLife() - currentRun.getCurrentLife());
    int effectiveHealAmount = Math.min(baseHealAmount, missingLife);
    boolean hasWounds = !currentRun.getActiveWounds().isEmpty();
    boolean restEnabled = effectiveHealAmount > 0 || hasWounds;
    String restDisabledReason = restEnabled
        ? null
        : "You are already at maximum life and have no wounds to cure.";

    // Show Sanctum dialog
    SanctumDialog dialog = new SanctumDialog(
        effectiveHealAmount, restEnabled, restDisabledReason);
    SanctumDialog.SanctumChoice choice = dialog.show();

    // Handle player's choice
    switch (choice) {
      case HEAL:
        // Gain life up to max and cure all wounds
        currentRun.gainLifeUpToMax(baseHealAmount);
        currentRun.clearWounds();
        break;

      case COOK:
        PaperCard craftedFood = craftSanctumFood();
        if (craftedFood == null) {
          FOptionPane.showMessageDialog(
              "No Food items matching your commander's color identity were available to craft.",
              "Sanctum");
          break;
        }
        currentRun.addCarryCard(craftedFood, CarryCardType.ITEM, SANCTUM_COOK_SOURCE_ID);
        showNodeResultDialog(
            "Sanctum",
            "You cooked:",
            List.of(new NodeResultPanel.CardSection(null, List.of(craftedFood))),
            900,
            700,
            NodeResultPanel.MessageAlignment.CENTER);
        break;

      case REFLECT:
        currentRun.addRemovalCredits(3);
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

    // Save run and update view
    RogueIO.saveRun(currentRun);
    update();
  }

  private PaperCard craftSanctumFood() {
    Predicate<PaperCard> foodFilter = PaperCardPredicates.fromRules(
        CardRulesPredicates.IS_ARTIFACT.and(CardRulesPredicates.subType("Food")));
    List<PaperCard> foods = currentRun.getAllCardsForActiveCommander(foodFilter);
    return foods.isEmpty() ? null : Aggregates.random(foods);
  }

  private void handleBazaarNode(NodeBazaar bazaarNode) {
    if (currentRun == null) {
      return;
    }

    runBazaarShopping(null);

    // Mark node as completed and move to next
    bazaarNode.setCompleted(true);
    currentRun.nextNode();

    // Evaluate achievements after Bazaar (deck/gold may have changed)
    RogueCommanderAchievements.instance.evaluateRunAchievements(currentRun);

    // Track stats and check for unlocks
    RogueStats.fireOnSideNodeCompleted(currentRun, RogueMetaProgress.getInstance());

    // Save run and update view
    RogueIO.saveRun(currentRun);
    update();
  }

  /** Run Bazaar shopping UI and apply purchases. Null context = ordinary Bazaar setup. */
  List<PaperCard> runBazaarShopping(BazaarContext ctx) {
    boolean customBazaar = ctx != null;
    RogueDeck rogueDeck = currentRun.getSelectedRogueDeck();
    if (rogueDeck == null) {
      System.err.println("ERROR: Could not find rogue deck for current run.");
      return List.of();
    }

    BazaarContext bazaarCtx = customBazaar ? ctx : createOrdinaryBazaarContext();
    if (!customBazaar) {
      RogueTutorialHelper.showIfNotSeen(RogueTutorial.BAZAAR);
      NPCEncounterComposite.INSTANCE.onBeforeBazaar(bazaarCtx, RogueMetaProgress.getInstance());
    }

    CardSelectionContext selCtx = customBazaar ? null : createBazaarSelectionContext();
    int freeRerolls = customBazaar ? 0 : selCtx.freeRerolls;
    int rerollCount = 0;
    Set<PaperCard> selectedCards;
    do {
      List<PaperCard> inventory = customBazaar
          ? buildCustomBazaarInventory(bazaarCtx)
          : buildOrdinaryBazaarInventory(rogueDeck, selCtx);

      applyBazaarDiscounts(bazaarCtx, inventory);
      injectBazaarItems(bazaarCtx, inventory);

      if (inventory.isEmpty()) {
        System.err.println("ERROR: No cards available in Bazaar inventory.");
        return List.of();
      }

      String rerollLabel = customBazaar ? null
          : CardRewardHelper.buildRerollLabel(freeRerolls, rerollCount);
      boolean rerollEnabled = !customBazaar
          && CardRewardHelper.canAffordReroll(freeRerolls, rerollCount, currentRun.getCurrentGold());
      BazaarDialog dialog = new BazaarDialog(
          inventory,
          currentRun.getCurrentGold(),
          bazaarCtx.title,
          rerollLabel,
          bazaarCtx.priceOverrides.isEmpty() ? null : bazaarCtx.priceOverrides);
      dialog.setRerollEnabled(rerollEnabled);
      selectedCards = dialog.show();

      if (!customBazaar) {
        rogueDeck.discardRewardOptions(getRewardPoolCards(inventory, bazaarCtx.injectedCards));
      }

      if (!customBazaar && selectedCards == null) {
        if (rerollCount >= freeRerolls) {
          int cost = CardRewardHelper.getRerollCost(rerollCount - freeRerolls);
          currentRun.spendGold(cost);
        }
        rerollCount++;
        bazaarCtx.priceOverrides.clear();
      }
    } while (!customBazaar && selectedCards == null);

    List<PaperCard> purchasedCards = applyBazaarPurchases(rogueDeck, bazaarCtx, selectedCards, customBazaar);
    if (!customBazaar) {
      for (NPCContext npcContext : NPCEncounterComposite.INSTANCE.onAfterBazaarPurchase(
          bazaarCtx, RogueMetaProgress.getInstance())) {
        new NPCDialog(npcContext).show();
      }
    }
    return purchasedCards;
  }

  private BazaarContext createOrdinaryBazaarContext() {
    BazaarContext bazaarCtx = new BazaarContext();
    bazaarCtx.title = "Bazaar";
    return bazaarCtx;
  }

  private CardSelectionContext createBazaarSelectionContext() {
    CardSelectionContext selCtx = new CardSelectionContext();
    RogueEffectComposite.INSTANCE.onCardSelection(selCtx, currentRun);
    return selCtx;
  }

  private List<PaperCard> buildOrdinaryBazaarInventory(RogueDeck rogueDeck, CardSelectionContext selCtx) {
    int baseNonMythics = 8;
    int baseMythics = 2;
    int totalNonMythics = Math.max(0, baseNonMythics - selCtx.extraMythics);
    int totalMythics = baseMythics + selCtx.extraMythics;

    Predicate<PaperCard> notAlreadyOwned = currentRun.getNotAlreadyInDeckPredicate();
    List<PaperCard> nonMythicCards = rogueDeck.drawRewardOptions(totalNonMythics,
        CardRewardHelper.combineFilters(PaperCardPredicates.IS_MYTHIC_RARE.negate(), notAlreadyOwned));
    List<PaperCard> mythicCards = rogueDeck.drawRewardOptions(totalMythics,
        CardRewardHelper.combineFilters(PaperCardPredicates.IS_MYTHIC_RARE, notAlreadyOwned));

    List<PaperCard> inventory = new ArrayList<>();
    inventory.addAll(nonMythicCards);
    inventory.addAll(mythicCards);
    return inventory;
  }

  private List<PaperCard> buildCustomBazaarInventory(BazaarContext bazaarCtx) {
    List<PaperCard> inventory = new ArrayList<>(bazaarCtx.inventory);
    if (inventory.size() <= BazaarDialog.MAX_DISPLAY_CARDS) {
      return inventory;
    }

    Collections.shuffle(inventory);
    return new ArrayList<>(inventory.subList(0, BazaarDialog.MAX_DISPLAY_CARDS));
  }

  private void applyBazaarDiscounts(BazaarContext bazaarCtx, List<PaperCard> inventory) {
    if (bazaarCtx.discountCount <= 0 || inventory.isEmpty()) {
      return;
    }

    List<Integer> indices = new ArrayList<>();
    for (int i = 0; i < inventory.size(); i++) {
      indices.add(i);
    }
    java.util.Collections.shuffle(indices);
    for (int i = 0; i < Math.min(bazaarCtx.discountCount, inventory.size()); i++) {
      PaperCard card = inventory.get(indices.get(i));
      int basePrice = BazaarPricing.getCardPrice(card);
      int discounted = Math.max(0, basePrice - bazaarCtx.discountAmount);
      if (discounted == 0 && basePrice > 2) {
        discounted = 1;
      }
      bazaarCtx.priceOverrides.put(card.getName(), discounted);
    }
  }

  private void injectBazaarItems(BazaarContext bazaarCtx, List<PaperCard> inventory) {
    if (bazaarCtx.injectedCards.isEmpty()) {
      return;
    }

    for (PaperCard injected : bazaarCtx.injectedCards) {
      if (!inventory.isEmpty()) {
        inventory.remove(inventory.size() - 1);
      }
      inventory.add(injected);
    }
  }

  private List<PaperCard> applyBazaarPurchases(RogueDeck rogueDeck, BazaarContext bazaarCtx,
                                               Set<PaperCard> selectedCards, boolean customBazaar) {
    if (selectedCards == null || selectedCards.isEmpty()) {
      return List.of();
    }

    List<PaperCard> realCards = customBazaar
        ? new ArrayList<>(selectedCards)
        : getRewardPoolCards(selectedCards, bazaarCtx.injectedCards);
    bazaarCtx.purchasedCards.clear();
    bazaarCtx.purchasedCards.addAll(selectedCards);

    if (!realCards.isEmpty()) {
      currentRun.addCardsToDeck(realCards, true);
      if (!customBazaar) {
        rogueDeck.removeFromCardPools(realCards);
      }
    }

    int totalCost = BazaarPricing.calculateTotalCost(selectedCards,
        bazaarCtx.priceOverrides.isEmpty() ? null : bazaarCtx.priceOverrides);
    currentRun.spendGold(totalCost);
    return realCards;
  }

  private static List<PaperCard> getRewardPoolCards(Collection<PaperCard> cards,
      Set<PaperCard> injectedCards) {
    List<PaperCard> rewardPoolCards = new ArrayList<>();
    for (PaperCard card : cards) {
      if (!injectedCards.contains(card)) {
        rewardPoolCards.add(card);
      }
    }
    return rewardPoolCards;
  }

  void completeSideNode(RoguePathNode node) {
    node.setCompleted(true);
    currentRun.nextNode();
    RogueStats.fireOnSideNodeCompleted(currentRun, RogueMetaProgress.getInstance());
    RogueIO.saveRun(currentRun);
    update();
  }

  boolean checkSideNodeDefeat(String defeatedBy) {
    if (currentRun == null || currentRun.getCurrentLife() > 0) {
      return false;
    }

    DefeatContext defeatCtx = new DefeatContext();
    RogueEffectComposite.INSTANCE.onDefeat(defeatCtx, currentRun);
    if (defeatCtx.revived) {
      currentRun.setCurrentLife(defeatCtx.reviveLife);
      FOptionPane.showMessageDialog(
          "Last Spark activated! You survived with " + defeatCtx.reviveLife + " life!",
          "Last Spark!");
      return false;
    }

    RogueWinLoseController.finalizeRunDefeat(currentRun, defeatedBy);
    showSideNodeDefeatDialog(defeatedBy);
    currentRun = null;
    CHomeUI.SINGLETON_INSTANCE.itemClick(EDocID.HOME_ROGUESTART);
    return true;
  }

  private void showSideNodeDefeatDialog(String defeatedBy) {
    String message = defeatedBy == null || defeatedBy.isEmpty()
        ? "You were defeated! Your Run has ended."
        : "You were defeated by " + defeatedBy + "! Your Run has ended.";
    NodeResultPanel resultPanel = new NodeResultPanel(message, List.of());
    FOptionPane optionPane = new FOptionPane(null, "Defeat", null, resultPanel, List.of("OK"), 0);
    resultPanel.initZoom(optionPane);
    optionPane.setVisible(true);
    optionPane.dispose();
  }

  void showNodeResultDialog(String title, String message,
                                    List<NodeResultPanel.CardSection> sections) {
    boolean hasCardSections = sections.stream().anyMatch(
        section -> section.cards() != null && !section.cards().isEmpty());
    int minHeight = hasCardSections ? 700 : 0;
    showNodeResultDialog(title, message, sections, 650, minHeight,
        NodeResultPanel.MessageAlignment.LEFT);
  }

  private void showNodeResultDialog(String title, String message,
                                    List<NodeResultPanel.CardSection> sections,
                                    int minWidth, int minHeight,
                                    NodeResultPanel.MessageAlignment messageAlignment) {
    NodeResultPanel resultPanel = new NodeResultPanel(
        message, sections, minWidth, minHeight, messageAlignment);
    FScrollPane scrollPane = new FScrollPane(resultPanel, false,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    Dimension resultSize = resultPanel.getPreferredSize();
    GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
        .getDefaultScreenDevice().getDefaultConfiguration();
    Rectangle screenBounds = gc.getBounds();
    Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
    int maxDialogWidth = (int) ((screenBounds.width - screenInsets.left - screenInsets.right) * 0.9) - 50;
    int maxDialogHeight = (int) ((screenBounds.height - screenInsets.top - screenInsets.bottom) * 0.9) - 80;
    Dimension dialogSize = new Dimension(
        Math.min(resultSize.width + 30, maxDialogWidth),
        Math.min(resultSize.height, maxDialogHeight));

    FSkin.SkinnedPanel wrapper = new FSkin.SkinnedPanel(new BorderLayout());
    wrapper.setOpaque(false);
    wrapper.add(scrollPane, BorderLayout.CENTER);
    wrapper.setPreferredSize(dialogSize);
    wrapper.setMinimumSize(dialogSize);

    FOptionPane optionPane = new FOptionPane(null, title, null, wrapper, List.of("OK"), 0);
    resultPanel.initZoom(optionPane);
    optionPane.setVisible(true);
    optionPane.dispose();
  }

  void handleChestNode(NodeChest chestNode) {
    if (currentRun == null) return;

    // Get or assign random loot
    ChestEffect chestEffect = chestNode.getChestEffect();
    if (chestEffect == null) {
      ChestEffect[] allChestEffects = ChestEffect.values();
      chestEffect = allChestEffects[new Random().nextInt(allChestEffects.length)];
      chestNode.setChestEffect(chestEffect);
    }

    // DEV: allow picking which loot to test
    if (ForgePreferences.DEV_MODE) {
      ChestEffect picked = (ChestEffect) JOptionPane.showInputDialog(
          null, "Override chest loot:", "[DEV] Pick Loot",
          JOptionPane.PLAIN_MESSAGE, null,
          ChestEffect.values(), chestEffect);
      if (picked != null) chestEffect = picked;
    }

    RogueTutorialHelper.showIfNotSeen(RogueTutorial.CHEST);

    // Show chest dialog (same structure as EventDialog)
    new ChestDialog(chestEffect).show();

    // Apply effect after player acknowledges
    NodeResultContext ctx = new NodeResultContext();
    if (chestEffect.getEffectType() == RogueEffect.EffectType.ONESHOT) {
      chestEffect.applyEffect(currentRun, ctx);
      boolean mythicOnly = ctx.trigger == NodeResultContext.ActionTriggerType.MYTHIC_CARD_REWARD;
      if (ctx.trigger == NodeResultContext.ActionTriggerType.CARD_REWARD
          || ctx.trigger == NodeResultContext.ActionTriggerType.MYTHIC_CARD_REWARD) {
        CardRewardHelper.runReward(currentRun,
            (title, cards, max, label, enabled, gold) -> new CardRewardDialog(title, cards, max, label, enabled, gold).show(),
            mythicOnly);
      }
    } else {
      currentRun.addChestEffect(chestEffect);
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

    node.setCompleted(true);
    var progress = RogueMetaProgress.getInstance();
    if (node instanceof NodePlanebound nodePlanebound) {
      RogueStats.fireOnMatchCompleted(currentRun, progress, true);
      currentRun.addGold(nodePlanebound.getGoldReward());
      progress.addEchoes(nodePlanebound.getEchoReward());
    } else {
      RogueStats.fireOnSideNodeCompleted(currentRun, progress);
    }
    // Last node — treat as win
    if (currentRun.getCurrentNodeIndex() >= currentRun.getPath().getNodeCount() - 1) {
      devWinRun();
      return;
    }

    currentRun.nextNode();
    RogueIO.saveRun(currentRun);
    update();
  }

  private void devWinRun() {
    if (currentRun == null) return;
    String commanderName = currentRun.getCurrentCommanderName();
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

