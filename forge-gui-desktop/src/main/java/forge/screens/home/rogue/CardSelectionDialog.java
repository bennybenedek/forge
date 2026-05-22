package forge.screens.home.rogue;

import forge.card.CardType;
import forge.item.PaperCard;
import forge.toolbox.FLabel;
import forge.toolbox.FOptionPane;
import forge.toolbox.FScrollPane;
import forge.toolbox.FSkin.SkinnedPanel;
import forge.view.arcane.CardPanel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

/**
 * Dialog for selecting cards from a provided list. Cards are shown face-up in a scrollable grid.
 * The player must select between the provided minimum and maximum counts before confirming.
 */
public class CardSelectionDialog {

  private static final int BASE_CARD_WIDTH = 240;
  private static final int CARD_SPACING = 15;
  private static final int PADDING = 20;
  private static final int CARDS_PER_ROW = 4;
  private static final int HEADER_HEIGHT = 65;

  private final String title;
  private final String subtitle;
  private final int minSelections;
  private final int maxSelections;
  private final Set<PaperCard> selectedCards;
  private final List<SelectableCardPanel> cardPanels;
  private final GridPanel gridPanel;
  private final FLabel lblTitle;
  private final FLabel lblInfo;
  private FOptionPane optionPane;
  private CardUtil zoomUtil;

  public CardSelectionDialog(String title, String subtitle, List<PaperCard> cards,
      int minSelections, int maxSelections) {
    this.title = title;
    this.subtitle = subtitle;
    this.minSelections = minSelections;
    this.maxSelections = maxSelections;
    this.selectedCards = new HashSet<>();
    this.cardPanels = new ArrayList<>();

    lblTitle = new FLabel.Builder()
        .text(title)
        .fontSize(16)
        .fontStyle(Font.BOLD)
        .fontAlign(SwingConstants.CENTER)
        .build();

    lblInfo = new FLabel.Builder()
        .text(getInfoText())
        .fontSize(14)
        .fontAlign(SwingConstants.CENTER)
        .build();

    // Sort by type (Deck Editor order), then by CMC
    cards = new ArrayList<>(cards);
    cards.sort(Comparator
        .<PaperCard>comparingInt(CardSelectionDialog::typeOrder)
        .thenComparingInt(c -> c.getRules().getManaCost().getCMC()));

    // Grid panel holds all cards with custom layout
    gridPanel = new GridPanel();
    for (PaperCard card : cards) {
      SelectableCardPanel cardPanel = new SelectableCardPanel(card);
      cardPanels.add(cardPanel);
      gridPanel.add(cardPanel);
    }

    // Calculate grid preferred size
    int numRows = (int) Math.ceil(cards.size() / (double) CARDS_PER_ROW);
    int cardHeight = Math.round(BASE_CARD_WIDTH * CardPanel.ASPECT_RATIO);
    int gridWidth = CARDS_PER_ROW * (BASE_CARD_WIDTH + CARD_SPACING) - CARD_SPACING + 2 * PADDING;
    int gridHeight = numRows * (cardHeight + CARD_SPACING) - CARD_SPACING + PADDING;
    gridPanel.setPreferredSize(new Dimension(gridWidth, gridHeight));
  }

