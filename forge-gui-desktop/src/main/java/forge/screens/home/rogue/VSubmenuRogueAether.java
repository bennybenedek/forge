package forge.screens.home.rogue;

import forge.gamemodes.rogue.BoonType;
import forge.gui.framework.DragCell;
import forge.gui.framework.DragTab;
import forge.gui.framework.EDocID;
import forge.localinstance.skin.FSkinProp;
import forge.screens.home.EMenuGroup;
import forge.screens.home.IVSubmenu;
import forge.screens.home.VHomeUI;
import forge.toolbox.FButton;
import forge.toolbox.FCheckBox;
import forge.toolbox.FLabel;
import forge.toolbox.FSkin;
import java.awt.Font;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;

/**
 * Assembles Swing components for the Aether screen.
 * Allows players to spend Echoes on permanent upgrades (Boons).
 */
public enum VSubmenuRogueAether implements IVSubmenu<CSubmenuRogueAether> {
    SINGLETON_INSTANCE;

    private DragCell parentCell;
    private final DragTab tab = new DragTab("Aether");

    private final FLabel lblTitle = new FLabel.Builder()
        .text("The Aether - Codex of Echoes")
        .fontAlign(SwingConstants.CENTER)
        .opaque(true)
        .fontSize(16)
        .build();

    private final FLabel lblEchoes = new FLabel.Builder()
        .text("Echoes: 0")
        .fontSize(16)
        .fontStyle(Font.BOLD)
        .build();

    private final FLabel lblActiveBoons = new FLabel.Builder()
        .text("Active Boons: 0/3")
        .fontSize(14)
        .build();

    // Boon panels - one for each boon type
    private final Map<BoonType, BoonPanel> boonPanels = new EnumMap<>(BoonType.class);

    private final FButton btnBack;
    private final FButton btnResetBoons;

