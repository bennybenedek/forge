package forge.screens.home.rogue;

import forge.Singletons;
import forge.gamemodes.rogue.*;
import forge.gamemodes.rogue.effect.*;
import forge.gamemodes.rogue.path.*;
import forge.gui.framework.EDocID;
import forge.gui.framework.FScreen;
import forge.gui.framework.ICDoc;
import forge.localinstance.achievements.RogueCommanderAchievements;
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
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;

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
  private final NodeBazaarHelper nodeBazaarHelper = new NodeBazaarHelper(this);
  private final NodeChestHelper nodeChestHelper = new NodeChestHelper(this);
  private final NodePlaneboundHelper nodePlaneboundHelper = new NodePlaneboundHelper();
  private final NodeSanctumHelper nodeSanctumHelper = new NodeSanctumHelper(this);
  private final NodeEffectResultHelper nodeEffectResultHelper =
      new NodeEffectResultHelper(this, nodeBazaarHelper, nodeChestHelper, nodeSanctumHelper, nodePlaneboundHelper);
  private final NodeEventHelper nodeEventHelper = new NodeEventHelper(this, nodeEffectResultHelper);

  CSubmenuRogueMap() {
    nodeChestHelper.setEffectResultHelper(nodeEffectResultHelper);
  }

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

    currentRun.getRunTimer().start();
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

    PathUpdateContext pathCtx = new PathUpdateContext();
    RogueEffectComposite.INSTANCE.onPathUpdate(pathCtx, currentRun);

    // Only allow selecting reachable nodes in the current row
    List<Integer> visibleInCurrentRow = currentRun.getPath()
        .getVisibleNodesInCurrentRow(currentNode.getRowIndex(), pathCtx);
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

  void enterNode() {
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
      nodePlaneboundHelper.handlePlaneboundNode(nodePlanebound, currentRun);
    } else if (node instanceof NodeSanctum nodeSanctum) {
      nodeSanctumHelper.handleSanctumNode(nodeSanctum, currentRun);
    } else if (node instanceof NodeBazaar nodeBazaar) {
      nodeBazaarHelper.handleBazaarNode(nodeBazaar, currentRun);
    } else if (node instanceof NodeEvent nodeEvent) {
      nodeEventHelper.handleEventNode(nodeEvent, currentRun);
    } else if (node instanceof NodeChest nodeChest) {
      nodeChestHelper.handleChestNode(nodeChest, currentRun);
    }
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

  void showNodeResultDialog(String title, String message,
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
    currentRun.getRunTimer().stop();
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
