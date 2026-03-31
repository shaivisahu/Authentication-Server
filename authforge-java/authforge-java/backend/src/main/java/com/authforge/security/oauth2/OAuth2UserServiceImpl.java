package com.authforge.security.oauth2;

import com.authforge.entity.User;
import com.authforge.exception.OAuth2AuthenticationException;
import com.authforge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuth2UserServiceImpl extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) {
        OAuth2User oAuth2User = super.loadUser(request);
        String registrationId = request.getClientRegistration().getRegistrationId();

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        if (userInfo.getEmail() == null || userInfo.getEmail().isBlank()) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        Optional<User> existingUser = userRepository.findByEmail(userInfo.getEmail());
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            if (!user.getProvider().name().equalsIgnoreCase(registrationId)) {
                throw new OAuth2AuthenticationException(
                    "Account already exists with " + user.getProvider() + ". Please use that to login.");
            }
            user.setProviderAttributes(userInfo);
        } else {
            user = registerOAuthUser(registrationId, userInfo);
        }

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    private User registerOAuthUser(String registrationId, OAuth2UserInfo info) {
        User.AuthProvider provider = User.AuthProvider.valueOf(registrationId.toUpperCase());
        String username = generateUsername(info.getName());

        User user = User.builder()
                .uuid(java.util.UUID.randomUUID().toString())
                .username(username)
                .email(info.getEmail())
                .provider(provider)
                .providerId(info.getId())
                .emailVerified(true)
                .role(User.Role.ROLE_USER)
                .build();

        return userRepository.save(user);
    }

    private String generateUsername(String name) {
        String base = name.toLowerCase().replaceAll("[^a-z0-9]", "_");
        if (base.length() > 40) base = base.substring(0, 40);
        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + "_" + suffix++;
        }
        return candidate;
    }
}
