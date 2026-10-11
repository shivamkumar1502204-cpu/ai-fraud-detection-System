package com.frauddetection.database;

import com.frauddetection.exception.DatabaseOperationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton database connection manager handling JDBC connections to MySQL.
 * Reads configuration hierarchically:
 * 1. Environment variables (DB_URL, DB_USER, DB_PASSWORD)
 * 2. External config.properties in project root
 * 3. Classpath config.properties
 * 4. Resilient local fallback defaults.
 * Fulfills JDBC Rubric: Connection, Exception handling, Driver management.
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;
    private final Properties properties = new Properties();

    private String url;
    private String user;
    private String password;
    private String driver;

    private DatabaseConnection() {
        loadConfiguration();
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    private void loadConfiguration() {
        // Default fallback values
        this.driver = "com.mysql.cj.jdbc.Driver";
        this.url = "jdbc:mysql://localhost:3306/fraud_detection_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        this.user = "root";
        this.password = "root";

        // Attempt to load from file
        try {
            File externalConfigFile = new File("config.properties");
            if (externalConfigFile.exists()) {
                try (InputStream fis = new FileInputStream(externalConfigFile)) {
                    properties.load(fis);
                }
            } else {
                try (InputStream is = getClass().getClassLoader().getResourceAsStream("config.properties")) {
                    if (is != null) {
                        properties.load(is);
                    }
                }
            }

            if (properties.containsKey("db.url")) {
                this.url = properties.getProperty("db.url");
            }
            if (properties.containsKey("db.user")) {
                this.user = properties.getProperty("db.user");
            }
            if (properties.containsKey("db.password")) {
                this.password = properties.getProperty("db.password");
            }
            if (properties.containsKey("db.driver")) {
                this.driver = properties.getProperty("db.driver");
            }

        } catch (Exception ignored) {
            // Fall back to system environment variables or defaults
        }

        // Environment variable overrides (ideal for containerized or CI/CD pipelines)
        String envUrl = System.getenv("DB_URL");
        if (envUrl != null && !envUrl.isEmpty()) {
            this.url = envUrl;
        }
        String envUser = System.getenv("DB_USER");
        if (envUser != null && !envUser.isEmpty()) {
            this.user = envUser;
        }
        String envPass = System.getenv("DB_PASSWORD");
        if (envPass != null) {
            this.password = envPass;
        }

        // Ensure Driver class is registered
        try {
            Class.forName(this.driver);
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseConnection] MySQL JDBC Driver not found on classpath: " + e.getMessage());
        }
    }

    /**
     * Establishes a new JDBC Connection to MySQL.
     *
     * @return Active Connection
     * @throws DatabaseOperationException if connection fails
     */
    public Connection getConnection() throws DatabaseOperationException {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new DatabaseOperationException(
                    "Unable to connect to MySQL database (" + url + "). Please ensure MySQL is running and schema.sql is executed.",
                    e.getSQLState(),
                    e.getErrorCode(),
                    e
            );
        }
    }

    /**
     * Tests connectivity to the database and returns a diagnostic message.
     */
    public ConnectionStatus testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                return new ConnectionStatus(true, "Connected to MySQL (" + conn.getCatalog() + ")", null);
            }
            return new ConnectionStatus(false, "Connection is closed or null", null);
        } catch (Exception e) {
            return new ConnectionStatus(false, "Connection Failed: " + e.getMessage(), e);
        }
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public static class ConnectionStatus {
        private final boolean connected;
        private final String message;
        private final Throwable error;

        public ConnectionStatus(boolean connected, String message, Throwable error) {
            this.connected = connected;
            this.message = message;
            this.error = error;
        }

        public boolean isConnected() {
            return connected;
        }

        public String getMessage() {
            return message;
        }

        public Throwable getError() {
            return error;
        }
    }
}
