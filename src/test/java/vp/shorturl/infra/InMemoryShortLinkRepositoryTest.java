package vp.shorturl.infra;

import org.junit.jupiter.api.Test;
import vp.shorturl.core.ShortLink;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryShortLinkRepositoryTest {

    @Test
    public void testSaveAndFindByShortId() {
        var repo = new InMemoryShortLinkRepository();

        var link = new ShortLink(
                "https://example.com",
                "abc123",
                UUID.randomUUID(),
                5,
                24
        );

        repo.save(link);

        var found = repo.findByShortId("abc123");

        assertTrue(found.isPresent());
        assertEquals("https://example.com", found.get().getUrl());
    }

    @Test
    public void testDeleteByShortId() {
        var repo = new InMemoryShortLinkRepository();

        var link = new ShortLink(
                "https://example.com",
                "x1y2z3",
                UUID.randomUUID(),
                5,
                24
        );

        repo.save(link);
        repo.deleteByShortId("x1y2z3");

        assertTrue(repo.findByShortId("x1y2z3").isEmpty());
    }

    @Test
    public void testFindByOwnerId() {
        var repo = new InMemoryShortLinkRepository();

        UUID u1 = UUID.randomUUID();
        UUID u2 = UUID.randomUUID();

        repo.save(new ShortLink("https://a.com", "a1", u1, 5, 24));
        repo.save(new ShortLink("https://b.com", "b1", u1, 5, 24));
        repo.save(new ShortLink("https://c.com", "c1", u2, 5, 24));

        var list = repo.findByOwnerId(u1);

        assertEquals(2, list.size());
    }

    @Test
    public void testFindAll() {
        var repo = new InMemoryShortLinkRepository();

        repo.save(new ShortLink("https://a.com", "1", UUID.randomUUID(), 5, 24));
        repo.save(new ShortLink("https://b.com", "2", UUID.randomUUID(), 5, 24));

        var all = repo.findAll();

        assertEquals(2, all.size());
    }
}
