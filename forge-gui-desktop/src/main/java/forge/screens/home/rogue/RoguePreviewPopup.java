package forge.screens.home.rogue;

import forge.gamemodes.rogue.KeywordHint;
import forge.gamemodes.rogue.PreviewReference;
import forge.gamemodes.rogue.PreviewReferenceType;
import forge.gamemodes.rogue.RogueConfig;
import forge.gui.CardPicturePanel;
import forge.item.PaperCard;
import forge.model.FModel;
import forge.toolbox.FLabel;
import forge.toolbox.FSkin;
import forge.toolbox.FTextArea;
import forge.toolbox.FSkin.SkinnedPanel;
import forge.view.arcane.CardPanel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Shared anchored preview popup for Rogue Commander dialogs.
 */
public class RoguePreviewPopup {
    private static final int CARD_PREVIEW_WIDTH = 220;
    private static final int CARD_PREVIEW_HEIGHT = Math.round(CARD_PREVIEW_WIDTH * CardPanel.ASPECT_RATIO);
    private static final int KEYWORD_PREVIEW_WIDTH = 220;
    private static final int KEYWORD_PREVIEW_MIN_HEIGHT = 120;
    private static final int KEYWORD_TITLE_TOP = 10;
    private static final int KEYWORD_SIDE_PADDING = 12;
    private static final int KEYWORD_BODY_TOP = 14;
    private static final int KEYWORD_BODY_BOTTOM = 12;
    private static final int PREVIEW_GAP = 8;
    private static final int HIDE_DELAY_MS = 180;
    private static final int ZOOM_WATCH_DELAY_MS = 120;

    private final PreviewClusterPanel previewPanel = new PreviewClusterPanel();
    private final Timer hideTimer = new Timer(HIDE_DELAY_MS, e -> hideNow());
    private final Timer zoomWatchTimer = new Timer(ZOOM_WATCH_DELAY_MS, e -> handleZoomWatch());
    private final List<CardPreviewPanel> currentCardPanels = new ArrayList<>();
    private Popup popup;
    private CardUtil zoomUtil;
    private JComponent activeSourceComponent;
    private boolean hoveringSource;
    private boolean hoveringPreview;

