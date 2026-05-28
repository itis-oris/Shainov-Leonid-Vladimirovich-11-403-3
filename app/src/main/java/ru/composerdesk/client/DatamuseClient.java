package ru.composerdesk.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Cacheable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DatamuseClient {
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    
    public List<String> getRhymes(String word) {
        try {
            String url = new StringBuilder("https://api.datamuse.com/words?rel_rhy=").append(java.net.URLEncoder.encode(word, "UTF-8")).append("&max=20").toString();
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();
            JsonNode json = mapper.readTree(response.body().string());

            List<String> rhymes = new ArrayList<>();
            for (JsonNode node : json) {
                rhymes.add(node.get("word").asText());
            }
            return rhymes;
        } catch (Exception e) {
            return List.of();
        }
    }
}