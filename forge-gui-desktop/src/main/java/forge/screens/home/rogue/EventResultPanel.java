package forge.screens.home.rogue;

import forge.gui.CardPicturePanel;
import forge.item.PaperCard;
import forge.toolbox.FLabel;
import forge.toolbox.FSkin.SkinnedPanel;
import forge.view.arcane.CardPanel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;

/**
 * Result panel for event effects that may involve card lists.
 * Shows a result message and optional named card sections with card images.
 */
public class EventResultPanel extends SkinnedPanel {

    private static final int CARD_WIDTH = 120;
    private static final int CARD_HEIGHT = Math.round(CARD_WIDTH * CardPanel.ASPECT_RATIO);
    private static final int CARD_SPACING = 8;

    private final List<ReadOnlyCardPanel> allCardPanels = new ArrayList<>();
    private CardUtil zoomUtil;

    public record CardSection(String label, List<PaperCard> cards) {}

    public EventResultPanel(String message, List<CardSection> sections) {
        super(new MigLayout("insets 10, gap 0, wrap", "[grow, center]", ""));
        setOpaque(false);

        // Result text
        FLabel lblMessage = new FLabel.Builder()
                .text(message)
                .fontSize(14)
                .fontAlign(SwingConstants.CENTER)
                .build();
        add(lblMessage, "w 100%!, h 28px!, ax center, gap 0 0 0 10px, wrap");

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

        // Calculate preferred size
        int width = 650;
        int height = 50; // message + padding
        for (CardSection section : sections) {
            if (section.cards() != null && !section.cards().isEmpty()) {
                height += 25 + CARD_HEIGHT + 10; // label + cards + gap
            }
        }
        if (sections.isEmpty() || sections.stream().allMatch(
                s -> s.cards() == null || s.cards().isEmpty())) {
            height = 50; // text-only
        }
        Dimension size = new Dimension(width, height);
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
            super(card, zoomSupplier);
            // Start face-up immediately (no flip animation)
            faceDown = false;
            updateCardDisplay();
        }

        @Override
        protected void toggleSelection() {
            // No-op: display only
        }
    }
}
