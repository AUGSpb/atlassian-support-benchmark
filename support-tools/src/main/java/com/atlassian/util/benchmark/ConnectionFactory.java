package com.atlassian.util.benchmark;

import com.atlassian.util.JiraDatabaseConfig;
import com.sun.istack.internal.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.apache.commons.lang3.Validate.notNull;

public class ConnectionFactory
{
    private final String userName;
    private final String password;
    private final String url;

    public ConnectionFactory(@NotNull String username, String password, @NotNull String url, @NotNull String driverClass) {
        notNull(username);
        notNull(driverClass);
        notNull(url);
        
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

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(url, userName, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getURL()
    {
        return url;
    }
}