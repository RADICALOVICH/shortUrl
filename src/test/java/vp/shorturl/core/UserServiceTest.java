package vp.shorturl.core;

import org.junit.jupiter.api.Test;
import vp.shorturl.infra.InMemoryUserRepository;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    @Test
    public void testCreateUser() {
        var repo = new InMemoryUserRepository();
        var service = new UserService(repo);

        var user = service.createUser();

        assertNotNull(user.getUuid());
        assertTrue(repo.findById(user.getUuid()).isPresent());
    }

    @Test
    public void testFindUserById() {
        var repo = new InMemoryUserRepository();
        var service = new UserService(repo);

        var created = service.createUser();
        var found = service.findById(created.getUuid());

        assertTrue(found.isPresent());
        assertEquals(created.getUuid(), found.get().getUuid());
    }

    @Test
    public void testFindByIdReturnsEmpty() {
        var repo = new InMemoryUserRepository();
        var service = new UserService(repo);

        UUID unknown = UUID.randomUUID();

        assertTrue(service.findById(unknown).isEmpty());
    }
}
