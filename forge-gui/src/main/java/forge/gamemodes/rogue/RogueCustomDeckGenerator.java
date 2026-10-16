package forge.gamemodes.rogue;

import forge.card.CardRarity;
import forge.card.ColorSet;
import forge.card.MagicColor;
import forge.deck.CardPool;
import forge.deck.CardRelationMatrixGenerator;
import forge.deck.CommanderBracketCalculator;
import forge.deck.Deck;
import forge.deck.DeckFormat;
import forge.deck.DeckSection;
import forge.deck.DeckgenUtil;
import forge.item.PaperCard;
import forge.model.FModel;
import forge.util.MyRandom;
import org.tinylog.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/** Builds a small, playable Rogue deck from Forge's Commander deck generator. */
public final class RogueCustomDeckGenerator {
    private RogueCustomDeckGenerator() {
    }

    public static List<PaperCard> getEligibleCommanders() {
        Set<String> commanderPoolNames = new HashSet<>();
        FModel.getCommanderPool().forEach(entry -> commanderPoolNames.add(entry.getKey().getName()));
        Map<String, PaperCard> commanders = new LinkedHashMap<>();
        for (PaperCard card : RogueConfig.getAllCards(candidate ->
            commanderPoolNames.contains(candidate.getName())
                && candidate.getRules().getType().isLegendary()
                && candidate.getRules().getType().isCreature()
                && DeckFormat.Commander.isLegalCard(candidate)
                && DeckFormat.Commander.isLegalCommander(candidate.getRules())
                && !candidate.getName().startsWith("A-"))) {
            commanders.putIfAbsent(card.getName(), card);
        }
        return new ArrayList<>(commanders.values());
    }

    public static RogueDeck generate() {
        List<PaperCard> commanders = getEligibleCommanders();
        Collections.shuffle(commanders, MyRandom.getRandom());

        Map<String, List<Map.Entry<PaperCard, Integer>>> matrix = getSynergyMatrix();
        for (int i = 0; i < Math.min(8, commanders.size()); i++) {
            RogueDeck deck = tryGenerate(commanders.get(i), matrix, true);
            if (deck != null) {
                return deck;
            }
        }
        return null;
    }

    public static RogueDeck generateForChosenCommander(PaperCard chosenCommander) {
        if (chosenCommander == null) {
            return null;
        }
        PaperCard commander = getEligibleCommanders().stream()
            .filter(card -> card.getName().equals(chosenCommander.getName()))
            .findFirst().orElse(null);
        if (commander == null) {
            return null;
        }

        Map<String, List<Map.Entry<PaperCard, Integer>>> matrix = getSynergyMatrix();
        for (int i = 0; i < 8; i++) {
            RogueDeck deck = tryGenerate(commander, matrix, false);
            if (deck != null) {
                return deck;
            }
        }
        return null;
    }

    public static RogueDeck generateStartingDeck(RogueDeck selectedDeck) {
        return generateModifiedDeck(selectedDeck, true);
    }

    public static RogueDeck generateRewardPool(RogueDeck selectedDeck) {
        return generateModifiedDeck(selectedDeck, false);
    }

    private static RogueDeck generateModifiedDeck(RogueDeck selectedDeck,
            boolean replaceStartingDeck) {
        if (selectedDeck == null || selectedDeck.getStartDeck() == null
            || selectedDeck.getStartDeck().getCommanders().size() != 1) {
            return null;
        }
        PaperCard commander = selectedDeck.getStartDeck().getCommanders().get(0);
        List<Map.Entry<PaperCard, Integer>> synergy = getCommanderSynergy(commander);
        for (int i = 0; i < 8; i++) {
            RogueDeck deck = tryGenerateModifiedDeck(selectedDeck, commander, synergy,
                replaceStartingDeck);
            if (deck != null) {
                return deck;
            }
        }
        return null;
    }

