package com.intuit.fileapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String objectName;  // The S3 object name
    private String username;    // Username or email of the user who accessed the object
    private String email;       // Email of the user (optional, if available)
    private LocalDateTime requestedAt;  // Timestamp of the audit log entry

    public AuditLog() {
    }

    public AuditLog(String objectName, String username, String email, LocalDateTime requestedAt) {
        this.objectName = objectName;
        this.username = username;
        this.email = email;
        this.requestedAt = requestedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }
}

