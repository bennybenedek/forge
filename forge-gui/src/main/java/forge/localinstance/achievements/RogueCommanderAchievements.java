package forge.localinstance.achievements;

import forge.game.Game;
import forge.game.GameType;
import forge.game.player.Player;
import forge.deck.Deck;
import forge.gamemodes.rogue.RogueRun;
import forge.item.PaperCard;
import forge.gui.GuiBase;
import forge.item.IPaperCard;
import forge.localinstance.properties.ForgeConstants;
import forge.model.FModel;
import forge.util.CardTranslation;
import forge.util.Localizer;
import java.util.Date;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;

public class RogueCommanderAchievements extends AchievementCollection {
    public static final RogueCommanderAchievements instance = new RogueCommanderAchievements();

    private RogueCommanderAchievements() {
        super("lblRogueCommanderRuns", ForgeConstants.ACHIEVEMENTS_DIR + "rogue-commander.xml",
              false, ForgeConstants.ROGUE_COMMANDER_ACHIEVEMENT_LIST_FILE);
    }

    @Override
    protected void addSharedAchievements() { }

    @Override
    protected void add(String cardName, String displayName, String flavorText) {
        add(new RogueCommanderRunWin(cardName, displayName, flavorText));
    }

    @Override
    protected void addAchievements() {
        super.addAchievements(); // Load commander achievements from file
        add(new LifeAbundance());
        add(new GoldHoarder());
        add(new LegendaryArmy());
    }

    @Override
    public void updateAll(Player player) {
        if (player.getGame().getRules().getGameType() != GameType.RogueCommander) {
            return;
        }
        for (Achievement achievement : achievements.values()) {
            achievement.update(player);
        }
        save();
    }

    /**
     * Evaluate run-level and deck-level achievements using RogueRun state.
     * Called from: handleVictory(), handleDefeat(), CEditorRogue.canSwitchAway()
     */
    public void evaluateRunAchievements(RogueRun run) {
        if (run == null) return;

        // Life Abundant: have 50+ life at the end of a match, before persistent life is clamped
        if (run.getLastMatchRawLife() >= 50) {
            updateAchievement("LifeAbundance");
        }

        // Gold Hoarder: have 15+ gold
        if (run.getCurrentGold() >= 15) {
            updateAchievement("GoldHoarder");
        }

        // Legendary Army: 20+ legendary permanents in deck
        Deck deck = run.getCurrentDeck();
        if (deck != null && deck.getMain() != null) {
            int legendaryCount = 0;
            for (PaperCard card : deck.getMain().toFlatList()) {
                if (card.getRules().getType().isLegendary() && card.getRules().getType().isPermanent()) {
                    legendaryCount++;
                }
            }
            if (legendaryCount > 20) {
                updateAchievement("LegendaryArmy");
            }
        }
    }

    /** Update a non-tiered (special) achievement to earned state using loadFromXml. */
    private void updateAchievement(String key) {
        Achievement a = achievements.get(key);
        if (a == null || a.getBest() > 0) return; // already earned or not found

        try {
            Element el = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .newDocument().createElement("a");
            el.setAttribute("best", "1");
            el.setAttribute("time", String.valueOf(new Date().getTime()));
            a.loadFromXml(el);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        a.updateTrophyImage();
        GuiBase.getInterface().showImageDialog(a.getImage(),
            a.getDisplayName() + "\n" + a.getSharedDesc() + "\n" + a.getMythicDesc(),
            Localizer.getInstance().getMessage("lblAchievementEarned"));
        save();
    }

    public void recordRunWon(String commanderName) {
        Achievement a = achievements.get(commanderName);
        if (a == null) return;

        boolean firstWin = a.getBest() == 0;

        try {
            Element el = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .newDocument().createElement("a");
            el.setAttribute("best", String.valueOf(a.getBest() + 1));
            el.setAttribute("time", String.valueOf(new Date().getTime()));
            a.loadFromXml(el);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (firstWin) {
            a.updateTrophyImage();
            GuiBase.getInterface().showImageDialog(a.getImage(),
                a.getDisplayName() + "\n" + a.getSharedDesc() + "\n" + a.getMythicDesc(),
                Localizer.getInstance().getMessage("lblAchievementEarned"));
        }
        save();
    }

    private class RogueCommanderRunWin extends ProgressiveAchievement {
        private RogueCommanderRunWin(String cardName, String displayName, String flavorText) {
            super(cardName, displayName,
                  "Win a Rogue Commander run with " + CardTranslation.getTranslatedName(cardName),
                  flavorText);
            setHidden(true);
        }

        @Override
        protected boolean eval(Player player, Game game) {
            return false; // Not used; commander wins are tracked via recordRunWon()
        }

        @Override
        public IPaperCard getPaperCard() {
            return FModel.getMagicDb().getCommonCards().getCard(getKey());
        }

        @Override
        protected String getNoun() {
            return Localizer.getInstance().getMessage("lblWin");
        }
    }
}
