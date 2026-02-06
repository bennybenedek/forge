package forge.screens.home.rogue;

import forge.gui.framework.DragCell;
import forge.gui.framework.DragTab;
import forge.gui.framework.EDocID;
import forge.localinstance.skin.FSkinProp;
import forge.screens.home.EMenuGroup;
import forge.screens.home.IVSubmenu;
import forge.screens.home.VHomeUI;
import forge.toolbox.*;
import forge.util.Localizer;
import forge.view.arcane.CardPanel;
import java.awt.Dimension;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;

/**
 * Assembles Swing components for Rogue Commander start screen.
 * Allows player to select a commander visually and begin a new run.
 */
public enum VSubmenuRogueStart implements IVSubmenu<CSubmenuRogueStart> {
    SINGLETON_INSTANCE;
    final Localizer localizer = Localizer.getInstance();

    // Card display constants
    private static final int BASE_CARD_WIDTH = 223;
    private static final int CARD_SPACING = 15;
    private static final int MAX_CARDS_PER_ROW = 6;
    private static final int MAX_ROWS = 2;

    // Fields used with interface IVDoc
    private DragCell parentCell;
    private final DragTab tab = new DragTab("Start New Run");

    private final FLabel lblTitle = new FLabel.Builder()
        .text("Pick Your Rogue Commander")
        .opaque(true)
        .fontSize(16)
        .build();

    // Commander card grid
    private final CommanderGridPanel pnlCommanderGrid = new CommanderGridPanel();
    private final List<CommanderCardPanel> commanderPanels = new ArrayList<>();
    private CardZoomUtil zoomUtil; // Lazily initialized on first use

    // Commander details
    private final FLabel lblCommanderName = new FLabel.Builder()
        .text("")
        .fontSize(18)
        .fontAlign(SwingConstants.LEFT)
        .build();

    private final FLabel lblDescriptionLabel = new FLabel.Builder()
        .text("Description:")
        .fontSize(14)
        .build();

    private final FLabel lblThemeLabel = new FLabel.Builder()
        .text("Theme:")
        .fontSize(14)
        .build();

    private final FTextArea txtDescription = new FTextArea("");
    private final FTextArea txtTheme = new FTextArea("");
    private FScrollPane scrollTheme;

    // Action buttons
    private final FButton btnBeginRun;
    private final FButton btnStats;
    private final FButton btnAether;

