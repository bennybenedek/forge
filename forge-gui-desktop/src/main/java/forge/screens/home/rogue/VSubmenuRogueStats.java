package forge.screens.home.rogue;

import forge.gui.framework.DragCell;
import forge.gui.framework.DragTab;
import forge.gui.framework.EDocID;
import forge.localinstance.skin.FSkinProp;
import forge.screens.home.EMenuGroup;
import forge.screens.home.IVSubmenu;
import forge.screens.home.VHomeUI;
import forge.toolbox.FButton;
import forge.toolbox.FLabel;
import forge.toolbox.FSkin;
import forge.util.Localizer;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;

/**
 * Assembles Swing components for Rogue Commander statistics screen.
 * Shows player's meta progression statistics across all runs.
 */
public enum VSubmenuRogueStats implements IVSubmenu<CSubmenuRogueStats> {
    SINGLETON_INSTANCE;

    final Localizer localizer = Localizer.getInstance();
    private DragCell parentCell;
    private final DragTab tab = new DragTab("Rogue Stats");

    private final FLabel lblTitle = new FLabel.Builder()
        .text("Rogue Commander - Statistics")
        .fontAlign(SwingConstants.CENTER)
        .opaque(true)
        .fontSize(16)
        .build();

    // Run statistics
    private final FLabel lblRunsStarted = new FLabel.Builder().text("Runs Started: 0").fontSize(14).build();
    private final FLabel lblRunsCompleted = new FLabel.Builder().text("Runs Completed: 0").fontSize(14).build();
    private final FLabel lblRunsWon = new FLabel.Builder().text("Runs Won: 0").fontSize(14).build();
    private final FLabel lblRunsLost = new FLabel.Builder().text("Runs Lost: 0").fontSize(14).build();

    // Match statistics
    private final FLabel lblMatchesWon = new FLabel.Builder().text("Matches Won: 0").fontSize(14).build();
    private final FLabel lblMatchesLost = new FLabel.Builder().text("Matches Lost: 0").fontSize(14).build();

    // Milestones
    private final FLabel lblMaxLife = new FLabel.Builder().text("Max Life Reached: 0").fontSize(14).build();
    private final FLabel lblMaxGold = new FLabel.Builder().text("Max Gold Earned: 0").fontSize(14).build();
    private final FLabel lblMaxCreatureTypes = new FLabel.Builder().text("Max Creature Types: 0").fontSize(14).build();

    // Per-commander statistics (dynamically populated)
    private JPanel commanderStatsPanel;
    private final List<FLabel> commanderStatLabels = new ArrayList<>();

    // Buttons
    private final FButton btnBack;
    private final FButton btnReset;

