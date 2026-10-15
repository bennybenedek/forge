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
        return generateCuratedPart(selectedDeck, true);
    }

    public static RogueDeck generateRewardPool(RogueDeck selectedDeck) {
        return generateCuratedPart(selectedDeck, false);
    }

    private static RogueDeck generateCuratedPart(RogueDeck selectedDeck, boolean replaceStartingDeck) {
        if (selectedDeck == null || selectedDeck.getStartDeck() == null
            || selectedDeck.getStartDeck().getCommanders().size() != 1) {
            return null;
        }
        PaperCard commander = selectedDeck.getStartDeck().getCommanders().get(0);
        List<Map.Entry<PaperCard, Integer>> synergy = getCommanderSynergy(commander);
        for (int i = 0; i < 8; i++) {
            try {
                CandidatePool candidates = prepareCandidates(commander, synergy);
                RogueDeck deck = selectedDeck.copy();
                if (replaceStartingDeck) {
                    Deck start = buildStartingDeck(candidates, selectedDeck.getName());
                    if (start == null || !isValidStartingDeck(candidates, start)) {
                        continue;
                    }
                    deck.setStartDeck(start);
                } else {
                    CardPool rewards = buildRewardPool(candidates, deck.getStartDeck());
                    if (!isValidRewardPool(rewards)) {
                        continue;
                    }
                    deck.setRewardPool(rewards);
                }
                return deck;
            } catch (RuntimeException e) {
                Logger.warn(e, "Could not generate custom deck part for {}", commander.getName());
            }
        }
        return null;
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
        CandidatePool candidates = prepareCandidates(commander, synergy);
        Deck start = buildStartingDeck(candidates,
            (randomlyChosen ? "Random " : "Chosen ") + commander.getName());
        if (start == null || !isValidStartingDeck(candidates, start)) {
            return null;
        }
        CardPool rewardPool = buildRewardPool(candidates, start);
        if (!isValidRewardPool(rewardPool)) {
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

    private record CandidatePool(PaperCard commander, List<PaperCard> legalCards,
            Map<String, PaperCard> legalByName, LinkedHashMap<String, PaperCard> generatedSpells,
            LinkedHashMap<String, PaperCard> generatedLands,
            List<Map.Entry<PaperCard, Integer>> rankedSynergy, int maxBracket) {}

    private static CandidatePool prepareCandidates(PaperCard commander,
            List<Map.Entry<PaperCard, Integer>> synergy) {
        Predicate<PaperCard> colorIdentity = card -> card.getRules().getColorIdentity()
            .hasNoColorsExcept(commander.getRules().getColorIdentity());
        List<PaperCard> legalCards = new ArrayList<>(RogueConfig.getRandomCards(card ->
            DeckFormat.Commander.isLegalCard(card) && colorIdentity.test(card)
                && !card.getName().equals(commander.getName())));
        Map<String, PaperCard> legalByName = new HashMap<>();
        for (PaperCard card : legalCards) {
            legalByName.putIfAbsent(card.getName(), card);
        }

        int commanderBracket = CommanderBracketCalculator.calculate(
            CommanderBracketCalculator.getCardNames(commander)).getBracket();
        int maxBracket = Math.max(2, commanderBracket);
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

        LinkedHashMap<String, PaperCard> generatedSpells = new LinkedHashMap<>();
        LinkedHashMap<String, PaperCard> generatedLands = new LinkedHashMap<>();
        for (PaperCard card : generated.getMain().toFlatList()) {
            PaperCard legal = legalByName.get(card.getName());
            if (legal == null) {
                continue;
            }
            if (legal.getRules().getType().isLand()) {
                if (!legal.getRules().getType().isBasicLand()) {
                    generatedLands.putIfAbsent(legal.getName(), legal);
                }
            } else {
                generatedSpells.putIfAbsent(legal.getName(), legal);
            }
        }

        List<Map.Entry<PaperCard, Integer>> rankedSynergy = new ArrayList<>(synergy);
        Collections.shuffle(rankedSynergy, MyRandom.getRandom());
        rankedSynergy.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        return new CandidatePool(commander, legalCards, legalByName, generatedSpells,
            generatedLands, rankedSynergy, maxBracket);
    }

    private static Deck buildStartingDeck(CandidatePool candidates, String name) {
        PaperCard commander = candidates.commander();
        List<PaperCard> legalCards = new ArrayList<>(candidates.legalCards());
        Map<String, PaperCard> legalByName = candidates.legalByName();
        LinkedHashMap<String, PaperCard> generatedSpells = candidates.generatedSpells();
        LinkedHashMap<String, PaperCard> generatedLands = candidates.generatedLands();
        List<Map.Entry<PaperCard, Integer>> rankedSynergy = candidates.rankedSynergy();
        int maxBracket = candidates.maxBracket();

        LinkedHashMap<String, PaperCard> spells = new LinkedHashMap<>();
        Set<String> supportingNames = new HashSet<>();
        Set<String> completeNames = new HashSet<>(CommanderBracketCalculator.getCardNames(commander));
        for (Map.Entry<PaperCard, Integer> entry : rankedSynergy) {
            PaperCard card = generatedSpells.get(entry.getKey().getName());
            if (card != null && card.getRules().getManaCost().getCMC() >= 4
                && card.getRules().getManaCost().getCMC() <= 6) {
                addSupportingCard(spells, card, supportingNames, completeNames, maxBracket);
                if (spells.size() == 4) {
                    break;
                }
            }
        }

        List<PaperCard> spellCandidates = new ArrayList<>(generatedSpells.values());
        Collections.shuffle(spellCandidates, MyRandom.getRandom());
        spellCandidates.sort(Comparator.comparingInt(card -> card.getRules().getManaCost().getCMC()));
        addSpells(spells, spellCandidates, supportingNames, completeNames, maxBracket);
        if (spells.size() < 19) {
            List<PaperCard> fallback = new ArrayList<>();
            for (Map.Entry<PaperCard, Integer> entry : rankedSynergy) {
                PaperCard card = legalByName.get(entry.getKey().getName());
                if (card != null) {
                    fallback.add(card);
                }
            }
            Collections.shuffle(fallback, MyRandom.getRandom());
            addSpells(spells, fallback, supportingNames, completeNames, maxBracket);
        }
        if (spells.size() < 19) {
            Collections.shuffle(legalCards, MyRandom.getRandom());
            addSpells(spells, legalCards, supportingNames, completeNames, maxBracket);
        }
        if (spells.size() != 19) {
            return null;
        }

        List<PaperCard> nonbasicLands = new ArrayList<>(generatedLands.values());
        Collections.shuffle(nonbasicLands, MyRandom.getRandom());
        ColorSet commanderColors = commander.getRules().getColorIdentity();
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
            if (canAddSupportingCard(card, supportingNames, completeNames, maxBracket)) {
                lands.add(card);
                supportingNames.addAll(CommanderBracketCalculator.getCardNames(card));
                completeNames.addAll(CommanderBracketCalculator.getCardNames(card));
            }
            if (lands.size() == nonbasicLimit) {
                break;
            }
        }
        if (!addBasicLands(lands, commanderColors)) {
            return null;
        }

        Deck start = new Deck(name);
        start.getOrCreate(DeckSection.Commander).add(commander);
        for (PaperCard card : spells.values()) {
            start.getMain().add(card);
        }
        for (PaperCard card : lands) {
            start.getMain().add(card);
        }

        return start;
    }

    private static CardPool buildRewardPool(CandidatePool candidates, Deck start) {
        List<PaperCard> legalCards = new ArrayList<>(candidates.legalCards());
        Map<String, PaperCard> legalByName = candidates.legalByName();
        LinkedHashMap<String, PaperCard> generatedSpells = candidates.generatedSpells();
        List<Map.Entry<PaperCard, Integer>> rankedSynergy = candidates.rankedSynergy();

        Set<String> used = new HashSet<>();
        for (PaperCard card : start.getMain().toFlatList()) {
            used.add(card.getName());
        }
        CardPool rewardPool = new CardPool();
        int synergyCount = 0;
        for (Map.Entry<PaperCard, Integer> entry : rankedSynergy) {
            PaperCard card = legalByName.get(entry.getKey().getName());
            if (addReward(rewardPool, used, card)) {
                synergyCount++;
            }
            if (synergyCount == 120) {
                break;
            }
        }
        Collections.shuffle(legalCards, MyRandom.getRandom());
        int mythics = (int) rewardPool.toFlatList().stream()
            .filter(card -> card.getRarity() == CardRarity.MythicRare).count();
        for (PaperCard card : legalCards) {
            if (mythics >= 12) {
                break;
            }
            if (card.getRarity() == CardRarity.MythicRare && addReward(rewardPool, used, card)) {
                mythics++;
            }
        }
        for (PaperCard card : generatedSpells.values()) {
            addReward(rewardPool, used, card);
            if (rewardPool.countAll() >= 180) {
                break;
            }
        }
        Collections.shuffle(legalCards, MyRandom.getRandom());
        for (PaperCard card : legalCards) {
            addReward(rewardPool, used, card);
            if (rewardPool.countAll() >= 180) {
                break;
            }
        }
        return rewardPool;
    }

    private static boolean isValidStartingDeck(CandidatePool candidates, Deck start) {
        PaperCard commander = candidates.commander();
        Predicate<PaperCard> colorIdentity = card -> card.getRules().getColorIdentity()
            .hasNoColorsExcept(commander.getRules().getColorIdentity());
        List<PaperCard> mainCards = start.getMain().toFlatList();
        Deck supportingDeck = new Deck("Supporting cards");
        for (PaperCard card : mainCards) {
            supportingDeck.getMain().add(card);
        }
        return start.getCommanders().size() == 1 && start.getMain().countAll() == 33
            && mainCards.stream().filter(card -> card.getRules().getType().isLand()).count() == 14
            && mainCards.stream().noneMatch(card -> !DeckFormat.Commander.isLegalCard(card)
                || !colorIdentity.test(card))
            && CommanderBracketCalculator.getBracket(supportingDeck) <= 2
            && CommanderBracketCalculator.getBracket(start) <= candidates.maxBracket();
    }

    private static boolean isValidRewardPool(CardPool rewardPool) {
        return rewardPool.countAll() == 180
            && rewardPool.toFlatList().stream()
                .filter(card -> card.getRarity() == CardRarity.MythicRare).count() >= 3;
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

    private static boolean addBasicLands(List<PaperCard> lands, ColorSet colors) {
        String[] basicNames = {"Plains", "Island", "Swamp", "Mountain", "Forest"};
        byte[] basicColors = {MagicColor.WHITE, MagicColor.BLUE, MagicColor.BLACK,
            MagicColor.RED, MagicColor.GREEN};
        List<PaperCard> basics = new ArrayList<>();
        for (int i = 0; i < basicNames.length; i++) {
            if (colors.hasAnyColor(basicColors[i])) {
                PaperCard basic = FModel.getMagicDb().getCommonCards().getUniqueByName(basicNames[i]);
                if (basic == null) {
                    return false;
                }
                basics.add(basic);
            }
        }
        if (basics.isEmpty()) {
            PaperCard wastes = FModel.getMagicDb().getCommonCards().getUniqueByName("Wastes");
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
