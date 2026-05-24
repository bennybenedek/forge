package forge.gamemodes.rogue;

import forge.gamemodes.rogue.effect.EventEffect;
import java.util.List;

/**
 * Catalog of all events in Rogue Commander mode.
 * Each event has a description and a list of choices with associated effects.
 */
public enum RogueEvent {

    WANDERING_HEALER("Wandering Healer",
        "A mysterious healer offers their services... for a price.",
        List.of(
            new EventChoice("Receive Healing Potion", "You gulp the aweful tasting but strangely empowering potion down.",
                EventEffect.HEALER_POTION),
            new EventChoice("Treat Wounds", "The healer mends your wounds.",
                EventEffect.HEALER_TREAT_WOUNDS),
            new EventChoice("Strengthen", "The healer nodds confidently after being done with your fortification.",
                EventEffect.HEALER_STRENGTHEN),
            new EventChoice("Decline", "You wave the healer away.",
                EventEffect.NOTHING)
        )),

    PLANAR_RIFT("Planar Rift",
        "A rift in the planes tears open before you.",
        List.of(
            new EventChoice("Enter the Rift", "The rift's energy empowers your Commander!",
                EventEffect.PLANAR_RIFT_BOOST),
            new EventChoice("Harvest the Energy", "You siphon raw mana from the rift.",
                EventEffect.PLANAR_RIFT_ENERGY)
        )),

    MERCHANT_CARAVAN("Merchant Caravan",
        "A caravan of planar merchants sets up shop.",
        List.of(
            new EventChoice("Browse Their Wares", "The merchant thanks you for the business.",
                EventEffect.CARAVAN_BROWSE),
            new EventChoice("Rob Them", "You take what you want by force.",
                EventEffect.CARAVAN_ROB)
        )),

    DRIFTED_AWAY("Drifted Away",
        "You come across a crash-site. The pilot lies nearby, hurt and unconscious, while the wreck still hums with fading power.",
        List.of(
            new EventChoice("Rescue", "The pilot stirs and joins your journey.",
                EventEffect.DRIFTED_RESCUE),
            new EventChoice("Steal", "You strip the wreck for whatever still works.",
                EventEffect.DRIFTED_STEAL)
        )),

    BENDING_DESTINY("Bending Destiny",
        "At a weathered crossroads shrine, a calm traveler offers guidance for the road ahead. " +
        "Some wisdom walks beside you, they say. Some wisdom waits in the scroll case.",
        List.of(
            new EventChoice("Walk together", "A steadfast traveler falls into step beside you.",
                EventEffect.BENDING_WALK),
            new EventChoice("Study", "You leave with lessons worth keeping close at hand.",
                EventEffect.BENDING_STUDY)
        )),

    PLANAR_TRIBUTE("Planar Tribute",
        "The planes demand tribute.",
        List.of(
            new EventChoice("Sacrifice", "The planes took what is yours.",
                EventEffect.PLANAR_TRIBUTE_REMOVE),
            new EventChoice("Give and take", "The planes reshaped your arsenal.",
                EventEffect.PLANAR_TRIBUTE_REPLACE)
        )),

    AMBUSH("Ambush!",
        "Hostile forces materialize from a rift! 'Empty your pockets or prepare to die. Your choice.', their leader says.",
        List.of(
            new EventChoice("Fight", "You fought your way through.",
                EventEffect.AMBUSH_FIGHT),
            new EventChoice("Bribe", "'Good choice, maggot.', the leader says and commands his companions back into the rift.",
                EventEffect.AMBUSH_BRIBE)
        )),

    PLANAR_EXCHANGE("Planar Exchange",
        "A shimmering portal offers to reshape your arsenal.",
        List.of(
            new EventChoice("Step Through", "The planes shift your deck...",
                EventEffect.PLANAR_EXCHANGE),
            new EventChoice("Stay Put", "You decide not to risk it.",
                EventEffect.NOTHING)
        )),

    GAMECHANGER("Gamechanger",
        "A suspicious figure eyes your deck with open contempt. \"Worthless,\" they sneer. " +
        "\"Once you change your game, you won't need half this library anymore.\"",
        List.of(
            new EventChoice("Trust blindly", "The figure strips away your old tricks and offers you something far stronger.",
                EventEffect.GAMECHANGER_TRUST),
            new EventChoice("Choose wisely", "The figure grins and disappears into the shadows.",
                EventEffect.GAMECHANGER_CHOOSE)
        )),

    GROUND_ZERO("Ground Zero",
        "You enter a cratered ruin choked with ash and broken steel. Strange relics lie half-buried in the dust, while twisted survivors skulk through the fallout.",
        List.of(
            new EventChoice("Loot something S.P.E.C.I.A.L.", "A few old-world lessons make your arsenal feel a little more S.P.E.C.I.A.L.",
                EventEffect.GROUND_ZERO_SPECIAL),
            new EventChoice("Use Workbench", "You salvage enough parts to restore a few obedient machines.",
                EventEffect.GROUND_ZERO_REPAIR),
            new EventChoice("Explore Wasteland", "The wasteland's radiation leaves part of your army forever changed.",
                EventEffect.GROUND_ZERO_MUTATE)
        )),

