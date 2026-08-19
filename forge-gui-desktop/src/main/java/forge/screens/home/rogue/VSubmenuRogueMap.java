package forge.screens.home.rogue;

import forge.gamemodes.rogue.RogueMetaProgress;
import forge.gamemodes.rogue.RogueRun;
import forge.gamemodes.rogue.RogueRun.CarryCard;
import forge.gamemodes.rogue.effect.DescensionLevel;
import forge.gamemodes.rogue.effect.RogueEffect;
import forge.gamemodes.rogue.effect.RogueEffectComposite;
import forge.gui.CardPicturePanel;
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
import forge.item.PaperCard;
import forge.util.Localizer;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;

/**
 * Assembles Swing components of "rogue map" submenu singleton.
 *
 * <br><br><i>(V at beginning of class name denotes a view class.)</i>
 */
public enum VSubmenuRogueMap implements IVSubmenu<CSubmenuRogueMap> {
  SINGLETON_INSTANCE;

  final Localizer localizer = Localizer.getInstance();
  private DragCell parentCell;
  private final DragTab tab = new DragTab("Rogue Commander");

  private final FLabel lblTitle = new FLabel.Builder()
      .text("Rogue Commander - Map")
      .fontAlign(SwingConstants.CENTER)
      .opaque(true)
      .fontSize(16)
      .build();

  private final FLabel lblCommanderAvatar = new FLabel.Builder()
      .iconScaleFactor(0.99f).iconInBackground(true).build();

  private final FLabel lblCommanderName = new FLabel.Builder()
      .text("")
      .fontSize(14)
      .fontStyle(Font.BOLD)
      .build();

  private final FLabel lblLife = new FLabel.Builder()
      .text("\u2665 Life: 20 / 20")
      .fontSize(14)
      .fontStyle(Font.BOLD)
      .build();

  private final FLabel lblGold = new FLabel.Builder()
      .text("Gold: 0")
      .fontSize(14)
      .fontStyle(Font.BOLD)
      .build();

  private final FLabel lblEchoes = new FLabel.Builder()
      .text("Echoes: 0")
      .fontSize(14)
      .fontStyle(Font.BOLD)
      .build();

  private final FLabel lblRemovalCredits = new FLabel.Builder()
      .text("Removal Credits: 0")
      .fontSize(14)
      .fontStyle(Font.BOLD)
      .build();

  private final FLabel lblDescension = new FLabel.Builder()
      .text("")
      .fontSize(14)
      .fontStyle(Font.BOLD)
      .build();

  private final PathVisualizerPanel pathVisualizer = new PathVisualizerPanel();
  private final FScrollPane scrollPathDisplay;

  private JPanel pnlEffects;
  private JPanel pnlCarryCards;
  private final FButton btnEnterNode;
  private final FButton btnEditDeck;
  private final FButton btnDevWinRun = new FButton("[DEV] Win Run");
  private final FButton btnDevNextNode = new FButton("[DEV] Next Node");
  private CardUtil zoomUtil;

