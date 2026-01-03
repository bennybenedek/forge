package forge.screens.home.rogue;

import forge.LobbyPlayer;
import forge.Singletons;
import forge.deck.CardPool;
import forge.deck.Deck;
import forge.deck.io.DeckSerializer;
import forge.game.GameType;
import forge.game.player.RegisteredPlayer;
import forge.gamemodes.match.HostedMatch;
import forge.gamemodes.rogue.*;
import forge.gui.GuiBase;
import forge.gui.SOverlayUtils;
import forge.gui.framework.EDocID;
import forge.gui.framework.FScreen;
import forge.gui.framework.ICDoc;
import forge.item.PaperCard;
import forge.localinstance.properties.ForgeConstants;
import forge.player.GamePlayerUtil;
import forge.screens.deckeditor.CDeckEditorUI;
import forge.screens.deckeditor.controllers.CEditorRogue;
import forge.screens.home.CHomeUI;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;
import javax.swing.SwingUtilities;

/**
 * Controls the "rogue map" submenu in the home UI.
 *
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 */
public enum CSubmenuRogueMap implements ICDoc {
    SINGLETON_INSTANCE;

    private final ActionListener actEnterNode = arg0 -> enterNode();
    private final ActionListener actEditDeck = arg0 -> editDeck();
    private final VSubmenuRogueMap view = VSubmenuRogueMap.SINGLETON_INSTANCE;

    // Test run data (for MVP - will be replaced with proper loading later)
    private RogueRun currentRun;

    @Override
    public void update() {
        // If no run exists, navigate to start screen
        if (currentRun == null) {
            CHomeUI.SINGLETON_INSTANCE.itemClick(EDocID.HOME_ROGUESTART);
            return;
        }

        updateView();
        SwingUtilities.invokeLater(() -> view.getBtnEnterNode().requestFocusInWindow());
    }

    @Override
    public void register() {
        // TODO document why this method is empty
    }

    @Override
    public void initialize() {
        view.getBtnEnterNode().addActionListener(actEnterNode);
        view.getBtnEditDeck().addActionListener(actEditDeck);
        view.getPathVisualizer().setNodeClickHandler(this::handleNodeClick);
    }

    /**
     * Handle click on a node panel.
     */
    private void handleNodeClick(NodePanel panel) {
        if (currentRun == null) {
            return;
        }

        // Get the index of the clicked node
        int nodeIndex = view.getPathVisualizer().getNodePanels().indexOf(panel);
        if (nodeIndex < 0) {
            return;
        }

        RoguePathNode clickedNode = currentRun.getPath().getNode(nodeIndex);
        if (clickedNode == null) {
            return;
        }

        // Only allow selecting Planebound nodes (not Sanctum, Bazaar, etc.)
        if (!(clickedNode instanceof NodePlanebound)) {
            return;
        }

        RoguePathNode currentNode = currentRun.getCurrentNode();
        if (currentNode == null) {
            return;
        }

        // Only allow selecting nodes in the current row
        if (clickedNode.getRowIndex() != currentNode.getRowIndex()) {
            return;
        }

        // Only allow selecting reachable nodes in the current row
        List<Integer> visibleInCurrentRow = currentRun.getPath().getVisibleNodesInCurrentRow(currentNode.getRowIndex());
        if (!visibleInCurrentRow.contains(nodeIndex)) {
            return; // Not reachable, ignore click
        }

        // Allow clicking any reachable plane in the current row to select it
        // Set as current node and update view
        currentRun.setCurrentNodeIndex(nodeIndex);
        updateViewWithSelection();
    }

    private void updateView() {
        view.updateDisplay(currentRun);
        updateViewWithSelection();
    }

