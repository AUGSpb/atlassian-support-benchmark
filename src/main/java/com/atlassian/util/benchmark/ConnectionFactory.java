package com.atlassian.util.benchmark;

import com.atlassian.util.JiraDatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.apache.commons.lang3.Validate.notNull;

public class ConnectionFactory {
    private final String username;
    private final String password;
    private final String url;
    private final String driverClass;
    private Connection connection;

    public ConnectionFactory(String username, String password, String url, String driverClass) {
        notNull(username);
        notNull(driverClass);
        notNull(url);

        // Check that driver class is accessible
        try {
            Class.forName(driverClass);
        } catch (Exception e) {
            System.out.println("Where is JDBC Driver? Include in your library path, please!");
            throw new IllegalArgumentException(e);
        }
        this.username = username;
        this.password = password;
        this.url = url;
        this.driverClass = driverClass;
    }

    public ConnectionFactory(JiraDatabaseConfig config) {
        this(config.getUsername(), config.getPassword(), config.getUrl(), config.getDriverClass());
    }

    /**
     * Attempts to establish a connection to the given database URL.
     * The <code>DriverManager</code> attempts to select an appropriate driver from
     * the set of registered JDBC drivers.
     *
     * @return a connection
     */
    public Connection getConnection() {
        if (connection !=null){
            return connection;
        }
        try {
            return connection = DriverManager.getConnection(url, username, password);
        } catch (final SQLException e) {
            System.out.println("URL: " + url);
            System.out.println("Username: " + username);
            System.out.println("Password: " + password);
            throw new RuntimeException("Error running SQL: " + e.getMessage(), e);
        }
    }

    public String getURL() {
        return url;
    }
}