package forge.screens.home.rogue;

import forge.gamemodes.rogue.effect.ChestLoot;
import forge.toolbox.FButton;
import forge.toolbox.FLabel;
import forge.toolbox.FOptionPane;
import forge.toolbox.FTextArea;
import forge.toolbox.FSkin.SkinnedPanel;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;

/**
 * Dialog for Chest node interaction. Shows loot name and description.
 * Same structure as EventDialog.
 */
public class ChestDialog {

  private static final int DIALOG_WIDTH = 400;
  private static final int DIALOG_HEIGHT = 250;

  private final MainPanel panel;
  private FOptionPane optionPane;
  private final RogueCardPreviewPopup previewPopup = new RogueCardPreviewPopup();

  public ChestDialog(ChestLoot loot) {
    panel = new MainPanel();

    FLabel lblTitle = new FLabel.Builder()
        .text(loot.getDisplayName())
        .fontSize(20).fontStyle(Font.BOLD).fontAlign(SwingConstants.CENTER).build();

    FTextArea txtDescription = new FTextArea(loot.getDescription());
    txtDescription.setFont(txtDescription.getFont().deriveFont(14f));
    previewPopup.attachTo(txtDescription, loot.getPreviewCardName());

    panel.add(lblTitle, "w 100%!, h 60px!, ax center, gap 0 0 20px 10px, wrap");
    panel.add(txtDescription, "w 100%!, ax center, gap 0 0 10px 20px, wrap");

    FButton btn = new FButton("<html><div style='padding:6px 10px;'><center><font size=4>OK</font></center></div></html>");
    btn.addActionListener(e -> {
      previewPopup.hide();
      optionPane.setResult(0);
      optionPane.setVisible(false);
    });
    panel.add(btn, "w 80%!, ax center, gap 0 0 10px 10px, wrap");

    Dimension dialogSize = new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT);
    panel.setPreferredSize(dialogSize);
    panel.setMinimumSize(dialogSize);
  }

  public void show() {
    optionPane = new FOptionPane(null, "Chest", null, panel,
        List.of(), -1);
    optionPane.getTitleBar().setVisible(false);
    panel.revalidate();
    panel.repaint();
    optionPane.setVisible(true);
    previewPopup.hide();
    optionPane.dispose();
  }

  private static class MainPanel extends SkinnedPanel {
    private MainPanel() {
      super(new MigLayout("insets 20, gap 0, wrap, align center", "[grow, center]", ""));
      setOpaque(false);
    }
  }
}
