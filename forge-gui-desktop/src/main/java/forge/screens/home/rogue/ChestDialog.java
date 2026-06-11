package forge.screens.home.rogue;

import forge.gamemodes.rogue.PreviewReference;
import forge.gamemodes.rogue.effect.ChestEffect;
import forge.toolbox.FButton;
import forge.toolbox.FLabel;
import forge.toolbox.FOptionPane;
import forge.toolbox.FSkin.SkinnedPanel;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.List;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;

/**
 * Dialog for Chest node interaction. Shows one reward action plus an optional skip action.
 */
public class ChestDialog {

  private static final int DIALOG_WIDTH = 400;
  private static final int MIN_DIALOG_HEIGHT = 400;
  private static final int PANEL_INSETS = 20;

  private final MainPanel panel;
  private final FButton btnReward;
  private final List<PreviewReference> previewReferences;
  private FOptionPane optionPane;
  private RoguePreviewPopup previewPopup;
  private boolean rewardAccepted;

  public ChestDialog(ChestEffect loot) {
    panel = new MainPanel();

    FLabel lblTitle = new FLabel.Builder()
        .text(loot.getDisplayName())
        .fontSize(20).fontStyle(Font.BOLD).fontAlign(SwingConstants.CENTER).build();

    previewReferences = loot.getPreviewReferences();
    btnReward = RogueButtonHelper.createChoiceButton(
        "Take Loot", loot.getDescription(), previewReferences);
    btnReward.addActionListener(e -> {
      rewardAccepted = true;
      hidePreview();
      optionPane.setResult(0);
      optionPane.setVisible(false);
    });

    int desiredHeight = PANEL_INSETS;

    panel.add(lblTitle, "w 100%!, h 60px!, ax center, gap 0 0 20px 10px, wrap");
    desiredHeight += 60 + 20 + 10;
    panel.add(btnReward, "w 80%!, ax center, gap 0 0 10px 20px, wrap");
    desiredHeight += btnReward.getPreferredSize().height + 10 + 20;

    FButton btnSkip = RogueButtonHelper.createChoiceButton("Skip Loot", "");
    btnSkip.addActionListener(e -> {
      rewardAccepted = false;
      hidePreview();
      optionPane.setResult(0);
      optionPane.setVisible(false);
    });
    panel.add(btnSkip, "w 80%!, ax center, gap 0 0 10px 10px, wrap");
    desiredHeight += btnSkip.getPreferredSize().height + 10 + 10;

    int dialogHeight = Math.min(Math.max(desiredHeight + PANEL_INSETS, MIN_DIALOG_HEIGHT), getMaxDialogHeight());
    Dimension dialogSize = new Dimension(DIALOG_WIDTH, dialogHeight);
    panel.setPreferredSize(dialogSize);
    panel.setMinimumSize(dialogSize);
  }

  public boolean show() {
    rewardAccepted = false;
    previewPopup = new RoguePreviewPopup();
    previewPopup.attachTo(btnReward, previewReferences);
    optionPane = new FOptionPane(null, "Chest", null, panel,
        List.of(), -1);
    optionPane.getTitleBar().setVisible(false);
    panel.revalidate();
    panel.repaint();
    optionPane.setVisible(true);
    hidePreview();
    optionPane.dispose();
    return rewardAccepted;
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

  private static class MainPanel extends SkinnedPanel {
    private MainPanel() {
      super(new MigLayout("insets 20, gap 0, wrap, align center", "[grow, center]", ""));
      setOpaque(false);
    }
  }
}
