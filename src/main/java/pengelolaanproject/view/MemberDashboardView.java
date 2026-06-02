package pengelolaanproject.view;

import pengelolaanproject.model.TaskModel;

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
 * High-fidelity, premium glassmorphic dashboard for team members.
 * Displays list of tasks assigned to the current user and allows updating task statuses.
 */
public class MemberDashboardView extends DashboardView {

    private JTable tblTasks;
    private DefaultTableModel tableModel;
    private GradientButton btnUpdateStatus;
    private List<TaskModel> currentTasks;

    public MemberDashboardView() {
        super();
        this.currentTasks = new ArrayList<>();
        render();
    }

    @Override
    public void render() {
        // Use BorderLayout for main structure
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(30, 40, 30, 40));

        // 1. Header Area
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Member Workspace");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSubtitle = new JLabel("Track your personal progress, manage status updates, and submit links.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(TEXT_SECONDARY);

        headerPanel.add(lblTitle);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        headerPanel.add(lblSubtitle);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Table / Center Area (Wrapped in a GlassCard)
        GlassCard cardPanel = new GlassCard(16);
        cardPanel.setLayout(new BorderLayout(15, 15));
        cardPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTableTitle = new JLabel("MY ASSIGNED TASKS");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTableTitle.setForeground(ACCENT_CYAN);
        cardPanel.add(lblTableTitle, BorderLayout.NORTH);

        // Initialize custom premium JTable for tasks
        String[] columns = {"ID", "Task Title", "Status", "Due Date", "Submission Link"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only
            }
        };

        tblTasks = new JTable(tableModel);
        tblTasks.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblTasks.setRowHeight(36);
        tblTasks.setGridColor(new Color(255, 255, 255, 20));
        tblTasks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTasks.setOpaque(false);
        tblTasks.setBackground(new Color(0, 0, 0, 0));
        tblTasks.setForeground(TEXT_PRIMARY);
        tblTasks.setSelectionBackground(new Color(ACCENT_PURPLE.getRed(), ACCENT_PURPLE.getGreen(), ACCENT_PURPLE.getBlue(), 120));
        tblTasks.setSelectionForeground(TEXT_PRIMARY);
        tblTasks.setShowVerticalLines(false);

        // Styling the table header
        JTableHeader header = tblTasks.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setOpaque(false);
        header.setBackground(INPUT_BG);
        header.setForeground(ACCENT_CYAN);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, INPUT_BORDER));

        // Custom Cell Renderer for status badges and formatted columns
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                setOpaque(false);
                setForeground(TEXT_PRIMARY);

                if (column == 0) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else if (column == 2) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                    // Color code status values
                    String val = String.valueOf(value);
                    if ("DONE".equals(val)) {
                        setForeground(new Color(40, 199, 111)); // Elegant Green
                    } else if ("REVIEW".equals(val)) {
                        setForeground(ACCENT_CYAN);
                    } else if ("IN_PROGRESS".equals(val)) {
                        setForeground(new Color(255, 159, 67)); // Gold Orange
                    } else {
                        setForeground(TEXT_SECONDARY);
                    }
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                return c;
            }
        };

        for (int i = 0; i < tblTasks.getColumnCount(); i++) {
            tblTasks.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        // Wrap JTable inside custom styled Scroll Pane
        JScrollPane scrollPane = new JScrollPane(tblTasks);
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

        btnUpdateStatus = new GradientButton("Update Status");
        btnUpdateStatus.setPreferredSize(new Dimension(160, 42));

        footerPanel.add(btnUpdateStatus);
        add(footerPanel, BorderLayout.SOUTH);
    }

    // =========================================================================
    // Public API Contracts
    // =========================================================================

    /**
     * Binds action listener for the Update Status action.
     */
    public void addUpdateStatusListener(ActionListener listener) {
        if (listener != null) {
            btnUpdateStatus.addActionListener(listener);
        }
    }

    /**
     * Renders/displays the provided list of tasks into the active dashboard table.
     */
    public void displayTasks(List<TaskModel> tasks) {
        this.currentTasks = tasks != null ? tasks : new ArrayList<>();
        tableModel.setRowCount(0);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        for (TaskModel task : this.currentTasks) {
            String dueDateStr = task.getDueDate() != null ? df.format(task.getDueDate()) : "-";
            String linkStr = (task.getSubmissionLink() != null && !task.getSubmissionLink().isEmpty())
                    ? task.getSubmissionLink() : "-";

            tableModel.addRow(new Object[]{
                    task.getId(),
                    task.getTitle(),
                    task.getStatus() != null ? task.getStatus().name() : "TODO",
                    dueDateStr,
                    linkStr
            });
        }
    }

    /**
     * Helper method to return the currently selected task in the table.
     * Helpful for controllers mapping operations to specific tasks.
     * Returns null if no row is selected.
     */
    public TaskModel getSelectedTask() {
        int selectedRow = tblTasks.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentTasks.size()) {
            return currentTasks.get(selectedRow);
        }
        return null;
    }

    /**
     * Exposes registering a mouse listener on the tasks table.
     * Useful for double-click bindings.
     */
    public void addTaskTableMouseListener(java.awt.event.MouseListener listener) {
        if (listener != null) {
            tblTasks.addMouseListener(listener);
        }
    }
}
