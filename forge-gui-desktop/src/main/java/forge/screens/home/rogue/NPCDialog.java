package forge.screens.home.rogue;

import forge.gamemodes.rogue.PreviewReference;
import forge.gamemodes.rogue.effect.NPCBoon;
import forge.gamemodes.rogue.npc.NPCContext;
import forge.gamemodes.rogue.npc.NPCContext.NPCChoice;
import forge.toolbox.FButton;
import forge.toolbox.FLabel;
import forge.toolbox.FOptionPane;
import forge.toolbox.FSkin;
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
 * Dialog for NPC encounters. Shows avatar, flavor text, and boon choice buttons.
 */
public class NPCDialog {

    private static final int DIALOG_WIDTH = 600;
    private static final int MIN_DIALOG_HEIGHT = 480;
    private static final int PANEL_INSETS = 20;
    private static final int FULL_WIDTH = DIALOG_WIDTH - 2 * PANEL_INSETS;
    private static final int BUTTON_WIDTH = FULL_WIDTH * 80 / 100;

    private final MainPanel panel;
    private final List<PreviewTarget> previewTargets = new ArrayList<>();
    private FOptionPane optionPane;
    private RoguePreviewPopup previewPopup;
    private NPCBoon selectedBoon;

    public NPCDialog(NPCContext ctx) {
        panel = new MainPanel();

        FLabel lblTitle = new FLabel.Builder()
                .text(ctx.npc().name)
                .fontSize(20).fontStyle(Font.BOLD).fontAlign(SwingConstants.CENTER).build();

        FLabel lblAvatar = new FLabel.Builder().build();
        lblAvatar.setIcon(FSkin.getAvatars().get(ctx.npc().avatarIndex));

        FTextArea txtFlavor = new FTextArea(ctx.flavorText());
        txtFlavor.setFont(txtFlavor.getFont().deriveFont(14f));
        txtFlavor.setSize(FULL_WIDTH, Short.MAX_VALUE);

        int desiredHeight = PANEL_INSETS;

        panel.add(lblTitle, "w 100%!, h 40px!, ax center, gap 0 0 20px 10px, wrap");
        desiredHeight += 40 + 20 + 10;

        panel.add(lblAvatar, "w 100px!, h 100px!, ax center, gap 0 0 10px 10px, wrap");
        desiredHeight += 100 + 10 + 10;

        panel.add(txtFlavor, "w 100%!, ax center, gap 0 0 10px 20px, wrap");
        desiredHeight += txtFlavor.getPreferredSize().height + 10 + 20;

        if (ctx.choices().isEmpty()) {
            FButton btn = new FButton("<html><div style='padding:6px 10px;'><center><font size=4>Continue</font></center></div></html>");
            btn.setSize(BUTTON_WIDTH, Short.MAX_VALUE);
            btn.addActionListener(e -> {
                hidePreview();
                optionPane.setResult(0);
                optionPane.setVisible(false);
            });
            panel.add(btn, "w 80%!, ax center, gap 0 0 10px 10px, wrap");
            desiredHeight += btn.getPreferredSize().height + 10 + 10;
        } else {
            for (NPCChoice choice : ctx.choices()) {
                String desc = choice.boon() != null ? choice.boon().getDescription() : "";
                String buttonHtml = desc.isEmpty()
                        ? "<html><div style='padding:6px 10px;'><center><font size=4>" + choice.label() + "</font></center></div></html>"
                        : "<html><div style='padding:6px 10px;'><center><font size=4>" + choice.label()
                        + "</font><br><font size=3>" + desc + "</font></center></div></html>";
                FButton btn = new FButton(buttonHtml);
                btn.setSize(BUTTON_WIDTH, Short.MAX_VALUE);
                btn.addActionListener(e -> {
                    hidePreview();
                    selectedBoon = choice.boon();
                    optionPane.setResult(0);
                    optionPane.setVisible(false);
                });
                previewTargets.add(new PreviewTarget(btn,
                        choice.boon() == null ? List.of() : choice.boon().getPreviewReferences()));
                panel.add(btn, "w 80%!, ax center, gap 0 0 10px 10px, wrap");
                desiredHeight += btn.getPreferredSize().height + 10 + 10;
            }
        }

        int dialogHeight = Math.min(Math.max(desiredHeight + PANEL_INSETS, MIN_DIALOG_HEIGHT), getMaxDialogHeight());
        Dimension dialogSize = new Dimension(DIALOG_WIDTH, dialogHeight);
        panel.setPreferredSize(dialogSize);
        panel.setMinimumSize(dialogSize);
    }

    /** Show dialog and return selected NPCBoon, or null if closed without choosing. */
    public NPCBoon show() {
        optionPane = new FOptionPane(null, "NPC Encounter", null, panel,
                List.of(), -1);
        optionPane.getTitleBar().setVisible(false);
        previewPopup = new RoguePreviewPopup();
        previewTargets.forEach(target -> previewPopup.attachTo(target.component(), target.references()));
        panel.revalidate();
        panel.repaint();
        optionPane.setVisible(true);
        hidePreview();
        optionPane.dispose();
        return selectedBoon;
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
