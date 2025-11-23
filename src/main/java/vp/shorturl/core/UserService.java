package vp.shorturl.core;

import java.util.Optional;
import java.util.UUID;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Создаёт нового пользователя с новым UUID и сохраняет его
    public User createUser() {
        UUID uuid = UUID.randomUUID();
        User user = new User(uuid);
        return userRepository.save(user);
    }

    // Ищет пользователя по UUID.
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }
}
