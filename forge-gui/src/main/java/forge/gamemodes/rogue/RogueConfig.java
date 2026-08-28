package forge.gamemodes.rogue;

import forge.CardStorageReader;
import forge.StaticData;
import forge.card.CardEdition;
import forge.card.CardRarity;
import forge.card.CardRules;
import forge.deck.CardPool;
import forge.deck.Deck;
import forge.deck.DeckFormat;
import forge.deck.DeckSection;
import forge.deck.io.DeckSerializer;
import forge.item.PaperCard;
import forge.localinstance.properties.ForgeConstants;
import forge.model.FModel;
import forge.util.FileSection;
import forge.util.FileUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Predicate;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Configuration for Rogue Commander mode.
 * Decks are loaded from res/rogue/commanders/ directory.
 * Paths are generated for each run.
 */
public class RogueConfig {
    private static final Set<String> EXCLUDED_RANDOM_DRAW_SET_CODES = Set.of("UNK", "UNH");

    private static final StaticData db = StaticData.instance();

    // Cache all plane cards to avoid reloading them repeatedly
    private static CardPool cachedPlanarPool = null;
    private static List<PaperCard> cachedGamechangerCards = null;
    private static List<PaperCard> cachedCommanderBanlistCards = null;

    private static boolean rogueCardsLoaded = false;

