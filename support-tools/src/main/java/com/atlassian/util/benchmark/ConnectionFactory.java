package com.atlassian.util.benchmark;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory
{
    private final String userName;
    private final String password;
    private final String url;

    public ConnectionFactory(String userName, String password, String url, String driverClass) {
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }

        this.userName = userName;
        this.password = password;
        this.url = url;
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