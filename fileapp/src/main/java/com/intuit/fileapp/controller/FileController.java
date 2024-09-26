package com.intuit.fileapp.controller;

import com.intuit.fileapp.model.AuditLog;
import com.intuit.fileapp.model.ObjectMetadataDTO;
import com.intuit.fileapp.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class FileController {

    @Autowired
    private S3Service s3Service;

    // Endpoint to get the metadata of an S3 object
    @GetMapping("/object_metadata/{objectName}")
    public ResponseEntity<ObjectMetadataDTO> getObjectMetadata(@PathVariable String objectName, @AuthenticationPrincipal OidcUser user) {
        ObjectMetadataDTO metadata = s3Service.getObjectMetadata(objectName, user);
        if (metadata != null) {
            return ResponseEntity.ok(metadata);
        } else {
            return ResponseEntity.notFound().build(); // Return 404 if object is not found
        }
    }

    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<String> checkHealth() {
        return ResponseEntity.ok("Application is healthy");
    }

    // Endpoint to fetch the audit log of all requested S3 object metadata
    @GetMapping("/audit_log")
    public ResponseEntity<List<AuditLog>> getAuditLog() {
        List<AuditLog> auditLog = s3Service.getAuditLog();
        return ResponseEntity.ok(auditLog);
    }

    @PostMapping("/create_bucket")
    public ResponseEntity<String> createBucket(@RequestParam String bucketName) {
        String result = s3Service.createBucket(bucketName);
        return ResponseEntity.ok(result);
    }
}
