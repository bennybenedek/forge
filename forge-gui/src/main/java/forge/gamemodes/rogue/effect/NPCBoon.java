package forge.gamemodes.rogue.effect;

import forge.game.player.RegisteredPlayer;
import forge.gamemodes.rogue.RogueRun;

/**
 * NPC boons that add command zone cards on match start.
 * Each constant maps to a rogue card script loaded via RogueEffect.addCustomCardToCommandZone.
 */
public enum NPCBoon implements RogueEffect {

    TYVAR_MIGHT("npc_tyvar_might", "Tyvar's Might",
            "Your Commander gets +2/+2.", "NPC Tyvar - Might"),
    TYVAR_DISCOUNT("npc_tyvar_discount", "Tyvar's Efficiency",
            "Your Commander costs {1} less to cast.", "NPC Tyvar - Discount"),
    TYVAR_HASTE("npc_tyvar_haste", "Tyvar's Fury",
            "Your Commander has haste.", "NPC Tyvar - Haste"),

    // Narset boons
    NARSET_TRAVELER("npc_narset_traveler", "Traveler",
            "Start each match with a Fractured Powerstone on the battlefield.", "Fractured Powerstone") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addRealCardToBattlefield(cardName, human);
        }
    },
    NARSET_ALCHEMIST("npc_narset_alchemist", "Alchemist",
            "Start each match with an Ichor Elixir on the battlefield.", "Ichor Elixir") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addRealCardToBattlefield(cardName, human);
        }
    },
    NARSET_CHAOSBOUND("npc_narset_chaosbound", "Chaosbound",
            "Start each match with an artifact you can sacrifice to trigger Chaos.", "NPC Narset - Chaosbound") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCustomCardToBattlefield(cardName, human);
        }
    },
    NARSET_GOD_OF_CHAOS("npc_narset_god_of_chaos", "God of Chaos",
            "All Planeswalk die results become Chaos instead.", "NPC Narset - God of Chaos");

    private final String id;
    private final String displayName;
    private final String description;
    protected final String cardName;

    NPCBoon(String id, String displayName, String description, String cardName) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.cardName = cardName;
    }

    @Override
    public EffectType getEffectType() { return EffectType.PERMANENT; }

    @Override
    public String getId() { return id; }

    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getDescription() { return description; }

    @Override
    public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
        RogueEffect.addCustomCardToCommandZone(cardName, human);
    }

    public static NPCBoon fromId(String id) {
        for (NPCBoon b : values())
            if (b.id.equals(id)) return b;
        return null;
    }
}
