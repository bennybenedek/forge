package forge.screens.home.rogue;

import forge.gamemodes.rogue.RogueMetaProgress;
import forge.gamemodes.rogue.RogueRun;
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
import forge.util.Localizer;
import java.awt.Font;
import javax.swing.JButton;
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

  private final PathVisualizerPanel pathVisualizer = new PathVisualizerPanel();
  private final FScrollPane scrollPathDisplay;

  private final FButton btnEnterNode;
  private final FButton btnEditDeck;

  VSubmenuRogueMap() {
    lblTitle.setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));

    // Add icons to labels
    lblGold.setIcon(FSkin.getIcon(FSkinProp.ICO_QUEST_COIN));
    lblEchoes.setIcon(FSkin.getIcon(FSkinProp.ICO_QUEST_GOLD));
    lblRemovalCredits.setIcon(FSkin.getIcon(FSkinProp.ICO_CARD_IMAGE));

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
      lblLife.setText("♥ Life: " + run.getCurrentLife());
      lblGold.setText("Gold: " + run.getCurrentGold());
      lblRemovalCredits.setText("Removal Credits: " + run.getRemovalCredits());
      pathVisualizer.updatePath(run);
    } else {
      lblLife.setText("♥ Life: 20");
      lblGold.setText("Gold: 0");
      lblRemovalCredits.setText("Removal Credits: 0");
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
    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay()
        .add(lblLife, "ax center, gap 0 0 10px 10px, split 4");
    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(lblGold, "gapleft 20px");
    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(lblEchoes, "gapleft 20px");
    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(lblRemovalCredits, "gapleft 20px");
    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay()
        .add(scrollPathDisplay, "w 96%!, gap 2% 2% 0 0, pushy, growy");
    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay()
        .add(btnEnterNode, "w 30%!, h 40px!, ax center, gap 0 2% 10px 20px, split 2");
    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(btnEditDeck, "w 30%!, h 40px!");

    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().repaintSelf();
    VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().revalidate();
  }

  public JButton getBtnEnterNode() {
    return btnEnterNode;
  }

  public JButton getBtnEditDeck() {
    return btnEditDeck;
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
