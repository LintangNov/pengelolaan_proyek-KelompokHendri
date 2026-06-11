package pengelolaanproject.view;

import pengelolaanproject.model.TaskModel;
import pengelolaanproject.model.TaskStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * High-fidelity, premium glassmorphic Kanban board representing the active tasks.
 * Arranged into 4 scrollable status columns: TODO, IN_PROGRESS, REVIEW, and DONE.
 * Supports task cards with contextual "Move" popup actions and submission prompts.
 */
public class TaskBoardView extends JPanel {

    private final JPanel colTodoPanel = new JPanel();
    private final JPanel colInProgressPanel = new JPanel();
    private final JPanel colReviewPanel = new JPanel();
    private final JPanel colDonePanel = new JPanel();

    private final List<ActionListener> statusChangeListeners = new ArrayList<>();
    
    // UI Event Context state variables for the Controller to query upon action trigger
    private TaskModel activeTaskForMove;
    private TaskStatus activeTargetStatus;
    private String activeSubmissionLink = "";

    // Direct access to gorgeous styling variables from DashboardView
    private static final Color BG_GRADIENT_START = DashboardView.BG_GRADIENT_START;
    private static final Color BG_GRADIENT_END = DashboardView.BG_GRADIENT_END;
    private static final Color TEXT_PRIMARY = DashboardView.TEXT_PRIMARY;
    private static final Color TEXT_SECONDARY = DashboardView.TEXT_SECONDARY;
    private static final Color ACCENT_CYAN = DashboardView.ACCENT_CYAN;
    private static final Color ACCENT_PURPLE = DashboardView.ACCENT_PURPLE;
    private static final Color INPUT_BG = DashboardView.INPUT_BG;
    private static final Color INPUT_BORDER = DashboardView.INPUT_BORDER;

    public TaskBoardView() {
        setOpaque(false);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(30, 35, 30, 35));

        // 1. Title Header
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Task Kanban Board");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSubtitle = new JLabel("Visualize workflows, track progress stages, and update task statuses collaboratively.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(TEXT_SECONDARY);

        headerPanel.add(lblTitle);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        headerPanel.add(lblSubtitle);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Kanban Board Grid Panel (4 Columns)
        JPanel boardGrid = new JPanel(new GridLayout(1, 4, 15, 0));
        boardGrid.setOpaque(false);

        // Build status columns
        boardGrid.add(createKanbanColumn("TO DO", TEXT_SECONDARY, colTodoPanel));
        boardGrid.add(createKanbanColumn("IN PROGRESS", new Color(255, 159, 67), colInProgressPanel));
        boardGrid.add(createKanbanColumn("IN REVIEW", ACCENT_CYAN, colReviewPanel));
        boardGrid.add(createKanbanColumn("DONE", new Color(40, 199, 111), colDonePanel));

