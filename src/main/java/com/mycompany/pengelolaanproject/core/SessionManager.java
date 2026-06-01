package com.mycompany.pengelolaanproject.core;

import com.mycompany.pengelolaanproject.model.User;

/**
 * Singleton class that stores the currently logged-in User object.
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {
    }

    /**
     * Retrieve the unique instance of SessionManager.
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Store the currently logged-in User.
     */
    public synchronized void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Get the currently logged-in User.
     */
    public synchronized User getCurrentUser() {
        return currentUser;
    }

    /**
     * Clear the user session (log out).
     */
    public synchronized void clearSession() {
        this.currentUser = null;
    }

    /**
     * Check if a user is currently logged in.
     */
    public synchronized boolean isLoggedIn() {
        return currentUser != null;
    }
}
