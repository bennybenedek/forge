package forge.screens.home.rogue;

import forge.gamemodes.rogue.effect.NPCBoon;
import forge.gamemodes.rogue.npc.NPCContext;
import forge.gamemodes.rogue.npc.NPCContext.NPCChoice;
import forge.toolbox.*;
import forge.toolbox.FSkin.SkinnedPanel;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;

/**
 * Dialog for NPC encounters. Shows avatar, flavor text, and boon choice buttons.
 */
public class NPCDialog {

    private static final int DIALOG_WIDTH = 600;
    private static final int DIALOG_HEIGHT = 480;

    private final MainPanel panel;
    private FOptionPane optionPane;
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

        panel.add(lblTitle, "w 100%!, h 40px!, ax center, gap 0 0 20px 10px, wrap");
        panel.add(lblAvatar, "w 100px!, h 100px!, ax center, gap 0 0 10px 10px, wrap");
        panel.add(txtFlavor, "w 100%!, ax center, gap 0 0 10px 20px, wrap");

        if (ctx.choices().isEmpty()) {
            // Informational dialog — single Continue button
            FButton btn = new FButton("<html><div style='padding:6px 10px;'><center><font size=4>Continue</font></center></div></html>");
            btn.addActionListener(e -> {
                optionPane.setResult(0);
                optionPane.setVisible(false);
            });
            panel.add(btn, "w 80%!, ax center, gap 0 0 10px 10px, wrap");
        } else {
            for (NPCChoice choice : ctx.choices()) {
                String desc = choice.boon() != null ? choice.boon().getDescription() : "";
                String buttonHtml = desc.isEmpty()
                        ? "<html><div style='padding:6px 10px;'><center><font size=4>" + choice.label() + "</font></center></div></html>"
                        : "<html><div style='padding:6px 10px;'><center><font size=4>" + choice.label()
                          + "</font><br><font size=3>" + desc + "</font></center></div></html>";
                FButton btn = new FButton(buttonHtml);
                btn.addActionListener(e -> {
                    selectedBoon = choice.boon();
                    optionPane.setResult(0);
                    optionPane.setVisible(false);
                });
                panel.add(btn, "w 80%!, ax center, gap 0 0 10px 10px, wrap");
            }
        }

        Dimension dialogSize = new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT);
        panel.setPreferredSize(dialogSize);
        panel.setMinimumSize(dialogSize);
    }

    /** Show dialog and return selected NPCBoon, or null if closed without choosing. */
    public NPCBoon show() {
        optionPane = new FOptionPane(null, "NPC Encounter", null, panel,
                List.of(), -1);
        optionPane.getTitleBar().setVisible(false);
        panel.revalidate();
        panel.repaint();
        optionPane.setVisible(true);
        optionPane.dispose();
        return selectedBoon;
    }

    private static class MainPanel extends SkinnedPanel {
        private MainPanel() {
            super(new MigLayout("insets 20, gap 0, wrap, align center", "[grow, center]", ""));
            setOpaque(false);
        }
    }
}
