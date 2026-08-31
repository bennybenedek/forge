package forge.screens.home.rogue;

import forge.gamemodes.rogue.RogueRun;
import forge.gamemodes.rogue.RogueRunHistoryEntry;
import forge.gamemodes.rogue.effect.RogueEffect;
import forge.toolbox.FLabel;
import forge.toolbox.FTextArea;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Timer;
import net.miginfocom.swing.MigLayout;

/**
 * Shared helper for Rogue UI widgets.
 */
public final class RogueUIHelper {

  private static final int TYPEWRITER_DELAY_MS = 15;
  private static final int TYPEWRITER_CHARS_PER_TICK = 2;

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

  public static TypewriterText prepareTypewriterText(FTextArea textArea, JComponent skipComponent,
      String fullText, int width) {
    TypewriterText typewriterText = new TypewriterText(textArea, fullText);
    textArea.setText(typewriterText.fullText);
    textArea.setSize(width, Short.MAX_VALUE);
    Dimension textSize = new Dimension(width, textArea.getPreferredSize().height);
    textArea.setPreferredSize(textSize);
    textArea.setMinimumSize(textSize);
    textArea.setText("");

    MouseAdapter skipRevealListener = new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        typewriterText.finish();
      }
    };
    textArea.addMouseListener(skipRevealListener);
    if (skipComponent != null && skipComponent != textArea) {
      skipComponent.addMouseListener(skipRevealListener);
    }

    return typewriterText;
  }

  public static final class TypewriterText {
    private final FTextArea textArea;
    private final String fullText;
    private Timer timer;
    private int revealedCharacters;
    private boolean fullyRevealed;

    private TypewriterText(FTextArea textArea, String fullText) {
      this.textArea = textArea;
      this.fullText = fullText == null ? "" : fullText;
    }

    public void start() {
      stop();
      revealedCharacters = 0;
      fullyRevealed = fullText.isEmpty();
      textArea.setText(fullyRevealed ? fullText : "");
      if (fullyRevealed) {
        return;
      }

      timer = new Timer(TYPEWRITER_DELAY_MS, e -> revealNextTextChunk());
      timer.start();
    }

    public void finish() {
      if (fullyRevealed) {
        return;
      }
      fullyRevealed = true;
      stop();
      textArea.setText(fullText);
      textArea.revalidate();
      textArea.repaint();
    }

    public void stop() {
      if (timer != null) {
        timer.stop();
        timer = null;
      }
    }

    private void revealNextTextChunk() {
      if (revealedCharacters >= fullText.length()) {
        finish();
        return;
      }

      revealedCharacters = Math.min(
          fullText.length(), revealedCharacters + TYPEWRITER_CHARS_PER_TICK);
      textArea.setText(fullText.substring(0, revealedCharacters));
      if (revealedCharacters >= fullText.length()) {
        finish();
      }
    }
  }
}
