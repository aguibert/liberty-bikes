package org.libertybikes.player.data;

import static com.mongodb.client.model.Filters.eq;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;

import org.bson.Document;
import org.libertybikes.player.service.Player;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.UpdateResult;

@ApplicationScoped
@Alternative
@Priority(10)
public class PlayerDBMongo implements PlayerDB {

    @Inject
    private MongoCollection<Document> mongoAllPlayers;

    /**
     * Inserts a new player into the database.
     *
     * @return Returns true if the player was created. False if a player with the same ID already existed
     */
    @Override
    public boolean create(Player p) {
        mongoAllPlayers.insertOne(p.toDocument());
        return true;
    }

    @Override
    public void update(Player p) {
        UpdateResult result = mongoAllPlayers.replaceOne(eq("id", p.id), p.toDocument());
        if (result.getMatchedCount() != 1)
            throw new NotFoundException("TODO: Person not found");
    }

    @Override
    public Player get(String id) {
        Document doc = mongoAllPlayers.find(eq("id", id)).first();
        return Player.fromDocument(doc);
    }

    @Override
    public Collection<Player> getAll() {
        Set<Player> allPlayers = new HashSet<>();
        for (Document doc : mongoAllPlayers.find())
            allPlayers.add(Player.fromDocument(doc));
        return allPlayers;
    }

    @Override
    public Collection<Player> topPlayers(int numPlayers) {
        Set<Player> allPlayers = new HashSet<>();
        for (Document doc : mongoAllPlayers.find()
                        .sort(Sorts.ascending("id"))
                        .limit(numPlayers)) {
            allPlayers.add(Player.fromDocument(doc));
        }
        return allPlayers;
    }

    @Override
    public long getRank(String id) {
        // TODO
        return 1;
//        Player p = get(id);
//        if (p == null)
//            return -1;
//        int wins = p.stats.numWins;
//        long numPlayersAhead = allPlayers.values()
//                        .stream()
//                        .filter(otherPlayer -> otherPlayer.stats.numWins > wins)
//                        .count();
//        return numPlayersAhead + 1;
    }

    @Override
    public boolean exists(String id) {
        return mongoAllPlayers.countDocuments(eq("id", id)) > 0;
    }

}
