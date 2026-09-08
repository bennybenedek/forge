package forge.gamemodes.rogue;

import java.util.List;

/**
 * Defines tutorials shown to new players in Rogue Commander mode. Each tutorial is shown once and
 * then marked as seen in RogueMetaProgress.
 */
public enum RogueTutorial {

  WELCOME(
      "Welcome to Rogue Commander",
      "Rogue Commander is a roguelite deckbuilding format. Start with a commander and an open-ended deck, "
          +
          "then embark on a Run, battling against 'Planebounds' to earn gold and new cards.",
      "Win matches to progress until the final Boss, but be careful - lost life is not restored after each battle!"
  ),

  COMMANDER_SELECTION(
      "Commander Selection",
      "Choose your Rogue commander wisely! Each commander comes with a unique starting deck " +
          "built around their strategies and archetypes, as well as a 'Reward Pool' you choose your card rewards from to improve your deck.",
      "You can unlock new commanders by winning Runs or through other achievements. "
  ),

  MAP_NAVIGATION(
      "Map Navigation",
      "Before you lies the Omenpath. The map shows your path through the Run (starting from top to bottom). Each Run will be a different path, with different opponents, encounters and locations.",
      "Choose your route carefully, some might be harder than others. Each Path is divided into rows, alternating between ones where you will battle an opponent on a Plane-node, and ones that offer healing, bazaars, and other encounters via Side-nodes.",
      "Whenever you complete a node, you unlock the next row of your path. If there is more than one accessible node in that row, you can select your next destination.\n"
          +
          "The more Plane rows you complete, the more life your opponents will start with."
  ),

  PRE_BATTLE(
      "Planebound Battles",
      "Battles on Plane-Nodes are played as a match of 'Commander' (you against the Planebound) with added 'Planechase' rules. So don't forget to keep an eye on your command zone for casting your Commander and rolling the Planar Die. Other than in a normal Planechase Match,"
          +
          "you will stay on the current Plane throughout the battle. 'Chaos' and 'When you Planeswalk...' effects will be resolved as normal, but no effect will cause a plane change.",
      "Good luck, Commander!"
  ),

  MATCH_UI(
      "Your First Match",
      "Take a moment to look around, Commander. In the default layout, your hand and battlefield are below your opponent's area. " +
          "Keep an eye on both life totals as the battle progresses. Double-click a card to cast it when the rules allow and you can pay its costs.",
      "At the bottom right, you will find your Command Zone, Graveyard, and Exile. " +
          "Your Commander can be cast from the Command Zone. Check there for other available actions too, including rolling the Planar Die by using the card.",
      "At the bottom left, the action prompts tell you what the game needs from you. Follow them to make choices and select targets.",
      "At the top left, the Stack shows spells and abilities about to resolve. " +
          "The Match Log beside it lets you review what has happened.",
      "At the top right, Card Details lets you read the cards you inspect. Take your time to understand their abilities."
  ),

  MATCH_CARD_HIGHLIGHTING(
      "Card Highlighting",
      "Notice the colored outlines, Commander. By default, blue helps identify cards you can play or whose abilities you can activate. " +
          "This includes land and spell plays, and during blocking it marks possible blockers.",
      "Red marks creatures available to declare as attackers. Magenta marks cards you have chosen or that the current interaction highlights.",
      "During mana payment, an orange glow shows the mana sources Auto-Pay proposes to use. " +
          "Check that preview before committing, especially when you want to keep certain sources available.",
      "A cyan outline is a reminder of Flash or a permission to play a card as though it has Flash. " +
          "Some highlights can be changed or disabled in the display preferences."
  ),

