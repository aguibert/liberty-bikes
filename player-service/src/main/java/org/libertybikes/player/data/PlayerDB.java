package org.libertybikes.player.data;

import java.util.Collection;

import org.libertybikes.player.service.Player;

public interface PlayerDB {

    public boolean create(Player p);

    public void update(Player p);

    public Player get(String id);

    public Collection<Player> getAll();

    public Collection<Player> topPlayers(int numPlayers);

    public long getRank(String id);

    public boolean exists(String id);

}
