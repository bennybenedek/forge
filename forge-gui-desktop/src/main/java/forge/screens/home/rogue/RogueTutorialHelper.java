package forge.screens.home.rogue;

import forge.gamemodes.rogue.RogueMetaProgress;
import forge.gamemodes.rogue.RogueTutorial;
import forge.toolbox.FOptionPane;

/**
 * Helper class for showing Rogue Commander tutorials.
 * Centralizes the logic for checking and displaying tutorials.
 */
public class RogueTutorialHelper {

    private RogueTutorialHelper() { }

    /**
     * Show the specified tutorials (always shown, marks as seen).
     * Tutorials are shown in the order provided.
     * Marks as seen BEFORE showing dialog to prevent duplicate popups from concurrent calls.
     */
    public static void show(RogueTutorial... tutorials) {
        RogueMetaProgress progress = RogueMetaProgress.getInstance();
        for (RogueTutorial tutorial : tutorials) {
            progress.markTutorialSeen(tutorial);
            FOptionPane.showMessageDialog(tutorial.getMessage(), tutorial.getTitle());
        }
    }

    /**
     * Show the specified tutorials if they haven't been seen yet.
     * Tutorials are shown in the order provided.
     * Marks as seen BEFORE showing dialog to prevent duplicate popups from concurrent calls.
     */
    public static void showIfNotSeen(RogueTutorial... tutorials) {
        RogueMetaProgress progress = RogueMetaProgress.getInstance();
        for (RogueTutorial tutorial : tutorials) {
            if (!hasSeenTutorial(tutorial)) {
                progress.markTutorialSeen(tutorial);
                FOptionPane.showMessageDialog(tutorial.getMessage(), tutorial.getTitle());
            }
        }
    }

    public static boolean hasSeenTutorial(RogueTutorial tutorial) {
        RogueMetaProgress progress = RogueMetaProgress.getInstance();
        return progress.hasSeenTutorial(tutorial);
    }
}
