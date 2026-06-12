package pengelolaanproject.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * High-fidelity, premium glassmorphic dark-themed login screen for the application.
 * Built using custom Swing rendering for modern design aesthetics.
 */
public class AuthView extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnRegisterLink;
    private JLabel lblError;

    // Palette Colors
    private static final Color BG_GRADIENT_START = new Color(34, 40, 49);
    private static final Color BG_GRADIENT_END = new Color(26, 30, 37);
    private static final Color CARD_BG = new Color(57, 62, 70, 200);
    private static final Color TEXT_PRIMARY = new Color(238, 238, 238);
    private static final Color TEXT_SECONDARY = new Color(158, 162, 169);
    private static final Color ACCENT_CYAN = new Color(0, 173, 181);
    private static final Color ACCENT_PURPLE = new Color(0, 110, 120);
    private static final Color INPUT_BG = new Color(34, 40, 49);
    private static final Color INPUT_BORDER = new Color(57, 62, 70);
    private static final Color ERROR_COLOR = new Color(230, 57, 70);

    public AuthView() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Sistem Manajemen Proyek - Login Aman");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main Background Panel with Gradient
        GradientPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new GridBagLayout());
        setContentPane(mainPanel);

        // Glassmorphic Card Container
        GlassCard cardPanel = new GlassCard();
        cardPanel.setPreferredSize(new Dimension(380, 480));
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBorder(new EmptyBorder(40, 30, 40, 30));

//        // 1. Header (Logo / Title / Subtitle)
//        JLabel lblLogo = new JLabel("", SwingConstants.CENTER);
//        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 36));
//        lblLogo.setForeground(ACCENT_CYAN);
//        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("Selamat Datang", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Masuk untuk mengelola ruang kerja Anda", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(TEXT_SECONDARY);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 2. Input Fields Panel
        JPanel inputsPanel = new JPanel();
        inputsPanel.setOpaque(false);
        inputsPanel.setLayout(new GridLayout(4, 1, 0, 8));
        inputsPanel.setMaximumSize(new Dimension(320, 180));
        inputsPanel.setBorder(new EmptyBorder(30, 0, 15, 0));

        // Username Field
        JLabel lblUser = new JLabel("USERNAME");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblUser.setForeground(ACCENT_CYAN);
        txtUsername = createStyledTextField("Masukkan username Anda");

        // Password Field
        JLabel lblPass = new JLabel("PASSWORD");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblPass.setForeground(ACCENT_CYAN);
        txtPassword = createStyledPasswordField();

        inputsPanel.add(lblUser);
        inputsPanel.add(txtUsername);
        inputsPanel.add(lblPass);
        inputsPanel.add(txtPassword);

        // 3. Error Area
        lblError = new JLabel(" ", SwingConstants.CENTER);
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblError.setForeground(ERROR_COLOR);
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblError.setMinimumSize(new Dimension(320, 20));
        lblError.setPreferredSize(new Dimension(320, 20));
        lblError.setMaximumSize(new Dimension(320, 20));

        // 4. Login Button
        btnLogin = new GradientButton("MASUK");
        btnLogin.setMaximumSize(new Dimension(320, 45));
        btnLogin.setPreferredSize(new Dimension(320, 45));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Register Link
        btnRegisterLink = new JButton("Belum punya akun? Daftar di sini") {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        btnRegisterLink.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnRegisterLink.setForeground(ACCENT_CYAN);
        btnRegisterLink.setContentAreaFilled(false);
        btnRegisterLink.setFocusPainted(false);
        btnRegisterLink.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        btnRegisterLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegisterLink.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Assembly
        // cardPanel.add(lblLogo);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        cardPanel.add(lblTitle);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        cardPanel.add(lblSubtitle);
        cardPanel.add(inputsPanel);
        cardPanel.add(lblError);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        cardPanel.add(btnLogin);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        cardPanel.add(btnRegisterLink);

        mainPanel.add(cardPanel);
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setBackground(INPUT_BG);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_CYAN);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(320, 42));
        field.setMaximumSize(new Dimension(320, 42));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(INPUT_BORDER, 1),
                BorderFactory.createEmptyBorder(4, 12, 8, 12)
        ));
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setBackground(INPUT_BG);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_CYAN);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(320, 42));
        field.setMaximumSize(new Dimension(320, 42));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(INPUT_BORDER, 1),
                BorderFactory.createEmptyBorder(4, 12, 8, 12)
        ));
        return field;
    }

    // Getters and helper methods for controllers
    public String getUsername() {
        return txtUsername.getText().trim();
    }

    public String getPassword() {
        return new String(txtPassword.getPassword());
    }

    public void showError(String message) {
        if (message == null || message.trim().isEmpty()) {
            lblError.setText(" ");
        } else {
            lblError.setText(message);
        }
    }

    public void clearFields() {
        txtUsername.setText("");
        txtPassword.setText("");
        lblError.setText(" ");
    }

    public void addLoginListener(ActionListener listener) {
        btnLogin.addActionListener(listener);
        // Also fire login action on pressing Enter in either input fields
        txtUsername.addActionListener(listener);
        txtPassword.addActionListener(listener);
    }

    public void addRegisterLinkListener(ActionListener listener) {
        if (listener != null) {
            btnRegisterLink.addActionListener(listener);
        }
    }

    public void close() {
        dispose();
    }

    // ==========================================
    // Custom Painting Classes for Premium UI
    // ==========================================

    /**
     * Deep gradient background rendering.
     */
    private static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, BG_GRADIENT_START, w, h, BG_GRADIENT_END);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, w, h);
            g2d.dispose();
        }
    }

    /**
     * Glassmorphic design card panel.
     */
    private static class GlassCard extends JPanel {
        public GlassCard() {
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
            g2d.fill(new RoundRectangle2D.Double(0, 0, w, h, 24, 24));

            // Subtle glowing border
            g2d.setStroke(new BasicStroke(1.5f));
            GradientPaint borderPaint = new GradientPaint(
                    0, 0, new Color(255, 255, 255, 60),
                    0, h, new Color(255, 255, 255, 10)
            );
            g2d.setPaint(borderPaint);
            g2d.draw(new RoundRectangle2D.Double(0.75, 0.75, w - 1.5, h - 1.5, 24, 24));

            g2d.dispose();
        }
    }

    /**
     * Modern animated gradient button with interactive hover states.
     */
    private static class GradientButton extends JButton {
        private boolean hovered = false;
        private boolean pressed = false;

        public GradientButton(String text) {
            super(text);
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

            // Determine active colors based on state
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
            g2d.fill(new RoundRectangle2D.Double(0, 0, w, h, 12, 12));

            // Optional hover border glow
            if (hovered) {
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.draw(new RoundRectangle2D.Double(0.75, 0.75, w - 1.5, h - 1.5, 12, 12));
            }

            super.paintComponent(g);
            g2d.dispose();
        }
    }
}
