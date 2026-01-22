package forge.screens.home.rogue;

import com.google.common.collect.ImmutableList;
import forge.item.PaperCard;
import forge.toolbox.FLabel;
import forge.toolbox.FOptionPane;
import forge.toolbox.FSkin.SkinnedPanel;
import forge.util.Localizer;
import forge.view.arcane.CardPanel;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Dialog for selecting reward cards visually.
 * Displays cards as images and allows selecting up to a maximum number.
 */
public class CardRewardDialog {
    private static final int CARD_WIDTH = 223;  // Larger cards for readability
    private static final int CARD_HEIGHT = Math.round(CARD_WIDTH * CardPanel.ASPECT_RATIO);
    private static final int CARD_SPACING = 15;
    private static final int PADDING = 30;

    private final String title;
    private final int maxSelections;
    private final Set<PaperCard> selectedCards;
    private final List<SelectableCardPanel> cardPanels;
    private final MainPanel panel;
    private final FLabel lblInfo;
    private final FLabel lblRewards;
    private FOptionPane optionPane;
    private CardZoomUtil zoomUtil;

    /**
     * Create a card reward selection dialog.
     * @param title Dialog title
     * @param cards List of cards to choose from
     * @param maxSelections Maximum number of cards to select
     */
    public CardRewardDialog(String title, List<PaperCard> cards, int maxSelections) {
        this.title = title;
        this.maxSelections = maxSelections;
        this.selectedCards = new HashSet<>();
        this.cardPanels = new ArrayList<>();

        // Create rewards label
        lblRewards = new FLabel.Builder()
                .text(getRewardsText())
                .fontSize(16)
                .fontStyle(Font.BOLD)
                .fontAlign(SwingConstants.CENTER)
                .build();

        // Create info label
        lblInfo = new FLabel.Builder()
                .text(getInfoText())
                .fontSize(14)
                .fontAlign(SwingConstants.CENTER)
                .build();

        // Create main panel
        panel = new MainPanel();
        panel.add(lblRewards);
        panel.add(lblInfo);

        // Create card panels
        for (PaperCard card : cards) {
            SelectableCardPanel cardPanel = new SelectableCardPanel(card);
            cardPanels.add(cardPanel);
            panel.add(cardPanel);
        }

        // Calculate dialog size (max 4 cards per row)
        int cardsPerRow = Math.min(cards.size(), 4);
        int numRows = (int) Math.ceil(cards.size() / 4.0);

        int dialogWidth = cardsPerRow * (CARD_WIDTH + CARD_SPACING) - CARD_SPACING + 2 * PADDING;
        int dialogHeight = numRows * (CARD_HEIGHT + CARD_SPACING) - CARD_SPACING + 140 + 2 * PADDING; // 140px for labels (removed button)

        Dimension dialogSize = new Dimension(dialogWidth, dialogHeight);
        panel.setPreferredSize(dialogSize);
        panel.setMinimumSize(dialogSize);
    }

    private void revealAllCards() {
        for (SelectableCardPanel cardPanel : cardPanels) {
            cardPanel.flip();
        }
        panel.revalidate();
        panel.repaint();
    }

    private String getRewardsText() {
        return String.format("Victory! %s.", title);
    }

    /**
     * Show the dialog and return the selected cards.
     * @return List of selected cards, or empty list if canceled
     */
    public List<PaperCard> show() {
        final Localizer localizer = Localizer.getInstance();
        optionPane = new FOptionPane(
                null,
                "Card Rewards",
                null,
                panel,
                ImmutableList.of(localizer.getMessage("lblOK"), localizer.getMessage("lblCancel")),
                0
        );

        // Setup zoom utility
        zoomUtil = new CardZoomUtil(optionPane);
        zoomUtil.setupZoomOverlay();

        panel.revalidate();
        panel.repaint();

        // Automatically reveal all cards after dialog is shown
        // Use invokeLater to ensure it runs after the dialog is displayed
        SwingUtilities.invokeLater(() -> {
            Timer revealTimer = new Timer(200, e -> {
                revealAllCards();
                ((Timer) e.getSource()).stop();
            });
            revealTimer.setRepeats(false);
            revealTimer.start();
        });

        optionPane.setVisible(true);

        int result = optionPane.getResult();
        optionPane.dispose();

        if (result == 0) {
            return new ArrayList<>(selectedCards);
        }
        return new ArrayList<>();
    }


    private void updateInfoLabel() {
        lblInfo.setText(getInfoText());
    }

    private String getInfoText() {
        return String.format("Select up to %d cards (%d selected)", maxSelections, selectedCards.size());
    }

    private void toggleCardSelection(SelectableCardPanel cardPanel) {
        PaperCard card = cardPanel.card;

        if (selectedCards.contains(card)) {
            // Deselect
            selectedCards.remove(card);
            cardPanel.setSelected(false);
        } else if (selectedCards.size() < maxSelections) {
            // Select (if under limit)
            selectedCards.add(card);
            cardPanel.setSelected(true);
        }

        updateInfoLabel();
    }

    private class MainPanel extends SkinnedPanel {
        private MainPanel() {
            super(null);
            setOpaque(false);
        }

        @Override
        public void doLayout() {
            int y = PADDING;
            int totalWidth = getWidth();

            // Layout rewards label
            lblRewards.setBounds(PADDING, y, totalWidth - 2 * PADDING, 35);
            y += 35 + 5;

            // Layout info label
            lblInfo.setBounds(PADDING, y, totalWidth - 2 * PADDING, 30);
            y += 30 + 15;

            // Layout card panels in rows of up to 4 cards
            int cardsPerRow = 4;
            int cardIndex = 0;

            for (int row = 0; cardIndex < cardPanels.size(); row++) {
                // Calculate how many cards in this row
                int cardsInThisRow = Math.min(cardsPerRow, cardPanels.size() - cardIndex);
                int rowWidth = cardsInThisRow * CARD_WIDTH + (cardsInThisRow - 1) * CARD_SPACING;
                int startX = (totalWidth - rowWidth) / 2;

                // Position cards in this row
                int x = startX;
                for (int col = 0; col < cardsInThisRow; col++) {
                    SelectableCardPanel cardPanel = cardPanels.get(cardIndex);
                    cardPanel.setBounds(x, y, CARD_WIDTH, CARD_HEIGHT);
                    x += CARD_WIDTH + CARD_SPACING;
                    cardIndex++;
                }

                // Move to next row
                y += CARD_HEIGHT + CARD_SPACING;
            }
        }
    }

    private class SelectableCardPanel extends SelectableCardPanelBase {
        private SelectableCardPanel(PaperCard card) {
            super(card, () -> CardRewardDialog.this.zoomUtil);
        }

        @Override
        protected void toggleSelection() {
            toggleCardSelection(this);
        }
    }
}
