package vp.shorturl.infra;


import vp.shorturl.core.User;
import vp.shorturl.core.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryUserRepository implements UserRepository {
    private final Map<UUID, User> users;

    public InMemoryUserRepository() {
        this.users = new HashMap<>();
    }

    @Override
    public User save(User user) {
        users.put(user.getUuid(),  user);
        return user;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(users.get(id));
    }
}
