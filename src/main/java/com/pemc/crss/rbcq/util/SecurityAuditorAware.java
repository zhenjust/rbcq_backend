package com.pemc.crss.rbcq.util;

import com.pemc.crss.rbcq.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Component;
import java.util.*;

import java.util.Optional;

@Component
@Slf4j
public class SecurityAuditorAware implements AuditorAware<Long> {

    private final UserInfoService userInfoService;

    public SecurityAuditorAware(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @Override
    public @NonNull Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Ensure user is authenticated
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        // Extract the Bearer token
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) authentication.getDetails();
        if (details == null) {
            return Optional.empty();
        }


        // Get the username from the /uaa/user endpoint
        try {
            Long userId = userInfoService.getUserIdFromToken(details.getTokenValue());
            return Optional.ofNullable(userId);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }


    }


    public @NonNull Optional<String> getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Ensure user is authenticated
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        // Extract the Bearer token
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) authentication.getDetails();
        if (details == null) {
            return Optional.empty();
        }


        // Get the username from the /uaa/user endpoint
        try {
            String userId = userInfoService.getUsernameFromToken(details.getTokenValue());
            return Optional.ofNullable(userId);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }




    }
}
