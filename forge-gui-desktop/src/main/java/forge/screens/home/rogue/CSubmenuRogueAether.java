package forge.screens.home.rogue;

import forge.gamemodes.rogue.AetherUpgrade;
import forge.gamemodes.rogue.RogueMetaProgress;
import forge.gamemodes.rogue.RogueTutorial;
import forge.gamemodes.rogue.effect.EchoEffect;
import forge.gui.framework.EDocID;
import forge.gui.framework.ICDoc;
import forge.localinstance.properties.ForgePreferences;
import forge.screens.home.CHomeUI;
import forge.toolbox.FOptionPane;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Controls the Aether screen for managing permanent upgrades (Boons).
 */
public enum CSubmenuRogueAether implements ICDoc {
  SINGLETON_INSTANCE;

  private final VSubmenuRogueAether view = VSubmenuRogueAether.SINGLETON_INSTANCE;
  private boolean initialized = false;

  @Override
  public void register() {
  }

  @Override
  public void initialize() {
    // Guard against multiple initialization (would cause duplicate listeners)
    if (initialized) {
      return;
    }
    initialized = true;

    view.getBtnBack().addActionListener(e -> goBack());
    view.getBtnResetBoons().addActionListener(e -> confirmResetBoons());
    view.getUpgradeCard().getBtnUpgrade().addActionListener(e -> purchaseNextUpgrade());

    if (ForgePreferences.DEV_MODE) {
      view.getBtnDevMaxAether().addActionListener(e -> devMaxAether());
      view.getBtnDevGainEchoes().addActionListener(e -> {
        RogueMetaProgress.getInstance().addEchoes(10);
        refreshDisplay();
      });
      view.getBtnDevGainSparks().addActionListener(e -> {
        RogueMetaProgress.getInstance().addSparks(10);
        refreshDisplay();
      });
    }

    // Setup listeners for each boon panel
    for (Map.Entry<EchoEffect, VSubmenuRogueAether.BoonPanel> entry : view.getBoonPanels()
        .entrySet()) {
      EchoEffect type = entry.getKey();
      VSubmenuRogueAether.BoonPanel panel = entry.getValue();

      panel.getBtnUpgrade().addActionListener(e -> upgradeBoon(type));
      // Panel click toggles active state (when unlocked)
      panel.setToggleCallback(p -> toggleBoonActive(p.getBoon(), !p.isActive()));
    }
  }

  @Override
  public void update() {
    refreshDisplay();
    RogueTutorialHelper.showIfNotSeen(RogueTutorial.AETHER);
  }

  private void refreshDisplay() {
    RogueMetaProgress progress = RogueMetaProgress.getInstance();

    Map<EchoEffect, Integer> boonRanks = new EnumMap<>(EchoEffect.class);
    for (EchoEffect type : EchoEffect.values()) {
      boonRanks.put(type, progress.getBoonRank(type));
    }

    Set<EchoEffect> activeBoons = progress.getActiveEchoBoons();

    view.updateDisplay(
        progress.getTotalEchoes(),
        progress.getTotalSparks(),
        progress.isDescensionModeUnlocked(),
        progress.getActiveBoonCount(),
        progress.getAetherUpgradeLevel(),
        boonRanks,
        activeBoons
    );
  }

  private void purchaseNextUpgrade() {
    RogueMetaProgress progress = RogueMetaProgress.getInstance();
    int nextLevel = progress.getAetherUpgradeLevel() + 1;
    if (progress.purchaseAetherUpgrade(nextLevel)) {
      // Re-populate to show newly unlocked boons (boon grid rebuilds on populate)
      view.populate();
      refreshDisplay();
      AetherUpgrade u = AetherUpgrade.forLevel(nextLevel);
      if (u != null) {
        FOptionPane.showMessageDialog(u.name + " unlocked!\n" + u.description, "Aether Upgrade Unlocked");
      }
    }
  }

  private void upgradeBoon(EchoEffect type) {
    RogueMetaProgress progress = RogueMetaProgress.getInstance();
    int rankBefore = progress.getBoonRank(type);
    if (progress.upgradeBoon(type)) {
      // Auto-activate boon when first unlocked (rank goes from 0 to 1)
      if (rankBefore == 0 && progress.getBoonRank(type) == 1) {
        progress.activateBoon(type);
      }
      refreshDisplay();
    }
  }

  private void toggleBoonActive(EchoEffect type, boolean active) {
    RogueMetaProgress progress = RogueMetaProgress.getInstance();
    if (active) {
      progress.activateBoon(type);
    } else {
      progress.deactivateBoon(type);
    }
    refreshDisplay();
  }

  private void devMaxAether() {
    RogueMetaProgress progress = RogueMetaProgress.getInstance();
    int maxLevel = AetherUpgrade.getMaxLevel();
    boolean isMaxed = progress.getAetherUpgradeLevel() >= maxLevel;
    progress.setAetherUpgradeLevel(isMaxed ? 0 : maxLevel);
    view.getBtnDevMaxAether().setText(isMaxed ? "[Dev] Max Aether" : "[Dev] Reset Aether");
    view.populate();
    refreshDisplay();
  }

  private void goBack() {
    CHomeUI.SINGLETON_INSTANCE.itemClick(EDocID.HOME_ROGUESTART);
  }

  private void confirmResetBoons() {
    boolean confirmed = FOptionPane.showConfirmDialog(
        "Are you sure you want to reset all Boons?\nAll spent Echoes will be refunded.",
        "Reset Boons",
        "Reset",
        "Cancel",
        false
    );

    if (confirmed) {
      int refunded = RogueMetaProgress.getInstance().resetBoons();
      refreshDisplay();
      if (refunded > 0) {
        FOptionPane.showMessageDialog("Refunded " + refunded + " Echoes.", "Boons Reset");
      }
    }
  }
}