    VSubmenuRogueStats() {
        lblTitle.setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));

        btnBack = new FButton("Back");
        btnBack.setIcon(FSkin.getImage(FSkinProp.ICO_OPEN).resize(24, 24).getIcon());

        btnReset = new FButton("Reset Progress");
        btnReset.setIcon(FSkin.getImage(FSkinProp.ICO_DELETE).resize(24, 24).getIcon());
    }

    /**
     * Update the display with current meta progress data.
     */
    public void updateDisplay(int runsStarted, int runsCompleted, int runsWon,
                              int matchesWon, int matchesLost,
                              int maxLife, int maxGold, int maxCreatureTypes) {
        lblRunsStarted.setText("Runs Started: " + runsStarted);
        lblRunsCompleted.setText("Runs Completed: " + runsCompleted);
        lblRunsWon.setText("Runs Won: " + runsWon);
        lblRunsLost.setText("Runs Lost: " + (runsCompleted - runsWon));
        lblMatchesWon.setText("Matches Won: " + matchesWon);
        lblMatchesLost.setText("Matches Lost: " + matchesLost);
        lblMaxLife.setText("Max Life Reached: " + maxLife);
        lblMaxGold.setText("Max Gold Earned: " + maxGold);
        lblMaxCreatureTypes.setText("Max Creature Types: " + maxCreatureTypes);
    }

    /**
     * Update the per-commander statistics display.
     * @param commanderStats Map of commander name -> [runsStarted, runsWon]
     */
    public void updateCommanderStats(Map<String, int[]> commanderStats) {
        if (commanderStatsPanel == null) {
            return;
        }

        // Clear existing labels
        commanderStatsPanel.removeAll();
        commanderStatLabels.clear();

        // Add section header
        FLabel lblCommanderSection = new FLabel.Builder()
            .text("Commander Statistics")
            .fontSize(16)
            .fontStyle(Font.BOLD)
            .build();
        commanderStatsPanel.add(lblCommanderSection, "gapbottom 10");

        if (commanderStats.isEmpty()) {
            FLabel lblNoData = new FLabel.Builder()
                .text("No commanders used yet")
                .fontSize(14)
                .build();
            commanderStatsPanel.add(lblNoData);
            commanderStatLabels.add(lblNoData);
        } else {
            // Sort commanders alphabetically
            List<String> sortedCommanders = new ArrayList<>(commanderStats.keySet());
            sortedCommanders.sort(String::compareToIgnoreCase);

            for (String commander : sortedCommanders) {
                int[] stats = commanderStats.get(commander);
                int started = stats[0];
                int won = stats[1];

                FLabel lblCommander = new FLabel.Builder()
                    .text(commander + ": " + won + "/" + started + " runs won")
                    .fontSize(14)
                    .build();
                commanderStatsPanel.add(lblCommander);
                commanderStatLabels.add(lblCommander);
            }
        }

        commanderStatsPanel.revalidate();
        commanderStatsPanel.repaint();
    }

    @Override
    public EMenuGroup getGroupEnum() {
        return EMenuGroup.ROGUE;
    }

    @Override
    public String getMenuTitle() {
        return "Stats";
    }

    @Override
    public EDocID getItemEnum() {
        return EDocID.HOME_ROGUESTATS;
    }

    @Override
    public void populate() {
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().removeAll();
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().setLayout(new MigLayout("insets 0, gap 0, wrap"));

        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(lblTitle, "w 98%!, h 30px!, gap 1% 0 15px 15px");

        // Stats panel
        JPanel statsPanel = createStatsPanel();
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(statsPanel, "w 98%!, gap 1% 0 20px 20px");

        // Buttons panel
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0, gap 10"));
        buttonPanel.setOpaque(false);
        buttonPanel.add(btnBack, "w 180px!, h 40px!");
        buttonPanel.add(btnReset, "w 180px!, h 40px!");
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(buttonPanel, "ax center, gap 0 0 20px 20px");

        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().repaintSelf();
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().revalidate();
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 20, gap 5, wrap 1", "[250px]", ""));
        panel.setOpaque(false);

        // Section: Run Statistics
        FLabel lblRunSection = new FLabel.Builder()
            .text("Run Statistics")
            .fontSize(16)
            .fontStyle(Font.BOLD)
            .build();
        panel.add(lblRunSection, "gapbottom 10");
        panel.add(lblRunsStarted);
        panel.add(lblRunsCompleted);
        panel.add(lblRunsWon);
        panel.add(lblRunsLost, "gapbottom 20");

        // Section: Match Statistics
        FLabel lblMatchSection = new FLabel.Builder()
            .text("Match Statistics")
            .fontSize(16)
            .fontStyle(Font.BOLD)
            .build();
        panel.add(lblMatchSection, "gapbottom 10");
        panel.add(lblMatchesWon);
        panel.add(lblMatchesLost, "gapbottom 20");

        // Section: Milestones
        FLabel lblMilestoneSection = new FLabel.Builder()
            .text("Milestones")
            .fontSize(16)
            .fontStyle(Font.BOLD)
            .build();
        panel.add(lblMilestoneSection, "gapbottom 10");
        panel.add(lblMaxLife);
        panel.add(lblMaxGold);
        panel.add(lblMaxCreatureTypes, "gapbottom 20");

        // Section: Commander Statistics (dynamically populated)
        commanderStatsPanel = new JPanel(new MigLayout("insets 0, gap 5, wrap 1", "[250px]", ""));
        commanderStatsPanel.setOpaque(false);
        panel.add(commanderStatsPanel, "grow");

        return panel;
    }

    public JButton getBtnBack() {
        return btnBack;
    }

    public JButton getBtnReset() {
        return btnReset;
    }

    @Override
    public EDocID getDocumentID() {
        return EDocID.HOME_ROGUESTATS;
    }

    @Override
    public DragTab getTabLabel() {
        return tab;
    }

    @Override
    public CSubmenuRogueStats getLayoutControl() {
        return CSubmenuRogueStats.SINGLETON_INSTANCE;
    }

    @Override
    public void setParentCell(DragCell cell0) {
        this.parentCell = cell0;
    }

    @Override
    public DragCell getParentCell() {
        return parentCell;
    }
}
