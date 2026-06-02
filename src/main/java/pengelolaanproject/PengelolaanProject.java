package pengelolaanproject;

import pengelolaanproject.controller.AuthController;
import pengelolaanproject.controller.DashboardController;
import pengelolaanproject.core.DatabaseConnection;
import pengelolaanproject.core.SessionManager;
import pengelolaanproject.model.AuthModel;
import pengelolaanproject.model.User;
import pengelolaanproject.repository.IProjectRepository;
import pengelolaanproject.repository.ProjectRepository;
import pengelolaanproject.view.AuthView;
import javax.swing.*;

/**
 * Main application runner and entry point.
 */
public class PengelolaanProject {

    public static void main(String[] args) {
        // Apply System Look and Feel for native operating system window borders
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        // Start Java Swing application on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            AuthModel model = new AuthModel();
            AuthView view = new AuthView();

            // Routing callback triggered on successful login
            Runnable onLoginSuccess = () -> {
                // Initialize repository and launch main dashboard workspace
                IProjectRepository repository = new ProjectRepository(DatabaseConnection.getInstance().getConnection());
                new DashboardController(repository);
            };

            // Wire MVVM / MVC layers
            new AuthController(model, view, onLoginSuccess);

            // Render view visible to user
            view.setVisible(true);
        });
    }
}
