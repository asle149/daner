package com.daner.auth.service;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AnonymousTokenResolver {

    public static final String HEADER = "X-Anonymous-Token";

    public Optional<UUID> resolve(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(headerValue.trim()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
