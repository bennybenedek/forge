package forge.screens.home.rogue;

import forge.gamemodes.rogue.RogueRun;
import forge.gamemodes.rogue.path.RoguePath;
import forge.gamemodes.rogue.path.RoguePathNode;
import forge.toolbox.FSkin;
import forge.toolbox.FSkin.SkinnedPanel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;

/**
 * Displays the entire path for a Rogue Commander run. Shows all nodes in a vertical linear
 * progression with connection lines.
 */
public class PathVisualizerPanel extends SkinnedPanel {

  private static final int NODE_SPACING = 40;  // Vertical space between rows
  private static final int HORIZONTAL_SPACING = 30;  // Horizontal space between nodes in same row
  private static final int PATH_LINE_WIDTH = 4;

  private final List<NodePanel> nodePanels;
  private int currentNodeIndex;
  private RogueRun currentRun;  // Store run for reachability checks
  private NodePanel.NodeClickHandler clickHandler;
  private Integer selectedPanelIndex;

  public PathVisualizerPanel() {
    setLayout(null);
    setOpaque(false);
    this.nodePanels = new ArrayList<>();
    this.currentNodeIndex = -1;
    this.selectedPanelIndex = null;
  }

  /**
   * Update the display with a new run's path.
   *
   * @param run The current run data
   */
  public void updatePath(RogueRun run) {
    if (run == null) {
      clearPath();
      return;
    }

    RoguePath path = run.getPath();
    if (path == null || path.getNodes().isEmpty()) {
      clearPath();
      return;
    }

    // Store run for later use
    this.currentRun = run;

    // Clear existing panels
    removeAll();
    nodePanels.clear();

    // Find current node index and current row
    RoguePathNode currentNode = run.getCurrentNode();
    currentNodeIndex = path.getNodes().indexOf(currentNode);
    int currentRow = currentNode != null ? currentNode.getRowIndex() : 0;

    // Get visible nodes in current row (reachable from last completed in previous row)
    List<Integer> visibleInCurrentRow = path.getVisibleNodesInCurrentRow(currentRow);

    // Create panels for each node
    List<RoguePathNode> nodes = path.getNodes();
    List<NodePlaneboundPanel> toReveal = new ArrayList<>();
    for (int i = 0; i < nodes.size(); i++) {
      RoguePathNode node = nodes.get(i);

      // Face-down logic: only show nodes that were reachable in their row
      boolean isFaceDown;
      if (node.getRowIndex() < currentRow) {
        // Past row - only show nodes that were visible when that row was current
        List<Integer> visibleInThatRow = path.getVisibleNodesInCurrentRow(node.getRowIndex());
        isFaceDown = !visibleInThatRow.contains(i);
      } else if (node.getRowIndex() == currentRow) {
        // Current row - show visible nodes
        isFaceDown = !visibleInCurrentRow.contains(i);
      } else {
        // Future rows - all face-down
        isFaceDown = true;
      }

      // Animate reveal for visible planes in current row
      boolean animateReveal = !isFaceDown && node.getRowIndex() == currentRow
          && visibleInCurrentRow.contains(i);

      // Calculate planebound row count for life display
      int planeboundRowCount = path.countPlaneboundRowsUpTo(node.getRowIndex());

      NodePanel nodePanel = NodePanelFactory.createPanel(node, isFaceDown, planeboundRowCount,
          animateReveal);
      nodePanel.setClickHandler(this::handleNodeClick);
      nodePanels.add(nodePanel);
      add(nodePanel);

      if (animateReveal && nodePanel instanceof NodePlaneboundPanel) {
        toReveal.add((NodePlaneboundPanel) nodePanel);
      }
    }

    calculatePreferredSize();
    revalidate();
    repaint();

    // Trigger staggered flip animations
    if (!toReveal.isEmpty()) {
      final int[] revealIndex = {0};
      Timer revealTimer = new Timer(150, e -> {
        if (revealIndex[0] < toReveal.size()) {
          toReveal.get(revealIndex[0]).flipToReveal();
          revealIndex[0]++;
        } else {
          ((Timer) e.getSource()).stop();
        }
      });
      revealTimer.setInitialDelay(300); // Brief pause before first flip
      revealTimer.start();
    }
  }

