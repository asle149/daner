package com.daner.common.ratelimit;

import com.daner.common.exception.BusinessException;
import com.daner.common.exception.ErrorCode;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@RequiredArgsConstructor
public class RateLimiter {

    private final RateLimitProperties properties;
    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public void checkMemberComment(Long userId) {
        consume("comment:member:" + userId, properties.memberCommentsPerMinute());
    }

    public void checkGuestComment(String token) {
        consume("comment:guest:" + token, properties.guestCommentsPerMinute());
    }

    public void checkMemberLike(Long userId) {
        consume("like:member:" + userId, properties.memberLikesPerMinute());
    }

    private void consume(String key, int permitsPerMinute) {
        Bucket bucket = buckets.computeIfAbsent(key, k -> Bucket.builder()
                .addLimit(Bandwidth.simple(permitsPerMinute, Duration.ofMinutes(1)))
                .build());
        if (!bucket.tryConsume(1)) {
            throw new BusinessException(ErrorCode.RATE_LIMIT_EXCEEDED);
        }
    }
}