        add(boardGrid, BorderLayout.CENTER);
    }

    /**
     * Builds a single Kanban column wrapped inside a GlassCard with a header and a scrollable card container.
     */
    private JPanel createKanbanColumn(String title, Color headerColor, JPanel cardsContainer) {
        JPanel columnWrapper = new JPanel(new BorderLayout(10, 10));
        columnWrapper.setOpaque(false);

        // Top Column Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(5, 5, 8, 5));

        JLabel lblColTitle = new JLabel(title);
        lblColTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblColTitle.setForeground(headerColor);
        headerPanel.add(lblColTitle, BorderLayout.WEST);
        columnWrapper.add(headerPanel, BorderLayout.NORTH);

        // Inner Card Container
        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));
        cardsContainer.setOpaque(false);
        cardsContainer.setBackground(new Color(0, 0, 0, 0));

        // Scroll pane wrapper for cards
        JScrollPane scrollPane = new JScrollPane(cardsContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(new Color(0, 0, 0, 0));

        // Wrap inside Glass Card
        DashboardView.GlassCard glassCard = new DashboardView.GlassCard(16);
        glassCard.setLayout(new BorderLayout());
        glassCard.setBorder(new EmptyBorder(12, 10, 12, 10));
        glassCard.add(scrollPane, BorderLayout.CENTER);

        columnWrapper.add(glassCard, BorderLayout.CENTER);
        return columnWrapper;
    }

    // =========================================================================
    // Public API Contracts
    // =========================================================================

    /**
     * Binds a listener triggered whenever a task status change is selected.
     */
    public void addStatusChangeListener(ActionListener listener) {
        if (listener != null) {
            statusChangeListeners.add(listener);
        }
    }

    /**
     * Re-renders the Kanban board columns with cards corresponding to the provided tasks.
     */
    public void displayBoard(List<TaskModel> tasks) {
        // Clear all previous items
        colTodoPanel.removeAll();
        colInProgressPanel.removeAll();
        colReviewPanel.removeAll();
        colDonePanel.removeAll();

        if (tasks != null) {
            for (TaskModel task : tasks) {
                JPanel card = createTaskCard(task);
                if (task.getStatus() == TaskStatus.TODO) {
                    colTodoPanel.add(card);
                    colTodoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                } else if (task.getStatus() == TaskStatus.IN_PROGRESS) {
                    colInProgressPanel.add(card);
                    colInProgressPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                } else if (task.getStatus() == TaskStatus.REVIEW) {
                    colReviewPanel.add(card);
                    colReviewPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                } else if (task.getStatus() == TaskStatus.DONE) {
                    colDonePanel.add(card);
                    colDonePanel.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }
        }

        // Revalidate & repaint all columns to refresh the UI
        colTodoPanel.revalidate();
        colTodoPanel.repaint();
        colInProgressPanel.revalidate();
        colInProgressPanel.repaint();
        colReviewPanel.revalidate();
        colReviewPanel.repaint();
        colDonePanel.revalidate();
        colDonePanel.repaint();
    }

    /**
     * Prompts the user for a submission link when a task moves to the REVIEW state.
     * Shows a JOptionPane.showInputDialog and returns the entered String.
     */
    public String promptSubmissionLink() {
        String input = JOptionPane.showInputDialog(
                this,
                "Please enter the GitHub Repository or Figma design URL to proceed to REVIEW:",
                "Task Deliverable Submission",
                JOptionPane.QUESTION_MESSAGE
        );
        return input;
    }

    /**
     * Returns the active task affected by the current state change.
     */
    public TaskModel getActiveTaskForMove() {
        return activeTaskForMove;
    }

    /**
     * Returns the target status selected for the active task.
     */
    public TaskStatus getActiveTargetStatus() {
        return activeTargetStatus;
    }

    /**
     * Returns the submission link submitted during the transition to REVIEW (if any).
     */
    public String getActiveSubmissionLink() {
        return activeSubmissionLink;
    }

    // =========================================================================
    // Card Rendering and Event Triggering
    // =========================================================================

    /**
     * Creates a glowing glassmorphic card representing a Task.
     */
    private JPanel createTaskCard(TaskModel task) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 12)); // Semi-transparent card
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2d.setColor(new Color(255, 255, 255, 25)); // Slight outline
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2d.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(8, 8));
        card.setBorder(new EmptyBorder(12, 12, 12, 12));
        card.setMaximumSize(new Dimension(320, 160));
        card.setPreferredSize(new Dimension(180, 160));

        // Title Label
        JLabel lblTitle = new JLabel(task.getTitle());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(TEXT_PRIMARY);
        card.add(lblTitle, BorderLayout.NORTH);

        // Details Panel
        JPanel detailsPanel = new JPanel();
        detailsPanel.setOpaque(false);
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));

        JLabel lblAssignee = new JLabel("Assignee ID: " + task.getAssigneeId());
        lblAssignee.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblAssignee.setForeground(TEXT_SECONDARY);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String dueStr = task.getDueDate() != null ? df.format(task.getDueDate()) : "No Due Date";
        JLabel lblDueDate = new JLabel("Due: " + dueStr);
        lblDueDate.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblDueDate.setForeground(TEXT_SECONDARY);

        detailsPanel.add(lblAssignee);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        detailsPanel.add(lblDueDate);

        // Description
        if (task.getDescription() != null && !task.getDescription().trim().isEmpty()) {
            JLabel lblDesc = new JLabel("<html><i>" + task.getDescription() + "</i></html>");
            lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            lblDesc.setForeground(TEXT_SECONDARY);
            detailsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
            detailsPanel.add(lblDesc);
        }

        // Notes
        if (task.getNotes() != null && !task.getNotes().trim().isEmpty()) {
            JLabel lblNotes = new JLabel("<html><b>Notes:</b> " + task.getNotes() + "</html>");
            lblNotes.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            lblNotes.setForeground(TEXT_SECONDARY);
            detailsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
            detailsPanel.add(lblNotes);
        }

        // Render URL badge if available
        if (task.getSubmissionLink() != null && !task.getSubmissionLink().trim().isEmpty()) {
            JButton btnOpenLink = new JButton("<html><u>✦ Open Submission</u></html>") {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                }
            };
            btnOpenLink.setFont(new Font("Segoe UI", Font.BOLD, 9));
            btnOpenLink.setForeground(ACCENT_CYAN);
            btnOpenLink.setContentAreaFilled(false);
            btnOpenLink.setFocusPainted(false);
            btnOpenLink.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            btnOpenLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnOpenLink.setAlignmentX(Component.LEFT_ALIGNMENT);

            btnOpenLink.addActionListener(evt -> {
                try {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(task.getSubmissionLink()));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(TaskBoardView.this, "Cannot open link: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            detailsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
            detailsPanel.add(btnOpenLink);
        }

        card.add(detailsPanel, BorderLayout.CENTER);

        // Move Action Button
        JButton btnMove = new JButton("Move ➜") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 20));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        btnMove.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnMove.setForeground(ACCENT_CYAN);
        btnMove.setContentAreaFilled(false);
        btnMove.setFocusPainted(false);
        btnMove.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        btnMove.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Wire JPopupMenu to select movement destination
        btnMove.addActionListener(e -> {
            JPopupMenu popup = new JPopupMenu();

            for (TaskStatus status : TaskStatus.values()) {
                if (status != task.getStatus()) {
                    JMenuItem item = new JMenuItem("Move to " + status.name().replace("_", " "));
                    item.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    
                    item.addActionListener(evt -> {
                        // Capture UI context
                        this.activeTaskForMove = task;
                        this.activeTargetStatus = status;

                        // Notify registered observers/controllers
                        java.awt.event.ActionEvent actionEvent = new java.awt.event.ActionEvent(
                                this,
                                java.awt.event.ActionEvent.ACTION_PERFORMED,
                                "statusChanged"
                        );
                        for (ActionListener l : statusChangeListeners) {
                            l.actionPerformed(actionEvent);
                        }
                    });
                    popup.add(item);
                }
            }
            popup.show(btnMove, 0, btnMove.getHeight());
        });

        JPanel btnPanel = new JPanel(new BorderLayout());
        btnPanel.setOpaque(false);
        btnPanel.add(btnMove, BorderLayout.EAST);
        card.add(btnPanel, BorderLayout.SOUTH);

        return card;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Paint deep dark background gradient
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
