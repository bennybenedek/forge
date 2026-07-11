package forge.gamemodes.rogue.effect;

import forge.game.player.RegisteredPlayer;
import forge.gamemodes.rogue.RogueRun;
import forge.gamemodes.rogue.RogueRun.CarryCardType;
import forge.gamemodes.rogue.npc.NPC;
import java.util.ArrayList;
import java.util.List;

/**
 * NPC effects granted during run start encounters.
 * Each constant defines its own behavior explicitly.
 */
public enum NPCEffect implements RogueEffect {

    TYVAR_LIEUTENANT_OF_APPRENTICES("npc_tyvar_lieutenant_of_apprentices", "Lieutenant Of Apprentices",
        FELLOW_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.ONESHOT, "Loyal Apprentice") {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addEffectCardAsCarryCard(run, ctx, CarryCardType.FELLOW);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.canAddCardAsCarryCard(getEffectCard());
        }
    },
    TYVAR_LIEUTENANT_OF_DRAKES("npc_tyvar_lieutenant_of_drakes", "Lieutenant Of Drakes",
        FELLOW_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.ONESHOT, "Loyal Drake") {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addEffectCardAsCarryCard(run, ctx, CarryCardType.FELLOW);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.canAddCardAsCarryCard(getEffectCard());
        }
    },
    TYVAR_LIEUTENANT_OF_GUARDIANS("npc_tyvar_lieutenant_of_guardians", "Lieutenant Of Guardians",
        FELLOW_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.ONESHOT, "Loyal Guardian") {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addEffectCardAsCarryCard(run, ctx, CarryCardType.FELLOW);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.canAddCardAsCarryCard(getEffectCard());
        }
    },
    TYVAR_LIEUTENANT_OF_SUBORDINATES("npc_tyvar_lieutenant_of_subordinates", "Lieutenant Of Subordinates",
        FELLOW_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.ONESHOT, "Loyal Subordinate") {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addEffectCardAsCarryCard(run, ctx, CarryCardType.FELLOW);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.canAddCardAsCarryCard(getEffectCard());
        }
    },
    TYVAR_LIEUTENANT_OF_UNICORNS("npc_tyvar_lieutenant_of_unicorns", "Lieutenant Of Unicorns",
        "Gain the {{Fellow}} %s.", NPC.TYVAR,
        EffectType.ONESHOT, "Loyal Unicorn") {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addEffectCardAsCarryCard(run, ctx, CarryCardType.FELLOW);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.canAddCardAsCarryCard(getEffectCard());
        }
    },
    TYVAR_RESPITE("npc_tyvar_respite", "Tyvar's Respite",
        ITEM_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.ONESHOT, "Campfire") {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addEffectCardAsCarryCard(run, ctx, CarryCardType.ITEM);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.canAddCardAsCarryCard(getEffectCard());
        }
    },
    TYVAR_LEADERSHIP("npc_tyvar_leadership", "Tyvar's Leadership",
        ITEM_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.ONESHOT, "Commander's Sphere") {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addEffectCardAsCarryCard(run, ctx, CarryCardType.ITEM);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.canAddCardAsCarryCard(getEffectCard());
        }
    },
    TYVAR_LEGACY("npc_tyvar_legacy", "Tyvar's Legacy",
        ITEM_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.ONESHOT, "Tome of Legends|MKC|1") {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addEffectCardAsCarryCard(run, ctx, CarryCardType.ITEM);
        }

        @Override
        public boolean isChoiceAvailable(RogueRun run) {
            return run.canAddCardAsCarryCard(getEffectCard());
        }
    },
    TYVAR_BASTION_COMMANDER("npc_tyvar_bastion_commander", "Bastion Commander",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Bastion Commander") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_BLOODSWORN_COMMANDER("npc_tyvar_bloodsworn_commander", "Bloodsworn Commander",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Bloodsworn Commander") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_AUGMENTED_COMMANDER("npc_tyvar_augmented_commander", "Augmented Commander",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Augmented Commander") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },

    // Narset traits
    NARSET_TRAVELER("npc_narset_traveler", "Traveler",
        TRAIT_GAIN_DESCRIPTION + " ![[Fractured Powerstone]]", NPC.NARSET,
        EffectType.PERMANENT, "Narset Trait - Traveler") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            RogueEffect.addCardToBattlefield("Fractured Powerstone", human);
        }
    },
    NARSET_ALCHEMIST("npc_narset_alchemist", "Alchemist",
        "Gain the {{Item}} %s.", NPC.NARSET,
        EffectType.ONESHOT, "Ichor Elixir") {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addEffectCardAsCarryCard(run, ctx, CarryCardType.ITEM);
        }
    },
    NARSET_CHAOSBOUND("npc_narset_chaosbound", "Chaosbound",
        TRAIT_GAIN_DESCRIPTION + " ![[Narset - Chaos Capsule]]", NPC.NARSET,
        EffectType.PERMANENT, "Narset Trait - Chaosbound") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            RogueEffect.addCardToBattlefield("Narset - Chaos Capsule", human);
        }
    },
    NARSET_GOD_OF_CHAOS("npc_narset_god_of_chaos", "God of Chaos",
        TRAIT_GAIN_DESCRIPTION, NPC.NARSET,
        EffectType.PERMANENT, "Narset Trait - God of Chaos") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    };

    private final String id;
    private final String displayName;
    private final String description;
    private final NPC ownerNpc;
    private final EffectType effectType;
    private final String effectCardReference;

    NPCEffect(String id, String displayName, String description, NPC ownerNpc, EffectType effectType,
              String effectCardReference) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.ownerNpc = ownerNpc;
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
    public String getRawDescription() { return formatEffectCardDescription(description); }

    @Override
    public String getEffectCardReference() { return effectCardReference; }

    public NPC getOwnerNpc() { return ownerNpc; }

    public boolean isChoiceAvailable(RogueRun run) { return true; }

    public static List<NPCEffect> getEffectsForNpc(NPC npc, RogueRun run) {
        List<NPCEffect> effects = new ArrayList<>();
        for (NPCEffect effect : values()) {
            if (effect.ownerNpc == npc && (run == null || effect.isChoiceAvailable(run))) {
                effects.add(effect);
            }
        }
        return effects;
    }

    public static NPCEffect fromId(String id) {
        for (NPCEffect b : values())
            if (b.id.equals(id)) return b;
        return null;
    }
}
