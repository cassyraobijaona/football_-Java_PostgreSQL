package com.football.bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private final String url = "jdbc:postgresql://localhost:5432/mini_football_db";
    private final String user = "mini_football_db_manager";
    private final String password = "malalatiana";

    public Connection getDBConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}