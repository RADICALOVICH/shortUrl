package vp.shorturl.core;

import org.junit.jupiter.api.Test;
import vp.shorturl.infra.InMemoryShortLinkRepository;
import vp.shorturl.infra.InMemoryUserRepository;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTest {

    @Test
    public void fullScenarioTest() {
        var userRepo = new InMemoryUserRepository();
        var linkRepo = new InMemoryShortLinkRepository();

        var userService = new UserService(userRepo);
        var shortLinkService = new ShortLinkService(linkRepo);

        var user = userService.createUser();
        assertNotNull(user);
        assertNotNull(user.getUuid());

        var link = shortLinkService.createShortLink(
                user.getUuid(),
                "https://example.org",
                2,
                24
        );

        assertEquals("https://example.org", shortLinkService.openShortLink(link.getShortId()));
        assertEquals("https://example.org", shortLinkService.openShortLink(link.getShortId()));
        assertThrows(IllegalStateException.class, () ->
                shortLinkService.openShortLink(link.getShortId())
        );

        UUID attacker = UUID.randomUUID();

        assertThrows(IllegalStateException.class, () ->
                shortLinkService.updateMaxUsages(attacker, link.getShortId(), 100)
        );

        assertThrows(IllegalStateException.class, () ->
                shortLinkService.updateExpiration(attacker, link.getShortId(), java.time.LocalDateTime.now().plusHours(10))
        );

        assertThrows(IllegalStateException.class, () ->
                shortLinkService.deleteShortLink(attacker, link.getShortId())
        );

        var expiredLink = shortLinkService.createShortLink(
                user.getUuid(),
                "https://expired.com",
                5,
                0
        );

        assertThrows(IllegalStateException.class, () ->
                shortLinkService.openShortLink(expiredLink.getShortId())
        );

        shortLinkService.removeExpiredLinks();
        assertTrue(linkRepo.findByShortId(expiredLink.getShortId()).isEmpty());
    }

    @Test
    public void multiUserScenario() {
        var userRepo = new InMemoryUserRepository();
        var linkRepo = new InMemoryShortLinkRepository();

        var userService = new UserService(userRepo);
        var shortLinkService = new ShortLinkService(linkRepo);

        var userA = userService.createUser();
        var userB = userService.createUser();

        var linkA1 = shortLinkService.createShortLink(
                userA.getUuid(),
                "https://siteA.com",
                5,
                24
        );

        var linkA2 = shortLinkService.createShortLink(
                userA.getUuid(),
                "https://siteA2.com",
                5,
                24
        );

        var linkB1 = shortLinkService.createShortLink(
                userB.getUuid(),
                "https://siteB.com",
                5,
                24
        );

        assertNotEquals(linkA1.getShortId(), linkB1.getShortId());
        assertNotEquals(linkA2.getShortId(), linkB1.getShortId());

        assertEquals("https://siteA.com", shortLinkService.openShortLink(linkA1.getShortId()));
        assertEquals("https://siteB.com", shortLinkService.openShortLink(linkB1.getShortId()));

        assertThrows(IllegalStateException.class, () ->
                shortLinkService.deleteShortLink(userB.getUuid(), linkA1.getShortId())
        );

        shortLinkService.deleteShortLink(userA.getUuid(), linkA1.getShortId());

        assertTrue(linkRepo.findByShortId(linkA1.getShortId()).isEmpty());

        var expiredLinkB = shortLinkService.createShortLink(
                userB.getUuid(),
                "https://expiredB.com",
                5,
                0
        );

        assertThrows(IllegalStateException.class, () ->
                shortLinkService.openShortLink(expiredLinkB.getShortId())
        );

        shortLinkService.removeExpiredLinks();

        assertTrue(linkRepo.findByShortId(expiredLinkB.getShortId()).isEmpty());

        var linksA = shortLinkService.getLinksByOwner(userA.getUuid());
        var linksB = shortLinkService.getLinksByOwner(userB.getUuid());

        assertEquals(1, linksA.size());
        assertEquals(1, linksB.size());
    }
}
