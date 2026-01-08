package forge.screens.home.rogue;

import forge.localinstance.skin.FSkinProp;
import forge.toolbox.FButton;
import forge.toolbox.FLabel;
import forge.toolbox.FOptionPane;
import forge.toolbox.FSkin;
import forge.toolbox.FSkin.SkinnedPanel;
import forge.util.Localizer;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;

/**
 * Dialog for Sanctum node interaction.
 * Allows player to heal life or gain card removal credits.
 */
public class SanctumDialog {
    private static final int DIALOG_WIDTH = 500;
    private static final int DIALOG_HEIGHT = 300;

    public enum SanctumChoice {
        HEAL,
        REMOVE_CARDS,
        SKIP
    }

    private final MainPanel panel;
    private FOptionPane optionPane;
    private SanctumChoice choice = SanctumChoice.SKIP;

    /**
     * Create a Sanctum dialog.
     * @param currentLife Player's current life total
     * @param maxLife Maximum life (starting life)
     * @param healAmount Amount of life to heal (up to max)
     * @param freeRemoves Number of free card removals offered
     */
    public SanctumDialog(int currentLife, int maxLife, int healAmount, int freeRemoves) {

        // Create main panel
        panel = new MainPanel();

        // Title label
        FLabel lblTitle = new FLabel.Builder()
                .text("Sanctum")
                .fontSize(20)
                .fontStyle(Font.BOLD)
                .fontAlign(SwingConstants.CENTER)
                .build();

        // Description label
        FLabel lblDescription = new FLabel.Builder()
                .text("Choose your blessing:")
                .fontSize(14)
                .fontAlign(SwingConstants.CENTER)
                .build();

        // Heal button
        FButton btnHeal = new FButton("♥ Heal " + healAmount + " Life (max. up to " + maxLife + ")");
        btnHeal.addActionListener(e -> {
            choice = SanctumChoice.HEAL;
            optionPane.setResult(0);
            optionPane.setVisible(false);
        });

        // Check if healing is possible
        if (currentLife >= maxLife) {
            btnHeal.setEnabled(false);
            btnHeal.setToolTipText("You are already at maximum life");
        }

        // Remove cards button
        FButton btnRemove = new FButton("Gain " + freeRemoves + " Card Removal Credits");
        btnRemove.setIcon(FSkin.getIcon(FSkinProp.ICO_CARD_IMAGE));
        btnRemove.addActionListener(e -> {
            choice = SanctumChoice.REMOVE_CARDS;
            optionPane.setResult(0);
            optionPane.setVisible(false);
        });

        // Add components to panel
        panel.add(lblTitle, "w 100%!, h 60px!, ax center, gap 0 0 20px 10px, wrap");
        panel.add(lblDescription, "w 100%!, h 30px!, ax center, gap 0 0 10px 20px, wrap");
        panel.add(btnHeal, "w 70%!, h 50px!, ax center, gap 0 0 10px 10px, wrap");
        panel.add(btnRemove, "w 70%!, h 50px!, ax center, gap 0 0 10px 10px");

        Dimension dialogSize = new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT);
        panel.setPreferredSize(dialogSize);
        panel.setMinimumSize(dialogSize);
    }

    /**
     * Show the dialog and return the player's choice.
     * @return The selected choice (HEAL, REMOVE_CARDS, or SKIP)
     */
    public SanctumChoice show() {
        final Localizer localizer = Localizer.getInstance();
        optionPane = new FOptionPane(
                null,
                "Sanctum",
                null,
                panel,
                List.of(localizer.getMessage("lblSkip")),
                -1
        );

        panel.revalidate();
        panel.repaint();

        optionPane.setVisible(true);
        optionPane.dispose();

        return choice;
    }

    private static class MainPanel extends SkinnedPanel {
        private MainPanel() {
            super(new MigLayout("insets 20, gap 0, wrap, align center", "[grow, center]", ""));
            setOpaque(false);
        }
    }
}
