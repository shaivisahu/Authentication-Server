package com.authforge.security.oauth2;

import com.authforge.config.AppProperties;
import com.authforge.service.RefreshTokenService;
import com.authforge.security.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final AppProperties appProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String targetUrl = determineTargetUrl(request, response, authentication);
        if (response.isCommitted()) return;
        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        String redirectUri = request.getParameter("redirect_uri");
        if (redirectUri != null && !isAuthorizedRedirectUri(redirectUri)) {
            throw new IllegalArgumentException("Unauthorized redirect URI");
        }
        String targetUrl = redirectUri != null ? redirectUri
                : appProperties.getOauth2().getAuthorizedRedirectUris().get(0);

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String accessToken = jwtProvider.generateAccessToken(principal.getEmail());
        String refreshToken = refreshTokenService.createRefreshToken(principal.getEmail(), request).getToken();

        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("token", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        URI clientRedirectUri = URI.create(uri);
        return appProperties.getOauth2().getAuthorizedRedirectUris().stream()
                .anyMatch(authorizedUri -> {
                    URI authorized = URI.create(authorizedUri);
                    return authorized.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                            && authorized.getPort() == clientRedirectUri.getPort();
                });
    }
}
