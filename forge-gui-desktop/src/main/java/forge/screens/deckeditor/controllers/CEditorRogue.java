/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package forge.screens.deckeditor.controllers;

import forge.deck.CardPool;
import forge.deck.Deck;
import forge.deck.DeckSection;
import forge.game.GameType;
import forge.gamemodes.rogue.RogueIO;
import forge.gamemodes.rogue.RogueRun;
import forge.gui.UiCommand;
import forge.gui.framework.FScreen;
import forge.item.PaperCard;
import forge.itemmanager.CardManager;
import forge.itemmanager.ItemManagerConfig;
import forge.model.FModel;
import forge.screens.deckeditor.AddBasicLandsDialog;
import forge.screens.deckeditor.views.VCardCatalog;
import forge.screens.deckeditor.views.VCurrentDeck;
import forge.screens.match.controllers.CDetailPicture;
import forge.util.ItemPool;
import java.util.Map.Entry;

/**
 * Deck editor for Rogue Commander mode.
 * Allows viewing the current deck with restrictions:
 * - Can remove cards up to the number added during the run
 * - Can freely add/remove basic lands
 * - Cannot add other cards (they must come from rewards)
 */
public final class CEditorRogue extends CDeckEditor<Deck> {
    private final DeckController<Deck> controller;
    private final RogueRun rogueRun;
    private final ItemPool<PaperCard> basicLandPool;

    // Rogue-specific UI elements
    private forge.toolbox.FLabel lblRemovalCredits;
    private forge.toolbox.FLabel btnUndo;

    // Undo action tracking
    private static class UndoAction {
        enum Type { ADD, REMOVE }
        final Type type;
        final java.util.List<java.util.Map.Entry<PaperCard, Integer>> items;
        final int nonBasicLandsCount; // For restoring removal credits

        UndoAction(Type type, Iterable<java.util.Map.Entry<PaperCard, Integer>> items, int nonBasicLandsCount) {
            this.type = type;
            this.items = new java.util.ArrayList<>();
            for (java.util.Map.Entry<PaperCard, Integer> entry : items) {
                this.items.add(entry);
            }
            this.nonBasicLandsCount = nonBasicLandsCount;
        }
    }
    private final java.util.Stack<UndoAction> undoStack = new java.util.Stack<>();

    public CEditorRogue(final RogueRun rogueRun0, final FScreen screen0, final CDetailPicture cDetailPicture0) {
        super(screen0, cDetailPicture0, GameType.RogueCommander);
        this.rogueRun = rogueRun0;

        // Create infinite pool of basic lands from the commander's edition
        basicLandPool = new ItemPool<>(PaperCard.class);

        // Get commander's edition
        String commanderName = rogueRun0.getSelectedRogueDeck().getCommanderCardName();
        PaperCard commanderCard = FModel.getMagicDb().getCommonCards().getCard(commanderName);
        String commanderEdition = (commanderCard != null) ? commanderCard.getEdition() : null;

        // Add basic lands from commander's edition
        int landsAdded = 0;
        for (Entry<PaperCard, Integer> entry : FModel.getAllCardsNoAlt()) {
            PaperCard card = entry.getKey();
            if (card.getRules().getType().isBasicLand()) {
                // Only add lands from commander's edition if we have one
                if (card.getEdition().equals(commanderEdition)) {
                    basicLandPool.add(card);
                    landsAdded++;
                }
            }
        }

        // Fallback 1: If < 5 lands from commander's edition, use Modern Horizons 3 (MH3) lands
        if (landsAdded < 5) {
            basicLandPool.clear();
            for (Entry<PaperCard, Integer> entry : FModel.getAllCardsNoAlt()) {
                PaperCard card = entry.getKey();
                if (card.getRules().getType().isBasicLand() && "MH3".equals(card.getEdition())) {
                    basicLandPool.add(card);
                }
            }
        }

        // Fallback 2: If still no lands (MH3 not available), add all basic lands
        if (basicLandPool.isEmpty()) {
            for (Entry<PaperCard, Integer> entry : FModel.getAllCardsNoAlt()) {
                PaperCard card = entry.getKey();
                if (card.getRules().getType().isBasicLand()) {
                    basicLandPool.add(card);
                }
            }
        }

        // Create managers with empty catalog (no cards to add except basic lands)
        final CardManager catalogManager = new CardManager(cDetailPicture0, false, false, false);
        final CardManager deckManager = new CardManager(cDetailPicture0, false, false, false);

        catalogManager.setCaption("Card Catalog");
        catalogManager.setAlwaysNonUnique(true);
        deckManager.setAlwaysNonUnique(true);

        this.setCatalogManager(catalogManager);
        this.setDeckManager(deckManager);

        this.controller = new DeckController<>(null, this, Deck::new);

        // Setup Add Basic Lands button
        getBtnAddBasicLands().setCommand((UiCommand) () -> addBasicLands(this));
    }

