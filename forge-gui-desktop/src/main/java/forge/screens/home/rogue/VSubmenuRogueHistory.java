package forge.screens.home.rogue;

import forge.gamemodes.rogue.RogueRunHistoryEntry;
import forge.gui.framework.DragCell;
import forge.gui.framework.DragTab;
import forge.gui.framework.EDocID;
import forge.localinstance.skin.FSkinProp;
import forge.screens.home.EMenuGroup;
import forge.screens.home.IVSubmenu;
import forge.screens.home.VHomeUI;
import forge.toolbox.FButton;
import forge.toolbox.FLabel;
import forge.toolbox.FScrollPane;
import forge.toolbox.FSkin;
import java.awt.*;
import java.util.ArrayList;
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

  private final JPanel pnlContent = new JPanel(new MigLayout("insets 10, gap 0, wrap, fillx"));
  private final FScrollPane scrollContent;
  private final FButton btnBack;

  VSubmenuRogueHistory() {
    lblTitle.setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));
    pnlContent.setOpaque(false);

    scrollContent = new FScrollPane(pnlContent, true,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    btnBack = new FButton("Back");
    btnBack.setIcon(FSkin.getImage(FSkinProp.ICO_OPEN).resize(24, 24).getIcon());
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
        pnlContent.add(new RunHistoryCard(entries.get(i), onViewDeck), "growx, gapbottom 8px");
      }
    }

    pnlContent.revalidate();
    pnlContent.repaint();
  }

  public JButton getBtnBack() { return btnBack; }

  @Override
  public void setParentCell(DragCell cell0) { this.parentCell = cell0; }

  @Override
  public DragCell getParentCell() { return parentCell; }

  /**
   * Panel displaying a single run history entry.
   * Layout: avatar on left, info rows on right, all left-aligned.
   */
  static class RunHistoryCard extends FSkin.SkinnedPanel {

    private boolean isHovered = false;

    RunHistoryCard(RogueRunHistoryEntry entry, Consumer<RogueRunHistoryEntry> onViewDeck) {
      super(new MigLayout("insets 10 12 10 12, gap 0", "[65px][grow]", ""));
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

      // Left column: Avatar (vertically centered)
      FLabel lblAvatar = new FLabel.Builder().build();
      lblAvatar.setIcon(FSkin.getAvatars().get(entry.getAvatarIndex()));
      lblAvatar.setPreferredSize(new Dimension(60, 60));
      add(lblAvatar, "cell 0 0, spany, w 60px!, h 60px!, ay top, gaptop 2px");

      // Right column: Info panel with all text rows left-aligned
      JPanel infoPanel = new JPanel(new MigLayout("insets 0, gap 0, wrap", "[grow][right]", "[]2[]2[]2[]2[]"));
      infoPanel.setOpaque(false);

      // Row 0: Commander name (left) + Outcome (right)
      FLabel lblName = new FLabel.Builder()
          .text(entry.getCommanderName())
          .fontSize(15)
          .fontStyle(Font.BOLD)
          .fontAlign(SwingConstants.LEFT)
          .build();

      Color outcomeColor;
      switch (entry.getOutcome()) {
        case "VICTORY": outcomeColor = new Color(0, 200, 0); break;
        case "DEFEAT": outcomeColor = new Color(220, 50, 50); break;
        default: outcomeColor = Color.GRAY; break;
      }
      FLabel lblOutcome = new FLabel.Builder()
          .text(entry.getOutcome())
          .fontSize(13)
          .fontStyle(Font.BOLD)
          .build();
      lblOutcome.setForeground(outcomeColor);

      infoPanel.add(lblName, "growx");
      infoPanel.add(lblOutcome);

      // Row 1: Detail (boss/defeated by) + timestamp
      String detail = "";
      if ("VICTORY".equals(entry.getOutcome()) && !entry.getBossOrDefeatedBy().isEmpty()) {
        detail = "Boss slain: " + entry.getBossOrDefeatedBy();
      } else if ("DEFEAT".equals(entry.getOutcome()) && !entry.getBossOrDefeatedBy().isEmpty()) {
        detail = "Defeated by: " + entry.getBossOrDefeatedBy();
      }
      if (!detail.isEmpty()) {
        infoPanel.add(new FLabel.Builder().text(detail).fontSize(12)
            .fontAlign(SwingConstants.LEFT).build(), "growx");
      } else {
        infoPanel.add(new JPanel() {{ setOpaque(false); }}, "growx");
      }
      infoPanel.add(new FLabel.Builder().text(entry.getTimestamp()).fontSize(11).build());

      // Row 2: Visited planes
      if (!entry.getVisitedPlanes().isEmpty()) {
        String planes = "Planes: " + String.join(", ", entry.getVisitedPlanes());
        FLabel lblPlanes = new FLabel.Builder().text(planes).fontSize(11)
            .fontAlign(SwingConstants.LEFT).build();
        lblPlanes.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT).brighter());
        infoPanel.add(lblPlanes, "span 2, growx");
      }

      // Row 3: Extra nodes
      if (!entry.getExtraNodes().isEmpty()) {
        String extras = "Stops: " + String.join(", ", entry.getExtraNodes());
        FLabel lblExtras = new FLabel.Builder().text(extras).fontSize(11)
            .fontAlign(SwingConstants.LEFT).build();
        lblExtras.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT).brighter());
        infoPanel.add(lblExtras, "span 2, growx");
      }

      // Row 4: Life/Gold (left) + View Deck button (right)
      String stats = "Life: " + entry.getFinalLife() + "  |  Gold: " + entry.getFinalGold();
      infoPanel.add(new FLabel.Builder().text(stats).fontSize(12)
          .fontAlign(SwingConstants.LEFT).build(), "growx");

      if (entry.getDeckSnapshot() != null) {
        FButton btnViewDeck = new FButton("View Deck");
        btnViewDeck.addActionListener(e -> onViewDeck.accept(entry));
        infoPanel.add(btnViewDeck, "w 100px!, h 26px!");
      }

      add(infoPanel, "cell 1 0, growx");
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
}
