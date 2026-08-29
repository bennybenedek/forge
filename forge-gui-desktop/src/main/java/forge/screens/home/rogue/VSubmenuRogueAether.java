package forge.screens.home.rogue;

import forge.gamemodes.rogue.AetherUpgrade;
import forge.gamemodes.rogue.RogueMetaProgress;
import forge.gamemodes.rogue.effect.EchoEffect;
import forge.gui.framework.DragCell;
import forge.gui.framework.DragTab;
import forge.gui.framework.EDocID;
import forge.localinstance.properties.ForgePreferences;
import forge.localinstance.skin.FSkinProp;
import forge.screens.home.EMenuGroup;
import forge.screens.home.IVSubmenu;
import forge.screens.home.VHomeUI;
import forge.toolbox.FButton;
import forge.toolbox.FLabel;
import forge.toolbox.FScrollPane;
import forge.toolbox.FSkin;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;

/**
 * Assembles Swing components for the Aether screen. Allows players to spend Echoes on permanent
 * upgrades (Boons).
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

  private final FLabel lblSparks = new FLabel.Builder()
      .text("Sparks: 0")
      .fontSize(16)
      .fontStyle(Font.BOLD)
      .build();

  private final FLabel lblActiveBoons = new FLabel.Builder()
      .text("Active Boons: 0/3 (click to toggle)")
      .fontSize(14)
      .build();

  // Boon panels - one for each boon
  private final Map<EchoEffect, BoonPanel> boonPanels = new EnumMap<>(EchoEffect.class);

  // Aether Upgrade card (persistent so listener can be wired once in initialize)
  private final AetherUpgradeCard upgradeCard = new AetherUpgradeCard();

  private final FButton btnBack;
  private final FButton btnResetBoons;
  private final FButton btnDevMaxAether = new FButton("[Dev] Max Aether");
  private final FButton btnDevGainEchoes = new FButton("[Dev] +10 Echoes");
  private final FButton btnDevGainSparks = new FButton("[Dev] +10 Sparks");

  VSubmenuRogueAether() {
    lblTitle.setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));
    lblEchoes.setIcon(FSkin.getIcon(FSkinProp.ICO_QUEST_GOLD));
    lblSparks.setIcon(FSkin.getIcon(FSkinProp.ICO_QUEST_ELIXIR));
    lblSparks.setVisible(false);
    btnBack = new FButton("Back");
    btnBack.setIcon(FSkin.getImage(FSkinProp.ICO_OPEN).resize(24, 24).getIcon());
    btnResetBoons = new FButton("Reset Boons");
    btnResetBoons.setIcon(FSkin.getImage(FSkinProp.ICO_DELETE).resize(24, 24).getIcon());

    // Create boon panels once at construction time (so listeners can be attached in initialize)
    for (EchoEffect boon : EchoEffect.values()) {
      boonPanels.put(boon, new BoonPanel(boon));
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
    headerPanel.add(lblSparks, "hidemode 3");
    headerPanel.add(lblActiveBoons);
    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(headerPanel, "w 98%!, gap 1% 0 10px 10px");

    // Boon grid in a scroll pane — takes all remaining vertical space
    RogueMetaProgress progress = RogueMetaProgress.getInstance();
    int upgradeLevel = progress.getAetherUpgradeLevel();
    AetherUpgrade next = AetherUpgrade.forLevel(upgradeLevel + 1);
    JComponent cardToShow;
    if (!progress.isDescensionModeUnlocked()) {
      cardToShow = createLockedInfoPanel();
    } else if (next != null) {
      cardToShow = upgradeCard;
    } else {
      cardToShow = null;
    }
    BoonGridPanel boonGrid = createBoonGrid(upgradeLevel, cardToShow);
    FScrollPane scrollBoons = new FScrollPane(boonGrid, true,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(scrollBoons, "w 98%!, gap 1% 0 10px 10px, pushy, growy");

    // Buttons always below the scroll area — always visible
    JPanel buttonPanel = new JPanel(new MigLayout("insets 0, gap 10"));
    buttonPanel.setOpaque(false);
    buttonPanel.add(btnBack, "w 180px!, h 40px!");
    buttonPanel.add(btnResetBoons, "w 180px!, h 40px!");
    if (ForgePreferences.DEV_MODE) {
      buttonPanel.add(btnDevMaxAether, "w 180px!, h 40px!");
      buttonPanel.add(btnDevGainEchoes, "w 180px!, h 40px!");
      buttonPanel.add(btnDevGainSparks, "w 180px!, h 40px!");
    }
    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(buttonPanel, "ax center, gap 0 0 10px 10px");

    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().repaintSelf();
    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().revalidate();
  }

  private BoonGridPanel createBoonGrid(int upgradeLevel, JComponent card) {
    List<BoonPanel> visible = new ArrayList<>();
    for (EchoEffect boon : EchoEffect.values()) {
      if (boon.isAccessibleAt(upgradeLevel)) {
        visible.add(boonPanels.get(boon));
      }
    }
    return new BoonGridPanel(visible, card);
  }

  private static JComponent createLockedInfoPanel() {
    FSkin.SkinnedPanel panel = new FSkin.SkinnedPanel(
        new MigLayout("insets 20 20 20 20, gap 10, wrap, fill, align center center"));
    panel.setOpaque(true);
    panel.setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));

    FLabel lblInfo = new FLabel.Builder()
        .text("<html><center>Unlock Descension Mode and earn Sparks<br>"
            + "to get more Boons, Active Boon-Slots and other Upgrades for the Aether</center></html>")
        .icon(FSkin.getImage(FSkinProp.ICO_LOCK).resize(20, 20))
        .iconScaleAuto(false)
        .fontSize(13)
        .build();

    panel.add(lblInfo, "growx, push, align center");
    return panel;
  }

  /**
   * Update the display with current meta progress data.
   */
  public void updateDisplay(int echoes, int sparks, boolean descensionUnlocked, int activeBoonCount,
      int upgradeLevel, Map<EchoEffect, Integer> boonRanks, Set<EchoEffect> activeBoons) {
    lblEchoes.setText("Echoes: " + echoes);
    lblSparks.setText("Sparks: " + sparks);
    lblSparks.setVisible(descensionUnlocked);

    // Compute actual slot count for label
    int boonSlots = 3;
    for (int l = 1; l <= upgradeLevel; l++) {
      AetherUpgrade u = AetherUpgrade.forLevel(l);
      if (u != null) boonSlots += u.extraBoonSlots;
    }
    lblActiveBoons.setText("Active Boons: " + activeBoonCount + "/" + boonSlots + " (click to toggle)");

    // Update upgrade card if visible
    AetherUpgrade next = AetherUpgrade.forLevel(upgradeLevel + 1);
    if (next != null && descensionUnlocked) {
      upgradeCard.update(next, sparks);
    }

    // Update boon panels
    for (Map.Entry<EchoEffect, BoonPanel> entry : boonPanels.entrySet()) {
      EchoEffect boon = entry.getKey();
      BoonPanel panel = entry.getValue();
      int rank = boonRanks.getOrDefault(boon, 0);
      boolean isActive = activeBoons.contains(boon);
      panel.update(rank, isActive, echoes, activeBoonCount, upgradeLevel);
    }
  }

  public JButton getBtnBack() {
    return btnBack;
  }

  public JButton getBtnResetBoons() {
    return btnResetBoons;
  }

  public JButton getBtnDevMaxAether() {
    return btnDevMaxAether;
  }

  public JButton getBtnDevGainEchoes() {
    return btnDevGainEchoes;
  }

  public JButton getBtnDevGainSparks() {
    return btnDevGainSparks;
  }

  public AetherUpgradeCard getUpgradeCard() {
    return upgradeCard;
  }

  public Map<EchoEffect, BoonPanel> getBoonPanels() {
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
   * Card displayed in the first row of the boon grid when an Aether Upgrade is available.
   * Same visual structure as BoonPanel: name (top), description (middle), button (bottom).
   */
  static class AetherUpgradeCard extends FSkin.SkinnedPanel {
    private final FLabel lblName;
    private final FLabel lblDescription;
    private final FButton btnUpgrade;
    private final javax.swing.Icon sparkIcon;
    private boolean isHovered = false;

    AetherUpgradeCard() {
      super(new MigLayout("insets 15 15 15 15, gap 5, wrap, fill"));
      setOpaque(true);
      setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));
      addMouseListener(new MouseAdapter() {
        @Override public void mouseEntered(MouseEvent e) { isHovered = true;  repaint(); }
        @Override public void mouseExited(MouseEvent e)  { isHovered = false; repaint(); }
      });

      final javax.swing.Icon rawSparkIcon = FSkin.getImage(FSkinProp.ICO_QUEST_ELIXIR).resize(16, 16).getIcon();
      sparkIcon = new javax.swing.Icon() {
        public int getIconWidth()  { return rawSparkIcon.getIconWidth(); }
        public int getIconHeight() { return rawSparkIcon.getIconHeight(); }
        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
          rawSparkIcon.paintIcon(c, g, x, y - 2);
        }
      };

      lblName = new FLabel.Builder()
          .text("")
          .fontSize(16).fontStyle(Font.BOLD).fontAlign(SwingConstants.CENTER).build();

      lblDescription = new FLabel.Builder()
          .text("")
          .fontSize(14).fontAlign(SwingConstants.CENTER).build();

      btnUpgrade = new FButton("Upgrade");
      btnUpgrade.setIcon(sparkIcon);
      btnUpgrade.setHorizontalTextPosition(SwingConstants.LEFT);

      add(lblName, "growx, ax center");
      add(lblDescription, "growx, ax center, wmax 370px");
      add(new JPanel() {{ setOpaque(false); }}, "growy, pushy");
      add(btnUpgrade, "ax center, w 160px!, h 30px!");
    }

    void update(AetherUpgrade upgrade, int sparks) {
      lblName.setText(upgrade.name);
      lblDescription.setText(upgrade.description);
      btnUpgrade.setText("Upgrade: " + upgrade.sparkCost);
      btnUpgrade.setIcon(sparkIcon);
      btnUpgrade.setEnabled(sparks >= upgrade.sparkCost);
    }

    FButton getBtnUpgrade() { return btnUpgrade; }

    @Override
    public void paint(Graphics g) {
      super.paint(g);
      Graphics2D g2d = (Graphics2D) g;
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      if (isHovered) {
        g2d.setColor(new Color(140, 140, 140, 180));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 10, 10);
      } else {
        g2d.setColor(new Color(80, 80, 80, 120));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 10, 10);
      }
    }
  }

  /**
   * Responsive grid of BoonPanels. Fixed card size (400×150); breaks from 3 → 2 → 1 columns
   * as the viewport narrows. An optional AetherUpgradeCard occupies its own first row, centred.
   * Implements Scrollable so the enclosing FScrollPane tracks width and enables vertical
   * scrolling only — matching the pattern in VSubmenuRogueHistory.
   */
  private static class BoonGridPanel extends JPanel implements Scrollable {
    private static final int CARD_W = 400;
    private static final int CARD_H = 150;
    private static final int GAP    = 15;
    private static final int INSET  = 20;

    private final List<BoonPanel> panels;
    private final JComponent upgradeCard; // null = no first row

    BoonGridPanel(List<BoonPanel> panels, JComponent upgradeCard) {
      super(null);
      this.panels = panels;
      this.upgradeCard = upgradeCard;
      setOpaque(false);
      if (upgradeCard != null) add(upgradeCard);
      for (BoonPanel p : panels) add(p);
    }

    private int cols() {
      int avail = getWidth() - 2 * INSET;
      if (avail >= 3 * CARD_W + 2 * GAP) return 3;
      if (avail >= 2 * CARD_W +     GAP) return 2;
      return 1;
    }

    @Override
    public void doLayout() {
      int startY = INSET;

      // Row 0: upgrade card centred on its own row
      if (upgradeCard != null) {
        int cardX = Math.max(INSET, (getWidth() - CARD_W) / 2);
        upgradeCard.setBounds(cardX, startY, CARD_W, CARD_H);
        startY += CARD_H + GAP;
      }

      if (!panels.isEmpty()) {
        int cols = cols();
        int gridW = cols * CARD_W + (cols - 1) * GAP;
        int startX = Math.max(INSET, (getWidth() - gridW) / 2);
        for (int i = 0; i < panels.size(); i++) {
          int col = i % cols;
          int row = i / cols;
          panels.get(i).setBounds(startX + col * (CARD_W + GAP), startY + row * (CARD_H + GAP), CARD_W, CARD_H);
        }
        int rows = (int) Math.ceil(panels.size() / (double) cols);
        startY += rows * CARD_H + (rows - 1) * GAP;
      }

      setPreferredSize(new Dimension(getWidth(), startY + INSET));
    }

    @Override
    public Dimension getPreferredSize() {
      int w = getWidth() > 0 ? getWidth() : (getParent() != null ? getParent().getWidth() : 1300);
      int startY = INSET;
      if (upgradeCard != null) startY += CARD_H + GAP;
      if (!panels.isEmpty()) {
        int avail = w - 2 * INSET;
        int cols = avail >= 3 * CARD_W + 2 * GAP ? 3 : avail >= 2 * CARD_W + GAP ? 2 : 1;
        int rows = (int) Math.ceil(panels.size() / (double) cols);
        startY += rows * CARD_H + (rows - 1) * GAP;
      }
      return new Dimension(w, startY + INSET);
    }

    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
    @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 64; }
    @Override public boolean getScrollableTracksViewportWidth()  { return true;  }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }
  }

  /**
   * Inner class representing a single boon panel in the grid. Click the panel to toggle active
   * state (when unlocked). Shows green border and "ACTIVE" badge when active. Shows yellow/gold
   * border on hover.
   */
  public static class BoonPanel extends FSkin.SkinnedPanel {

    private final EchoEffect boon;
    private final FLabel lblName;
    private final FLabel lblDescription;
    private final FLabel lblRank;
    private final FButton btnUpgrade;
    // Cache own icon instance to avoid shared state issues (similar to NodePlaneboundPanel pattern)
    private final javax.swing.Icon cachedEchoIcon;

    // Visual state
    private boolean isActive = false;
    private boolean isHovered = false;
    private boolean canToggle = false;
    private int currentRank = 0;

    // Click callback for toggling active state
    private Consumer<BoonPanel> toggleCallback;

    public BoonPanel(EchoEffect boon) {
      super(new MigLayout("insets 15 15 15 15, gap 5, wrap, fill"));
      this.boon = boon;

      // Create and cache own icon instance at construction time
      final javax.swing.Icon rawEchoIcon = FSkin.getImage(FSkinProp.ICO_QUEST_GOLD).resize(20, 20).getIcon();
      cachedEchoIcon = new javax.swing.Icon() {
        public int getIconWidth()  { return rawEchoIcon.getIconWidth(); }
        public int getIconHeight() { return rawEchoIcon.getIconHeight(); }
        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
          rawEchoIcon.paintIcon(c, g, x, y - 2);
        }
      };

      setOpaque(true);
      setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));

      lblName = new FLabel.Builder()
          .text(boon.getDisplayName())
          .fontSize(16)
          .fontStyle(Font.BOLD)
          .fontAlign(SwingConstants.CENTER)
          .build();

      lblDescription = new FLabel.Builder()
          .text("")
          .fontSize(14)
          .fontAlign(SwingConstants.CENTER)
          .build();

      lblRank = new FLabel.Builder()
          .text("Rank: 0/" + boon.getMaxRank())
          .fontSize(12)
          .fontAlign(SwingConstants.CENTER)
          .build();

      btnUpgrade = new FButton("Unlock");
      btnUpgrade.setIcon(cachedEchoIcon);
      btnUpgrade.setHorizontalTextPosition(SwingConstants.LEFT);

      add(lblName, "growx, ax center");
      add(lblDescription, "growx, ax center, wmax 370px");
      add(lblRank, "growx, ax center");

      // Spacer to push controls to bottom
      add(new JPanel() {{
        setOpaque(false);
      }}, "growy, pushy");

      // Upgrade button centered at bottom
      add(btnUpgrade, "ax center, w 160px!, h 30px!");

      // Add mouse listener for hover and click
      addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          // Only toggle if clicking outside the upgrade button and can toggle
          if (canToggle && toggleCallback != null) {
            toggleCallback.accept(BoonPanel.this);
          }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
          isHovered = true;
          if (canToggle) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
          }
          repaint();
        }

        @Override
        public void mouseExited(MouseEvent e) {
          isHovered = false;
          setCursor(Cursor.getDefaultCursor());
          repaint();
        }
      });
    }

    /**
     * Set the callback for when the panel is clicked to toggle active state.
     */
    public void setToggleCallback(Consumer<BoonPanel> callback) {
      this.toggleCallback = callback;
    }

    /**
     * Update the panel display based on current boon state.
     */
    public void update(int rank, boolean active, int echoes, int activeBoonCount, int upgradeLevel) {
      this.currentRank = rank;
      this.isActive = active;
      int effectiveMax = boon.getEffectiveMaxRank(upgradeLevel);
      // Compute actual slot count
      int boonSlots = 3;
      for (int l = 1; l <= upgradeLevel; l++) {
        AetherUpgrade u = AetherUpgrade.forLevel(l);
        if (u != null) boonSlots += u.extraBoonSlots;
      }
      this.canToggle = rank > 0 && (active || activeBoonCount < boonSlots);

      lblRank.setText("Rank: " + rank + "/" + effectiveMax);

      // Update description to show all rank values with current rank highlighted
      lblDescription.setText(boon.getDescriptionWithAllRanks(rank, upgradeLevel));

      // Update upgrade button using cached icon instance
      if (rank >= effectiveMax) {
        btnUpgrade.setText("Max Rank");
        btnUpgrade.setEnabled(false);
      } else {
        int cost = boon.getEchoCostForRank(rank + 1);
        btnUpgrade.setText(rank == 0 ? "Unlock: " + cost : "Upgrade: " + cost);
        btnUpgrade.setIcon(cachedEchoIcon);
        btnUpgrade.setEnabled(echoes >= cost);
      }

      // Update cursor based on toggle ability
      if (isHovered && canToggle) {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      } else {
        setCursor(Cursor.getDefaultCursor());
      }

      repaint();
    }

    @Override
    public void paint(Graphics g) {
      super.paint(g);

      Graphics2D g2d = (Graphics2D) g;
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      int width = getWidth();
      int height = getHeight();

      // Draw border based on state
      if (isHovered && canToggle) {
        // Hovered (and can toggle): yellow/gold border - takes priority to show clickability
        g2d.setColor(new Color(255, 215, 0));
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRoundRect(2, 2, width - 4, height - 4, 10, 10);
      } else if (isActive) {
        // Active (not hovered): thick green border
        g2d.setColor(new Color(0, 255, 0, 200));
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRoundRect(2, 2, width - 4, height - 4, 10, 10);
      } else if (currentRank > 0) {
        // Unlocked but not active: subtle border
        g2d.setColor(new Color(100, 100, 100, 150));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(2, 2, width - 4, height - 4, 10, 10);
      } else if (isHovered) {
        // Locked + hovered: slightly brighter grey border
        g2d.setColor(new Color(140, 140, 140, 180));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(2, 2, width - 4, height - 4, 10, 10);
      } else {
        // Locked: thin grey border
        g2d.setColor(new Color(80, 80, 80, 120));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(1, 1, width - 2, height - 2, 10, 10);
      }

      // Draw "ACTIVE" badge in top-right corner (always shown when active)
      if (isActive) {
        int badgeWidth = 60;
        int badgeHeight = 20;
        int badgeX = width - badgeWidth - 8;
        int badgeY = 8;

        // Badge background
        g2d.setColor(new Color(0, 200, 0, 230));
        g2d.fillRoundRect(badgeX, badgeY, badgeWidth, badgeHeight, 8, 8);

        // Badge text
        g2d.setColor(Color.WHITE);
        g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 11f));
        g2d.drawString("ACTIVE", badgeX + 8, badgeY + 14);
      }
    }

    public EchoEffect getBoon() {
      return boon;
    }

    public FButton getBtnUpgrade() {
      return btnUpgrade;
    }

    public boolean isActive() {
      return isActive;
    }
  }
}
