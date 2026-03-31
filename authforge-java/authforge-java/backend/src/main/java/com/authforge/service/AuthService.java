package com.authforge.service;

import com.authforge.dto.request.LoginRequest;
import com.authforge.dto.request.SignupRequest;
import com.authforge.dto.response.AuthResponse;
import com.authforge.entity.RefreshToken;
import com.authforge.entity.User;
import com.authforge.exception.BadRequestException;
import com.authforge.repository.UserRepository;
import com.authforge.security.jwt.JwtProvider;
import com.authforge.security.oauth2.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final AuditService auditService;

    @Transactional
    public AuthResponse signup(SignupRequest req, HttpServletRequest httpRequest) {
        if (userRepository.existsByEmail(req.getEmail())) {
            auditService.log(null, "SIGNUP_FAILED", httpRequest, false, "Email already in use: " + req.getEmail());
            throw new BadRequestException("Email is already registered");
        }
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }

        User user = User.builder()
                .uuid(UUID.randomUUID().toString())
                .username(req.getUsername())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(User.Role.ROLE_USER)
                .provider(User.AuthProvider.LOCAL)
                .emailVerified(false)
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());
        auditService.log(user.getId(), "SIGNUP_SUCCESS", httpRequest, true, null);

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);

        return buildAuthResponse(auth, httpRequest);
    }

    @Transactional
    public AuthResponse login(LoginRequest req, HttpServletRequest httpRequest) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        auditService.log(principal.getId(), "LOGIN_SUCCESS", httpRequest, true, null);

        return buildAuthResponse(auth, httpRequest);
    }

    @Transactional
    public AuthResponse refreshToken(String oldRefreshToken, HttpServletRequest httpRequest) {
        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(oldRefreshToken, httpRequest);
        String newAccessToken = jwtProvider.generateAccessToken(newRefreshToken.getUser().getEmail());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(900L)
                .build();
    }

    @Transactional
    public void logout(String refreshToken, String email, boolean allSessions) {
        if (allSessions) {
            refreshTokenService.revokeAllUserTokens(email);
        } else if (refreshToken != null) {
            refreshTokenService.revokeToken(refreshToken);
        }
    }

    private AuthResponse buildAuthResponse(Authentication auth, HttpServletRequest request) {
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        String accessToken = jwtProvider.generateAccessToken(principal.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(principal.getEmail(), request);

        User user = userRepository.findByEmail(principal.getEmail()).orElseThrow();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(900L)
                .userId(user.getUuid())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }
}