  VSubmenuRogueMap() {
    lblTitle.setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));

    // Add icons to labels
    lblGold.setIcon(FSkin.getIcon(FSkinProp.ICO_QUEST_COIN));
    lblEchoes.setIcon(FSkin.getIcon(FSkinProp.ICO_QUEST_GOLD));
    lblRemovalCredits.setIcon(FSkin.getIcon(FSkinProp.ICO_CARD_IMAGE));
    lblDescension.setIcon(FSkin.getIcon(FSkinProp.ICO_QUEST_ZEP));
    lblDescension.setVisible(false);

    // Setup buttons with icons
    btnEditDeck = new FButton("Edit Rogue Deck");
    btnEditDeck.setIcon(FSkin.getIcon(FSkinProp.ICO_CARD_IMAGE));

    btnEnterNode = new FButton("Enter Node");
    btnEnterNode.setIcon(FSkin.getIcon(FSkinProp.ICO_OPEN));

    // Setup scroll pane for path visualizer
    scrollPathDisplay = new FScrollPane(pathVisualizer, true);
    scrollPathDisplay.setOpaque(false);
  }

  /**
   * Update the display with current run data.
   */
  public void updateDisplay(RogueRun run) {
    // Echoes come from meta progression (persistent), not from the run
    int totalEchoes = RogueMetaProgress.getInstance().getTotalEchoes();
    lblEchoes.setText("Echoes: " + totalEchoes);

    if (run != null) {
      lblCommanderName.setText(run.getCurrentCommanderName());
      lblCommanderAvatar.setIcon(FSkin.getAvatars().get(run.getSelectedRogueDeck().getAvatarIndex()));
      lblLife.setText("\u2665 Life: " + run.getCurrentLife() + " / " + run.getMaxLife());
      lblGold.setText("Gold: " + run.getCurrentGold());
      lblRemovalCredits.setText("Removal Credits: " + run.getRemovalCredits());
      int descLevel = run.getDescensionLevel();
      lblDescension.setVisible(descLevel > 0);
      if (descLevel > 0) {
        lblDescension.setText("Descension: " + descLevel);
        StringBuilder tooltip = new StringBuilder("<html>");
        for (int level = 1; level <= descLevel; level++) {
          DescensionLevel descension = DescensionLevel.forLevel(level);
          if (descension == null) {
            continue;
          }
          tooltip.append("* Level ").append(level)
              .append(" - ").append(descension.name)
              .append(": ").append(descension.description)
              .append("<br>");
        }
        tooltip.append("</html>");
        lblDescension.setToolTipText(tooltip.toString());
      } else {
        lblDescension.setToolTipText(null);
      }

      // Populate active effects (echo boons, descension, event traits, chest traits, wounds...)
      List<RogueEffect> allEffects = RogueEffectComposite.getAllEffects(run);
      allEffects.removeIf(effect -> effect instanceof DescensionLevel);
      RogueUIHelper.populateEffectPanel(pnlEffects, allEffects, run);
      populateCarryCardPanel(run.getCarryCards());

      pathVisualizer.updatePath(run);
    } else {
      lblCommanderName.setText("");
      lblCommanderAvatar.setIcon(FSkin.getIcon(FSkinProp.ICO_MINUS));
      lblLife.setText("\u2665 Life: 20 / 20");
      lblGold.setText("Gold: 0");
      lblRemovalCredits.setText("Removal Credits: 0");
      lblDescension.setVisible(false);
      lblDescension.setToolTipText(null);
      populateCarryCardPanel(List.of());
      pathVisualizer.clearPath();
    }
  }

  @Override
  public EMenuGroup getGroupEnum() {
    return EMenuGroup.ROGUE;
  }

  @Override
  public String getMenuTitle() {
    return "Continue Run";
  }

  @Override
  public EDocID getItemEnum() {
    return EDocID.HOME_ROGUEMAP;
  }

  @Override
  public void populate() {
    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().removeAll();
    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().setLayout(new MigLayout("insets 0, gap 0, wrap"));

    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(lblTitle, "w 98%!, h 30px!, gap 1% 0 15px 15px");

    // Info row — responsive: wraps when window narrows
    JPanel infoRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 3));
    infoRow.setOpaque(false);
    lblCommanderAvatar.setPreferredSize(new Dimension(45, 45));
    infoRow.add(lblCommanderAvatar);
    infoRow.add(lblCommanderName);
    infoRow.add(lblLife);
    infoRow.add(lblGold);
    infoRow.add(lblEchoes);
    infoRow.add(lblRemovalCredits);
    infoRow.add(lblDescension);

    // Active effects panel (echo boons, descension, event traits, chest traits, wounds)
    pnlEffects = RogueUIHelper.createEffectPanel();
    infoRow.add(pnlEffects);
    pnlCarryCards = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
    pnlCarryCards.setOpaque(false);
    infoRow.add(pnlCarryCards);

    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(infoRow, "w 98%!, h pref!, gap 1% 0 10px 10px");
    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay()
        .add(scrollPathDisplay, "w 96%!, gap 2% 2% 0 0, pushy, growy");
    int split = ForgePreferences.DEV_MODE ? 4 : 2;
    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay()
        .add(btnEnterNode, "w 30%!, h 40px!, ax center, gap 0 2% 10px 20px, split " + split);
    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(btnEditDeck, "w 30%!, h 40px!");
    if (ForgePreferences.DEV_MODE) {
      VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(btnDevNextNode, "w 15%!, h 40px!");
      VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(btnDevWinRun, "w 15%!, h 40px!");
    }

    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().repaintSelf();
    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().revalidate();
  }

  public JButton getBtnEnterNode() {
    return btnEnterNode;
  }

  public JButton getBtnEditDeck() {
    return btnEditDeck;
  }

  public JButton getBtnDevWinRun() {
    return btnDevWinRun;
  }

  public JButton getBtnDevNextNode() {
    return btnDevNextNode;
  }

  public PathVisualizerPanel getPathVisualizer() {
    return pathVisualizer;
  }

  @Override
  public EDocID getDocumentID() {
    return EDocID.HOME_ROGUEMAP;
  }

  @Override
  public DragTab getTabLabel() {
    return tab;
  }

  @Override
  public CSubmenuRogueMap getLayoutControl() {
    return CSubmenuRogueMap.SINGLETON_INSTANCE;
  }

  @Override
  public void setParentCell(DragCell cell0) {
    this.parentCell = cell0;
  }

  @Override
  public DragCell getParentCell() {
    return parentCell;
  }

  private void populateCarryCardPanel(List<CarryCard> carryCards) {
    pnlCarryCards.removeAll();

    for (CarryCard carryCard : carryCards) {
      PaperCard paperCard = carryCard.toPaperCard();
      if (paperCard == null) {
        continue;
      }
      pnlCarryCards.add(createCarryCardThumbnail(carryCard, paperCard));
    }

    pnlCarryCards.revalidate();
    pnlCarryCards.repaint();
  }

  private JPanel createCarryCardThumbnail(CarryCard carryCard, PaperCard paperCard) {
    int thumbnailWidth = 32;
    int thumbnailHeight = 45;

    CardPicturePanel picturePanel = new CardPicturePanel();
    picturePanel.setOpaque(false);
    picturePanel.setItem(paperCard);
    picturePanel.setPreferredSize(new Dimension(thumbnailWidth, thumbnailHeight));
    picturePanel.setMinimumSize(new Dimension(thumbnailWidth, thumbnailHeight));
    picturePanel.setMaximumSize(new Dimension(thumbnailWidth, thumbnailHeight));
    picturePanel.setToolTipText(buildCarryCardTooltip(carryCard, paperCard));
    picturePanel.addMouseWheelListener(e -> {
      if (e.getWheelRotation() < 0 && getZoomUtil() != null) {
        getZoomUtil().showZoom(paperCard);
      }
    });

    JPanel thumbnailPanel = new JPanel(new MigLayout("insets 0, gap 0"));
    thumbnailPanel.setOpaque(false);
    thumbnailPanel.setPreferredSize(new Dimension(thumbnailWidth, thumbnailHeight));
    thumbnailPanel.setMinimumSize(new Dimension(thumbnailWidth, thumbnailHeight));
    thumbnailPanel.setMaximumSize(new Dimension(thumbnailWidth, thumbnailHeight));
    thumbnailPanel.setToolTipText(buildCarryCardTooltip(carryCard, paperCard));
    thumbnailPanel.add(picturePanel, "w " + thumbnailWidth + "!, h " + thumbnailHeight + "!");
    thumbnailPanel.addMouseWheelListener(e -> {
      if (e.getWheelRotation() < 0 && getZoomUtil() != null) {
        getZoomUtil().showZoom(paperCard);
      }
    });
    return thumbnailPanel;
  }

  private String buildCarryCardTooltip(CarryCard carryCard, PaperCard paperCard) {
    return paperCard.getName() + " (" + switch (carryCard.type()) {
      case ITEM -> "Item";
      case FELLOW -> "Fellow";
      case SCROLL -> "Scroll";
    } + ")";
  }

  private CardUtil getZoomUtil() {
    if (zoomUtil == null && pnlCarryCards != null) {
      Window window = SwingUtilities.getWindowAncestor(pnlCarryCards);
      if (window != null) {
        zoomUtil = new CardUtil(window);
        zoomUtil.setupZoomOverlay();
      }
    }
    return zoomUtil;
  }

}