    public RoguePreviewPopup() {
        previewPanel.setOpaque(true);
        previewPanel.setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));
        registerHoverTracking(previewPanel);

        hideTimer.setRepeats(false);
        zoomWatchTimer.setRepeats(true);
    }

    public void attachTo(JComponent component, List<PreviewReference> references) {
        List<ResolvedPreviewItem> previewItems = resolvePreviewItems(references);
        if (component == null || previewItems.isEmpty()) {
            return;
        }

        PaperCard primaryCard = previewItems.stream()
                .filter(ResolvedPreviewItem::isCard)
                .map(ResolvedPreviewItem::card)
                .findFirst()
                .orElse(null);

        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hoveringSource = true;
                cancelHide();
                show(component, previewItems, primaryCard);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoveringSource = false;
                scheduleHide();
            }
        });

        component.addMouseWheelListener(e -> handleWheel(component, primaryCard, e.getWheelRotation()));
    }

    public void hide() {
        hoveringSource = false;
        hoveringPreview = false;
        cancelHide();
        hideNow();
    }

    private void show(JComponent component, List<ResolvedPreviewItem> previewItems, PaperCard primaryCard) {
        cancelHide();
        hideNow();
        ensureZoomUtil(component);
        activeSourceComponent = component;

        Point anchor = component.getLocationOnScreen();
        GraphicsConfiguration gc = component.getGraphicsConfiguration();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);

        int screenTop = gc.getBounds().y + insets.top;
        int screenRight = gc.getBounds().x + gc.getBounds().width - insets.right;
        int screenBottom = gc.getBounds().y + gc.getBounds().height - insets.bottom;

        int x = anchor.x + component.getWidth() + PREVIEW_GAP;
        int availableWidth = Math.max(0, screenRight - x);
        buildPreviewPanels(previewItems, availableWidth);
        if (previewPanel.getComponentCount() == 0) {
            return;
        }

        Dimension size = previewPanel.getPreferredSize();
        int y = anchor.y + (component.getHeight() - size.height) / 2;
        if (y + size.height > screenBottom) {
            y = Math.max(screenTop, screenBottom - size.height);
        }
        if (y < screenTop) {
            y = screenTop;
        }

        popup = PopupFactory.getSharedInstance().getPopup(component, previewPanel, x, y);
        popup.show();
    }

    private void buildPreviewPanels(List<ResolvedPreviewItem> previewItems, int availableWidth) {
        previewPanel.removeAll();
        currentCardPanels.clear();

        List<JComponent> panels = createPreviewPanels(previewItems);
        List<JComponent> visiblePanels = capPreviewPanels(panels, availableWidth);

        boolean firstItem = true;
        for (JComponent panel : visiblePanels) {
            if (!firstItem) {
                previewPanel.add(Box.createHorizontalStrut(PREVIEW_GAP));
            }

            if (panel instanceof CardPreviewPanel cardPreviewPanel) {
                currentCardPanels.add(cardPreviewPanel);
            }
            previewPanel.add(panel);
            firstItem = false;
        }

        previewPanel.revalidate();
        previewPanel.repaint();
    }

    private void ensureZoomUtil(JComponent component) {
        if (zoomUtil != null) {
            return;
        }

        Window window = SwingUtilities.getWindowAncestor(component);
        if (window != null) {
            zoomUtil = new CardUtil(window);
            zoomUtil.setupZoomOverlay();
        }
    }

    private void handleWheel(JComponent component, PaperCard previewCard, int wheelRotation) {
        if (previewCard == null) {
            return;
        }

        ensureZoomUtil(component);
        if (zoomUtil == null) {
            return;
        }

        if (wheelRotation < 0) {
            zoomUtil.showZoom(previewCard);
        } else if (wheelRotation > 0 && isZoomActive()) {
            zoomUtil.closeZoom();
        }
    }

    private void scheduleHide() {
        refreshHoverStateFromPointer();
        if (hoveringSource || hoveringPreview) {
            return;
        }

        if (isZoomActive()) {
            if (!zoomWatchTimer.isRunning()) {
                zoomWatchTimer.start();
            }
        } else {
            hideTimer.restart();
        }
    }

    private void cancelHide() {
        if (hideTimer.isRunning()) {
            hideTimer.stop();
        }
        if (zoomWatchTimer.isRunning()) {
            zoomWatchTimer.stop();
        }
    }

    private void hideNow() {
        if (popup != null) {
            popup.hide();
            popup = null;
        }
        activeSourceComponent = null;
        currentCardPanels.clear();
        previewPanel.removeAll();
        previewPanel.revalidate();
        previewPanel.repaint();
    }

    private void handleZoomWatch() {
        refreshHoverStateFromPointer();
        if (hoveringSource || hoveringPreview) {
            zoomWatchTimer.stop();
            return;
        }
        if (!isZoomActive()) {
            zoomWatchTimer.stop();
            hideTimer.restart();
        }
    }

    private boolean isZoomActive() {
        return zoomUtil != null && zoomUtil.isZooming();
    }

    private void refreshHoverStateFromPointer() {
        hoveringSource = isPointerOver(activeSourceComponent);
        hoveringPreview = isPointerOver(previewPanel);
        currentCardPanels.forEach(panel -> panel.setHovered(isPointerOver(panel)));
    }

    private static boolean isPointerOver(JComponent component) {
        if (component == null || !component.isShowing()) {
            return false;
        }

        Point pointer = MouseInfo.getPointerInfo() == null ? null : MouseInfo.getPointerInfo().getLocation();
        if (pointer == null) {
            return false;
        }

        Point localPoint = new Point(pointer);
        SwingUtilities.convertPointFromScreen(localPoint, component);
        return component.contains(localPoint);
    }

    private List<JComponent> createPreviewPanels(List<ResolvedPreviewItem> previewItems) {
        List<JComponent> panels = new ArrayList<>(previewItems.size());
        for (ResolvedPreviewItem item : previewItems) {
            if (item.isCard()) {
                panels.add(new CardPreviewPanel(item.card()));
            } else if (item.isKeyword()) {
                panels.add(new KeywordPreviewPanel(item.keywordHint()));
            }
        }
        return panels;
    }

    private List<JComponent> capPreviewPanels(List<JComponent> panels, int availableWidth) {
        if (panels.isEmpty()) {
            return List.of();
        }

        if (availableWidth <= 0) {
            return List.of();
        }

        if (calculateWidth(panels) <= availableWidth) {
            return panels;
        }

        List<JComponent> visiblePanels = new ArrayList<>();
        for (JComponent panel : panels) {
            List<JComponent> candidatePanels = new ArrayList<>(visiblePanels);
            candidatePanels.add(panel);
            if (calculateWidth(candidatePanels) <= availableWidth) {
                visiblePanels.add(panel);
            } else {
                break;
            }
        }
        return visiblePanels;
    }

    private int calculateWidth(List<JComponent> panels) {
        int width = 0;
        boolean firstItem = true;
        for (JComponent panel : panels) {
            if (!firstItem) {
                width += PREVIEW_GAP;
            }
            width += panel.getPreferredSize().width;
            firstItem = false;
        }
        return width;
    }

    private static PaperCard resolveCard(String cardName) {
        if (cardName == null || cardName.isBlank()) {
            return null;
        }

        RogueConfig.loadRogueCards();
        PaperCard card = FModel.getMagicDb().getCommonCards().getCard(cardName);
        if (card == null) {
            card = FModel.getMagicDb().getVariantCards().getCard(cardName);
        }
        return card;
    }

    private List<ResolvedPreviewItem> resolvePreviewItems(List<PreviewReference> references) {
        if (references == null || references.isEmpty()) {
            return List.of();
        }

        List<ResolvedPreviewItem> items = new ArrayList<>();
        for (PreviewReference reference : references) {
            if (reference.type() == PreviewReferenceType.CARD) {
                PaperCard card = resolveCard(reference.token());
                if (card != null) {
                    items.add(ResolvedPreviewItem.forCard(card));
                }
            } else if (reference.type() == PreviewReferenceType.KEYWORD) {
                KeywordHint hint = KeywordHint.fromToken(reference.token());
                if (hint != null) {
                    items.add(ResolvedPreviewItem.forKeyword(hint));
                }
            }
        }
        return items;
    }

    private void registerHoverTracking(JComponent component) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                SwingUtilities.invokeLater(() -> {
                    refreshHoverStateFromPointer();
                    cancelHide();
                });
            }

            @Override
            public void mouseExited(MouseEvent e) {
                SwingUtilities.invokeLater(() -> {
                    refreshHoverStateFromPointer();
                    scheduleHide();
                });
            }
        });
    }

    private record ResolvedPreviewItem(PaperCard card, KeywordHint keywordHint) {
        private static ResolvedPreviewItem forCard(PaperCard card) {
            return new ResolvedPreviewItem(card, null);
        }

        private static ResolvedPreviewItem forKeyword(KeywordHint keywordHint) {
            return new ResolvedPreviewItem(null, keywordHint);
        }

        private boolean isCard() {
            return card != null;
        }

        private boolean isKeyword() {
            return keywordHint != null;
        }
    }

    private class PreviewClusterPanel extends SkinnedPanel {
        private PreviewClusterPanel() {
            super(new FlowLayout(FlowLayout.LEFT, 0, 0));
            setOpaque(true);
            setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));
        }
    }

    private class CardPreviewPanel extends SkinnedPanel {
        private final PaperCard card;
        private final CardPicturePanel picturePanel = new CardPicturePanel();
        private boolean hovered;

        private CardPreviewPanel(PaperCard card) {
            super(new BorderLayout());
            this.card = card;

            setOpaque(true);
            setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));
            setBorder(new FSkin.LineSkinBorder(FSkin.getColor(FSkin.Colors.CLR_BORDERS)));
            setPreferredSize(new Dimension(CARD_PREVIEW_WIDTH, CARD_PREVIEW_HEIGHT));

            picturePanel.setOpaque(false);
            picturePanel.setPreferredSize(new Dimension(CARD_PREVIEW_WIDTH, CARD_PREVIEW_HEIGHT));
            picturePanel.setItem(card);
            add(picturePanel, BorderLayout.CENTER);

            registerHoverTracking(this);
            registerHoverTracking(picturePanel);
            addMouseWheelListener(e -> handleWheel(this, this.card, e.getWheelRotation()));
            picturePanel.addMouseWheelListener(e -> handleWheel(this, this.card, e.getWheelRotation()));
        }

        private void setHovered(boolean hovered) {
            this.hovered = hovered;
            repaint();
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (!hovered) {
                return;
            }

            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(new Color(255, 215, 0));
            g2d.setStroke(new BasicStroke(4));
            g2d.drawRect(3, 3, getWidth() - 6, getHeight() - 6);
            g2d.dispose();
        }
    }

    private class KeywordPreviewPanel extends SkinnedPanel {
        private KeywordPreviewPanel(KeywordHint hint) {
            super(new BorderLayout());
            setOpaque(true);
            setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));
            setBorder(new FSkin.LineSkinBorder(FSkin.getColor(FSkin.Colors.CLR_BORDERS)));

            FLabel title = new FLabel.Builder()
                    .text(hint.getTitle())
                    .fontSize(16)
                    .fontStyle(Font.BOLD)
                    .fontAlign(SwingConstants.LEFT)
                    .build();
            title.setBorder(BorderFactory.createEmptyBorder(KEYWORD_TITLE_TOP, KEYWORD_SIDE_PADDING, 0, KEYWORD_SIDE_PADDING));

            FTextArea body = new FTextArea(hint.getHintText());
            body.setFont(body.getFont().deriveFont(13f));
            body.setBorder(BorderFactory.createEmptyBorder(KEYWORD_BODY_TOP, KEYWORD_SIDE_PADDING, KEYWORD_BODY_BOTTOM, KEYWORD_SIDE_PADDING));

            int contentWidth = KEYWORD_PREVIEW_WIDTH - (KEYWORD_SIDE_PADDING * 2);
            body.setSize(new Dimension(contentWidth, Short.MAX_VALUE));

            int preferredHeight = title.getPreferredSize().height + body.getPreferredSize().height;
            setPreferredSize(new Dimension(KEYWORD_PREVIEW_WIDTH, Math.max(KEYWORD_PREVIEW_MIN_HEIGHT, preferredHeight)));

            add(title, BorderLayout.NORTH);
            add(body, BorderLayout.CENTER);

            registerHoverTracking(this);
            registerHoverTracking(title);
            registerHoverTracking(body);
        }
    }
}
