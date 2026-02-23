package forge.screens.deckeditor.controllers;

import forge.card.CardRules;
import forge.card.CardRulesPredicates;
import forge.card.CardType;
import forge.card.ICardFace;
import forge.card.ColorSet;
import forge.card.MagicColor;
import forge.deck.Deck;
import forge.deck.DeckBase;
import forge.game.GameType;
import forge.gui.framework.ICDoc;
import forge.item.InventoryItem;
import forge.item.PaperCard;
import forge.item.PaperCardPredicates;
import forge.itemmanager.SItemManagerUtil.StatTypes;
import forge.screens.deckeditor.CDeckEditorUI;
import forge.screens.deckeditor.views.VStatistics;
import forge.util.ItemPool;
import forge.util.Localizer;
import java.util.Map.Entry;
import java.util.function.Predicate;
import javax.swing.JLabel;

/**
 * Controls the "analysis" panel in the deck editor UI.
 *
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 *
 */
public enum CStatistics implements ICDoc {
    /** */
    SINGLETON_INSTANCE;

    //========== Overridden methods

    @Override
    public void register() {
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#initialize()
     */
    @Override
    public void initialize() {
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#update()
     */
    @Override
    public void update() {
        analyze();
    }

    private void setLabelValue(final JLabel label, final ItemPool<PaperCard> deck, final Predicate<CardRules> predicate, final int total) {
        final int tmp = deck.countAll(PaperCardPredicates.fromRules(predicate));
        label.setText(tmp + " (" + calculatePercentage(tmp, total) + "%)");
    }

    private void setLabelValue(final JLabel label, final String str, final int value, final int total) {
        String labelText = String.format("%s%d (%d%%)", str, value, calculatePercentage(value, total));
        label.setText(labelText);
    }

    //========== Other methods
    @SuppressWarnings("unchecked")
    private <T extends InventoryItem, TModel extends DeckBase> void analyze() {
        final ACEditorBase<T, TModel> ed = (ACEditorBase<T, TModel>)
                CDeckEditorUI.SINGLETON_INSTANCE.getCurrentEditorController();

        if (ed == null) { return; }

        final ItemPool<PaperCard> deck = ItemPool.createFrom(ed.getDeckManager().getPool(), PaperCard.class);

        int total = deck.countAll();
        final int landCount = deck.countAll(PaperCardPredicates.fromRules(CStatistics::isOrHasLandFace));
        int totalWithoutLands = total - landCount;
        final int[] shardCount = calculateShards(deck);

        // Hack-ish: avoid /0 cases, but still populate labels :)
        if (total == 0) { total = 1; }
        if (totalWithoutLands == 0) { totalWithoutLands = 1; }

        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblCreature(), deck, CardRulesPredicates.IS_CREATURE, total);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblLand(), deck, CardRulesPredicates.IS_LAND, total);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblEnchantment(), deck, CardRulesPredicates.IS_ENCHANTMENT, total);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblArtifact(), deck, CardRulesPredicates.IS_ARTIFACT, total);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblInstant(), deck, CardRulesPredicates.IS_INSTANT, total);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblSorcery(), deck, CardRulesPredicates.IS_SORCERY, total);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblPlaneswalker(), deck, CardRulesPredicates.IS_PLANESWALKER, total);

        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblMulti(), deck, CardRulesPredicates.IS_MULTICOLOR, total);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblColorless(), deck, CardRulesPredicates.IS_COLORLESS, total);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblBlack(), deck, CardRulesPredicates.isMonoColor(MagicColor.BLACK), total);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblBlue(), deck, CardRulesPredicates.isMonoColor(MagicColor.BLUE), total);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblGreen(), deck, CardRulesPredicates.isMonoColor(MagicColor.GREEN), total);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblRed(), deck, CardRulesPredicates.isMonoColor(MagicColor.RED), total);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblWhite(), deck, CardRulesPredicates.isMonoColor(MagicColor.WHITE), total);

        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblCMC0(), deck, StatTypes.CMC_0.predicate, total);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblCMC1(), deck, StatTypes.CMC_1.predicate, total);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblCMC2(), deck, StatTypes.CMC_2.predicate, total);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblCMC3(), deck, StatTypes.CMC_3.predicate, total);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblCMC4(), deck, StatTypes.CMC_4.predicate, total);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblCMC5(), deck, StatTypes.CMC_5.predicate, total);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblCMC6(), deck, StatTypes.CMC_6.predicate, total);

        int totShards = calculateTotalShards(shardCount);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblWhiteShard(), "Shards:", shardCount[0], totShards);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblBlueShard(), "Shards:", shardCount[1], totShards);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblBlackShard(), "Shards:", shardCount[2], totShards);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblRedShard(), "Shards:", shardCount[3], totShards);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblGreenShard(), "Shards:", shardCount[4], totShards);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblColorlessShard(), "Shards:", shardCount[5], totShards);

        ColorSet deckColors = ColorSet.fromMask(0);
        GameType gameType = ed.getGameType();
        if (gameType != null && gameType.getDeckFormat().hasCommander()) {
            Deck humanDeck = ed.getHumanDeck();
            if (humanDeck != null) {
                byte cmdCI = 0;
                for (PaperCard commander : humanDeck.getCommanders()) {
                    cmdCI |= commander.getRules().getColorIdentity().getColor();
                }
                deckColors = ColorSet.fromMask(cmdCI);
            }
        }
        final int[] landMana = calculateLandManaProduction(deck, deckColors);
        int totalLandMana = landCount > 0 ? landCount : 1;
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblWhiteLand(), "Lands:", landMana[0], totalLandMana);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblBlueLand(), "Lands:", landMana[1], totalLandMana);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblBlackLand(), "Lands:", landMana[2], totalLandMana);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblRedLand(), "Lands:", landMana[3], totalLandMana);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblGreenLand(), "Lands:", landMana[4], totalLandMana);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblColorlessLand(), "Lands:", landMana[5], totalLandMana);

        int tmc = 0;
        for (final Entry<PaperCard, Integer> e : deck) {
            tmc += e.getKey().getRules().getManaCost().getCMC() * e.getValue();
        }
        final double amc = Math.round((double) tmc / (double) total * 100) / 100.0d;
        final double amcNoLands = Math.round((double) tmc / (double) totalWithoutLands * 100) / 100.0d;

        final double expectedLands = expectedLandsInOpeningHand(deck.countAll(), landCount);

        VStatistics.SINGLETON_INSTANCE.getLblTotal().setText(
                String.format("%s: %d", Localizer.getInstance().getMessage("lblTotalCards").toUpperCase(), deck.countAll()));
        VStatistics.SINGLETON_INSTANCE.getLblTMC().setText(
                String.format("%s: %d", Localizer.getInstance().getMessage("lblTotalManaCost").toUpperCase(), tmc));
        VStatistics.SINGLETON_INSTANCE.getLblAMC().setText(String.format("%s: %.2f",
                Localizer.getInstance().getMessage("lblAverageManaCost").toUpperCase(), amc));
        VStatistics.SINGLETON_INSTANCE.getLblAMCWithoutLands().setText(String.format("%s: %.2f",
            Localizer.getInstance().getMessage("lblAverageManaCostWithoutLands").toUpperCase(), amcNoLands));
        VStatistics.SINGLETON_INSTANCE.getLblExpectedLands().setText(String.format("%s: %.2f",
            Localizer.getInstance().getMessage("lblExpectedLands").toUpperCase(), expectedLands));
    }

    /**
     * Calculate the expected number of lands in a 7-card opening hand.
     */
    public static double expectedLandsInOpeningHand(int deckSize, int landCount) {
        if (deckSize == 0) return 0.0;
        int openingHandSize = 7;
        double expected = openingHandSize * ((double) landCount / deckSize);
        return Math.round(expected * 100.0) / 100.0;
    }

    /**
     * Divides X by Y, multiplies by 100, rounds, returns.
     *
     * @param x0 &emsp; Numerator (int)
     * @param y0 &emsp; Denominator (int)
     * @return rounded result (int)
     */
    public static int calculatePercentage(final int x0, final int y0) {
        return (int) Math.round((double) (x0 * 100) / (double) y0);
    }

    public static int[] calculateShards(final ItemPool<PaperCard> deck) {
        final int[] counts = new int[6]; // in WUBRGC order
        for (final PaperCard c : deck.toFlatList()) {
            final int[] cShards = c.getRules().getManaCost().getColorShardCounts();
            for (int i = 0; i < 6; i++) {
                counts[i] += cShards[i];
            }
        }
        return counts;
    }

    public static int calculateTotalShards(int[] counts) {
        int total = 0;
        for (int count : counts) {
            total += count;
        }
        return total;
    }

    private static boolean isOrHasLandFace(final CardRules rules) {
        if (rules.getType().isLand()) return true;
        return rules.getOtherPart() != null && rules.getOtherPart().getType().isLand();
    }

    public static int[] calculateLandManaProduction(final ItemPool<PaperCard> deck, final ColorSet deckColors) {
        final int[] counts = new int[6]; // WUBRGC order
        for (final PaperCard c : deck.toFlatList()) {
            CardRules rules = c.getRules();
            if (!isOrHasLandFace(rules)) {
                continue;
            }
            boolean[] produces = getLandManaColors(rules, deckColors);
            for (int i = 0; i < 6; i++) {
                if (produces[i]) { counts[i]++; }
            }
        }
        return counts;
    }

    private static boolean[] getLandManaColors(final CardRules rules, final ColorSet deckColors) {
        boolean[] produces = new boolean[6]; // WUBRGC

        // Check both faces for land subtypes and mana abilities
        ICardFace[] faces = rules.getOtherPart() != null
                ? new ICardFace[]{rules.getMainPart(), rules.getOtherPart()}
                : new ICardFace[]{rules.getMainPart()};

        for (ICardFace face : faces) {
            if (!face.getType().isLand()) {
                continue;
            }
            applySubtypes(produces, face.getType());
            parseManaAbilities(produces, face, deckColors);
        }
        return produces;
    }

    private static void applySubtypes(boolean[] produces, CardType type) {
        if (type.hasSubtype("Plains"))   produces[0] = true;
        if (type.hasSubtype("Island"))   produces[1] = true;
        if (type.hasSubtype("Swamp"))    produces[2] = true;
        if (type.hasSubtype("Mountain")) produces[3] = true;
        if (type.hasSubtype("Forest"))   produces[4] = true;
    }

    private static void parseManaAbilities(boolean[] produces, ICardFace face, ColorSet deckColors) {
        for (String ability : face.getAbilities()) {
            // Mana abilities with Produced$
            int idx = ability.indexOf("Produced$ ");
            if (idx >= 0) {
                String produced = ability.substring(idx + 10);
                int pipe = produced.indexOf('|');
                if (pipe >= 0) produced = produced.substring(0, pipe).trim();

                if (produced.startsWith("Combo ColorIdentity")) {
                    applyColorSet(produces, deckColors);
                } else if (produced.startsWith("Combo Any") || produced.equals("Any")) {
                    for (int i = 0; i < 5; i++) produces[i] = true;
                } else if (produced.startsWith("Combo ")) {
                    applyColorLetters(produces, produced.substring(6));
                } else {
                    applyColorLetters(produces, produced);
                }
                continue;
            }

            // ManaReflected abilities
            if (ability.contains("ManaReflected")) {
                applyColorSet(produces, deckColors);
            }
        }
    }

    private static void applyColorSet(boolean[] produces, ColorSet colors) {
        if (colors.hasWhite()) produces[0] = true;
        if (colors.hasBlue())  produces[1] = true;
        if (colors.hasBlack()) produces[2] = true;
        if (colors.hasRed())   produces[3] = true;
        if (colors.hasGreen()) produces[4] = true;
    }

    private static void applyColorLetters(boolean[] produces, String letters) {
        if (letters.contains("W")) produces[0] = true;
        if (letters.contains("U")) produces[1] = true;
        if (letters.contains("B")) produces[2] = true;
        if (letters.contains("R")) produces[3] = true;
        if (letters.contains("G")) produces[4] = true;
        if (letters.contains("C")) produces[5] = true;
    }

}
