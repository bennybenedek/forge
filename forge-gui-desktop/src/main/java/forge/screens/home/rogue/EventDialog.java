package forge.screens.home.rogue;

import forge.gamemodes.rogue.RogueEvent;
import forge.gamemodes.rogue.RogueEvent.EventChoice;
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
 * Dialog for Event node interaction. Shows event description and choice buttons.
 */
public class EventDialog {

  private static final int DIALOG_WIDTH = 500;
  private static final int DIALOG_HEIGHT = 400;

  private final MainPanel panel;
  private FOptionPane optionPane;
  private EventChoice selectedChoice;

  public EventDialog(RogueEvent event) {
    panel = new MainPanel();

    FLabel lblTitle = new FLabel.Builder()
        .text(event.getDisplayName())
        .fontSize(20).fontStyle(Font.BOLD).fontAlign(SwingConstants.CENTER).build();

    FLabel lblDescription = new FLabel.Builder()
        .text(event.getDescription())
        .fontSize(14).fontAlign(SwingConstants.CENTER).build();

    panel.add(lblTitle, "w 100%!, h 60px!, ax center, gap 0 0 20px 10px, wrap");
    panel.add(lblDescription, "w 100%!, h 30px!, ax center, gap 0 0 10px 20px, wrap");

    for (EventChoice choice : event.getChoices()) {
      FButton btn = new FButton("<html><center><font size=4>" + choice.label()
          + "</font><br><font size=3>" + choice.effect().getDescription() + "</font></center></html>");
      btn.addActionListener(e -> {
        selectedChoice = choice;
        optionPane.setResult(0);
        optionPane.setVisible(false);
      });
      panel.add(btn, "w 70%!, h 50px!, ax center, gap 0 0 10px 10px, wrap");
    }

    Dimension dialogSize = new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT);
    panel.setPreferredSize(dialogSize);
    panel.setMinimumSize(dialogSize);
  }

  /** Show dialog and return selected choice, or null if skipped. */
  public EventChoice show() {
    optionPane = new FOptionPane(null, "Event", null, panel,
        List.of(), -1);
    optionPane.getTitleBar().setVisible(false);
    panel.revalidate();
    panel.repaint();
    optionPane.setVisible(true);
    optionPane.dispose();
    return selectedChoice;
  }

  private static class MainPanel extends SkinnedPanel {
    private MainPanel() {
      super(new MigLayout("insets 20, gap 0, wrap, align center", "[grow, center]", ""));
      setOpaque(false);
    }
  }
}
