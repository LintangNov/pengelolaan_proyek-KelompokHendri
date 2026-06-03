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
}
