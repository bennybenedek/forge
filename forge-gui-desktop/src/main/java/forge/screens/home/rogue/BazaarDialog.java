package forge.screens.home.rogue;

import forge.deckchooser.FDeckViewer;
import forge.gamemodes.rogue.BazaarPricing;
import forge.item.PaperCard;
import forge.localinstance.skin.FSkinProp;
import forge.toolbox.FLabel;
import forge.toolbox.FOptionPane;
import forge.toolbox.FSkin;
import forge.toolbox.FSkin.SkinnedPanel;
import forge.util.Localizer;
import forge.view.arcane.CardPanel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import net.miginfocom.swing.MigLayout;

/**
 * Dialog for Bazaar node interaction.
 * Allows player to purchase cards using gold based on rarity pricing.
 */
public class BazaarDialog {
    private static final int BASE_CARD_WIDTH = 240;  // Desired card width
    private static final int PRICE_LABEL_HEIGHT = 40;  // Space for price label below card
    private static final int CARD_SPACING = 10;
    private static final int MAX_CARDS_PER_ROW = 5;
    private static final int MAX_ROWS = 2;
    private static final int HEADER_HEIGHT = 95;  // Space for title, gold status, description (compact)

    private final MainPanel panel;
    private CardZoomUtil zoomUtil;
    private final List<PaperCard> availableCards;
    private final int availableGold;
    private final Set<PaperCard> selectedCards = new HashSet<>();
    private final FLabel lblGoldStatus;

    // Computed card dimensions (may be scaled down)
    private int cardWidth;
    private int cardImageHeight;
    private int cardHeight;
    private int priceLabelHeight;

    /**
     * Create a Bazaar dialog.
     * @param cards List of cards available for purchase (9 non-mythic + 1 mythic)
     * @param gold Player's available gold
     */
    public BazaarDialog(List<PaperCard> cards, int gold) {
        this.availableCards = new ArrayList<>(cards);
        this.availableGold = gold;

        // Create main panel
        panel = new MainPanel();

        // Title label
        FLabel lblTitle = new FLabel.Builder()
                .text("Bazaar")
                .fontSize(20)
                .fontStyle(Font.BOLD)
                .fontAlign(SwingConstants.CENTER)
                .build();

        // Gold status label
        lblGoldStatus = new FLabel.Builder()
                .text("Available Gold: " + availableGold)
                .fontSize(14)
                .fontAlign(SwingConstants.CENTER)
                .build();

        // Description label
        FLabel lblDescription = new FLabel.Builder()
                .text("Select cards to purchase (prices based on rarity)")
                .fontSize(12)
                .fontAlign(SwingConstants.CENTER)
                .build();

        // Add components to panel (compact layout to maximize card space)
        panel.add(lblTitle, "w 100%!, h 28px!, ax center, wrap");
        panel.add(lblGoldStatus, "w 100%!, h 22px!, ax center, wrap");
        panel.add(lblDescription, "w 100%!, h 20px!, ax center, gap 0 0 5px 10px, wrap");

        // Calculate layout: max 5 cards per row, max 2 rows
        int cardsPerRow = Math.min(availableCards.size(), MAX_CARDS_PER_ROW);
        int numRows = Math.min(MAX_ROWS, (int) Math.ceil(availableCards.size() / (double) cardsPerRow));

        // Get usable screen space (accounts for taskbar and DPI scaling)
        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration();
        Rectangle screenBounds = gc.getBounds();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
        int usableWidth = screenBounds.width - screenInsets.left - screenInsets.right;
        int usableHeight = screenBounds.height - screenInsets.top - screenInsets.bottom;

        // Reserve space for dialog title bar (~30px) and FOptionPane buttons (~50px)
        int maxDialogWidth = (int) (usableWidth * 0.9);
        int maxDialogHeight = (int) (usableHeight * 0.9) - 80;

        // Calculate desired dimensions at full card size
        int baseCardImageHeight = Math.round(BASE_CARD_WIDTH * CardPanel.ASPECT_RATIO);
        int baseCardHeight = baseCardImageHeight + PRICE_LABEL_HEIGHT;
        int desiredWidth = cardsPerRow * (BASE_CARD_WIDTH + CARD_SPACING) - CARD_SPACING + 40;
        int desiredHeight = numRows * (baseCardHeight + 15) - 15 + HEADER_HEIGHT + 15;

        // Dialog size is desired size capped to screen bounds
        int dialogWidth = Math.min(desiredWidth, maxDialogWidth);
        int dialogHeight = Math.min(desiredHeight, maxDialogHeight);

        // Initialize card dimensions (doLayout will recalculate based on actual size)
        cardWidth = BASE_CARD_WIDTH;
        cardImageHeight = baseCardImageHeight;
        priceLabelHeight = PRICE_LABEL_HEIGHT;
        cardHeight = baseCardHeight;

        Dimension dialogSize = new Dimension(dialogWidth, dialogHeight);
        panel.setPreferredSize(dialogSize);
        panel.setMinimumSize(dialogSize);
        panel.setSize(dialogSize);
    }

