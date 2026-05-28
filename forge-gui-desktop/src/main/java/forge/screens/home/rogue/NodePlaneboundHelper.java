package forge.screens.home.rogue;

import forge.LobbyPlayer;
import forge.deck.CardPool;
import forge.deck.Deck;
import forge.deck.io.DeckSerializer;
import forge.game.GameType;
import forge.game.player.RegisteredPlayer;
import forge.gamemodes.match.HostedMatch;
import forge.gamemodes.rogue.RogueConfig;
import forge.gamemodes.rogue.RoguePlanebound;
import forge.gamemodes.rogue.RogueRun;
import forge.gamemodes.rogue.RogueRun.CarryCard;
import forge.gamemodes.rogue.RogueTutorial;
import forge.gamemodes.rogue.effect.Cursed;
import forge.gamemodes.rogue.effect.RogueEffect;
import forge.gamemodes.rogue.effect.RogueEffectComposite;
import forge.gamemodes.rogue.effect.Wrathful;
import forge.gamemodes.rogue.path.NodePlanebound;
import forge.gui.GuiBase;
import forge.gui.SOverlayUtils;
import forge.item.PaperCard;
import forge.localinstance.properties.ForgeConstants;
import forge.localinstance.properties.ForgePreferences;
import forge.player.GamePlayerUtil;
import forge.toolbox.FOptionPane;
import forge.toolbox.FSkin;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;

class NodePlaneboundHelper {

    void handlePlaneboundNode(NodePlanebound node, RogueRun currentRun) {
        if (currentRun == null || currentRun.getHostedMatch() != null) {
            return;
        }

        handlePlaneboundBoons(node, currentRun);

        SwingUtilities.invokeLater(() -> {
            SOverlayUtils.startGameOverlay();
            SOverlayUtils.showOverlay();
        });

        if (ForgePreferences.DEV_MODE) {
            RoguePlanebound picked = (RoguePlanebound) JOptionPane.showInputDialog(
                null, "Override planebound:", "[DEV] Pick Planebound",
                JOptionPane.PLAIN_MESSAGE, null,
                RogueConfig.loadPlanebounds().toArray(), node.getRoguePlanebound());
            if (picked != null) {
                node.setRoguePlanebound(picked);
            }
        }

        try {
            CardPool allPlanes = RogueConfig.getAllPlanes();

            String cardPlaneName = node.getRoguePlanebound().planeName();
            PaperCard designatedPlane = null;
            for (PaperCard card : allPlanes.toFlatList()) {
                if (cardPlaneName.equalsIgnoreCase(card.getName())) {
                    designatedPlane = card;
                    break;
                }
            }

            List<PaperCard> sharedPlaneDeck = new ArrayList<>();
            if (designatedPlane != null) {
                sharedPlaneDeck.add(designatedPlane);
            } else {
                System.err.println("Warning: Could not find plane card: " + cardPlaneName);
            }

            Set<GameType> appliedVariants = EnumSet.of(GameType.Commander, GameType.Planechase);

            RegisteredPlayer human = RegisteredPlayer.forVariants(
                2, appliedVariants, currentRun.getCurrentDeck(),
                null, false, sharedPlaneDeck, null
            );
            human.setStartingLife(currentRun.getCurrentLife());

            LobbyPlayer lobbyPlayer = GamePlayerUtil.getGuiPlayer();
            lobbyPlayer.setName(currentRun.getCurrentCommanderName());
            lobbyPlayer.setAvatarIndex(currentRun.getSelectedRogueDeck().getAvatarIndex());
            lobbyPlayer.setSleeveIndex(currentRun.getSelectedRogueDeck().getSleeveIndex());
            human.setPlayer(lobbyPlayer);

            if (!currentRun.getCarryCards().isEmpty()) {
                RogueTutorialHelper.showIfNotSeen(RogueTutorial.CARRY_CARDS);
                RogueEffect.addCardToCommandZone("Rogue - Carry Card Enabler", human);
            }
            for (CarryCard card : currentRun.getCarryCards()) {
                RogueEffect.addCardToCommandZone(card.toPaperCard(), human);
            }

            Deck planeboundDeck = loadPlaneboundDeck(node.getRoguePlanebound().deckPath());

            RegisteredPlayer ai = RegisteredPlayer.forVariants(
                2, appliedVariants, planeboundDeck,
                null, false, sharedPlaneDeck, null
            );
            ai.setPlayer(GamePlayerUtil.createAiPlayer(
                node.getRoguePlanebound().planeboundName(),
                node.getRoguePlanebound().avatarIndex(),
                0));

            int planeboundRowCount = currentRun.getPath().countPlaneboundRowsUpTo(node.getRowIndex());
            ai.setStartingLife(node.getPlaneboundLife(planeboundRowCount));

            RogueEffectComposite.INSTANCE.onMatchStart(human, ai, currentRun);

            List<RegisteredPlayer> players = Arrays.asList(human, ai);
            HostedMatch hostedMatch = GuiBase.getInterface().hostMatch();
            currentRun.setHostedMatch(hostedMatch);

            hostedMatch.startMatch(
                GameType.RogueCommander,
                appliedVariants,
                players,
                human,
                GuiBase.getInterface().getNewGuiGame()
            );
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(SOverlayUtils::hideOverlay);
        }

        SwingUtilities.invokeLater(SOverlayUtils::hideOverlay);
    }

