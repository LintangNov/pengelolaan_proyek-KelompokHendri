package pengelolaanproject.view;

import pengelolaanproject.core.UserRole;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * High-fidelity, premium glassmorphic registration form panel.
 * Allows users to register new accounts in the system with role selection.
 */
public class RegisterView extends JPanel {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JComboBox<UserRole> cmbRole;
    private DashboardView.GradientButton btnRegister;
    private JLabel lblError;

    // Direct access to gorgeous styling variables
    private static final Color BG_GRADIENT_START = DashboardView.BG_GRADIENT_START;
    private static final Color BG_GRADIENT_END = DashboardView.BG_GRADIENT_END;
    private static final Color TEXT_PRIMARY = DashboardView.TEXT_PRIMARY;
    private static final Color TEXT_SECONDARY = DashboardView.TEXT_SECONDARY;
    private static final Color ACCENT_CYAN = DashboardView.ACCENT_CYAN;
    private static final Color INPUT_BG = DashboardView.INPUT_BG;
    private static final Color INPUT_BORDER = DashboardView.INPUT_BORDER;
    private static final Color ERROR_COLOR = DashboardView.ERROR_COLOR;

    public RegisterView() {
        setOpaque(false);
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // Glassmorphic Card Container
        DashboardView.GlassCard card = new DashboardView.GlassCard(20);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(25, 25, 25, 25));
        card.setPreferredSize(new Dimension(380, 420));

        // Header Title & Subtitle
        JLabel lblTitle = new JLabel("Daftar Akun");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Buat identitas baru untuk mengakses ruang kerja.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(TEXT_SECONDARY);
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lblTitle);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(lblSubtitle);
        card.add(Box.createRigidArea(new Dimension(0, 20)));

        // Username Input
        JLabel lblUser = new JLabel("USERNAME");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblUser.setForeground(ACCENT_CYAN);
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtUsername = createStyledTextField();
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lblUser);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(txtUsername);
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        // Password Input
        JLabel lblPass = new JLabel("PASSWORD");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblPass.setForeground(ACCENT_CYAN);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtPassword = createStyledPasswordField();
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lblPass);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(txtPassword);
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        // Role Dropdown ComboBox
        JLabel lblRole = new JLabel("PERAN PENGGUNA");
        lblRole.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblRole.setForeground(ACCENT_CYAN);
        lblRole.setAlignmentX(Component.LEFT_ALIGNMENT);

        cmbRole = new JComboBox<>(UserRole.values()) {
            @Override
            public void updateUI() {
                super.updateUI();
                // Paksa foreground tetap TEXT_PRIMARY setelah L&F update
                setForeground(new Color(238, 238, 238));
            }
        };
        cmbRole.setBackground(INPUT_BG);
        cmbRole.setForeground(new Color(238, 238, 238));
        cmbRole.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbRole.setMaximumSize(new Dimension(320, 36));
        cmbRole.setPreferredSize(new Dimension(320, 36));
        cmbRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbRole.setBorder(BorderFactory.createLineBorder(INPUT_BORDER, 1));

        // Renderer untuk dropdown list DAN selected item
        cmbRole.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? new Color(0, 110, 120) : new Color(34, 40, 49));
                setForeground(new Color(238, 238, 238));
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                return this;
            }
        });

        // Paksa warna editor (bagian yang tampil saat item dipilih)
        Component editorComp = cmbRole.getEditor().getEditorComponent();
        if (editorComp instanceof JTextField) {
            ((JTextField) editorComp).setBackground(new Color(34, 40, 49));
            ((JTextField) editorComp).setForeground(new Color(238, 238, 238));
        }

        card.add(lblRole);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(cmbRole);
        card.add(Box.createRigidArea(new Dimension(0, 14)));

        // Error message label
        lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblError.setForeground(ERROR_COLOR);
        lblError.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblError.setPreferredSize(new Dimension(320, 20));
        lblError.setMinimumSize(new Dimension(320, 20));
        lblError.setMaximumSize(new Dimension(320, 20));

        card.add(lblError);
        card.add(Box.createRigidArea(new Dimension(0, 8)));

        // Register Button
        btnRegister = new DashboardView.GradientButton("DAFTAR");
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRegister.setPreferredSize(new Dimension(320, 42));
        btnRegister.setMaximumSize(new Dimension(320, 42));
        btnRegister.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(btnRegister);

        add(card);
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setBackground(INPUT_BG);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_CYAN);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(320, 42));
        field.setPreferredSize(new Dimension(320, 42));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(INPUT_BORDER, 1),
                BorderFactory.createEmptyBorder(4, 10, 8, 10)));

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_CYAN, 1),
                        BorderFactory.createEmptyBorder(4, 10, 8, 10)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(INPUT_BORDER, 1),
                        BorderFactory.createEmptyBorder(4, 10, 8, 10)));
            }
        });
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setBackground(INPUT_BG);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_CYAN);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(320, 42));
        field.setPreferredSize(new Dimension(320, 42));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(INPUT_BORDER, 1),
                BorderFactory.createEmptyBorder(4, 10, 8, 10)));

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_CYAN, 1),
                        BorderFactory.createEmptyBorder(4, 10, 8, 10)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(INPUT_BORDER, 1),
                        BorderFactory.createEmptyBorder(4, 10, 8, 10)));
            }
        });
        return field;
    }

    public void addRegisterListener(ActionListener listener) {
        if (listener != null) {
            btnRegister.addActionListener(listener);
        }
    }

    public String getUsername() {
        return txtUsername.getText().trim();
    }

    public String getPassword() {
        return new String(txtPassword.getPassword());
    }

    public UserRole getSelectedRole() {
        return (UserRole) cmbRole.getSelectedItem();
    }

    public void showError(String error) {
        if (error == null || error.trim().isEmpty()) {
            lblError.setText(" ");
        } else {
            lblError.setText(error);
        }
    }

    public void clearForm() {
        txtUsername.setText("");
        txtPassword.setText("");
        cmbRole.setSelectedIndex(0);
        lblError.setText(" ");
    }

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