    /**
     * Show the dialog and return the selected cards.
     * @return Set of purchased cards (empty if skipped)
     */
    public Set<PaperCard> show() {
        final Localizer localizer = Localizer.getInstance();
        final int VIEW_DECK_OPTION = 2;

        int result;
        do {
            FOptionPane optionPane = new FOptionPane(
                    null,
                    "Bazaar",
                    null,
                    panel,
                    List.of("Buy Selected Cards", localizer.getMessage("lblSkip"), "View Deck"),
                    1  // Default to Skip button
            );

            // Setup zoom utility
            zoomUtil = new CardZoomUtil(optionPane);
            zoomUtil.setupZoomOverlay();

            // Start the card reveal animation
            panel.startRevealAnimation();

            panel.revalidate();
            panel.repaint();

            optionPane.setVisible(true);
            result = optionPane.getResult();
            optionPane.dispose();

            // If View Deck clicked, show deck and re-display dialog
            if (result == VIEW_DECK_OPTION) {
                showCurrentDeck();
            }
        } while (result == VIEW_DECK_OPTION);

        // Return selected cards if Buy was clicked (result == 0), otherwise empty set
        if (result == 0) {
            return selectedCards;
        }
        return new HashSet<>();
    }

    private void showCurrentDeck() {
        var currentRun = CSubmenuRogueMap.SINGLETON_INSTANCE.getCurrentRun();
        if (currentRun != null && currentRun.getCurrentDeck() != null) {
            FDeckViewer.show(currentRun.getCurrentDeck());
        }
    }

    private class MainPanel extends SkinnedPanel {
        private final List<SelectableCardPanel> cardPanels = new ArrayList<>();
        private Timer revealTimer;
        private int revealIndex = 0;

        private MainPanel() {
            super(new MigLayout("insets 10, gap 0, wrap", "[grow, center]", ""));
            setOpaque(false);

            // Create card panels
            for (PaperCard card : availableCards) {
                SelectableCardPanel cardPanel = new SelectableCardPanel(card);
                cardPanels.add(cardPanel);
            }
        }

        /**
         * Start the card reveal animation with a timer.
         */
        public void startRevealAnimation() {
            revealIndex = 0;
            revealTimer = new Timer(100, e -> {
                if (revealIndex < cardPanels.size()) {
                    cardPanels.get(revealIndex).reveal();
                    revealIndex++;
                } else {
                    revealTimer.stop();
                }
            });
            revealTimer.start();
        }

        @Override
        public void doLayout() {
            super.doLayout();

            if (cardPanels.isEmpty()) {
                return;
            }

            int totalWidth = getWidth();
            int totalHeight = getHeight();

            // Calculate available space for cards
            int availableWidth = totalWidth - 40; // 40 padding (20 each side)
            int availableHeight = totalHeight - HEADER_HEIGHT - 15;

            // Calculate cards per row and number of rows
            int cardsPerRow = Math.min(MAX_CARDS_PER_ROW, cardPanels.size());
            int numRows = Math.min(MAX_ROWS, (int) Math.ceil(cardPanels.size() / (double) cardsPerRow));

            // Calculate scale to fit cards in available space
            int baseCardImageHeight = Math.round(BASE_CARD_WIDTH * CardPanel.ASPECT_RATIO);
            int baseCardHeight = baseCardImageHeight + PRICE_LABEL_HEIGHT;
            int desiredWidth = cardsPerRow * (BASE_CARD_WIDTH + CARD_SPACING) - CARD_SPACING;
            int desiredHeight = numRows * (baseCardHeight + 15) - 15;

            float widthScale = availableWidth > 0 ? Math.min(1.0f, (float) availableWidth / desiredWidth) : 1.0f;
            float heightScale = availableHeight > 0 ? Math.min(1.0f, (float) availableHeight / desiredHeight) : 1.0f;
            float scale = Math.min(widthScale, heightScale);

            // Apply scale to card dimensions (including price label)
            cardWidth = Math.round(BASE_CARD_WIDTH * scale);
            cardImageHeight = Math.round(cardWidth * CardPanel.ASPECT_RATIO);
            priceLabelHeight = Math.round(PRICE_LABEL_HEIGHT * scale);
            cardHeight = cardImageHeight + priceLabelHeight;

            // Calculate starting position for grid (centered horizontally, top-aligned vertically)
            int gridWidth = cardsPerRow * cardWidth + (cardsPerRow - 1) * CARD_SPACING;
            int startX = (totalWidth - gridWidth) / 2;
            int startY = HEADER_HEIGHT;

            // Layout cards in grid
            int x = startX;
            int y = startY;
            int cardCount = 0;

            for (SelectableCardPanel cardPanel : cardPanels) {
                if (cardCount >= MAX_CARDS_PER_ROW * MAX_ROWS) break;

                cardPanel.setBounds(x, y, cardWidth, cardHeight);

                cardCount++;
                if (cardCount % cardsPerRow == 0) {
                    x = startX;
                    y += cardHeight + 15;
                } else {
                    x += cardWidth + CARD_SPACING;
                }
            }

            // Add card panels to display
            for (SelectableCardPanel cardPanel : cardPanels) {
                if (cardPanel.getParent() == null) {
                    add(cardPanel);
                }
            }
        }
    }