    CROOKED_COUNSEL("Crooked Counsel",
        "An old wizard, once a cherished friend, greets you with a smile that no longer reaches his eyes. " +
        "\"The shadow rises,\" he murmurs. \"Stand against it if you must... but it wouldn't be wise, my friend.\"",
        List.of(
            new EventChoice("Rally the Free Peoples", "You assemble a fellowship worthy of the long road ahead.",
                EventEffect.CROOKED_COUNSEL_FELLOWSHIP),
            new EventChoice("Join with the Dark Lord", "Dark riders answer your choice, and your old allies fall away.",
                EventEffect.CROOKED_COUNSEL_NAZGUL),
            new EventChoice("Keep to your own path", "A quiet will settles over you, and the ring answers only to your hand.",
                EventEffect.CROOKED_COUNSEL_RING)
        )),

    THORNS("Thorns",
        "The land itself rejects your presence. Thorns rise from the soil, leaving your footsteps stained with blood.",
        List.of(
            new EventChoice("Endure", "You suffer a wound.",
                EventEffect.THORNS_ENDURE),
            new EventChoice("Press On", "You lose 4 life.",
                EventEffect.THORNS_PRESS)
        )),

    HORROR("Horror",
        "A glistening horror clings to your soul, invisible but insatiable. It drinks deep, of your vitality, your future, and your fortune.",
        List.of(
            new EventChoice("Surrender", "You lose all your gold.",
                EventEffect.LOSE_ALL_GOLD),
            new EventChoice("Resist", "You lose all your echoes.",
                EventEffect.LOSE_ALL_ECHOES)
        )),

    LOST_CONNECTION("Lost",
        "Your connection to your spark flickers. You reach for your Commander's presence, but the link has gone cold.",
        List.of(
            new EventChoice("Depart", "Your Commander disappeared into the void.",
                EventEffect.LOST_DEPART),
            new EventChoice("Persist", "Your Commander holds on, but feels diminished.",
                EventEffect.LOST_PERSIST),
            new EventChoice("Replace", "A new legend answers your call and takes command of your deck.",
                EventEffect.LOST_REPLACE)
        )),

    SATCHEL("Satchel",
        "Hidden beneath a crumbled pillar, you find a satchel of cards bound in leather; remnants of a Planeswalker who walked here before you.",
        List.of(
            new EventChoice("Open the Satchel", "You find a hidden chest.",
                EventEffect.FIND_CHEST)
        )),

    SHRINE("Shrine",
        "A crumbling shrine pulses faintly with restorative energy. As you kneel, you immediately feel its soothing powers.",
        List.of(
            new EventChoice("Kneel", "You discover a hidden Sanctum.",
                EventEffect.FIND_SANCTUM)
        )),

    DISTORTION("Distortion",
        "Time lurches sideways. Nothing left to salvage. No spoils. No memory. Only silence.",
        List.of(
            new EventChoice("Suffer", "The silence consumed you.",
                EventEffect.DISTORTION_SKIP_REWARDS),
            new EventChoice("Endure", "You salvage only fading fragments from the coming battles.",
                EventEffect.DISTORTION_FADED_REWARDS)
        )),

    MEET_TYVAR("Tyvar Kell",
        "An elf Planeswalker emerges from a rift, his spark blazing with raw energy. " +
        "\"I am Tyvar Kell, and I know talent when I see it. Let me become your Commander's trainer.\"",
        List.of(
            new EventChoice("Accept", "Tyvar Kell will appear at the start of future Runs to train your Commander.",
                EventEffect.MEET_NPC_TYVAR)
        )) {
        @Override
        public boolean isAvailable() {
            return false;
        }
    };

    public record EventChoice(String label, String resultText, EventEffect effect) {}

    private final String displayName;
    private final String description;
    private final List<EventChoice> choices;

    RogueEvent(String displayName, String description, List<EventChoice> choices) {
        this.displayName = displayName;
        this.description = description;
        this.choices = choices;
    }

    public String getDisplayName() { return displayName; }
    public String getRawDescription() { return description; }
    public String getDescription() { return TextHelper.stripPreviewMarkers(getRawDescription()); }
    public List<PreviewReference> getPreviewReferences() { return TextHelper.extractPreviewReferences(getRawDescription()); }
    public String getPreviewCardName() { return TextHelper.extractFirstCardName(getRawDescription()); }
    public List<EventChoice> getChoices() { return choices; }

    /** Whether this event should appear in the event pool. Override for one-time events. */
    public boolean isAvailable() { return true; }

    @Override
    public String toString() { return displayName; }
}
