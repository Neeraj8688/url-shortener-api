package com.neeraj.urlshortener.service;

import com.neeraj.urlshortener.dto.ShortenRequest;
import com.neeraj.urlshortener.dto.ShortenResponse;
import com.neeraj.urlshortener.exception.DuplicateAliasException;
import com.neeraj.urlshortener.exception.ShortCodeNotFoundException;
import com.neeraj.urlshortener.model.UrlMapping;
import com.neeraj.urlshortener.repository.UrlMappingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlShortenerService {

    private final UrlMappingRepository repository;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.short-code-length:6}")
    private int shortCodeLength;

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Shorten a URL. Returns existing entry if same URL was already shortened.
     */
    @Transactional
    public ShortenResponse shorten(ShortenRequest request) {
        String alias = request.getCustomAlias();

        // If custom alias provided, validate uniqueness
        if (alias != null && !alias.isBlank()) {
            if (repository.existsByShortCode(alias)) {
                throw new DuplicateAliasException(alias);
            }
        } else {
            // Check if URL was already shortened (return existing)
            var existing = repository.findByOriginalUrl(request.getUrl());
            if (existing.isPresent()) {
                log.info("URL already shortened, returning existing: {}", existing.get().getShortCode());
                return toResponse(existing.get());
            }
            alias = generateUniqueCode();
        }

        UrlMapping mapping = UrlMapping.builder()
                .shortCode(alias)
                .originalUrl(request.getUrl())
                .clickCount(0L)
                .build();

        UrlMapping saved = repository.save(mapping);
        log.info("Created short URL: {} -> {}", saved.getShortCode(), saved.getOriginalUrl());
        return toResponse(saved);
    }

    /**
     * Resolve a short code to the original URL, incrementing click count.
     */
    @Transactional
    public String resolve(String shortCode) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortCodeNotFoundException(shortCode));

        repository.incrementClickCount(shortCode, LocalDateTime.now());
        log.info("Redirecting code '{}' -> {}", shortCode, mapping.getOriginalUrl());
        return mapping.getOriginalUrl();
    }

    /**
     * Get stats for a short code without incrementing click count.
     */
    public ShortenResponse getStats(String shortCode) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortCodeNotFoundException(shortCode));
        return toResponse(mapping);
    }

    /**
     * List all shortened URLs with their stats.
     */
    public List<ShortenResponse> listAll() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Delete a short URL.
     */
    @Transactional
    public void delete(String shortCode) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortCodeNotFoundException(shortCode));
        repository.delete(mapping);
        log.info("Deleted short code: {}", shortCode);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String generateUniqueCode() {
        String code;
        do {
            code = randomString(shortCodeLength);
        } while (repository.existsByShortCode(code));
        return code;
    }

    private String randomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    private ShortenResponse toResponse(UrlMapping m) {
        return ShortenResponse.builder()
                .shortCode(m.getShortCode())
                .shortUrl(baseUrl + "/" + m.getShortCode())
                .originalUrl(m.getOriginalUrl())
                .clickCount(m.getClickCount())
                .createdAt(m.getCreatedAt())
                .lastAccessedAt(m.getLastAccessedAt())
                .build();
    }
}
