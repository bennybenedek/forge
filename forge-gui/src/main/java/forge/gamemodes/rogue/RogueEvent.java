package forge.gamemodes.rogue;

import forge.gamemodes.rogue.effect.EventEffect;
import java.util.List;

/**
 * Catalog of all events in Rogue Commander mode.
 * Each event has a description and a list of choices with associated effects.
 */
public enum RogueEvent {

    AFTER_DUSK("After Dusk",
        "Past midnight, you find shelter in a sprawling mansion where every corridor creaks, every candle gutters, and something hungry moves behind the walls.",
        List.of(
            new EventChoice("Turn insane", "The house slips into your thoughts and leaves nightmares behind.",
                EventEffect.AFTER_DUSK_INSANE),
            new EventChoice("Explore mansion", "You brave the shifting halls and claim a few rooms for yourself.",
                EventEffect.AFTER_DUSK_EXPLORE),
            new EventChoice("Feed monsters", "The mansion's oldest horrors are sated and follow in your wake.",
                EventEffect.AFTER_DUSK_FEED)
        )),

    AMBUSH("Ambush!",
        "Hostile forces materialize from a rift! 'Empty your pockets or prepare to die. Your choice.', their leader says.",
        List.of(
            new EventChoice("Fight", "You fought your way through.",
                EventEffect.AMBUSH_FIGHT),
            new EventChoice("Bribe", "'Good choice, maggot.', the leader says and commands his companions back into the rift.",
                EventEffect.AMBUSH_BRIBE)
        )),

