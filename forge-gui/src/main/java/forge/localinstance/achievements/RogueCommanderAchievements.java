package forge.localinstance.achievements;

import forge.game.Game;
import forge.game.player.Player;
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
    public void updateAll(Player player) { }

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
        }

        @Override
        protected boolean eval(Player player, Game game) {
            return true;
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
