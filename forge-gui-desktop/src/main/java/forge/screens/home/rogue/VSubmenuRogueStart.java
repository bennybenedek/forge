package forge.screens.home.rogue;

import forge.gui.framework.DragCell;
import forge.gui.framework.DragTab;
import forge.gui.framework.EDocID;
import forge.localinstance.properties.ForgePreferences;
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
 * Assembles Swing components for Rogue Commander start screen. Allows player to select a commander
 * visually and begin a new run.
 */
public enum VSubmenuRogueStart implements IVSubmenu<CSubmenuRogueStart> {
  SINGLETON_INSTANCE;
  final Localizer localizer = Localizer.getInstance();

  // Card display constants
  private static final int BASE_CARD_WIDTH = 250;  // Cards scale down dynamically if too big
  private static final int CARD_SPACING = 15;
  private static final int MAX_CARDS_PER_ROW = 7;
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
  private CardUtil zoomUtil; // Lazily initialized on first use

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

  private final FLabel lblDescriptionLock = new FLabel.Builder()
      .icon(FSkin.getImage(FSkinProp.ICO_LOCK).resize(14, 14)).build();

  private final FLabel lblThemeLabel = new FLabel.Builder()
      .text("Themes:")
      .fontSize(14)
      .build();

  private final FTextArea txtDescription = new FTextArea("");
  private final FTextArea txtTheme = new FTextArea("");
  private FScrollPane scrollTheme;

  // Action buttons
  private final FButton btnBeginRun;
  private final FButton btnStats;
  private final FButton btnAether;
  private final FButton btnHistory;

  // Dev buttons
  private final FButton btnDevUnlockAll = new FButton("[Dev] Unlock All");
  private final FButton btnDevNPCProgress = new FButton("[Dev] NPC Progress");

  // Descension UI
  private final FCheckBox chkDescension = new FCheckBox("Descension Mode");
  private final FButton btnDescensionDown = new FButton("<");
  private final FLabel lblDescensionLock = new FLabel.Builder()
      .icon(FSkin.getImage(FSkinProp.ICO_LOCK).resize(20, 20)).build();
  private final FLabel lblDescensionLevel = new FLabel.Builder().text("").fontSize(14).build();
  private final FButton btnDescensionUp = new FButton(">");
  private final FLabel lblDescensionDesc = new FLabel.Builder().text("").fontSize(12).build();
  private JPanel pnlDescensionLevel;
  private JPanel pnlDescensionLock;
  private final FLabel lblDescensionLockText = new FLabel.Builder().text("").fontSize(12).build();

  VSubmenuRogueStart() {
    // Setup buttons with icons (matching Path View style)
    btnBeginRun = new FButton("Start Run");
    btnBeginRun.setIcon(FSkin.getImage(FSkinProp.ICO_ALPHASTRIKE).resize(24, 24).getIcon());

    btnStats = new FButton("Codex");
    btnStats.setIcon(FSkin.getImage(FSkinProp.ICO_QUEST_BOOK).resize(24, 24).getIcon());

    btnAether = new FButton("Aether");
    btnAether.setIcon(FSkin.getImage(FSkinProp.ICO_QUEST_GOLD).resize(24, 24).getIcon());

    btnHistory = new FButton("History");
    btnHistory.setIcon(FSkin.getImage(FSkinProp.ICO_QUEST_NOTES).resize(24, 24).getIcon());

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
    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().setLayout(new MigLayout("insets 0, gap 0, wrap"));

    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(lblTitle, "w 98%!, h 30px!, gap 1% 0 15px 15px");
    // Commander grid - takes its preferred size based on cards
    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(pnlCommanderGrid, "w 98%!, gap 1% 0 15px 15px");

    // Commander details panel - right after grid
    JPanel pnlDetails = createDetailsPanel();
    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(pnlDetails, "w 98%!, gap 1% 0 0 15px");

    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().repaintSelf();
    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().revalidate();
  }

