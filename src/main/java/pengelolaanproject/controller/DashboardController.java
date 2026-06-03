package pengelolaanproject.controller;

import pengelolaanproject.core.BaseController;
import pengelolaanproject.core.DatabaseConnection;
import pengelolaanproject.core.SessionManager;
import pengelolaanproject.core.UserRole;
import pengelolaanproject.model.ProjectModel;
import pengelolaanproject.model.TaskModel;
import pengelolaanproject.model.TaskStatus;
import pengelolaanproject.model.User;
import pengelolaanproject.repository.IProjectRepository;
import pengelolaanproject.view.ManagerDashboardView;
import pengelolaanproject.view.MemberDashboardView;
import pengelolaanproject.view.TaskBoardView;
import pengelolaanproject.view.DashboardView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Main workspace controller managing the dashboards for managers (PM) and members (DEV/UIUX).
 * Enforces business rules, role checks, and wires Kanban board transitions.
 */
public class DashboardController extends BaseController {
    private final IProjectRepository repository;
    private final JFrame mainFrame;
    private final User currentUser;

    // View Components
    private final DashboardView dashboardView;
    private ManagerDashboardView managerView;
    private MemberDashboardView memberView;

    // Injected Controller
    private final ProjectController projectController;

    // Maps taskId to projectId for member views where project scope is not directly available
    private final Map<Integer, Integer> taskProjectMap = new HashMap<>();

    /**
     * Constructs the controller, initializes the main app frame, and setups the workspace based on user role.
     *
     * @param repository        project repository data contract
     * @param dashboardView     the view injected based on role
     * @param projectController the project controller injected for project creation
     */
    public DashboardController(IProjectRepository repository, DashboardView dashboardView, ProjectController projectController) {
        this.repository = repository;
        this.dashboardView = dashboardView;
        this.projectController = projectController;
        this.currentUser = SessionManager.getInstance().getCurrentUser();

        // Initialize application main window
        this.mainFrame = new JFrame("Project Management System - Workspace");
        this.mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.mainFrame.setSize(1024, 768);
        this.mainFrame.setLocationRelativeTo(null);

        if (currentUser == null) {
            JOptionPane.showMessageDialog(null, "No active user session found. Exiting.", "Session Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        initializeWorkspace();
    }

    /**
     * Instantiates correct dashboard view based on user role, registers listeners, and displays frame.
     */
    private void initializeWorkspace() {
        if (dashboardView instanceof ManagerDashboardView) {
            this.managerView = (ManagerDashboardView) dashboardView;

            // Wire manager quick actions
            this.managerView.addCreateProjectListener(new CreateProjectButtonListener());
            this.managerView.addAssignTaskListener(new AssignTaskButtonListener());
            this.managerView.addApproveTaskListener(new ApproveTaskButtonListener());

            // Double-click table row to view Kanban Board
            this.managerView.addProjectTableMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        ProjectModel selected = managerView.getSelectedProject();
                        if (selected != null) {
                            openTaskBoard(selected);
                        }
                    }
                }
            });

