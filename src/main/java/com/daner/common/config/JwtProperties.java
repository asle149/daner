package com.daner.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        int accessTokenValidityMinutes,
        int refreshTokenValidityDays,
        int signupTokenValidityMinutes
) {
}