    /**
     * Update the view with selection state and button text.
     */
    private void updateViewWithSelection() {
        if (currentRun == null) {
            return;
        }

        // Update path visualizer selection
        view.getPathVisualizer().setSelectedNode(currentRun.getCurrentNodeIndex());

        // Update button state based on current node
        RoguePathNode currentNode = currentRun.getCurrentNode();
        int currentIndex = currentRun.getCurrentNodeIndex();

        if (currentNode == null) {
            view.getBtnEnterNode().setEnabled(false);
            view.getBtnEnterNode().setText("No Node Available");
            return;
        }

        // Check if current node is a Planebound
        if (currentNode instanceof NodePlanebound) {
            // Check if there are multiple planes in the current row
            List<RoguePathNode> currentRowNodes = currentRun.getPath().getNodesInRow(currentNode.getRowIndex());
            List<RoguePathNode> currentRowPlanes = new ArrayList<>();
            for (RoguePathNode node : currentRowNodes) {
                if (node instanceof NodePlanebound) {
                    currentRowPlanes.add(node);
                }
            }

            // If multiple planes in row, check if current selection is valid
            if (currentRowPlanes.size() > 1) {
                // Check if current node is one of the planes in current row
                RoguePathNode currentNodeCheck = currentRun.getPath().getNode(currentIndex);
                if (currentNodeCheck instanceof NodePlanebound &&
                    currentNodeCheck.getRowIndex() == currentNode.getRowIndex()) {
                    view.getBtnEnterNode().setEnabled(true);
                    view.getBtnEnterNode().setText("Enter " + ((NodePlanebound) currentNodeCheck).getRoguePlanebound().planeName());
                } else {
                    view.getBtnEnterNode().setEnabled(false);
                    view.getBtnEnterNode().setText("Select Plane First");
                }
            } else {
                // Single plane - enable button
                view.getBtnEnterNode().setEnabled(true);
                view.getBtnEnterNode().setText("Enter " + ((NodePlanebound) currentNode).getRoguePlanebound().planeboundName());
            }
        } else if (currentNode instanceof NodeSanctum) {
            view.getBtnEnterNode().setEnabled(true);
            view.getBtnEnterNode().setText("Enter Sanctum");
        } else if (currentNode instanceof NodeBazaar) {
            view.getBtnEnterNode().setEnabled(true);
            view.getBtnEnterNode().setText("Enter Bazaar");
        } else {
            view.getBtnEnterNode().setEnabled(true);
            view.getBtnEnterNode().setText("Enter Node");
        }
    }

    private void enterNode() {
        if (currentRun == null) {
            return;
        }

        // Use current node
        RoguePathNode node = currentRun.getCurrentNode();

        if (node == null) {
            return;
        }

        // Handle different node types
        if (node instanceof NodePlanebound) {
            startMatch((NodePlanebound) node);
        } else if (node instanceof NodeSanctum) {
            handleSanctumNode((NodeSanctum) node);
        } else if (node instanceof NodeBazaar) {
            handleBazaarNode((NodeBazaar) node);
        } else if (node instanceof NodeEvent || node instanceof NodeChest) {
            // TODO: Implement these node types
            currentRun.nextNode();
            updateView();
        }
    }

