package com.football;

import com.football.bd.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class TestConnection {

    public void run() {
        DBConnection db = new DBConnection();
        try (Connection conn = db.getDBConnection()) {
            if (conn != null) {
                System.out.println("Connexion réussie !");
            } else {
                System.out.println("Connexion échouée !");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        new TestConnection().run();
    }
}
