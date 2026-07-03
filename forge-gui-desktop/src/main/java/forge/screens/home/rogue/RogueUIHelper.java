package forge.screens.home.rogue;

import forge.gamemodes.rogue.RogueRun;
import forge.gamemodes.rogue.RogueRunHistoryEntry;
import forge.gamemodes.rogue.effect.RogueEffect;
import forge.toolbox.FLabel;
import java.util.List;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/**
 * Shared helper for Rogue UI widgets.
 */
public final class RogueUIHelper {

  private RogueUIHelper() {
  }

  public static JPanel createEffectPanel() {
    JPanel panel = new JPanel(new MigLayout("insets 0, gap 8 1"));
    panel.setOpaque(false);
    return panel;
  }

  public static void populateEffectPanel(JPanel panel, List<RogueEffect> effects, RogueRun run) {
    panel.removeAll();
    for (int i = 0; i < effects.size(); i++) {
      RogueEffect effect = effects.get(i);
      addEffectLabel(panel, effect.getUIDisplayText(), effect.getActiveDescription(run), i);
    }
    panel.revalidate();
    panel.repaint();
  }

  public static void populateEffectPanelFromSnapshots(JPanel panel,
      List<RogueRunHistoryEntry.EffectSnapshot> effects) {
    panel.removeAll();
    for (int i = 0; i < effects.size(); i++) {
      RogueRunHistoryEntry.EffectSnapshot effect = effects.get(i);
      addEffectLabel(panel, effect.getDisplayText(), effect.getTooltipText(), i);
    }
    panel.revalidate();
    panel.repaint();
  }

  private static void addEffectLabel(JPanel panel, String displayText, String tooltipText, int index) {
    int col = index / 3;
    int row = index % 3;
    FLabel effectLabel = new FLabel.Builder()
        .text(displayText)
        .fontSize(11)
        .build();
    effectLabel.setToolTipText(tooltipText);
    panel.add(effectLabel, "cell " + col + " " + row);
  }
}
