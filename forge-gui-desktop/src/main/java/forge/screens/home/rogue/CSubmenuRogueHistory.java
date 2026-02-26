package forge.screens.home.rogue;

import forge.deckchooser.FDeckViewer;
import forge.gamemodes.rogue.RogueMetaProgress;
import forge.gui.framework.EDocID;
import forge.gui.framework.ICDoc;
import forge.screens.home.CHomeUI;

/**
 * Controller for the Rogue Commander run history screen.
 */
public enum CSubmenuRogueHistory implements ICDoc {
  SINGLETON_INSTANCE;

  private final VSubmenuRogueHistory view = VSubmenuRogueHistory.SINGLETON_INSTANCE;
  private boolean initialized = false;

  @Override
  public void register() {
  }

  @Override
  public void initialize() {
    if (initialized) {
      return;
    }
    initialized = true;
    view.getBtnBack().addActionListener(e -> goBack());
  }

  @Override
  public void update() {
    view.displayHistory(
        RogueMetaProgress.getInstance().getRunHistory(),
        entry -> FDeckViewer.show(entry.getDeckSnapshot())
    );
  }

  private void goBack() {
    CHomeUI.SINGLETON_INSTANCE.itemClick(EDocID.HOME_ROGUESTART);
  }
}
