package pengelolaanproject;

import pengelolaanproject.controller.AuthController;
import pengelolaanproject.controller.DashboardController;
import pengelolaanproject.controller.ProjectController;
import pengelolaanproject.core.DatabaseConnection;
import pengelolaanproject.core.SessionManager;
import pengelolaanproject.core.UserRole;
import pengelolaanproject.model.AuthModel;
import pengelolaanproject.model.User;
import pengelolaanproject.repository.IProjectRepository;
import pengelolaanproject.repository.ProjectRepository;
import pengelolaanproject.view.AuthView;
import pengelolaanproject.view.DashboardView;
import pengelolaanproject.view.ManagerDashboardView;
import pengelolaanproject.view.MemberDashboardView;
import pengelolaanproject.view.ProjectView;
import pengelolaanproject.view.TaskBoardView;

import javax.swing.*;
import java.sql.Connection;

/**
 * Main application runner and entry point.
 */
public class Main {

    private static IProjectRepository projectRepository;
    private static AuthModel authModel;
    private static ProjectController projectController;

    public static void main(String[] args) {
        // Apply System Look and Feel for native operating system window borders
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        // Start Java Swing application on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            // 1. DatabaseConnection (get Connection)
            Connection connection = DatabaseConnection.getInstance().getConnection();

            // 2. ProjectRepository (inject Connection)
            projectRepository = new ProjectRepository(connection);

            // 3. AuthModel (inject Connection)
            authModel = new AuthModel(connection);

            // 4. ProjectView
            ProjectView projectView = new ProjectView();

            // 5. TaskBoardView
            TaskBoardView taskBoardView = new TaskBoardView();

            // 6. ProjectController (inject ProjectView, TaskBoardView, ProjectRepository)
            projectController = new ProjectController(projectView, taskBoardView, projectRepository);

            // Start Login interface
            showLogin();
        });
    }

    /**
     * Instantiates and displays the login screen, and handles the post-login routing.
     */
    public static void showLogin() {
        // 7. AuthView
        AuthView authView = new AuthView();

        // Routing callback triggered on successful login
        Runnable onLoginSuccess = () -> {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            DashboardView dashboardView;

            // Handle the routing based on UserRole
            if (currentUser.getRole() == UserRole.PM) {
                dashboardView = new ManagerDashboardView();
            } else {
                dashboardView = new MemberDashboardView();
            }

            // Callback when logging out
            Runnable onLogout = () -> {
                SessionManager.getInstance().clearSession();
                showLogin();
            };

            // Instantiate DashboardController with correct view type and logout callback
            new DashboardController(projectRepository, dashboardView, projectController, onLogout);
        };

        // 8. AuthController (inject AuthModel, AuthView; on success, instantiate DashboardController)
        new AuthController(authModel, authView, onLoginSuccess);

        // Render view visible to user
        authView.setVisible(true);
    }
}
