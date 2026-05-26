package forge.gamemodes.rogue.effect;

import forge.game.player.RegisteredPlayer;
import forge.gamemodes.rogue.RogueConfig;
import forge.gamemodes.rogue.RogueRun;
import forge.item.PaperCard;

/**
 * NPC effets granted during run start encounters.
 * Each constant defines its own behavior explicitly.
 */
public enum NPCEffect implements RogueEffect {

    TYVAR_MIGHT("npc_tyvar_might", "Tyvar's Might", "Gain the {{Boon}} **Tyvar's Might**. ![[Tyvar Boon - Might]]") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToCommandZone("Tyvar Boon - Might", human);
        }
    },
    TYVAR_DISCOUNT("npc_tyvar_discount", "Tyvar's Efficiency", "Gain the {{Boon}} **Tyvar's Efficiency**. ![[Tyvar Boon - Efficiency]]") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToCommandZone("Tyvar Boon - Efficiency", human);
        }
    },
    TYVAR_HASTE("npc_tyvar_haste", "Tyvar's Fury", "Gain the {{Boon}} **Tyvar's Fury**. ![[Tyvar Boon - Fury]]") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToCommandZone("Tyvar Boon - Fury", human);
        }
    },

    // Narset boons
    NARSET_TRAVELER("npc_narset_traveler", "Traveler", "Start each match with a [[Fractured Powerstone]] on the battlefield.") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToBattlefield("Fractured Powerstone", human);
        }
    },
    NARSET_ALCHEMIST("npc_narset_alchemist", "Alchemist", "Start the Run with an [[Ichor Elixir]] {{Item}} in the command zone.") {
        @Override
        public void onGranted(RogueRun run) {
            PaperCard ichorElixir = RogueConfig.getCard("Ichor Elixir", null, null);
            run.addCarryCard(ichorElixir, RogueRun.CarryCardType.ITEM, getId());
        }
    },
    NARSET_CHAOSBOUND("npc_narset_chaosbound", "Chaosbound", "Start each match with a **Chaosbound** ![[Narset - Chaosbound]] on the battlefield.") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToBattlefield("Narset - Chaosbound", human);
        }
    },
    NARSET_GOD_OF_CHAOS("npc_narset_god_of_chaos", "God of Chaos", "Gain the {{Boon}} **God of Chaos**. ![[Narset Boon - God of Chaos]]") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToCommandZone("Narset Boon - God of Chaos", human);
        }
    };

    private final String id;
    private final String displayName;
    private final String description;

    NPCEffect(String id, String displayName, String description) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getRawDescription() { return description; }

    public static NPCEffect fromId(String id) {
        for (NPCEffect b : values())
            if (b.id.equals(id)) return b;
        return null;
    }
}