    private static RogueDeck tryGenerateModifiedDeck(RogueDeck selectedDeck, PaperCard commander,
            List<Map.Entry<PaperCard, Integer>> synergy, boolean replaceStartingDeck) {
        try {
            DeckGenerationContext context = prepareDeckGenerationContext(commander, synergy);
            RogueDeck deck = selectedDeck.copy();
            if (replaceStartingDeck) {
                Deck start = buildStartingDeck(context, selectedDeck.getName(),
                    selectedDeck.getLandEdition());
                if (start == null || isStartingDeckInvalid(context, start)) {
                    return null;
                }
                deck.setStartDeck(start);
            } else {
                CardPool rewards = buildRewardPool(context, deck.getStartDeck());
                if (isRewardPoolInvalid(rewards)) {
                    return null;
                }
                deck.setRewardPool(rewards);
            }
            return deck;
        } catch (RuntimeException e) {
            Logger.warn(e, "Could not generate custom deck part for {}", commander.getName());
            return null;
        }
    }

    private static Map<String, List<Map.Entry<PaperCard, Integer>>> getSynergyMatrix() {
        if (!CardRelationMatrixGenerator.cardPools.containsKey(DeckFormat.Commander.toString())) {
            try {
                CardRelationMatrixGenerator.initializeFormat(DeckFormat.Commander);
            } catch (RuntimeException e) {
                Logger.warn(e, "Commander synergy data unavailable; using color-identity generation");
            }
        }
        return CardRelationMatrixGenerator.cardPools.get(DeckFormat.Commander.toString());
    }

    private static List<Map.Entry<PaperCard, Integer>> getCommanderSynergy(PaperCard commander) {
        Map<String, List<Map.Entry<PaperCard, Integer>>> matrix = getSynergyMatrix();
        return matrix == null ? List.of() : matrix.getOrDefault(commander.getName(), List.of());
    }

    private static RogueDeck tryGenerate(PaperCard commander,
            Map<String, List<Map.Entry<PaperCard, Integer>>> matrix, boolean randomlyChosen) {
        List<Map.Entry<PaperCard, Integer>> synergy = matrix == null
            ? List.of() : matrix.getOrDefault(commander.getName(), List.of());
        try {
            return generateForCommander(commander, synergy, randomlyChosen);
        } catch (RuntimeException e) {
            Logger.warn(e, "Could not generate Rogue deck for {}", commander.getName());
            return null;
        }
    }

    private static RogueDeck generateForCommander(PaperCard commander,
            List<Map.Entry<PaperCard, Integer>> synergy, boolean randomlyChosen) {
        DeckGenerationContext context = prepareDeckGenerationContext(commander, synergy);
        Deck start = buildStartingDeck(context,
            (randomlyChosen ? "Random " : "Chosen ") + commander.getName(), null);
        if (start == null || isStartingDeckInvalid(context, start)) {
            return null;
        }
        CardPool rewardPool = buildRewardPool(context, start);
        if (isRewardPoolInvalid(rewardPool)) {
            return null;
        }

        RogueDeck rogueDeck = new RogueDeck();
        rogueDeck.setName(start.getName());
        rogueDeck.setCommanderCardName(commander.getName());
        rogueDeck.setStartDeck(start);
        rogueDeck.setRewardPool(rewardPool);
        rogueDeck.setDescription(randomlyChosen ? "A random Commander and starting deck."
            : "A chosen Commander and generated starting deck.");
        rogueDeck.setThemeDescription("Generated from Commander synergy and color identity.");
        rogueDeck.setAvatarIndex(125);
        rogueDeck.setSleeveIndex(1);
        rogueDeck.setLandEdition("");
        rogueDeck.setIncludeColorlessBasics(commander.getRules().getColorIdentity().isColorless());
        return rogueDeck;
    }

    private record DeckGenerationContext(PaperCard commander, List<PaperCard> startingCards,
            Map<String, PaperCard> startingByName, List<PaperCard> eligibleCards,
            Map<String, PaperCard> eligibleByName, LinkedHashMap<String, PaperCard> generatedSpells,
            LinkedHashMap<String, PaperCard> generatedRewardSpells,
            LinkedHashMap<String, PaperCard> generatedLands,
            List<Map.Entry<PaperCard, Integer>> rankedSynergy, int maxBracket) {}

