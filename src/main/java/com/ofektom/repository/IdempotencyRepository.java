package com.ofektom.repository;

import com.ofektom.model.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdempotencyRepository extends JpaRepository<IdempotencyKey, Long> {
    Optional<IdempotencyKey> findByKeyValue(String keyValue);
    
    boolean existsByKeyValue(String keyValue);
}

