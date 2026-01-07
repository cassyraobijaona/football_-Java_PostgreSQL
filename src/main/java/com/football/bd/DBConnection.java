package com.football.bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public Connection getDBConnection() throws SQLException {

        String url = System.getenv("JDBC_URL");
        String user = System.getenv("USERNAME");
        String password = System.getenv("PASSWORD");

        if (url == null || user == null || password == null) {
            throw new RuntimeException("Variables d'environnement manquantes");
        }
        System.out.println(System.getenv("JDBC_URL"));
        System.out.println(System.getenv("USERNAME"));
        System.out.println(System.getenv("PASSWORD"));

        return DriverManager.getConnection(url, user, password);
    }

}
