package forge.screens.home.rogue;

import forge.gamemodes.rogue.BoonType;
import forge.gamemodes.rogue.RogueMetaProgress;
import forge.gui.framework.EDocID;
import forge.gui.framework.ICDoc;
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

        // Setup listeners for each boon panel
        for (Map.Entry<BoonType, VSubmenuRogueAether.BoonPanel> entry : view.getBoonPanels().entrySet()) {
            BoonType type = entry.getKey();
            VSubmenuRogueAether.BoonPanel panel = entry.getValue();

            panel.getBtnUpgrade().addActionListener(e -> upgradeBoon(type));
            panel.getChkActive().addActionListener(e -> toggleBoonActive(type, panel.getChkActive().isSelected()));
        }
    }

    @Override
    public void update() {
        refreshDisplay();
    }

    private void refreshDisplay() {
        RogueMetaProgress progress = RogueMetaProgress.getInstance();

        Map<BoonType, Integer> boonRanks = new EnumMap<>(BoonType.class);
        for (BoonType type : BoonType.values()) {
            boonRanks.put(type, progress.getBoonRank(type));
        }

        Set<BoonType> activeBoons = progress.getActiveBoons();

        view.updateDisplay(
            progress.getTotalEchoes(),
            progress.getActiveBoonCount(),
            boonRanks,
            activeBoons
        );
    }

    private void upgradeBoon(BoonType type) {
        RogueMetaProgress progress = RogueMetaProgress.getInstance();
        if (progress.upgradeBoon(type)) {
            refreshDisplay();
        }
    }

    private void toggleBoonActive(BoonType type, boolean active) {
        RogueMetaProgress progress = RogueMetaProgress.getInstance();
        if (active) {
            progress.activateBoon(type);
        } else {
            progress.deactivateBoon(type);
        }
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