    private VSubmenuRogueStart() {
        // Setup buttons with icons (matching Path View style)
        btnBeginRun = new FButton("Start Run");
        btnBeginRun.setIcon(FSkin.getImage(FSkinProp.ICO_ALPHASTRIKE).resize(24, 24).getIcon());

        btnStats = new FButton("Stats");
        btnStats.setIcon(FSkin.getImage(FSkinProp.ICO_QUEST_BOOK).resize(24, 24).getIcon());

        btnAether = new FButton("Aether");
        btnAether.setIcon(FSkin.getImage(FSkinProp.ICO_QUEST_GOLD).resize(24, 24).getIcon());

        lblTitle.setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));

        // Setup description text areas
        txtDescription.setOpaque(true);
        txtDescription.setEditable(false);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setFocusable(false);
        txtDescription.setFont(FSkin.getFont(14));
        txtDescription.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
        txtDescription.setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));

        txtTheme.setOpaque(true);
        txtTheme.setEditable(false);
        txtTheme.setLineWrap(true);
        txtTheme.setWrapStyleWord(true);
        txtTheme.setFocusable(false);
        txtTheme.setFont(FSkin.getFont(14));
        txtTheme.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
        txtTheme.setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));
    }

    @Override
    public EDocID getItemEnum() {
        return EDocID.HOME_ROGUESTART;
    }

    @Override
    public EDocID getDocumentID() {
        return EDocID.HOME_ROGUESTART;
    }

    @Override
    public DragTab getTabLabel() {
        return tab;
    }

    @Override
    public EMenuGroup getGroupEnum() {
        return EMenuGroup.ROGUE;
    }

    @Override
    public String getMenuTitle() {
        return "Start New Run";
    }

    @Override
    public CSubmenuRogueStart getLayoutControl() {
        return CSubmenuRogueStart.SINGLETON_INSTANCE;
    }

    @Override
    public void populate() {
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().removeAll();
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().setLayout(new MigLayout("insets 0, gap 0, wrap, fill"));

        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(lblTitle, "w 98%!, h 30px!, gap 1% 0 15px 15px");
        // Commander grid grows to fill available space, pushing details panel down
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(pnlCommanderGrid, "w 98%!, grow, push, gap 1% 0 15px 0");

        // Add commander details panel (fixed at bottom)
        JPanel pnlDetails = createDetailsPanel();
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(pnlDetails, "w 98%!, gap 1% 0 0 15px");

        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().repaintSelf();
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().revalidate();
    }

    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new MigLayout("insets 0, gap 0, wrap 2", "[150px][grow]", "[]10px[]10px[]20px[]"));

        // Row 1: Commander name
        panel.add(new FLabel.Builder().text("Commander:").fontSize(14).build(), "cell 0 0, alignx left, aligny top");
        panel.add(lblCommanderName, "cell 1 0, alignx left, growx");

        // Row 2: Description (label can change to "Unlock:" for locked commanders)
        panel.add(lblDescriptionLabel, "cell 0 1, alignx left, aligny top");
        panel.add(new FScrollPane(txtDescription, false,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER),
            "cell 1 1, growx, h 60px!");

        // Row 3: Theme (hidden for locked commanders)
        panel.add(lblThemeLabel, "cell 0 2, alignx left, aligny top");
        scrollTheme = new FScrollPane(txtTheme, false,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(scrollTheme, "cell 1 2, growx, h 40px!");

        // Row 4: Buttons
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0, gap 10"));
        buttonPanel.setOpaque(false);
        buttonPanel.add(btnBeginRun, "w 150px!, h 40px!");
        buttonPanel.add(btnAether, "w 150px!, h 40px!");
        buttonPanel.add(btnStats, "w 150px!, h 40px!");
        panel.add(buttonPanel, "cell 0 3, span 2, alignx center");

        return panel;
    }

    @Override
    public void setParentCell(DragCell cell0) {
        this.parentCell = cell0;
    }

    @Override
    public DragCell getParentCell() {
        return this.parentCell;
    }

    // Getters for controller access
    public FLabel getLblCommanderName() {
        return lblCommanderName;
    }

    public FLabel getLblDescriptionLabel() {
        return lblDescriptionLabel;
    }

    public FTextArea getTxtDescription() {
        return txtDescription;
    }

    public FTextArea getTxtTheme() {
        return txtTheme;
    }

    public FLabel getLblThemeLabel() {
        return lblThemeLabel;
    }

    public FScrollPane getScrollTheme() {
        return scrollTheme;
    }

    public JButton getBtnBeginRun() {
        return btnBeginRun;
    }

    public JButton getBtnStats() {
        return btnStats;
    }

    public JButton getBtnAether() {
        return btnAether;
    }

    public List<CommanderCardPanel> getCommanderPanels() {
        return commanderPanels;
    }

    public CommanderGridPanel getCommanderGridPanel() {
        return pnlCommanderGrid;
    }

    /**
     * Get the zoom utility, creating it lazily if needed.
     * This ensures the window hierarchy is ready when zoom is first used.
     */
    public CardZoomUtil getZoomUtil() {
        if (zoomUtil == null) {
            Window window = SwingUtilities.getWindowAncestor(pnlCommanderGrid);
            if (window != null) {
                zoomUtil = new CardZoomUtil(window);
                zoomUtil.setupZoomOverlay();
            }
        }
        return zoomUtil;
    }

    /**
     * Panel that displays commander cards in a grid (max 4 per row).
     */
    public class CommanderGridPanel extends FSkin.SkinnedPanel {
        // Computed card dimensions (may be scaled)
        private int cardWidth = BASE_CARD_WIDTH;
        private int cardHeight = Math.round(BASE_CARD_WIDTH * CardPanel.ASPECT_RATIO);

        public CommanderGridPanel() {
            super(null);
            setOpaque(false);
        }

        public void clear() {
            removeAll();
            commanderPanels.clear();
        }

        public void addCommanderPanel(CommanderCardPanel panel) {
            commanderPanels.add(panel);
            add(panel);
        }

        @Override
        public void doLayout() {
            if (commanderPanels.isEmpty()) {
                return;
            }

            int totalWidth = getWidth();
            int totalHeight = getHeight();

            // Calculate cards per row (max 6, but also fit within width)
            int cardsPerRow = Math.min(MAX_CARDS_PER_ROW,
                    Math.max(1, (totalWidth + CARD_SPACING) / (BASE_CARD_WIDTH + CARD_SPACING)));

            // Calculate number of rows needed (max 2)
            int numRows = Math.min(MAX_ROWS, (int) Math.ceil(commanderPanels.size() / (double) cardsPerRow));

            // Calculate scale factor to fit within available space
            int baseCardHeight = Math.round(BASE_CARD_WIDTH * CardPanel.ASPECT_RATIO);
            int desiredWidth = cardsPerRow * (BASE_CARD_WIDTH + CARD_SPACING) - CARD_SPACING;
            int desiredHeight = numRows * (baseCardHeight + CARD_SPACING) - CARD_SPACING + 30; // +30 padding

            float widthScale = totalWidth > 0 ? Math.min(1.0f, (float) totalWidth / desiredWidth) : 1.0f;
            float heightScale = totalHeight > 0 ? Math.min(1.0f, (float) totalHeight / desiredHeight) : 1.0f;
            float scale = Math.min(widthScale, heightScale);

            // Apply scale
            cardWidth = Math.round(BASE_CARD_WIDTH * scale);
            cardHeight = Math.round(cardWidth * CardPanel.ASPECT_RATIO);

            // Layout cards
            int cardIndex = 0;
            int y = 15;

            for (int row = 0; row < MAX_ROWS && cardIndex < commanderPanels.size(); row++) {
                int cardsInThisRow = Math.min(cardsPerRow, commanderPanels.size() - cardIndex);
                int rowWidth = cardsInThisRow * cardWidth + (cardsInThisRow - 1) * CARD_SPACING;
                int startX = (totalWidth - rowWidth) / 2;

                int x = startX;
                for (int col = 0; col < cardsInThisRow; col++) {
                    CommanderCardPanel panel = commanderPanels.get(cardIndex);
                    panel.setBounds(x, y, cardWidth, cardHeight);
                    x += cardWidth + CARD_SPACING;
                    cardIndex++;
                }

                y += cardHeight + CARD_SPACING;
            }
        }

        @Override
        public Dimension getPreferredSize() {
            if (commanderPanels.isEmpty()) {
                return new Dimension(0, 0);
            }

            // Calculate preferred size for 2 rows at base card size
            int cardsPerRow = Math.min(MAX_CARDS_PER_ROW, commanderPanels.size());
            int numRows = Math.min(MAX_ROWS, (int) Math.ceil(commanderPanels.size() / (double) cardsPerRow));
            int baseCardHeight = Math.round(BASE_CARD_WIDTH * CardPanel.ASPECT_RATIO);
            int height = numRows * (baseCardHeight + CARD_SPACING) + 15;
            int width = cardsPerRow * (BASE_CARD_WIDTH + CARD_SPACING) - CARD_SPACING;
            return new Dimension(width, height);
        }
    }
}
