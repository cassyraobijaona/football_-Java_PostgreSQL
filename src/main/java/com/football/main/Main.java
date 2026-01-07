package com.football.main;

import com.football.dao.DataRetriever;
import com.football.model.Player;
import com.football.model.PlayerPositionEnum;
import com.football.model.Team;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        DataRetriever dr = new DataRetriever();

        System.out.println("==== Test findTeamById (id=1) ====");
        Team realMadrid = dr.findTeamById(1);
        System.out.println("Équipe : " + realMadrid.getName());
        System.out.println("Joueurs :");
        for (Player p : realMadrid.getPlayers()) {
            System.out.println("- " + p.getName() + " (" + p.getGoalNb() + " buts)");
        }

        try {
            int goals = realMadrid.getPlayersGoals();
            System.out.println("Nombre total de buts : " + goals);
        } catch (RuntimeException e) {
            System.out.println("Exception : " + e.getMessage());
        }


        System.out.println("\n==== Test findTeamById (id=5) ====");
        Team interMiami = dr.findTeamById(5);
        System.out.println("Équipe : " + interMiami.getName());
        System.out.println("Joueurs : " + interMiami.getPlayers().size());
        try {
            int goals = interMiami.getPlayersGoals();
            System.out.println("Nombre total de buts : " + goals);
        } catch (RuntimeException e) {
            System.out.println("Exception : " + e.getMessage());
        }

        System.out.println("\n==== Test findPlayers (page=1, size=2) ====");
        List<Player> playersPage1 = dr.findPlayers(1, 2);
        for (Player p : playersPage1) {
            System.out.println("- " + p.getName() + " (" + p.getGoalNb() + " buts)");
        }

        System.out.println("\n==== Test findPlayers (page=3, size=5) ====");
        List<Player> playersPage3 = dr.findPlayers(3, 5);
        System.out.println("Nombre de joueurs récupérés : " + playersPage3.size());


        System.out.println("\n==== Test findTeamsByPlayerName(\"an\") ====");
        List<Team> teamsWithAn = dr.findTeamsByPlayerName("an");
        for (Team t : teamsWithAn) {
            System.out.println("- " + t.getName());
        }


        System.out.println("\n==== Test findPlayersByCriteria ====");
        List<Player> filteredPlayers = dr.findPlayersByCriteria(
                "ud", PlayerPositionEnum.MIDF, "Madrid", null, 1, 10
        );
        for (Player p : filteredPlayers) {
            System.out.println("- " + p.getName() + " (" + p.getGoalNb() + " buts)");
        }


        System.out.println("\n==== Test createPlayers avec conflit ====");
        List<Player> newPlayersConflict = new ArrayList<>();
        newPlayersConflict.add(new Player(6, "Jude Bellingham", 23, PlayerPositionEnum.STR, null, 5));
        newPlayersConflict.add(new Player(7, "Pedri", 24, PlayerPositionEnum.MIDF, null, null));
        try {
            dr.createPlayers(newPlayersConflict);
        } catch (RuntimeException e) {
            System.out.println("Exception attendue : " + e.getMessage());
        }


        System.out.println("\n==== Test createPlayers normal ====");
        List<Player> newPlayers = new ArrayList<>();
        newPlayers.add(new Player(8, "Vini", 25, PlayerPositionEnum.STR, null, 3));
        newPlayers.add(new Player(9, "Pedri", 24, PlayerPositionEnum.MIDF, null, 2));
        List<Player> createdPlayers = dr.createPlayers(newPlayers);
        System.out.println("Joueurs créés :");
        for (Player p : createdPlayers) {
            System.out.println("- " + p.getName() + " (" + p.getGoalNb() + " buts)");
        }


        System.out.println("\n==== Test saveTeam (ajout joueur à Real Madrid) ====");
        Player extraPlayer = new Player(10, "Nouvel Attaquant", 22, PlayerPositionEnum.STR, null, 1);
        realMadrid.addPlayer(extraPlayer);
        dr.saveTeam(realMadrid);

        Team updatedMadrid = dr.findTeamById(1);
        System.out.println("Joueurs après ajout :");
        for (Player p : updatedMadrid.getPlayers()) {
            System.out.println("- " + p.getName() + " (" + p.getGoalNb() + " buts)");
        }
        try {
            int updatedGoals = updatedMadrid.getPlayersGoals();
            System.out.println("Nombre total de buts : " + updatedGoals);
        } catch (RuntimeException e) {
            System.out.println("Exception : " + e.getMessage());
        }


        System.out.println("\n==== Test saveTeam (vider FC Barcelone) ====");
        Team barca = dr.findTeamById(2);
        barca.setPlayers(new ArrayList<>());
        dr.saveTeam(barca);

        Team emptyBarca = dr.findTeamById(2);
        System.out.println("Joueurs FC Barcelone : " + emptyBarca.getPlayers().size());
    }
}
