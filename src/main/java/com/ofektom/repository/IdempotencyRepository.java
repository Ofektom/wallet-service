package com.ofektom.repository;

import com.ofektom.model.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for IdempotencyKey persistence operations.
 * Used to track and prevent duplicate transaction processing.
 */
@Repository
public interface IdempotencyRepository extends JpaRepository<IdempotencyKey, Long> {
    Optional<IdempotencyKey> findByKeyValue(String keyValue);
    
    // Checks if idempotency key exists (for quick duplicate detection)
    boolean existsByKeyValue(String keyValue);
}

