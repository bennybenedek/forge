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
    TYVAR_CAMP("npc_tyvar_camp", "Tyvar's Camp",
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
    TYVAR_SPHERE("npc_tyvar_sphere", "Tyvar's Sphere",
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
    TYVAR_TOME("npc_tyvar_tome", "Tyvar's Tome",
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
    TYVAR_AUGMENTED("npc_tyvar_augmented", "Augmented",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Augmented") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_BASTION("npc_tyvar_bastion", "Bastion",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Bastion") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_BLOODSWORN("npc_tyvar_bloodsworn", "Bloodsworn",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Bloodsworn") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_BRAWLER("npc_tyvar_brawler", "Brawler",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Brawler") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_CHARLATAN("npc_tyvar_charlatan", "Charlatan",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Charlatan") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_CHEF("npc_tyvar_chef", "Chef",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Chef") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_CRIMINAL("npc_tyvar_criminal", "Criminal",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Criminal") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_CULTIST("npc_tyvar_cultist", "Cultist",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Cultist") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_FLAME("npc_tyvar_flame", "Flame",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Flame") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_SAGE("npc_tyvar_sage", "Sage",
        TRAIT_GAIN_DESCRIPTION, NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Sage") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
        }
    },
    TYVAR_BEACON("npc_tyvar_beacon", "Tyvar's Beacon",
        TRAIT_GAIN_DESCRIPTION + " ![[Command Beacon]]", NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Beacon") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            RogueEffect.addCardToBattlefield("Command Beacon", human);
        }
    },
    TYVAR_WAR_ROOM("npc_tyvar_war_room", "Tyvar's War Room",
        TRAIT_GAIN_DESCRIPTION + " ![[War Room]]", NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - War Room") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            RogueEffect.addCardToBattlefield("War Room", human);
        }
    },
    TYVAR_PALACE("npc_tyvar_palace", "Tyvar's Palace",
        TRAIT_GAIN_DESCRIPTION + " ![[Opal Palace]]", NPC.TYVAR,
        EffectType.PERMANENT, "Tyvar Trait - Palace") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            RogueEffect.addCardToBattlefield("Opal Palace", human);
        }
    },

    // Narset traits
    NARSET_POWERSTONE("npc_narset_powerstone", "Narset's Powerstone",
        ITEM_GAIN_DESCRIPTION, NPC.NARSET,
        EffectType.ONESHOT, "Fractured Powerstone") {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addEffectCardAsCarryCard(run, ctx, CarryCardType.ITEM);
        }
    },
    NARSET_ELIXIR("npc_narset_alchemist", "Narset's Elixir",
        ITEM_GAIN_DESCRIPTION, NPC.NARSET,
        EffectType.ONESHOT, "Ichor Elixir") {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addEffectCardAsCarryCard(run, ctx, CarryCardType.ITEM);
        }
    },
    NARSET_CHAOS_CAPSULE("npc_narset_chaos_capsule", "Narset's Chaos Capsule",
        ITEM_GAIN_DESCRIPTION, NPC.NARSET,
        EffectType.ONESHOT, "Narset Item - Chaos Capsule") {
        @Override
        public void applyEffect(RogueRun run, EffectResultContext ctx) {
            addEffectCardAsCarryCard(run, ctx, CarryCardType.ITEM);
        }
    },
    NARSET_CHAOSWALKER("npc_chaoswalker", "Chaoswalker",
        TRAIT_GAIN_DESCRIPTION, NPC.NARSET,
        EffectType.PERMANENT, "Narset Trait - Chaoswalker") {
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
