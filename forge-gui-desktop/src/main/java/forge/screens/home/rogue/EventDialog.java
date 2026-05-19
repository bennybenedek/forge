package forge.screens.home.rogue;

import forge.gamemodes.rogue.PreviewReference;
import forge.gamemodes.rogue.RogueEvent;
import forge.gamemodes.rogue.RogueEvent.EventChoice;
import forge.gamemodes.rogue.RogueRun;
import forge.toolbox.FButton;
import forge.toolbox.FLabel;
import forge.toolbox.FOptionPane;
import forge.toolbox.FTextArea;
import forge.toolbox.FSkin.SkinnedPanel;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;

/**
 * Dialog for Event node interaction. Shows event description and choice buttons.
 */
public class EventDialog {

  private static final int DIALOG_WIDTH = 600;
  private static final int MIN_DIALOG_HEIGHT = 400;
  private static final int PANEL_INSETS = 20;
  private static final int FULL_WIDTH = DIALOG_WIDTH - 2 * PANEL_INSETS;
  private static final int BUTTON_WIDTH = FULL_WIDTH * 80 / 100;

  private final MainPanel panel;
  private final List<PreviewTarget> previewTargets = new ArrayList<>();
  private FOptionPane optionPane;
  private RoguePreviewPopup previewPopup;
  private EventChoice selectedChoice;

  public EventDialog(RogueEvent event, RogueRun run) {
    panel = new MainPanel();

    FLabel lblTitle = new FLabel.Builder()
        .text(event.getDisplayName())
        .fontSize(20).fontStyle(Font.BOLD).fontAlign(SwingConstants.CENTER).build();

    FTextArea txtDescription = new FTextArea(event.getDescription());
    txtDescription.setFont(txtDescription.getFont().deriveFont(14f));
    txtDescription.setSize(FULL_WIDTH, Short.MAX_VALUE);
    previewTargets.add(new PreviewTarget(txtDescription, event.getPreviewReferences()));

    int desiredHeight = PANEL_INSETS;

    panel.add(lblTitle, "w 100%!, h 60px!, ax center, gap 0 0 20px 10px, wrap");
    desiredHeight += 60 + 20 + 10;
    panel.add(txtDescription, "w 100%!, ax center, gap 0 0 10px 20px, wrap");
    desiredHeight += txtDescription.getPreferredSize().height + 10 + 20;

    for (EventChoice choice : event.getChoices()) {
      FButton btn = new FButton("<html><div style='padding:6px 10px;'><center><font size=4>" + choice.label()
          + "</font><br><font size=3>" + choice.effect().getDescription() + "</font></center></div></html>");
      btn.setSize(BUTTON_WIDTH, Short.MAX_VALUE);
      btn.addActionListener(e -> {
        hidePreview();
        selectedChoice = choice;
        optionPane.setResult(0);
        optionPane.setVisible(false);
      });
      boolean enabled = choice.effect().isChoiceAvailable(run);
      btn.setEnabled(enabled);
      if (!enabled) {
        btn.setToolTipText(choice.effect().getUnavailableReason(run));
      }
      previewTargets.add(new PreviewTarget(btn, choice.effect().getPreviewReferences()));
      panel.add(btn, "w 80%!, ax center, gap 0 0 10px 10px, wrap");
      desiredHeight += btn.getPreferredSize().height + 10 + 10;
    }

    int dialogHeight = Math.min(Math.max(desiredHeight + PANEL_INSETS, MIN_DIALOG_HEIGHT), getMaxDialogHeight());
    Dimension dialogSize = new Dimension(DIALOG_WIDTH, dialogHeight);
    panel.setPreferredSize(dialogSize);
    panel.setMinimumSize(dialogSize);
  }

  /** Show dialog and return selected choice, or null if skipped. */
  public EventChoice show() {
    optionPane = new FOptionPane(null, "Event", null, panel,
        List.of(), -1);
    optionPane.getTitleBar().setVisible(false);
    previewPopup = new RoguePreviewPopup();
    previewTargets.forEach(target -> previewPopup.attachTo(target.component(), target.references()));
    panel.revalidate();
    panel.repaint();
    optionPane.setVisible(true);
    hidePreview();
    optionPane.dispose();
    return selectedChoice;
  }

  private void hidePreview() {
    if (previewPopup != null) {
      previewPopup.hide();
    }
  }

  private static int getMaxDialogHeight() {
    GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
        .getDefaultScreenDevice().getDefaultConfiguration();
    Rectangle screenBounds = gc.getBounds();
    Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
    int usableHeight = screenBounds.height - screenInsets.top - screenInsets.bottom;
    return (int) (usableHeight * 0.9) - 80;
  }

  private record PreviewTarget(JComponent component, List<PreviewReference> references) {}

  private static class MainPanel extends SkinnedPanel {
    private MainPanel() {
      super(new MigLayout("insets 20, gap 0, wrap, align center", "[grow, center]", ""));
      setOpaque(false);
    }
  }
}