    VSubmenuRogueAether() {
        lblTitle.setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));
        lblEchoes.setIcon(FSkin.getIcon(FSkinProp.ICO_QUEST_GOLD));
        btnBack = new FButton("Back");
        btnBack.setIcon(FSkin.getImage(FSkinProp.ICO_OPEN).resize(24, 24).getIcon());
        btnResetBoons = new FButton("Reset Boons");
        btnResetBoons.setIcon(FSkin.getImage(FSkinProp.ICO_DELETE).resize(24, 24).getIcon());

        // Create boon panels once at construction time (so listeners can be attached in initialize)
        for (BoonType type : BoonType.values()) {
            boonPanels.put(type, new BoonPanel(type));
        }
    }

    @Override
    public EMenuGroup getGroupEnum() {
        return EMenuGroup.ROGUE;
    }

    @Override
    public String getMenuTitle() {
        return "Aether";
    }

    @Override
    public EDocID getItemEnum() {
        return EDocID.HOME_ROGUEAETHER;
    }

    @Override
    public EDocID getDocumentID() {
        return EDocID.HOME_ROGUEAETHER;
    }

    @Override
    public DragTab getTabLabel() {
        return tab;
    }

    @Override
    public CSubmenuRogueAether getLayoutControl() {
        return CSubmenuRogueAether.SINGLETON_INSTANCE;
    }

    @Override
    public void populate() {
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().removeAll();
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().setLayout(new MigLayout("insets 0, gap 0, wrap"));

        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(lblTitle, "w 98%!, h 30px!, gap 1% 0 15px 15px");

        // Echo and active boon display
        JPanel headerPanel = new JPanel(new MigLayout("insets 10, gap 20"));
        headerPanel.setOpaque(false);
        headerPanel.add(lblEchoes);
        headerPanel.add(lblActiveBoons);
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(headerPanel, "w 98%!, gap 1% 0 10px 10px");

        // Boon grid
        JPanel boonGrid = createBoonGrid();
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(boonGrid, "w 98%!, gap 1% 0 20px 20px");

        // Buttons
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0, gap 10"));
        buttonPanel.setOpaque(false);
        buttonPanel.add(btnBack, "w 180px!, h 40px!");
        buttonPanel.add(btnResetBoons, "w 180px!, h 40px!");
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(buttonPanel, "ax center, gap 0 0 20px 20px");

        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().repaintSelf();
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().revalidate();
    }

    private JPanel createBoonGrid() {
        JPanel panel = new JPanel(new MigLayout("insets 20, gap 15, wrap 2"));
        panel.setOpaque(false);

        // Reuse existing panels (created in constructor)
        for (BoonType type : BoonType.values()) {
            panel.add(boonPanels.get(type), "w 400px!, h 150px!");
        }

        return panel;
    }

    /**
     * Update the display with current meta progress data.
     */
    public void updateDisplay(int echoes, int activeBoonCount,
                              Map<BoonType, Integer> boonRanks,
                              Set<BoonType> activeBoons) {
        lblEchoes.setText("Echoes: " + echoes);
        lblActiveBoons.setText("Active Boons: " + activeBoonCount + "/3");

        for (Map.Entry<BoonType, BoonPanel> entry : boonPanels.entrySet()) {
            BoonType type = entry.getKey();
            BoonPanel panel = entry.getValue();
            int rank = boonRanks.getOrDefault(type, 0);
            boolean isActive = activeBoons.contains(type);
            panel.update(rank, isActive, echoes, activeBoonCount);
        }
    }

    public JButton getBtnBack() {
        return btnBack;
    }

    public JButton getBtnResetBoons() {
        return btnResetBoons;
    }

    public Map<BoonType, BoonPanel> getBoonPanels() {
        return boonPanels;
    }

    @Override
    public void setParentCell(DragCell cell0) {
        this.parentCell = cell0;
    }

    @Override
    public DragCell getParentCell() {
        return parentCell;
    }

    /**
     * Inner class representing a single boon panel in the grid.
     */
    public static class BoonPanel extends FSkin.SkinnedPanel {
        private final BoonType type;
        private final FLabel lblName;
        private final FLabel lblDescription;
        private final FLabel lblRank;
        private final FButton btnUpgrade;
        private final FCheckBox chkActive;
        // Cache own icon instance to avoid shared state issues (similar to NodePlaneboundPanel pattern)
        private final javax.swing.Icon cachedEchoIcon;

        public BoonPanel(BoonType type) {
            super(new MigLayout("insets 10 10 10 10, gap 5, wrap, fill"));
            this.type = type;

            // Create and cache own icon instance at construction time
            cachedEchoIcon = FSkin.getImage(FSkinProp.ICO_QUEST_GOLD).resize(16, 16).getIcon();

            setOpaque(true);
            setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));

            lblName = new FLabel.Builder()
                .text(type.getDisplayName())
                .fontSize(14)
                .fontStyle(Font.BOLD)
                .fontAlign(SwingConstants.CENTER)
                .build();

            lblDescription = new FLabel.Builder()
                .text(type.getDescription())
                .fontSize(12)
                .fontAlign(SwingConstants.CENTER)
                .build();

            lblRank = new FLabel.Builder()
                .text("Rank: 0/" + type.getMaxRank())
                .fontSize(12)
                .fontAlign(SwingConstants.CENTER)
                .build();

            btnUpgrade = new FButton("Unlock");
            btnUpgrade.setIcon(cachedEchoIcon);
            chkActive = new FCheckBox("Active");
            chkActive.setEnabled(false);

            add(lblName, "growx, ax center");
            add(lblDescription, "growx, ax center, wmax 370px");
            add(lblRank, "growx, ax center");

            // Spacer to push controls to bottom
            add(new JPanel() {{ setOpaque(false); }}, "growy, pushy");

            JPanel controls = new JPanel(new MigLayout("insets 0, gap 15"));
            controls.setOpaque(false);
            controls.add(btnUpgrade, "w 160px!, h 30px!");
            controls.add(chkActive);
            add(controls, "ax center, dock south");
        }

        /**
         * Update the panel display based on current boon state.
         */
        public void update(int rank, boolean active, int echoes, int activeBoonCount) {
            lblRank.setText("Rank: " + rank + "/" + type.getMaxRank());

            // Update description to show current effect value
            if (rank > 0) {
                lblDescription.setText(type.getDescriptionAtRank(rank));
            } else {
                lblDescription.setText(type.getDescription());
            }

            // Update upgrade button using cached icon instance
            if (rank >= type.getMaxRank()) {
                btnUpgrade.setText("Max Rank");
                btnUpgrade.setEnabled(false);
            } else {
                int cost = type.getEchoCostForRank(rank + 1);
                btnUpgrade.setText(rank == 0 ? "Unlock (" + cost + ")" : "Upgrade (" + cost + ")");
                btnUpgrade.setIcon(cachedEchoIcon);
                btnUpgrade.setEnabled(echoes >= cost);
            }

            // Update active checkbox
            chkActive.setSelected(active);
            boolean canToggle = rank > 0 && (active || activeBoonCount < 3);
            chkActive.setEnabled(canToggle);
        }

        public FButton getBtnUpgrade() {
            return btnUpgrade;
        }

        public FCheckBox getChkActive() {
            return chkActive;
        }
    }
}
