package com.pemc.crss.rbcq.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserInfoService {

    @Value("${authorization.server.user-info-uri}")
    private String userInfoUri;

    private final RestTemplate restTemplate;

    /**
     * Fetches the user's "name" from the /uaa/user endpoint.
     *
     * @param accessToken Bearer token from the current request
     * @return The "name" of the authenticated user
     */
    public String getUsernameFromToken(String accessToken) {
        // Create HTTP headers with the Bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);

        // Call the /uaa/user endpoint
        ResponseEntity<Map> response = restTemplate.postForEntity(
                userInfoUri,
                new HttpEntity<>(headers),
                Map.class
        );

        // Parse the response to extract the "name" field
        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null && responseBody.containsKey("user")) {
            Map<String, Object> userDetails = (Map<String, Object>) responseBody.get("user");
            if (userDetails != null && userDetails.containsKey("name")) {
                return userDetails.get("name").toString(); // Return the name field
            }
        }

        throw new RuntimeException("Failed to extract 'name' from the /uaa/user response.");
    }
}