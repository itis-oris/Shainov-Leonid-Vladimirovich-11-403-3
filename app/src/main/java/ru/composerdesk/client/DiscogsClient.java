package ru.composerdesk.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Component
public class DiscogsClient {
    private static final Logger log = LoggerFactory.getLogger(DiscogsClient.class);

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    private long lastRequestTime = 0;

    private synchronized void respectRateLimit() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRequestTime;
        if (elapsed < 3000) {
            try {
                Thread.sleep(3000 - elapsed);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        lastRequestTime = System.currentTimeMillis();
    }

    public int countExactArtist(String artistName) {
        try {
            respectRateLimit();

            String encoded = URLEncoder.encode(artistName, StandardCharsets.UTF_8);
            String url = "https://api.discogs.com/database/search?q={}&type=artist&per_page=100".replace("{}", encoded);

            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "ComposerDesk/1.0")
                    .header("Accept", "application/json")
                    .get()
                    .build();

            log.debug("Discogs request: {}", url);

            Response response = client.newCall(request).execute();
            String body = response.body().string();
            log.debug("Discogs response: {}", response.code());

            if (!response.isSuccessful()) {
                return 0;
            }

            JsonNode json = mapper.readTree(body);
            JsonNode results = json.get("results");

            if (results != null && results.isArray()) {
                int exactMatches = 0;
                for (JsonNode result : results) {
                    String title = result.get("title").asText();
                    if (title.equalsIgnoreCase(artistName)) {
                        exactMatches++;
                    }
                }
                return exactMatches;
            }

            return 0;

        } catch (Exception e) {
            log.error("Discogs exception: {}", e.getMessage());
            return 0;
        }
    }

    public int countExactRelease(String releaseTitle) {
        try {
            respectRateLimit();

            String encoded = URLEncoder.encode(releaseTitle, StandardCharsets.UTF_8);
            String url = "https://api.discogs.com/database/search?release_title={}&type=release&per_page=100".replace("{}", encoded);

            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "ComposerDesk/1.0")
                    .header("Accept", "application/json")
                    .get()
                    .build();

            log.debug("Discogs request: {}", url);

            Response response = client.newCall(request).execute();
            String body = response.body().string();
            log.debug("Discogs response: {}", response.code());

            if (!response.isSuccessful()) {
                return 0;
            }

            JsonNode json = mapper.readTree(body);
            JsonNode results = json.get("results");

            if (results != null && results.isArray()) {
                int exactMatches = 0;
                for (JsonNode result : results) {
                    String title = result.get("title").asText();
                    if (title.equalsIgnoreCase(releaseTitle)) {
                        exactMatches++;
                    }
                }

                JsonNode pagination = json.get("pagination");
                if (pagination != null && pagination.get("pages").asInt() > 1) {
                    return pagination.get("items").asInt();
                }

                return exactMatches;
            }

            return 0;

        } catch (Exception e) {
            log.error("Discogs exception: {}", e.getMessage());
            return 0;
        }
    }
}