  MATCH_PHASES_AND_YIELDS(
      "Phase Stops and Yield Settings",
      "Let us put the game's yield controls to work, Commander. Click the phase buttons to enable or disable the default priority stops during a turn. " +
          "You are choosing when the game pauses for your input, not removing phases from the game.",
      "The 'End Turn' button at the bottom left passes automatically through the rest of the current turn, subject to yield interruptions. " +
          "It does not end the turn immediately. When your last action can be undone, that button shows 'Undo' instead.",
      "Auto-Pass is turned on by default. The game currently passes automatically when it detects no available actions. " +
          "To turn it off, uncheck 'Game > Enable auto-pass' or press P. " +
          "Unlike the temporary yield requested by 'End Turn', Auto-Pass stays enabled until you turn it off.",
      "Open 'Game > Yield Settings' to choose which events interrupt a yield and return control to you, such as being targeted or having attackers declared against you. " +
          "Set these controls to suit your pace.",
      "Check 'Forge -> Help -> Getting Started -> 'How to Play' and the Forge Wiki for more details on the rules of Magic The Gathering and how to play in Forge."
  ),

  POST_BATTLE(
      "After a battle",
      "The gold you earned from the battle can be spent at any Bazaar, but any unspent Gold will be lost at the end of a Run.\n"
          +
          "Your won Echoes can be spent on Boons, which are permanent upgrades that apply to all future Runs, and are not lost at the end of a Run.",
      "The cards you earned were added to your Rogue deck. You can view your deck at any time by clicking 'Edit Rogue Deck'.\n"
          +
          "If you gained life above your Run's Max Life during the battle, it will reset back to your Max Life after the battle."
  ),

  ELITE_PLANEBOUND(
      "Elite Panebound",
      "An Elite awaits in the next row on the path (marked with a star)! Elite Planebounds are tougher than regular opponents but offer greater rewards:\n"
          +
          "double Echoes, double Gold, and an additional Mythic card reward."
  ),

  CARD_REWARDS(
      "You won!",
      "You made it! You survived your first battle! I knew I could count on you, Commander.",
      "After winning a battle, choose cards from your Reward Pool to add to your deck.\n" +
          "By default, 7 random cards are offered, from which 1 of them is guaranteed to be of mythic rarity.",
      "You can reroll for a new set of cards by spending Gold. The Gold cost increases by 2 for each reroll.\n"
          +
          "Unchosen or rerolled cards leave the active Reward Pool and only return when there are not enough cards left to offer.",
      "For each card added, you will also gain a 'Removal Credit' to remove an unwanted card from your deck."
  ),

  BAZAAR(
      "The Bazaar",
      "Spend your earned Gold at the Bazaar to purchase new cards, price depending on Card rarity.",
          "You can reroll for a new selection by spending Gold, increasing by 2 for each reroll.\n"
          +
          "Unbought or rerolled cards leave the active Reward Pool and only return when there are not enough cards left to offer.",
      "For each card added, you will also gain a 'Removal Credit' to remove an unwanted card from your deck."
  ),

  EVENT(
      "An Event",
      "Events are random encounters that can have a variety of choices and outcomes for your Run.\n"
          + "Some events may offer powerful rewards, but often at a cost."
  ),

  CHEST(
      "A Chest",
      "Chests contain two random Loot rewards - gold, cards from your Reward Pool, or permanent positive Traits that last for the rest of the Run.",
      "Chose which one to keep."
  ),

  SANCTUM(
      "The Sanctum",
      "The Sanctum offers various services: resting, cooking and reflecting.\n" +
          "Gain life (up until your Max. Life) and cure all wounds, craft a random Food item or gain 3 Removal Credits.",
      "You may use only one of them."
  ),

  DECK_EDITOR(
      "Deck Editor",
      "Review and edit your current Rogue Deck at any time (seen in the center).\n"
          +
          "You can always add and remove as many basic lands as needed (from the Pool on the left), but other cards can only be added as rewards during the Run, and can only be removed with Removal Credits earned by adding cards from a Card Reward or Shop.",
      "Right-click or double-click a card to add / remove it. You can always revert your choices by clicking 'Undo'.",
      "To keep track of your Mana cost, Mana Production from Lands, average Lands in your starting hand and other Stats, switch to the 'Statistic' tab at the top center",
      "Click 'Back to Map' above your deck cards when you are ready to continue your Run\n"
      +
      "Changes to your deck will be saved automatically and persist for the rest of the Run, but will be reset at the end of the Run regardless of win or loss."
  ),