    private void startMatch(NodePlanebound node) {
        // Show loading overlay
        SwingUtilities.invokeLater(() -> {
            SOverlayUtils.startGameOverlay();
            SOverlayUtils.showOverlay();
        });

        try {
            // Get all plane cards from the centralized cache
            CardPool allPlanes = forge.gamemodes.rogue.RogueConfig.getAllPlanes();

            // Find the designated plane for this node
            String cardPlaneName = node.getRoguePlanebound().planeName();
            PaperCard designatedPlane = null;
            for (PaperCard card : allPlanes.toFlatList()) {
                if (cardPlaneName.equalsIgnoreCase(card.getName())) {
                    designatedPlane = card;
                    break;
                }
            }

            // Create shared plane deck with multiple copies of the designated plane
            List<PaperCard> sharedPlaneDeck = new java.util.ArrayList<>();
            if (designatedPlane != null) {
                // Add 10 copies of the same plane to satisfy Planechase mechanics
                for (int i = 0; i < 10; i++) {
                    sharedPlaneDeck.add(designatedPlane);
                }
            } else {
                System.err.println("Warning: Could not find plane card: " + cardPlaneName);
            }

            // Configure Commander + Planechase variants
            Set<GameType> appliedVariants = EnumSet.of(GameType.Commander, GameType.Planechase);

            // Create human player with persistent life
            RegisteredPlayer human = RegisteredPlayer.forVariants(
                2,                   // player count
                appliedVariants,                // applied variants
                currentRun.getCurrentDeck(),    // player's deck
                null,                           // schemes (not used)
                false,                          // is archenemy
                sharedPlaneDeck,                // shared plane deck
                null                            // vanguard avatar
            );

            // Override starting life with persistent life from run
            human.setStartingLife(currentRun.getCurrentLife());

            // Use the singleton lobbyPlayer for consistent player identification
            // This ensures isMatchWonBy() works correctly in RogueWinLoseController
            LobbyPlayer lobbyPlayer = GamePlayerUtil.getGuiPlayer();
            lobbyPlayer.setName(currentRun.getSelectedRogueDeck().getName());
            lobbyPlayer.setAvatarIndex(currentRun.getSelectedRogueDeck().getAvatarIndex());
            lobbyPlayer.setSleeveIndex(currentRun.getSelectedRogueDeck().getSleeveIndex());
            human.setPlayer(lobbyPlayer);

            // Load Planebound deck
            Deck planeboundDeck = loadPlaneboundDeck(node.getRoguePlanebound().deckPath());

            // Create AI Planebound opponent
            RegisteredPlayer ai = RegisteredPlayer.forVariants(
                2,                                    // player count
                appliedVariants,                      // applied variants
                planeboundDeck,                       // Planebound deck
                null,                                  // schemes (not used)
                false,                                 // is archenemy
                sharedPlaneDeck,                      // shared plane deck
                null                                   // vanguard avatar
            );
            ai.setPlayer(GamePlayerUtil.createAiPlayer(
                node.getRoguePlanebound().planeboundName(),
                node.getRoguePlanebound().avatarIndex(),
                0));

            // Calculate life based on row: 5 + (5 * rowIndex)
            int planeboundLife = 5 + (5 * node.getRowIndex());
            ai.setStartingLife(planeboundLife);

            // Start match
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
            SwingUtilities.invokeLater(() -> {
                SOverlayUtils.hideOverlay();
                // TODO: Show error message to user
            });
        }

        SwingUtilities.invokeLater(SOverlayUtils::hideOverlay);
    }

    private Deck loadPlaneboundDeck(String deckPath) {
        // Load deck from file path
        // The deckPath is relative to the res directory, e.g., "rogue/planebounds/meria.dck"
        File deckFile = new File(ForgeConstants.RES_DIR, deckPath);

        if (!deckFile.exists()) {
            throw new RuntimeException("Planebound deck not found: " + deckPath + " (full path: " + deckFile.getAbsolutePath() + ")");
        }

        return DeckSerializer.fromFile(deckFile);
    }

    public RogueRun getCurrentRun() {
        return currentRun;
    }

    public void setCurrentRun(RogueRun run) {
        this.currentRun = run;
    }

    private void editDeck() {
        if (currentRun == null) {
            return;
        }

        // Switch to deck editor screen first (this loads saved layout/tab)
        Singletons.getControl().setCurrentScreen(FScreen.DECK_EDITOR_CONSTRUCTED);

        // Then create and set the Rogue deck editor (this should override whatever tab was loaded)
        CEditorRogue rogueEditor = new CEditorRogue(
            currentRun,
            FScreen.DECK_EDITOR_CONSTRUCTED,
            CDeckEditorUI.SINGLETON_INSTANCE.getCDetailPicture()
        );
        CDeckEditorUI.SINGLETON_INSTANCE.setEditorController(rogueEditor);

        // Now select the Card Catalog tab to show our "Basic Lands Only" catalog
        javax.swing.SwingUtilities.invokeLater(() -> {
            forge.screens.deckeditor.views.VCardCatalog catalog = forge.screens.deckeditor.views.VCardCatalog.SINGLETON_INSTANCE;
            if (catalog.getParentCell() != null) {
                catalog.getParentCell().setSelected(catalog);
            }
        });
    }

