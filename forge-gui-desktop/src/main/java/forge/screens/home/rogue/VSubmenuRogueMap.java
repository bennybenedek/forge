package forge.screens.home.rogue;

import forge.gamemodes.rogue.EchoBoon;
import forge.gamemodes.rogue.RogueMetaProgress;
import forge.gamemodes.rogue.RogueRun;
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
import forge.util.Localizer;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
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
      .text("♥ Life: 20")
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

  private final FButton btnEnterNode;
  private final FButton btnEditDeck;
  private final FButton btnDevWinRun = new FButton("[DEV] Win Run");

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
      lblCommanderName.setText(run.getSelectedRogueDeck().getCommanderCardName());
      lblCommanderAvatar.setIcon(FSkin.getAvatars().get(run.getSelectedRogueDeck().getAvatarIndex()));
      lblLife.setText("♥ Life: " + run.getCurrentLife());
      lblGold.setText("Gold: " + run.getCurrentGold());
      lblRemovalCredits.setText("Removal Credits: " + run.getRemovalCredits());
      int descLevel = run.getDescensionLevel();
      lblDescension.setVisible(descLevel > 0);
      if (descLevel > 0) lblDescension.setText("Descension: " + descLevel);
      pathVisualizer.updatePath(run);
    } else {
      lblCommanderName.setText("");
      lblCommanderAvatar.setIcon(FSkin.getIcon(FSkinProp.ICO_MINUS));
      lblLife.setText("♥ Life: 20");
      lblGold.setText("Gold: 0");
      lblRemovalCredits.setText("Removal Credits: 0");
      lblDescension.setVisible(false);
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

    // Active Aether Boons: 2 columns x max 3 rows, fill column 1 first
    RogueMetaProgress progress = RogueMetaProgress.getInstance();
    List<EchoBoon> activeBoons = new ArrayList<>();
    for (EchoBoon t : EchoBoon.values()) {
      if (progress.isBoonActive(t)) activeBoons.add(t);
    }
    if (!activeBoons.isEmpty()) {
      JPanel pnlBoons = new JPanel(new MigLayout("insets 0, gap 8 1"));
      pnlBoons.setOpaque(false);
      for (int i = 0; i < activeBoons.size(); i++) {
        int col = i / 3;
        int row = i % 3;
        FLabel boonLbl = new FLabel.Builder().text(activeBoons.get(i).getDisplayName()).fontSize(11).build();
        boonLbl.setToolTipText(activeBoons.get(i).getDescription());
        pnlBoons.add(boonLbl, "cell " + col + " " + row);
      }
      infoRow.add(pnlBoons);
    }

    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(infoRow, "w 98%!, gap 1% 0 10px 10px");
    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay()
        .add(scrollPathDisplay, "w 96%!, gap 2% 2% 0 0, pushy, growy");
    int split = ForgePreferences.DEV_MODE ? 3 : 2;
    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay()
        .add(btnEnterNode, "w 30%!, h 40px!, ax center, gap 0 2% 10px 20px, split " + split);
    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(btnEditDeck, "w 30%!, h 40px!");
    if (ForgePreferences.DEV_MODE) {
      VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(btnDevWinRun, "w 30%!, h 40px!");
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

}