    private static DeckGenerationContext prepareDeckGenerationContext(PaperCard commander,
            List<Map.Entry<PaperCard, Integer>> synergy) {
        Predicate<PaperCard> colorIdentity = card -> card.getRules().getColorIdentity()
            .hasNoColorsExcept(commander.getRules().getColorIdentity());
        Predicate<PaperCard> eligible = card ->
            DeckFormat.Commander.isLegalCard(card) && colorIdentity.test(card)
                && !card.getName().equals(commander.getName());
        String preferredEdition = commander.getEdition();
        List<PaperCard> eligibleCards = new ArrayList<>(
            RogueConfig.getRandomCardsForEdition(eligible, preferredEdition));
        List<PaperCard> startingCards = getStartingPrints(eligibleCards, eligible, preferredEdition);
        Set<String> excludedStartingCardNames = Set.of("Sol Ring", "Arcane Signet");
        startingCards.removeIf(card -> excludedStartingCardNames.contains(card.getName()));
        Map<String, PaperCard> eligibleByName = indexByName(eligibleCards);
        Map<String, PaperCard> startingByName = indexByName(startingCards);

        int commanderBracket = CommanderBracketCalculator.calculate(
            CommanderBracketCalculator.getCardNames(commander)).getBracket();
        int maxBracket = Math.max(2, commanderBracket);
        Deck generated = generateCommanderDeck(commander, synergy, maxBracket);

        LinkedHashMap<String, PaperCard> generatedSpells = new LinkedHashMap<>();
        LinkedHashMap<String, PaperCard> generatedRewardSpells = new LinkedHashMap<>();
        LinkedHashMap<String, PaperCard> generatedLands = new LinkedHashMap<>();
        for (PaperCard card : generated.getMain().toFlatList()) {
            addGeneratedCard(card, startingByName, eligibleByName, generatedSpells,
                generatedRewardSpells, generatedLands);
        }

        List<Map.Entry<PaperCard, Integer>> rankedSynergy = new ArrayList<>(synergy);
        Collections.shuffle(rankedSynergy, MyRandom.getRandom());
        rankedSynergy.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        return new DeckGenerationContext(commander, startingCards, startingByName, eligibleCards,
            eligibleByName, generatedSpells, generatedRewardSpells, generatedLands,
            rankedSynergy, maxBracket);
    }

    private static List<PaperCard> getStartingPrints(List<PaperCard> eligibleCards,
            Predicate<PaperCard> eligible, String preferredEdition) {
        List<PaperCard> startingCards = new ArrayList<>();
        for (PaperCard card : eligibleCards) {
            PaperCard startingCard = card.getRarity() == CardRarity.MythicRare
                ? RogueConfig.getNonMythicRandomPrint(card, eligible, preferredEdition) : card;
            if (startingCard != null) {
                startingCards.add(startingCard);
            }
        }
        return startingCards;
    }

    private static Map<String, PaperCard> indexByName(List<PaperCard> cards) {
        Map<String, PaperCard> byName = new HashMap<>();
        for (PaperCard card : cards) {
            byName.putIfAbsent(card.getName(), card);
        }
        return byName;
    }

    private static Deck generateCommanderDeck(PaperCard commander,
            List<Map.Entry<PaperCard, Integer>> synergy, int maxBracket) {
        Deck generated = null;
        if (!synergy.isEmpty()) {
            try {
                generated = DeckgenUtil.generateRandomCommanderDeck(commander,
                    DeckFormat.Commander, false, true, maxBracket);
            } catch (RuntimeException e) {
                Logger.warn(e, "Commander synergy generation failed for {}; using color-identity generation",
                    commander.getName());
            }
        }
        if (generated == null || generated.getMain().countAll() == 0) {
            generated = DeckgenUtil.generateRandomCommanderDeck(commander,
                DeckFormat.Commander, false, false, maxBracket);
        }
        return generated;
    }

