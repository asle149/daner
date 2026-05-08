package com.daner.auth.service;

import com.daner.auth.entity.RefreshToken;
import com.daner.auth.repository.RefreshTokenRepository;
import com.daner.common.config.JwtProperties;
import com.daner.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    @Transactional
    public void store(User user, String rawToken) {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(jwtProperties.refreshTokenValidityDays());
        RefreshToken entity = RefreshToken.builder()
                .user(user)
                .tokenHash(hash(rawToken))
                .expiresAt(expiresAt)
                .build();
        refreshTokenRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public boolean isStored(String rawToken) {
        return refreshTokenRepository.findByTokenHash(hash(rawToken))
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
                .isPresent();
    }

    @Transactional
    public void revokeAllForUser(Long userId) {
        refreshTokenRepository.deleteAllByUserId(userId);
    }

    private static String hash(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
