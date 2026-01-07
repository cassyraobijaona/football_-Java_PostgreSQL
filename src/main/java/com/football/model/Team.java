package com.football.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Team {
    private int id;
    private String name;
    private ContinentEnum continent;
    private List<Player> players;

    public Team(int id, String name, ContinentEnum continent) {
        this.id = id;
        this.name = name;
        this.continent = continent;
        this.players = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ContinentEnum getContinent() { return continent; }
    public void setContinent(ContinentEnum continent) { this.continent = continent; }

    public List<Player> getPlayers() { return players; }
    public void setPlayers(List<Player> players) { this.players = players; }


    public void addPlayer(Player player) {
        this.players.add(player);
    }

    public int getPlayersGoals() {
        if (players == null || players.isEmpty()) return 0;

        int totalGoals = 0;
        for (Player player : players) {
            if (player.getGoalNb() == null) {
                throw new RuntimeException(
                        "Impossible de calculer le nombre total de buts : le joueur "
                                + player.getName() + " a un nombre de buts inconnu."
                );
            }
            totalGoals += player.getGoalNb();
        }
        return totalGoals;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return id == team.id &&
                Objects.equals(name, team.name) &&
                continent == team.continent &&
                Objects.equals(players, team.players);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, continent, players);
    }
}
