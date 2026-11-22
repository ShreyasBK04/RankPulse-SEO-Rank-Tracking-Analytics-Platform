package com.demo.ranktracker.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * Client for the SearchAPI.io Google Rank Tracking API.
 */
@Component
public class SearchApiClient {

    private static final Logger log = LoggerFactory.getLogger(SearchApiClient.class);
    private final RestClient restClient;

    public SearchApiClient(RestClient.Builder builder,
                           @Value("${search.api-key}") String apiKey) {
        String authHeader = apiKey != null && apiKey.startsWith("Bearer ") ? apiKey : "Bearer " + apiKey;
        this.restClient = builder
                .baseUrl("https://www.searchapi.io/api/v1/search")
                .defaultHeader("Authorization", authHeader)
                .build();
    }

    /**
     * Fetches the top 100 Google organic results for a keyword.
     *
     * @param keyword the search query
     * @return list of organic results, or empty list if none found
     * @throws RestClientException if the API call fails
     */
    public List<Result> top100(String keyword) {
        log.info("Querying SearchAPI.io (google_rank_tracking) for query: '{}'", keyword);
        try {
            Response response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("engine", "google_rank_tracking")
                            .queryParam("q", keyword)
                            .build())
                    .retrieve()
                    .body(Response.class);

            if (response == null) {
                log.warn("SearchAPI returned empty response for query: '{}'", keyword);
                return List.of();
            }

            if (response.error() != null && !response.error().isBlank()) {
                log.error("SearchAPI error message: {}", response.error());
                throw new RestClientException("SearchAPI Error: " + response.error());
            }

            List<Result> results = response.organicResults() != null
                    ? response.organicResults()
                    : List.of();

            log.info("SearchAPI returned {} organic SERP results for '{}'", results.size(), keyword);
            return results;

        } catch (RestClientException e) {
            log.error("SearchAPI call failed for keyword '{}': {}", keyword, e.getMessage());
            throw e;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
            @JsonProperty("organic_results") List<Result> organicResults,
            @JsonProperty("error") String error
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(
            @JsonProperty("position") Integer position,
            @JsonProperty("link") String link,
            @JsonProperty("domain") String domain,
            @JsonProperty("title") String title,
            @JsonProperty("snippet") String snippet
    ) {}
}
