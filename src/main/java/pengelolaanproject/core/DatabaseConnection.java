package pengelolaanproject.core;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Thread-safe Singleton class that manages a single JDBC connection to MySQL.
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    private String url;
    private String username;
    private String password;

    private DatabaseConnection() {
        loadConfig();
    }

    /**
     * Loads the connection configuration from the classpath resources (db.properties).
     */
    private void loadConfig() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                System.err.println("Warning: db.properties not found in resources, applying defaults.");
                url = "jdbc:mysql://localhost:8111/pengelolaan_proyek";
                username = "root";
                password = "";
                return;
            }
            props.load(input);
            url = props.getProperty("db.url", "jdbc:mysql://localhost:8111/pengelolaan_proyek");
            username = props.getProperty("db.username", "root");
            password = props.getProperty("db.password", "");
        } catch (IOException ex) {
            System.err.println("Error reading db.properties: " + ex.getMessage());
            url = "jdbc:mysql://localhost:8111/pengelolaan_proyek";
            username = "root";
            password = "";
        }
    }

    /**
     * Retrieve the unique instance of DatabaseConnection.
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Returns a live, open java.sql.Connection.
     * Re-establishes the connection if it was closed or lost.
     */
    public synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Ensure Driver class is registered
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(url, username, password);
                System.out.println("Connection established to: " + url);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Failed to establish a live connection to MySQL: " + e.getMessage());
        }
        return connection;
    }
}