  RUN_COMPLETE(
      "Run Complete",
      "You completed your first Run! Win or lose, you've earned Echoes " +
          "based on your progress. Visit the Aether to unlock Boons to make you stronger in future Runs.",
      "Open the 'Codex' to view and reset your overall game progress, stats, unlocked cards and tutorials.\n"
          +
          "View all your past Runs and Rogue Decks in the 'History'."
  ),

  FIRST_RUN_WIN(
      "The Journey Has Only Begun",
      "You have done it, Commander. Your first Run ends in victory! I knew you had the strength to reach this far. " +
          "But do not mistake this victory for the end of our journey. We have only just begun.",
      "Win Runs with three different Commanders to unlock Descension. There, the trials grow harder, and each victory takes us deeper. " +
          "The truth behind all of this is still waiting to be uncovered. When you are ready, we must go further."
  ),

  AETHER(
      "Welcome to the Aether",
      "Welcome to my realm. In the Aether, you can spend your Echoes on powerful Boons that provide permanent upgrades for all future Runs. "
          +
          "Unlock and upgrade boons with Echoes, then activate up to 3 boons at the same time."
  ),

  DESCENSION_UNLOCKED(
      "Descension Mode Unlocked",
      "You have won Runs with 3 different Commanders - Descension Mode is now unlocked!\n" +
          "Descension Mode adds stacking difficulty modifiers to your Runs. Select a Commander you have already won with to enable it.",
      "Winning at a Descension Level unlocks the next level for that Commander, and earns you a Spark. Sparks can be used to unlock special upgrades in the Aether."
  ),

  DESCENSION_LEVEL_1_WIN(
      "Deeper Into Descension",
      "Your first Descension victory, Commander. You have faced the deeper trials and prevailed. " +
          "Descension Level 2 is now unlocked for the Commander who earned this victory.",
      "Remember: each Descension Level includes the modifiers of every previous level. " +
          "Level 2 adds its own challenge on top of Level 1, and that pattern continues as you descend.",
      "You also earned a Spark. Bring it to the Aether, where Sparks and Echoes can be spent on upgrades to the Aether itself. " +
          "Prepare well. There is more for us to discover."
  ),

  DESCENSION_LEVEL_7_WIN(
      "Beyond the Final Trial",
      "Descension Level 7 lies behind you, Commander. You have overcome the highest challenge currently available in Rogue Commander. " +
          "Few journeys demand so much. You have every reason to be proud of yours.",
      "Our story is not finished. Rogue Commander is still in development, with more updates and content planned, " +
          "including Challenges and a second path beyond the final boss. Stay tuned for what comes next.",
      "Until then, there are still Achievements to pursue and other Commanders to master. " +
          "Thank you for playing, Commander. I look forward to meeting you on the path again."
  ),

  CARRY_CARDS(
      "Carry Cards",
      "You have acquired a carry card! Carry cards persist across matches and can be cast from your command zone.",
      "There are three types:\n" +
          "- Items: Artifacts such as equipment and vehicles.\n" +
          "- Fellows: Creatures that fight alongside you.\n" +
          "- Scrolls: Instants and sorceries kept in your command zone until used.",
      "Items and fellows are lost permanently if they are neither in the command zone nor on the battlefield after a match. Scrolls are lost if they end a match outside the command zone."
  ),

  CODEX(
      "Codex",
      "The Codex tracks your Rogue Commander progress. Global Stats shows run and match records, Rogue Commanders shows each commander's reward cards, Planebounds shows encountered planes and their decks, and Traits shows discovered Run Trait cards.",
      "Entries have 3 states: Unknown entries never appeared during a Run, Seen entries were offered or revealed, and Acquired entries were added to your deck or gained during a Run."
  );

  private final String title;
  private final List<String> messageChunks;

  RogueTutorial(String title, String... messageChunks) {
    this.title = title;
    this.messageChunks = List.of(messageChunks);
  }

  public String getTitle() {
    return title;
  }

  public String getMessage() {
    return String.join("\n", messageChunks);
  }

  public List<String> getMessageChunks() {
    return messageChunks;
  }

  public String getId() {
    return name();
  }
}
