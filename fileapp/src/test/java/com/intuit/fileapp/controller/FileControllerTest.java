package com.intuit.fileapp.controller;

import com.intuit.fileapp.model.AuditLog;
import com.intuit.fileapp.model.ObjectMetadataDTO;
import com.intuit.fileapp.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.*;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;  // For simulating HTTP requests

    @MockBean
    private S3Service s3Service;  // Mock the S3Service

    @Mock
    private OidcUser oidcUser;  // Mock the OIDC user for authentication

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

    // Test case: Get metadata for an existing S3 object
    @Test
    public void testGetObjectMetadataSuccess() throws Exception {
        // Arrange
        ObjectMetadataDTO metadataDTO = new ObjectMetadataDTO();
        metadataDTO.setVersionId("v1");
        metadataDTO.setStorageClass("STANDARD");
        metadataDTO.setPartsCount(12345L);
        metadataDTO.setLastModified(java.util.Date.from(Instant.now()));

        when(s3Service.getObjectMetadata("test-object", oidcUser)).thenReturn(metadataDTO);

        // Act & Assert
        mockMvc.perform(get("/api/object_metadata/test-object")
                        .principal(() -> String.valueOf(oidcUser))  // Simulate authenticated user
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())  // Expect 200 OK
                .andExpect(jsonPath("$.versionId").value("v1"))
                .andExpect(jsonPath("$.storageClass").value("STANDARD"))
                .andExpect(jsonPath("$.partsCount").value(12345L));
    }

    // Test case: S3 object metadata not found
    @Test
    public void testGetObjectMetadataNotFound() throws Exception {
        // Arrange
        when(s3Service.getObjectMetadata("non-existent-object", oidcUser)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/object_metadata/non-existent-object")
                        .principal(() -> String.valueOf(oidcUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());  // Expect 404 Not Found
    }

    // Test case: Health check endpoint
    @Test
    public void testCheckHealth() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())  // Expect 200 OK
                .andExpect(content().string("Application is healthy"));
    }

    // Test case: Fetch audit log
    @Test
    public void testGetAuditLog() throws Exception {
        // Arrange
        AuditLog log1 = new AuditLog("object1", "user1", "user1@example.com", java.time.LocalDateTime.now());
        AuditLog log2 = new AuditLog("object2", "user2", "user2@example.com", java.time.LocalDateTime.now());
        List<AuditLog> auditLogs = Arrays.asList(log1, log2);
        when(s3Service.getAuditLog()).thenReturn(auditLogs);

        // Act & Assert
        mockMvc.perform(get("/api/audit_log"))
                .andExpect(status().isOk())  // Expect 200 OK
                .andExpect(jsonPath("$[0].objectName").value("object1"))
                .andExpect(jsonPath("$[1].objectName").value("object2"));
    }
}