    /**
     * Add basic lands to the deck (unrestricted).
     */
    private static void addBasicLands(CEditorRogue editor) {

        AddBasicLandsDialog dialog = new AddBasicLandsDialog(editor.getHumanDeck(), null);
        CardPool landsToAdd = dialog.show();

        if (landsToAdd != null && !landsToAdd.isEmpty()) {
            for (Entry<PaperCard, Integer> entry : landsToAdd) {
                editor.getHumanDeck().getMain().add(entry.getKey(), entry.getValue());
            }
            editor.getDeckController().notifyModelChanged();
            editor.resetTables();
        }
    }

    @Override
    protected CardLimit getCardLimit() {
        return CardLimit.None;  // No card limit restrictions for Commander
    }

    @Override
    protected void onAddItems(Iterable<Entry<PaperCard, Integer>> items, boolean toAlternate) {
        if (toAlternate) { return; }

        // Only allow adding if all items are basic lands
        for (Entry<PaperCard, Integer> entry : items) {
            if (!entry.getKey().getRules().getType().isBasicLand()) {
                // Non-basic land detected - reject all additions
                return;
            }
        }

        // Get allowed additions (applies card limits if needed)
        ItemPool<PaperCard> itemsToAdd = getAllowedAdditions(items);
        if (itemsToAdd.isEmpty()) { return; }

        // Add to deck manager
        this.getDeckManager().addItems(itemsToAdd);

        // Push ADD action onto undo stack (basic lands don't affect removal credits)
        undoStack.push(new UndoAction(UndoAction.Type.ADD, itemsToAdd, 0));

        // Since catalog is infinite, just select the added items
        // (don't remove them from catalog)
        this.getCatalogManager().selectItemEntrys(itemsToAdd);

        this.getDeckController().notifyModelChanged();

        // Update the undo button state
        updateRemovalCreditsLabel();
    }

