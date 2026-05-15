package forge.screens.home.rogue;

import forge.gamemodes.rogue.RogueConfig;
import forge.gui.CardPicturePanel;
import forge.item.PaperCard;
import forge.model.FModel;
import forge.toolbox.FSkin;
import forge.toolbox.FSkin.SkinnedPanel;
import forge.view.arcane.CardPanel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
import javax.swing.JComponent;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Shared anchored card-preview popup for Rogue Commander dialogs.
 */
public class RogueCardPreviewPopup {
    private static final int PREVIEW_WIDTH = 220;
    private static final int PREVIEW_HEIGHT = Math.round(PREVIEW_WIDTH * CardPanel.ASPECT_RATIO);
    private static final int PREVIEW_GAP = 8;
    private static final int HIDE_DELAY_MS = 180;
    private static final int ZOOM_WATCH_DELAY_MS = 120;

    private final CardPicturePanel picturePanel = new CardPicturePanel();
    private final PreviewPanel previewPanel = new PreviewPanel();
    private final Timer hideTimer = new Timer(HIDE_DELAY_MS, e -> hideNow());
    private final Timer zoomWatchTimer = new Timer(ZOOM_WATCH_DELAY_MS, e -> handleZoomWatch());
    private Popup popup;
    private CardUtil zoomUtil;
    private PaperCard currentCard;
    private JComponent activeSourceComponent;
    private boolean hoveringSource;
    private boolean hoveringPreview;

    public RogueCardPreviewPopup() {
        picturePanel.setOpaque(false);
        picturePanel.setPreferredSize(new Dimension(PREVIEW_WIDTH, PREVIEW_HEIGHT));

        previewPanel.setOpaque(true);
        previewPanel.setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));
        previewPanel.setBorder(new FSkin.LineSkinBorder(FSkin.getColor(FSkin.Colors.CLR_BORDERS)));
        previewPanel.add(picturePanel, BorderLayout.CENTER);
        previewPanel.setPreferredSize(new Dimension(PREVIEW_WIDTH, PREVIEW_HEIGHT));

        hideTimer.setRepeats(false);
        zoomWatchTimer.setRepeats(true);

        MouseAdapter previewHoverListener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hoveringPreview = true;
                previewPanel.setHovered(true);
                cancelHide();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoveringPreview = false;
                previewPanel.setHovered(false);
                scheduleHide();
            }
        };
        previewPanel.addMouseListener(previewHoverListener);
        picturePanel.addMouseListener(previewHoverListener);

        previewPanel.addMouseWheelListener(e -> handleWheel(previewPanel, currentCard, e.getWheelRotation()));
        picturePanel.addMouseWheelListener(e -> handleWheel(previewPanel, currentCard, e.getWheelRotation()));
    }

    public void attachTo(JComponent component, String cardName) {
        PaperCard previewCard = resolveCard(cardName);
        if (component == null || previewCard == null) {
            return;
        }

        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hoveringSource = true;
                cancelHide();
                show(component, previewCard);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoveringSource = false;
                scheduleHide();
            }
        });

        component.addMouseWheelListener(e -> handleWheel(component, previewCard, e.getWheelRotation()));
    }

    public void hide() {
        hoveringSource = false;
        hoveringPreview = false;
        cancelHide();
        hideNow();
    }

    private void show(JComponent component, PaperCard previewCard) {
        cancelHide();
        hideNow();
        ensureZoomUtil(component);
        activeSourceComponent = component;
        currentCard = previewCard;
        picturePanel.setItem(previewCard);

        Point anchor = component.getLocationOnScreen();
        Dimension size = previewPanel.getPreferredSize();
        GraphicsConfiguration gc = component.getGraphicsConfiguration();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);

        int screenLeft = gc.getBounds().x + insets.left;
        int screenTop = gc.getBounds().y + insets.top;
        int screenRight = gc.getBounds().x + gc.getBounds().width - insets.right;
        int screenBottom = gc.getBounds().y + gc.getBounds().height - insets.bottom;

        int x = anchor.x + component.getWidth() + PREVIEW_GAP;
        if (x + size.width > screenRight) {
            x = Math.max(screenLeft, anchor.x - size.width - PREVIEW_GAP);
        }

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
        currentCard = null;
        activeSourceComponent = null;
        previewPanel.setHovered(false);
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
        previewPanel.setHovered(hoveringPreview);
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

    private class PreviewPanel extends SkinnedPanel {
        private boolean hovered;

        private PreviewPanel() {
            super(new BorderLayout());
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
}
