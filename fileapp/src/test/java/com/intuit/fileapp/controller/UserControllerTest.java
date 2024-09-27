package com.intuit.fileapp.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.*;

@WebMvcTest(UserController.class)  // Only test the UserController class
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;  // MockMvc to simulate HTTP requests

    @Mock
    private OidcUser oidcUser;  // Mock OidcUser

    @BeforeEach
    public void setUp() {
        // Arrange: Mock an OidcIdToken
        Map<String, Object> claims = Map.of("sub", "1234567890", "name", "John Doe", "email", "john.doe@example.com");
        OidcIdToken idToken = new OidcIdToken("tokenValue", Instant.now(), Instant.now().plusSeconds(3600), claims);

        // Arrange: Mock an OidcUserInfo
        OidcUserInfo userInfo = new OidcUserInfo(claims);

        // Arrange: Set up OidcUserAuthority and mock user methods
        OidcUserAuthority authority = new OidcUserAuthority(idToken, userInfo);
        Collection<GrantedAuthority> authorities = Collections.singletonList(authority);  // Explicitly use List<GrantedAuthority>

        when(oidcUser.getFullName()).thenReturn("John Doe");
        when(oidcUser.getEmail()).thenReturn("john.doe@example.com");

        // Set the OidcUser in the SecurityContext
        SecurityContextHolder.setContext(new SecurityContextImpl(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        oidcUser, null, authorities)));
    }

    // Test case: User is authenticated and user info is returned
    @Test
    public void testGetUserInfoAuthenticated() throws Exception {
        // Arrange: Mock the OidcUser and its methods
        when(oidcUser.getFullName()).thenReturn("John Doe");
        when(oidcUser.getEmail()).thenReturn("john.doe@example.com");

        // Act & Assert: Perform a GET request to /api/userinfo and verify the response
        mockMvc.perform(get("/api/userinfo")
                        .principal(() -> String.valueOf(oidcUser))  // Simulate the user being authenticated
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())  // Expect HTTP 200 OK
                .andExpect(jsonPath("$.name").value("John Doe"))  // Verify "name" in the response
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));  // Verify "email" in the response
    }

    // Test case: User is not authenticated, should return 401 Unauthorized
    @Test
    public void testGetUserInfoUnauthenticated() throws Exception {
        SecurityContextHolder.clearContext();
        // Act & Assert: Perform a GET request to /api/userinfo without user and expect HTTP 401
        mockMvc.perform(get("/api/userinfo")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection());  // Expect HTTP 401 Unauthorized
    }
}