    private void handlePlaneboundBoons(NodePlanebound node, RogueRun currentRun) {
        int wrathfulCount = node.getWrathfulCount();
        int cursedCount = node.getCursedCount();
        if (wrathfulCount == 0 && cursedCount == 0) {
            return;
        }

        ImageIcon flameIcon = NodePlaneboundPanel.createFlameIcon(14, 18);
        ImageIcon pentagramIcon = NodePlaneboundPanel.createPentagramIcon(14, 18);
        FSkin.SkinnedPanel effectsPanel = new FSkin.SkinnedPanel(
            new MigLayout("insets 5, gap 0, wrap", "[grow]"));
        effectsPanel.setOpaque(false);

        Set<Wrathful> usedWrathful = new HashSet<>();
        for (int i = 0; i < wrathfulCount; i++) {
            Wrathful w = Wrathful.getRandomExcluding(usedWrathful);
            usedWrathful.add(w);
            currentRun.addWrathful(w);
            JLabel lbl = new JLabel(w.getDisplayName() + " - " + w.getDescription(), flameIcon, SwingConstants.LEFT);
            lbl.setFont(FSkin.getRelativeFont(12).getBaseFont());
            lbl.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT).getColor());
            lbl.setIconTextGap(5);
            lbl.setOpaque(false);
            effectsPanel.add(lbl, "growx, h 24px!, wrap");
        }

        Set<Cursed> usedCursed = new HashSet<>();
        for (int i = 0; i < cursedCount; i++) {
            Cursed c = Cursed.getRandomExcluding(usedCursed);
            usedCursed.add(c);
            currentRun.addCursed(c);
            JLabel lbl = new JLabel(c.getDisplayName() + " - " + c.getDescription(), pentagramIcon, SwingConstants.LEFT);
            lbl.setFont(FSkin.getRelativeFont(12).getBaseFont());
            lbl.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT).getColor());
            lbl.setIconTextGap(5);
            lbl.setOpaque(false);
            effectsPanel.add(lbl, "growx, h 24px!, wrap");
        }

        boolean hasWrathful = wrathfulCount > 0;
        boolean hasCursed = cursedCount > 0;
        String title = hasWrathful && hasCursed ? "Wrathful & Cursed Planebound"
            : hasCursed ? "Cursed Planebound" : "Wrathful Planebound";
        String message = hasWrathful && hasCursed ? "This Planebound is Wrathful and Cursed!"
            : hasCursed ? "This Planebound is Cursed!" : "This Planebound is Wrathful!";
        FOptionPane.showOptionDialog(message, title, null, effectsPanel, List.of("OK"), 0);
    }

    private Deck loadPlaneboundDeck(String deckPath) {
        File deckFile = new File(ForgeConstants.RES_DIR, deckPath);

        if (!deckFile.exists()) {
            throw new RuntimeException(
                "Planebound deck not found: " + deckPath + " (full path: " + deckFile.getAbsolutePath()
                    + ")");
        }

        return DeckSerializer.fromFile(deckFile);
    }
}
