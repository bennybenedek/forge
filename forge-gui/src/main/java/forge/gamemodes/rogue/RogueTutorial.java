package forge.gamemodes.rogue;

/**
 * Defines tutorials shown to new players in Rogue Commander mode. Each tutorial is shown once and
 * then marked as seen in RogueMetaProgress.
 */
public enum RogueTutorial {

  WELCOME(
      "Welcome to Rogue Commander",
      "Rogue Commander is a roguelight deckbuilding format. Start with a commander and an open-ended deck, "
          +
          "then embark on a Run, battling against 'Planebounds' to earn gold and new cards.\n" +
          "Win matches to progress until the final Boss, but be careful - lost life is not restored after each battle!"
  ),

  COMMANDER_SELECTION(
      "Commander Selection",
      "Choose your Rogue commander wisely! Each commander comes with a unique starting deck " +
          "built around their strategy and archetypes, as well as a 'Reward Pool' you choose your card rewards from.\n"
          +
          "You can unlock new commanders by winning Runs or through other achievements. "
  ),

  MAP_NAVIGATION(
      "Map Navigation",
      "The map shows your path through the Run (starting from top to bottom). Each Run will be a different path, with different opponents, encounters and locations.\n"
          +
          "Choose your route carefully - different nodes offer battles, healing, shops, and other encounters.\n"
          +
          "Whenever you complete a node, you unlock the next row of your path. If there is more than 1 accessible node in your current row, you can select your next destination.\n"
          +
          "The more rows you complete, the more life your opponents will start with."
  ),

  PRE_BATTLE(
      "Planebound Battles",
      "Battles on Plane-Nodes are played as a match of 'Commander' (you against the Planebound) with added 'Planechase' rules. So don't forget to check your command zone for casting your Commander and rolling the Planar Die. Other than in a normal Planechase Match,"
          +
          "you will stay on the current Plane throughout the battle. 'Chaos' and 'When you Planeswalk...' effects will be resolved as normal, but no effect will cause a plane change.\n"
          +
          "Check 'Forge -> Help -> Getting Started -> 'How to Play' and the Forge Wiki for more details on the rules of Magic The Gathering and how to play in Forge. Good luck, Commander!"
  ),

  POST_BATTLE(
      "You won!",
      "Congrats to your Win, Commander! The gold you earned from the battle can be spent at any Bazaar, but any unspent Gold will be lost at the end of a Run.\n"
          +
          "Your won Echoes can be spent on Boons, which are permanent upgrades that apply to all future Runs, and are not lost at the end of a Run.\n"
          +
          "The cards you earned were added to your Rogue deck. You can view your deck at any time by clicking 'Edit Rogue Deck'.\n"
          +
          "If you gained life above your Run's Max Life during the battle, it will reset back to your Max Life after the battle."
  ),

  ELITE_ENCOUNTER(
      "Elite Encounter",
      "An Elite awaits (marked with a star)! Elite enemies are tougher than regular opponents but offer greater rewards: "
          +
          "double Echoes, double Gold, and a Mythic card reward."
  ),

  CARD_REWARDS(
      "Card Rewards",
      "After winning a battle, choose cards from your Reward Pool to add to your deck.\n" +
          "By default, 7 random cards are offered, from which 1 of them is guaranteed to be of mythic rarity.\n"
          +
          "You can reroll for a new set of cards by spending Gold. The cost starts at 2 and increases by 2 for each reroll.\n"
          +
          "Unchosen or rerolled cards leave the active Reward Pool and only return when there are not enough cards left to offer.\n"
          +
          "For each card added, you will also gain a 'Removal Credit' to remove an unwanted card from your deck."
  ),

  BAZAAR(
      "The Bazaar",
      "Spend your hard-earned gold at the Bazaar to purchase new cards, price depending on Card rarity. "
          +
          "You can reroll for a new selection by spending Gold, starting at 2 and increasing by 2 each time.\n"
          +
          "Unbought or rerolled cards leave the active Reward Pool and only return when there are not enough cards left to offer.\n"
          +
          "For each card added, you will also gain a 'Removal Credit' to remove an unwanted card from your deck."
  ),

  EVENT(
      "An Event",
      "Events are random encounters that can have a variety of choices and outcomes for your Run. "
          + "Some events may offer powerful rewards, but often at a cost."
  ),

  CHEST(
      "A Chest",
      "Chests contain a random reward - gold, echoes, cards from your Reward Pool, or permanent positive Traits that last for the rest of the Run."
  ),

  SANCTUM(
      "The Sanctum",
      "The Sanctum offers two services: resting and cooking. " +
          "Gain life (up until your Max. Life) and cure all wounds, or craft a random Food item " +
          "that will persist as a carry card for future matches."
  ),

  DECK_EDITOR(
      "Deck Editor",
      "Review your current deck at any time. Keep track of your cards, " +
          "mana curve, and strategy. You can always add and remove as many basic lands as needed, but other cards can only be added as rewards during the Run, and can only be removed with Removal Credits earned by adding cards from a Card Reward or Shop.\n" +
          "Changes to your deck will be saved automatically and persist for the rest of the Run, but will be reset at the end of the Run regardless of win or loss."
  ),

  RUN_COMPLETE(
      "Run Complete",
      "Congratulations on completing a Run! Win or lose, you've earned Echoes " +
          "based on your progress. Use them in the Aether to unlock Boons to make you stronger in future Runs.\n"
          +
          "Visit 'Codex' to view and reset your overall game progress and tutorials.\n"
          +
          "View all your past Runs and Rogue Decks in 'History'."
  ),

  AETHER(
      "Welcome to the Aether",
      "In the Aether, you can spend your hard-earned Echoes on powerful Boons that provide permanent upgrades for all future Runs. "
          +
          "Unlock and upgrade boons with Echoes, then activate up to 3 boons at the same time."
  ),

  DESCENSION_UNLOCKED(
      "Descension Mode Unlocked",
      "You have won Runs with 3 different Commanders - Descension Mode is now unlocked!\n" +
          "Descension Mode adds stacking difficulty modifiers to your Runs. Select a Commander you have already won with to enable it.\n" +
          "Winning at a Descension Level unlocks the next level for that Commander, and earns you a Spark. Sparks can be used to unlock special upgrades in the Aether."
  ),

  CARRY_CARDS(
      "Carry Cards",
      "You have acquired a carry card! Carry cards persist across matches and can be cast from your command zone.\n" +
          "There are three types:\n" +
          "- Items: Artifacts such as equipment and relics.\n" +
          "- Fellows: Creatures that fight alongside you.\n" +
          "- Scrolls: Instants and sorceries kept in your command zone until used.\n" +
          "Items and fellows are lost permanently if they are neither in the command zone nor on the battlefield after a match. Scrolls are lost if they end a match outside the command zone."
  ),

  CODEX(
      "Codex",
      "The Codex tracks your Rogue Commander progress. Global Stats shows run and match records, Rogue Commanders shows each commander's reward cards, Planebounds shows encountered planes and their decks, and Traits shows discovered run traits.\n" +
          "Entries have three states: Unknown entries are hidden, Seen entries were offered or revealed, and Acquired entries were added to your deck or gained during a Run."
  );

  private final String title;
  private final String message;

  RogueTutorial(String title, String message) {
    this.title = title;
    this.message = message;
  }

  public String getTitle() {
    return title;
  }

  public String getMessage() {
    return message;
  }

  public String getId() {
    return name();
  }
}
