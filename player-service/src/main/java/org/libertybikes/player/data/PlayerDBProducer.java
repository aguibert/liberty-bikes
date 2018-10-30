package org.libertybikes.player.data;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.CDI;
import javax.naming.InitialContext;

@ApplicationScoped
public class PlayerDBProducer {

    @Produces
    public PlayerDB createPlayerDB() {
        try {
            InitialContext.doLookup("java:comp/DefaultDataSource");
            System.out.println("JDBC appears to be configured.");
            return CDI.current().select(PlayerDB_JDBC.class).get();
        } catch (Exception e) {
            e.printStackTrace();
            return new PlayerDB_HashMap();
        }
    }

}
