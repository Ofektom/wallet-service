package com.ofektom.repository;

import com.ofektom.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

/**
 * Repository interface for Wallet persistence operations.
 * Provides methods for finding wallets by business identifier with optional locking.
 */
@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    
    // Finds wallet with pessimistic lock for concurrent transaction safety
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.walletId = :walletId")
    Optional<Wallet> findByWalletIdWithLock(@Param("walletId") String walletId);
    
    // Finds wallet by business identifier without locking (for read operations)
    Optional<Wallet> findByWalletId(String walletId);
    
    boolean existsByWalletId(String walletId);
}
