package com.authforge.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {

    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> signupBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> refreshBuckets = new ConcurrentHashMap<>();

    /** Login: 10 attempts per 15 minutes per IP */
    public Bucket resolveLoginBucket(String ip) {
        return loginBuckets.computeIfAbsent(ip, k ->
            Bucket.builder()
                .addLimit(Bandwidth.builder()
                    .capacity(10)
                    .refillIntervally(10, Duration.ofMinutes(15))
                    .build())
                .build());
    }

    /** Signup: 5 registrations per hour per IP */
    public Bucket resolveSignupBucket(String ip) {
        return signupBuckets.computeIfAbsent(ip, k ->
            Bucket.builder()
                .addLimit(Bandwidth.builder()
                    .capacity(5)
                    .refillIntervally(5, Duration.ofHours(1))
                    .build())
                .build());
    }

    /** Refresh: 60 requests per 15 minutes per IP */
    public Bucket resolveRefreshBucket(String ip) {
        return refreshBuckets.computeIfAbsent(ip, k ->
            Bucket.builder()
                .addLimit(Bandwidth.builder()
                    .capacity(60)
                    .refillIntervally(60, Duration.ofMinutes(15))
                    .build())
                .build());
    }
}
