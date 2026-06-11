package pengelolaanproject.controller;

import pengelolaanproject.core.BaseController;
import pengelolaanproject.core.SessionManager;
import pengelolaanproject.model.AuthModel;
import pengelolaanproject.model.User;
import pengelolaanproject.view.AuthView;
import pengelolaanproject.view.RegisterView;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Controller class wiring AuthModel and AuthView.
 * Handles validation flow, sets SessionManager, and fires the dashboard route transition.
 */
public class AuthController extends BaseController {
    private final AuthModel model;
    private final AuthView view;
    private final Runnable onLoginSuccess;

    /**
     * Constructs the controller and wires the event listeners.
     *
     * @param model          data model for auth
     * @param view           visual interface frame
     * @param onLoginSuccess routing callback fired upon successful validation
     */
    public AuthController(AuthModel model, AuthView view, Runnable onLoginSuccess) {
        this.model = model;
        this.view = view;
        this.onLoginSuccess = onLoginSuccess;

        // Wire event listeners in the view
        this.view.addLoginListener(new LoginButtonListener());
        this.view.addRegisterLinkListener(new RegisterLinkListener());
    }

    /**
     * Inner class implementing action handling for login trigger.
     */
    private class LoginButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = view.getUsername();
            String password = view.getPassword();

            // Simple client-side validation
            if (username.isEmpty() || password.isEmpty()) {
                view.showError("Username and password fields cannot be empty.");
                return;
            }

            view.showError(""); // Reset any prior errors

            // Query database via model
            User user = model.login(username, password);

            if (user != null) {
                // Store user session in Singleton SessionManager
                SessionManager.getInstance().setCurrentUser(user);
                System.out.println("Login successful: " + user.getUsername() + " (" + user.getRole() + ")");

                // Close login panel
                view.close();

                // Fire the routing callback to load dashboard or next sequence
                if (onLoginSuccess != null) {
                    onLoginSuccess.run();
                }
            } else {
                view.showError("Invalid username or password. Please try again.");
            }
        }
    }

    /**
     * Inner class implementing action handling for registration link.
     */
    private class RegisterLinkListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            RegisterView registerView = new RegisterView();
            JDialog registerDialog = new JDialog(view, "Create New Account", true);
            registerDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            registerDialog.setContentPane(registerView);
            registerDialog.pack();
            registerDialog.setSize(440, 480);
            registerDialog.setLocationRelativeTo(view);
            registerDialog.setResizable(false);

            registerView.addRegisterListener(evt -> {
                String username = registerView.getUsername();
                String password = registerView.getPassword();
                pengelolaanproject.core.UserRole role = registerView.getSelectedRole();

                if (username.isEmpty() || password.isEmpty()) {
                    registerView.showError("Username and password cannot be empty.");
                    return;
                }

                registerView.showError(""); // Reset error

                boolean success = model.register(username, password, role);
                if (success) {
                    JOptionPane.showMessageDialog(
                            registerDialog,
                            "User account \"" + username + "\" successfully registered!",
                            "Registration Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    registerDialog.dispose();
                } else {
                    registerView.showError("Failed to register. Username might already be taken.");
                }
            });

            registerView.clearForm();
            registerDialog.setVisible(true);
        }
    }
}
