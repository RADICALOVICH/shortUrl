package vp.shorturl.core;

import org.junit.jupiter.api.Test;
import vp.shorturl.infra.InMemoryShortLinkRepository;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ShortLinkServiceTest {

    // --- 1. ShortId должен быть уникальным ---
    @Test
    public void testUniqueShortId() {
        var repo = new InMemoryShortLinkRepository();
        var service = new ShortLinkService(repo);

        UUID user = UUID.randomUUID();

        var l1 = service.createShortLink(user, "https://example.com", 5, 24);
        var l2 = service.createShortLink(user, "https://example.com", 5, 24);

        assertNotEquals(l1.getShortId(), l2.getShortId(),
                "Short IDs should be unique");
    }

    // --- 2. openShortLink должен увеличивать usagesCount ---
    @Test
    public void testOpenIncrementsUsage() {
        var repo = new InMemoryShortLinkRepository();
        var service = new ShortLinkService(repo);

        UUID user = UUID.randomUUID();
        var link = service.createShortLink(user, "https://ya.ru", 5, 24);

        service.openShortLink(link.getShortId());
        service.openShortLink(link.getShortId());

        var updated = repo.findByShortId(link.getShortId()).get();
        assertEquals(2, updated.getUsagesCount(),
                "Usage counter must increase on each open");
    }

    // --- 3. Превышение лимита должно блокировать открытие ---
    @Test
    public void testUsageLimitBlocksLink() {
        var repo = new InMemoryShortLinkRepository();
        var service = new ShortLinkService(repo);

        UUID user = UUID.randomUUID();
        var link = service.createShortLink(user, "https://ya.ru", 2, 24);

        service.openShortLink(link.getShortId());
        service.openShortLink(link.getShortId());

        assertThrows(IllegalStateException.class, () ->
                        service.openShortLink(link.getShortId()),
                "Should throw when usage limit reached"
        );
    }

    // --- 4. Открытие после TTL должно бросать исключение ---
    @Test
    public void testTTLExpirationBlocksLink() {
        var repo = new InMemoryShortLinkRepository();
        var service = new ShortLinkService(repo);

        UUID user = UUID.randomUUID();
        var link = service.createShortLink(user, "https://ya.ru", 5, 0); // TTL = 0

        assertThrows(IllegalStateException.class, () ->
                        service.openShortLink(link.getShortId()),
                "Expired link must not be opened"
        );
    }

    // --- 5. Автоудаление истекших ссылок ---
    @Test
    public void testAutoDeleteExpired() {
        var repo = new InMemoryShortLinkRepository();
        var service = new ShortLinkService(repo);

        UUID user = UUID.randomUUID();
        var link = service.createShortLink(user, "https://ya.ru", 5, 0); // TTL = 0

        service.removeExpiredLinks();

        assertTrue(repo.findByShortId(link.getShortId()).isEmpty(),
                "Expired link must be removed automatically");
    }

    // --- 6. Только владелец может менять maxUsages ---
    @Test
    public void testUpdateMaxUsagesOwnerOnly() {
        var repo = new InMemoryShortLinkRepository();
        var service = new ShortLinkService(repo);

        UUID owner = UUID.randomUUID();
        UUID hacker = UUID.randomUUID();

        var link = service.createShortLink(owner, "https://ya.ru", 5, 24);

        assertThrows(IllegalStateException.class, () ->
                        service.updateMaxUsages(hacker, link.getShortId(), 50),
                "Non-owner must not update limit"
        );
    }

    // --- 7. Владелец успешно меняет лимит ---
    @Test
    public void testUpdateMaxUsagesSuccess() {
        var repo = new InMemoryShortLinkRepository();
        var service = new ShortLinkService(repo);

        UUID owner = UUID.randomUUID();
        var link = service.createShortLink(owner, "https://ya.ru", 5, 24);

        service.updateMaxUsages(owner, link.getShortId(), 10);

        assertEquals(10,
                repo.findByShortId(link.getShortId()).get().getMaxUsages(),
                "Owner must successfully update max usages");
    }

    // --- 8. Владелец может менять TTL ---
    @Test
    public void testUpdateTTLSuccess() {
        var repo = new InMemoryShortLinkRepository();
        var service = new ShortLinkService(repo);

        UUID owner = UUID.randomUUID();
        var link = service.createShortLink(owner, "https://ya.ru", 5, 24);

        LocalDateTime newTime = LocalDateTime.now().plusHours(10);

        service.updateExpiration(owner, link.getShortId(), newTime);

        assertEquals(newTime,
                repo.findByShortId(link.getShortId()).get().getExpiredAt(),
                "Owner must successfully update TTL");
    }

    // --- 9. Посторонний не может менять TTL ---
    @Test
    public void testUpdateTTLNotOwner() {
        var repo = new InMemoryShortLinkRepository();
        var service = new ShortLinkService(repo);

        UUID owner = UUID.randomUUID();
        UUID outsider = UUID.randomUUID();

        var link = service.createShortLink(owner, "https://ya.ru", 5, 24);

        assertThrows(IllegalStateException.class, () ->
                        service.updateExpiration(outsider, link.getShortId(), LocalDateTime.now().plusHours(5)),
                "Non-owner must not update TTL"
        );
    }

    // --- 10. Удаление: только владелец ---
    @Test
    public void testDeleteOnlyOwner() {
        var repo = new InMemoryShortLinkRepository();
        var service = new ShortLinkService(repo);

        UUID owner = UUID.randomUUID();
        UUID outsider = UUID.randomUUID();

        var link = service.createShortLink(owner, "https://ya.ru", 5, 24);

        assertThrows(IllegalStateException.class, () ->
                        service.deleteShortLink(outsider, link.getShortId()),
                "Non-owner must not delete link"
        );
    }

    // --- 11. Удаление владельцем ---
    @Test
    public void testDeleteSuccess() {
        var repo = new InMemoryShortLinkRepository();
        var service = new ShortLinkService(repo);

        UUID owner = UUID.randomUUID();
        var link = service.createShortLink(owner, "https://ya.ru", 5, 24);

        service.deleteShortLink(owner, link.getShortId());

        assertTrue(repo.findByShortId(link.getShortId()).isEmpty(),
                "Owner must successfully delete link");
    }
}