  /**
   * Handle click on a node panel.
   */
  private void handleNodeClick(NodePanel panel) {
    if (clickHandler != null) {
      int index = nodePanels.indexOf(panel);
      if (index >= 0) {
        clickHandler.onNodeClicked(panel);
      }
    }
  }

  /**
   * Set the click handler for node panels.
   */
  public void setNodeClickHandler(NodePanel.NodeClickHandler handler) {
    this.clickHandler = handler;
  }

  /**
   * Set the selected node by index. Clears previous selection and highlights the new selected
   * node.
   */
  public void setSelectedNode(Integer nodeIndex) {
    // Clear previous selection
    if (selectedPanelIndex != null && selectedPanelIndex < nodePanels.size()) {
      nodePanels.get(selectedPanelIndex).setSelected(false);
    }

    // Set new selection
    selectedPanelIndex = nodeIndex;
    if (selectedPanelIndex != null && selectedPanelIndex < nodePanels.size()) {
      nodePanels.get(selectedPanelIndex).setSelected(true);
    }
  }

  /**
   * Clear all nodes from the display.
   */
  public void clearPath() {
    removeAll();
    nodePanels.clear();
    currentNodeIndex = -1;
    revalidate();
    repaint();
  }

  /**
   * Calculate the preferred size based on number of nodes.
   */
  private void calculatePreferredSize() {
    if (nodePanels.isEmpty() || currentRun == null || currentRun.getPath() == null) {
      setPreferredSize(new Dimension(0, 0));
      return;
    }

    RoguePath path = currentRun.getPath();
    java.util.Map<Integer, List<NodePanel>> rowMap = groupNodesByRow();
    int maxRow = path.getMaxRow();

    int maxRowWidth = 0;
    int totalHeight = 20; // Top padding

    for (int row = 0; row <= maxRow; row++) {
      List<NodePanel> rowPanels = rowMap.get(row);
      if (rowPanels == null || rowPanels.isEmpty()) {
        continue;
      }

      // Calculate row width (all panels + spacing)
      int rowWidth = 0;
      int rowHeight = 0;
      for (NodePanel panel : rowPanels) {
        rowWidth += panel.getPreferredSize().width;
        rowHeight = Math.max(rowHeight, panel.getPreferredSize().height);
      }
      rowWidth += (rowPanels.size() - 1) * HORIZONTAL_SPACING;

      maxRowWidth = Math.max(maxRowWidth, rowWidth);
      totalHeight += rowHeight + NODE_SPACING;
    }

    // Add padding
    totalHeight += 20;

    setPreferredSize(new Dimension(maxRowWidth + 40, totalHeight));
  }

