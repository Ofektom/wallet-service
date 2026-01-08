package com.ofektom.model;

import com.ofektom.enums.TransactionType;
import com.ofektom.utils.Money;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Transaction entity representing a wallet transaction.
 * Records credit/debit operations and transfers for audit purposes.
 */
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transaction_wallet_id", columnList = "wallet_id"),
    @Index(name = "idx_transaction_transaction_id", columnList = "transaction_id", unique = true)
})
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "transaction_id", unique = true, nullable = false, length = 36)
    private String transactionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 10)
    private TransactionType transactionType;
    
    @Column(name = "amount_in_minor_units", nullable = false)
    private Long amountInMinorUnits;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public Transaction() {
    }
    
    public Transaction(Long id, String transactionId, Wallet wallet, TransactionType transactionType, Long amountInMinorUnits, LocalDateTime createdAt) {
        this.id = id;
        this.transactionId = transactionId;
        this.wallet = wallet;
        this.transactionType = transactionType;
        this.amountInMinorUnits = amountInMinorUnits;
        this.createdAt = createdAt;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public void setAmountInMinorUnits(Long amountInMinorUnits) {
        this.amountInMinorUnits = amountInMinorUnits;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @PrePersist
    protected void onCreate() {
        if (transactionId == null) {
            transactionId = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    public Wallet getWallet() {
        return wallet;
    }
    
    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }
    
    public TransactionType getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public Money getAmount() {
        return Money.ofMinorUnits(amountInMinorUnits);
    }
    
    public void setAmount(Money amount) {
        this.amountInMinorUnits = amount.getAmountInMinorUnits();
    }
    
    public static Transaction create(Wallet wallet, TransactionType type, Money amount) {
        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        return transaction;
    }
}

