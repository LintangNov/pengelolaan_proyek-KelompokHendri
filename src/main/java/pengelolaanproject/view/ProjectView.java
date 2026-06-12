package pengelolaanproject.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * High-fidelity, premium glassmorphic form panel for creating projects.
 * Features styled fields for Project Name, Start Date, and Deadline with real-time format validation.
 */
public class ProjectView extends JPanel {

    private JTextField txtName;
    private JTextField txtStartDate;
    private JTextField txtDeadline;
    private DashboardView.GradientButton btnCreate;
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

    public ProjectView() {
        setOpaque(false);
        initComponents();
    }

    private void initComponents() {
        // Layout and Margin
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridx = 0;

        // 1. Title/Header inside glassmorphic card container
        DashboardView.GlassCard card = new DashboardView.GlassCard(20);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(30, 30, 30, 30));
        card.setPreferredSize(new Dimension(420, 460));

        JLabel lblTitle = new JLabel("Buat Project Baru");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Inisialisasi alur kerja baru dan tetapkan batas waktu.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(TEXT_SECONDARY);
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lblTitle);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(lblSubtitle);
        card.add(Box.createRigidArea(new Dimension(0, 24)));

        // 2. Project Name Input
        JLabel lblName = new JLabel("NAMA PROJECT");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblName.setForeground(ACCENT_CYAN);
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtName = createStyledTextField("mis. Phoenix Overhaul");
        txtName.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lblName);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(txtName);
        card.add(Box.createRigidArea(new Dimension(0, 16)));

        // 3. Start Date Input
        JLabel lblStartDate = new JLabel("START DATE (YYYY-MM-DD)");
        lblStartDate.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblStartDate.setForeground(ACCENT_CYAN);
        lblStartDate.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        txtStartDate = createStyledTextField(sdf.format(new Date()));
        txtStartDate.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lblStartDate);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(txtStartDate);
        card.add(Box.createRigidArea(new Dimension(0, 16)));

        // 4. Deadline Input
        JLabel lblDeadline = new JLabel("DEADLINE (YYYY-MM-DD)");
        lblDeadline.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblDeadline.setForeground(ACCENT_CYAN);
        lblDeadline.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtDeadline = createStyledTextField("mis. 2026-12-31");
        txtDeadline.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lblDeadline);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(txtDeadline);
        card.add(Box.createRigidArea(new Dimension(0, 14)));

        // 5. Error display message
        lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblError.setForeground(ERROR_COLOR);
        lblError.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblError.setPreferredSize(new Dimension(360, 20));
        lblError.setMinimumSize(new Dimension(360, 20));
        lblError.setMaximumSize(new Dimension(360, 20));

        card.add(lblError);
        card.add(Box.createRigidArea(new Dimension(0, 8)));

        // 6. Create Button
        btnCreate = new DashboardView.GradientButton("BUAT PROJECT");
        btnCreate.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCreate.setPreferredSize(new Dimension(360, 44));
        btnCreate.setMaximumSize(new Dimension(360, 44));
        btnCreate.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(btnCreate);

        // Add the glass card panel to center
        add(card);
    }

    private JTextField createStyledTextField(String textVal) {
        JTextField field = new JTextField(textVal);
        field.setBackground(INPUT_BG);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_CYAN);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(360, 42));
        field.setPreferredSize(new Dimension(360, 42));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(INPUT_BORDER, 1),
                BorderFactory.createEmptyBorder(4, 12, 8, 12)
        ));

        // Subtle glow focus effect
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_CYAN, 1),
                        BorderFactory.createEmptyBorder(4, 12, 8, 12)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(INPUT_BORDER, 1),
                        BorderFactory.createEmptyBorder(4, 12, 8, 12)
                ));
            }
        });
        return field;
    }

    // =========================================================================
    // Public API Contracts
    // =========================================================================

    /**
     * Binds listener for the Project Create action submission.
     */
    public void addCreateListener(ActionListener listener) {
        if (listener != null) {
            btnCreate.addActionListener(listener);
        }
    }

    /**
     * Gets the current project name entered in the input field.
     */
    public String getProjectName() {
        return txtName.getText().trim();
    }

    /**
     * Parses and retrieves the entered deadline Date object.
     * Performs strict parsing and updates error messages in real-time.
     * Returns null if parsing fails or input is blank.
     */
    public Date getDeadline() {
        String deadlineStr = txtDeadline.getText().trim();
        if (deadlineStr.isEmpty()) {
            lblError.setText("Deadline is required.");
            return null;
        }

        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            df.setLenient(false);
            Date date = df.parse(deadlineStr);
            lblError.setText(" "); // Reset error
            return date;
        } catch (Exception e) {
            lblError.setText("Invalid deadline format. Use YYYY-MM-DD.");
            return null;
        }
    }

    /**
     * Parses and retrieves the entered start Date object.
     * Returns null if parsing fails.
     */
    public Date getStartDate() {
        String startStr = txtStartDate.getText().trim();
        if (startStr.isEmpty()) {
            return new Date();
        }

        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            df.setLenient(false);
            return df.parse(startStr);
        } catch (Exception e) {
            return new Date(); // fallback to current
        }
    }

    /**
     * Displays a custom error message on the form.
     */
    public void showError(String error) {
        if (error == null || error.trim().isEmpty()) {
            lblError.setText(" ");
        } else {
            lblError.setText(error);
        }
    }

    /**
     * Clears/resets all form input fields.
     */
    public void clearForm() {
        txtName.setText("");
        txtDeadline.setText("");
        lblError.setText(" ");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        txtStartDate.setText(sdf.format(new Date()));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Matching deep dark gradient background
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
