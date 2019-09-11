package org.libertybikes.player.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;

import org.libertybikes.player.service.Player;

@ApplicationScoped
@Priority(5)
public class PlayerDBInMem implements PlayerDB {

    // TODO back this by a DB instead of in-mem
    private final Map<String, Player> allPlayers = new HashMap<>();

    /**
     * Inserts a new player into the database.
     *
     * @return Returns true if the player was created. False if a player with the same ID already existed
     */
    @Override
    public boolean create(Player p) {
        return allPlayers.putIfAbsent(p.id, p) == null;
    }

    @Override
    public void update(Player p) {
        allPlayers.put(p.id, p);
    }

    @Override
    public Player get(String id) {
        return allPlayers.get(id);
    }

    @Override
    public Collection<Player> getAll() {
        return allPlayers.values();
    }

    @Override
    public Collection<Player> topPlayers(int numPlayers) {
        return allPlayers.values()
                        .stream()
                        .sorted(Player::compareOverall)
                        .limit(numPlayers)
                        .collect(Collectors.toList());
    }

    @Override
    public long getRank(String id) {
        Player p = get(id);
        if (p == null)
            return -1;
        int wins = p.stats.numWins;
        long numPlayersAhead = allPlayers.values()
                        .stream()
                        .filter(otherPlayer -> otherPlayer.stats.numWins > wins)
                        .count();
        return numPlayersAhead + 1;
    }

    @Override
    public boolean exists(String id) {
        return allPlayers.containsKey(id);
    }

}
