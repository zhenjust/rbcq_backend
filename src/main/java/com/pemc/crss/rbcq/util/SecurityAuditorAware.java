package com.pemc.crss.rbcq.util;

import com.pemc.crss.rbcq.service.UserInfoService;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityAuditorAware implements AuditorAware<String> {

    private final UserInfoService userInfoService;

    public SecurityAuditorAware(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Ensure user is authenticated
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        // Extract the Bearer token
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) authentication.getDetails();
        String accessToken = details.getTokenValue();

        // Get the username from the /uaa/user endpoint
        String username = userInfoService.getUsernameFromToken(accessToken);

        return Optional.of(username);
    }
}