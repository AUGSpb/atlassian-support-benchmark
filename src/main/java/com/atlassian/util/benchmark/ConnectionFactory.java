package com.atlassian.util.benchmark;

import com.atlassian.util.JiraDatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.apache.commons.lang3.Validate.notNull;

public class ConnectionFactory {
    private final String userName;
    private final String password;
    private final String url;

    public ConnectionFactory(String username, String password, String url, String driverClass) {
        notNull(username);
        notNull(driverClass);
        notNull(url);

        // Check that driver class is accessible
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
        this.userName = username;
        this.password = password;
        this.url = url;
    }

    public ConnectionFactory(JiraDatabaseConfig config) {
        this(config.getUsername(), config.getPassword(), config.getUrl(), config.getDriverClass());
    }

    /**
     * Attempts to establish a connection to the given database URL.
     * The <code>DriverManager</code> attempts to select an appropriate driver from
     * the set of registered JDBC drivers.
     * @return a connection
     */
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(url, userName, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getURL() {
        return url;
    }
}