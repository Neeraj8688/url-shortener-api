package com.neeraj.urlshortener;

import com.neeraj.urlshortener.dto.ShortenRequest;
import com.neeraj.urlshortener.dto.ShortenResponse;
import com.neeraj.urlshortener.repository.UrlMappingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UrlShortenerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UrlMappingRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    // ── Test 1: POST /shorten creates a short URL ─────────────────────────────

    @Test
    @DisplayName("POST /shorten returns 201 with short code for valid URL")
    void shouldShortenValidUrl() throws Exception {
        ShortenRequest request = new ShortenRequest("https://www.example.com/some/very/long/path", null);

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").isNotEmpty())
                .andExpect(jsonPath("$.shortUrl").value(containsString("http://localhost:8080/")))
                .andExpect(jsonPath("$.originalUrl").value("https://www.example.com/some/very/long/path"))
                .andExpect(jsonPath("$.clickCount").value(0));
    }

    // ── Test 2: Custom alias is respected ─────────────────────────────────────

    @Test
    @DisplayName("POST /shorten with custom alias uses that alias as the short code")
    void shouldRespectCustomAlias() throws Exception {
        ShortenRequest request = new ShortenRequest("https://www.github.com", "mygithub");

        MvcResult result = mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        ShortenResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ShortenResponse.class);

        assertThat(response.getShortCode()).isEqualTo("mygithub");
        assertThat(response.getShortUrl()).endsWith("/mygithub");
    }

    // ── Test 3: Duplicate alias returns 409 ───────────────────────────────────

    @Test
    @DisplayName("POST /shorten returns 409 when custom alias already exists")
    void shouldReturn409ForDuplicateAlias() throws Exception {
        ShortenRequest first = new ShortenRequest("https://www.google.com", "myalias");
        mockMvc.perform(post("/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(first)));

        ShortenRequest duplicate = new ShortenRequest("https://www.bing.com", "myalias");
        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    // ── Test 4: GET /:code redirects and tracks click ─────────────────────────

    @Test
    @DisplayName("GET /:code redirects to original URL and increments click count")
    void shouldRedirectAndTrackClick() throws Exception {
        // Create short URL
        ShortenRequest request = new ShortenRequest("https://www.wikipedia.org", "wiki");
        mockMvc.perform(post("/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Hit the redirect endpoint
        mockMvc.perform(get("/wiki"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://www.wikipedia.org"));

        // Verify click count incremented
        mockMvc.perform(get("/api/urls/wiki/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clickCount").value(1));
    }

    // ── Test 5: GET /:code returns 404 for unknown code ───────────────────────

    @Test
    @DisplayName("GET /:code returns 404 for unknown short code")
    void shouldReturn404ForUnknownCode() throws Exception {
        mockMvc.perform(get("/nonexistentcode"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(containsString("nonexistentcode")));
    }

    // ── Test 6: Invalid URL returns 400 ───────────────────────────────────────

    @Test
    @DisplayName("POST /shorten returns 400 for invalid URL")
    void shouldReturn400ForInvalidUrl() throws Exception {
        ShortenRequest request = new ShortenRequest("not-a-valid-url", null);

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    // ── Test 7: GET /api/urls lists all URLs ─────────────────────────────────

    @Test
    @DisplayName("GET /api/urls returns all shortened URLs")
    void shouldListAllUrls() throws Exception {
        mockMvc.perform(post("/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ShortenRequest("https://www.a.com", null))));
        mockMvc.perform(post("/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ShortenRequest("https://www.b.com", null))));

        mockMvc.perform(get("/api/urls"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // ── Test 8: DELETE /api/urls/:code removes the entry ─────────────────────

    @Test
    @DisplayName("DELETE /api/urls/:code removes the short URL")
    void shouldDeleteShortUrl() throws Exception {
        mockMvc.perform(post("/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ShortenRequest("https://www.delete-me.com", "deleteme"))));

        mockMvc.perform(delete("/api/urls/deleteme"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/deleteme"))
                .andExpect(status().isNotFound());
    }
}
