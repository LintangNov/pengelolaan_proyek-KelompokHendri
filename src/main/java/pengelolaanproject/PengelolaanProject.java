package pengelolaanproject;

import pengelolaanproject.controller.AuthController;
import pengelolaanproject.core.SessionManager;
import pengelolaanproject.model.AuthModel;
import pengelolaanproject.model.User;
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
                User user = SessionManager.getInstance().getCurrentUser();

                // Representing the visual dashboard trigger contract
                JOptionPane.showMessageDialog(
                        null,
                        "Successfully Logged In!\n\n" +
                        "Welcome, " + user.getUsername() + "\n" +
                        "Role: " + user.getRole() + "\n" +
                        "Session Token Active: " + SessionManager.getInstance().isLoggedIn(),
                        "Dashboard Navigation Signal",
                        JOptionPane.INFORMATION_MESSAGE
                );

                // Safe termination for verification exit, or could run full dashboard frame.
                System.exit(0);
            };

            // Wire MVVM / MVC layers
            new AuthController(model, view, onLoginSuccess);

            // Render view visible to user
            view.setVisible(true);
        });
    }
}
