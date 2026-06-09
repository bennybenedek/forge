package forge.gamemodes.rogue.effect;

import forge.game.player.RegisteredPlayer;
import forge.gamemodes.rogue.RogueRun;
import forge.item.PaperCard;

/**
 * NPC effets granted during run start encounters.
 * Each constant defines its own behavior explicitly.
 */
public enum NPCEffect implements RogueEffect {

    TYVAR_MIGHT("npc_tyvar_might", "Tyvar's Might",
        "Gain the {{Trait}} **Tyvar's Might**.",
        EffectType.PERMANENT, "Tyvar Trait - Might") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_DISCOUNT("npc_tyvar_discount", "Tyvar's Efficiency",
        "Gain the {{Trait}} **Tyvar's Efficiency**.",
        EffectType.PERMANENT, "Tyvar Trait - Efficiency") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_HASTE("npc_tyvar_haste", "Tyvar's Fury",
        "Gain the {{Trait}} **Tyvar's Fury**.",
        EffectType.PERMANENT, "Tyvar Trait - Fury") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },

    // Narset traits
    NARSET_TRAVELER("npc_narset_traveler", "Traveler",
        "Start each match with a [[Fractured Powerstone]] on the battlefield.",
        EffectType.PERMANENT, null) {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToBattlefield("Fractured Powerstone", human);
        }
    },
    NARSET_ALCHEMIST("npc_narset_alchemist", "Alchemist",
        "Start the Run with an [[Ichor Elixir]] {{Item}} in the command zone.",
        EffectType.ONESHOT, "Ichor Elixir") {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            PaperCard ichorElixir = getEffectCard();
            run.addCarryCard(ichorElixir, RogueRun.CarryCardType.ITEM, getId());
        }
    },
    NARSET_CHAOSBOUND("npc_narset_chaosbound", "Chaosbound",
        "Start each match with a **Chaos Capsule** on the battlefield.",
        EffectType.PERMANENT, "Narset - Chaos Capsule") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToBattlefield(human);
        }
    },
    NARSET_GOD_OF_CHAOS("npc_narset_god_of_chaos", "God of Chaos",
        "Gain the {{Trait}} **God of Chaos**.",
        EffectType.PERMANENT, "Narset Trait - God of Chaos") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    };

    private final String id;
    private final String displayName;
    private final String description;
    private final EffectType effectType;
    private final String effectCardReference;

    NPCEffect(String id, String displayName, String description, EffectType effectType,
              String effectCardReference) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.effectType = effectType;
        this.effectCardReference = effectCardReference;
    }

    @Override
    public EffectType getEffectType() { return effectType; }

    @Override
    public String getId() { return id; }

    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getRawDescription() { return description; }

    @Override
    public String getEffectCardReference() { return effectCardReference; }

    public static NPCEffect fromId(String id) {
        for (NPCEffect b : values())
            if (b.id.equals(id)) return b;
        return null;
    }
}
