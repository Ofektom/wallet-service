package com.ofektom.model;

import com.ofektom.enums.TransactionType;
import com.ofektom.utils.Money;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Wallet domain entity representing a user's wallet.
 * Encapsulates wallet balance and business logic for balance operations.
 */
@Entity
@Table(name = "wallets", indexes = @Index(name = "idx_wallet_wallet_id", columnList = "wallet_id", unique = true))
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "wallet_id", unique = true, nullable = false, length = 36)
    private String walletId;
    
    @Column(name = "balance_in_minor_units", nullable = false)
    private Long balanceInMinorUnits;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;
    
    @PrePersist
    protected void onCreate() {
        if (walletId == null) {
            walletId = UUID.randomUUID().toString();
        }
        if (balanceInMinorUnits == null) {
            balanceInMinorUnits = 0L;
        }
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public String getWalletId() {
        return walletId;
    }

    public Long getBalanceInMinorUnits() {
        return balanceInMinorUnits;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public Money getBalance() {
        return Money.ofMinorUnits(balanceInMinorUnits);
    }
    
    public void setBalance(Money balance) {
        this.balanceInMinorUnits = balance.getAmountInMinorUnits();
    }
    
    // Credits the wallet with the specified amount
    public void credit(Money amount) {
        Money newBalance = getBalance().add(amount);
        setBalance(newBalance);
    }
    
    // Debits the wallet with the specified amount (validates sufficient balance)
    public void debit(Money amount) {
        Money currentBalance = getBalance();
        if (currentBalance.isLessThan(amount)) {
            throw new IllegalStateException("Insufficient balance");
        }
        Money newBalance = currentBalance.subtract(amount);
        setBalance(newBalance);
    }
    
    // Processes a transaction using the Strategy pattern (CREDIT or DEBIT)
    public void processTransaction(TransactionType type, Money amount) {
        Money newBalance = type.apply(getBalance(), amount);
        setBalance(newBalance);
    }
    
    // Checks if wallet has sufficient balance for the given amount
    public boolean hasSufficientBalance(Money amount) {
        return getBalance().isGreaterThanOrEqual(amount);
    }
}
