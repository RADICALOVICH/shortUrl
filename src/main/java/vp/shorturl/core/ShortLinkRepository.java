package vp.shorturl.core;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShortLinkRepository {

    ShortLink save(ShortLink link);

    Optional<ShortLink> findByShortId(String shortId);

    List<ShortLink> findByOwnerId(UUID ownerId);

    void deleteByShortId(String shortId);
}