package forge.screens.home.rogue;

import com.google.common.collect.ImmutableList;
import forge.deckchooser.FDeckViewer;
import forge.gamemodes.rogue.RogueTutorial;
import forge.item.PaperCard;
import forge.toolbox.FLabel;
import forge.toolbox.FOptionPane;
import forge.toolbox.FSkin.SkinnedPanel;
import forge.util.Localizer;
import forge.view.arcane.CardPanel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Dialog for selecting reward cards visually. Displays cards as images and allows selecting up to a
 * maximum number.
 */
public class CardRewardDialog {

  private static final int BASE_CARD_WIDTH = 240;  // Desired card width
  private static final int CARD_SPACING = 15;
  private static final int PADDING = 20;
  private static final int MAX_CARDS_PER_ROW = 4;
  private static final int MAX_ROWS = 2;
  private static final int HEADER_HEIGHT = 65;  // Space for labels (compact)
  private static final int MIN_DIALOG_WIDTH = 900;  // Minimum width for better zoom
  private static final int MIN_DIALOG_HEIGHT = 700; // Minimum height for better zoom

  private final String title;
  private final int maxSelections;
  private final Set<PaperCard> selectedCards;
  private final List<SelectableCardPanel> cardPanels;
  private final MainPanel panel;
  private final FLabel lblInfo;
  private final FLabel lblRewards;
  private FOptionPane optionPane;
  private CardZoomUtil zoomUtil;

  // Computed card dimensions (may be scaled down)
  private int cardWidth;
  private int cardHeight;

  /**
   * Create a card reward selection dialog.
   *
   * @param title         Dialog title
   * @param cards         List of cards to choose from
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

    // Calculate layout: max cards per row, max 2 rows
    int cardsPerRow = Math.min(cards.size(), MAX_CARDS_PER_ROW);
    int numRows = Math.min(MAX_ROWS, (int) Math.ceil(cards.size() / (double) cardsPerRow));

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
    int baseCardHeight = Math.round(BASE_CARD_WIDTH * CardPanel.ASPECT_RATIO);
    int desiredWidth = cardsPerRow * (BASE_CARD_WIDTH + CARD_SPACING) - CARD_SPACING + 2 * PADDING;
    int desiredHeight =
        numRows * (baseCardHeight + CARD_SPACING) - CARD_SPACING + HEADER_HEIGHT + PADDING;

    // Dialog size: apply minimum, then cap to screen bounds
    int dialogWidth = Math.min(Math.max(desiredWidth, MIN_DIALOG_WIDTH), maxDialogWidth);
    int dialogHeight = Math.min(Math.max(desiredHeight, MIN_DIALOG_HEIGHT), maxDialogHeight);

    // Initialize card dimensions (doLayout will recalculate based on actual size)
    cardWidth = BASE_CARD_WIDTH;
    cardHeight = baseCardHeight;

    Dimension dialogSize = new Dimension(dialogWidth, dialogHeight);
    panel.setPreferredSize(dialogSize);
    panel.setMinimumSize(dialogSize);
    panel.setSize(dialogSize);
  }

  private void revealAllCards() {
    for (SelectableCardPanel cardPanel : cardPanels) {
      cardPanel.flip();
    }
    panel.revalidate();
    panel.repaint();

    RogueTutorialHelper.showIfNotSeen(RogueTutorial.CARD_REWARDS);
  }

  private String getRewardsText() {
    return String.format("Victory! %s.", title);
  }

  /**
   * Show the dialog and return the selected cards.
   *
   * @return List of selected cards, or empty list if canceled
   */
  public List<PaperCard> show() {
    final Localizer localizer = Localizer.getInstance();
    final int VIEW_DECK_OPTION = 2;

    int result;
    do {
      optionPane = new FOptionPane(
          null,
          "Card Rewards",
          null,
          panel,
          ImmutableList.of(localizer.getMessage("lblOK"), localizer.getMessage("lblCancel"),
              "View Deck"),
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
      result = optionPane.getResult();
      optionPane.dispose();

      // If View Deck clicked, show deck and re-display dialog
      if (result == VIEW_DECK_OPTION) {
        showCurrentDeck();
      }
    } while (result == VIEW_DECK_OPTION);

    if (result == 0) {
      return new ArrayList<>(selectedCards);
    }
    return new ArrayList<>();
  }


  private void updateInfoLabel() {
    lblInfo.setText(getInfoText());
  }

  private String getInfoText() {
    return String.format("Select up to %d cards (%d selected)", maxSelections,
        selectedCards.size());
  }

  private void showCurrentDeck() {
    var currentRun = CSubmenuRogueMap.SINGLETON_INSTANCE.getCurrentRun();
    if (currentRun != null && currentRun.getCurrentDeck() != null) {
      FDeckViewer.show(currentRun.getCurrentDeck());
    }
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
      int totalWidth = getWidth();
      int totalHeight = getHeight();

      int y = PADDING;

      // Layout rewards label (compact)
      lblRewards.setBounds(PADDING, y, totalWidth - 2 * PADDING, 28);
      y += 28 + 3;

      // Layout info label (compact)
      lblInfo.setBounds(PADDING, y, totalWidth - 2 * PADDING, 22);
      y += 22 + 5;

      // Calculate available space for cards
      int availableWidth = totalWidth - 2 * PADDING;
      int availableHeight = totalHeight - y - 10; // small bottom margin

      // Calculate cards per row and number of rows
      int cardsPerRow = Math.min(MAX_CARDS_PER_ROW, cardPanels.size());
      int numRows = Math.min(MAX_ROWS, (int) Math.ceil(cardPanels.size() / (double) cardsPerRow));

      // Calculate scale to fit cards in available space
      int baseCardHeight = Math.round(BASE_CARD_WIDTH * CardPanel.ASPECT_RATIO);
      int desiredWidth = cardsPerRow * (BASE_CARD_WIDTH + CARD_SPACING) - CARD_SPACING;
      int desiredHeight = numRows * (baseCardHeight + CARD_SPACING) - CARD_SPACING;

      float widthScale =
          availableWidth > 0 ? Math.min(1.0f, (float) availableWidth / desiredWidth) : 1.0f;
      float heightScale =
          availableHeight > 0 ? Math.min(1.0f, (float) availableHeight / desiredHeight) : 1.0f;
      float scale = Math.min(widthScale, heightScale);

      // Apply scale to card dimensions
      cardWidth = Math.round(BASE_CARD_WIDTH * scale);
      cardHeight = Math.round(cardWidth * CardPanel.ASPECT_RATIO);

      // Layout card panels (top-aligned after header)
      int cardIndex = 0;
      int cardY = y;
      for (int row = 0; cardIndex < cardPanels.size() && row < MAX_ROWS; row++) {
        int cardsInThisRow = Math.min(cardsPerRow, cardPanels.size() - cardIndex);
        int rowWidth = cardsInThisRow * cardWidth + (cardsInThisRow - 1) * CARD_SPACING;
        int startX = (totalWidth - rowWidth) / 2;

        int x = startX;
        for (int col = 0; col < cardsInThisRow; col++) {
          SelectableCardPanel cardPanel = cardPanels.get(cardIndex);
          cardPanel.setBounds(x, cardY, cardWidth, cardHeight);
          x += cardWidth + CARD_SPACING;
          cardIndex++;
        }

        cardY += cardHeight + CARD_SPACING;
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
