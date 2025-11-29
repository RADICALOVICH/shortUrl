package vp.shorturl.infra;

import org.junit.jupiter.api.Test;
import vp.shorturl.core.User;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryUserRepositoryTest {

    @Test
    public void testSaveAndFindById() {
        var repo = new InMemoryUserRepository();

        var user = new User(UUID.randomUUID());
        repo.save(user);

        var found = repo.findById(user.getUuid());

        assertTrue(found.isPresent());
        assertEquals(user.getUuid(), found.get().getUuid());
    }

    @Test
    public void testFindByIdReturnsEmpty() {
        var repo = new InMemoryUserRepository();

        UUID random = UUID.randomUUID();

        assertTrue(repo.findById(random).isEmpty());
    }
}
