package com.intuit.fileapp.service;

import com.intuit.fileapp.model.AuditLog;
import com.intuit.fileapp.model.ObjectMetadataDTO;
import com.intuit.fileapp.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private S3Service s3Service;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Initializes mocks
    }

    // Test case for successful bucket creation
    @Test
    public void testCreateBucketSuccess() {
        // Arrange
        String bucketName = "test-bucket";
        CreateBucketResponse createBucketResponse = mock(CreateBucketResponse.class);
        when(s3Client.createBucket(any(CreateBucketRequest.class))).thenReturn(createBucketResponse);
        when(createBucketResponse.location()).thenReturn("us-east-1");

        // Act
        String result = s3Service.createBucket(bucketName);

        // Assert
        assertEquals("Bucket created successfully: us-east-1", result);
        verify(s3Client).createBucket(any(CreateBucketRequest.class));
    }

    // Test case for failed bucket creation
    @Test
    public void testCreateBucketFailure() {
        // Arrange
        String bucketName = "test-bucket";
        S3Exception s3Exception = (S3Exception) S3Exception.builder().message("Error creating bucket").build();
        when(s3Client.createBucket(any(CreateBucketRequest.class))).thenThrow(s3Exception);

        // Act
        String result = s3Service.createBucket(bucketName);

        // Assert
        assertEquals("Failed to create bucket: Error creating bucket", result);
        verify(s3Client).createBucket(any(CreateBucketRequest.class));
    }

    // Test case for retrieving object metadata
    @Test
    public void testGetObjectMetadataSuccess() {
        // Arrange
        String objectName = "test-object";
        OidcUser oidcUser = mock(OidcUser.class);
        when(oidcUser.getName()).thenReturn("test-user");
        when(oidcUser.getEmail()).thenReturn("test-user@example.com");

        HeadObjectResponse headObjectResponse = mock(HeadObjectResponse.class);
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(headObjectResponse);
        when(headObjectResponse.versionId()).thenReturn("v1");
        when(headObjectResponse.storageClassAsString()).thenReturn("STANDARD");
        when(headObjectResponse.contentLength()).thenReturn(12345L);
        when(headObjectResponse.lastModified()).thenReturn(Instant.now());

        // Act
        ObjectMetadataDTO result = s3Service.getObjectMetadata(objectName, oidcUser);

        // Assert
        assertNotNull(result);
        assertEquals("v1", result.getVersionId());
        assertEquals("STANDARD", result.getStorageClass());
        assertEquals(12345L, result.getPartsCount());

        // Verify that the audit log is saved
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    // Test case for getObjectMetadata when object is not found
    @Test
    public void testGetObjectMetadataFailure() {
        // Arrange
        String objectName = "non-existent-object";
        OidcUser oidcUser = mock(OidcUser.class);
        when(oidcUser.getName()).thenReturn("test-user");
        when(oidcUser.getEmail()).thenReturn("test-user@example.com");

        when(s3Client.headObject(any(HeadObjectRequest.class))).thenThrow(NoSuchKeyException.builder().build());

        // Act
        ObjectMetadataDTO result = s3Service.getObjectMetadata(objectName, oidcUser);

        // Assert
        assertNull(result); // Should return null when object is not found
    }

    // Test case for getting the audit log
    @Test
    public void testGetAuditLog() {
        // Arrange
        AuditLog log1 = new AuditLog("object1", "user1", "user1@example.com", LocalDateTime.now());
        AuditLog log2 = new AuditLog("object2", "user2", "user2@example.com", LocalDateTime.now());
        List<AuditLog> auditLogs = Arrays.asList(log1, log2);
        when(auditLogRepository.findAll()).thenReturn(auditLogs);

        // Act
        List<AuditLog> result = s3Service.getAuditLog();

        // Assert
        assertEquals(2, result.size());
        assertEquals("object1", result.get(0).getObjectName());
        verify(auditLogRepository).findAll();
    }
}
