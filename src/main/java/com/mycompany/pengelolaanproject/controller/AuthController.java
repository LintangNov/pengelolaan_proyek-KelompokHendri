package com.mycompany.pengelolaanproject.controller;

import com.mycompany.pengelolaanproject.core.BaseController;
import com.mycompany.pengelolaanproject.core.SessionManager;
import com.mycompany.pengelolaanproject.model.AuthModel;
import com.mycompany.pengelolaanproject.model.User;
import com.mycompany.pengelolaanproject.view.AuthView;
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

        // Wire event listener in the view
        this.view.addLoginListener(new LoginButtonListener());
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
}
