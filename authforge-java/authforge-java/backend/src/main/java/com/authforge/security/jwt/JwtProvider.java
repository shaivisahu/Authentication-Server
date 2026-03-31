package com.authforge.security.jwt;

import com.authforge.config.AppProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtProvider {

    private final AppProperties appProperties;

    // ── Access Token ────────────────────────────────────────

    public String generateAccessToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return buildToken(userDetails.getUsername(), appProperties.getJwt().getAccessExpirationMs(),
                getAccessKey());
    }

    public String generateAccessToken(String email) {
        return buildToken(email, appProperties.getJwt().getAccessExpirationMs(), getAccessKey());
    }

    public String getEmailFromAccessToken(String token) {
        return parseClaims(token, getAccessKey()).getSubject();
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token, getAccessKey());
    }

    // ── Refresh Token ───────────────────────────────────────

    public String generateRefreshToken(String email) {
        return buildToken(email, appProperties.getJwt().getRefreshExpirationMs(), getRefreshKey());
    }

    public String getEmailFromRefreshToken(String token) {
        return parseClaims(token, getRefreshKey()).getSubject();
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token, getRefreshKey());
    }

    // ── Internals ───────────────────────────────────────────

    private String buildToken(String subject, long expirationMs, SecretKey key) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(expiry)
                .issuer("authforge")
                .signWith(key)
                .compact();
    }

    private Claims parseClaims(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer("authforge")
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean validateToken(String token, SecretKey key) {
        try {
            parseClaims(token, key);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT unsupported: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT malformed: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("JWT signature invalid: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT empty: {}", e.getMessage());
        }
        return false;
    }

    private SecretKey getAccessKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(appProperties.getJwt().getAccessSecret()));
    }

    private SecretKey getRefreshKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(appProperties.getJwt().getRefreshSecret()));
    }
}
