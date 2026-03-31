package com.authforge.security.oauth2;

import com.authforge.exception.OAuth2AuthenticationException;
import java.util.Map;

public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if ("google".equalsIgnoreCase(registrationId)) {
            return new GoogleOAuth2UserInfo(attributes);
        }
        throw new OAuth2AuthenticationException("Login with " + registrationId + " is not supported.");
    }
}
