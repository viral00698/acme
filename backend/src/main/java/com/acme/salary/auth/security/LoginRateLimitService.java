package com.acme.salary.auth.security;

import com.acme.salary.common.exception.RateLimitExceededException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class LoginRateLimitService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_SECONDS = 900; // 15 minutes

    private final ConcurrentHashMap<String, CopyOnWriteArrayList<Instant>> failureLog = new ConcurrentHashMap<>();

    public void checkRateLimit(String key) {
        CopyOnWriteArrayList<Instant> attempts = failureLog.get(key);
        if (attempts == null) {
            return;
        }

        Instant cutoff = Instant.now().minusSeconds(LOCKOUT_DURATION_SECONDS);
        attempts.removeIf(instant -> instant.isBefore(cutoff));

        if (attempts.size() >= MAX_FAILED_ATTEMPTS) {
            throw new RateLimitExceededException(
                    "Too many failed login attempts. Please wait 15 minutes before trying again."
            );
        }
    }

    public void recordFailedAttempt(String key) {
        failureLog.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(Instant.now());
    }

    public void recordSuccessfulAttempt(String key) {
        failureLog.remove(key);
    }
}
