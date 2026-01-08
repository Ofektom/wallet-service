package com.ofektom.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity to track idempotency keys and prevent duplicate transaction processing.
 * Uses unique constraint at database level for thread-safe idempotency checks.
 */
@Entity
@Table(name = "idempotency_keys", 
    uniqueConstraints = @UniqueConstraint(name = "uk_idempotency_key", columnNames = "key_value"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyKey {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "key_value", unique = true, nullable = false, length = 255)
    private String keyValue;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
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