  public List<PaperCard> show() {
    // Wrapper panel with header + scroll pane
    SkinnedPanel wrapper = new SkinnedPanel(new BorderLayout());
    wrapper.setOpaque(false);

    // Header
    SkinnedPanel header = new SkinnedPanel(new GridLayout(2, 1));
    header.setOpaque(false);
    header.setPreferredSize(new Dimension(0, HEADER_HEIGHT));
    header.add(lblTitle);
    header.add(lblInfo);
    wrapper.add(header, BorderLayout.NORTH);

    // Scrollable card area
    FScrollPane scrollPane = new FScrollPane(gridPanel, false,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    wrapper.add(scrollPane, BorderLayout.CENTER);

    // Dialog size — fit screen
    GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
        .getDefaultScreenDevice().getDefaultConfiguration();
    Rectangle screenBounds = gc.getBounds();
    Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
    int usableWidth = screenBounds.width - screenInsets.left - screenInsets.right;
    int usableHeight = screenBounds.height - screenInsets.top - screenInsets.bottom;
    int dialogWidth = Math.min((int) (usableWidth * 0.9),
        CARDS_PER_ROW * (BASE_CARD_WIDTH + CARD_SPACING) + 2 * PADDING + 30); // +30 for scrollbar
    int dialogHeight = (int) (usableHeight * 0.85) - 80;

    Dimension dialogSize = new Dimension(dialogWidth, dialogHeight);
    wrapper.setPreferredSize(dialogSize);
    wrapper.setMinimumSize(dialogSize);

    optionPane = new FOptionPane(null, title, null, wrapper, List.of("OK"), 0);
    optionPane.getTitleBar().setVisible(false);
    optionPane.setButtonEnabled(0, minSelections == 0);

    zoomUtil = new CardUtil(optionPane);
    zoomUtil.setupZoomOverlay();

    optionPane.setVisible(true);
    optionPane.dispose();

    if (optionPane.getResult() == 0) {
      return new ArrayList<>(selectedCards);
    }
    return new ArrayList<>();
  }

  private void toggleCardSelection(SelectableCardPanel cardPanel) {
    PaperCard card = cardPanel.card;

    if (selectedCards.contains(card)) {
      selectedCards.remove(card);
      cardPanel.setSelected(false);
    } else if (selectedCards.size() < maxSelections) {
      selectedCards.add(card);
      cardPanel.setSelected(true);
    }

    updateInfoLabel();
    // Enable OK only when the current selection is within the allowed range.
    if (optionPane != null) {
      optionPane.setButtonEnabled(0, selectedCards.size() >= minSelections
          && selectedCards.size() <= maxSelections);
    }
  }

  private void updateInfoLabel() {
    lblInfo.setText(getInfoText());
  }

  private String getInfoText() {
    if (minSelections == maxSelections) {
      return String.format("%s (%d / %d selected)", subtitle, selectedCards.size(), maxSelections);
    }
    return String.format("%s (%d selected, choose %d to %d)", subtitle, selectedCards.size(),
        minSelections, maxSelections);
  }

  /** Returns type sort index matching Deck Editor order (GroupDef.CARD_TYPE). */
  private static int typeOrder(PaperCard c) {
    CardType type = c.getRules().getType();
    if (type.isLand()) return 6;
    if (type.isPlaneswalker()) return 0;
    if (type.isCreature()) return 1;
    if (type.isSorcery()) return 2;
    if (type.isInstant()) return 3;
    if (type.isArtifact()) return 4;
    if (type.isEnchantment()) return 5;
    if (type.isBattle()) return 7;
    return 8;
  }

  /** Grid panel that lays out cards in a fixed-width grid. */
  private class GridPanel extends SkinnedPanel {
    GridPanel() {
      super(null);
      setOpaque(false);
    }

    @Override
    public void doLayout() {
      int totalWidth = getWidth();
      int availableWidth = totalWidth - 2 * PADDING;

      // Scale cards to fit available width
      int desiredRowWidth = CARDS_PER_ROW * (BASE_CARD_WIDTH + CARD_SPACING) - CARD_SPACING;
      float scale = Math.min(1.0f, (float) availableWidth / desiredRowWidth);
      int cardWidth = Math.round(BASE_CARD_WIDTH * scale);
      int cardHeight = Math.round(cardWidth * CardPanel.ASPECT_RATIO);
      int spacing = Math.round(CARD_SPACING * scale);

      int y = PADDING / 2;
      int cardIndex = 0;
      while (cardIndex < cardPanels.size()) {
        int cardsInRow = Math.min(CARDS_PER_ROW, cardPanels.size() - cardIndex);
        int rowWidth = cardsInRow * cardWidth + (cardsInRow - 1) * spacing;
        int startX = (totalWidth - rowWidth) / 2;
        int x = startX;

        for (int col = 0; col < cardsInRow; col++) {
          cardPanels.get(cardIndex).setBounds(x, y, cardWidth, cardHeight);
          x += cardWidth + spacing;
          cardIndex++;
        }
        y += cardHeight + spacing;
      }

      // Update preferred size so scroll pane knows full content height
      setPreferredSize(new Dimension(totalWidth, y + PADDING / 2));
    }
  }

  private class SelectableCardPanel extends SelectableCardPanelBase {
    SelectableCardPanel(PaperCard card) {
      super(card, () -> CardSelectionDialog.this.zoomUtil, false);
    }

    @Override
    protected void toggleSelection() {
      toggleCardSelection(this);
    }
  }
}