    private static void addGeneratedCard(PaperCard card, Map<String, PaperCard> startingByName,
            Map<String, PaperCard> eligibleByName, Map<String, PaperCard> generatedSpells,
            Map<String, PaperCard> generatedRewardSpells, Map<String, PaperCard> generatedLands) {
        PaperCard eligibleCard = eligibleByName.get(card.getName());
        if (eligibleCard != null && !eligibleCard.getRules().getType().isLand()) {
            generatedRewardSpells.putIfAbsent(eligibleCard.getName(), eligibleCard);
        }
        PaperCard startingCard = startingByName.get(card.getName());
        if (startingCard == null) {
            return;
        }
        if (startingCard.getRules().getType().isLand()) {
            if (!startingCard.getRules().getType().isBasicLand()) {
                generatedLands.putIfAbsent(startingCard.getName(), startingCard);
            }
        } else {
            generatedSpells.putIfAbsent(startingCard.getName(), startingCard);
        }
    }

    private static Deck buildStartingDeck(DeckGenerationContext context, String name,
            String landEdition) {
        Set<String> supportingNames = new HashSet<>();
        Set<String> completeNames = new HashSet<>(
            CommanderBracketCalculator.getCardNames(context.commander()));
        Map<String, PaperCard> spells = selectStartingSpells(context, supportingNames, completeNames);
        if (spells.size() != 19) {
            return null;
        }
        List<PaperCard> lands = selectStartingLands(context, supportingNames, completeNames,
            landEdition);
        if (lands.isEmpty()) {
            return null;
        }

        Deck start = new Deck(name);
        start.getOrCreate(DeckSection.Commander).add(context.commander());
        for (PaperCard card : spells.values()) {
            start.getMain().add(card);
        }
        for (PaperCard card : lands) {
            start.getMain().add(card);
        }
        return start;
    }

    private static Map<String, PaperCard> selectStartingSpells(DeckGenerationContext context,
            Set<String> supportingNames, Set<String> completeNames) {
        LinkedHashMap<String, PaperCard> spells = new LinkedHashMap<>();
        addHighCurveSynergySpells(spells, supportingNames, completeNames, context);

        List<PaperCard> spellCandidates = new ArrayList<>(context.generatedSpells().values());
        Collections.shuffle(spellCandidates, MyRandom.getRandom());
        spellCandidates.sort(Comparator.comparingInt(card -> card.getRules().getManaCost().getCMC()));
        addSpells(spells, spellCandidates, supportingNames, completeNames, context.maxBracket());
        if (spells.size() < 19) {
            List<PaperCard> fallback = getSynergyFallbackSpells(context);
            Collections.shuffle(fallback, MyRandom.getRandom());
            addSpells(spells, fallback, supportingNames, completeNames, context.maxBracket());
        }
        if (spells.size() < 19) {
            List<PaperCard> startingCards = new ArrayList<>(context.startingCards());
            Collections.shuffle(startingCards, MyRandom.getRandom());
            addSpells(spells, startingCards, supportingNames, completeNames,
                context.maxBracket());
        }
        return spells;
    }

    private static void addHighCurveSynergySpells(Map<String, PaperCard> spells,
            Set<String> supportingNames, Set<String> completeNames, DeckGenerationContext context) {
        for (Map.Entry<PaperCard, Integer> entry : context.rankedSynergy()) {
            PaperCard card = context.generatedSpells().get(entry.getKey().getName());
            if (card != null && card.getRules().getManaCost().getCMC() >= 4
                && card.getRules().getManaCost().getCMC() <= 6) {
                addSupportingCard(spells, card, supportingNames, completeNames,
                    context.maxBracket());
                if (spells.size() == 4) {
                    break;
                }
            }
        }
    }

    private static List<PaperCard> getSynergyFallbackSpells(DeckGenerationContext context) {
        List<PaperCard> fallback = new ArrayList<>();
        for (Map.Entry<PaperCard, Integer> entry : context.rankedSynergy()) {
            PaperCard card = context.startingByName().get(entry.getKey().getName());
            if (card != null) {
                fallback.add(card);
            }
        }
        return fallback;
    }

