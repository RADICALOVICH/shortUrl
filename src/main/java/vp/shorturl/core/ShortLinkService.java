package vp.shorturl.core;

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
        ShortLink link = new ShortLink(url, "hello", ownerId, maxUsages, expirationHours);
        return shortLinkRepository.save(link);
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
