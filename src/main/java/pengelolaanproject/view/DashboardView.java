package pengelolaanproject.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Abstract base class for dashboards in the project management system.
 * Defines the contract for view rendering and provides shared premium dark-theme assets.
 */
public abstract class DashboardView extends JPanel {

    // Harmonious Premium Palette Colors
    public static final Color BG_GRADIENT_START = new Color(34, 40, 49);
    public static final Color BG_GRADIENT_END = new Color(26, 30, 37);
    public static final Color CARD_BG = new Color(57, 62, 70, 200);
    public static final Color TEXT_PRIMARY = new Color(238, 238, 238);
    public static final Color TEXT_SECONDARY = new Color(158, 162, 169);
    public static final Color ACCENT_CYAN = new Color(0, 173, 181);
    public static final Color ACCENT_PURPLE = new Color(0, 110, 120);
    public static final Color INPUT_BG = new Color(34, 40, 49);
    public static final Color INPUT_BORDER = new Color(57, 62, 70);
    public static final Color ERROR_COLOR = new Color(230, 57, 70);

    public DashboardView() {
        setOpaque(false);
    }

    /**
     * Abstract method to render the UI components of the dashboard.
     * Concrete subclasses must implement this.
     */
    public abstract void render();

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Render base deep dark gradient background
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        GradientPaint gp = new GradientPaint(0, 0, BG_GRADIENT_START, w, h, BG_GRADIENT_END);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, w, h);
        g2d.dispose();
    }

    // =========================================================================
    // Reusable Custom Styling Components for Subclasses
    // =========================================================================

    /**
     * Glowing semi-translucent container representing a glassmorphic card.
     */
    public static class GlassCard extends JPanel {
        private int cornerRadius = 24;

        public GlassCard() {
            setOpaque(false);
        }

        public GlassCard(int cornerRadius) {
            this.cornerRadius = cornerRadius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Fill card with semi-translucent elevated color
            g2d.setColor(CARD_BG);
            g2d.fill(new RoundRectangle2D.Double(0, 0, w, h, cornerRadius, cornerRadius));

            // Subtle glowing border
            g2d.setStroke(new BasicStroke(1.5f));
            GradientPaint borderPaint = new GradientPaint(
                    0, 0, new Color(255, 255, 255, 60),
                    0, h, new Color(255, 255, 255, 10)
            );
            g2d.setPaint(borderPaint);
            g2d.draw(new RoundRectangle2D.Double(0.75, 0.75, w - 1.5, h - 1.5, cornerRadius, cornerRadius));

            g2d.dispose();
        }
    }

    /**
     * Modern animated gradient button with interactive hover/press states.
     */
    public static class GradientButton extends JButton {
        private boolean hovered = false;
        private boolean pressed = false;
        private int cornerRadius = 12;

        public GradientButton(String text) {
            super(text);
            initButton();
        }

        public GradientButton(String text, int cornerRadius) {
            super(text);
            this.cornerRadius = cornerRadius;
            initButton();
        }

        private void initButton() {
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(TEXT_PRIMARY);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    pressed = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    pressed = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            Color start = ACCENT_PURPLE;
            Color end = ACCENT_CYAN;

            if (pressed) {
                start = start.darker();
                end = end.darker();
            } else if (hovered) {
                // Glow/lighten accent on hover
                start = new Color(0, 140, 150);
                end = new Color(0, 210, 220);
            }

            GradientPaint gp = new GradientPaint(0, 0, start, w, h, end);
            g2d.setPaint(gp);
            g2d.fill(new RoundRectangle2D.Double(0, 0, w, h, cornerRadius, cornerRadius));

            if (hovered) {
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.draw(new RoundRectangle2D.Double(0.75, 0.75, w - 1.5, h - 1.5, cornerRadius, cornerRadius));
            }

            super.paintComponent(g);
            g2d.dispose();
        }
    }
}
