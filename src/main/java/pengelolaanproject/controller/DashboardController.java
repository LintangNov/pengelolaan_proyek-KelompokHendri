package pengelolaanproject.controller;

import pengelolaanproject.core.BaseController;
import pengelolaanproject.core.DatabaseConnection;
import pengelolaanproject.core.SessionManager;
import pengelolaanproject.core.UserRole;
import pengelolaanproject.model.ProjectModel;
import pengelolaanproject.model.ProjectStatus;
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
    private final Runnable onLogout;

    // View Components
    private final DashboardView dashboardView;
    private ManagerDashboardView managerView;
    private MemberDashboardView memberView;

    // Injected Controller
    private final ProjectController projectController;

    // Maps taskId to projectId for member views where project scope is not directly available
    private final Map<Integer, Integer> taskProjectMap = new HashMap<>();
    private final Map<Integer, String> taskProjectNameMap = new HashMap<>();

    private Map<Integer, User> userCache = new HashMap<>();

    /**
     * Constructs the controller, initializes the main app frame, and setups the workspace based on user role.
     *
     * @param repository        project repository data contract
     * @param dashboardView     the view injected based on role
     * @param projectController the project controller injected for project creation
     * @param onLogout          callback fired upon logout request
     */
    public DashboardController(IProjectRepository repository, DashboardView dashboardView, ProjectController projectController, Runnable onLogout) {
        this.repository = repository;
        this.dashboardView = dashboardView;
        this.projectController = projectController;
        this.onLogout = onLogout;
        this.currentUser = SessionManager.getInstance().getCurrentUser();

        // Initialize application main window
        this.mainFrame = new JFrame("Project Management System - Workspace");
        this.mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.mainFrame.setSize(1024, 768);
        this.mainFrame.setLocationRelativeTo(null);

        if (currentUser == null) {
            JOptionPane.showMessageDialog(null, "Sesi pengguna aktif tidak ditemukan. Keluar.", "Error Sesi", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        loadUserCache();
        initializeWorkspace();
    }

    private void loadUserCache() {
        userCache.clear();
        String query = "SELECT id, username, role FROM USERS";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null) return;
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                User u = new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    "",
                    UserRole.fromString(rs.getString("role"))
                );
                userCache.put(u.getId(), u);
            }
        } catch (SQLException ex) {
            System.err.println("Failed to load user cache: " + ex.getMessage());
        }
    }

    /**
     * Instantiates correct dashboard view based on user role, registers listeners, and displays frame.
     */
    private void initializeWorkspace() {
        if (dashboardView instanceof ManagerDashboardView) {
            this.managerView = (ManagerDashboardView) dashboardView;

            // Wire manager quick actions
            this.managerView.addCreateProjectListener(new CreateProjectButtonListener());
            this.managerView.addEditProjectListener(new EditProjectButtonListener());
            this.managerView.addSelesaikanProjectListener(new SelesaikanProjectButtonListener());
            this.managerView.addLogoutListener(new LogoutButtonListener());

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
            this.memberView.addLogoutListener(new LogoutButtonListener());

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
            List<TaskModel> visibleTasks = new ArrayList<>();
            taskProjectMap.clear();
            taskProjectNameMap.clear();

            for (ProjectModel project : allProjects) {
                for (TaskModel task : project.getTasks()) {
                    User assignee = userCache.get(task.getAssigneeId());
                    if (assignee != null && assignee.getRole() == currentUser.getRole()) {
                        visibleTasks.add(task);
                        taskProjectMap.put(task.getId(), project.getId());
                        taskProjectNameMap.put(task.getId(), project.getName());
                    }
                }
            }
            memberView.displayTasks(visibleTasks, userCache, taskProjectNameMap);
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
     * Action handler for Logout. Disposes frame and runs logout routing callback.
     */
    private class LogoutButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int confirm = JOptionPane.showConfirmDialog(
                    mainFrame,
                    "Are you sure you want to log out?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                mainFrame.dispose();
                if (onLogout != null) {
                    onLogout.run();
                }
            }
        }
    }

    private class EditProjectButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ProjectModel project = managerView.getSelectedProject();
            if (project == null) {
                JOptionPane.showMessageDialog(mainFrame,
                    "Pilih project dari tabel terlebih dahulu.",
                    "Tidak Ada Project Dipilih", JOptionPane.WARNING_MESSAGE);
                return;
            }

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

            JTextField txtName     = new JTextField(project.getName());
            JTextField txtStart    = new JTextField(project.getStartDate() != null ? df.format(project.getStartDate()) : "");
            JTextField txtDeadline = new JTextField(project.getDeadline()  != null ? df.format(project.getDeadline())  : "");

            JComboBox<ProjectStatus> cmbStatus = new JComboBox<>(ProjectStatus.values());
            cmbStatus.setSelectedItem(project.getStatus() != null ? project.getStatus() : ProjectStatus.AKTIF);
            cmbStatus.setForeground(new Color(34, 40, 49));
            cmbStatus.setBackground(Color.WHITE);
            cmbStatus.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (!isSelected) {
                        c.setForeground(new Color(34, 40, 49));
                        c.setBackground(Color.WHITE);
                    }
                    return c;
                }
            });

            JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
            form.add(new JLabel("Nama Project:"));    form.add(txtName);
            form.add(new JLabel("Start Date (YYYY-MM-DD):")); form.add(txtStart);
            form.add(new JLabel("Deadline (YYYY-MM-DD):")); form.add(txtDeadline);
            form.add(new JLabel("Status Project:")); form.add(cmbStatus);

            int result = JOptionPane.showConfirmDialog(
                mainFrame, form,
                "Edit Project: " + project.getName(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
            );

            if (result != JOptionPane.OK_OPTION) return;

            String newName = txtName.getText().trim();
            if (newName.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "Nama project tidak boleh kosong.",
                    "Validasi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Date newDeadline  = null;
            Date newStartDate = project.getStartDate();

            if (!txtDeadline.getText().trim().isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    sdf.setLenient(false);
                    newDeadline = sdf.parse(txtDeadline.getText().trim());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(mainFrame,
                        "Format deadline tidak valid. Gunakan YYYY-MM-DD.",
                        "Validasi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            if (!txtStart.getText().trim().isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    sdf.setLenient(false);
                    newStartDate = sdf.parse(txtStart.getText().trim());
                } catch (Exception ex) { /* biarkan nilai lama */ }
            }

            if (newStartDate != null && newDeadline != null && newStartDate.after(newDeadline)) {
                JOptionPane.showMessageDialog(mainFrame,
                    "Start date tidak boleh setelah deadline.",
                    "Validasi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            ProjectStatus newStatus = (ProjectStatus) cmbStatus.getSelectedItem();

            try {
                project.setName(newName);
                project.setDeadline(newDeadline);
                project.setStartDate(newStartDate);
                project.setStatus(newStatus);
                repository.saveProject(project);
                JOptionPane.showMessageDialog(mainFrame,
                    "Project berhasil diperbarui!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame,
                    "Gagal menyimpan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class SelesaikanProjectButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ProjectModel project = managerView.getSelectedProject();
            if (project == null) {
                JOptionPane.showMessageDialog(mainFrame,
                    "Pilih project dari tabel terlebih dahulu.",
                    "Tidak Ada Project Dipilih", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (project.getStatus() == ProjectStatus.SELESAI) {
                JOptionPane.showMessageDialog(mainFrame,
                    "Project ini sudah selesai.",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    mainFrame,
                    "Apakah Anda yakin ingin menyelesaikan project \"" + project.getName() + "\"?",
                    "Konfirmasi Selesaikan Project",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    project.setStatus(ProjectStatus.SELESAI);
                    repository.saveProject(project);
                    JOptionPane.showMessageDialog(mainFrame,
                        "Project \"" + project.getName() + "\" berhasil diselesaikan!",
                        "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(mainFrame,
                        "Gagal menyelesaikan project: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(mainFrame, "Pilih task dari tabel terlebih dahulu.", "Tidak Ada Task Dipilih", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Enforce assignee restriction: members can only update tasks assigned to themselves
            if (currentUser.getRole() != UserRole.PM && task.getAssigneeId() != currentUser.getId()) {
                JOptionPane.showMessageDialog(
                        mainFrame,
                        "Access Denied: You can only update tasks assigned to you.",
                        "Access Restriction",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            // Enforce role restriction: Members can only choose TODO, IN_PROGRESS, REVIEW (never DONE directly)
            TaskStatus[] options = { TaskStatus.TODO, TaskStatus.IN_PROGRESS, TaskStatus.REVIEW };

            JComboBox<TaskStatus> cmbStatus = new JComboBox<>(options);
            cmbStatus.setSelectedItem(task.getStatus());
            cmbStatus.setForeground(new Color(34, 40, 49));
            cmbStatus.setBackground(Color.WHITE);
            cmbStatus.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (!isSelected) {
                        c.setForeground(new Color(34, 40, 49));
                        c.setBackground(Color.WHITE);
                    }
                    return c;
                }
            });
            
            JTextArea txtNotes = new JTextArea(task.getNotes(), 3, 20);
            txtNotes.setFont(UIManager.getFont("TextField.font"));
            txtNotes.setLineWrap(true);
            txtNotes.setWrapStyleWord(true);
            JScrollPane notesScroll = new JScrollPane(txtNotes);
            
            JTextField txtLink = new JTextField(task.getSubmissionLink());
            txtLink.setEnabled(task.getStatus() == TaskStatus.REVIEW);

            cmbStatus.addActionListener(evt -> {
                txtLink.setEnabled(cmbStatus.getSelectedItem() == TaskStatus.REVIEW);
            });

            JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
            panel.add(new JLabel("Status:"));
            panel.add(cmbStatus);
            panel.add(new JLabel("Notes (Optional):"));
            panel.add(notesScroll);
            panel.add(new JLabel("Submission Link (Required for REVIEW):"));
            panel.add(txtLink);

            int result = JOptionPane.showConfirmDialog(
                    mainFrame,
                    panel,
                    "Update Task: " + task.getTitle(),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION) {
                TaskStatus selected = (TaskStatus) cmbStatus.getSelectedItem();
                String notes = txtNotes.getText().trim();
                String link = txtLink.getText().trim();

                if (selected == TaskStatus.REVIEW && link.isEmpty()) {
                    JOptionPane.showMessageDialog(mainFrame, "Tautan pengumpulan wajib diisi untuk review.", "Validasi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    task.setStatus(selected);
                    task.setNotes(notes);
                    if (selected == TaskStatus.REVIEW) {
                        task.setSubmissionLink(link);
                    }

                    int projectId = taskProjectMap.getOrDefault(task.getId(), 0);
                    if (projectId <= 0) {
                        JOptionPane.showMessageDialog(mainFrame, "Gagal memuat detail project untuk task ini.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    repository.saveTask(task, projectId);

                    JOptionPane.showMessageDialog(mainFrame, "Status berhasil diperbarui!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(mainFrame, "Gagal memperbarui task: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // =========================================================================
    // Kanban Board Integration
    // =========================================================================

    private void showAssignTaskDialog(ProjectModel project, Component parent) {
        // Retrieve assignable users (DEV, UIUX) from database to prevent input errors
        List<User> assignableUsers = getAssignableUsers();
        if (assignableUsers.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Tidak ada developer atau desainer yang dapat ditugaskan.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Construct form UI
        JTextField txtTitle = new JTextField();
        JTextArea txtDesc = new JTextArea(3, 20);
        txtDesc.setFont(UIManager.getFont("TextField.font"));
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(txtDesc);

        JComboBox<User> cmbAssignee = new JComboBox<>();
        for (User u : assignableUsers) {
            cmbAssignee.addItem(u);
        }
        cmbAssignee.setForeground(new Color(34, 40, 49));
        cmbAssignee.setBackground(Color.WHITE);
        // Pretty formatting for assignee selector
        cmbAssignee.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof User) {
                    User u = (User) value;
                    setText(u.getUsername() + " (" + u.getRole() + ")");
                }
                if (!isSelected) {
                    setForeground(new Color(34, 40, 49));
                    setBackground(Color.WHITE);
                }
                return this;
            }
        });

        JTextField txtDueDate = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 8, 8));
        formPanel.add(new JLabel("Task Title:"));
        formPanel.add(txtTitle);
        formPanel.add(new JLabel("Description (Optional):"));
        formPanel.add(descScroll);
        formPanel.add(new JLabel("Assignee:"));
        formPanel.add(cmbAssignee);
        formPanel.add(new JLabel("Due Date (YYYY-MM-DD):"));
        formPanel.add(txtDueDate);

        int result = JOptionPane.showConfirmDialog(
                parent,
                formPanel,
                "Assign New Task to " + project.getName(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String title = txtTitle.getText().trim();
            String description = txtDesc.getText().trim();
            User assignee = (User) cmbAssignee.getSelectedItem();
            String dueDateStr = txtDueDate.getText().trim();

            // Validation
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "Judul task tidak boleh kosong.", "Validasi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (assignee == null) {
                JOptionPane.showMessageDialog(parent, "Assignee wajib diisi.", "Validasi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Date dueDate;
            try {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                df.setLenient(false);
                dueDate = df.parse(dueDateStr);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parent, "Format tenggat waktu tidak valid. Gunakan YYYY-MM-DD.", "Validasi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // Create task model and save
                TaskModel task = new TaskModel(title, assignee.getId());
                task.setDescription(description);
                task.setDueDate(dueDate);
                task.setStatus(TaskStatus.TODO);

                repository.saveTask(task, project.getId());

                JOptionPane.showMessageDialog(parent, "Task berhasil ditugaskan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parent, "Gagal menyimpan task: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

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

        // Tombol Assign Task hanya visible untuk PM
        boardView.setAssignTaskVisible(currentUser.getRole() == UserRole.PM);

        // Wire listener assign task
        boardView.addAssignTaskListener(ev -> {
            showAssignTaskDialog(project, boardFrame);
            ProjectModel refreshed = repository.findProjectById(project.getId());
            if (refreshed != null) {
                boardView.setProjectStatus(refreshed.getStatus() != null ? refreshed.getStatus().name() : "AKTIF");
                boardView.displayBoard(refreshed.getTasks(), userCache, refreshed.getName());
            }
        });

        // Load project tasks
        ProjectModel loaded = repository.findProjectById(project.getId());
        if (loaded != null) {
            boardView.setProjectStatus(loaded.getStatus() != null ? loaded.getStatus().name() : "AKTIF");
            boardView.displayBoard(loaded.getTasks(), userCache, loaded.getName());
        }

        // Status change listener from drag/move popup on Kanban cards
        boardView.addStatusChangeListener(e -> {
            TaskModel task = boardView.getActiveTaskForMove();
            TaskStatus targetStatus = boardView.getActiveTargetStatus();
            String link = boardView.getActiveSubmissionLink();

            // Role restriction enforcement: only PM can move tasks to DONE
            if (targetStatus == TaskStatus.DONE && currentUser.getRole() != UserRole.PM) {
                JOptionPane.showMessageDialog(
                        boardFrame,
                        "Akses Ditolak: Hanya manajer yang dapat menyetujui task dan memindahkannya ke DONE.",
                        "Batasan Hak Akses",
                        JOptionPane.ERROR_MESSAGE
                );
                // Revert board display
                ProjectModel reset = repository.findProjectById(project.getId());
                if (reset != null) {
                    boardView.setProjectStatus(reset.getStatus() != null ? reset.getStatus().name() : "AKTIF");
                    boardView.displayBoard(reset.getTasks(), userCache, reset.getName());
                }
                return;
            }

            // Enforce assignee restriction: members can only move tasks assigned to themselves
            if (currentUser.getRole() != UserRole.PM && task.getAssigneeId() != currentUser.getId()) {
                JOptionPane.showMessageDialog(
                        boardFrame,
                        "Access Denied: You can only update tasks assigned to you.",
                        "Access Restriction",
                        JOptionPane.ERROR_MESSAGE
                );
                // Revert board display
                ProjectModel reset = repository.findProjectById(project.getId());
                if (reset != null) {
                    boardView.setProjectStatus(reset.getStatus() != null ? reset.getStatus().name() : "AKTIF");
                    boardView.displayBoard(reset.getTasks(), userCache, reset.getName());
                }
                return;
            }

            // Open confirmation panel with notes and (if REVIEW) link input
            JTextArea txtNotes = new JTextArea(task.getNotes(), 3, 20);
            txtNotes.setFont(UIManager.getFont("TextField.font"));
            txtNotes.setLineWrap(true);
            txtNotes.setWrapStyleWord(true);
            JScrollPane notesScroll = new JScrollPane(txtNotes);
 
            JTextField txtLink = new JTextField(task.getSubmissionLink());
 
            JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
            panel.add(new JLabel("Notes (Optional):"));
            panel.add(notesScroll);
 
            if (targetStatus == TaskStatus.REVIEW) {
                panel = new JPanel(new GridLayout(3, 2, 8, 8));
                panel.add(new JLabel("Notes (Optional):"));
                panel.add(notesScroll);
                panel.add(new JLabel("Submission Link (Required for REVIEW):"));
                panel.add(txtLink);
            }
 
            int confirm = JOptionPane.showConfirmDialog(
                    boardFrame,
                    panel,
                    "Konfirmasi Pindah ke " + targetStatus.name(),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );
 
            if (confirm == JOptionPane.OK_OPTION) {
                String notes = txtNotes.getText().trim();
                String linkInput = txtLink.getText().trim();
 
                if (targetStatus == TaskStatus.REVIEW && linkInput.isEmpty()) {
                    JOptionPane.showMessageDialog(boardFrame, "Tautan pengumpulan wajib diisi untuk review.", "Validasi", JOptionPane.ERROR_MESSAGE);
                    // Revert board display
                    ProjectModel reset = repository.findProjectById(project.getId());
                    if (reset != null) {
                        boardView.setProjectStatus(reset.getStatus() != null ? reset.getStatus().name() : "AKTIF");
                        boardView.displayBoard(reset.getTasks(), userCache);
                    }
                    return;
                }
 
                try {
                    task.setStatus(targetStatus);
                    task.setNotes(notes);
                    if (targetStatus == TaskStatus.REVIEW) {
                        task.setSubmissionLink(linkInput);
                    }
 
                    repository.saveTask(task, project.getId());
 
                    // Refresh Kanban Board
                    ProjectModel refreshed = repository.findProjectById(project.getId());
                    if (refreshed != null) {
                        boardView.setProjectStatus(refreshed.getStatus() != null ? refreshed.getStatus().name() : "AKTIF");
                        boardView.displayBoard(refreshed.getTasks(), userCache, refreshed.getName());
                    }
 
                    // Refresh parent dashboard
                    refreshData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(boardFrame, "Gagal memperbarui task: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Cancelled - Reset board display
                ProjectModel reset = repository.findProjectById(project.getId());
                if (reset != null) {
                    boardView.setProjectStatus(reset.getStatus() != null ? reset.getStatus().name() : "AKTIF");
                    boardView.displayBoard(reset.getTasks(), userCache, reset.getName());
                }
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
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null) {
            System.err.println("Database connection is null in getAssignableUsers");
            return users;
        }
        try (PreparedStatement stmt = conn.prepareStatement(query);
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