  @Override
  public void doLayout() {
    if (nodePanels.isEmpty() || currentRun == null) {
      return;
    }

    RoguePath path = currentRun.getPath();
    if (path == null) {
      return;
    }

    // Group nodes by row
    java.util.Map<Integer, List<NodePanel>> rowMap = groupNodesByRow();
    int maxRow = path.getMaxRow();

    int y = 20;

    for (int row = 0; row <= maxRow; row++) {
      List<NodePanel> rowPanels = rowMap.get(row);
      if (rowPanels == null || rowPanels.isEmpty()) {
        continue;
      }

      // Sort all nodes by column index
      rowPanels.sort((p1, p2) -> {
        int idx1 = nodePanels.indexOf(p1);
        int idx2 = nodePanels.indexOf(p2);
        RoguePathNode n1 = path.getNodes().get(idx1);
        RoguePathNode n2 = path.getNodes().get(idx2);
        return Integer.compare(n1.getColumnIndex(), n2.getColumnIndex());
      });

      // Calculate row height (max height of all panels in row)
      int rowHeight = 0;
      for (NodePanel panel : rowPanels) {
        rowHeight = Math.max(rowHeight, panel.getPreferredSize().height);
      }

      // Layout all nodes horizontally centered
      int totalWidth = 0;
      for (NodePanel panel : rowPanels) {
        totalWidth += panel.getPreferredSize().width;
      }
      totalWidth += (rowPanels.size() - 1) * HORIZONTAL_SPACING;

      int startX = (getWidth() - totalWidth) / 2;

      for (NodePanel panel : rowPanels) {
        int panelWidth = panel.getPreferredSize().width;
        int panelHeight = panel.getPreferredSize().height;
        panel.setBounds(startX, y, panelWidth, panelHeight);
        startX += panelWidth + HORIZONTAL_SPACING;
      }

      y += rowHeight + NODE_SPACING;
    }
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    if (nodePanels.isEmpty() || currentRun == null) {
      return;
    }

    RoguePath path = currentRun.getPath();
    if (path == null) {
      return;
    }

    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // Draw connection lines to reachable nodes
    g2d.setColor(FSkin.getColor(FSkin.Colors.CLR_BORDERS).getColor());
    g2d.setStroke(new BasicStroke(PATH_LINE_WIDTH));

    List<RoguePathNode> nodes = path.getNodes();

    for (int i = 0; i < nodePanels.size(); i++) {
      RoguePathNode fromNode = nodes.get(i);
      List<Integer> reachableIndices = path.getReachableNodeIndices(i);

      if (reachableIndices.isEmpty()) {
        continue;
      }

      NodePanel fromPanel = nodePanels.get(i);

      for (Integer toIndex : reachableIndices) {
        if (toIndex >= nodePanels.size()) {
          continue;
        }

        NodePanel toPanel = nodePanels.get(toIndex);

        // Calculate line positions (center bottom of from to center top of to)
        int x1 = fromPanel.getX() + (fromPanel.getWidth() / 2);
        int y1 = fromPanel.getY() + fromPanel.getHeight();
        int x2 = toPanel.getX() + (toPanel.getWidth() / 2);
        int y2 = toPanel.getY();

        // Draw the connecting line
        // Use curved line for diagonal connections
        if (Math.abs(x2 - x1) > 10) {
          drawCurvedLine(g2d, x1, y1, x2, y2);
        } else {
          g2d.drawLine(x1, y1, x2, y2);
        }

        // Draw arrow at the end
        drawArrow(g2d, x1, y1, x2, y2);
      }
    }
  }

  /**
   * Draw a curved line (cubic Bezier curve) from (x1, y1) to (x2, y2).
   */
  private void drawCurvedLine(Graphics2D g2d, int x1, int y1, int x2, int y2) {
    // Control points for cubic Bezier curve
    // Place control points vertically between start and end
    int midY = (y1 + y2) / 2;
    int ctrl1X = x1;
    int ctrl1Y = midY;
    int ctrl2X = x2;
    int ctrl2Y = midY;

    java.awt.geom.CubicCurve2D curve = new java.awt.geom.CubicCurve2D.Float(
        x1, y1,
        ctrl1X, ctrl1Y,
        ctrl2X, ctrl2Y,
        x2, y2
    );

    g2d.draw(curve);
  }

  /**
   * Draw an arrow pointing from (x1, y1) to (x2, y2).
   */
  private void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
    int arrowSize = 10;

    // Calculate angle
    double angle = Math.atan2(y2 - y1, x2 - x1);

    // Arrow head points
    int x3 = (int) (x2 - arrowSize * Math.cos(angle - Math.PI / 6));
    int y3 = (int) (y2 - arrowSize * Math.sin(angle - Math.PI / 6));
    int x4 = (int) (x2 - arrowSize * Math.cos(angle + Math.PI / 6));
    int y4 = (int) (y2 - arrowSize * Math.sin(angle + Math.PI / 6));

    // Draw arrow head
    g2d.drawLine(x2, y2, x3, y3);
    g2d.drawLine(x2, y2, x4, y4);
  }

  /**
   * Group node panels by their row index.
   */
  private java.util.Map<Integer, List<NodePanel>> groupNodesByRow() {
    java.util.Map<Integer, List<NodePanel>> rowMap = new java.util.HashMap<>();

    if (currentRun == null || currentRun.getPath() == null) {
      return rowMap;
    }

    List<RoguePathNode> nodes = currentRun.getPath().getNodes();
    for (int i = 0; i < nodePanels.size() && i < nodes.size(); i++) {
      RoguePathNode node = nodes.get(i);
      int row = node.getRowIndex();

      rowMap.computeIfAbsent(row, k -> new ArrayList<>()).add(nodePanels.get(i));
    }

    return rowMap;
  }

  /**
   * Get all node panels.
   */
  public List<NodePanel> getNodePanels() {
    return nodePanels;
  }

  /**
   * Get the current node index.
   */
  public int getCurrentNodeIndex() {
    return currentNodeIndex;
  }
}
