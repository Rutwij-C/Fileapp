package com.intuit.fileapp.service;
import com.intuit.fileapp.model.AuditLog;
import com.intuit.fileapp.model.ObjectMetadataDTO;
import com.intuit.fileapp.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.regions.Region;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class S3Service {

    private final S3Client s3Client;

    @Autowired
    private AuditLogRepository auditLogRepository;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String createBucket(String bucketName) {
        try {
            CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            CreateBucketResponse createBucketResponse = s3Client.createBucket(createBucketRequest);
            return "Bucket created successfully: " + createBucketResponse.location();
        } catch (S3Exception e) {
            e.printStackTrace();
            return "Failed to create bucket: " + e.awsErrorDetails().errorMessage();
        }
    }

    public ObjectMetadataDTO getObjectMetadata(String objectName, OidcUser user) {
        try {
            // Create the request
            String bucketName = "fileapp-bucket";
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectName)
                    .build();

            // Fetch the object's metadata
            HeadObjectResponse response = s3Client.headObject(request);

            // Capture user details from OIDC session
            String username = user.getName();
            String email = user.getEmail();

            // Save the audit log in the database
            AuditLog auditLog = new AuditLog(objectName, username, email, LocalDateTime.now());
            auditLogRepository.save(auditLog);

            // Map the response to your DTO
            ObjectMetadataDTO metadataDTO = new ObjectMetadataDTO();
            metadataDTO.setVersionId(response.versionId());
            metadataDTO.setStorageClass(response.storageClassAsString());
            metadataDTO.setPartsCount(response.contentLength());
            metadataDTO.setLastModified(Date.from(response.lastModified()));

            return metadataDTO;
        } catch (Exception e) {
            // Handle exception (object not found, etc.)
            return null;
        }
    }

    // Method to return the audit log (list of all S3 object names that have been queried)
    public List<AuditLog> getAuditLog() {
        // Return the audit logs from the database
        return auditLogRepository.findAll();
    }
}