    /**
     * Panel for displaying a single selectable card with flip animation and price label.
     */
    private class SelectableCardPanel extends SelectableCardPanelBase {
        public SelectableCardPanel(PaperCard card) {
            super(card, () -> BazaarDialog.this.zoomUtil);
        }

        /**
         * Reveal this card with flip animation.
         * Delegates to base class flip() method.
         */
        public void reveal() {
            flip();
        }

        @Override
        public void doLayout() {
            super.doLayout();
            // Position card image at full size, leaving space below for price label
            cardPicture.setBounds(0, 0, getWidth(), cardImageHeight);
        }

        @Override
        protected void toggleSelection() {
            // Check if we can afford to select this card
            if (!selected) {
                int potentialCost = calculateTotalCost() + BazaarPricing.getCardPrice(card);
                if (potentialCost > availableGold) {
                    // Can't afford this card
                    return;
                }
            }

            selected = !selected;

            if (selected) {
                selectedCards.add(card);
            } else {
                selectedCards.remove(card);
            }

            updateGoldStatus();
            repaint();
        }

        @Override
        public void paint(Graphics g) {
            // Call base class paint (handles animation and selection highlight)
            super.paint(g);

            // Draw price label on top
            if (!faceDown) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                drawPriceLabel(g2d, getWidth(), getHeight());
            }
        }

        /**
         * Draw price label with coin icon below the card (no background box).
         */
        private void drawPriceLabel(Graphics2D g2d, int width, int height) {
            int price = BazaarPricing.getCardPrice(card);

            // Calculate position in the space below the card image
            int labelY = cardImageHeight;

            // Scale icon size proportionally
            int iconSize = Math.max(16, Math.round(28 * priceLabelHeight / (float) PRICE_LABEL_HEIGHT));
            int fontSize = Math.max(12, Math.round(20 * priceLabelHeight / (float) PRICE_LABEL_HEIGHT));

            // Draw coin icon
            Image coinIcon = FSkin.getImage(FSkinProp.ICO_QUEST_COIN).getIcon().getImage();
            int iconX = (width - iconSize - 55) / 2;
            int iconY = labelY + (priceLabelHeight - iconSize) / 2;
            g2d.drawImage(coinIcon, iconX, iconY, iconSize, iconSize, null);

            // Draw price text with shadow for visibility
            g2d.setFont(new Font("Arial", Font.BOLD, fontSize));
            FontMetrics fm = g2d.getFontMetrics();
            String priceText = String.valueOf(price);
            int textX = iconX + iconSize + 8;
            int textY = labelY + (priceLabelHeight + fm.getAscent()) / 2 - 2;

            // Draw shadow
            g2d.setColor(Color.BLACK);
            g2d.drawString(priceText, textX + 1, textY + 1);

            // Draw text
            g2d.setColor(Color.YELLOW);
            g2d.drawString(priceText, textX, textY);
        }

        /**
         * Calculate total cost of selected cards using shared pricing.
         */
        private int calculateTotalCost() {
            return BazaarPricing.calculateTotalCost(selectedCards);
        }

        /**
         * Update the gold status label.
         */
        private void updateGoldStatus() {
            int totalCost = calculateTotalCost();
            int remaining = availableGold - totalCost;

            lblGoldStatus.setText(String.format("Gold: %d / %d (Cost: %d)", remaining, availableGold, totalCost));
        }
    }
}
