package vp.shorturl.infra;

import vp.shorturl.core.ShortLink;
import vp.shorturl.core.ShortLinkRepository;
import vp.shorturl.core.User;

import java.util.*;

public class InMemoryShortLinkRepository implements ShortLinkRepository {
    private final Map<String, ShortLink> links;

    public InMemoryShortLinkRepository() {
        this.links = new HashMap<>();
    }

    @Override
    public ShortLink save(ShortLink link) {
       links.put(link.getShortId(), link);
       return link;
    }

    @Override
    public Optional<ShortLink> findByShortId(String shortId) {
        return Optional.ofNullable(links.get(shortId));
    }

    @Override
    public List<ShortLink> findByOwnerId(UUID ownerId) {
        List<ShortLink> result = new ArrayList<>();

        for (ShortLink link : links.values()){
            if (link.getOwnerId().equals(ownerId)){
                result.add(link);
            }
        }
        return result;
    }

    @Override
    public void deleteByShortId(String shortId) {
        links.remove(shortId);
    }
}
