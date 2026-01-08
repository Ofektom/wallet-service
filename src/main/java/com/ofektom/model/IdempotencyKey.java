package com.ofektom.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entity to track idempotency keys and prevent duplicate transaction processing.
 * Uses unique constraint at database level for thread-safe idempotency checks.
 */
@Entity
@Table(name = "idempotency_keys", 
    uniqueConstraints = @UniqueConstraint(name = "uk_idempotency_key", columnNames = "key_value"))
public class IdempotencyKey {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "key_value", unique = true, nullable = false, length = 255)
    private String keyValue;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public IdempotencyKey() {
    }
    
    public IdempotencyKey(Long id, String keyValue, LocalDateTime createdAt) {
        this.id = id;
        this.keyValue = keyValue;
        this.createdAt = createdAt;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getKeyValue() {
        return keyValue;
    }
    
    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    public static IdempotencyKey of(String keyValue) {
        IdempotencyKey key = new IdempotencyKey();
        key.setKeyValue(keyValue);
        return key;
    }
}

