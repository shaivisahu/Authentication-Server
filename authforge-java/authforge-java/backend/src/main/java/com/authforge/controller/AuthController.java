package com.authforge.controller;

import com.authforge.config.RateLimitConfig;
import com.authforge.dto.request.LoginRequest;
import com.authforge.dto.request.SignupRequest;
import com.authforge.dto.request.TokenRefreshRequest;
import com.authforge.dto.response.ApiResponse;
import com.authforge.dto.response.AuthResponse;
import com.authforge.security.oauth2.UserPrincipal;
import com.authforge.service.AuthService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RateLimitConfig rateLimitConfig;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(
            @Valid @RequestBody SignupRequest req,
            HttpServletRequest httpRequest) {

        Bucket bucket = rateLimitConfig.resolveSignupBucket(getClientIp(httpRequest));
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("Too many signup attempts. Please try again later."));
        }

        AuthResponse response = authService.signup(req, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Account created successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest req,
            HttpServletRequest httpRequest) {

        Bucket bucket = rateLimitConfig.resolveLoginBucket(getClientIp(httpRequest));
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("Too many login attempts. Please try again in 15 minutes."));
        }

        AuthResponse response = authService.login(req, httpRequest);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody TokenRefreshRequest req,
            HttpServletRequest httpRequest) {

        Bucket bucket = rateLimitConfig.resolveRefreshBucket(getClientIp(httpRequest));
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("Too many refresh requests."));
        }

        AuthResponse response = authService.refreshToken(req.getRefreshToken(), httpRequest);
        return ResponseEntity.ok(ApiResponse.ok("Token refreshed", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestBody(required = false) TokenRefreshRequest req,
            @RequestParam(defaultValue = "false") boolean allSessions,
            @AuthenticationPrincipal UserPrincipal principal) {

        authService.logout(
                req != null ? req.getRefreshToken() : null,
                principal.getEmail(),
                allSessions);
        return ResponseEntity.ok(ApiResponse.ok(
                allSessions ? "All sessions revoked" : "Logged out successfully", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserPrincipal>> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(principal));
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        return (xff != null && !xff.isBlank()) ? xff.split(",")[0].trim() : request.getRemoteAddr();
    }
}