  private JPanel createDetailsPanel() {
    JPanel panel = new JPanel();
    panel.setOpaque(false);
    panel.setLayout(
        new MigLayout("insets 0, gap 0, wrap 2", "[150px][grow]", "[]10px[]10px[]10px[]10px[]20px[]"));

    // Row 0: Commander name
    panel.add(new FLabel.Builder().text("Commander:").fontSize(14).build(),
        "cell 0 0, alignx left, aligny top");
    panel.add(lblCommanderName, "cell 1 0, alignx left, growx");

    // Row 1: Description (label can change to "Unlock:" for locked commanders)
    lblDescriptionLock.setVisible(false);
    panel.add(lblDescriptionLock, "cell 0 1, w 14px!, h 14px!, aligny top, hidemode 3, split 2");
    panel.add(lblDescriptionLabel, "cell 0 1, alignx left, aligny top");
    panel.add(new FScrollPane(txtDescription, false,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER),
        "cell 1 1, growx, h 60px!");

    // Row 2: Theme (hidden for locked commanders)
    panel.add(lblThemeLabel, "cell 0 2, alignx left, aligny top");
    scrollTheme = new FScrollPane(txtTheme, false,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    panel.add(scrollTheme, "cell 1 2, growx, h 40px!");

    // Row 3: Descension checkbox (unlocked) OR global lock indicator (locked)
    chkDescension.setVisible(false);
    panel.add(chkDescension, "cell 0 3, span 2, alignx left, hidemode 3");

    pnlDescensionLock = new JPanel(new MigLayout("insets 0, gap 5"));
    pnlDescensionLock.setOpaque(false);
    pnlDescensionLock.add(
        new FLabel.Builder().icon(FSkin.getImage(FSkinProp.ICO_LOCK).resize(20, 20)).build(),
        "w 20px!, h 20px!");
    pnlDescensionLock.add(lblDescensionLockText, "growx");
    pnlDescensionLock.setVisible(false);
    panel.add(pnlDescensionLock, "cell 0 3, span 2, growx, hidemode 3");

    // Row 4: Descension level selector (hidden by default)
    pnlDescensionLevel = new JPanel(new MigLayout("insets 0, gap 5, wrap 1"));
    pnlDescensionLevel.setOpaque(false);
    JPanel arrowRow = new JPanel(new MigLayout("insets 0, gap 5"));
    arrowRow.setOpaque(false);
    arrowRow.add(btnDescensionDown, "w 50px!, h 30px!");
    arrowRow.add(lblDescensionLock, "w 20px!, h 20px!, hidemode 3");
    arrowRow.add(lblDescensionLevel, "growx");
    arrowRow.add(btnDescensionUp, "w 50px!, h 30px!");
    pnlDescensionLevel.add(arrowRow, "growx");
    pnlDescensionLevel.add(lblDescensionDesc, "growx");
    pnlDescensionLevel.setVisible(false);
    panel.add(pnlDescensionLevel, "cell 0 4, span 2, growx");

    // Row 5: Buttons
    JPanel buttonPanel = new JPanel(new MigLayout("insets 0, gap 10"));
    buttonPanel.setOpaque(false);
    buttonPanel.add(btnBeginRun, "w 150px!, h 40px!");
    buttonPanel.add(btnAether, "w 150px!, h 40px!");
    buttonPanel.add(btnHistory, "w 150px!, h 40px!");
    buttonPanel.add(btnStats, "w 150px!, h 40px!");
    if (ForgePreferences.DEV_MODE) {
      buttonPanel.add(btnDevUnlockAll, "w 150px!, h 40px!");
      buttonPanel.add(btnDevNPCProgress, "w 150px!, h 40px!");
    }
    panel.add(buttonPanel, "cell 0 5, span 2, alignx center");

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

  public FLabel getLblDescriptionLock() {
    return lblDescriptionLock;
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

  public JButton getBtnHistory() {
    return btnHistory;
  }

  public JButton getBtnDevUnlockAll() {
    return btnDevUnlockAll;
  }

  public JButton getBtnDevNPCProgress() {
    return btnDevNPCProgress;
  }

  public JCheckBox getChkDescension() {
    return chkDescension;
  }

  public FButton getBtnDescensionDown() {
    return btnDescensionDown;
  }

  public FLabel getLblDescensionLevel() {
    return lblDescensionLevel;
  }

  public FButton getBtnDescensionUp() {
    return btnDescensionUp;
  }

  public FLabel getLblDescensionLock() {
    return lblDescensionLock;
  }

  public FLabel getLblDescensionDesc() {
    return lblDescensionDesc;
  }

  public JPanel getPnlDescensionLevel() {
    return pnlDescensionLevel;
  }

  public JPanel getPnlDescensionLock() {
    return pnlDescensionLock;
  }

  public FLabel getLblDescensionLockText() {
    return lblDescensionLockText;
  }

  public List<CommanderCardPanel> getCommanderPanels() {
    return commanderPanels;
  }

  public CommanderGridPanel getCommanderGridPanel() {
    return pnlCommanderGrid;
  }

  /**
   * Get the zoom utility, creating it lazily if needed. This ensures the window hierarchy is ready
   * when zoom is first used.
   */
  public CardUtil getZoomUtil() {
    if (zoomUtil == null) {
      Window window = SwingUtilities.getWindowAncestor(pnlCommanderGrid);
      if (window != null) {
        zoomUtil = new CardUtil(window);
        zoomUtil.setupZoomOverlay();
      }
    }
    return zoomUtil;
  }

  /**
   * Panel that displays commander cards in a responsive grid. Cards scale down on smaller screens,
   * with optimal cards-per-row calculated dynamically.
   */
  public class CommanderGridPanel extends FSkin.SkinnedPanel {

    private int cardWidth;
    private int cardHeight;

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
      int numCards = commanderPanels.size();
      int availableHeight = totalHeight - 30; // padding

      // Find the cardsPerRow that gives the largest cards while fitting all
      int bestCardsPerRow = 1;
      int bestCardWidth = 0;

      for (int tryCardsPerRow = 1; tryCardsPerRow <= Math.min(numCards, MAX_CARDS_PER_ROW);
          tryCardsPerRow++) {
        int tryNumRows = (int) Math.ceil(numCards / (double) tryCardsPerRow);
          if (tryNumRows > MAX_ROWS) {
              continue;
          }

        // Calculate max card width based on width constraint
        int maxWidthFromWidth = (totalWidth - (tryCardsPerRow - 1) * CARD_SPACING) / tryCardsPerRow;

        // Calculate max card width based on height constraint
        int availableForCards = availableHeight - (tryNumRows - 1) * CARD_SPACING;
        int maxHeightFromHeight = availableForCards / tryNumRows;
        int maxWidthFromHeight = Math.round(maxHeightFromHeight / CardPanel.ASPECT_RATIO);

        // Take the smaller of the two (limiting factor)
        int maxCardWidth = Math.min(maxWidthFromWidth, maxWidthFromHeight);

        // Cap at base size (no growth beyond base)
        maxCardWidth = Math.min(maxCardWidth, BASE_CARD_WIDTH);

        if (maxCardWidth > bestCardWidth) {
          bestCardWidth = maxCardWidth;
          bestCardsPerRow = tryCardsPerRow;
        }
      }

      int cardsPerRow = bestCardsPerRow;
      int numRows = (int) Math.ceil(numCards / (double) cardsPerRow);

      // Apply the best card size
      cardWidth = bestCardWidth;
      cardHeight = Math.round(cardWidth * CardPanel.ASPECT_RATIO);

      // Calculate actual grid height and center vertically
      int actualGridHeight = numRows * cardHeight + (numRows - 1) * CARD_SPACING;
      int startY = Math.max(15, (totalHeight - actualGridHeight) / 2);

      // Layout cards
      int cardIndex = 0;
      int y = startY;

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

      // Calculate preferred size for max 2 rows at base card size
      int cardsPerRow = Math.min(MAX_CARDS_PER_ROW, commanderPanels.size());
      int numRows = Math.min(MAX_ROWS,
          (int) Math.ceil(commanderPanels.size() / (double) cardsPerRow));
      int baseCardHeight = Math.round(BASE_CARD_WIDTH * CardPanel.ASPECT_RATIO);
      // Height: rows of cards + spacing between rows + padding
      int height = numRows * baseCardHeight + (numRows - 1) * CARD_SPACING + 30;
      int width = cardsPerRow * (BASE_CARD_WIDTH + CARD_SPACING) - CARD_SPACING;
      return new Dimension(width, height);
    }
  }
}
