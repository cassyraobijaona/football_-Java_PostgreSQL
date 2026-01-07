package com.football.dao;

import com.football.bd.DBConnection;
import com.football.model.ContinentEnum;
import com.football.model.Player;
import com.football.model.PlayerPositionEnum;
import com.football.model.Team;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    private final DBConnection dbConnection;

    public DataRetriever() {
        this.dbConnection = new DBConnection();
    }

    public Team findTeamById(Integer id) {
        Connection connection = null;
        PreparedStatement teamStmt = null;
        PreparedStatement playerStmt = null;
        ResultSet teamRs = null;
        ResultSet playerRs = null;
        Team team = null;

        try {
            connection = dbConnection.getDBConnection();

            String teamSql = "SELECT id, name, continent FROM Team WHERE id = ?";
            String playerSql = "SELECT id, name, age, position FROM Player WHERE id_team = ?";

            teamStmt = connection.prepareStatement(teamSql);
            teamStmt.setInt(1, id);
            teamRs = teamStmt.executeQuery();

            if (teamRs.next()) {
                team = new Team(
                        teamRs.getInt("id"),
                        teamRs.getString("name"),
                        ContinentEnum.valueOf(teamRs.getString("continent"))
                );

                playerStmt = connection.prepareStatement(playerSql);
                playerStmt.setInt(1, id);
                playerRs = playerStmt.executeQuery();

                while (playerRs.next()) {
                    Player player = new Player(
                            playerRs.getInt("id"),
                            playerRs.getString("name"),
                            playerRs.getInt("age"),
                            PlayerPositionEnum.valueOf(playerRs.getString("position")),
                            team
                    );
                    team.getPlayers().add(player);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try { if (playerRs != null) playerRs.close(); } catch (Exception ignored) {}
            try { if (playerStmt != null) playerStmt.close(); } catch (Exception ignored) {}
            try { if (teamRs != null) teamRs.close(); } catch (Exception ignored) {}
            try { if (teamStmt != null) teamStmt.close(); } catch (Exception ignored) {}
            try { if (connection != null) connection.close(); } catch (Exception ignored) {}
        }

        return team;
    }

    public List<Player> findPlayers(int page, int size) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Player> players = new ArrayList<>();
        int offset = (page - 1) * size;

        try {
            connection = dbConnection.getDBConnection();
            String sql = "SELECT id, name, age, position FROM Player ORDER BY id LIMIT ? OFFSET ?";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, size);
            stmt.setInt(2, offset);
            rs = stmt.executeQuery();

            while (rs.next()) {
                players.add(new Player(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        PlayerPositionEnum.valueOf(rs.getString("position")),
                        null
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (connection != null) connection.close(); } catch (Exception ignored) {}
        }

        return players;
    }

    public List<Player> createPlayers(List<Player> newPlayers) {
        Connection connection = null;
        PreparedStatement checkStmt = null;
        PreparedStatement insertStmt = null;
        ResultSet rs = null;

        String checkSql = "SELECT COUNT(*) FROM Player WHERE id = ?";
        String insertSql = "INSERT INTO Player (id, name, age, position, id_team) " +
                "VALUES (?, ?, ?, ?::position_enum, ?)";

        try {
            connection = dbConnection.getDBConnection();
            connection.setAutoCommit(false);

            checkStmt = connection.prepareStatement(checkSql);
            insertStmt = connection.prepareStatement(insertSql);


            for (Player player : newPlayers) {
                checkStmt.setInt(1, player.getId());
                rs = checkStmt.executeQuery();
                rs.next();
                if (rs.getInt(1) > 0) {
                    throw new RuntimeException("Player already exists with id: " + player.getId());
                }
                rs.close();
            }


            for (Player player : newPlayers) {
                insertStmt.setInt(1, player.getId());
                insertStmt.setString(2, player.getName());
                insertStmt.setInt(3, player.getAge());
                insertStmt.setString(4, player.getPosition().name());

                if (player.getTeam() != null) {
                    insertStmt.setInt(5, player.getTeam().getId());
                } else {
                    insertStmt.setNull(5, Types.INTEGER);
                }

                insertStmt.executeUpdate();
            }

            connection.commit();
            return newPlayers;

        } catch (SQLException e) {
            try { if (connection != null) connection.rollback(); } catch (Exception ignored) {}
            throw new RuntimeException(e);
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (checkStmt != null) checkStmt.close(); } catch (Exception ignored) {}
            try { if (insertStmt != null) insertStmt.close(); } catch (Exception ignored) {}
            try { if (connection != null) connection.close(); } catch (Exception ignored) {}
        }
    }

    public Team saveTeam(Team team) {
        Connection connection = null;
        PreparedStatement checkStmt = null;
        PreparedStatement insertStmt = null;
        PreparedStatement updateStmt = null;
        PreparedStatement clearStmt = null;
        PreparedStatement attachStmt = null;
        ResultSet rs = null;

        try {
            connection = dbConnection.getDBConnection();
            connection.setAutoCommit(false);

            String checkSql = "SELECT COUNT(*) FROM Team WHERE id = ?";
            String insertSql = "INSERT INTO Team(id, name, continent) VALUES (?, ?, ?::continent_enum)";
            String updateSql = "UPDATE Team SET name = ?, continent = ?::continent_enum WHERE id = ?";
            String clearPlayersSql = "UPDATE Player SET id_team = NULL WHERE id_team = ?";
            String attachPlayerSql = "UPDATE Player SET id_team = ? WHERE id = ?";


            checkStmt = connection.prepareStatement(checkSql);
            checkStmt.setInt(1, team.getId());
            rs = checkStmt.executeQuery();
            rs.next();
            boolean teamExists = rs.getInt(1) > 0;
            rs.close();

            if (!teamExists) {
                insertStmt = connection.prepareStatement(insertSql);
                insertStmt.setInt(1, team.getId());
                insertStmt.setString(2, team.getName());
                insertStmt.setString(3, team.getContinent().name());
                insertStmt.executeUpdate();
            } else {
                updateStmt = connection.prepareStatement(updateSql);
                updateStmt.setString(1, team.getName());
                updateStmt.setString(2, team.getContinent().name());
                updateStmt.setInt(3, team.getId());
                updateStmt.executeUpdate();
            }


            clearStmt = connection.prepareStatement(clearPlayersSql);
            clearStmt.setInt(1, team.getId());
            clearStmt.executeUpdate();

            attachStmt = connection.prepareStatement(attachPlayerSql);
            for (Player player : team.getPlayers()) {
                attachStmt.setInt(1, team.getId());
                attachStmt.setInt(2, player.getId());
                attachStmt.executeUpdate();
            }

            connection.commit();
            return team;

        } catch (SQLException e) {
            try { if (connection != null) connection.rollback(); } catch (Exception ignored) {}
            throw new RuntimeException(e);
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (checkStmt != null) checkStmt.close(); } catch (Exception ignored) {}
            try { if (insertStmt != null) insertStmt.close(); } catch (Exception ignored) {}
            try { if (updateStmt != null) updateStmt.close(); } catch (Exception ignored) {}
            try { if (clearStmt != null) clearStmt.close(); } catch (Exception ignored) {}
            try { if (attachStmt != null) attachStmt.close(); } catch (Exception ignored) {}
            try { if (connection != null) connection.close(); } catch (Exception ignored) {}
        }
    }

    public List<Team> findTeamsByPlayerName(String playerName) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Team> teams = new ArrayList<>();

        try {
            connection = dbConnection.getDBConnection();
            String sql = "SELECT DISTINCT t.id, t.name, t.continent " +
                    "FROM Team t " +
                    "JOIN Player p ON p.id_team = t.id " +
                    "WHERE p.name ILIKE ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, "%" + playerName + "%");
            rs = stmt.executeQuery();

            while (rs.next()) {
                teams.add(new Team(
                        rs.getInt("id"),
                        rs.getString("name"),
                        ContinentEnum.valueOf(rs.getString("continent"))
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (connection != null) connection.close(); } catch (Exception ignored) {}
        }

        return teams;
    }

    public List<Player> findPlayersByCriteria(String playerName, PlayerPositionEnum position,
                                              String teamName, ContinentEnum continent,
                                              int page, int size) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Player> players = new ArrayList<>();
        int offset = (page - 1) * size;

        try {
            connection = dbConnection.getDBConnection();

            StringBuilder sql = new StringBuilder(
                    "SELECT p.id, p.name, p.age, p.position " +
                            "FROM Player p " +
                            "LEFT JOIN Team t ON p.id_team = t.id " +
                            "WHERE 1=1"
            );

            if (playerName != null) sql.append(" AND p.name ILIKE ?");
            if (position != null) sql.append(" AND p.position = ?::position_enum");
            if (teamName != null) sql.append(" AND t.name ILIKE ?");
            if (continent != null) sql.append(" AND t.continent = ?::continent_enum");

            sql.append(" ORDER BY p.id LIMIT ? OFFSET ?");

            stmt = connection.prepareStatement(sql.toString());

            int index = 1;
            if (playerName != null) stmt.setString(index++, "%" + playerName + "%");
            if (position != null) stmt.setString(index++, position.name());
            if (teamName != null) stmt.setString(index++, "%" + teamName + "%");
            if (continent != null) stmt.setString(index++, continent.name());

            stmt.setInt(index++, size);
            stmt.setInt(index, offset);

            rs = stmt.executeQuery();

            while (rs.next()) {
                players.add(new Player(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        PlayerPositionEnum.valueOf(rs.getString("position")),
                        null
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (connection != null) connection.close(); } catch (Exception ignored) {}
        }

        return players;
    }
}