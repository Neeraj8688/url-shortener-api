package com.neeraj.urlshortener.controller;

import com.neeraj.urlshortener.dto.ShortenRequest;
import com.neeraj.urlshortener.dto.ShortenResponse;
import com.neeraj.urlshortener.service.UrlShortenerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class UrlShortenerController {

    private final UrlShortenerService service;

    @PostMapping("/shorten")
    public ResponseEntity<ShortenResponse> shorten(@Valid @RequestBody ShortenRequest request) {
        ShortenResponse response = service.shorten(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        String originalUrl = service.resolve(code);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }

    @GetMapping("/api/urls/{code}/stats")
    public ResponseEntity<ShortenResponse> getStats(@PathVariable String code) {
        return ResponseEntity.ok(service.getStats(code));
    }

    @GetMapping("/api/urls")
    public ResponseEntity<List<ShortenResponse>> listAll() {
        return ResponseEntity.ok(service.listAll());
    }

    @DeleteMapping("/api/urls/{code}")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        service.delete(code);
        return ResponseEntity.noContent().build();
    }
}
