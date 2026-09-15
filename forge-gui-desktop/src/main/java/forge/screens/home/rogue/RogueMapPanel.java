package forge.screens.home.rogue;

import forge.gamemodes.rogue.RogueRun;
import forge.toolbox.FLabel;
import forge.toolbox.FScrollPane;
import forge.toolbox.FSkin;
import forge.toolbox.FSkin.SkinnedPanel;
import java.awt.Component;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;

/**
 * Shared title and path display used by the Rogue Commander map and its read-only preview.
 */
final class RogueMapPanel extends SkinnedPanel {

  private final PathVisualizerPanel pathVisualizer = new PathVisualizerPanel();

  RogueMapPanel(Component infoComponent) {
    super(new MigLayout("insets 0, gap 0, wrap", "[grow]", ""));
    setOpaque(false);

    FLabel lblTitle = new FLabel.Builder()
        .text("Rogue Commander - Map")
        .fontAlign(SwingConstants.CENTER)
        .opaque(true)
        .fontSize(16)
        .build();
    lblTitle.setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));
    add(lblTitle, "w 98%!, h 30px!, gap 1% 0 15px 15px");

    if (infoComponent != null) {
      add(infoComponent, "w 98%!, h pref!, gap 1% 0 10px 10px");
    }

    FScrollPane scrollPathDisplay = new FScrollPane(pathVisualizer, true);
    scrollPathDisplay.setOpaque(false);
    add(scrollPathDisplay, "w 96%!, gap 2% 2% 0 0, pushy, growy");
  }

  void updatePath(RogueRun run) {
    pathVisualizer.updatePath(run);
    Integer currentNodeIndex = run != null && run.getCurrentNodeIndex() >= 0
        ? run.getCurrentNodeIndex() : null;
    pathVisualizer.setSelectedNode(currentNodeIndex);
  }

  void clearPath() {
    pathVisualizer.clearPath();
    pathVisualizer.setSelectedNode(null);
  }

  PathVisualizerPanel getPathVisualizer() {
    return pathVisualizer;
  }
}
