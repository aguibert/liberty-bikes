package org.libertybikes.player.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/")
@ApplicationScoped
public class PlayerService {

    // TODO: persist this into a DB
    Map<String, Player> allPlayers = new HashMap<>();

    @PostConstruct
    public void initPlayers() {
        // TODO temp way to initialize some players
        for (int i = 0; i < 10; i++)
            createPlayer("Bob Sampleton");
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Player> getPlayers() {
        return allPlayers.values();
    }

    @GET
    @Path("/create")
    public String createPlayer(@QueryParam("playerName") String name) {
        System.out.println("Creating player with name: " + name);
        Player p = new Player(name);
        allPlayers.put(p.id, p);
        return p.id;
    }

    @GET
    @Path("/{playerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Player getPlayer(@PathParam("playerId") String id) {
        return allPlayers.get(id);
    }
}
