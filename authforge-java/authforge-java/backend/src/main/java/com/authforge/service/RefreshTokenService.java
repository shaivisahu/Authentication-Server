package com.authforge.service;

import com.authforge.config.AppProperties;
import com.authforge.entity.RefreshToken;
import com.authforge.entity.User;
import com.authforge.exception.TokenRefreshException;
import com.authforge.repository.RefreshTokenRepository;
import com.authforge.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final AppProperties appProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public RefreshToken createRefreshToken(String email, HttpServletRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        long expirationMs = appProperties.getJwt().getRefreshExpirationMs();
        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusSeconds(expirationMs / 1000))
                .ipAddress(getClientIp(request))
                .userAgent(truncate(request.getHeader("User-Agent"), 512))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken rotateRefreshToken(String oldToken, HttpServletRequest request) {
        RefreshToken existing = refreshTokenRepository.findByToken(oldToken)
                .orElseThrow(() -> new TokenRefreshException("Refresh token not found"));

        if (existing.isRevoked()) {
            // Possible token reuse attack — revoke all tokens for this user
            refreshTokenRepository.revokeAllByUser(existing.getUser());
            throw new TokenRefreshException("Refresh token was already used. All sessions revoked for security.");
        }

        if (existing.isExpired()) {
            throw new TokenRefreshException("Refresh token has expired. Please log in again.");
        }

        // Revoke old token and issue new one
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        return createRefreshToken(existing.getUser().getEmail(), request);
    }

    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    @Transactional
    public void revokeAllUserTokens(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        refreshTokenRepository.revokeAllByUser(user);
    }

    @Transactional(readOnly = true)
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenRefreshException("Refresh token not found"));
    }

    /** Scheduled cleanup: runs every hour, removes expired/revoked tokens */
    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredAndRevoked(LocalDateTime.now());
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        return (xff != null && !xff.isBlank()) ? xff.split(",")[0].trim() : request.getRemoteAddr();
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