            this.mainFrame.setContentPane(managerView);
        } else if (dashboardView instanceof MemberDashboardView) {
            this.memberView = (MemberDashboardView) dashboardView;

            // Wire member actions
            this.memberView.addUpdateStatusListener(new UpdateStatusButtonListener());

            // Double-click table row to view Kanban Board
            this.memberView.addTaskTableMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        TaskModel selectedTask = memberView.getSelectedTask();
                        if (selectedTask != null) {
                            int projectId = taskProjectMap.getOrDefault(selectedTask.getId(), 0);
                            if (projectId > 0) {
                                ProjectModel project = repository.findProjectById(projectId);
                                if (project != null) {
                                    openTaskBoard(project);
                                }
                            }
                        }
                    }
                }
            });

            this.mainFrame.setContentPane(memberView);
        }

        // Set window titles dynamically based on user context
        this.mainFrame.setTitle("Project Management System - Logged in as: " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        
        refreshData();
        this.mainFrame.setVisible(true);
    }

    /**
     * Queries database via repository and updates UI tables.
     */
    public void refreshData() {
        if (currentUser.getRole() == UserRole.PM) {
            List<ProjectModel> projects = repository.findAllProjects();
            managerView.displayProjects(projects);
        } else {
            List<ProjectModel> allProjects = repository.findAllProjects();
            List<TaskModel> memberTasks = new ArrayList<>();
            taskProjectMap.clear();

            for (ProjectModel project : allProjects) {
                for (TaskModel task : project.getTasks()) {
                    if (task.getAssigneeId() == currentUser.getId()) {
                        memberTasks.add(task);
                        taskProjectMap.put(task.getId(), project.getId());
                    }
                }
            }
            memberView.displayTasks(memberTasks);
        }
    }

    // =========================================================================
    // PM Quick Actions Handlers
    // =========================================================================

    /**
     * Action handler for Create Project button. Launches ProjectController.
     */
    private class CreateProjectButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            projectController.setOnProjectCreated(() -> refreshData());
            projectController.show();
        }
    }

    /**
     * Action handler for Task Assignment. Prompts for details and saves new task.
     */
    private class AssignTaskButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ProjectModel project = managerView.getSelectedProject();
            if (project == null) {
                JOptionPane.showMessageDialog(mainFrame, "Please select a project from the table first.", "No Project Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Retrieve assignable users (DEV, UIUX) from database to prevent input errors
            List<User> assignableUsers = getAssignableUsers();
            if (assignableUsers.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "No assignable developers or designers found in the system.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Construct form UI
            JTextField txtTitle = new JTextField();
            JComboBox<User> cmbAssignee = new JComboBox<>();
            for (User u : assignableUsers) {
                cmbAssignee.addItem(u);
            }
            // Pretty formatting for assignee selector
            cmbAssignee.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof User) {
                        User u = (User) value;
                        setText(u.getUsername() + " (" + u.getRole() + ")");
                    }
                    return this;
                }
            });

            JTextField txtDueDate = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

            JPanel formPanel = new JPanel(new GridLayout(3, 2, 8, 8));
            formPanel.add(new JLabel("Task Title:"));
            formPanel.add(txtTitle);
            formPanel.add(new JLabel("Assignee:"));
            formPanel.add(cmbAssignee);
            formPanel.add(new JLabel("Due Date (YYYY-MM-DD):"));
            formPanel.add(txtDueDate);

            int result = JOptionPane.showConfirmDialog(
                    mainFrame,
                    formPanel,
                    "Assign New Task to " + project.getName(),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION) {
                String title = txtTitle.getText().trim();
                User assignee = (User) cmbAssignee.getSelectedItem();
                String dueDateStr = txtDueDate.getText().trim();

                // Validation
                if (title.isEmpty()) {
                    JOptionPane.showMessageDialog(mainFrame, "Task title cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (assignee == null) {
                    JOptionPane.showMessageDialog(mainFrame, "Assignee is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Date dueDate;
                try {
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    df.setLenient(false);
                    dueDate = df.parse(dueDateStr);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(mainFrame, "Invalid due date format. Please use YYYY-MM-DD.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    // Create task model and save
                    TaskModel task = new TaskModel(title, assignee.getId());
                    task.setDueDate(dueDate);
                    task.setStatus(TaskStatus.TODO);

                    repository.saveTask(task, project.getId());

                    JOptionPane.showMessageDialog(mainFrame, "Task successfully assigned!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(mainFrame, "Failed to save task: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * Action handler for PM task approvals. Marks a REVIEW task as DONE.
     */
    private class ApproveTaskButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ProjectModel project = managerView.getSelectedProject();
            if (project == null) {
                JOptionPane.showMessageDialog(mainFrame, "Please select a project from the table first.", "No Project Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Reload project tasks from repository to ensure fresh states
            ProjectModel loaded = repository.findProjectById(project.getId());
            if (loaded == null) {
                JOptionPane.showMessageDialog(mainFrame, "Failed to load project details.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Get tasks under REVIEW
            List<TaskModel> reviewTasks = new ArrayList<>();
            for (TaskModel t : loaded.getTasks()) {
                if (t.getStatus() == TaskStatus.REVIEW) {
                    reviewTasks.add(t);
                }
            }

            if (reviewTasks.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "No tasks are currently in REVIEW status for this project.", "No Action Needed", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Form to select review task
            JComboBox<TaskModel> cmbTasks = new JComboBox<>();
            for (TaskModel t : reviewTasks) {
                cmbTasks.addItem(t);
            }
            cmbTasks.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof TaskModel) {
                        TaskModel t = (TaskModel) value;
                        setText(t.getTitle() + " (Link: " + t.getSubmissionLink() + ")");
                    }
                    return this;
                }
            });

            JPanel reviewPanel = new JPanel(new GridLayout(2, 1, 8, 8));
            reviewPanel.add(new JLabel("Select a submission deliverable to approve:"));
            reviewPanel.add(cmbTasks);

            int result = JOptionPane.showConfirmDialog(
                    mainFrame,
                    reviewPanel,
                    "Approve Task Submission",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION) {
                TaskModel task = (TaskModel) cmbTasks.getSelectedItem();
                if (task != null) {
                    try {
                        task.setStatus(TaskStatus.DONE);
                        repository.saveTask(task, loaded.getId());

                        JOptionPane.showMessageDialog(mainFrame, "Task marked as DONE successfully!", "Task Approved", JOptionPane.INFORMATION_MESSAGE);
                        refreshData();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(mainFrame, "Approval failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    // =========================================================================
    // Team Member Actions Handlers
    // =========================================================================

    /**
     * Action handler for status updates. Prompts DEV/UIUX and enforces transitions.
     */
    private class UpdateStatusButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            TaskModel task = memberView.getSelectedTask();
            if (task == null) {
                JOptionPane.showMessageDialog(mainFrame, "Please select a task from the table first.", "No Task Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Enforce role restriction: Members can only choose TODO, IN_PROGRESS, REVIEW (never DONE directly)
            TaskStatus[] options = { TaskStatus.TODO, TaskStatus.IN_PROGRESS, TaskStatus.REVIEW };

            TaskStatus selected = (TaskStatus) JOptionPane.showInputDialog(
                    mainFrame,
                    "Choose target status for task \"" + task.getTitle() + "\":",
                    "Update Status",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    task.getStatus()
            );

            if (selected != null && selected != task.getStatus()) {
                String link = task.getSubmissionLink();

                if (selected == TaskStatus.REVIEW) {
                    link = JOptionPane.showInputDialog(
                            mainFrame,
                            "Enter the GitHub Repository URL or Figma design URL to proceed to REVIEW:",
                            "Submission Link Required",
                            JOptionPane.QUESTION_MESSAGE
                    );

                    if (link == null) {
                        return; // Canceled dialog
                    }
                    link = link.trim();
                    if (link.isEmpty()) {
                        JOptionPane.showMessageDialog(mainFrame, "Submission link is mandatory to submit for review.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                try {
                    task.setStatus(selected);
                    task.setSubmissionLink(link);

                    int projectId = taskProjectMap.getOrDefault(task.getId(), 0);
                    if (projectId <= 0) {
                        JOptionPane.showMessageDialog(mainFrame, "Failed to resolve project scope for the task.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    repository.saveTask(task, projectId);

                    JOptionPane.showMessageDialog(mainFrame, "Status updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(mainFrame, "Failed to update task: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // =========================================================================
    // Kanban Board Integration
    // =========================================================================

    /**
     * Opens the interactive Kanban board window for a specific project.
     * Enforces role access controls on transitions.
     *
     * @param project the project whose tasks should be displayed
     */
    public void openTaskBoard(ProjectModel project) {
        TaskBoardView boardView = new TaskBoardView();

        JFrame boardFrame = new JFrame("Task Kanban Board - " + project.getName());
        boardFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        boardFrame.setSize(950, 650);
        boardFrame.setContentPane(boardView);
        boardFrame.setLocationRelativeTo(mainFrame);

        // Load project tasks
        ProjectModel loaded = repository.findProjectById(project.getId());
        boardView.displayBoard(loaded.getTasks());

        // Status change listener from drag/move popup on Kanban cards
        boardView.addStatusChangeListener(e -> {
            TaskModel task = boardView.getActiveTaskForMove();
            TaskStatus targetStatus = boardView.getActiveTargetStatus();
            String link = boardView.getActiveSubmissionLink();

            // Role restriction enforcement: only PM can move tasks to DONE
            if (targetStatus == TaskStatus.DONE && currentUser.getRole() != UserRole.PM) {
                JOptionPane.showMessageDialog(
                        boardFrame,
                        "Access Denied: Only managers can approve tasks and move them to DONE.",
                        "Role Enforcement",
                        JOptionPane.ERROR_MESSAGE
                );
                // Revert board display
                ProjectModel reset = repository.findProjectById(project.getId());
                boardView.displayBoard(reset.getTasks());
                return;
            }

            try {
                task.setStatus(targetStatus);
                if (targetStatus == TaskStatus.REVIEW) {
                    task.setSubmissionLink(link);
                }

                repository.saveTask(task, project.getId());

                // Refresh Kanban Board
                ProjectModel refreshed = repository.findProjectById(project.getId());
                boardView.displayBoard(refreshed.getTasks());

                // Refresh parent dashboard
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(boardFrame, "Failed to update task: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        boardFrame.setVisible(true);
    }

    // =========================================================================
    // Database Helpers
    // =========================================================================

    /**
     * Connects to database to load all users with DEV or UIUX roles.
     */
    private List<User> getAssignableUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT id, username, role FROM USERS WHERE role IN ('DEV', 'UIUX')";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        "",
                        UserRole.fromString(rs.getString("role"))
                ));
            }
        } catch (SQLException ex) {
            System.err.println("SQL Exception loading assignable users: " + ex.getMessage());
        }
        return users;
    }
}
