package pengelolaanproject.view;

import pengelolaanproject.model.ProjectModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * High-fidelity, premium glassmorphic dashboard for project managers.
 * Allows viewing all projects and provides quick actions for project creation,
 * task assignment, and task approval.
 */
public class ManagerDashboardView extends DashboardView {

    private JTable tblProjects;
    private DefaultTableModel tableModel;
    private GradientButton btnCreateProject;
    private GradientButton btnEditProject;
    private GradientButton btnLogout;
    private List<ProjectModel> currentProjects;

    public ManagerDashboardView() {
        super();
        this.currentProjects = new ArrayList<>();
        render();
    }

    @Override
    public void render() {
        // Use BorderLayout for main structure
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(30, 40, 30, 40));

        // 1. Header Area
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Ruang Kerja Manajer");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSubtitle = new JLabel("Awasi proyek, kelola siklus hidup, alokasikan sumber daya, dan setujui penyelesaian.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(TEXT_SECONDARY);

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 6)));
        titlePanel.add(lblSubtitle);

        headerPanel.add(titlePanel, BorderLayout.WEST);

        // Logout Button
        btnLogout = new GradientButton("Logout", 8);
        btnLogout.setPreferredSize(new Dimension(100, 36));
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        logoutPanel.setOpaque(false);
        logoutPanel.add(btnLogout);

        headerPanel.add(logoutPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Table / Center Area (Wrapped in a GlassCard)
        GlassCard cardPanel = new GlassCard(16);
        cardPanel.setLayout(new BorderLayout(15, 15));
        cardPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTableTitle = new JLabel("PROYEK AKTIF");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTableTitle.setForeground(ACCENT_CYAN);
        cardPanel.add(lblTableTitle, BorderLayout.NORTH);

        // Initialize custom premium JTable
        String[] columns = {"ID", "Nama Project", "Tanggal Mulai", "Tenggat Waktu", "Total Task"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only
            }
        };

        tblProjects = new JTable(tableModel);
        tblProjects.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblProjects.setRowHeight(36);
        tblProjects.setGridColor(new Color(255, 255, 255, 20));
        tblProjects.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblProjects.setOpaque(false);
        tblProjects.setBackground(new Color(0, 0, 0, 0));
        tblProjects.setForeground(TEXT_PRIMARY);
        tblProjects.setSelectionBackground(new Color(ACCENT_PURPLE.getRed(), ACCENT_PURPLE.getGreen(), ACCENT_PURPLE.getBlue(), 120));
        tblProjects.setSelectionForeground(TEXT_PRIMARY);
        tblProjects.setShowVerticalLines(false);

        // Styling the table header
        JTableHeader header = tblProjects.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setOpaque(false);
        header.setBackground(INPUT_BG);
        header.setForeground(ACCENT_CYAN);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, INPUT_BORDER));

        // Custom Cell Renderer for padded/centered text and semi-transparent look
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                if (isSelected) {
                    setOpaque(true);
                    setBackground(table.getSelectionBackground());
                    setForeground(table.getSelectionForeground());
                } else {
                    setOpaque(false);
                    setForeground(TEXT_PRIMARY);
                }
                if (column == 0) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                return c;
            }
        };

        for (int i = 0; i < tblProjects.getColumnCount(); i++) {
            tblProjects.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        // Wrap JTable inside custom styled Scroll Pane
        JScrollPane scrollPane = new JScrollPane(tblProjects);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(0, 0, 0, 0));

        cardPanel.add(scrollPane, BorderLayout.CENTER);
        add(cardPanel, BorderLayout.CENTER);

        // 3. Actions / Footer Area
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        btnCreateProject = new GradientButton("Buat Project");
        btnCreateProject.setPreferredSize(new Dimension(150, 42));

        btnEditProject = new GradientButton("Edit Project");
        btnEditProject.setPreferredSize(new Dimension(130, 42));

        footerPanel.add(btnCreateProject);
        footerPanel.add(btnEditProject);
        add(footerPanel, BorderLayout.SOUTH);
    }

    // =========================================================================
    // Public API Contracts
    // =========================================================================

    /**
     * Binds action listener for the Create Project action.
     */
    public void addCreateProjectListener(ActionListener listener) {
        if (listener != null) {
            btnCreateProject.addActionListener(listener);
        }
    }

    public void addEditProjectListener(ActionListener listener) {
        if (listener != null) btnEditProject.addActionListener(listener);
    }

    /**
     * Binds action listener for the Logout action.
     */
    public void addLogoutListener(ActionListener listener) {
        if (listener != null) {
            btnLogout.addActionListener(listener);
        }
    }

    /**
     * Renders/displays the provided list of projects into the active dashboard table.
     */
    public void displayProjects(List<ProjectModel> projects) {
        this.currentProjects = projects != null ? projects : new ArrayList<>();
        tableModel.setRowCount(0);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        for (ProjectModel project : this.currentProjects) {
            String startDateStr = project.getStartDate() != null ? df.format(project.getStartDate()) : "-";
            String deadlineStr = project.getDeadline() != null ? df.format(project.getDeadline()) : "-";
            int tasksCount = project.getTasks() != null ? project.getTasks().size() : 0;

            tableModel.addRow(new Object[]{
                    project.getId(),
                    project.getName(),
                    startDateStr,
                    deadlineStr,
                    tasksCount
            });
        }
    }

    /**
     * Helper method to return the currently selected project in the table.
     * Helpful for controllers mapping operations to specific projects.
     * Returns null if no row is selected.
     */
    public ProjectModel getSelectedProject() {
        int selectedRow = tblProjects.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentProjects.size()) {
            return currentProjects.get(selectedRow);
        }
        return null;
    }

    /**
     * Exposes registering a mouse listener on the projects table.
     * Useful for double-click bindings to view detail boards.
     */
    public void addProjectTableMouseListener(java.awt.event.MouseListener listener) {
        if (listener != null) {
            tblProjects.addMouseListener(listener);
        }
    }
}
