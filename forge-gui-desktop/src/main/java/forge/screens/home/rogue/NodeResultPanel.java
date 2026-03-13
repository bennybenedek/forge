package forge.screens.home.rogue;

import forge.item.PaperCard;
import forge.toolbox.FLabel;
import forge.toolbox.FSkin.SkinnedPanel;
import forge.toolbox.FTextArea;
import forge.view.arcane.CardPanel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.miginfocom.swing.MigLayout;

/**
 * Result panel for node effects that may involve card lists.
 * Shows a result message and optional named card sections with card images.
 */
public class NodeResultPanel extends SkinnedPanel {

    private static final int CARD_WIDTH = 180;
    private static final int CARD_HEIGHT = Math.round(CARD_WIDTH * CardPanel.ASPECT_RATIO);
    private static final int CARD_SPACING = 12;

    private final List<ReadOnlyCardPanel> allCardPanels = new ArrayList<>();
    private CardUtil zoomUtil;

    public record CardSection(String label, List<PaperCard> cards) {}

    public NodeResultPanel(String message, List<CardSection> sections) {
        super(new MigLayout("insets 10, gap 0, wrap", "[grow, center]", ""));
        setOpaque(false);

        // Result text
        FTextArea txtMessage = new FTextArea(message);
        txtMessage.setFont(txtMessage.getFont().deriveFont(14f));
        add(txtMessage, "w 100%!, ax center, gap 0 0 0 10px, wrap");

        // Card sections
        for (CardSection section : sections) {
            if (section.cards() == null || section.cards().isEmpty()) continue;

            FLabel lblSection = new FLabel.Builder()
                    .text(section.label())
                    .fontSize(13)
                    .fontStyle(Font.BOLD)
                    .build();
            add(lblSection, "w 100%!, h 22px!, gap 5px 0 5px 3px, wrap");

            // Card row panel
            int rowWidth = section.cards().size() * (CARD_WIDTH + CARD_SPACING) - CARD_SPACING;
            SkinnedPanel cardRow = new SkinnedPanel(
                    new MigLayout("insets 0, gap " + CARD_SPACING + "px", "", ""));
            cardRow.setOpaque(false);

            for (PaperCard card : section.cards()) {
                ReadOnlyCardPanel cardPanel = new ReadOnlyCardPanel(card, () -> zoomUtil);
                allCardPanels.add(cardPanel);
                cardRow.add(cardPanel, "w " + CARD_WIDTH + "!, h " + CARD_HEIGHT + "!");
            }

            add(cardRow, "ax center, wrap");
        }

        // Calculate preferred size based on content, capped to screen bounds
        boolean hasCards = sections.stream().anyMatch(
                s -> s.cards() != null && !s.cards().isEmpty());
        int maxCardsInRow = sections.stream()
                .filter(s -> s.cards() != null)
                .mapToInt(s -> s.cards().size())
                .max().orElse(0);
        int desiredWidth = hasCards
                ? Math.max(650, maxCardsInRow * (CARD_WIDTH + CARD_SPACING) - CARD_SPACING + 40)
                : 650;
        int desiredHeight = 50; // message + padding
        for (CardSection section : sections) {
            if (section.cards() != null && !section.cards().isEmpty()) {
                desiredHeight += 25 + CARD_HEIGHT + 10; // label + cards + gap
            }
        }
        if (!hasCards) {
            desiredHeight = 50; // text-only
        }

        // Cap to usable screen space (same approach as CardRewardDialog)
        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration();
        Rectangle screenBounds = gc.getBounds();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
        int maxWidth = (int) ((screenBounds.width - screenInsets.left - screenInsets.right) * 0.9) - 80;
        int maxHeight = (int) ((screenBounds.height - screenInsets.top - screenInsets.bottom) * 0.9) - 80;

        Dimension size = new Dimension(
                Math.min(desiredWidth, maxWidth),
                Math.min(desiredHeight, maxHeight));
        setPreferredSize(size);
        setMinimumSize(size);
    }

    /**
     * Initialize zoom support. Must be called after the parent FOptionPane is created.
     */
    public void initZoom(Window parentWindow) {
        zoomUtil = new CardUtil(parentWindow);
        zoomUtil.setupZoomOverlay();
    }

    /**
     * Read-only card panel — starts face-up, no selection.
     */
    private static class ReadOnlyCardPanel extends SelectableCardPanelBase {

        ReadOnlyCardPanel(PaperCard card, Supplier<CardUtil> zoomSupplier) {
            super(card, zoomSupplier, false);
        }

        @Override
        protected void toggleSelection() {
            // No-op: display only
        }
    }
}