    @Override
    protected void onRemoveItems(Iterable<Entry<PaperCard, Integer>> items, boolean toAlternate) {
        if (toAlternate) { return; }

        // Count how many non-basic lands are being removed
        int nonBasicLandsToRemove = 0;
        for (Entry<PaperCard, Integer> entry : items) {
            if (!entry.getKey().getRules().getType().isBasicLand()) {
                nonBasicLandsToRemove += entry.getValue();
            }
        }

        // Check if player has enough removal credits
        if (nonBasicLandsToRemove > rogueRun.getRemovalCredits()) {
            // Not enough removal credits
            String message = "You cannot remove any more non-basic land cards (You need more removal credits, e.g. from adding non-basic land cards to your Deck from Card Rewards).";
            javax.swing.JOptionPane.showMessageDialog(
                null,
                message,
                "Removal Limit Reached",
                javax.swing.JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Remove from deck manager
        this.getDeckManager().removeItems(items);

        // Deduct removal credits from RogueRun (persisted)
        rogueRun.setRemovalCredits(rogueRun.getRemovalCredits() - nonBasicLandsToRemove);

        // Push REMOVE action onto undo stack with non-basic land count for credit restoration
        undoStack.push(new UndoAction(UndoAction.Type.REMOVE, items, nonBasicLandsToRemove));

        // Since catalog is infinite, don't add items back to catalog
        // (they're always available)

        this.getDeckController().notifyModelChanged();

        // Save the run to persist credit changes
        RogueIO.saveRun(rogueRun);

        // Update the removal credits label
        updateRemovalCreditsLabel();
    }

    @Override
    protected void buildAddContextMenu(EditorContextMenuBuilder cmb) {
        cmb.addMoveItems("Add", "to deck");
    }

    @Override
    protected void buildRemoveContextMenu(EditorContextMenuBuilder cmb) {
        // Standard remove context menu
        cmb.addMoveItems("Remove", null);
    }

    @Override
    public void resetTables() {
        Deck deck = this.rogueRun.getCurrentDeck();

        // Set catalog to infinite basic lands pool (makes a copy, which is fine)
        this.getCatalogManager().setPool(basicLandPool, true);

        // Set deck to current run's deck BY REFERENCE
        // IMPORTANT: Use single-parameter setPool() to work by reference!
        // The two-parameter version setPool(pool, false) creates a COPY which breaks persistence
        if (deck != null) {
            this.getDeckManager().setPool(deck.getMain());
        } else {
            this.getDeckManager().setPool(new CardPool());
        }
    }

    @Override
    public DeckController<Deck> getDeckController() {
        return this.controller;
    }


    @Override
    protected void resetUI() {
        super.resetUI();

        // Hide add buttons (can't add cards from catalog)
        getBtnAdd().setVisible(false);
        getBtnAdd4().setVisible(false);

        // Hide basic lands button (card catalog provides this functionality)
        getBtnAddBasicLands().setVisible(false);

        // Keep remove buttons visible
        getBtnRemove().setVisible(true);
        getBtnRemove4().setVisible(true);

        // Hide deck management buttons (can't save/load in Rogue mode)
        VCurrentDeck.SINGLETON_INSTANCE.getBtnSave().setVisible(false);
        VCurrentDeck.SINGLETON_INSTANCE.getBtnSaveAs().setVisible(false);
        VCurrentDeck.SINGLETON_INSTANCE.getBtnNew().setVisible(false);
        VCurrentDeck.SINGLETON_INSTANCE.getBtnOpen().setVisible(false);
        VCurrentDeck.SINGLETON_INSTANCE.getBtnImport().setVisible(false);
        VCurrentDeck.SINGLETON_INSTANCE.getBtnPrintProxies().setVisible(false);

        // Disable deck title editing
        VCurrentDeck.SINGLETON_INSTANCE.getTxfTitle().setEnabled(false);

        // Set title
        VCurrentDeck.SINGLETON_INSTANCE.getLblTitle().setText("Rogue Commander Deck:");
        VCardCatalog.SINGLETON_INSTANCE.getTabLabel().setText("Basic Lands Only");

        // Add Rogue-specific UI elements to deck manager button panel
        // These will automatically be isolated to this editor instance (not shared)
        lblRemovalCredits = new forge.toolbox.FLabel.Builder()
            .text("Removals: " + rogueRun.getRemovalCredits())
            .fontSize(12)
            .build();
        this.getDeckManager().getPnlButtons().add(lblRemovalCredits, "w 18%!, h 30px!, gapx 5");

        btnUndo = new forge.toolbox.FLabel.Builder()
            .text("Undo")
            .tooltip("Undo last removal")
            .fontSize(12)
            .opaque(true)
            .hoverable(true)
            .build();
        btnUndo.setCommand(this::undoLastRemoval);
        this.getDeckManager().getPnlButtons().add(btnUndo, "w 12%!, h 30px!, gapx 5");

        // Update label text and button state
        updateRemovalCreditsLabel();
    }

    @Override
    public Boolean isSectionImportable(DeckSection section) {
        return false;  // No importing in Rogue mode
    }

    @Override
    public boolean canSwitchAway(boolean isClosing) {
        // Save the RogueRun to persist deck changes
        if (rogueRun != null) {
            RogueIO.saveRun(rogueRun);
            // Reload to ensure we're working with the saved version
            // This prevents stale data if the editor is opened again
            resetTables();
        }
        return true;
    }

    @Override
    public void resetUIChanges() {
        // Reset UI to default state
        VCurrentDeck.SINGLETON_INSTANCE.getBtnSave().setVisible(true);
        VCurrentDeck.SINGLETON_INSTANCE.getBtnSaveAs().setVisible(true);
        VCurrentDeck.SINGLETON_INSTANCE.getBtnNew().setVisible(true);
        VCurrentDeck.SINGLETON_INSTANCE.getBtnOpen().setVisible(true);
        VCurrentDeck.SINGLETON_INSTANCE.getBtnImport().setVisible(true);
        VCurrentDeck.SINGLETON_INSTANCE.getTxfTitle().setEnabled(true);
        getBtnAdd().setVisible(true);
        getBtnAdd4().setVisible(true);
    }

    @Override
    public void update() {
        this.getCatalogManager().setup(ItemManagerConfig.CARD_CATALOG);
        this.getDeckManager().setup(ItemManagerConfig.DECK_EDITOR);

        // Clear undo stack for new editing session
        undoStack.clear();

        resetUI();
        resetTables();

        this.getDeckController().setModel(rogueRun.getCurrentDeck());

        // Set deck name
        if (rogueRun.getCurrentDeck() != null) {
            String deckName = rogueRun.getSelectedRogueDeck().getName() + " - Rogue Run";
            rogueRun.getCurrentDeck().setName(deckName);
            VCurrentDeck.SINGLETON_INSTANCE.getTxfTitle().setText(deckName);
        }
    }

    @Override
    protected ItemPool<PaperCard> getAllowedAdditions(Iterable<Entry<PaperCard, Integer>> itemsToAdd) {
        // Only basic lands can be added
        final ItemPool<PaperCard> additions = new ItemPool<>(PaperCard.class);

        for (final Entry<PaperCard, Integer> itemEntry : itemsToAdd) {
            final PaperCard card = itemEntry.getKey();
            if (card.getRules().getType().isBasicLand()) {
                additions.add(card, itemEntry.getValue());
            }
        }

        return additions;
    }

    /**
     * Updates the removal credits label to show available removals.
     */
    private void updateRemovalCreditsLabel() {
        if (lblRemovalCredits != null) {
            lblRemovalCredits.setText("Removals: " + rogueRun.getRemovalCredits());

            // Update undo button state
            if (btnUndo != null) {
                btnUndo.setEnabled(!undoStack.isEmpty());
            }
        }
    }

    /**
     * Undoes the last action (either ADD or REMOVE).
     * - If last action was REMOVE: re-adds the cards and restores removal credits
     * - If last action was ADD: removes the cards that were added
     */
    private void undoLastRemoval() {
        if (undoStack.isEmpty()) {
            return;
        }

        // Pop the last action
        UndoAction action = undoStack.pop();

        // Convert items list to ItemPool for deck manager
        ItemPool<PaperCard> itemPool = new ItemPool<>(PaperCard.class);
        for (java.util.Map.Entry<PaperCard, Integer> entry : action.items) {
            itemPool.add(entry.getKey(), entry.getValue());
        }

        // Reverse the action
        if (action.type == UndoAction.Type.REMOVE) {
            // Last action was REMOVE → undo by adding cards back
            this.getDeckManager().addItems(itemPool);

            // Restore removal credits (only for non-basic lands)
            rogueRun.setRemovalCredits(rogueRun.getRemovalCredits() + action.nonBasicLandsCount);

        } else { // action.type == UndoAction.Type.ADD
            // Last action was ADD → undo by removing cards
            this.getDeckManager().removeItems(action.items);

            // No removal credits to restore (additions don't cost credits)
        }

        this.getDeckController().notifyModelChanged();

        // Save the run to persist credit changes
        RogueIO.saveRun(rogueRun);

        updateRemovalCreditsLabel();
    }
}
