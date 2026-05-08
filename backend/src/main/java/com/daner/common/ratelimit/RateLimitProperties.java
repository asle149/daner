package com.daner.common.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(
        int memberCommentsPerMinute,
        int guestCommentsPerMinute,
        int memberLikesPerMinute
) {

    public RateLimitProperties {
        if (memberCommentsPerMinute <= 0) memberCommentsPerMinute = 10;
        if (guestCommentsPerMinute <= 0) guestCommentsPerMinute = 3;
        if (memberLikesPerMinute <= 0) memberLikesPerMinute = 30;
    }
}
