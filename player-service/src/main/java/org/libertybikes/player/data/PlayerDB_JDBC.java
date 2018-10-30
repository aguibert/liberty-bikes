package org.libertybikes.player.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;

import org.libertybikes.player.service.Player;

public class PlayerDB_JDBC implements PlayerDB {

    private static final String TABLE_NAME = "PLAYERS";
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_NUM_GAMES = "totalGames";
    private static final String COL_NUM_WINS = "totalWins";
    private static final String COL_RATING = "rating";

    // TODO back this by a DB instead of in-mem
    private final Map<String, Player> allPlayers = new HashMap<>();

    @Resource
    DataSource ds;

    @PostConstruct
    public void createTables() throws SQLException {
        System.out.println("@AGG creating table");
        try (Connection con = ds.getConnection()) {
            String CREATE_TABLE = new StringBuilder("CREATE TABLE ")
                            .append(TABLE_NAME)
                            .append(" (")
                            .append(COL_ID)
                            .append(" varchar(30) unique not null, ")
                            .append(COL_NAME)
                            .append(" varchar(30), ")
                            .append(COL_NUM_GAMES)
                            .append(" int, ")
                            .append(COL_NUM_WINS)
                            .append(" int, ")
                            .append(COL_RATING)
                            .append("int)")
                            .toString();
            con.createStatement().execute(CREATE_TABLE);
        }
    }

    /**
     * Inserts a new player into the database.
     *
     * @return Returns true if the player was created. False if a player with the same ID already existed
     */
    @Override
    public boolean create(Player p) {
        System.out.println("@AGG inside create");
        try (Connection con = ds.getConnection()) {
            String insert = new StringBuilder("INSERT INTO")
                            .append(TABLE_NAME)
                            .append(" VALUES (")
                            .append(p.id)
                            .append(",")
                            .append(p.name)
                            .append(",")
                            .append(p.stats.totalGames)
                            .append(p.stats.numWins)
                            .append(p.stats.rating)
                            .toString();
            System.out.println("@AGG insert: " + insert);
            con.createStatement().executeUpdate(insert);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return allPlayers.putIfAbsent(p.id, p) == null;
    }

    @Override
    public void update(Player p) {
        allPlayers.put(p.id, p);
    }

    @Override
    public Player get(String id) {
        try (Connection con = ds.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery("SELECT * from " + TABLE_NAME + " where " + COL_ID + "=" + id);
            return rs.next() ? getPlayer(rs) : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Collection<Player> getAll() {
        try (Connection con = ds.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery("SELECT * from " + TABLE_NAME);
            List<Player> players = new ArrayList<>();
            while (rs.next())
                players.add(getPlayer(rs));
            return players;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
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

    private Player getPlayer(ResultSet rs) throws SQLException {
        String cID = rs.getString(COL_ID);
        String cNAME = rs.getString(COL_NAME);
        Player p = new Player(cNAME, cID);
        p.stats.numWins = rs.getInt(COL_NUM_WINS);
        p.stats.rating = rs.getInt(COL_RATING);
        p.stats.totalGames = rs.getInt(COL_NUM_GAMES);
        System.out.println("@AGG got " + p);
        return p;
    }

}
