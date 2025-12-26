package forge.screens.home.rogue;

import forge.ImageCache;
import forge.ImageKeys;
import forge.gui.CardPicturePanel;
import forge.item.PaperCard;
import forge.toolbox.FSkin.SkinnedPanel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.function.Supplier;
import javax.swing.Timer;

/**
 * Base class for selectable card panels with flip animation.
 * Provides common functionality for card selection dialogs.
 */
public abstract class SelectableCardPanelBase extends SkinnedPanel {
    protected final PaperCard card;
    protected final CardPicturePanel cardPicture;
    protected boolean selected;
    protected boolean faceDown;
    protected boolean animating;
    protected double scaleX;
    private Timer animationTimer;
    private final Supplier<CardZoomUtil> zoomUtilSupplier;

    /**
     * Create a selectable card panel.
     * @param card The card to display
     * @param zoomUtilSupplier Supplier for zoom utility (accessed at runtime, can return null)
     */
    public SelectableCardPanelBase(PaperCard card, Supplier<CardZoomUtil> zoomUtilSupplier) {
        super(null);
        this.card = card;
        this.selected = false;
        this.faceDown = true; // Start face-down
        this.animating = false;
        this.scaleX = 1.0;
        this.cardPicture = new CardPicturePanel();
        this.zoomUtilSupplier = zoomUtilSupplier;

        setOpaque(false);
        setLayout(null); // Manual layout

        // Set the card to display (start with card back)
        updateCardDisplay();
        cardPicture.setOpaque(false);
        add(cardPicture);

        // Add mouse listener for selection
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Toggle selection when revealed
                if (!faceDown) {
                    toggleSelection();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setCursor(Cursor.getDefaultCursor());
            }
        });

        // Add mouse wheel listener for card zoom (only when revealed)
        addMouseWheelListener(e -> {
            if (!faceDown && e.getWheelRotation() < 0) {
                CardZoomUtil zoomUtil = zoomUtilSupplier != null ? zoomUtilSupplier.get() : null;
                if (zoomUtil != null) {
                    zoomUtil.showZoom(card);
                }
            }
        });
    }

    /**
     * Update the card display (face-up or face-down).
     */
    protected void updateCardDisplay() {
        if (faceDown) {
            // Show card back
            BufferedImage cardBack = ImageCache.getOriginalImage(
                ImageKeys.getTokenKey(ImageKeys.HIDDEN_CARD), true, null);
            cardPicture.setItem(cardBack);
        } else {
            // Show actual card
            cardPicture.setItem(card);
        }
    }

    /**
     * Start the flip animation to reveal the card.
     */
    public void flip() {
        if (animating || !faceDown) {
            return; // Already revealed or animating
        }

        animating = true;
        final int animationDuration = 300; // milliseconds
        final int framesPerSecond = 60;
        final int frameDelay = 1000 / framesPerSecond;
        final int totalFrames = animationDuration / frameDelay;
        final double scaleStep = 2.0 / totalFrames; // Scale from 1.0 to -1.0 to 1.0

        final int[] currentFrame = {0};
        final boolean[] imageFlipped = {false};

        animationTimer = new Timer(frameDelay, e -> {
            currentFrame[0]++;

            // Calculate scale (1.0 -> 0.0 -> 1.0)
            if (currentFrame[0] <= totalFrames / 2) {
                // First half: shrink from 1.0 to 0.0
                scaleX = 1.0 - (currentFrame[0] * scaleStep);
            } else {
                // Second half: expand from 0.0 to 1.0
                scaleX = (currentFrame[0] - totalFrames / 2) * scaleStep;
            }

            // Flip the card image at the middle of the animation
            if (!imageFlipped[0] && currentFrame[0] >= totalFrames / 2) {
                faceDown = false;
                updateCardDisplay();
                imageFlipped[0] = true;
            }

            repaint();

            // End animation
            if (currentFrame[0] >= totalFrames) {
                animationTimer.stop();
                animating = false;
                scaleX = 1.0;
                repaint();
            }
        });

        animationTimer.start();
    }

    /**
     * Set the selection state.
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }

    /**
     * Toggle the selection state.
     * Subclasses can override to add custom logic (e.g., budget checking).
     */
    protected abstract void toggleSelection();

    @Override
    public void doLayout() {
        // Make card picture fill the panel
        cardPicture.setBounds(0, 0, getWidth(), getHeight());
    }

    @Override
    public void paint(Graphics g) {
        final Graphics2D g2d = (Graphics2D) g;
        final int width = getWidth();
        final int height = getHeight();

        // Apply horizontal scale transformation if animating
        if (animating && scaleX != 1.0) {
            // Save the original transform
            AffineTransform originalTransform = g2d.getTransform();

            // Create scale transform centered on the card
            AffineTransform scaleTransform = new AffineTransform();
            scaleTransform.translate(width / 2.0, 0); // Move origin to center
            scaleTransform.scale(Math.max(0.01, scaleX), 1.0); // Scale horizontally (min 0.01 to avoid zero)
            scaleTransform.translate(-width / 2.0, 0); // Move origin back

            g2d.transform(scaleTransform);
            super.paint(g);
            g2d.setTransform(originalTransform);
        } else {
            super.paint(g);
        }

        // Draw selection indicators ON TOP of everything (only if not animating)
        if (selected && !animating) {
            drawSelectionHighlight(g2d, width, height);
        }
    }

    /**
     * Draw the selection highlight (green border + checkmark).
     */
    protected void drawSelectionHighlight(Graphics2D g2d, int width, int height) {
        // Draw thick border
        g2d.setColor(new Color(0, 255, 0, 200)); // Green with transparency
        g2d.setStroke(new BasicStroke(6)); // Thicker border
        g2d.drawRect(3, 3, width - 6, height - 6);

        // Draw checkmark in top-right corner
        int checkSize = 30;
        int checkX = width - checkSize - 8;
        int checkY = 8;

        // Draw circle background
        g2d.setColor(new Color(0, 200, 0, 230));
        g2d.fillOval(checkX, checkY, checkSize, checkSize);

        // Draw checkmark
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3));
        int[] xPoints = {checkX + 7, checkX + 12, checkX + 23};
        int[] yPoints = {checkY + 15, checkY + 20, checkY + 10};
        g2d.drawPolyline(xPoints, yPoints, 3);
    }
}
