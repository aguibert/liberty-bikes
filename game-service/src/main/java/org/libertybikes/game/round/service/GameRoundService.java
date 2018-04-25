package org.libertybikes.game.round.service;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.libertybikes.game.core.GameRound;
import org.libertybikes.game.core.GameRound.State;

@Path("/round")
@ApplicationScoped
public class GameRoundService {

    @Resource
    ManagedScheduledExecutorService exec;

    private final Map<String, GameRound> allRounds = new ConcurrentHashMap<>();

    @PostConstruct
    public void initService() {
        trustAllCertificates();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<GameRound> listAllGames() {
        return allRounds.values();
    }

    @POST
    @Path("/create")
    public String createRound() {
        GameRound p = new GameRound();
        allRounds.put(p.id, p);
        System.out.println("Created round id=" + p.id);
        if (allRounds.size() > 5)
            System.out.println("WARNING: Found " + allRounds.size() + " active games in GameRoundService. " +
                               "They are probably not being cleaned up properly: " + allRounds.keySet());
        return p.id;
    }

    @POST
    @Path("/create")
    public GameRound createRoundById(@QueryParam("gameId") String gameId) {
        GameRound newRound = new GameRound(gameId);
        GameRound existingRound = allRounds.putIfAbsent(gameId, newRound);
        GameRound round = existingRound == null ? newRound : existingRound;
        System.out.println("Created round id=" + round.id);
        if (allRounds.size() > 5)
            System.out.println("WARNING: Found " + allRounds.size() + " active games in GameRoundService. " +
                               "They are probably not being cleaned up properly: " + allRounds.keySet());
        return round;
    }

    @GET
    @Path("/available")
    public String getAvailableRound() {
        Optional<GameRound> availableRound = allRounds.values()
                        .stream()
                        .filter(r -> r.gameState == GameRound.State.OPEN)
                        .findFirst();
        if (availableRound.isPresent())
            return availableRound.get().id;
        else
            return createRound();
    }

    @GET
    @Path("/{roundId}")
    @Produces(MediaType.APPLICATION_JSON)
    public GameRound getRound(@PathParam("roundId") String roundId) {
        return allRounds.get(roundId);
    }

    public GameRound requeue(GameRound oldRound, boolean isPlayer) {
        // Do not allow anyone to skip ahead past a round that has not started yet
        if (!oldRound.isStarted())
            return null;

        GameRound nextRound = createRoundById(oldRound.nextRoundId);

        // If player tries to requeue and next game is already in progress, requeue ahead to the next game
        if (isPlayer && nextRound.isStarted())
            return requeue(nextRound, isPlayer);
        // If next round is already done, requeue ahead to next game
        else if (nextRound.gameState == GameRound.State.FINISHED)
            return requeue(nextRound, isPlayer);
        else
            return nextRound;
    }

    public void deleteRound(GameRound round) {
        String roundId = round.id;
        if (round.gameState == State.OPEN)
            round.gameState = State.FINISHED;
        System.out.println("Scheduling round id=" + roundId + " for deletion in 5 minutes");
        // Do not immediately delete rounds in order to give players/spectators time to move along to the next game
        exec.schedule(() -> {
            allRounds.remove(roundId);
            System.out.println("Deleted round id=" + roundId);
        }, 5, TimeUnit.MINUTES);
    }

    // TODO: For now we will trust all self-signed certificates
    private static void trustAllCertificates() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {}

            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {}
        } };

        try {
            System.out.println("@AGG default SSLContext is: " + SSLContext.getDefault().getProtocol());
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            System.out.println("Unable to install custom SSL socket factory to trust self-signed certificates");
            e.printStackTrace();
        }
        System.out.println("Installed no-op TrustManager");
    }

}