    private static List<PaperCard> selectStartingLands(DeckGenerationContext context,
            Set<String> supportingNames, Set<String> completeNames, String landEdition) {
        List<PaperCard> nonbasicLands = new ArrayList<>(context.generatedLands().values());
        Collections.shuffle(nonbasicLands, MyRandom.getRandom());
        ColorSet commanderColors = context.commander().getRules().getColorIdentity();
        nonbasicLands.sort(Comparator.comparingInt(
            (PaperCard card) -> fixingColors(card, commanderColors)).reversed());
        int nonbasicLimit = switch (commanderColors.countColors()) {
            case 3 -> 8;
            case 4 -> 10;
            case 5 -> 9;
            default -> 6;
        };
        List<PaperCard> lands = new ArrayList<>();
        for (PaperCard card : nonbasicLands) {
            if (canAddSupportingCard(card, supportingNames, completeNames, context.maxBracket())) {
                lands.add(card);
                supportingNames.addAll(CommanderBracketCalculator.getCardNames(card));
                completeNames.addAll(CommanderBracketCalculator.getCardNames(card));
            }
            if (lands.size() == nonbasicLimit) {
                break;
            }
        }
        if (!addBasicLands(lands, commanderColors,
            RogueConfig.getBasicLandEdition(landEdition, context.commander()))) {
            return Collections.emptyList();
        }
        return lands;
    }

    private static CardPool buildRewardPool(DeckGenerationContext context, Deck start) {
        List<PaperCard> eligibleCards = new ArrayList<>(context.eligibleCards());
        Set<String> used = new HashSet<>();
        for (PaperCard card : start.getMain().toFlatList()) {
            used.add(card.getName());
        }
        CardPool rewardPool = new CardPool();
        addSynergyRewards(rewardPool, used, context);
        Collections.shuffle(eligibleCards, MyRandom.getRandom());
        addMythicRewards(rewardPool, used, eligibleCards);
        addRewardsUpToLimit(rewardPool, used, context.generatedRewardSpells().values());
        Collections.shuffle(eligibleCards, MyRandom.getRandom());
        addRewardsUpToLimit(rewardPool, used, eligibleCards);
        return rewardPool;
    }

    private static void addSynergyRewards(CardPool rewardPool, Set<String> used,
            DeckGenerationContext context) {
        int synergyCount = 0;
        for (Map.Entry<PaperCard, Integer> entry : context.rankedSynergy()) {
            PaperCard card = context.eligibleByName().get(entry.getKey().getName());
            if (addReward(rewardPool, used, card)) {
                synergyCount++;
            }
            if (synergyCount == 120) {
                break;
            }
        }
    }

    private static void addMythicRewards(CardPool rewardPool, Set<String> used,
            List<PaperCard> eligibleCards) {
        int mythics = (int) rewardPool.toFlatList().stream()
            .filter(card -> card.getRarity() == CardRarity.MythicRare).count();
        for (PaperCard card : eligibleCards) {
            if (mythics >= 12) {
                break;
            }
            if (card.getRarity() == CardRarity.MythicRare && addReward(rewardPool, used, card)) {
                mythics++;
            }
        }
    }

    private static void addRewardsUpToLimit(CardPool rewardPool, Set<String> used,
            Iterable<PaperCard> cards) {
        for (PaperCard card : cards) {
            addReward(rewardPool, used, card);
            if (rewardPool.countAll() >= 180) {
                break;
            }
        }
    }

    private static boolean isStartingDeckInvalid(DeckGenerationContext context, Deck start) {
        PaperCard commander = context.commander();
        Predicate<PaperCard> colorIdentity = card -> card.getRules().getColorIdentity()
            .hasNoColorsExcept(commander.getRules().getColorIdentity());
        List<PaperCard> mainCards = start.getMain().toFlatList();
        Deck supportingDeck = new Deck("Supporting cards");
        for (PaperCard card : mainCards) {
            supportingDeck.getMain().add(card);
        }
        return start.getCommanders().size() != 1 || start.getMain().countAll() != 33
            || mainCards.stream().filter(card -> card.getRules().getType().isLand()).count() != 14
            || mainCards.stream().anyMatch(card -> !DeckFormat.Commander.isLegalCard(card)
                || !colorIdentity.test(card))
            || CommanderBracketCalculator.getBracket(supportingDeck) > 2
            || CommanderBracketCalculator.getBracket(start) > context.maxBracket();
    }

