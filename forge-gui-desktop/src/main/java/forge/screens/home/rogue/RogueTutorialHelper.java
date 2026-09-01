package forge.screens.home.rogue;

import forge.gamemodes.rogue.RogueMetaProgress;
import forge.gamemodes.rogue.RogueTutorial;
import forge.gamemodes.rogue.effect.ChoiceRerollContext;
import forge.gamemodes.rogue.npc.NPC;
import forge.gamemodes.rogue.npc.NPCContext;
import java.util.List;

/**
 * Helper class for showing Rogue Commander tutorials. Centralizes the logic for checking and
 * displaying tutorials.
 */
public class RogueTutorialHelper {

  private RogueTutorialHelper() {
  }

  /**
   * Show the specified tutorials (always shown, marks as seen). Tutorials are shown in the order
   * provided. Marks as seen BEFORE showing dialog to prevent duplicate popups from concurrent
   * calls.
   */
  public static void show(RogueTutorial... tutorials) {
    RogueMetaProgress progress = RogueMetaProgress.getInstance();
    for (RogueTutorial tutorial : tutorials) {
      progress.markTutorialSeen(tutorial);
      showTutorialDialog(tutorial);
    }
  }

  /**
   * Show the specified tutorials if they haven't been seen yet. Tutorials are shown in the order
   * provided. Marks as seen BEFORE showing dialog to prevent duplicate popups from concurrent
   * calls.
   */
  public static void showIfNotSeen(RogueTutorial... tutorials) {
    RogueMetaProgress progress = RogueMetaProgress.getInstance();
    for (RogueTutorial tutorial : tutorials) {
      if (!hasSeenTutorial(tutorial)) {
        progress.markTutorialSeen(tutorial);
        showTutorialDialog(tutorial);
      }
    }
  }

  public static boolean hasSeenTutorial(RogueTutorial tutorial) {
    RogueMetaProgress progress = RogueMetaProgress.getInstance();
    return progress.hasSeenTutorial(tutorial);
  }

  private static void showTutorialDialog(RogueTutorial tutorial) {
    NPCContext context = new NPCContext(
        NPC.TEFERI,
        tutorial.getMessageChunks(),
        List.of(new NPCContext.NPCChoice("Continue", null)),
        null,
        null);
    new NPCDialog(context, new ChoiceRerollContext()).show();
  }
}
