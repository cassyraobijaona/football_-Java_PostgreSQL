package com.football.dao;

import com.football.bd.DBConnection;
import com.football.model.ContinentEnum;
import com.football.model.Player;
import com.football.model.PlayerPositionEnum;
import com.football.model.Team;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataRetrieverTest {

    private final DataRetriever dataRetriever;

    public DataRetrieverTest() {
        dataRetriever = new DataRetriever();
    }

    private void resetDatabaseToInitialState() {
        DBConnection db = new DBConnection();
        try (Connection conn = db.getDBConnection()) {
            conn.setAutoCommit(false);

            String deletePlayers = "DELETE FROM Player";
            try (PreparedStatement stmt = conn.prepareStatement(deletePlayers)) {
                stmt.executeUpdate();
            }

            String deleteTeams = "DELETE FROM Team";
            try (PreparedStatement stmt = conn.prepareStatement(deleteTeams)) {
                stmt.executeUpdate();
            }

            String insertTeams = """
                INSERT INTO Team (id, name, continent) VALUES
                (1, 'Real Madrid CF', 'EUROPA'),
                (2, 'FC Barcelona', 'EUROPA'),
                (3, 'Atlético de Madrid', 'EUROPA'),
                (4, 'Al Ahly SC', 'AFRICA'),
                (5, 'Inter Miami CF', 'AMERICA')
                """;
            try (PreparedStatement stmt = conn.prepareStatement(insertTeams)) {
                stmt.executeUpdate();
            }

            String resetTeamSeq = """
                SELECT setval(pg_get_serial_sequence('Team', 'id'), (SELECT MAX(id) FROM Team))
                """;
            try (PreparedStatement stmt = conn.prepareStatement(resetTeamSeq)) {
                stmt.executeQuery();
            }

            String insertPlayers = """
                INSERT INTO Player (id, name, age, position, id_team) VALUES
                (1, 'Thibaut Courtois', 32, 'GK', 1),
                (2, 'Dani Carvajal', 33, 'DEF', 1),
                (3, 'Jude Bellingham', 21, 'MIDF', 1),
                (4, 'Robert Lewandowski', 36, 'STR', 2),
                (5, 'Antoine Griezmann', 33, 'STR', 3)
                """;
            try (PreparedStatement stmt = conn.prepareStatement(insertPlayers)) {
                stmt.executeUpdate();
            }

            String resetPlayerSeq = """
                SELECT setval(pg_get_serial_sequence('Player', 'id'), (SELECT MAX(id) FROM Player))
                """;
            try (PreparedStatement stmt = conn.prepareStatement(resetPlayerSeq)) {
                stmt.executeQuery();
            }

            conn.commit();
        } catch (SQLException e) {
            System.err.println("Erreur lors du reset complet de la base : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void runAllTests() {
        resetDatabaseToInitialState();
        testFindTeamById_existingTeamWithPlayers();

        resetDatabaseToInitialState();
        testFindTeamById_existingTeamNoPlayers();

        resetDatabaseToInitialState();
        testFindPlayersPagination();

        resetDatabaseToInitialState();
        testFindTeamsByPlayerName();

        resetDatabaseToInitialState();
        testFindPlayersByCriteria();

        resetDatabaseToInitialState();
        testCreatePlayers_existingPlayerException();

        resetDatabaseToInitialState();
        testCreatePlayers_newPlayers();

        resetDatabaseToInitialState();
        testSaveTeam_addPlayer();

        resetDatabaseToInitialState();
        testSaveTeam_removeAllPlayers();
    }

    public void testFindTeamById_existingTeamWithPlayers() {
        System.out.println("===== TEST findTeamById (id=1) =====");
        Team team = dataRetriever.findTeamById(1);
        if (team != null) {
            System.out.println("Team trouvé : " + team.getName());
            System.out.println("Joueurs :");
            for (Player p : team.getPlayers()) {
                System.out.println("- " + p.getName() + " | âge : " + p.getAge() + " | poste : " + p.getPosition());
            }
        } else {
            System.out.println("Team non trouvé");
        }
        System.out.println();
    }

    public void testFindTeamById_existingTeamNoPlayers() {
        System.out.println("===== TEST findTeamById (id=5) =====");
        Team team = dataRetriever.findTeamById(5);
        if (team != null) {
            System.out.println("Team trouvé : " + team.getName());
            System.out.println("Joueurs : " + team.getPlayers().size());
        } else {
            System.out.println("Team non trouvé");
        }
        System.out.println();
    }

    public void testFindPlayersPagination() {
        System.out.println("===== TEST findPlayers Pagination =====");
        List<Player> playersPage1 = dataRetriever.findPlayers(1, 2);
        System.out.println("Page 1, size 2 :");
        for (Player p : playersPage1) {
            System.out.println("- " + p.getName());
        }

        List<Player> playersPage3 = dataRetriever.findPlayers(3, 5);
        System.out.println("Page 3, size 5 : " + playersPage3.size() + " joueurs");
        System.out.println();
    }

    public void testFindTeamsByPlayerName() {
        System.out.println("===== TEST findTeamsByPlayerName =====");
        List<Team> teams = dataRetriever.findTeamsByPlayerName("an");
        for (Team t : teams) {
            System.out.println("- " + t.getName());
        }
        System.out.println();
    }

    public void testFindPlayersByCriteria() {
        System.out.println("===== TEST findPlayersByCriteria =====");
        List<Player> players = dataRetriever.findPlayersByCriteria(
                "ud", PlayerPositionEnum.MIDF, "Madrid", ContinentEnum.EUROPA, 1, 10
        );
        for (Player p : players) {
            System.out.println("- " + p.getName());
        }
        System.out.println();
    }

    public void testCreatePlayers_existingPlayerException() {
        System.out.println("===== TEST createPlayers (existants) =====");
        List<Player> players = new ArrayList<>();
        players.add(new Player(1, "Thibaut Courtois", 32, PlayerPositionEnum.GK, null));
        players.add(new Player(3, "Jude Bellingham", 21, PlayerPositionEnum.MIDF, null));

        try {
            dataRetriever.createPlayers(players);
            System.out.println("ERREUR : Aucune exception levée alors qu'attendue !");
        } catch (RuntimeException e) {
            System.out.println("Erreur attendue : " + e.getMessage());
        }
        System.out.println();
    }
    public void testCreatePlayers_newPlayers() {
        System.out.println("===== TEST createPlayers (nouveaux) =====");
        List<Player> players = new ArrayList<>();
        players.add(new Player(6, "Vini", 25, PlayerPositionEnum.STR, null));
        players.add(new Player(7, "Pedri", 24, PlayerPositionEnum.MIDF, null));

        List<Player> created = dataRetriever.createPlayers(players);
        for (Player p : created) {
            System.out.println("- " + p.getName());
        }
        System.out.println();
    }

    public void testSaveTeam_addPlayer() {
        System.out.println("===== TEST saveTeam (ajout joueur) =====");
        Team team = dataRetriever.findTeamById(1);
        Player newPlayer = new Player(6, "Vini", 25, PlayerPositionEnum.STR, null);
        team.getPlayers().add(newPlayer);

        Team updated = dataRetriever.saveTeam(team);
        System.out.println("Joueurs après ajout : " + updated.getPlayers().size());
        for (Player p : updated.getPlayers()) {
            System.out.println("- " + p.getName());
        }
        System.out.println();
    }

    public void testSaveTeam_removeAllPlayers() {
        System.out.println("===== TEST saveTeam (supprimer tous les joueurs) =====");
        Team team = dataRetriever.findTeamById(2);
        team.getPlayers().clear();

        Team updated = dataRetriever.saveTeam(team);
        System.out.println("Joueurs après suppression : " + updated.getPlayers().size());
        System.out.println();
    }

    public static void main(String[] args) {
        DataRetrieverTest test = new DataRetrieverTest();
        test.runAllTests();
    }
}