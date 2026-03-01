package forge.screens.home.rogue;

import forge.gamemodes.rogue.RogueRunHistoryEntry;
import forge.gui.framework.DragCell;
import forge.gui.framework.DragTab;
import forge.gui.framework.EDocID;
import forge.localinstance.skin.FSkinProp;
import forge.screens.home.EMenuGroup;
import forge.screens.home.IVSubmenu;
import forge.screens.home.VHomeUI;
import forge.toolbox.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;

/**
 * View for Rogue Commander run history screen.
 */
public enum VSubmenuRogueHistory implements IVSubmenu<CSubmenuRogueHistory> {
  SINGLETON_INSTANCE;

  private DragCell parentCell;
  private final DragTab tab = new DragTab("Run History");

  private final FLabel lblTitle = new FLabel.Builder()
      .text("Rogue Commander - Run History")
      .fontAlign(SwingConstants.CENTER)
      .opaque(true)
      .fontSize(16)
      .build();

  private final ScrollablePanel pnlContent = new ScrollablePanel(new MigLayout("insets 10, gap 0, wrap, fillx"));
  private final FScrollPane scrollContent;
  private final FButton btnBack;
  private final FButton btnResetHistory;

  VSubmenuRogueHistory() {
    lblTitle.setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));
    pnlContent.setOpaque(false);

    scrollContent = new FScrollPane(pnlContent, true,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    btnBack = new FButton("Back");
    btnBack.setIcon(FSkin.getImage(FSkinProp.ICO_OPEN).resize(24, 24).getIcon());

    btnResetHistory = new FButton("Reset History");
    btnResetHistory.setIcon(FSkin.getImage(FSkinProp.ICO_DELETE).resize(24, 24).getIcon());
  }

  @Override
  public EMenuGroup getGroupEnum() { return EMenuGroup.ROGUE; }

  @Override
  public String getMenuTitle() { return "Run History"; }

  @Override
  public EDocID getItemEnum() { return EDocID.HOME_ROGUEHISTORY; }

  @Override
  public EDocID getDocumentID() { return EDocID.HOME_ROGUEHISTORY; }

  @Override
  public DragTab getTabLabel() { return tab; }

  @Override
  public CSubmenuRogueHistory getLayoutControl() { return CSubmenuRogueHistory.SINGLETON_INSTANCE; }

  @Override
  public void populate() {
    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().removeAll();
    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().setLayout(new MigLayout("insets 0, gap 0, wrap"));

    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(lblTitle, "w 98%!, h 30px!, gap 1% 0 15px 15px");
    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(scrollContent, "w 98%!, h 80%!, gap 1% 0 10px 10px");

    JPanel buttonPanel = new JPanel(new MigLayout("insets 0, gap 10"));
    buttonPanel.setOpaque(false);
    buttonPanel.add(btnBack, "w 180px!, h 40px!");
    buttonPanel.add(btnResetHistory, "w 180px!, h 40px!");
    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(buttonPanel, "ax center, gap 0 0 10px 10px");

    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().repaintSelf();
    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().revalidate();
  }

  public void displayHistory(List<RogueRunHistoryEntry> entries, Consumer<RogueRunHistoryEntry> onViewDeck) {
    pnlContent.removeAll();

    if (entries == null || entries.isEmpty()) {
      FLabel lblEmpty = new FLabel.Builder()
          .text("No completed runs yet.")
          .fontSize(14)
          .fontAlign(SwingConstants.CENTER)
          .build();
      pnlContent.add(lblEmpty, "growx, ax center, gaptop 40px");
    } else {
      // Display in reverse order (newest first)
      for (int i = entries.size() - 1; i >= 0; i--) {
        pnlContent.add(new RunHistoryCard(entries.get(i), onViewDeck), "growx, pushx, gapbottom 8");
      }
    }

    pnlContent.revalidate();
    pnlContent.repaint();
  }

  public JButton getBtnBack() { return btnBack; }
  public JButton getBtnResetHistory() { return btnResetHistory; }

  @Override
  public void setParentCell(DragCell cell0) { this.parentCell = cell0; }

  @Override
  public DragCell getParentCell() { return parentCell; }

  /**
   * Panel displaying a single run history entry.
   * Layout: avatar on left, info rows on right, all left aligned.
   */
  static class RunHistoryCard extends FSkin.SkinnedPanel {
    private static final int INSET = 10;
    private static final int AVATAR_SIZE = 60;
    private static final int AVATAR_GAP = 5;
    private static final int ROW_GAP = 2;
    private static final int BTN_WIDTH = 100;
    private static final int BTN_HEIGHT = 26;

    private boolean isHovered = false;
    private final FLabel lblAvatar;
    private final FLabel lblName;
    private final FLabel lblOutcome;
    private final FLabel lblDetail;
    private final FLabel lblTimestamp;
    private final FTextArea txtPath;
    private final FLabel lblStats;
    private final FButton btnViewDeck;

    RunHistoryCard(RogueRunHistoryEntry entry, Consumer<RogueRunHistoryEntry> onViewDeck) {
      super(null); // No layout manager — manual positioning
      setOpaque(true);
      setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));

      addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
          isHovered = true;
          repaint();
        }
        @Override
        public void mouseExited(java.awt.event.MouseEvent e) {
          isHovered = false;
          repaint();
        }
      });

      // Avatar
      lblAvatar = new FLabel.Builder().build();
      lblAvatar.setIcon(FSkin.getAvatars().get(entry.getAvatarIndex()));
      add(lblAvatar);

      // Commander name
      lblName = new FLabel.Builder()
          .text(entry.getCommanderName())
          .fontSize(15)
          .fontStyle(Font.BOLD)
          .fontAlign(SwingConstants.LEFT)
          .build();
      add(lblName);

      // Outcome
      Color outcomeColor;
      switch (entry.getOutcome()) {
        case "VICTORY": outcomeColor = new Color(0, 200, 0); break;
        case "DEFEAT": outcomeColor = new Color(220, 50, 50); break;
        default: outcomeColor = Color.GRAY; break;
      }
      lblOutcome = new FLabel.Builder()
          .text(entry.getOutcome())
          .fontSize(13)
          .fontStyle(Font.BOLD)
          .fontAlign(SwingConstants.RIGHT)
          .build();
      lblOutcome.setForeground(outcomeColor);
      add(lblOutcome);

      // Detail (boss/defeated by)
      String detail = "";
      if ("VICTORY".equals(entry.getOutcome()) && !entry.getBossOrDefeatedBy().isEmpty()) {
        detail = "Boss slain: " + entry.getBossOrDefeatedBy();
      } else if ("DEFEAT".equals(entry.getOutcome()) && !entry.getBossOrDefeatedBy().isEmpty()) {
        detail = "Defeated by: " + entry.getBossOrDefeatedBy();
      }
      lblDetail = new FLabel.Builder().text(detail).fontSize(12)
          .fontAlign(SwingConstants.LEFT).build();
      add(lblDetail);

      // Timestamp
      lblTimestamp = new FLabel.Builder()
          .text(entry.getTimestamp())
          .fontSize(11)
          .fontAlign(SwingConstants.RIGHT)
          .build();
      add(lblTimestamp);

      // Path
      if (!entry.getPath().isEmpty()) {
        txtPath = new FTextArea("Path: " + String.join(", ", entry.getPath()));
        txtPath.setEditable(false);
        txtPath.setLineWrap(true);
        txtPath.setWrapStyleWord(true);
        txtPath.setFocusable(false);
        txtPath.setOpaque(false);
        txtPath.setFont(FSkin.getFont(12));
        txtPath.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT).brighter());
        add(txtPath);
      } else {
        txtPath = null;
      }

      // Stats
      String stats = "Life: " + entry.getFinalLife() + "  |  Gold: " + entry.getFinalGold();
      lblStats = new FLabel.Builder().text(stats).fontSize(12)
          .fontAlign(SwingConstants.LEFT).build();
      add(lblStats);

      // View Deck button
      if (entry.getDeckSnapshot() != null) {
        btnViewDeck = new FButton("View Deck");
        btnViewDeck.addActionListener(e -> onViewDeck.accept(entry));
        add(btnViewDeck);
      } else {
        btnViewDeck = null;
      }
    }

    @Override
    public void doLayout() {
      int w = getWidth();
      int contentX = INSET + AVATAR_SIZE + AVATAR_GAP;
      int contentW = w - contentX - INSET;
      int rightW = btnViewDeck != null ? BTN_WIDTH + 5 : 0;
      int leftW = contentW - rightW;
      int y = INSET;
      int rowH = 20;

      // Avatar
      lblAvatar.setBounds(INSET, INSET, AVATAR_SIZE, AVATAR_SIZE);

      // Row 0: Name (left) + Outcome (right)
      lblName.setBounds(contentX, y, leftW, rowH);
      lblOutcome.setBounds(contentX + leftW, y, rightW, rowH);
      y += rowH + ROW_GAP;

      // Row 1: Detail (left) + Timestamp (right)
      lblDetail.setBounds(contentX, y, leftW, rowH);
      lblTimestamp.setBounds(contentX + leftW, y, rightW, rowH);
      y += rowH + ROW_GAP;

      // Row 2: Path (left column only, variable height)
      if (txtPath != null) {
        txtPath.setSize(leftW, Short.MAX_VALUE);
        int pathH = txtPath.getPreferredSize().height;
        txtPath.setBounds(contentX, y, leftW, pathH);
        y += pathH + ROW_GAP;
      }

      // Row 3: Stats (left) + View Deck button (right)
      if (btnViewDeck != null) {
        lblStats.setBounds(contentX, y, leftW, BTN_HEIGHT);
        btnViewDeck.setBounds(contentX + contentW - BTN_WIDTH, y, BTN_WIDTH, BTN_HEIGHT);
        y += BTN_HEIGHT + INSET + 4;
      } else {
        lblStats.setBounds(contentX, y, contentW, rowH);
        y += rowH + INSET;
      }

      // Update preferred size to match actual layout so parent allocates correct height
      setPreferredSize(new Dimension(w, y));
    }

    @Override
    public Dimension getPreferredSize() {
      int w = getParent() != null ? getParent().getWidth() : 500;
      int contentW = w - INSET - AVATAR_SIZE - AVATAR_GAP - INSET;
      int rightW = btnViewDeck != null ? BTN_WIDTH + 5 : 0;
      int leftW = contentW - rightW;
      int y = INSET;
      y += 20 + ROW_GAP; // row 0
      y += 20 + ROW_GAP; // row 1
      if (txtPath != null) {
        txtPath.setSize(Math.max(leftW, 100), Short.MAX_VALUE);
        y += txtPath.getPreferredSize().height + ROW_GAP;
      }
      y += (btnViewDeck != null ? BTN_HEIGHT + 4 : 20) + INSET;
      return new Dimension(w, y);
    }

    @Override
    public void paint(Graphics g) {
      super.paint(g);
      Graphics2D g2d = (Graphics2D) g;
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      if (isHovered) {
        g2d.setColor(new Color(180, 180, 180, 200));
        g2d.setStroke(new BasicStroke(3));
      } else {
        g2d.setColor(new Color(100, 100, 100, 150));
        g2d.setStroke(new BasicStroke(1));
      }
      g2d.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 10, 10);
    }
  }

  /** JPanel that implements Scrollable to track viewport width, preventing horizontal scrolling. */
  private static class ScrollablePanel extends JPanel implements Scrollable {
    ScrollablePanel(LayoutManager layout) {
      super(layout);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
      return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
      return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
      return 64;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
      return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
      return false;
    }
  }
}