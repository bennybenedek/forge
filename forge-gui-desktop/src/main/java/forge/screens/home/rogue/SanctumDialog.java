package forge.screens.home.rogue;

import forge.gamemodes.rogue.PreviewReference;
import forge.gamemodes.rogue.TextHelper;
import forge.toolbox.FButton;
import forge.toolbox.FLabel;
import forge.toolbox.FOptionPane;
import forge.toolbox.FSkin.SkinnedPanel;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;

/**
 * Dialog for Sanctum node interaction. Allows player to rest or craft a carry item.
 */
public class SanctumDialog {

  private static final int DIALOG_WIDTH = 600;
  private static final int DIALOG_HEIGHT = 390;
  private static final int CHOICE_RESULT = 1;

  public enum SanctumChoice {
    HEAL,
    COOK,
    REFLECT,
    SKIP
  }

  private final MainPanel panel;
  private final List<PreviewTarget> previewTargets = new ArrayList<>();
  private FOptionPane optionPane;
  private RoguePreviewPopup previewPopup;
  private SanctumChoice choice = SanctumChoice.SKIP;

  /**
   * Create a Sanctum dialog.
   *
   * @param effectiveHealAmount Actual life gain the player would receive right now
   * @param restEnabled Whether REST should be selectable
   * @param restDisabledReason Tooltip shown when REST is disabled
   */
  public SanctumDialog(int effectiveHealAmount, boolean restEnabled, String restDisabledReason) {
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

    String restDescription = "Gain " + effectiveHealAmount + " Life & Cure all {{Wound}}s.";
    FButton btnRest = RogueButtonHelper.createChoiceButton(
        "Rest", TextHelper.stripPreviewMarkers(restDescription),
        TextHelper.extractPreviewReferences(restDescription));
    btnRest.addActionListener(e -> {
      hidePreview();
      choice = SanctumChoice.HEAL;
      optionPane.setResult(CHOICE_RESULT);
      optionPane.setVisible(false);
    });
    btnRest.setEnabled(restEnabled);
    if (!restEnabled && restDisabledReason != null && !restDisabledReason.isEmpty()) {
      btnRest.setToolTipText(restDisabledReason);
    }
    previewTargets.add(new PreviewTarget(btnRest, TextHelper.extractPreviewReferences(restDescription)));

    String cookDescription = "Gain a random Food {{Item}}.";
    FButton btnCook = RogueButtonHelper.createChoiceButton(
        "Cook", TextHelper.stripPreviewMarkers(cookDescription),
        TextHelper.extractPreviewReferences(cookDescription));
    btnCook.addActionListener(e -> {
      hidePreview();
      choice = SanctumChoice.COOK;
      optionPane.setResult(CHOICE_RESULT);
      optionPane.setVisible(false);
    });
    previewTargets.add(new PreviewTarget(btnCook, TextHelper.extractPreviewReferences(cookDescription)));

    String reflectDescription = "Gain 3 {{Removal Credits}}.";
    FButton btnReflect = RogueButtonHelper.createChoiceButton(
        "Reflect", TextHelper.stripPreviewMarkers(reflectDescription),
        TextHelper.extractPreviewReferences(reflectDescription));
    btnReflect.addActionListener(e -> {
      hidePreview();
      choice = SanctumChoice.REFLECT;
      optionPane.setResult(CHOICE_RESULT);
      optionPane.setVisible(false);
    });
    previewTargets.add(new PreviewTarget(btnReflect, TextHelper.extractPreviewReferences(reflectDescription)));

    panel.add(lblTitle, "w 100%!, h 60px!, ax center, gap 0 0 20px 10px, wrap");
    panel.add(lblDescription, "w 100%!, h 30px!, ax center, gap 0 0 10px 20px, wrap");
    panel.add(btnRest, "w 80%!, ax center, gap 0 0 10px 10px, wrap");
    panel.add(btnCook, "w 80%!, ax center, gap 0 0 10px 10px, wrap");
    panel.add(btnReflect, "w 80%!, ax center, gap 0 0 10px 10px, wrap");

    Dimension dialogSize = new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT);
    panel.setPreferredSize(dialogSize);
    panel.setMinimumSize(dialogSize);
  }

  /**
   * Show the dialog and return the player's choice.
   *
   * @return The selected choice (HEAL, COOK, REFLECT, or SKIP)
   */
  public SanctumChoice show() {
    optionPane = new FOptionPane(
        null,
        "Sanctum",
        null,
        panel,
        List.of("Skip"),
        0
    );
    optionPane.getTitleBar().setVisible(false);
    previewPopup = new RoguePreviewPopup();
    previewTargets.forEach(target -> previewPopup.attachTo(target.component(), target.references()));

    panel.revalidate();
    panel.repaint();

    optionPane.setVisible(true);
    hidePreview();
    optionPane.dispose();

    if (optionPane.getResult() == 0) {
      choice = SanctumChoice.SKIP;
    }
    return choice;
  }

  private void hidePreview() {
    if (previewPopup != null) {
      previewPopup.hide();
    }
  }

  private record PreviewTarget(JComponent component, List<PreviewReference> references) {}

  private static class MainPanel extends SkinnedPanel {

    private MainPanel() {
      super(new MigLayout("insets 20, gap 0, wrap, align center", "[grow, center]", ""));
      setOpaque(false);
    }
  }
}
