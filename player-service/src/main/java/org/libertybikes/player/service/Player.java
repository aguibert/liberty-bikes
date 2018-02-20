package org.libertybikes.player.service;

import java.util.UUID;

import javax.json.bind.annotation.JsonbCreator;

public class Player {

    public final String id;

    public String name;

    public Player(String name) {
        this(name, UUID.randomUUID().toString());
    }

    @JsonbCreator
    public Player(String name, String id) {
        this.name = name;
        this.id = id;
    }

}
