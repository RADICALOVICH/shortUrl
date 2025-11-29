package vp.shorturl.core;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ShortLinkService {
    private final ShortLinkRepository shortLinkRepository;

    public ShortLinkService(ShortLinkRepository shortLinkRepository) {
        this.shortLinkRepository = shortLinkRepository;
    }

    public ShortLink createShortLink(
            UUID ownerId,
            String url,
            int maxUsages,
            int expirationHours
    ) {
        String shortId = generateUniqueShortId();
        ShortLink link = new ShortLink(url, shortId, ownerId, maxUsages, expirationHours);
        return shortLinkRepository.save(link);
    }

    public String openShortLink(String shortId){
        ShortLink shortLink = shortLinkRepository.findByShortId(shortId)
                .orElseThrow(() -> new IllegalStateException("Short link not found"));
        return shortLink.getUrl();
    }

    // Редактирование лимита владельцем
    public void updateMaxUsages(UUID ownerId, String shortId, int newMaxUsages) {
        ShortLink link = shortLinkRepository.findByShortId(shortId)
                .orElseThrow(() -> new IllegalStateException("Short link not found"));

        if (!link.getOwnerId().equals(ownerId)) {
            throw new IllegalStateException("You are not the owner of this link");
        }

        link.setMaxUsages(newMaxUsages);
        shortLinkRepository.save(link);
    }

    public void updateExpiration(UUID ownerId, String shortId, LocalDateTime newExpiredAt) {
        ShortLink link = shortLinkRepository.findByShortId(shortId)
                .orElseThrow(() -> new IllegalStateException("Short link not found"));

        if (!link.getOwnerId().equals(ownerId)) {
            throw new IllegalStateException("You are not the owner of this link");
        }

        link.setExpiredAt(newExpiredAt);
        shortLinkRepository.save(link);
    }

    public void deleteShortLink(UUID ownerId, String shortId) {
        ShortLink link = shortLinkRepository.findByShortId(shortId)
                .orElseThrow(() -> new IllegalStateException("Short link not found"));

        if (!link.getOwnerId().equals(ownerId)) {
            throw new IllegalStateException("You are not the owner of this link");
        }

        shortLinkRepository.deleteByShortId(shortId);
    }

    public List<ShortLink> getLinksByOwner(UUID ownerId) {
        return shortLinkRepository.findByOwnerId(ownerId);
    }

    public void removeExpiredLinks() {
        List<ShortLink> allLinks = shortLinkRepository.findAll();

        for (ShortLink link : allLinks) {
            if (link.isExpired()) {
                System.out.println("[INFO] Auto-delete: link " + link.getShortId() +
                        " was removed because TTL expired.");
                shortLinkRepository.deleteByShortId(link.getShortId());
            }
        }
    }


    private String generateUniqueShortId() {
        String id;
        do {
            id = generateRandomShortId();
        } while (shortLinkRepository.findByShortId(id).isPresent());
        return id;
    }

    private String generateRandomShortId() {
        String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        int length = 8;

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = ThreadLocalRandom.current().nextInt(alphabet.length());
            sb.append(alphabet.charAt(index));
        }
        return sb.toString();
    }

}