    private static boolean isRewardPoolInvalid(CardPool rewardPool) {
        return rewardPool.countAll() != 180
            || rewardPool.toFlatList().stream()
                .filter(card -> card.getRarity() == CardRarity.MythicRare).count() < 3;
    }

    private static void addSpells(Map<String, PaperCard> spells, List<PaperCard> candidates,
            Set<String> supportingNames, Set<String> completeNames, int maxBracket) {
        for (PaperCard card : candidates) {
            if (!card.getRules().getType().isLand()
                && card.getRules().getManaCost().getCMC() <= 6) {
                addSupportingCard(spells, card, supportingNames, completeNames, maxBracket);
            }
            if (spells.size() == 19) {
                return;
            }
        }
    }

    private static void addSupportingCard(Map<String, PaperCard> spells, PaperCard card,
            Set<String> supportingNames, Set<String> completeNames, int maxBracket) {
        if (spells.containsKey(card.getName())
            || !canAddSupportingCard(card, supportingNames, completeNames, maxBracket)) {
            return;
        }
        spells.put(card.getName(), card);
        supportingNames.addAll(CommanderBracketCalculator.getCardNames(card));
        completeNames.addAll(CommanderBracketCalculator.getCardNames(card));
    }

    private static boolean canAddSupportingCard(PaperCard card, Set<String> supportingNames,
            Set<String> completeNames, int maxBracket) {
        if (card.getRarity() == CardRarity.MythicRare) {
            return false;
        }
        Set<String> cardNames = CommanderBracketCalculator.getCardNames(card);
        Set<String> nextSupporting = new HashSet<>(supportingNames);
        nextSupporting.addAll(cardNames);
        if (CommanderBracketCalculator.calculate(nextSupporting).getBracket() > 2) {
            return false;
        }
        Set<String> nextComplete = new HashSet<>(completeNames);
        nextComplete.addAll(cardNames);
        return CommanderBracketCalculator.calculate(nextComplete).getBracket() <= maxBracket;
    }

    private static int fixingColors(PaperCard land, ColorSet commanderColors) {
        if (land.getRules().getOracleText().toLowerCase(Locale.ROOT).contains("any color")) {
            return commanderColors.countColors();
        }
        return Integer.bitCount(land.getRules().getDeckbuildingColors().getColor()
            & commanderColors.getColor());
    }

    private static boolean addBasicLands(List<PaperCard> lands, ColorSet colors, String landEdition) {
        String[] basicNames = {"Plains", "Island", "Swamp", "Mountain", "Forest"};
        byte[] basicColors = {MagicColor.WHITE, MagicColor.BLUE, MagicColor.BLACK,
            MagicColor.RED, MagicColor.GREEN};
        List<PaperCard> basics = new ArrayList<>();
        for (int i = 0; i < basicNames.length; i++) {
            if (colors.hasAnyColor(basicColors[i])) {
                PaperCard basic = RogueConfig.getBasicLandPrint(basicNames[i], landEdition);
                if (basic == null) {
                    return false;
                }
                basics.add(basic);
            }
        }
        if (basics.isEmpty()) {
            PaperCard wastes = RogueConfig.getBasicLandPrint("Wastes", landEdition);
            if (wastes == null) {
                return false;
            }
            basics.add(wastes);
        }
        for (int i = 0; lands.size() < 14; i++) {
            lands.add(basics.get(i % basics.size()));
        }
        return true;
    }

    private static boolean addReward(CardPool rewards, Set<String> used, PaperCard card) {
        if (card == null || card.getRules().getType().isBasicLand() || !used.add(card.getName())) {
            return false;
        }
        rewards.add(card);
        return true;
    }
}
