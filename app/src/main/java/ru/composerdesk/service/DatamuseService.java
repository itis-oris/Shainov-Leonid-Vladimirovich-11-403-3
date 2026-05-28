package ru.composerdesk.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.composerdesk.client.DatamuseClient;

import java.util.List;

@Service
public class DatamuseService {

    private static final Logger log = LoggerFactory.getLogger(DatamuseService.class);
    private final DatamuseClient client;

    public DatamuseService(DatamuseClient client) {
        this.client = client;
    }

    @Cacheable(value = "rhymes", key = "#word")
    public List<String> getRhymes(String word) {
        log.debug("Fetching rhymes for: {}", word);
        return client.getRhymes(word);
    }
}