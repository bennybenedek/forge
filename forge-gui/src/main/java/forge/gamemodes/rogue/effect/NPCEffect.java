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

    TYVAR_MIGHT("npc_tyvar_might", "Tyvar's Might",
        "Gain the {{Trait}} **Tyvar's Might**. ![[Tyvar Trait - Might]]", EffectType.PERMANENT) {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToCommandZone("Tyvar Trait - Might", human);
        }
    },
    TYVAR_DISCOUNT("npc_tyvar_discount", "Tyvar's Efficiency",
        "Gain the {{Trait}} **Tyvar's Efficiency**. ![[Tyvar Trait - Efficiency]]",
        EffectType.PERMANENT) {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToCommandZone("Tyvar Trait - Efficiency", human);
        }
    },
    TYVAR_HASTE("npc_tyvar_haste", "Tyvar's Fury",
        "Gain the {{Trait}} **Tyvar's Fury**. ![[Tyvar Trait - Fury]]", EffectType.PERMANENT) {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToCommandZone("Tyvar Trait - Fury", human);
        }
    },

    // Narset traits
    NARSET_TRAVELER("npc_narset_traveler", "Traveler",
        "Start each match with a [[Fractured Powerstone]] on the battlefield.", EffectType.PERMANENT) {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToBattlefield("Fractured Powerstone", human);
        }
    },
    NARSET_ALCHEMIST("npc_narset_alchemist", "Alchemist",
        "Start the Run with an [[Ichor Elixir]] {{Item}} in the command zone.", EffectType.ONESHOT) {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            PaperCard ichorElixir = RogueConfig.getCard("Ichor Elixir", null, null);
            run.addCarryCard(ichorElixir, RogueRun.CarryCardType.ITEM, getId());
        }
    },
    NARSET_CHAOSBOUND("npc_narset_chaosbound", "Chaosbound",
        "Start each match with a **Chaos Capsule** ![[Narset - Chaos Capsule]] on the battlefield.",
        EffectType.PERMANENT) {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToBattlefield("Narset - Chaos Capsule", human);
        }
    },
    NARSET_GOD_OF_CHAOS("npc_narset_god_of_chaos", "God of Chaos",
        "Gain the {{Trait}} **God of Chaos**. ![[Narset Trait - God of Chaos]]",
        EffectType.PERMANENT) {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToCommandZone("Narset Trait - God of Chaos", human);
        }
    };

    private final String id;
    private final String displayName;
    private final String description;
    private final EffectType effectType;

    NPCEffect(String id, String displayName, String description, EffectType effectType) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.effectType = effectType;
    }

    @Override
    public EffectType getEffectType() { return effectType; }

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
