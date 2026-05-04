package forge.screens.home.rogue;

import forge.toolbox.FButton;
import forge.toolbox.FLabel;
import forge.toolbox.FOptionPane;
import forge.toolbox.FSkin.SkinnedPanel;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;

/**
 * Dialog for Sanctum node interaction. Allows player to heal life or craft a carry item.
 */
public class SanctumDialog {

  private static final int DIALOG_WIDTH = 600;
  private static final int DIALOG_HEIGHT = 380;

  public enum SanctumChoice {
    HEAL,
    COOK,
    SKIP
  }

  private final MainPanel panel;
  private FOptionPane optionPane;
  private SanctumChoice choice = SanctumChoice.SKIP;

  /**
   * Create a Sanctum dialog.
   *
   * @param healAmount Amount of life to heal (up to max)
   */
  public SanctumDialog(int healAmount) {
    panel = new MainPanel();

    FLabel lblTitle = new FLabel.Builder()
        .text("Sanctum")
        .fontSize(20)
        .fontStyle(Font.BOLD)
        .fontAlign(SwingConstants.CENTER)
        .build();

    FLabel lblDescription = new FLabel.Builder()
        .text("Choose your action:")
        .fontSize(14)
        .fontAlign(SwingConstants.CENTER)
        .build();

    FButton btnRest = new FButton(buildChoiceHtml(
        "REST", "Gain " + healAmount + " Life & Cure All Wounds"));
    btnRest.addActionListener(e -> {
      choice = SanctumChoice.HEAL;
      optionPane.setResult(0);
      optionPane.setVisible(false);
    });

    FButton btnCook = new FButton(buildChoiceHtml(
        "COOK", "Craft a random Food item"));
    btnCook.addActionListener(e -> {
      choice = SanctumChoice.COOK;
      optionPane.setResult(0);
      optionPane.setVisible(false);
    });

    FButton btnSkip = new FButton(buildChoiceHtml("SKIP", ""));
    btnSkip.addActionListener(e -> {
      choice = SanctumChoice.SKIP;
      optionPane.setResult(0);
      optionPane.setVisible(false);
    });

    panel.add(lblTitle, "w 100%!, h 60px!, ax center, gap 0 0 20px 10px, wrap");
    panel.add(lblDescription, "w 100%!, h 30px!, ax center, gap 0 0 10px 20px, wrap");
    panel.add(btnRest, "w 80%!, ax center, gap 0 0 10px 10px, wrap");
    panel.add(btnCook, "w 80%!, ax center, gap 0 0 10px 10px, wrap");
    panel.add(btnSkip, "w 80%!, ax center, gap 0 0 10px 10px, wrap");

    Dimension dialogSize = new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT);
    panel.setPreferredSize(dialogSize);
    panel.setMinimumSize(dialogSize);
  }

  /**
   * Show the dialog and return the player's choice.
   *
   * @return The selected choice (HEAL, COOK, or SKIP)
   */
  public SanctumChoice show() {
    optionPane = new FOptionPane(
        null,
        "Sanctum",
        null,
        panel,
        List.of(),
        -1
    );
    optionPane.getTitleBar().setVisible(false);

    panel.revalidate();
    panel.repaint();

    optionPane.setVisible(true);
    optionPane.dispose();

    return choice;
  }

  private static String buildChoiceHtml(String title, String description) {
    if (description == null || description.isEmpty()) {
      return "<html><div style='padding:6px 10px;'><center><font size=4>" + title
          + "</font></center></div></html>";
    }
    return "<html><div style='padding:6px 10px;'><center><font size=4>" + title
        + "</font><br><font size=3>" + description + "</font></center></div></html>";
  }

  private static class MainPanel extends SkinnedPanel {

    private MainPanel() {
      super(new MigLayout("insets 20, gap 0, wrap, align center", "[grow, center]", ""));
      setOpaque(false);
    }
  }
}
