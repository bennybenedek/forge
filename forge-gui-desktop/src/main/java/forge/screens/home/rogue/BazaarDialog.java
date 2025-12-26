package forge.screens.home.rogue;

import com.google.common.collect.ImmutableList;
import forge.gamemodes.rogue.BazaarPricing;
import forge.item.PaperCard;
import forge.localinstance.skin.FSkinProp;
import forge.toolbox.FButton;
import forge.toolbox.FLabel;
import forge.toolbox.FOptionPane;
import forge.toolbox.FSkin;
import forge.toolbox.FSkin.SkinnedPanel;
import forge.util.Localizer;
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
    private static final int DIALOG_WIDTH = 1100;
    private static final int DIALOG_HEIGHT = 700;
    private static final int CARD_WIDTH = 180;
    private static final int CARD_HEIGHT = 250;
    private static final int CARDS_PER_ROW = 5;

    private final MainPanel panel;
    private FOptionPane optionPane;
    private CardZoomUtil zoomUtil;
    private final List<PaperCard> availableCards;
    private final int availableGold;
    private final Set<PaperCard> selectedCards = new HashSet<>();
    private final FButton btnBuy;
    private final FLabel lblGoldStatus;

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
                .text("Bazaar of Wonders")
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

        // Buy button (no icon, smaller size)
        btnBuy = new FButton("Buy Selected Cards");
        btnBuy.addActionListener(e -> {
            optionPane.setResult(0);
            optionPane.setVisible(false);
        });

        // Add components to panel (button added separately below cards)
        panel.add(lblTitle, "w 100%!, h 40px!, ax center, wrap");
        panel.add(lblGoldStatus, "w 100%!, h 30px!, ax center, wrap");
        panel.add(lblDescription, "w 100%!, h 25px!, ax center, gap 0 0 10px 15px, wrap");

        Dimension dialogSize = new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT);
        panel.setPreferredSize(dialogSize);
        panel.setMinimumSize(dialogSize);
    }

    /**
     * Show the dialog and return the selected cards.
     * @return Set of purchased cards (empty if skipped)
     */
    public Set<PaperCard> show() {
        final Localizer localizer = Localizer.getInstance();
        optionPane = new FOptionPane(
                null,
                "Bazaar",
                null,
                panel,
                ImmutableList.of(localizer.getMessage("lblSkip")),
                -1
        );

        // Setup zoom utility
        zoomUtil = new CardZoomUtil(optionPane);
        zoomUtil.setupZoomOverlay();

        // Start the card reveal animation
        panel.startRevealAnimation();

        panel.revalidate();
        panel.repaint();

        optionPane.setVisible(true);
        optionPane.dispose();

        return selectedCards;
    }

    /**
     * Calculate total cost of selected cards using shared pricing.
     */
    private int calculateTotalCost() {
        return BazaarPricing.calculateTotalCost(selectedCards);
    }

    /**
     * Update the gold status label and Buy button state.
     */
    private void updateGoldStatus() {
        int totalCost = calculateTotalCost();
        int remaining = availableGold - totalCost;

        lblGoldStatus.setText(String.format("Gold: %d / %d (Cost: %d)", remaining, availableGold, totalCost));

        // Disable Buy button if over budget or nothing selected
        btnBuy.setEnabled(totalCost > 0 && totalCost <= availableGold);
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

            // Calculate starting position for grid
            int totalRows = (int) Math.ceil((double) cardPanels.size() / CARDS_PER_ROW);
            int gridWidth = CARDS_PER_ROW * CARD_WIDTH + (CARDS_PER_ROW - 1) * 10; // 10px spacing
            int gridHeight = totalRows * CARD_HEIGHT + (totalRows - 1) * 15; // 15px spacing

            int startX = (getWidth() - gridWidth) / 2;
            int startY = 130; // Below header labels

            // Layout cards in grid (5 per row)
            int x = startX;
            int y = startY;
            int cardCount = 0;

            for (SelectableCardPanel cardPanel : cardPanels) {
                cardPanel.setBounds(x, y, CARD_WIDTH, CARD_HEIGHT);

                cardCount++;
                if (cardCount % CARDS_PER_ROW == 0) {
                    // Start new row
                    x = startX;
                    y += CARD_HEIGHT + 15;
                } else {
                    x += CARD_WIDTH + 10;
                }
            }

            // Add card panels to display
            for (SelectableCardPanel cardPanel : cardPanels) {
                if (cardPanel.getParent() == null) {
                    add(cardPanel);
                }
            }

            // Position Buy button below cards grid
            int buttonWidth = 250;
            int buttonHeight = 45;
            int buttonX = (getWidth() - buttonWidth) / 2;
            int buttonY = startY + gridHeight + 20; // 20px gap after cards
            btnBuy.setBounds(buttonX, buttonY, buttonWidth, buttonHeight);

            // Add button if not already added
            if (btnBuy.getParent() == null) {
                add(btnBuy);
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
         * Draw price label with coin icon below the card.
         */
        private void drawPriceLabel(Graphics2D g2d, int width, int height) {
            int price = BazaarPricing.getCardPrice(card);

            // Draw semi-transparent background
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(0, height - 30, width, 30);

            // Draw coin icon
            Image coinIcon = FSkin.getImage(FSkinProp.ICO_QUEST_COIN).getIcon().getImage();
            int iconSize = 20;
            int iconX = (width - iconSize - 40) / 2;
            g2d.drawImage(coinIcon, iconX, height - 25, iconSize, iconSize, null);

            // Draw price text
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            FontMetrics fm = g2d.getFontMetrics();
            String priceText = String.valueOf(price);
            int textX = iconX + iconSize + 5;
            int textY = height - 25 + fm.getAscent();
            g2d.drawString(priceText, textX, textY);
        }
    }
}
