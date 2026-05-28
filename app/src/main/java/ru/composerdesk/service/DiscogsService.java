package ru.composerdesk.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.composerdesk.client.DiscogsClient;

@Service
public class DiscogsService {

    private static final Logger log = LoggerFactory.getLogger(DiscogsService.class);
    private final DiscogsClient client;

    public DiscogsService(DiscogsClient client) {
        this.client = client;
    }

    @Cacheable(value = "trackTitle", key = "#title")
    public int checkTrackTitle(String title) {
        log.debug("Checking track title: {}", title);
        return client.countExactRelease(title);
    }

    @Cacheable(value = "releaseTitle", key = "#title")
    public int checkReleaseTitle(String title) {
        log.debug("Checking release title: {}", title);
        return client.countExactRelease(title);
    }

    @Cacheable(value = "artistName", key = "#name")
    public int checkArtistName(String name) {
        log.debug("Checking artist name: {}", name);
        return client.countExactArtist(name);
    }
}