    private void handleSanctumNode(NodeSanctum sanctumNode) {
        if (currentRun == null) {
            return;
        }

        // Get current and max life
        int currentLife = currentRun.getCurrentLife();
        int maxLife = currentRun.getStartingLife();
        int healAmount = sanctumNode.getHealAmount();
        int freeRemoves = sanctumNode.getFreeRemoves();

        // Show Sanctum dialog
        SanctumDialog dialog = new SanctumDialog(currentLife, maxLife, healAmount, freeRemoves);
        SanctumDialog.SanctumChoice choice = dialog.show();

        // Handle player's choice
        switch (choice) {
            case HEAL:
                // Heal player by healAmount, capped at maximum life
                currentRun.healLife(healAmount);
                break;

            case REMOVE_CARDS:
                // Add removal credits (allows removing cards from deck later)
                currentRun.addRemovalCredits(freeRemoves);
                break;

            case SKIP:
                // Do nothing
                break;
        }

        // Mark node as completed and move to next
        sanctumNode.setCompleted(true);
        currentRun.nextNode();

        // Save run and update view
        RogueIO.saveRun(currentRun);
        updateView();
    }

    private void handleBazaarNode(NodeBazaar bazaarNode) {
        if (currentRun == null) {
            return;
        }

        // Get the rogue deck to draw cards from reward pool
        RogueDeck rogueDeck = currentRun.getSelectedRogueDeck();
        if (rogueDeck == null) {
            System.err.println("ERROR: Could not find rogue deck for current run.");
            currentRun.nextNode();
            updateView();
            return;
        }

        // Generate Bazaar inventory: 9 non-mythic + 1 mythic from reward pool
        List<PaperCard> nonMythicCards = rogueDeck.drawRewardOptions(9, forge.item.PaperCardPredicates.IS_MYTHIC_RARE.negate());
        List<PaperCard> mythicCards = rogueDeck.drawRewardOptions(1, forge.item.PaperCardPredicates.IS_MYTHIC_RARE);

        // Combine into single inventory
        List<PaperCard> inventory = new ArrayList<>();
        inventory.addAll(nonMythicCards);
        inventory.addAll(mythicCards);

        if (inventory.isEmpty()) {
            System.err.println("ERROR: No cards available in reward pool for Bazaar.");
            currentRun.nextNode();
            updateView();
            return;
        }

        // Get current gold
        int currentGold = currentRun.getCurrentGold();

        // Show Bazaar dialog
        BazaarDialog dialog = new BazaarDialog(inventory, currentGold);
        Set<PaperCard> selectedCards = dialog.show();

        // If player bought cards, add them to deck and deduct gold
        if (!selectedCards.isEmpty()) {
            // Add bought cards to player's deck (same method as card rewards)
            currentRun.addCardsToRun(new ArrayList<>(selectedCards));

            // Calculate and deduct gold cost using shared pricing
            int totalCost = BazaarPricing.calculateTotalCost(selectedCards);
            currentRun.setCurrentGold(currentGold - totalCost);

            // Remove ONLY the purchased cards from reward pool (not all inventory)
            rogueDeck.removeFromRewardPool(new ArrayList<>(selectedCards));
        }
        // If nothing purchased, don't remove anything - cards stay in pool for future Bazaars

        // Mark node as completed and move to next
        bazaarNode.setCompleted(true);
        currentRun.nextNode();

        // Save run and update view
        RogueIO.saveRun(currentRun);
        updateView();
    }
}
