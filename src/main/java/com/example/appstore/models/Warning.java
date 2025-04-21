// src/main/java/com/example/appstore/models/Warning.java
package com.example.appstore.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Warning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "developer_id")
    private User developer;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getDeveloper() { return developer; }
    public void setDeveloper(User developer) { this.developer = developer; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