    /**
     * Load Rogue Commander-specific card scripts from res/rogue/cards/ into the live card DB.
     * Mirrors the pattern used by Adventure mode's custom_cards. Safe to call multiple times.
     */
    public static void loadRogueCards() {
        if (rogueCardsLoaded) return;
        rogueCardsLoaded = true;

        File cardsDir = new File(ForgeConstants.RES_DIR, "rogue/cards");
        if (!cardsDir.exists() || !cardsDir.isDirectory()) return;

        File[] cardFiles = cardsDir.listFiles((dir, name) -> name.endsWith(".txt"));
        if (cardFiles == null) return;

        CardRules.Reader rulesReader = new CardRules.Reader();
        for (File cardFile : cardFiles) {
            try (FileInputStream fis = new FileInputStream(cardFile);
                 InputStreamReader isr = new InputStreamReader(fis, Charset.forName(CardStorageReader.DEFAULT_CHARSET_NAME))) {
                rulesReader.reset();
                List<String> lines = FileUtil.readAllLines(isr, true);
                String name = cardFile.getName();
                String baseName = name.endsWith(".txt") ? name.substring(0, name.length() - 4) : name;
                CardRules rules = rulesReader.readCard(lines, baseName);
                rules.setCustom();
                PaperCard card = new PaperCard(rules, CardEdition.UNKNOWN_CODE, CardRarity.Special);
                (rules.isVariant() ? db.getVariantCards() : db.getCommonCards()).addCard(card);
            } catch (Exception e) {
                System.err.println("Error loading rogue card " + cardFile.getName() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Load all available Rogue Decks from the commanders directory.
     * Scans for .dck files and their corresponding _rewards.dck and .meta files.
     */
    public static List<RogueDeck> loadRogueDecks() {
        List<RogueDeck> decks = new ArrayList<>();
        File commanderDir = new File(ForgeConstants.RES_DIR, "rogue/commanders");

        if (!commanderDir.exists() || !commanderDir.isDirectory()) {
            System.err.println("Warning: Rogue commanders directory not found: " + commanderDir.getAbsolutePath());
            return decks;
        }

        File[] files = commanderDir.listFiles((dir, name) ->
                name.endsWith(".dck") && !name.endsWith("_rewards.dck"));

        if (files == null || files.length == 0) {
            System.err.println("Warning: No commander deck files found in " + commanderDir.getAbsolutePath());
            return decks;
        }

        for (File deckFile : files) {
            try {
                RogueDeck rogueDeck = loadRogueDeckFromFile(deckFile);
                if (rogueDeck != null) {
                    decks.add(rogueDeck);
                }
            } catch (Exception e) {
                System.err.println("Error loading rogue deck from " + deckFile.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        return decks;
    }

    /**
     * Load a single Rogue Deck from a file.
     * Expected files: [name].dck, [name]_rewards.dck, [name].meta
     */
    private static RogueDeck loadRogueDeckFromFile(File deckFile) throws IOException {
        // Extract base name (e.g., "anim_pakal" from "anim_pakal.dck")
        String baseName = deckFile.getName().replace(".dck", "");

        // Load start deck
        Deck startDeck = DeckSerializer.fromFile(deckFile);
        if (startDeck == null) {
            System.err.println("Warning: Failed to load deck from " + deckFile.getName());
            return null;
        }

        // Load rewards deck
        File rewardsFile = new File(deckFile.getParent(), baseName + "_rewards.dck");
        Deck rewardsDeck = null;
        if (rewardsFile.exists()) {
            rewardsDeck = DeckSerializer.fromFile(rewardsFile);
        }

        // Load metadata
        File metaFile = new File(deckFile.getParent(), baseName + ".meta");
        Properties meta = new Properties();
        if (metaFile.exists()) {
            try (FileInputStream fis = new FileInputStream(metaFile)) {
                meta.load(fis);
            }
        }

        // Create RogueDeckData
        RogueDeck rogueDeck = new RogueDeck();
        rogueDeck.setName(startDeck.getName());
        rogueDeck.setStartDeck(startDeck);

        // Extract commander name from Commander section
        if (startDeck.has(DeckSection.Commander)) {
            CardPool commanders = startDeck.get(DeckSection.Commander);
            if (commanders != null && !commanders.isEmpty()) {
                rogueDeck.setCommanderCardName(commanders.toFlatList().get(0).getName());
            }
        }

        // Set reward pool from rewards deck
        if (rewardsDeck != null) {
            CardPool rewardPool = new CardPool();
            rewardPool.addAll(rewardsDeck.getMain());
            rogueDeck.setRewardPool(rewardPool);
        }

        // Set metadata
        rogueDeck.setDescription(meta.getProperty("description", ""));
        rogueDeck.setThemeDescription(meta.getProperty("theme", ""));
        rogueDeck.setAvatarIndex(Integer.parseInt(meta.getProperty("avatarIndex", "1")));
        rogueDeck.setSleeveIndex(Integer.parseInt(meta.getProperty("sleeveIndex", "1")));
        rogueDeck.setLandEdition(meta.getProperty("landEdition", ""));
        rogueDeck.setIncludeColorlessBasics(Boolean.parseBoolean(
            meta.getProperty("includeColorlessBasics", "false")));

        // Set unlock condition
        String unlockString = meta.getProperty("unlock", null);
        if (unlockString != null && !unlockString.isEmpty()) {
            rogueDeck.setUnlockCondition(new RogueUnlockCondition(unlockString));
        }

        return rogueDeck;
    }

    /**
     * Get all plane cards from the variant cards collection.
     * Returns a cached CardPool to avoid reloading planes repeatedly.
     * This method is thread-safe and lazy-loads the planes on first call.
     *
     * @return CardPool containing all available plane cards
     */
    public static CardPool getAllPlanes() {
        if (cachedPlanarPool == null) {
            cachedPlanarPool = new CardPool();

            // Search variant cards for planes
            for (PaperCard card : db.getVariantCards().getAllCards()) {
                if (card.getRules().getType().isPlane()) {
                    cachedPlanarPool.add(card);
                }
            }
        }
        return cachedPlanarPool;
    }

    /**
     * Load the shared Gamechanger card pool from res/rogue/util/gamechangers.dck.
     * Returns a cached copy to avoid repeated deck file parsing.
     */
    public static List<PaperCard> getGamechangerCards() {
        if (cachedGamechangerCards == null) {
            File deckFile = new File(ForgeConstants.RES_DIR, "rogue/util/gamechangers.dck");
            if (!deckFile.exists()) {
                System.err.println("Warning: Gamechanger deck not found: "
                    + deckFile.getAbsolutePath());
                cachedGamechangerCards = List.of();
            } else {
                Deck deck = DeckSerializer.fromFile(deckFile);
                if (deck == null) {
                    System.err.println("Warning: Failed to load Gamechanger deck from "
                        + deckFile.getAbsolutePath());
                    cachedGamechangerCards = List.of();
                } else {
                    cachedGamechangerCards = new ArrayList<>(deck.getMain().toFlatList());
                }
            }
        }
        return new ArrayList<>(cachedGamechangerCards);
    }

    /**
     * Load the shared Commander banlist card pool from res/rogue/util/commander_banlist.dck.
     * Returns a cached copy to avoid repeated deck file parsing.
     */
    public static List<PaperCard> getCommanderBanlistCards() {
        if (cachedCommanderBanlistCards == null) {
            File deckFile = new File(ForgeConstants.RES_DIR, "rogue/util/commander_banlist.dck");
            if (!deckFile.exists()) {
                System.err.println("Warning: Commander banlist deck not found: "
                    + deckFile.getAbsolutePath());
                cachedCommanderBanlistCards = List.of();
            } else {
                Deck deck = DeckSerializer.fromFile(deckFile);
                if (deck == null) {
                    System.err.println("Warning: Failed to load Commander banlist deck from "
                        + deckFile.getAbsolutePath());
                    cachedCommanderBanlistCards = List.of();
                } else {
                    cachedCommanderBanlistCards = new ArrayList<>(deck.getMain().toFlatList());
                }
            }
        }
        return new ArrayList<>(cachedCommanderBanlistCards);
    }

    /**
     * Load all available Planebound configurations from the planebounds directory.
     * Scans for .dck files and loads their metadata.
     */
    public static List<RoguePlanebound> loadPlanebounds() {
        List<RoguePlanebound> planebounds = new ArrayList<>();
        File planeboundsDir = new File(ForgeConstants.RES_DIR, "rogue/planebounds");

        if (!planeboundsDir.exists() || !planeboundsDir.isDirectory()) {
            System.err.println("Warning: Rogue planebounds directory not found: " + planeboundsDir.getAbsolutePath());
            return planebounds;
        }

        File[] files = planeboundsDir.listFiles((dir, name) -> name.endsWith(".dck"));

        if (files == null || files.length == 0) {
            System.err.println("Warning: No planebound deck files found in " + planeboundsDir.getAbsolutePath());
            return planebounds;
        }

        for (File deckFile : files) {
            try {
                RoguePlanebound planebound = loadPlaneboundFromFile(deckFile);
                if (planebound != null) {
                    planebounds.add(planebound);
                }
            } catch (Exception e) {
                System.err.println("Error loading planebound from " + deckFile.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        return planebounds;
    }

    /**
     * Load a single Planebound configuration from a deck file.
     * Reads metadata including name, planeName, and avatarIndex.
     */
    private static RoguePlanebound loadPlaneboundFromFile(File deckFile) {
        // Parse deck file sections
        Map<String, List<String>> sections = FileSection.parseSections(FileUtil.readFile(deckFile));
        if (sections.isEmpty()) {
            System.err.println("Warning: Failed to parse deck file " + deckFile.getName());
            return null;
        }

        // Extract metadata section
        List<String> metadataLines = sections.get("metadata");
        if (metadataLines == null || metadataLines.isEmpty()) {
            System.err.println("Warning: No metadata section found in " + deckFile.getName());
            return null;
        }

        // Parse metadata as key-value pairs
        FileSection metadata = FileSection.parse(metadataLines, FileSection.EQUALS_KV_SEPARATOR);

        // Extract required metadata
        String planeboundName = metadata.get("name", "Unknown Planebound");
        String planeName = metadata.get("planeName", "Unknown Plane");
        int avatarIndex = metadata.getInt("avatarIndex", 1);
        int typeIndex = metadata.getInt("type", 0); // Default to NORMAL (0)
        RoguePlaneboundType type = RoguePlaneboundType.fromIndex(typeIndex);

        // Build relative deck path
        String deckPath = "rogue/planebounds/" + deckFile.getName();

        return new RoguePlanebound(planeName, planeboundName, deckPath, avatarIndex, type);
    }

    public static Deck loadPlaneboundDeck(RoguePlanebound planebound) {
        if (planebound == null || planebound.deckPath() == null || planebound.deckPath().isBlank()) {
            return null;
        }

        File deckFile = new File(ForgeConstants.RES_DIR, planebound.deckPath());
        if (!deckFile.exists()) {
            System.err.println("Warning: Planebound deck not found: " + deckFile.getAbsolutePath());
            return null;
        }
        return DeckSerializer.fromFile(deckFile);
    }

    // Helper method to get all unique cards from the database
    public static List<PaperCard> getAllCards() {
        return getAllCards(null);
    }

    public static List<PaperCard> getAllCards(Predicate<PaperCard> filter) {
        Predicate<PaperCard> effectiveFilter = getAllFilters(filter);

        return FModel.getMagicDb().getCommonCards().getUniqueCards().stream()
                .filter(effectiveFilter)
                .toList();
    }

    /**
     * Returns a rules-name print for flavor-name cards while leaving ordinary prints unchanged.
     */
    public static PaperCard getRulesNamePrint(PaperCard card) {
        return card != null && card.hasFlavorName() ? getCard(card.getName(), null, null) : card;
    }

    /**
     * Returns the preferred Rogue display print for a selected card without changing editions.
     * Prefers non-flavor-name prints with earlier collector numbers in the same edition.
     */
    public static PaperCard getPreferredPrint(PaperCard card) {
        PaperCard rulesNamePrint = getRulesNamePrint(card);
        if (rulesNamePrint == null) {
            return null;
        }

        return FModel.getMagicDb().getCommonCards()
                .getAllCards(rulesNamePrint).stream()
                .filter(candidate -> candidate.getEdition().equals(rulesNamePrint.getEdition()))
                .filter(candidate -> !candidate.hasFlavorName())
                .min(Comparator.comparing(PaperCard::getCollectorNumberSortingKey))
                .orElse(rulesNamePrint);
    }

    private static @NonNull Predicate<PaperCard> getAllFilters(Predicate<PaperCard> filter) {
        Predicate<PaperCard> rogueBaseFilter =
            card -> !card.getRules().isCustom()
                && !EXCLUDED_RANDOM_DRAW_SET_CODES.contains(card.getEdition());
        Predicate<PaperCard> nonDigitalFilter =
            card -> {
                CardEdition edition = db.getCardEdition(card.getEdition());
                return !card.isRebalanced()
                    && edition != null
                    && edition.getType() != CardEdition.Type.ONLINE
                    && edition.getType() != CardEdition.Type.FUNNY;
            };
        Predicate<PaperCard> commanderLegalFilter = DeckFormat.RogueCommander::isLegalCard;
        return filter != null
                ? rogueBaseFilter.and(nonDigitalFilter).and(commanderLegalFilter).and(filter)
                : rogueBaseFilter.and(nonDigitalFilter).and(commanderLegalFilter);
    }

    /**
     * Helper method to get cards from the database.
     * setCode and artIndex may be null to ignore edition or art selection.
     */
    public static PaperCard getCard(String cardName, String setCode, Integer artIndex) {
        loadRogueCards();
        PaperCard card;
        if (setCode == null || setCode.isEmpty()) {
            card = db.getCommonCards().getCard(cardName);
        } else if (artIndex == null) {
            card = db.getCommonCards().getCard(cardName, setCode);
        } else {
            card = db.getCommonCards().getCard(cardName, setCode, artIndex);
        }
        if (card == null) {
            System.err.println("Warning: Card not found: " + cardName);
            // Return a basic land as fallback
            return db.getCommonCards().getCard("Plain");
        }
        return card;
    }
}