    AMONG_MURDERERS("Among Murderers",
        "Behind tall windows and warm candlelight, every polished smile conceals a motive. " +
            "Servants whisper, goblets tremble, and somewhere in the manor a killer waits to see who will be blamed.",
        List.of(
            new EventChoice("Investigate", "You study the scene in silence and leave with incriminating evidence.",
                EventEffect.AMONG_MURDERERS_INVESTIGATE),
            new EventChoice("Confess", "A hush falls across the room as suspicion settles over your commander and refuses to leave.",
                EventEffect.AMONG_MURDERERS_CONFESS),
            new EventChoice("Hire", "A few discreet payments bring sharp-eyed detectives into your service.",
                EventEffect.AMONG_MURDERERS_HIRE)
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

    BURROWED_INTO_TROUBLE("Burrowed into Trouble",
        "A trader's caravan creaks beneath the weight of iron cages. Squirrels, otters, raccoons, and other small woodland creatures peer out with wide, mournful eyes while the trader smiles and asks what price your pity will bear.",
        List.of(
            new EventChoice("Browse", "You linger among the cages and bargain for a few woodland companions.",
                EventEffect.BURROWED_BROWSE),
            new EventChoice("Free", "You break the cages open and leave the trader cursing, with wounded pride and grateful legends at your side.",
                EventEffect.BURROWED_FREE),
            new EventChoice("Sell", "The trader eagerly buys every suitable beast you part with.",
                EventEffect.BURROWED_SELL)
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

    DISTORTION("Distortion",
        "Time lurches sideways. Space is deformed. ",
        List.of(
            new EventChoice("Embrace", "You let the distortion have its way, and the road ahead comes back wrong.",
                EventEffect.DISTORTION_EMBRACE),
            new EventChoice("Endure", "You salvage only fading fragments from the coming battles.",
                EventEffect.DISTORTION_ENDURE)
        )),

    DRIFTED_AWAY("Drifted Away",
        "You come across a crash-site. The pilot lies nearby, hurt and unconscious, while the wreck still hums with fading power.",
        List.of(
            new EventChoice("Rescue", "The pilot stirs and joins your journey.",
                EventEffect.DRIFTED_RESCUE),
            new EventChoice("Steal", "You strip the wreck for whatever still works.",
                EventEffect.DRIFTED_STEAL)
        )),

    ETERNAL_CRUSADE("Eternal Crusade",
        "Smoke rolls through the shattered outpost as armored giants march toward the front lines without a word. Beneath the failing lumen lights, prayers echo through the halls while you hear something wounded and hungry clawing in the dark.",
        List.of(
            new EventChoice("Secure Specimen", "The containment seals groan, then fail on your terms alone. What emerges is obedient enough for now.",
                EventEffect.ETERNAL_CRUSADE_SECURE_SPECIMEN),
            new EventChoice("Join Space Marines", "You kneel with the crusade, and ceramite discipline hardens every battle still ahead.",
                EventEffect.ETERNAL_CRUSADE_JOIN_SPACE_MARINES),
            new EventChoice("Offer Sacrifice", "The throne drinks deep, and in the hush that follows, its ancient engines stir to life beside you.",
                EventEffect.ETERNAL_CRUSADE_OFFER_SACRIFICE)
        )),

    FINAL_PREPARATIONS("Final Preparations",
        "At the edge of the world, crystal light spills across the city, still glowing as if nothing could ever end. Mercenaries laugh over cheap drinks and weary travelers barter for one last advantage. Every smile carries the quiet knowledge that by morning, you and half these faces may be gone.",
        List.of(
            new EventChoice("Visit Smith", "Steel rings on the anvil late into the night, and you leave the market carrying one last heirloom worthy of the road ahead.",
                EventEffect.FINAL_PREPARATIONS_VISIT_SMITH),
            new EventChoice("Learn Summoning", "Old rites and half-forgotten names answer your call, and one great presence agrees to walk beside you.",
                EventEffect.FINAL_PREPARATIONS_LEARN_SUMMONING),
            new EventChoice("Level Up", "A night's rest, a hot meal, and one last round of hard-earned lessons leave you stronger for what comes next.",
                EventEffect.FINAL_PREPARATIONS_LEVEL_UP)
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

    HORROR("Horror",
        "A glistening horror clings to your soul, invisible but insatiable. It drinks deep, of your vitality, your future, and your fortune.",
        List.of(
            new EventChoice("Surrender", "You lose all your gold.",
                EventEffect.HORROR_SURRENDER),
            new EventChoice("Resist", "You lose all your echoes.",
                EventEffect.HORROR_RESIST)
        )),

    INFAMOUS_JUNCTION("Once Upon a Time at an Infamous Junction",
        "A hard little town squats at the edge of the badlands, all splintered porches, swinging saloon doors, and watchful eyes behind dusty windows. " +
            "The posters are fresh, the sheriff is outnumbered, and every soul in town looks like they've already chosen a side.",
        List.of(
            new EventChoice("Raise a Gang", "By sundown your crew prepared for life as outlaws, ready to take what's rightfully theirs.",
                EventEffect.INFAMOUS_JUNCTION_RAISE_GANG),
            new EventChoice("Rob the Local Bank", "The alarm comes late, the horses come fast, and by the time the town gives chase you're already riding richer.",
                EventEffect.INFAMOUS_JUNCTION_ROB_BANK),
            new EventChoice("Rope the Lost Cattle", "Out in the scrub you find more than strays, and one steady mount chooses to follow you back.",
                EventEffect.INFAMOUS_JUNCTION_ROPE_CATTLE)
        )),

    LOST_NOT_FORGOTTEN("Lost, But not Forgotten",
        "Torchlight gutters in the dungeon's stale air as the corridors close around you, slick with age, moss, and old blood. Somewhere ahead, chains rattle, stones shift, and the dark promises either treasure or a grave no one will remember.",
        List.of(
            new EventChoice("Stumble Into Party", "Four seasoned adventurers emerge from the gloom as though they had been tracking your steps for miles.",
                EventEffect.LOST_NOT_FORGOTTEN_PARTY),
            new EventChoice("Level Up", "By torchlight and hard-won instinct, you refine a few more tricks fit for the depths below.",
                EventEffect.LOST_NOT_FORGOTTEN_LEVEL_UP),
            new EventChoice("Venture deeper", "You press onward into the dark, where every chamber holds either fortune or ruin.",
                EventEffect.LOST_NOT_FORGOTTEN_VENTURE_DEEPER)
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
    },

    MERCHANT_CARAVAN("Merchant Caravan",
        "A caravan of planar merchants sets up shop.",
        List.of(
            new EventChoice("Browse Their Wares", "The merchant thanks you for the business.",
                EventEffect.CARAVAN_BROWSE),
            new EventChoice("Rob Them", "You take what you want by force.",
                EventEffect.CARAVAN_ROB)
        )),

    NEON_LID("Neon-Lid",
        "Neon rain hisses across Kamigawa's midnight streets while shrine lights shimmer through the smog. Ancient vows and chrome temptations call you toward three different paths.",
        List.of(
            new EventChoice("Path of the Samurai", "You trade flesh for discipline and rebuild your deck around the blade.",
                EventEffect.NEON_LID_SAMURAI),
            new EventChoice("Path of the Ninja", "You vanish into the alleys and return deadlier than ever before.",
                EventEffect.NEON_LID_NINJA),
            new EventChoice("Path of Inner Peace", "You follow the glow of the shrines and let their wisdom settle over you.",
                EventEffect.NEON_LID_SHRINE)
        )),

    ON_THE_EDGE("On the Edge",
        "A city-sized spacecraft drifts ahead of you in the black, its hull lit by running lights that still burn despite the silence. " +
            "Scorched docking rings, torn cargo fins, and a thousand dead windows make it look abandoned, but the ship is still moving just enough to feel dangerous.",
        List.of(
            new EventChoice("Hijack", "You catch the drifting craft before it shears away and make it your own.",
                EventEffect.ON_THE_EDGE_HIJACK),
            new EventChoice("Board", "You invoke the ship's still functioning hyperdrive come back out somewhere else on the route entirely.",
                EventEffect.ON_THE_EDGE_BOARD),
            new EventChoice("Scavenge", "The wreck is full of spent frames and stubborn machine minds, and a few still wake when you pry them loose.",
                EventEffect.ON_THE_EDGE_SCAVENGE)
        )),

    PLANAR_EXCHANGE("Planar Exchange",
        "A shimmering portal offers to reshape your arsenal.",
        List.of(
            new EventChoice("Step Through", "The planes shift your deck...",
                EventEffect.PLANAR_EXCHANGE_EXCHANGE),
            new EventChoice("Stay Put", "You decide not to risk it.",
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

    PLANAR_TRIBUTE("Planar Tribute",
        "The planes demand tribute.",
        List.of(
            new EventChoice("Sacrifice", "The planes took what is yours.",
                EventEffect.PLANAR_TRIBUTE_REMOVE),
            new EventChoice("Give and take", "The planes reshaped your arsenal.",
                EventEffect.PLANAR_TRIBUTE_REPLACE)
        )),

    SATCHEL("Satchel",
        "Hidden beneath a crumbled pillar, you find a satchel of cards bound in leather; remnants of a Planeswalker who walked here before you.",
        List.of(
            new EventChoice("Open the Satchel", "You find a hidden chest.",
                EventEffect.SATCHEL_OPEN)
        )),

    SHRINE("Shrine",
        "A crumbling shrine pulses faintly with restorative energy. As you kneel, you immediately feel its soothing powers.",
        List.of(
            new EventChoice("Kneel", "You discover a hidden Sanctum.",
                EventEffect.SHRINE_KNEEL)
        )),

    THORNS("Thorns",
        "The land itself rejects your presence. Thorns rise from the soil, leaving your footsteps stained with blood.",
        List.of(
            new EventChoice("Endure", "You suffer a wound.",
                EventEffect.THORNS_ENDURE),
            new EventChoice("Press On", "You lose 4 life.",
                EventEffect.THORNS_PRESS)
        )),

    TRAPPED_IN_THE_LAIR("Trapped in the Lair",
        "You stumble through a tunnel of slick black flesh and jagged bone, only to realize the lair is breathing around you. " +
            "Somewhere deeper inside, something enormous shifts in the dark, and every path forward feels like a step into its hunger.",
        List.of(
            new EventChoice("Slay", "You carve your way out through blood and ruin, carrying what the beast had already devoured.",
                EventEffect.TRAPPED_IN_THE_LAIR_SLAY),
            new EventChoice("Tame", "You lower your guard for a heartbeat, and one ancient terror answers with wary obedience.",
                EventEffect.TRAPPED_IN_THE_LAIR_TAME),
            new EventChoice("Examine", "You study the lair's pulsing growths and leave forever altered by what they reveal.",
                EventEffect.TRAPPED_IN_THE_LAIR_EXAMINE)
        )),

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
        ));

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
