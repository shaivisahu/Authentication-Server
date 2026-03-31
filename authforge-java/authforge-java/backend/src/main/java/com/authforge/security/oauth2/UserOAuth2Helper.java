package com.authforge.security.oauth2;

import com.authforge.entity.User;

/**
 * Extension utility to update User fields from OAuth2 provider info.
 * Called on each OAuth2 login to keep profile data fresh.
 */
public class UserOAuth2Helper {

    public static void updateUserFromOAuth2Info(User user, OAuth2UserInfo info) {
        // Update provider-linked fields if changed
        if (info.getId() != null && !info.getId().equals(user.getProviderId())) {
            user.setProviderId(info.getId());
        }
    }
}
