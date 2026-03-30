package forge.gamemodes.rogue.effect;

import forge.game.player.RegisteredPlayer;
import forge.gamemodes.rogue.RogueRun;

/**
 * NPC boons granted during run start encounters.
 * Each constant defines its own behavior explicitly.
 */
public enum NPCBoon implements RogueEffect {

    TYVAR_MIGHT("npc_tyvar_might", "Tyvar's Might", "Your Commander gets +2/+2.") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToCommandZone("NPC Tyvar - Might", human);
        }
    },
    TYVAR_DISCOUNT("npc_tyvar_discount", "Tyvar's Efficiency", "Your Commander costs {1} less to cast.") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToCommandZone("NPC Tyvar - Discount", human);
        }
    },
    TYVAR_HASTE("npc_tyvar_haste", "Tyvar's Fury", "Your Commander has haste.") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToCommandZone("NPC Tyvar - Haste", human);
        }
    },

    // Narset boons
    NARSET_TRAVELER("npc_narset_traveler", "Traveler", "Start each match with a Fractured Powerstone on the battlefield.") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToBattlefield("Fractured Powerstone", human);
        }
    },
    NARSET_ALCHEMIST("npc_narset_alchemist", "Alchemist", "Start each match with the item 'Ichor Elixir' in the command zone.") {
        @Override
        public void onGranted(RogueRun run) {
            run.addCarryCard("Ichor Elixir", RogueRun.CarryCardType.ITEM, getId());
        }
    },
    NARSET_CHAOSBOUND("npc_narset_chaosbound", "Chaosbound", "Start each match with an artifact you can sacrifice to trigger Chaos.") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToBattlefield("NPC Narset - Chaosbound", human);
        }
    },
    NARSET_GOD_OF_CHAOS("npc_narset_god_of_chaos", "God of Chaos", "All Planeswalk die results become Chaos instead.") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToCommandZone("NPC Narset - God of Chaos", human);
        }
    };

    private final String id;
    private final String displayName;
    private final String description;

    NPCBoon(String id, String displayName, String description) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
    }

    @Override
    public EffectType getEffectType() { return EffectType.PERMANENT; }

    @Override
    public String getId() { return id; }

    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getDescription() { return description; }

    public static NPCBoon fromId(String id) {
        for (NPCBoon b : values())
            if (b.id.equals(id)) return b;
        return null;
    }
}
