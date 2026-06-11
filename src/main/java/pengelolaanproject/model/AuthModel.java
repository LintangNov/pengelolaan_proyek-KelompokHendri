package pengelolaanproject.model;

import pengelolaanproject.core.DatabaseConnection;
import pengelolaanproject.core.UserRole;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Model class handling authentication business logic.
 * Queries the USERS table to validate credentials.
 */
public class AuthModel {
    private final Connection connection;

    public AuthModel(Connection connection) {
        this.connection = connection;
    }

    /**
     * Queries the USERS table for a matching username and password.
     * On success, returns a fully populated User object.
     * On failure, returns null.
     *
     * @param username user-input username
     * @param password user-input password
     * @return User object or null if credentials are invalid or query fails
     */
    public User login(String username, String password) {
        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            return null;
        }

        String query = "SELECT id, username, password, role FROM USERS WHERE username = ? AND password = ?";
        Connection conn = this.connection;

        if (conn == null) {
            System.err.println("Error: Database connection is offline or unavailable.");
            return null;
        }

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username.trim());
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String dbUsername = rs.getString("username");
                    String dbPassword = rs.getString("password");
                    String dbRole = rs.getString("role");

                    UserRole role = UserRole.fromString(dbRole);
                    return new User(id, dbUsername, dbPassword, role);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception occurred during authentication: " + e.getMessage());
        }
        return null;
    }

    /**
     * Registers a new user. Checks for username uniqueness.
     * On success, returns true. On failure, returns false.
     */
    public boolean register(String username, String password, UserRole role) {
        if (username == null || password == null || role == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            return false;
        }

        // Check if username already exists
        String checkQuery = "SELECT id FROM USERS WHERE username = ?";
        Connection conn = this.connection;
        if (conn == null) {
            System.err.println("Error: Database connection is offline or unavailable.");
            return false;
        }

        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setString(1, username.trim());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    System.err.println("Username already exists: " + username);
                    return false; // Already exists
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception during username availability check: " + e.getMessage());
            return false;
        }

        // Insert new user
        String insertQuery = "INSERT INTO USERS (username, password, role) VALUES (?, ?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
            insertStmt.setString(1, username.trim());
            insertStmt.setString(2, password);
            insertStmt.setString(3, role.name());
            int affectedRows = insertStmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("SQL Exception during user registration: " + e.getMessage());
            return false;
        }
    }
}
