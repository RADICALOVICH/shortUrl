package vp.shorturl.core;

import java.time.LocalDateTime;
import java.util.UUID;

public class ShortLink {
    private final String url;
    private final String shortId;
    private final UUID ownerId;
    private int maxUsages;
    private int usagesCount;
    private final LocalDateTime createdAt;
    private LocalDateTime expiredAt;

    public ShortLink(String url, String shortId, UUID ownerId, int maxUsages, int expirationHours) {
        this.url = url;
        this.shortId = shortId;
        this.ownerId = ownerId;
        this.maxUsages = maxUsages;
        this.createdAt = LocalDateTime.now();
        this.expiredAt = this.createdAt.plusHours(expirationHours);
        this.usagesCount = 0;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getShortId() {
        return shortId;
    }

    public String getUrl() {
        if (LocalDateTime.now().isAfter(expiredAt)) {
            throw new IllegalStateException("Link expired");
        }
        if (this.usagesCount >= this.maxUsages) {
            throw new IllegalStateException("Usage limit reached");
        }
        usagesCount++;
        return url;
    }

    public void setMaxUsages(int maxUsages) {
        this.maxUsages = maxUsages;
    }

    public void setExpiredAt(LocalDateTime expiredAt) {
        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Expire date must be greater than now");
        }
        this.expiredAt = expiredAt;
    }

    public boolean isExpired(){
        return this.expiredAt.isBefore(LocalDateTime.now());
    }
}
