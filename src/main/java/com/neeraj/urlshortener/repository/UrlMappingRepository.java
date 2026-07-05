package com.neeraj.urlshortener.repository;

import com.neeraj.urlshortener.model.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

    Optional<UrlMapping> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    Optional<UrlMapping> findByOriginalUrl(String originalUrl);

    @Modifying
    @Query("UPDATE UrlMapping u SET u.clickCount = u.clickCount + 1, u.lastAccessedAt = :accessedAt WHERE u.shortCode = :shortCode")
    void incrementClickCount(String shortCode, LocalDateTime accessedAt);
}
