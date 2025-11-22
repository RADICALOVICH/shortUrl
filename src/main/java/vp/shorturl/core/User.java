package vp.shorturl.core;

import java.util.UUID;

public class User {
    private final UUID uuid;

//    конструктор создания пользователя
    public User(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }
}
