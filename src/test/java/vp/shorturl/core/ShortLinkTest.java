package vp.shorturl.core;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ShortLinkTest {

    @Test
    public void testGetUrlSuccess() {
        var link = new ShortLink(
                "https://example.com",
                "abc123",
                UUID.randomUUID(),
                5,
                24
        );

        assertEquals("https://example.com", link.getUrl());
    }

    @Test
    public void testExpiredThrows() {
        var link = new ShortLink(
                "https://example.com",
                "abc123",
                UUID.randomUUID(),
                5,
                0
        );

        assertThrows(IllegalStateException.class, link::getUrl);
    }

    @Test
    public void testLimitExceededThrows() {
        var link = new ShortLink(
                "https://example.com",
                "abc123",
                UUID.randomUUID(),
                1,
                24
        );

        link.getUrl();

        assertThrows(IllegalStateException.class, link::getUrl);
    }

    @Test
    public void testUsageIncrement() {
        var link = new ShortLink(
                "https://example.com",
                "abc123",
                UUID.randomUUID(),
                5,
                24
        );

        link.getUrl();
        link.getUrl();

        assertEquals(2, link.getUsagesCount());
    }

    @Test
    public void testSetMaxUsages() {
        var link = new ShortLink(
                "https://example.com",
                "abc123",
                UUID.randomUUID(),
                3,
                24
        );

        link.setMaxUsages(10);

        assertEquals(10, link.getMaxUsages());
    }

    @Test
    public void testSetExpiredAtInvalid() {
        var link = new ShortLink(
                "https://example.com",
                "abc123",
                UUID.randomUUID(),
                3,
                24
        );

        LocalDateTime past = LocalDateTime.now().minusHours(1);

        assertThrows(IllegalStateException.class, () ->
                link.setExpiredAt(past)
        );
    }

    @Test
    public void testIsExpired() {
        var link = new ShortLink(
                "https://example.com",
                "abc123",
                UUID.randomUUID(),
                3,
                0
        );

        assertTrue(link.isExpired());
    }
}
